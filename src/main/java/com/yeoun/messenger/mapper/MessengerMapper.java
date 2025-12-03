package com.yeoun.messenger.mapper;

import java.util.List;

import com.yeoun.messenger.dto.MsgRoomListDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.messenger.dto.MsgStatusDTO;

@Mapper
@Repository
public interface MessengerMapper {
	
	List<MsgStatusDTO> selectUsers(@Param("id") String username);
	MsgRoomListDTO selectChat(@Param("empId") String empId, @Param("roomId") Long roomId);
	List<MsgRoomListDTO> selectChats(@Param("id") String username);

}
