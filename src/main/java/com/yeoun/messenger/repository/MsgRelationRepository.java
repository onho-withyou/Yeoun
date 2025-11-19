package com.yeoun.messenger.repository;

import com.yeoun.messenger.entity.MsgRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MsgRelationRepository extends JpaRepository<MsgRelation, Long> {

	// 두 사람이 속한 개인 채팅방이 있는지 조회
    @Query("""
       SELECT r.roomId.roomId
       FROM MsgRelation r
       JOIN MsgRelation r2 
         ON r.roomId.roomId = r2.roomId.roomId
       WHERE r.empId.empId = :targetUser
         AND r2.empId.empId = :authUser
         AND r.roomId.groupYn = 'N'
       """)
    Long findOneToOneRoom(@Param("authUser")String authUser, @Param("targetUser")String targetUser);
    
    
    // 마지막 조회 메시지 업데이트
    @Modifying
    @Query("""
        UPDATE MsgRelation r 
        SET r.lastReadId = :lastReadId 
        WHERE r.empId = :empId 
        AND r.roomId = :roomId
    """)
    void updateLastRead(@Param("empId")String empId, @Param("roomId")Long roomId, @Param("lastReadId")Long lastReadId);

    // 방에 속한 모든 멤버를 조회
    List<MsgRelation> findByRoomId_RoomId(Long roomId);
    

}
