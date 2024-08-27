package com.train.cloudDisk;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.train.cloudDisk.mapper.FileMapper;
import com.train.cloudDisk.service.FileService;
import jakarta.annotation.Resource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;
import java.io.IOException;

@MapperScan("com.train.cloudDisk.mapper")
@SpringBootApplication
public class CloudDiskApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudDiskApplication.class, args);
		//initDataBase();
	}

//	private static void initDataBase() {

//	}
}
