package com.train.cloudDisk.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.train.cloudDisk.tool.SizeSerializer;

import java.time.LocalDateTime;

@TableName("file")
public class FileItem {
    @TableField("name")
    private String name;
    @TableField("path")
    private String path;

    @TableField("created")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime created;

    @TableField("modified")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime modified;

    @TableField("size")
    @JsonSerialize(using = SizeSerializer.class)
    private long size;
    @TableField("user_id")
    private int user_id;
    @TableField("type")
    private String type;
    @TableField(exist = false)
    private String user_name;

    public FileItem(@JsonProperty("name") String name,
                    @JsonProperty("path") String path,
                    @JsonProperty("created") LocalDateTime created,
                    @JsonProperty("modified") LocalDateTime modified,
                    @JsonProperty("size") long size,
                    @JsonProperty("user_id") int user_id,
                    @JsonProperty("type") String type) {
        this.name = name;
        this.path = path;
        this.created = created;
        this.modified = modified;
        this.size = size;
        this.user_id = user_id;;
        this.type = type;
    }

    public FileItem() {
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getcreated() {
        return created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setcreated(LocalDateTime created) {
        this.created = created;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }


    public void setUser_id(int user_id){
        this.user_id = user_id;
    }

    public void setUser_name(String user_name){
        this.user_name = user_name;
    }

}