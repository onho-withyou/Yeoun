package com.yeoun.emp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.entity.Dept;

@Repository
public interface DeptRepository extends JpaRepository<Dept, String> {
	
	// 활성화된 부서 목록 조회
	// => USE_YN = 'Y'인 부서 / 부서명 오름차순 정렬
	@Query("SELECT d FROM Dept d"
			+ " WHERE d.useYn = 'Y'"
			+ " ORDER BY d.deptName ASC")
	List<Dept> findActive();

}
