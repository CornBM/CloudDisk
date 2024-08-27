package com.train.cloudDisk.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("share")
public class Share {
    @TableField("file_name")
    private String file_name;
    @TableField("file_path")
    private String file_path;
    @TableField("user_id")
    private int user_id;

    public Share(String file_name, String file_path, String user_id) {
        this.file_name = file_name;
        this.file_path = file_path;
        this.user_id = Integer.parseInt(user_id);
    }
    public String getFile_name() {
        return file_name;
    }
    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }
    public String getFile_path() {
        return file_path;
    }
    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }
    public int getUser_id() {
        return user_id;
    }
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
