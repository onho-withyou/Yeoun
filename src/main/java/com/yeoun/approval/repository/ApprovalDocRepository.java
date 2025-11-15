package com.yeoun.approval.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.approval.entity.ApprovalDoc;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;

import java.util.List;
import java.util.Optional;


@Repository
public interface ApprovalDocRepository extends JpaRepository<ApprovalDoc, Long> {
	Optional<ApprovalDoc> findByEmpId(String empId);
	
	// 사원 목록 조회
	@Query("SELECT m FROM Emp m")
    List<Emp> findAllMember();
	
	//부서목록조회
	@Query("SELECT d FROM Dept d")
	List<Dept> findAllDepartments();

	// 그리드 - 1.결재사항 - 진행해야할 결재만 - 결재권한자만 볼수있음
	// 그리드 - 2.전체결재- 나와관련된 모든 결재문서
	@Query(value="""
			SELECT adr.approvalId
       				,adr.approvalTitle
       				,adr.empId
       				,e.empName
       				,e.deptId
       				,d.deptName
       				,adr.approver
       				,p.posCode
       				,p.posName
       				,adr.createdDate
       				,adr.finishDate
       				,adr.docStatus
			FROM Emp e, Dept d, Position p,
				( SELECT DISTINCT
    					ad.approvalId
    					,ad.approvalTitle
    					,ad.empId
    					,ad.approver
    					,ad.createdDate
    					,ad.finishDate
    					,ad.docStatus
				FROM ApprovalDoc ad, Approver ar
				WHERE (ad.approver = ar.empId AND ar.viewing = 'y') OR ad.empId = :empId) adr
			WHERE e.empId = adr.empId 
			AND e.deptId = d.deptId
			AND e.posCode = p.posCode
			""", nativeQuery = true)	
	List<Object[]> findAllApprovalDocs(@Param("empId") String empId);

	// 그리드 - 3.내결재목록 - 내가 올린결재목록
	// 그리드 - 4.결재대기 - 나와관련된 모든 결재대기문서
	// 그리드 - 5.결재완료 - 결재권한자가 결재를 완료하면 볼수 있음(1차,2차,3차 모든결재 완료시)

			

}
