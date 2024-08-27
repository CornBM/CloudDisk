package com.train.cloudDisk.mapper;

import com.train.cloudDisk.model.FileItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FileMapper extends BaseMapper<FileItem> {
    // 使用注解声明更新方法
    @Update("UPDATE file SET name = #{newName} WHERE name = #{oldName} AND path = #{path} AND user_id = #{userId}")
    int updateNameByKey(@Param("newName") String newName,
                        @Param("oldName") String oldName,
                        @Param("path") String path,
                        @Param("userId") String userId);

    // 使用注解声明更新路径的方法
    @Update("UPDATE file SET path = #{newPath} WHERE name = #{name} AND path = #{oldPath} AND user_id = #{userId}")
    int updatePathByKey(@Param("newPath") String newPath,
                        @Param("name") String name,
                        @Param("oldPath") String oldPath,
                        @Param("userId") String userId);

    @Update("UPDATE file SET type = #{newType} WHERE name = #{name} AND path = #{oldPath} AND user_id = #{userId}")
    int updateTypeByKey(@Param("newType") String newType,
                        @Param("name") String name,
                        @Param("oldPath") String oldPath,
                        @Param("userId") String userId);

    @Select("SELECT file.*, user.account_name as user_name " +
            "FROM share " +
            "LEFT OUTER JOIN file ON share.file_name = file.name AND share.file_path = file.path AND share.user_id = file.user_id " +
            "LEFT OUTER JOIN user ON file.user_id = user.id " +
            "WHERE (user.account_name like #{userName}) AND file.name LIKE CONCAT('%', #{fileName}, '%')")
    @Results({
            @Result(property = "user_name", column = "user_name")
    })
    List<FileItem> selectByDoubleName(@Param("fileName") String fileName, @Param("userName") String userName);

    @Select("SELECT * FROM file WHERE user_id = #{userId} AND path = #{path} AND name = #{name}")
    List<FileItem> selectByKey( @Param("name") String name, @Param("path") String path, @Param("userId") String userId);
}
