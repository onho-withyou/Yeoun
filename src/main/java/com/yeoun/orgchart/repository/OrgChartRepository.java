package com.yeoun.orgchart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yeoun.emp.entity.Dept;

@Repository
public interface OrgChartRepository extends JpaRepository<Dept, String> {
	
	// 조직도 조회용
    @Query("""
        select 
            d.deptId as deptId,
            d.parentDeptId as parentDeptId,
            d.deptName as deptName,
            p.posName as posName,
            e.empName as empName,
            p.rankOrder as posOrder
        from Dept d
        left join Emp e 
    		on e.dept.deptId = d.deptId
    		and e.status = 'ACTIVE'
        left join Position p on e.position.posCode = p.posCode
        order by d.deptId, p.rankOrder desc, e.empName
    """)
	List<OrgNodeProjection> findOrgNodes();

}
