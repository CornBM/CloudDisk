package com.train.cloudDisk.tool;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.train.cloudDisk.mapper.FileMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Component
public class CommandLineRunnerImpl implements CommandLineRunner {

    @Resource
    private FileMapper fileMapper;

    @Value("${root_dir}")
    private String root;

    @Value("${isReloadDisk}")
    private boolean isReloadDisk;

    private FileUtils f;

    @Override
    public void run(String... args) throws Exception {
		// 将文件夹中已经的文件信息导入数据库
		//清空数据库中原有的文件信息
        if(!isReloadDisk)
            return;

        System.out.println("开始重载文件夹数据");
		f = new FileUtils(root, "",fileMapper);
		fileMapper.delete(new QueryWrapper<>());
		File rootFile = new File(root);
		File[] files = rootFile.listFiles();
		assert files!= null;
		for (File userFolder : files) {
			String userId = userFolder.getName();
			File[] userFiles = userFolder.listFiles();
			if (userFiles!= null) {
				try {
					for (File file : userFiles) {
						f.traverseDirectory(file, userId);
					}
				} catch (IOException e) {
					System.out.println("遍历"+userId+"文件夹失败");
				}
			}
		}
        System.out.println("重载文件夹数据完成");
    }
}
