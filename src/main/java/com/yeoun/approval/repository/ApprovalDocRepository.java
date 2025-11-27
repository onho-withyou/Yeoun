package com.yeoun.approval.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yeoun.approval.dto.ApprovalFormDTO;
import org.springframework.stereotype.Repository; 
import com.yeoun.approval.entity.ApprovalDoc;
import com.yeoun.approval.entity.ApprovalForm;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.entity.Position;

import java.util.List;
import java.util.Optional;


@Repository
public interface ApprovalDocRepository extends JpaRepository<ApprovalDoc, Long> {
	//Optional<ApprovalDoc> findByEmpId(String empId);
	Optional<Emp> findByEmpId(String empId);
	
	// 사원 목록 조회
	@Query("SELECT m FROM Emp m WHERE status='ACTIVE'")
    List<Emp> findAllMember();

	// 퇴사안한 사원목록 조회
	@Query(value ="""
			 SELECT emp_id,emp_name FROM emp WHERE status='ACTIVE'
			""",nativeQuery = true)
    List<Object[]> findAllMember2();

	// 직급정보 불러오기
	@Query(value ="""
			SELECT * FROM position
			""",nativeQuery = true)
	List<Position> findPosition();
	//기안서 양식종류 - 부서별 다름
    @Query(value ="""
    		SELECT afede.form_code
				 ,afede.form_name
				 ,afede.dept_id
				 ,afede.dept_name
				 ,afede.approver_1
				 ,afede.approver_name1
				 ,afede.approver_2
				 ,afede.approver_name2
				 ,afede.approver_3
				 ,e3.emp_name
			FROM (SELECT afed.form_code
				 		,afed.form_name
				 		,afed.dept_id
				 		,afed.dept_name
				 		,afed.approver_1
				 		,afed.approver_name1
				 		,afed.approver_2 
				 		,e2.emp_name as approver_name2
				 		,afed.approver_3		 
				 FROM (SELECT af.form_code
				 				,af.form_name
				 				,af.dept_id
				 				,d.dept_name
				 				,af.approver_1
				 				,e1.emp_name as approver_name1
				 				,af.approver_2
				 				,af.approver_3
				 		FROM approval_form af
				 		INNER JOIN dept d ON af.dept_id = d.dept_id
				 		LEFT OUTER JOIN emp e1 ON af.approver_1 = e1.emp_id) afed
				LEFT OUTER JOIN emp e2 ON afed.approver_2 = e2.emp_id) afede
				LEFT OUTER JOIN emp e3 ON afede.approver_3 = e3.emp_id
			WHERE afede.dept_id = :deptId
    		""",nativeQuery = true)
    List<ApprovalForm> findAllFormTypes(@Param("deptId") String deptId);
	
	//부서목록조회
	@Query("SELECT d FROM Dept d")
	List<Dept> findAllDepartments();

	// 그리드 - 1.결재사항 - 진행해야할 결재만 - 결재권한자만 볼수있음
	// 열람권한은 무조건 대문자 Y여야함
	  @Query(value = """
		        SELECT 
		               ROWNUM AS row_no
		             , ade.approval_id
		             , ade.approval_title
                     , ade.form_type
		             , ade.emp_id
		             , ade.emp_name
		             , ade.dept_id
		             , ade.dept_name
		             , ade.approver
		             , e.emp_name AS approver_name
		             , ade.pos_code
		             , ade.pos_name
		             , ade.created_date
		             , ade.finish_date
		             , ade.start_date
		             , ade.end_date
		             , ade.leave_type
                     , ade.to_pos_code -- 변경되는 기안자 직급
		             , ade.to_dept_id
		             , ade.expnd_type
		             , ade.reason
		             , ade.doc_status
		        FROM emp e
		           , (
		                SELECT
		                       ad.approval_id
		                     , ad.approval_title
                             , ad.form_type
		                     , ad.emp_id
		                     , e.emp_name
		                     , e.dept_id
		                     , d.dept_name
		                     , ad.approver
		                     , p.pos_code
		                     , p.pos_name
		                     , ad.created_date
		                     , ad.finish_date
		                     , ad.start_date   -- 휴가 시작
		                     , ad.end_date     -- 휴가 종료
		                     , ad.leave_type   -- 휴가 유형
                             , ad.to_pos_code  -- 변경되는 기안자 직급
		                     , ad.to_dept_id   -- 발령 부서
		                     , ad.expnd_type   -- 지출 종류
		                     , ad.reason       -- 사유
		                     , ad.doc_status
		                FROM approval_doc ad
		                   , emp e
		                   , dept d
		                   , position p
		                WHERE ad.emp_id = e.emp_id
		                  AND e.dept_id = d.dept_id
		                  AND e.pos_code = p.pos_code
		                  AND ad.approver = :empId       -- 결재권자 = 로그인 사번
		                  AND ad.doc_status NOT IN ('완료','반려') -- 진행중/대기만
		             ) ade
		        WHERE ade.approver = e.emp_id         -- 결재자 정보 조인
		        """, nativeQuery = true)
	List<Object[]> findPendingApprovalDocs(@Param("empId") String empId);

	// 그리드 - 2.전체결재- 나와관련된 모든 결재문서
	@Query(value = """
				
				SELECT rownum AS row_no
							,adre.approval_id
							,adre.approval_title
							,adre.form_type
							,adre.emp_id
							,adre.emp_name
							,adre.dept_id
							,adre.dept_name
							,adre.approver
							,e.emp_name as approver_name
							,adre.pos_code
							,adre.pos_name
							,adre.created_date
							,adre.finish_date
							,adre.start_date
							,adre.end_date
							,adre.leave_type
							,adre.to_pos_code
							,adre.to_dept_id
							,adre.expnd_type
							,adre.reason
							,adre.doc_status
						FROM
						(SELECT 
								adr.approval_id
								,adr.approval_title
								,adr.form_type
								,adr.emp_id
								,e.emp_name
								,e.dept_id
								,d.dept_name
								,adr.approver
								,p.pos_code
								,p.pos_name
								,adr.created_date
								,adr.finish_date
								,adr.start_date
								,adr.end_date
								,adr.leave_type
								,adr.to_pos_code
								,adr.to_dept_id
								,adr.expnd_type
								,adr.reason
								,adr.doc_status
							FROM emp e, dept d,position p,
							( SELECT distinct 
									ad.approval_id
									,ad.approval_title
									,ad.form_type
									,ad.emp_id
									,ad.approver
									,ad.created_date
									,ad.finish_date
									,ad.start_date -- 휴가시작날짜
									,ad.end_date -- 휴가종료날짜
									,ad.leave_type-- 연차유형,휴가종류
									,ad.to_pos_code
									,ad.to_dept_id-- 발령부서
									,ad.expnd_type-- 지출종류
									,ad.reason-- 결재사유내용
									,ad.doc_status
								FROM approval_doc ad,approver ar
								WHERE (ad.approver is not null
									and ad.approver = ar.emp_id 
									and ar.viewing = 'Y'
									and ar.emp_id= :empId) 
								OR ad.emp_id = :empId ) adr
								WHERE e.emp_id = adr.emp_id 
								and e.dept_id = d.dept_id
								and e.pos_code = p.pos_code) adre,emp e
							WHERE adre.approver = e.emp_id
				""", nativeQuery = true)	
	List<Object[]> findAllApprovalDocs(@Param("empId") String empId);

	// 그리드 - 3.내결재목록 - 내가 올린결재목록
	@Query(value = """
				
           SELECT rownum AS row_no
                    ,adre.approval_id
                    ,adre.approval_title
                    ,adre.form_type
                    ,adre.emp_id
                    ,adre.emp_name
                    ,adre.dept_id
                    ,adre.dept_name
                    ,adre.approver
                    ,e.emp_name as approver_name
                    ,adre.pos_code
                    ,adre.pos_name
                    ,adre.created_date
                    ,adre.finish_date
                    ,adre.start_date
                    ,adre.end_date
                    ,adre.leave_type
					,adre.to_pos_code
                    ,adre.to_dept_id
                    ,adre.expnd_type
                    ,adre.reason
                    ,adre.doc_status
            FROM (SELECT 
      				ad.approval_id
      				,ad.approval_title
                    ,ad.form_type
      				,ad.emp_id
      				,e.emp_name
      				,e.dept_id
      				,d.dept_name
      				,ad.approver
      				,p.pos_code
      				,p.pos_name
      				,ad.created_date
      				,ad.finish_date
                    ,ad.start_date -- 휴가시작날짜
                    ,ad.end_date -- 휴가종료날짜
                    ,ad.leave_type-- 연차유형,휴가종류
					,ad.to_pos_code -- 변경직급
                    ,ad.to_dept_id-- 발령부서
                    ,ad.expnd_type-- 지출종류
                    ,ad.reason-- 결재사유내용
      				,ad.doc_status
				FROM approval_doc ad, emp e, dept d, position p
				WHERE ad.emp_id = e.emp_id 
				AND e.dept_id = d.dept_id
				AND e.pos_code = p.pos_code
				AND e.emp_id = :empId) adre, emp e
            WHERE adre.approver = e.emp_id
                
				""", nativeQuery = true)
	List<Object[]> findMyApprovalDocs(@Param("empId") String empId);
	
	// 그리드 - 4.결재대기 - 나와관련된 모든 결재대기문서(내가결재권한자인것,내가올린문서)
	@Query(value = """
		 SELECT  rownum AS row_no
            	,adre.approval_id
            	,adre.approval_title
            	,adre.form_type
            	,adre.emp_id
            	,adre.emp_name
            	,adre.dept_id
            	,adre.dept_name
            	,adre.approver
            	,e.emp_name as approver_name
            	,adre.pos_code
            	,adre.pos_name
            	,adre.created_date
            	,adre.finish_date
            	,adre.start_date
            	,adre.end_date
            	,adre.leave_type
				,adre.to_pos_code
            	,adre.to_dept_id
            	,adre.expnd_type
            	,adre.reason
            	,adre.doc_status
    		FROM (SELECT
						adr.approval_id
						,adr.approval_title
                    	,adr.form_type
						,adr.emp_id
						,e.emp_name
						,d.dept_id
						,d.dept_name
						,adr.approver
						,p.pos_code
						,p.pos_name
						,adr.created_date
						,adr.finish_date
                    	,adr.start_date -- 휴가시작날짜
                    	,adr.end_date -- 휴가종료날짜
                    	,adr.leave_type-- 연차유형,휴가종류
						,adr.to_pos_code -- 변경직급
                    	,adr.to_dept_id-- 발령부서
                    	,adr.expnd_type-- 지출종류
                    	,adr.reason-- 결재사유내용
						,adr.doc_status
						,adr.viewing
			FROM emp e, dept d, position p, 
               ( SELECT  distinct
                            ad.approval_id -- 문서id
							,ad.approval_title --문서제목
                            ,ad.form_type -- 문서양식
							,ad.emp_id --문서작성자
							,ad.approver  -- 결재권한자
							,ad.created_date --결재시작일시
							,ad.finish_date --결재완료일시
                            ,ad.start_date -- 휴가시작날짜
                            ,ad.end_date -- 휴가종료날짜
                            ,ad.leave_type-- 연차유형,휴가종류
							,ad.to_pos_code -- 변경직급
                            ,ad.to_dept_id-- 발령부서
                            ,ad.expnd_type-- 지출종류
                            ,ad.reason-- 결재사유내용
							,ad.doc_status --결재문서상태
                            ,ar.viewing -- 결재권한자의 문서열람권한
					FROM approval_doc ad                     
                    INNER JOIN approver ar
                    ON ad.doc_status NOT LIKE '%완료%'  -- 공통- 문서작성자이면서, 결재권한자 일때
                    WHERE (ar.viewing ='Y' and ad.emp_id = :empId)--문서작성자일떄--ar.viewing의 경우에는 Y나N을 안걸어주면 뻥튀기되서2배로나옴
                    OR (ad.approval_id = ar.approval_id       -- 결재권한자일때
                    AND ar.viewing ='Y' AND ar.emp_id = :empId)) adr -- 박경찬 문서작성자이면서, 결재권한자일떄 
            WHERE adr.emp_id = e.emp_id
			AND e.dept_id = d.dept_id
			AND e.pos_code = p.pos_code) adre, emp e
        WHERE adre.approver = e.emp_id
	""", nativeQuery = true)
	List<Object[]> findWaitingApprovalDocs(@Param("empId") String empId);		

	// 그리드 - 5.결재완료 - 결재권한자가 결재를 완료하면 볼수 있음(1차,2차,3차 모든결재 완료시)
	 @Query(value = """
				SELECT  ROWNUM
            			,adre.approval_id
            			,adre.approval_title
            			,adre.form_type
            			,adre.emp_id
            			,adre.emp_name
            			,adre.dept_id
            			,adre.dept_name
            			,adre.approver
            			,e.emp_name as approver_name
            			,adre.pos_code
            			,adre.pos_name
            			,adre.created_date
            			,adre.finish_date
            			,adre.start_date
            			,adre.end_date
            			,adre.leave_type
						,adre.to_pos_code 
            			,adre.to_dept_id
            			,adre.expnd_type
            			,adre.reason
            			,adre.doc_status
            		FROM (SELECT 
			        			adr.approval_id
			        			,adr.approval_title
                    			,adr.form_type
			        			,adr.emp_id
			        			,e.emp_name
			        			,e.dept_id
			        			,d.dept_name
			        			,adr.approver
			        			,p.pos_code
			        			,p.pos_name
			        			,adr.created_date
			        			,adr.finish_date
                    			,adr.start_date -- 휴가시작날짜
                    			,adr.end_date -- 휴가종료날짜
                    			,adr.leave_type -- 연차유형,휴가종류
								,adr.to_pos_code -- 변경직급
                    			,adr.to_dept_id-- 발령부서
                    			,adr.expnd_type-- 지출종류
                    			,adr.reason-- 결재사유내용
			        			,adr.doc_status
					FROM emp e,dept d,position p,
						(SELECT distinct ad.approval_id
						        	,ad.approval_title
                                	,ad.form_type
						        	,ad.approver
						        	,ad.emp_id
						        	,ad.created_date
						        	,ad.finish_date
                                	,ad.start_date -- 휴가시작날짜
                                	,ad.end_date -- 휴가종료날짜
                                	,ad.leave_type-- 연차유형,휴가종류\
									,ad.to_pos_code -- 변경직급
                                	,ad.to_dept_id-- 발령부서
                                	,ad.expnd_type-- 지출종류
                                	,ad.reason-- 결재사유내용
						        	,ad.doc_status
							FROM approval_doc ad,approver ar
							WHERE (ad.doc_status = '완료'
							AND ad.approval_id = ar.approval_id 
							AND ar.emp_id = :empId
							AND ar.viewing = 'Y')
							OR(ad.doc_status = '완료' and ad.emp_id = :empId)) adr
					WHERE e.emp_id = adr.emp_id
					AND e.dept_id = d.dept_id
					AND e.pos_code = p.pos_code) adre, emp e
        	WHERE adre.approver = e.emp_id

	 """, nativeQuery = true)
	 List<Object[]> findFinishedApprovalDocs(@Param("empId") String empId);
	 
	//main에사용하는쿼리 현재결재권한자로서 결재해야할 것과 내가 올린 결재만
	@Query("select a from ApprovalDoc a " 
			+ "where (a.docStatus not in ('완료', '반려') and a.approver = :empId)" 
			+ "or a.empId = :empId "
			+ "order by createdDate desc")
	Page<ApprovalDoc> getSummaryApprovalPage(@Param("empId") String empId, Pageable pageable);
			


}
