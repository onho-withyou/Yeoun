package com.yeoun.messenger.dto;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;

import com.yeoun.messenger.repository.MsgStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MsgStatusDTO {
		private String empId;					// 사원 ID (EMP의 EMP_ID FK)
		private String avlbStat;				// 가용상태
		private LocalDateTime avlbUpdated;		// 가용상태 변경일시
		private String autoWorkStat;			// 자동 근무상태 (ATTENDANCE의 STATUS_CODE와 동기화)
		private String manualWorkStat;			// 수동 근무상태
		private LocalDateTime workStatUpdated;	// 근무상태 변경일시
		private String workStatSource;			// 근무상태 변경주체
		private String onlineYn;				// 접속 여부
		private LocalDateTime lastLogin;		// 마지막 접속시간
		private String remark;					// 비고
		
		private String empName;					// 사원 이름 (추가필드)
		private String deptName;				// 부서 이름 (추가필드)
		private String posName;					// 직무 이름 (추가필드)
		
		
		// ===========================================================
		// DTO <-> Entity 변환 메서드 구현
		
		private static ModelMapper modelMapper = new ModelMapper();
		
		public MsgStatus toEntity() {
			return modelMapper.map(this, MsgStatus.class);
		}
		
		public static MsgStatusDTO fromEntity(MsgStatus msgStatus) {
			return modelMapper.map(msgStatus, MsgStatusDTO.class);
		}
		
}






