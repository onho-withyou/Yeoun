package com.yeoun.messenger.repository;

import com.yeoun.messenger.entity.MsgMessage;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MsgMessageRepository extends JpaRepository<MsgMessage, Long> {

    List<MsgMessage> findByRoomId_RoomIdOrderBySentDate(Long roomId);
	
	// 메시지 내용 검색
	@Query("select distinct m.roomId.roomId from MsgMessage m " +
		       "where m.msgContent like %:keyword%")
	List<Long> findRoomIdByMessageContent(@Param("keyword") String keyword);

	// 검색어를 포함한 메시지 1개 찾기
	@Query("""
		select m.msgContent
		from MsgMessage m
		where m.roomId.roomId = :roomId
		  and m.msgContent like %:keyword%
		order by m.sentDate desc
		fetch first 1 rows only
		""")
	String findMatchedMessage(@Param("roomId") Long roomId,
							  @Param("keyword") String keyword);

	// 가장 최근 메시지 1개 찾기
	@Query("""
		select m.msgId
		from MsgMessage m
		where m.roomId.roomId = :roomId
		order by m.sentDate desc
		fetch first 1 rows only
		""")
	Long findLastMessage(@Param("roomId") Long roomId);

	// 특정 메시지 시간
	@Query("""
		select m.sentDate 
		from MsgMessage m
		where m.roomId.roomId = :roomId
		order by m.sentDate desc
		fetch first 1 rows only
		""")
	LocalDateTime findSentDate(@Param("roomId") Long roomId);



}
