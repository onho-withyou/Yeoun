package com.yeoun.messenger.repository;

import com.yeoun.messenger.entity.MsgMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MsgMessageRepository extends JpaRepository<MsgMessage, Long> {

    List<MsgMessage> findByRoomId_RoomIdOrderBySentDate(Long roomId);

}
