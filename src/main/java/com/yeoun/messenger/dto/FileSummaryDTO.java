package com.yeoun.messenger.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FileSummaryDTO {
    private Long fileId;
    private String category;
    private String originFileName;
    private String fileName;
    private String filePath;

    /**
     * fileId : file_attach 테이블 PK
     * category : MIME 타입 (ex: image/png, text/plain, application/pdf)
     * originalFileName : 사용자가 올린 원본 파일명 (다운로드 버튼 만들 때 표시)
     * fileName : 저장된 파일명
     * filePath : 파일 경로
     */
}
