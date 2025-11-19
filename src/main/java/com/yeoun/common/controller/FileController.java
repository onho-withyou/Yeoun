package com.yeoun.common.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.yeoun.common.dto.FileAttachDTO;
import com.yeoun.common.service.FileAttachService;
import com.yeoun.common.util.FileUtil;
import com.yeoun.main.service.ScheduleService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {
	private final FileAttachService fileAttachService;
	private final FileUtil fileUtil;
	
	@Value("${file.uploadBaseLocation}")
	private String uploadBaseLocation;
	
	// ---------------------------------------------------------------------
	
	@GetMapping("/download/{fileId}")
	public ResponseEntity<Resource> fileDownload(@PathVariable("fileId") Long fileId) {
//		System.out.println("요기왔다감");
		FileAttachDTO fileDTO = fileAttachService.getFile(fileId);
		
		try {
			//파일의 위치와 파일이름 결합하여 파일 특정
			Path path = Paths.get(uploadBaseLocation, fileDTO.getFilePath())
							.resolve(fileDTO.getFileName())
							.normalize();
			
			// 파일 존재여부 확인
			if(Files.notExists(path)) { // 경로 및 파일이 존재하지 않을 경우
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "다운로드 할 파일이 존재하지 않습니다.");
			} else if(!Files.isReadable(path)) { // 경로 및 파일은 존재하나, 파일 읽기 권한이 없는 경우
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "파일 접근 권한이 없습니다.");
			}
			
			// 리턴할 resource 객체 생성
			Resource resource = new UrlResource(path.toUri());
			
			String contentType = Files.probeContentType(path);
			
			// 파일의 타입 확인후 존재하지 않을경우 기본타입지정
			if(contentType == null) {
				contentType = MediaType.APPLICATION_OCTET_STREAM.toString();
			}
			
			// 한글,공백 포함된 파일 처리 필요
			ContentDisposition contentDisposition = ContentDisposition.builder("attachment") 
					.filename(fileDTO.getOriginFileName(), StandardCharsets.UTF_8) 
					.build(); // 객체 생성
			
			return ResponseEntity.ok() 
					.contentType(MediaType.parseMediaType(contentType)) 
					.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
					.body(resource); // 본문데이터는 파일을 감싼 Resource 객체 전달 
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "다운로드 할 파일이 존재하지 않습니다!");
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 타입 식별 에러!");
		}
		
	}
	
}
