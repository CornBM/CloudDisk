package com.train.cloudDisk.controller;

import com.train.cloudDisk.mapper.UserMapper;
import com.train.cloudDisk.model.FileItem;
import com.train.cloudDisk.model.FileParams;
import com.train.cloudDisk.model.Response;
import com.train.cloudDisk.service.FileService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;

@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    @Resource
    private UserMapper userMapper;

    @CrossOrigin(origins = "*")
    @PostMapping("/upload")
    public Response<String> upload(MultipartHttpServletRequest request, @RequestParam("path") String path) {
        String id = (String) request.getAttribute("id");
        List<MultipartFile> files = request.getFiles("file");
        return fileService.upload(files, id, path.replace("/", "\\"));
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/download")
    public ResponseEntity<InputStreamResource> download(HttpServletRequest request, @RequestBody FileParams<String> params) {
        String path = params.getPath();
        String id = (String) request.getAttribute("id");
        String category = params.getCategory();
        return fileService.download(id, path, category);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/file")
    public Response<List<FileItem>> file(HttpServletRequest request, @RequestBody FileParams<String> params){
        String path = params.getPath();
        String id = (String) request.getAttribute("id");
        System.err.println("id:" + id + "  path:"+path);
        return fileService.file(id, path);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/delete")
    public Response<String> delete(HttpServletRequest request, @RequestBody FileParams<List<String>> params) {
        List<String> path = params.getPath();
        String id = (String) request.getAttribute("id");
        return fileService.delete(id, path);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/rename")
    public Response<String> rename(HttpServletRequest request, @RequestBody FileParams<String> params) {
        String path = params.getPath();
        String name = params.getName();
        String id = (String) request.getAttribute("id");
        System.err.println("rename id:" + id + "  path:" + path + "  name:" + name);
        return fileService.rename(id, path, name);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/mkdir")
    public Response<String> mkdir(HttpServletRequest request, @RequestBody FileParams<String> params) {
        String path = params.getPath();
        String name = params.getName();
        String id = (String) request.getAttribute("id");
        return fileService.mkdir(id, path, name);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/copy")
    public Response<String> copy(HttpServletRequest request, @RequestBody FileParams<List<String>> params){
        List<String> path = params.getPath();
        String id = (String) request.getAttribute("id");
        return fileService.copy(id, path);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/move")
    public Response<String> move(HttpServletRequest request, @RequestBody FileParams<List<String>> params) {
        List<String> path = params.getPath();
        String id = (String) request.getAttribute("id");
        return fileService.move(id, path);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/search")
    public Response<List<FileItem>> search(HttpServletRequest request, @RequestBody FileParams<String> params) {
        String category = params.getCategory();
        String name = params.getName();
        String path = params.getPath();
        String id = (String) request.getAttribute("id");
        return fileService.search(id, name, path, category);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/share")
    public Response<String> share(HttpServletRequest request, @RequestBody FileParams<List<String>> params){
        List<String> names = params.getPath();
        System.err.println("names:" + names.size());
        String path = params.getName();
        String id = (String) request.getAttribute("id");
        return fileService.share(id, path, names);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/unshare")
    public Response<String> unshare(HttpServletRequest request, @RequestBody FileParams<List<String>> params){
        List<String> names = params.getPath();
        String path = params.getName();
        String id = (String) request.getAttribute("id");
        return fileService.cancelShare(id, path, names);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/searchShare")
    public Response<List<FileItem>> searchShare(HttpServletRequest request, @RequestBody FileParams<String> params) {
        String userName = params.getUser_name();
        String fileName = params.getName();
        String id = (String) request.getAttribute("id");
        return fileService.searchShare(id, fileName, userName);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/downloadShare")
    public ResponseEntity<InputStreamResource> downloadShare(HttpServletRequest request, @RequestBody FileParams<String> params) {
        String path = params.getPath();
        String user_name = params.getUser_name();
        String id = userMapper.findByUsername(user_name).getId() + "";
        return fileService.download(id, path, null);
    }
}
