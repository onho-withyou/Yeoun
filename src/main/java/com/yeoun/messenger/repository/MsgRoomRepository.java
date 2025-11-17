package com.yeoun.messenger.repository;

import com.yeoun.messenger.entity.MsgRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MsgRoomRepository extends JpaRepository<MsgRoom, Long> {


}
