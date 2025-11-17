package com.yeoun.messenger.repository;

import com.yeoun.messenger.entity.MsgRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MsgRelationRepository extends JpaRepository<MsgRelation, Long> {

    @Query("""
       SELECT r.roomId.roomId
       FROM MsgRelation r
       JOIN MsgRelation r2 
         ON r.roomId.roomId = r2.roomId.roomId
       WHERE r.empId.empId = :targetUser
         AND r2.empId.empId = :authUser
         AND r.roomId.groupYn = 'N'
       """)
    Long findOneToOneRoom(String authUser, String targetUser);

}
