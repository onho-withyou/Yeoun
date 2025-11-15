INSERT INTO emp(gender, hire_date, emp_id, created_date, last_login, photo_file_id, post_code, status, rrn, mobile, role_code, emp_name, email, address1, address2, emp_pwd) VALUES (
	'M', '25/11/09', '2511301', '25/11/10 11:16:22.030042000', '', '', '28562', 'ACTIVE', '', '010-1234-2222', 'USER', '홍길동', 'hong@hong.com', '광주 동구 2순환로 575 (지산동)', '2', '$2a$10$j1X19qACsW1Yh0F0gG4meu.JMoxuTjZf46xW.i4aC2NkfvyWyM7v6'
);

-- 부서 DEPT
INSERT INTO DEPT (DEPT_ID, DEPT_NAME, PARENT_ID, HEAD_EMP_ID, USE_YN, CREATED_DATE, DEPT_ABBR)
VALUES ('DEP000', 'ERP본부', NULL, NULL, 'Y', SYSDATE, 'ERP');
INSERT INTO DEPT (DEPT_ID, DEPT_NAME, PARENT_ID, HEAD_EMP_ID, USE_YN, CREATED_DATE, DEPT_ABBR)
VALUES ('DEP100', 'MES본부', NULL, NULL, 'Y', SYSDATE, 'MES');
INSERT INTO DEPT (DEPT_ID, DEPT_NAME, PARENT_ID, HEAD_EMP_ID, USE_YN, CREATED_DATE, DEPT_ABBR)
VALUES ('DEP001', '개발부', 'DEP000', NULL, 'Y', SYSDATE, 'DEV');
INSERT INTO DEPT (DEPT_ID, DEPT_NAME, PARENT_ID, HEAD_EMP_ID, USE_YN, CREATED_DATE, DEPT_ABBR)
VALUES ('DEP002', '마케팅부', 'DEP000', NULL, 'Y', SYSDATE, 'MKT');
INSERT INTO DEPT (DEPT_ID, DEPT_NAME, PARENT_ID, HEAD_EMP_ID, USE_YN, CREATED_DATE, DEPT_ABBR)
VALUES ('DEP003', '생산부', 'DEP000', NULL, 'Y', SYSDATE, 'PRD');
INSERT INTO DEPT (DEPT_ID, DEPT_NAME, PARENT_ID, HEAD_EMP_ID, USE_YN, CREATED_DATE, DEPT_ABBR)
VALUES ('DEP004', '영업부', 'DEP000', NULL, 'Y', SYSDATE, 'SAL');
INSERT INTO DEPT (DEPT_ID, DEPT_NAME, PARENT_ID, HEAD_EMP_ID, USE_YN, CREATED_DATE, DEPT_ABBR)
VALUES ('DEP005', '인사부', 'DEP000', NULL, 'Y', SYSDATE, 'HR');

-- 직급 POSITION
INSERT INTO POSITION (POS_CODE, POS_NAME, RANK_ORDER, IS_EXECUTIVE, USE_YN, CREATED_DATE)
VALUES ('POS001', '사원', 1, 'N', 'Y', SYSDATE);
INSERT INTO POSITION (POS_CODE, POS_NAME, RANK_ORDER, IS_EXECUTIVE, USE_YN, CREATED_DATE)
VALUES ('POS002', '대리', 2, 'N', 'Y', SYSDATE);
INSERT INTO POSITION (POS_CODE, POS_NAME, RANK_ORDER, IS_EXECUTIVE, USE_YN, CREATED_DATE)
VALUES ('POS003', '과장', 3, 'N', 'Y', SYSDATE);
INSERT INTO POSITION (POS_CODE, POS_NAME, RANK_ORDER, IS_EXECUTIVE, USE_YN, CREATED_DATE)
VALUES ('POS004', '차장', 4, 'N', 'Y', SYSDATE);
INSERT INTO POSITION (POS_CODE, POS_NAME, RANK_ORDER, IS_EXECUTIVE, USE_YN, CREATED_DATE)
VALUES ('POS005', '부장', 5, 'N', 'Y', SYSDATE);
INSERT INTO POSITION (POS_CODE, POS_NAME, RANK_ORDER, IS_EXECUTIVE, USE_YN, CREATED_DATE)
VALUES ('POS006', '이사', 6, 'Y', 'Y', SYSDATE);
INSERT INTO POSITION (POS_CODE, POS_NAME, RANK_ORDER, IS_EXECUTIVE, USE_YN, CREATED_DATE)
VALUES ('POS007', '사장', 7, 'Y', 'Y', SYSDATE);

-- 역할 ROLE
INSERT INTO ROLE (ROLE_CODE, ROLE_NAME, ROLE_DESC, USE_YN, CREATED_DATE)
VALUES ('ROLE_ADMIN',     '전체 관리자', '시스템 전역 접근 가능(임원/관리자)', 'Y', SYSDATE);
INSERT INTO ROLE (ROLE_CODE, ROLE_NAME, ROLE_DESC, USE_YN, CREATED_DATE)
VALUES ('ROLE_ADMIN_SUB', '부서 관리자', '부서 단위 관리 권한(자동 부서장)', 'Y', SYSDATE);

-- 결재문서 APPROVAL_DOC
INSERT INTO approval_doc(
            APPROVAL_ID	  -- 결재 문서ID
            ,APPROVAL_TITLE	-- 문서제목
            ,APPROVER	--결재권한자
            ,CREATED_DATE	--생성일자
            ,DOC_STATUS	--문서상태--반려,1차완료,2차완료,3차완료
            ,EMP_ID	--사원번호
            ,EXPND_TYPE	 --지출종류
            ,FINISH_DATE --완료 예정일자	
            ,FORM_TYPE --양식종류
            ,REASON	--사유
            ,START_DATE --시작 휴가일자
            ,END_DATE -- 종료 휴가일자
            ,TO_DEPT_ID --발령부서
            )
VALUES(202511150101,'테스트용 문서제목','2511491',sysdate,'','2511491','고정지출',sysdate,'지출결의서','테스트용입니다.',sysdate,sysdate,'개발부');
INSERT INTO approval_doc(APPROVAL_ID ,APPROVAL_TITLE ,APPROVER	,CREATED_DATE ,DOC_STATUS,EMP_ID ,EXPND_TYPE ,FINISH_DATE ,FORM_TYPE ,REASON,START_DATE,END_DATE,TO_DEPT_ID)
VALUES(202511150102,'개발부용 테스트 문서제목','',sysdate,'','2103347','변동지출',sysdate,'자유양식결재서','테스트용입니다.',sysdate,sysdate,'마케팅부');
INSERT INTO approval_doc(APPROVAL_ID ,APPROVAL_TITLE ,APPROVER	,CREATED_DATE ,DOC_STATUS,EMP_ID ,EXPND_TYPE ,FINISH_DATE ,FORM_TYPE ,REASON,START_DATE,END_DATE,TO_DEPT_ID)
VALUES(202511150103,'마케팅부용 테스트 문서제목','',sysdate,'','1907475','변동지출',sysdate,'인사발령신청서','테스트용입니다.',sysdate,sysdate,'생산부');
INSERT INTO approval_doc(APPROVAL_ID ,APPROVAL_TITLE ,APPROVER	,CREATED_DATE ,DOC_STATUS,EMP_ID ,EXPND_TYPE ,FINISH_DATE ,FORM_TYPE ,REASON,START_DATE,END_DATE,TO_DEPT_ID)
VALUES(202511150104,'생산부용 테스트 문서제목','',sysdate,'','1707923','비정기지출',sysdate,'휴가연차신청서','테스트용입니다.',sysdate,sysdate,'영업부');
INSERT INTO approval_doc(APPROVAL_ID ,APPROVAL_TITLE ,APPROVER	,CREATED_DATE ,DOC_STATUS,EMP_ID ,EXPND_TYPE ,FINISH_DATE ,FORM_TYPE ,REASON,START_DATE,END_DATE,TO_DEPT_ID)
VALUES(202511150105,'영업부용 테스트 문서제목','',sysdate,'','1209349','고정지출',sysdate,'지출결의서','테스트용입니다.',sysdate,sysdate,'인사부');
INSERT INTO approval_doc(APPROVAL_ID ,APPROVAL_TITLE ,APPROVER	,CREATED_DATE ,DOC_STATUS,EMP_ID ,EXPND_TYPE ,FINISH_DATE ,FORM_TYPE ,REASON,START_DATE,END_DATE,TO_DEPT_ID)
VALUES(202511150106,'인사부용 테스트 문서제목','',sysdate,'','2511733','고정지출',sysdate,'자유양식결재서','테스트용입니다.',sysdate,sysdate,'ERP본부');
INSERT INTO approval_doc(APPROVAL_ID ,APPROVAL_TITLE ,APPROVER	,CREATED_DATE ,DOC_STATUS,EMP_ID ,EXPND_TYPE ,FINISH_DATE ,FORM_TYPE ,REASON,START_DATE,END_DATE,TO_DEPT_ID)
VALUES(202511150107,'인사부용 테스트 전도연 문서제목','2401300',sysdate,'','2104502','고정지출',sysdate,'자유양식결재서','테스트용전도연입니다.',sysdate,sysdate,'MES본부');
INSERT INTO approval_doc(APPROVAL_ID ,APPROVAL_TITLE ,APPROVER	,CREATED_DATE ,DOC_STATUS,EMP_ID ,EXPND_TYPE ,FINISH_DATE ,FORM_TYPE ,REASON,START_DATE,END_DATE,TO_DEPT_ID)
VALUES(202511150108,'인사부용 테스트 전도연2 문서제목','',sysdate,'','2104502','비정기지출',sysdate,'자유양식결재서','테스트용 2 입니다.',sysdate,sysdate,'개발3팀본부');
INSERT INTO approval_doc(APPROVAL_ID ,APPROVAL_TITLE ,APPROVER	,CREATED_DATE ,DOC_STATUS,EMP_ID ,EXPND_TYPE ,FINISH_DATE ,FORM_TYPE ,REASON,START_DATE,END_DATE,TO_DEPT_ID)
VALUES(202511150109,'인사부용 테스트 전도연3 문서제목','',sysdate,'','2104502','정기지출',sysdate,'자유양식결재서','테스트용 3 입니다.',sysdate,sysdate,'개발2팀본부');

-- 결재권한자
INSERT INTO approver(
            emp_id, -- 결재자 사원 id
            approval_id, -- 결재서류 id
            approval_status, --결재 상태
            delegate_status, --전결자 상태
            order_approvers, --결재순서
            viewing)  --열람권한
VALUES('2511491',202511150101,1,'본인','1','Y');
INSERT INTO approver(emp_id, approval_id, approval_status, delegate_status, order_approvers, viewing) 
VALUES('2511733',202511150101,0,'전결','2','');
INSERT INTO approver(emp_id, approval_id, approval_status, delegate_status, order_approvers, viewing) 
VALUES('2407628',202511150101,0,'대결','3','');
INSERT INTO approver(emp_id, approval_id, approval_status, delegate_status, order_approvers, viewing) 
VALUES('2401300',202511150101,0,'선결','','');
INSERT INTO approver(emp_id, approval_id, approval_status, delegate_status, order_approvers, viewing) 
VALUES('2110664',202511150101,0,'','','');
INSERT INTO approver(emp_id, approval_id, approval_status, delegate_status, order_approvers, viewing) 
VALUES('0009236',202511150102,0,'','3','');
INSERT INTO approver(emp_id, approval_id, approval_status, delegate_status, order_approvers, viewing) 
VALUES('1707705',202511150102,0,'','2','');
INSERT INTO approver(emp_id, approval_id, approval_status, delegate_status, order_approvers, viewing) 
VALUES('1301823',202511150102,0,'','1','');
INSERT INTO approver(emp_id, approval_id, approval_status, delegate_status, order_approvers, viewing) 
VALUES('2511793',202511150107,0,'','3','');
INSERT INTO approver(emp_id, approval_id, approval_status, delegate_status, order_approvers, viewing) 
VALUES('2401300',202511150107,0,'','2','y');
INSERT INTO approver(emp_id, approval_id, approval_status, delegate_status, order_approvers, viewing) 
VALUES('2104502',202511150107,0,'','1','');
