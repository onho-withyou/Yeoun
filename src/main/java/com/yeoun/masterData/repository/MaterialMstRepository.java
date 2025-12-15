package com.yeoun.masterData.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yeoun.masterData.entity.MaterialMst;

public interface MaterialMstRepository extends JpaRepository<MaterialMst, String> {

	// 원자재 조회
	Optional<MaterialMst> findByMatId(String materialOrder);

	List<MaterialMst> findByMatType(String matType);
	
	List<MaterialMst> findByMatTypeAndUseYn(String matType, String useYn);

	@Query(value = """
			SELECT *
			FROM MATERIAL_MST m
			-- matId 또는 matName이 비어있거나 NULL이면 전체조회, 그렇지 않으면 포함(부분일치) 검색
			WHERE (:matId IS NULL OR :matId = '' OR m.MAT_ID LIKE '%' || :matId || '%')
			AND (:matName IS NULL OR :matName = '' OR m.MAT_NAME LIKE '%' || :matName || '%')
			AND (m.USE_YN = 'Y')
			""", nativeQuery = true)
	List<MaterialMst> findByMatIdList(@Param("matId") String matId, @Param("matName") String matName);

}
