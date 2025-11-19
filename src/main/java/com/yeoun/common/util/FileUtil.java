package com.yeoun.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.yeoun.common.dto.FileAttachDTO;
import com.yeoun.common.entity.FileAttach;

import lombok.extern.log4j.Log4j2;


@Component
@Log4j2
public class FileUtil {
	// 파일 업로드에 사용할 경로를 properties 파일에서 가져오기
	// => 변수 선언부에 @Value("${프로퍼티속성명}") 형태로 선언
	@Value("${file.uploadBaseLocation}")
	private String uploadBaseLocation;
	
	// ======================================================================
	// 파일업로드 인터페이스
	public interface FileUploadHelpper{
		String getTargetTable();
		Long getTargetTableId();
	}
	// ======================================================================
	// ======================================================================
	// 파일 업로드 후 List<FileAttachDTO> 반환
	public <T extends FileUploadHelpper> List<FileAttachDTO> uploadFile(T entity,List<MultipartFile> files) throws IOException {
		// ItemImg 엔티티 목록을 저장할 List<ItemImg> 객체 생성
		List<FileAttach> FileList = new ArrayList<>();
		
		// [ 파일 저장될 디렉토리 생성 ]
		LocalDate today = LocalDate.now(); 
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd"); 
		String subDir = today.format(dtf);
		
		// 파일 저장 경로에 대한 Path 객체 생성하고 해당 경로를 실제 서버상에 생성
		Path uploadDir = Paths.get(uploadBaseLocation, subDir).toAbsolutePath().normalize();
		
		// 생성된 Path 객체에 해당하는 디렉토리가 실제 서버상에 존재하지 않을 경우 새로 생성
		if(!Files.exists(uploadDir)) {
			Files.createDirectories(uploadDir); // 하위 경로를 포함한 경로 상의 모든 디렉토리 생성
		}
		// =======================================================================================
		// 업로드한 파일정보를 저장할 객체 생성
		List<FileAttachDTO> fileList = new ArrayList<FileAttachDTO>();
		// FileUploadHelpper를 상속한 엔티티의 getFiles()메서드의 리턴값 MultipartFile[]배열값 반복하여 파일 업로드
		for(MultipartFile mFile : files) {
			if(!mFile.isEmpty()) { // 파일이 존재할 때만 업로드 실행
				String originalFileName = mFile.getOriginalFilename(); //파일의 원본이름
				
				String uuid = UUID.randomUUID().toString();
				String realFileName = uuid + "_" + originalFileName; // 실제 저장되는 파일이름
				
				Path destinationPath = uploadDir.resolve(realFileName); //resolve메서드를 통해 경로 + 파일이름
				
				mFile.transferTo(destinationPath); //transferTo(Path) 메서드로 파일업로드
				
				// 파일DB 저장을위해 fileDTO에 파일정보 저장
				FileAttachDTO fileDTO = FileAttachDTO.builder()
										.refTable(entity.getTargetTable())
										.refId(Long.parseLong(entity.getTargetTableId().toString()))
										.category(mFile.getContentType())
										.fileName(realFileName)
										.originFileName(originalFileName)
										.filePath(subDir)
										.fileSize(mFile.getSize())
										.build();
				//fileList에 fileDTO 정보 추가
				fileList.add(fileDTO);
			}
		}
		// 업로드된 파일리스트 반환
		return fileList;
	}
	
	// ======================================================================
	// 실제 서버상에 업로드 된 파일 제거(단일 파일 삭제)
	public void deleteFile() {
		
//			Path path = Paths.get(uploadBaseLocation, itemImgDTO.getImgLocation()) // 기본 경로와 파일별 상세경로를 결합하여 Path 객체 생성
//					.resolve(itemImgDTO.getImgName()) // 디렉토리에 실제 파일명 결합(get() 메서드 파라미터에 추가로 기술해도 됨)
//					.normalize();
//			
//			// Files 클래스의 deleteIfExists() 메서드 호출하여 해당 파일이 서버상에 존재할 경우 삭제 처리
//			try {
//				Files.deleteIfExists(path);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
	}

	// 실제 서버상에 업로드 된 파일 제거(다중 파일 삭제)
	public void deleteFiles() {
		// deleteFile() 메서드 재사용하기 위하여 반복문을 통해 리스트 내의 ItemImgDTO 객체를 파라미터로 전달
//			for(ItemImgDTO itemImgDTO : itemImgDTOList) {
//				deleteFile(itemImgDTO);
//			}
	}
}
