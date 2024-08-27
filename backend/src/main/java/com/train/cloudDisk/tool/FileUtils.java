package com.train.cloudDisk.tool;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.train.cloudDisk.mapper.FileMapper;
import com.train.cloudDisk.model.FileItem;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FileUtils {
    private final FileMapper fileMapper;

    private final String root;
    private final String temp;

    public FileUtils(String root, String temp,FileMapper fileMapper) {
        this.root = root;
        this.temp = temp;
        this.fileMapper = fileMapper;
    }
    // 定义日期时间格式
    public final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");

    public String formatDateTime(FileTime time){
        return time.toInstant().atZone(ZoneId.systemDefault()).format(formatter);
    }



    public FileItem getItemFromFile(int userId, File file, String path, boolean isDatabase) throws IOException {
        BasicFileAttributes b = Files.readAttributes(
                file.toPath(),
                BasicFileAttributes.class);

        return new FileItem(
                file.getName(),
                path,
                LocalDateTime.ofInstant(b.creationTime().toInstant(), ZoneId.systemDefault()),
                LocalDateTime.ofInstant(b.lastModifiedTime().toInstant(), ZoneId.systemDefault()),
                b.size(),
                userId,
                getCategory(file)
        );
    }

    public String getCategory(File file) {
        if (file == null) {
            return "Invalid file";
        }
        if (file.isDirectory()){
            return "folder";
        }
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == name.length() - 1) {
            return "else";
        }

        String extension = name.substring(dotIndex + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg", "png", "gif", "bmp", "tiff" -> "pic";
            case "mp4", "avi", "mov", "mkv", "flv", "wmv" -> "vid";
            case "zip", "rar", "tar", "gz", "7z", "bz2" -> "com";
            default -> "else";
        };
    }

    public void deleteDirectoryLegacyIO(File file, String userId) throws IOException {
        File[] list = file.listFiles();  //无法做到list多层文件夹数据
        String currentPath = file.getParent();
        if (list != null) {
            for (File temp : list) {     //先去递归删除子文件夹及子文件
                deleteDirectoryLegacyIO(temp, userId);   //注意这里是递归调用
            }
        }
        if(!file.delete())   //再删除自己
            throw new IOException("Failed to delete " + file.getAbsolutePath());
        else{
            // 修改数据库
            QueryWrapper<FileItem> queryWrapper = new QueryWrapper<FileItem>();
            String pathStr = Path.of(currentPath.replace(new File(root,userId).getPath(), "")).normalize().toString();
            queryWrapper.eq("name", file.getName());
            queryWrapper.eq("path", pathStr.isEmpty() ? "\\" : pathStr);
            queryWrapper.eq("user_id", Integer.parseInt(userId));
            System.err.println("delete file: "+file.getName()+" path: "+pathStr+" user_id: "+userId);
            fileMapper.delete(queryWrapper);
        }
    }

    //递归遍历目录及其子目录，计入数据库
    public void traverseDirectory(File file, String userId) throws IOException {
        File[] list = file.listFiles();  //无法做到list多层文件夹数据
        String currentPath = file.getParent();
        if (list != null) {
            for (File temp : list) {     //先去递归遍历子文件夹及子文件
                traverseDirectory(
                        temp,
                        userId);   //注意这里是递归调用
            }
        }

        File rootFile = new File(root, userId);
        String relativePath = currentPath.replace(rootFile.getPath(), "");
        // 保存文件信息到数据库
        fileMapper.insert(
                getItemFromFile(
                        Integer.parseInt(userId),
                        file,
                        Path.of(relativePath.isEmpty() ? relativePath + "\\" : relativePath).normalize().toString(),
                        true));
    }

    public File getThumbnail(File file, String category) throws IOException {
        String fileName = file.getPath().replace("\\", "").replace("/", "") + ".jpg";
        if(category.equals("pic")){
            Thumbnails.of(file)
                    .height(200)
                    .outputQuality(0.5f)
                    .toFile(new File(temp, fileName));
            file = new File(temp, fileName);
            return file;
        }else if(category.equals("vid")){
            return file;
        }
        else{
            return file;
        }
    }
}
