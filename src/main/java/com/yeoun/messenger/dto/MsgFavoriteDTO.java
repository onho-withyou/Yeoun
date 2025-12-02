package com.yeoun.messenger.dto;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;

import com.yeoun.emp.entity.Emp;
import com.yeoun.messenger.entity.MsgFavorite;
import com.yeoun.messenger.entity.MsgFavoriteId;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MsgFavoriteDTO {
    private String empId;	// 즐겨찾기 추가한 사용자의 ID
    private String fvUser;	// 즐겨찾기 추가된 사용자의 ID
    private LocalDateTime createdDate;	// 즐겨찾기 추가한 시간
    
    // ===========================================================
 	// DTO <-> Entity 변환 메서드 구현
 		
 	private static ModelMapper modelMapper = new ModelMapper();
 	
 	public MsgFavorite toEntity(Emp empId, Emp fvUser) {
 		
 		MsgFavoriteId id = new MsgFavoriteId(
 			empId.getEmpId(),
 			fvUser.getEmpId()
 		);
 		
 		MsgFavorite msgFavorite = new MsgFavorite();
 		msgFavorite.setId(id);
 		msgFavorite.setEmpId(empId);
 		msgFavorite.setFvUser(fvUser);
 		
 		return msgFavorite;
 	}
 	
 	public static MsgFavoriteDTO fromEntity(MsgFavorite msgFavorite) {
 		return modelMapper.map(msgFavorite, MsgFavoriteDTO.class);
 	}
 	
 	
}








