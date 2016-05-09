package com.yimeicloud.study.file_upload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FileUploadController {
	
	Logger logger = LoggerFactory.getLogger(FileUploadController.class);
	
	// 20KB
	private static long MAX_SIZE = 20 * 1024;
	// .txt .doc
	private static List<String> LIMIT_TYPE = Arrays.asList("text/plain", "application/msword");
	
	@RequestMapping(method=RequestMethod.GET, value="/")
	public String provideUploadInfo(Model model) {
		// get exists files
		File rootDir = new File(Application.ROOT);
		File[] files = rootDir.listFiles();
		// file exist check
		if(null != files && files.length > 0) {
			logger.info("exists files:" + files.length);
			List<String> fileNames = new ArrayList<String>();
			
			for(File file : files) {
				fileNames.add(file.getName());
			}
			model.addAttribute("files", fileNames);
		} else {
			logger.info("no file exists");
		}
		
		return "uploadForm";
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/")
	public String handleFileUpload(@RequestParam("name") String name, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		
		// save name check
		if(name.contains("/")) {
			logger.info("Folder separators not allowed");
			redirectAttributes.addFlashAttribute("message", "Folder separators not allowed");
			return "redirect:/";
		}
		// file empty check
		if(!file.isEmpty()) {
			logger.info("file type:" + file.getContentType());
			// file type check
			if(!LIMIT_TYPE.contains(file.getContentType())) {
				logger.info("file type incorrect!");
				redirectAttributes.addFlashAttribute("message", "file type incorrect!");
				return "redirect:/";
			}
			
			logger.info("file size:" + file.getSize());
			// file size check
			if(file.getSize() > MAX_SIZE) {
				logger.info("file is too max");
				redirectAttributes.addFlashAttribute("message", "file is too max");
				return "redirect:/";
			}
			
			// save file
			try {
				OutputStream fileStream = new FileOutputStream(new File(Application.ROOT + "/" + name));
				FileCopyUtils.copy(file.getInputStream(), fileStream);
				
				fileStream.close();
				logger.info("You successfully uploaded " + name + "!");
				redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + name + "!");
			} catch (IOException e) {
				logger.info("You failed to upload " + name + " => " + e.getMessage());
				redirectAttributes.addFlashAttribute("message", "You failed to upload " + name + " => " + e.getMessage());
			}
		} else {
			logger.info("You failed to upload " + name + " because the file was empty");
			redirectAttributes.addFlashAttribute("message", "You failed to upload " + name + " because the file was empty");
		}
		
		return "redirect:/";
	}
	
	@RequestMapping(method=RequestMethod.GET, value="download/{fileId}")
	public HttpServletResponse download(@PathVariable(value="fileId") int fileId, HttpServletResponse response, RedirectAttributes redirectAttributes) {
		// get exists files
		File rootDir = new File(Application.ROOT);
 		File[] files = rootDir.listFiles();
		
		File file = files[fileId];
		
		try {
			InputStream fis = new BufferedInputStream(new FileInputStream(file));
			byte[] buffer = new byte[fis.available()];
			fis.read(buffer);
			
			response.reset();
			// 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + file.getName());
            response.addHeader("Content-Length", "" + file.length());
            response.setContentType("application/octet-stream");
            
			OutputStream os = new BufferedOutputStream(response.getOutputStream());
			os.write(buffer);
			
			os.flush();
			os.close();
			fis.close();
			logger.info("download success!");
		} catch (IOException e) {
			logger.info("error happened when download file:" + e.getMessage());
		}
		return response;
	}
}
