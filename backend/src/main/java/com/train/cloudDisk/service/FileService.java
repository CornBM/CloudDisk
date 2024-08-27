package com.train.cloudDisk.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.train.cloudDisk.config.Code;
import com.train.cloudDisk.mapper.FileMapper;
import com.train.cloudDisk.mapper.ShareMapper;
import com.train.cloudDisk.mapper.UserMapper;
import com.train.cloudDisk.model.FileItem;
import com.train.cloudDisk.model.Response;
import com.train.cloudDisk.model.Share;
import com.train.cloudDisk.model.User;
import com.train.cloudDisk.tool.FileUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class FileService {
    @Resource
    private FileMapper fileMapper;

    @Resource
    private ShareMapper shareMapper;

    @Resource
    private UserMapper userMapper;

    @Value("${root_dir}")
    private String root;

    @Value("${temp_dir}")
    private String temp;

    private FileUtils f;

    @PostConstruct
    private void init() {
        f = new FileUtils(root, temp,fileMapper);
    }

    public Response<List<FileItem>> file(String id, String path) {
        File dir = new File(new File(root, id), path);
        if (dir.exists() && dir.isDirectory()) {
            // 遍历目录
            File[] files = dir.listFiles();
            List<FileItem> fileItems = new ArrayList<>();
            try {
                assert files != null;
                for (File file : files) {
                    fileItems.add(f.getItemFromFile(Integer.parseInt(id), file, path, false));
                }
            } catch (IOException | NullPointerException e) {
                System.err.println("路径不存在！"+ dir.toPath());
                return new Response<>("404", "路径不存在！", null);
            }

            return new Response<>(Code.SUCCESS, "当前目录: "+ path + "！", fileItems);

        }
        else{
            System.err.println("路径不存在！"+ dir.toPath());
            return new Response<>("404", "路径不存在！", null);
        }
    }

    public Response<String> upload(List<MultipartFile> files, String id, String path) {
        String rootPath = new File(new File(root, id), path).getPath();
        MultipartFile file = null;
        BufferedOutputStream stream = null;
        for (MultipartFile multipartFile : files) {
            file = multipartFile;
            if (!file.isEmpty()) {
                try {
                    File tempFile = new File(rootPath, Objects.requireNonNull(file.getOriginalFilename()));
                    String filePath = Path.of(path).normalize().toString();
                    System.err.println("收到文件：" + tempFile.toPath());
                    while(!fileMapper.selectByKey(tempFile.getName(), filePath, id).isEmpty()){
                        String[] name = tempFile.getName().split("\\.");
                        if(name.length == 1)
                            tempFile = new File(temp, name[0] + "_副本");
                        else
                            tempFile = new File(rootPath, name[0] + "_副本."+ name[1]);
                    }
                    file.transferTo(tempFile.getAbsoluteFile());
                    // 保存文件信息到数据库
                    fileMapper.insert(
                            f.getItemFromFile(Integer.parseInt(id),
                                    tempFile,
                                    filePath,
                                    true));
                } catch (Exception e) {
                    e.printStackTrace();
                    return new Response<>(
                            "500",
                            "当前服务器状态异常！",
                            null);
                }
            } else {
                return new Response<>(
                        "500",
                        "文件:" + file.getOriginalFilename() + "为空！",
                        null);
            }
        }
        assert file != null;
        return new Response<>(Code.SUCCESS, "已上传: "+ file.getOriginalFilename() + "！", null);
    }

    public ResponseEntity<InputStreamResource> download(String id, String path, String category) {
        try {
            // 指定BLOB文件的路径
            File file = new File(new File(root, id), path);
            if (category != null) {
                if(category.equals("pic") || category.equals("vid"))
                    file = f.getThumbnail(file, category);
                else
                    System.err.println("download path:"+path);
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            // 设置HTTP头信息
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream"); // 根据实际文件类型设置

            // 创建ResponseEntity对象
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length()) // 设置内容长度
                    .body(resource);
        } catch (Exception e) {
            System.err.println("下载失败！");
            // 处理文件未找到的情况
            return ResponseEntity.notFound().build();
        }
    }

    public Response<String> delete(String id, List<String> path) {
        for (String p : path) {
            File file = new File(new File(root, id), p);
            System.err.println("delete  id:" + id + "  path:" + p);
            if (!file.exists())
                return new Response<>(Code.FAILURE, "文件不存在！", null);
            try {
                f.deleteDirectoryLegacyIO(file, id);
            } catch (IOException e) {
                System.err.println("删除失败！" + file.getPath());
                return new Response<>(Code.FAILURE, "当前服务器状态异常！", null);
            }
        }
        return new Response<>(Code.SUCCESS, "已删除: "+ path + "！", null);
    }

    public Response<String> rename(String id, String path, String name) {
        File file = new File(new File(root, id), path);
        String oldName = file.getName();
        Path filePath = file.toPath();
        if (!file.exists())
            return new Response<>(Code.FAILURE, "文件不存在！", null);
        if (!file.isFile())
            return new Response<>(Code.FAILURE, "不是文件！", null);
        try {
            // 修改数据库
            Path parent = Path.of(path.replace(oldName, ""));
            String pathStr = parent.normalize().toString();
            QueryWrapper<FileItem> wrapper = new QueryWrapper<>();
            wrapper.eq("name", oldName);
            wrapper.eq("path", pathStr);
            wrapper.eq("user_id", Integer.parseInt(id));
            var fileItem = fileMapper.selectOne(wrapper);
            System.err.println("修改数据库:" + pathStr +" "+ oldName + "->" + name);
            fileMapper.updateNameByKey(name, fileItem.getName(), pathStr, id);
            fileMapper.updateTypeByKey(f.getCategory(new File(name)), name, pathStr, id);
            Files.move(filePath, filePath.resolveSibling(name));
        } catch (IOException e) {
            System.err.println("重命名失败！" + filePath);
            return new Response<>(Code.FAILURE, "当前服务器状态异常！", null);
        }
        return new Response<>(Code.SUCCESS, "已经更名为: "+ name + "！", null);
    }

    public Response<String> mkdir(String id, String path, String name) {
        File dir = new File(new File(root, id), path);
        if (!dir.exists()) {
            return new Response<>(Code.FAILURE, "目录不存在！", null);
        }
        if (dir.isFile()) {
            return new Response<>(Code.FAILURE, "不是目录！", null);
        }
        File newDir = new File(dir, name);
        if (newDir.exists()) {
            return new Response<>(Code.FAILURE, "目录已存在！", null);
        }
        if (!newDir.mkdir())
            return new Response<>(Code.FAILURE, "当前服务器状态异常！", null);

        // 保存文件信息到数据库
        try {
            fileMapper.insert(
                    f.getItemFromFile(Integer.parseInt(id),
                            newDir,
                            Path.of(path).normalize().toString(),
                            true));
            return new Response<>(Code.SUCCESS, "创建目录成功！", null);
        } catch (IOException e) {
            System.err.println("创建目录到数据库失败！" + newDir.toPath());
            return new Response<>(Code.FAILURE, "当前服务器状态异常！", null);
        }
    }

    public Response<String> copy(String id, List<String> path) {
        String destPath = path.getLast();
        File destDirFile = new File(new File(root, id), destPath);
        if (!destDirFile.exists() || !destDirFile.isDirectory()) {
            return new Response<>(Code.FAILURE, "目标目录不存在！", null);
        }
        path.removeLast();
        for (String p : path) {
            File sourceFile = new File(new File(root, id), p);
            if (!sourceFile.exists()) {
                return new Response<>(Code.FAILURE, "源文件不存在！", null);
            }
            try {
                File destFile = new File(destDirFile, sourceFile.getName());
                String filePath = Path.of(destPath).normalize().toString();
                while(!fileMapper.selectByKey(destFile.getName(), filePath, id).isEmpty()){
                    String[] name = destFile.getName().split("\\.");
                    if(name.length == 1)
                        destFile = new File(destDirFile, name[0] + "_副本");
                    else
                        destFile = new File(destDirFile, name[0] + "_副本."+ name[1]);
                }
                System.err.println("复制：" + p + " -> " + destFile.toPath());
                Files.copy(sourceFile.toPath(), destFile.toPath());
                // 保存文件信息到数据库
                fileMapper.insert(
                        f.getItemFromFile(
                                Integer.parseInt(id),
                                destFile,
                                filePath,
                                true));
            } catch (IOException e) {
                System.err.println("复制失败！" + sourceFile.toPath());
                return new Response<>(Code.FAILURE, "当前服务器状态异常！", null);
            }
        }
        return new Response<>(Code.SUCCESS, "已复制到"+ destPath + "！", null);
    }

    public Response<String> move(String id, List<String> path) {
        String destPath = path.getLast();
        File destDirFile = new File(new File(root, id), destPath);
        if (!destDirFile.exists() || !destDirFile.isDirectory()) {
            return new Response<>(Code.FAILURE, "目标目录不存在！", null);
        }
        path.removeLast();
        for (String p : path) {
            File sourceFile = new File(new File(root, id), p);
            if (!sourceFile.exists()) {
                return new Response<>(Code.FAILURE, "源文件不存在！", null);
            }
            try {
                System.err.println("移动：" + p + " -> " + destDirFile.toPath());
                String oldPath = Path.of(p.replace(sourceFile.getName(), "")).normalize().toString();
                // 修改数据库
                QueryWrapper<FileItem> wrapper = new QueryWrapper<>();
                wrapper.eq("name", sourceFile.getName());
                wrapper.eq("path", oldPath);
                FileItem fileItem = fileMapper.selectList(wrapper).getFirst();
                String newPath = Path.of(destPath).normalize().toString();
                while(!fileMapper.selectByKey(fileItem.getName(), newPath, id).isEmpty()){
                    String[] name = fileItem.getName().split("\\.");
                    if(name.length == 1)
                        fileItem.setName(name[0] + "_副本");
                    else
                        fileItem.setName(name[0] + "_副本."+ name[1]);

                }
                fileMapper.updatePathByKey(
                        newPath,
                        fileItem.getName(),
                        oldPath,
                        id);

                File destFile = new File(destDirFile, fileItem.getName());
                Files.move(sourceFile.toPath(), destFile.toPath());
            } catch (IOException e) {
                System.err.println("移动失败！" + sourceFile.toPath());
                return new Response<>(Code.FAILURE, "当前服务器状态异常！", null);
            }
        }
        return new Response<>(Code.SUCCESS, "已移动到: "+ destPath + "！", null);
    }

    public Response<List<FileItem>> search(String id, String name, String path,String category) {
        String filePath = Path.of(path).normalize().toString();
        System.err.println("搜索：" + name + " " + filePath + " " + category);
        QueryWrapper<FileItem> wrapper = new QueryWrapper<>();
        if(name != null && !name.isEmpty())
            wrapper.like("name", name);
        if(category!= null) {
            if(!category.equals("all"))
                wrapper.eq("type", category);
            else
                wrapper.ge("path", filePath);
        }
        wrapper.eq("user_id", Integer.parseInt(id));
        List<FileItem> fileItems=null;
        try {
            fileItems = fileMapper.selectList(wrapper);
            if (fileItems.isEmpty()) {
                return new Response<>("404", "搜索结果为空！", null);
            }
        }
        catch (Exception e){
            return new Response<>(Code.FAILURE, "数据有误！", null);
        }
        return new Response<>(Code.SUCCESS, "已返回搜索结果", fileItems);
    }

    public Response<String> share(String id, String path, List<String> names) {
        String filePath = Path.of(path).normalize().toString();
        for(String name : names){
            try {
                System.err.println("分享：" + name + " " + filePath + " " + id);
                shareMapper.insert(new Share(name, filePath, id));
            }catch (Exception e){
                System.err.println("分享失败！" + name + " " + filePath + " " + id);
                return new Response<>(Code.FAILURE, "已经分享过！", null);
            }
        }
        return new Response<>(Code.SUCCESS, "已分享: "+ names + "！", null);
    }

    public Response<String> cancelShare(String id, String path,List<String> names) {
        String filePath = Path.of(path).normalize().toString();
        for (String name : names) {
            try {
                shareMapper.delete(
                        new QueryWrapper<Share>()
                                .eq("file_path", filePath)
                                .eq("file_name", name)
                                .eq("user_id", id));
            } catch (Exception e) {
                System.err.println("取消分享失败！" + filePath + " " + name + " " + id);
                return new Response<>(Code.FAILURE, "当前服务器状态异常！", null);
            }
        }
        return new Response<>(Code.SUCCESS, "已取消分享！", null);
    }

    public Response<List<FileItem>> searchShare(String id, String fileName, String userName) {
        if(fileName == null || fileName.isEmpty()){
            fileName = "";
            // '%filename%'
        }
        if(userName == null || userName.isEmpty()){
            userName = userMapper.selectById(Integer.parseInt(id)).getAccountName();
        }
        if(userName.equals("all")){
            userName = "%";
        }
        List<FileItem> fileItems = fileMapper.selectByDoubleName(fileName, userName);
        if (fileItems.isEmpty()) {
            return new Response<>("404", "搜索结果为空！", null);
        }
        return new Response<>(Code.SUCCESS, "已返回搜索结果！", fileItems);
    }

}
