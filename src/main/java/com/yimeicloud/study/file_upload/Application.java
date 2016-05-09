package com.yimeicloud.study.file_upload;

import java.io.File;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner{

	public static String ROOT = "upload-dir";
	
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

	@Override
	public void run(String... args) throws Exception {
		File root = new File(ROOT);
		if(!root.exists()) {
			root.mkdir();
		}
	}
}