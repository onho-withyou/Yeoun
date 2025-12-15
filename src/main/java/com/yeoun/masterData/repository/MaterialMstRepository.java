package com.yeoun.masterData.repository;

import java.util.List;
import java.util.Map;
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
			SELECT
				    m.*,
				    ec.emp_name AS created_by_name,
				    eu.emp_name AS updated_by_name
				FROM
				    MATERIAL_MST m
				LEFT JOIN
				    EMP ec
				ON
				    m.created_id = ec.emp_id
				LEFT JOIN
				    EMP eu
				ON
				    m.updated_id = eu.emp_id
			WHERE (:matId IS NULL OR :matId = '' OR m.MAT_ID LIKE '%' || :matId || '%')
			AND (:matName IS NULL OR :matName = '' OR m.MAT_NAME LIKE '%' || :matName || '%')
			AND (m.USE_YN = 'Y')
			""", nativeQuery = true)
	List<Map<String, Object>> findByMatIdList(@Param("matId") String matId, @Param("matName") String matName);

}
