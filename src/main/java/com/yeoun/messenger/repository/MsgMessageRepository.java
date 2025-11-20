package com.yeoun.messenger.repository;

import com.yeoun.messenger.entity.MsgMessage;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MsgMessageRepository extends JpaRepository<MsgMessage, Long> {

    List<MsgMessage> findByRoomId_RoomIdOrderBySentDate(Long roomId);
	
	// 메시지 내용 검색
	@Query("select distinct m.roomId.roomId from MsgMessage m " +
		       "where m.msgContent like %:keyword%")
	List<Long> findRoomIdByMessageContent(@Param("keyword") String keyword);


}
