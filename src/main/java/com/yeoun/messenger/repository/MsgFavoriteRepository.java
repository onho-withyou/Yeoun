package com.yeoun.messenger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.messenger.entity.MsgFavorite;
import com.yeoun.messenger.entity.MsgFavoriteId;

@Repository
public interface MsgFavoriteRepository extends JpaRepository<MsgFavorite, MsgFavoriteId> {

	boolean existsByEmpId_EmpIdAndFvUser_EmpId(String empId, String fvUser);	// 즐겨찾기 여부 확인
	int deleteByEmpId_EmpIdAndFvUser_EmpId(String empId, String fvUser);		// 즐겨찾기 삭제

}
