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
	// 열람권한이 있는지에대해생각해보기
	@Query(value = """
			SELECT 
				ROWNUM
				,ade.approval_id
				,ade.approval_title
				,ade.emp_id
				,ade.emp_name
				,ade.dept_id
				,ade.dept_name
				,ade.approver
				,e.emp_name AS approvar_name
				,ade.pos_code
				,ade.pos_name
				,ade.created_date
				,ade.finish_date
				,ade.doc_status
			FROM emp e
					,(SELECT
						ad.approval_id
						,ad.approval_title
						,ad.emp_id
						,e.emp_name
						,e.dept_id
						,d.dept_name
						,ad.approver
						,p.pos_code
						,p.pos_name
						,ad.created_date
						,ad.finish_date
						,ad.doc_status
					FROM approval_doc ad,emp e,dept d,position p
					WHERE ad.emp_id = e.emp_id 
					AND e.dept_id = d.dept_id
					AND e.pos_code = p.pos_code
					AND ad.approver = :empId ) ade
			WHERE ade.approver = e.emp_id	
				""", nativeQuery = true)
	List<Object[]> findPendingApprovalDocs(@Param("empId") String empId);

	// 그리드 - 2.전체결재- 나와관련된 모든 결재문서
	@Query(value = """
				SELECT  rownum AS rnum
						,adr.approval_id AS approvalId
       					,adr.approval_title AS approvalTitle
       					,adr.emp_id AS empId
       					,e.emp_name	AS empName
       					,e.dept_id	AS deptId
       					,d.dept_name AS deptName
       					,adr.approver AS approver
       					,p.pos_code  AS posCode
       					,p.pos_name  AS posName
       					,adr.created_date AS createdDate
       					,adr.finish_date AS finishDate
       					,adr.doc_status AS docStatus
				FROM emp e, dept d,position p,
					( SELECT distinct 
    					ad.approval_id
    					,ad.approval_title
    					,ad.emp_id
    					,ad.approver
    					,ad.created_date
    					,ad.finish_date
    					,ad.doc_status
					FROM approval_doc ad,approver ar
					WHERE (ad.approver = ar.emp_id AND ar.viewing = 'y') OR ad.emp_id = :empId ) adr
				WHERE e.emp_id = adr.emp_id 
				AND e.dept_id = d.dept_id
				AND e.pos_code = p.pos_code
				
				""", nativeQuery = true)	
	List<Object[]> findAllApprovalDocs(@Param("empId") String empId);

	// 그리드 - 3.내결재목록 - 내가 올린결재목록
	@Query(value = """
				SELECT ROWNUM
      				,ad.approval_id
      				,ad.approval_title
      				,ad.emp_id
      				,e.emp_name
      				,e.dept_id
      				,d.dept_name
      				,ad.approver
      				,p.pos_code
      				,p.pos_name
      				,ad.created_date
      				,ad.finish_date
      				,ad.doc_status
				FROM approval_doc ad, emp e, dept d, position p
				WHERE ad.emp_id = e.emp_id 
				AND e.dept_id = d.dept_id
				AND e.pos_code = p.pos_code
				AND e.emp_id = :empId
				
				""", nativeQuery = true)
	List<Object[]> findMyApprovalDocs(@Param("empId") String empId);
	// 그리드 - 4.결재대기 - 나와관련된 모든 결재대기문서

	// 그리드 - 5.결재완료 - 결재권한자가 결재를 완료하면 볼수 있음(1차,2차,3차 모든결재 완료시)

			


}
