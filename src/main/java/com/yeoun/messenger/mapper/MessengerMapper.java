package com.yeoun.messenger.mapper;

import java.util.List;

import com.yeoun.messenger.dto.MsgRoomDTO;
import com.yeoun.messenger.entity.MsgRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.messenger.dto.MsgStatusDTO;

@Mapper
@Repository
public interface MessengerMapper {
	
	List<MsgStatusDTO> selectUsers(@Param("id") String username);
	List<MsgRoomDTO> selectChats(@Param("id") String username);

}
