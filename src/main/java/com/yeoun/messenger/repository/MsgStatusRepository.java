package com.yeoun.messenger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MsgStatusRepository extends JpaRepository<MsgStatus, String> {

}
