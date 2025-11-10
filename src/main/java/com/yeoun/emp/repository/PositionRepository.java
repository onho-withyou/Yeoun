package com.yeoun.emp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.entity.Position;

@Repository
public interface PositionRepository extends JpaRepository<Position, String> {
	
	// 활성화된 직급 목록 조회
	// => USE_YN = 'Y'인 직급 / RANK_ORDER 순으로 오름차순 정렬
	@Query("SELECT p FROM Position p"
			+ " WHERE p.useYn = 'Y'"
			+ " ORDER BY p.rankOrder ASC")
	List<Position> findActive();

}
