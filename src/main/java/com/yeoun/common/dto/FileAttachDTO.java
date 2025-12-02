package com.yeoun.common.dto;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;

import com.yeoun.common.entity.FileAttach;
import com.yeoun.notice.dto.NoticeDTO;
import com.yeoun.notice.entity.Notice;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class FileAttachDTO {
	private Long fileId;
	private String refTable;
	private Long refId;
	private String category;
	private String fileName;
	private String originFileName;
	private String filePath;
	private Long fileSize;
	private LocalDate createdDate;
	
	@Builder
	public FileAttachDTO(Long fileId, String refTable, Long refId, String category, String fileName,
			String originFileName, String filePath, Long fileSize, LocalDate createdDate) {
		super();
		this.fileId = fileId;
		this.refTable = refTable;
		this.refId = refId;
		this.category = category;
		this.fileName = fileName;
		this.originFileName = originFileName;
		this.filePath = filePath;
		this.fileSize = fileSize;
		this.createdDate = createdDate;
	}
	
	static ModelMapper modelMapper = new ModelMapper(); 
	
	public static FileAttachDTO fromEntity(FileAttach file) {
		
		return modelMapper.map(file, FileAttachDTO.class);
	}
	
	public FileAttach toEntity() {
		return modelMapper.map(this, FileAttach.class);
	}
	
}
