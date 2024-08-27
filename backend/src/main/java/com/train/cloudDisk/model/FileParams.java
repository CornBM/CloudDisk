package com.train.cloudDisk.model;

import java.util.List;

public class FileParams<T> {
    private String user_name;
    private String category;
    private T path;
    private String name;

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public T getPath() {
        return path;
    }

    public void setPath(T path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}