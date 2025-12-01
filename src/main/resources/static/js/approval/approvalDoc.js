	//결재.js
	//변수는 v- ,함수는 f-,그리드는 g- 주석 
	// 현재 로그인한 사용자 EMP_ID
	const LOGIN_USER_ID = document.getElementById('currentUserId').value;
	const LOGIN_USER_NAME = document.getElementById('currentUserName').value;
	// 현재 열린 문서의 approvalId
	let approvalId;
	// 현재 열린 문서의 결재권자(approval) 
	let currentApprover;
	// 모달의 결재확인 버튼
	
	// 결제확인 버튼
	const approvalCheckBtn = document.getElementById('approvalCheckBtn');
	// 반려 버튼
	const approvalCompanionBtn = document.getElementById('approvalCompanionBtn');
	
	// ========================================================
	// v- 결재권한자
	let elemApproverIdNum = null;//결재권한자 count 중요! 꼬이면안됨
	// ========================================================
	
	// f- 결재확인 버튼 눌렀을때 동작할 함수
	approvalCheckBtn.addEventListener('click', () => {
		patchApproval("accept");
	});
	
	// f- 반려버튼 눌렀을때 동작할 함수
	approvalCompanionBtn.addEventListener('click', () => {
		patchApproval("deny")		
	});
	
	// f- null-safe 날짜 변환 함수
	function toDateStr(value) {
	  if (!value) return '';              // null, undefined, '' 전부 빈 문자열 처리
	  return String(value).split('T')[0]; // 혹시 문자열 아니어도 방어
	}
	
		
	// f- 현재 로그인한 사용자와 결재권자 비교
	function checkApprover() {
		if(currentApprover != LOGIN_USER_ID) {
			alert("승인 또는 반려권한이 없습니다."); 
			return true;
		}
	}
	
	// f- 결재 패치 보내기 함수
	function patchApproval(btn) {
		// 현재 로그인한 사용자와 결재권자 비교
		if(checkApprover()) return;
		let msg = "";
		btn == 'accept' ? msg = "승인하시겠습니까?" : msg = "반려하시겠습니까?"
		 
		
		// 결재권한자와 사용자가 동일인물일 때
		if(confirm(msg)) {
			//결재 확인 동작함수
			fetch(`/api/approvals/${approvalId}?btn=${btn}` , {
				method: 'PATCH'
				, headers: {
					[csrfHeader]: csrfToken
				}
			})
			.then(response => {
				if (!response.ok) return response.json().then(err => { throw new Error(err.result); });
				return response.json();
			})
			.then(data => {
				alert(data.result);
				// 결제승인완료시 새로고침
				location.reload();
				
			}).catch(error => {
				console.error('에러', error)
				alert("결재 승인 실패!!");
			});
		 }
	}
	
	// f- 결제상세보기 => 결제권자 정보 불러오기함수
	async function getApproverList(approvalId) {
		try {
			const response = await fetch(`/api/approvals/approvers/${approvalId}`, {method: 'GET'});
			
			if(!response.ok) {
				const errorData = await response.json();
				throw new Error(errorData.result);
			}
			const data = await response.json();
			return data;
		} catch(error) {
			alert("결재권자 목록을 불러올 수 없습니다!");
			return null;
		}
	}
	
	//grid - 1.결재사항 - 진행해야할 결재만 - 결재권한자만 볼수있음
	//grid - 2.전체결재 - 나와관련된 모든 결재문서
	//grid - 3.내 결재목록 - 내가 기안한 문서
	//grid - 4.결재대기 - 나와관련된 모든 결재대기
	//grid - 5.결재완료 - 나와 관련된 결재완료한 문서
	window.onload = function() {
		AllGridSearch();//조회버튼
		empData();
	}
	
	let approverDiv = document.querySelector('#approver');
	
	let itemData;


	// f- selectbox - 인사정보 불러오기
	async function empData() {
		try {
			const response = await fetch("/approval/empList");
			const data = await response.json();
			itemData  = [];
			let obj ={};
			//console.log(data);
			data.map((item,index)=>{
				obj["value"] = item[0]; //사번
				obj["label"] = item[1]+"("+item[0]+")"; //이름(사번)
				itemData.push(obj);
				obj = {};
			});
			let selectBox;
			//셀렉트박스 - 토스트유아이
			selectBox = new tui.SelectBox('#select-box', {
			  data: itemData
			});
			
			//셀렉트박스 닫힐때
			selectBox.on('close',(ev)=>{
				let selectlabel = selectBox.getSelectedItem().label;
				let approverEmpId = selectBox.getSelectedItem().value;

				if(selectlabel != null && approverArr.length < 3){//셀렉트 라벨선택시 3번까지만셈
					print(ev.type, selectlabel);
					approverArr.push({
						empId: approverEmpId
						, approverOrder: window.count 
						, delegateStatus : 'N' //여기서 전결상태도 불러오자
						, originalEmpId: approverEmpId // 초기 사번 저장
					});
					console.log("@@@@@@@@@@@@@@@@@@@@@@",approverArr);
				}
				
			});
			//const modal = document.getElementById('approval-modal');
			//그리드 1클릭시 상세버튼
			grid1.on("click", async (ev) => {
		
				const target = ev.nativeEvent.target;
				// const targetElement = ev.nativeEvent.target; 이 줄이 빠진 경우
				if (ev.targetType === 'cell' && target.tagName === 'BUTTON') {
					
					const rowData = grid1.getRow(ev.rowKey);
					$('#approval-modal').modal('show');
				
					document.getElementById('saveBtn').style.display = "none";//approvalCompanionBtn//approvalCheckBtn
					document.getElementById('attachmentBtn').style.display = "none";//첨부파일
					document.getElementById('downloadArea').style.display = "block";//다운로드
					document.getElementById('approvalCompanionBtn').style.display = "inline-block";//반려
					document.getElementById('approvalCheckBtn').style.display = "inline-block";//결재확인
					// 문서 열릴때 approvalId에 현재 열린 문서id 저장
					approvalId = rowData.approval_id;
					getApprovalDocFileData(approvalId);
					// 문서 열릴때 현재 결재권자(approval) 저장
					currentApprover = rowData.approver;
					console.log("rowData",rowData);//DraftingHidden
					document.getElementById('Drafting').innerText = rowData.approval_title;
					document.getElementById('DraftingHidden').value = rowData.approval_title;
					//document.getElementById('Drafting').value = rowData.approval_title;
					document.getElementById('today-date').innerText = toDateStr(rowData.created_date) ;//결재 작성날짜 = 결재시작일
					document.getElementById('approval-title').value = rowData.approval_title;
					//양식종류 form-menu
					document.getElementById('approver-name').value  = rowData.emp_id;//결재자명
					document.getElementById('form-menu').value = rowData.form_type;//양식종류
					//const createdDate = rowData.created_date;
					document.getElementById('create-date').value = toDateStr(rowData.created_date);//결재시작일 =결재 작성날짜 
					document.getElementById('finish-date').value = toDateStr(rowData.finish_date);//결재완료날짜
					//휴가 연차신청서 
					document.getElementById('start-date').value = toDateStr(rowData.start_date); //휴가시작날짜
					document.getElementById('end-date').value = toDateStr(rowData.end_date); //휴가종료날짜
					//document.getElementById('leave-radio').value = rowData.leave_type;// 연차유형 라디오- 없앳음 -휴가종류로 들어감
					document.getElementById('leave-type').value = rowData.leave_type;//휴가종류
					console.log("rowData.to_dept_id",rowData.to_dept_id);
					document.getElementById('position').value = rowData.to_pos_code;
					document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서
					document.getElementById('expnd-type').value = rowData.expnd_type;//지출종류EXPND_TYPE
					//document.getElementById('approver').value = rowData.approver;//결재권한자
					//상세버튼 클릭시 디폴트 결재권한자 div 생기게하는 로직
					//여러번 누르면 한번씩 이전값을가지고있음
					const approverList = await getApproverList(approvalId);
					console.log("approverList ---------->",approverList);
					let sortedList; 
					
					if(approverList.length > 0) {
						sortedList = approverList.sort((a, b) => {
							return Number (a.orderApprovers) - Number(b.orderApprovers);
						});
						console.log("approverList---------------->",approverList);

						window.count = 0;
						approverDiv.innerHTML = "";
						console.log("sortedList---->",sortedList);
						for (const approver of sortedList) {
							selectBox.select(approver.empId);
							print("default", selectBox.getSelectedItem()?.label);
						}

					}
					//document.getElementById('approver').innerText = rowData.approver;//전결자
					document.getElementById('reason-write').value = rowData.reason;//결재사유내용
					//selectBox.disable();
					// 상세버튼 양식종류에 따른 form 보이기/숨기기
					formChange(rowData.form_type);
					formDisable();	
				}	
			});

			grid2.on("click", async (ev) => {
		
				const target = ev.nativeEvent.target;
				if (ev.targetType === 'cell' && target.tagName === 'BUTTON') {
					const rowData = grid2.getRow(ev.rowKey);
					$('#approval-modal').modal('show');
					
					document.getElementById('saveBtn').style.display = "none";
					document.getElementById('attachmentBtn').style.display = "none";//첨부파일
					document.getElementById('downloadArea').style.display = "block";//다운로드
					document.getElementById('approvalCompanionBtn').style.display = "inline-block";//반려
					document.getElementById('approvalCheckBtn').style.display = "inline-block";//결재확인
					
					// 문서 열릴때 approvalId에 현재 열린 문서id 저장
					approvalId = rowData.approval_id;
					getApprovalDocFileData(approvalId);
					// 문서 열릴때 현재 결재권자(approval) 저장
					currentApprover = rowData.approver;
					
					document.getElementById('Drafting').innerText = rowData.approval_title;
					document.getElementById('DraftingHidden').value = rowData.approval_title;
					document.getElementById('today-date').innerText = toDateStr(rowData.created_date);//결재 작성날짜 = 결재시작일
					//document.getElementById('approval-title').value = rowData.approval_title;
					document.getElementById('form-menu').value = rowData.form_type;//양식종류//양식종류form-menu
					document.getElementById('approver-name').value  = rowData.emp_id;//결재자명
					document.getElementById('create-date').value = toDateStr(rowData.created_date);//결재시작일 =결재 작성날짜 
					document.getElementById('finish-date').value = toDateStr(rowData.finish_date);//결재완료날짜
					//휴가 연차신청서 
					document.getElementById('start-date').value = toDateStr(rowData.start_date); //휴가시작날짜
					document.getElementById('end-date').value = toDateStr(rowData.end_date); //휴가종료날짜
					//document.getElementById('leave-radio').value = rowData.leave_type;// 연차유형 라디오- 없앳음 -휴가종류로 들어감
					document.getElementById('leave-type').value = rowData.leave_type;//휴가종류
					document.getElementById('position').value = rowData.to_pos_code;//변경직급
					document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서
					document.getElementById('expnd-type').value = rowData.expnd_type;//지출종류EXPND_TYPE
					//document.getElementById('approver').value = rowData.approver;//결재권한자
					
					const approverList = await getApproverList(approvalId);
					
					let sortedList; 
					if(approverList.length > 0) {
						sortedList = approverList.sort((a, b) => {
							return Number (a.orderApprovers) - Number(b.orderApprovers);
						});

						window.count = 0;
						approverDiv.innerHTML = "";
						for (const approver of sortedList) {
							selectBox.select(approver.empId);
							print("default", selectBox.getSelectedItem()?.label);
						}

					}
					//document.getElementById('approver').innerText = rowData.approver;//전결자
					document.getElementById('reason-write').value = rowData.reason;//결재사유내용
					//selectBox.disable();
					formChange(rowData.form_type);
					formDisable();
				}
			});


			grid3.on("click", async (ev) => {
		
				const target = ev.nativeEvent.target;
				if (ev.targetType === 'cell' && target.tagName === 'BUTTON') {
					const rowData = grid3.getRow(ev.rowKey);
					$('#approval-modal').modal('show');
					
					document.getElementById('saveBtn').style.display = "none";
					document.getElementById('attachmentBtn').style.display = "none";//첨부파일
					document.getElementById('downloadArea').style.display = "block";//다운로드
					document.getElementById('approvalCompanionBtn').style.display = "inline-block";//반려
					document.getElementById('approvalCheckBtn').style.display = "inline-block";//결재확인
					
					// 문서 열릴때 approvalId에 현재 열린 문서id 저장
					approvalId = rowData.approval_id;
					getApprovalDocFileData(approvalId);
					// 문서 열릴때 현재 결재권자(approval) 저장
					currentApprover = rowData.approver;
					
					document.getElementById('Drafting').innerText = rowData.approval_title;
					document.getElementById('DraftingHidden').value = rowData.approval_title;
					document.getElementById('today-date').innerText = toDateStr(rowData.created_date);//결재 작성날짜 = 결재시작일
					//document.getElementById('approval-title').value = rowData.approval_title;
					document.getElementById('form-menu').value = rowData.form_type;//양식종류//양식종류form-menu
					document.getElementById('approver-name').value  = rowData.emp_id;//결재자명
					console.log("rowData.created_date",toDateStr(rowData.created_date) );
					const createdDate = rowData.created_date;
					document.getElementById('create-date').value = toDateStr(rowData.created_date);//결재시작일 =결재 작성날짜 
					document.getElementById('finish-date').value = toDateStr(rowData.finish_date);//결재완료날짜
					//휴가 연차신청서 
					document.getElementById('start-date').value = toDateStr(rowData.start_date); //휴가시작날짜
					document.getElementById('end-date').value = toDateStr(rowData.end_date); //휴가종료날짜
					//document.getElementById('leave-radio').value = rowData.leave_type;// 연차유형 라디오- 없앳음 -휴가종류로 들어감
					document.getElementById('leave-type').value = rowData.leave_type;//휴가종류
					document.getElementById('position').value = rowData.to_pos_code; //변경직급
					document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서
					document.getElementById('expnd-type').value = rowData.expnd_type;//지출종류EXPND_TYPE
					//document.getElementById('approver').value = rowData.approver;//결재권한자
					const approverList = await getApproverList(approvalId);
					
					let sortedList; 
					
					if(approverList.length > 0) {
						sortedList = approverList.sort((a, b) => {
							return Number (a.orderApprovers) - Number(b.orderApprovers);
						});
                        console.log("approverList---------------->",approverList);

						window.count = 0;
						approverDiv.innerHTML = "";

						for (const approver of sortedList) {
							selectBox.select(approver.empId);
							print("default", selectBox.getSelectedItem()?.label);
						}

					}
					//document.getElementById('approver').innerText = rowData.approver;//전결자
					document.getElementById('reason-write').value = rowData.reason;//결재사유내용
					//selectBox.disable();
					formChange(rowData.form_type);
					formDisable();
				}
			});	

			grid4.on("click", async (ev) => {
				
			const target = ev.nativeEvent.target;
			if (ev.targetType === 'cell' && target.tagName === 'BUTTON') {
				const rowData = grid4.getRow(ev.rowKey);
				$('#approval-modal').modal('show');
				
				document.getElementById('saveBtn').style.display = "none";//등록버튼
				document.getElementById('attachmentBtn').style.display = "none";//첨부파일
				document.getElementById('downloadArea').style.display = "block";//다운로드
				document.getElementById('approvalCompanionBtn').style.display = "inline-block";//반려버튼
				document.getElementById('approvalCheckBtn').style.display = "inline-block";//결재확인버튼
				
				// 문서 열릴때 approvalId에 현재 열린 문서id 저장
				approvalId = rowData.approval_id;
				getApprovalDocFileData(approvalId);
				// 문서 열릴때 현재 결재권자(approval) 저장
				currentApprover = rowData.approver;
				
				document.getElementById('Drafting').innerText = rowData.approval_title;
				document.getElementById('DraftingHidden').value = rowData.approval_title;
				document.getElementById('today-date').innerText = toDateStr(rowData.created_date);//결재 작성날짜 = 결재시작일
				//document.getElementById('approval-title').value = rowData.approval_title;
				document.getElementById('form-menu').value = rowData.form_type;//양식종류//양식종류form-menu
				document.getElementById('approver-name').value  = rowData.emp_id;//결재자명
				
				const createdDate = rowData.created_date;
				document.getElementById('create-date').value = toDateStr(rowData.created_date);//결재시작일 =결재 작성날짜 
				document.getElementById('finish-date').value = toDateStr(rowData.finish_date);//결재완료날짜
				//휴가 연차신청서 
				document.getElementById('start-date').value = toDateStr(rowData.start_date); //휴가시작날짜
				document.getElementById('end-date').value = toDateStr(rowData.end_date); //휴가종료날짜
				//document.getElementById('leave-radio').value = rowData.leave_type;// 연차유형 라디오- 없앳음 -휴가종류로 들어감
				document.getElementById('leave-type').value = rowData.leave_type;//휴가종류
				document.getElementById('position').value = rowData.to_pos_code; //변경직급
				document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서
				document.getElementById('expnd-type').value = rowData.expnd_type;//지출종류EXPND_TYPE
				//document.getElementById('approver').value = rowData.approver;//결재권한자
				const approverList = await getApproverList(approvalId);

					let sortedList; 

					if(approverList.length > 0) {
						sortedList = approverList.sort((a, b) => {
							return Number (a.orderApprovers) - Number(b.orderApprovers);
						});

						window.count = 0;
						approverDiv.innerHTML = "";

						for (const approver of sortedList) {
							selectBox.select(approver.empId);
							print("default", selectBox.getSelectedItem()?.label);
						}

					}
					//document.getElementById('approver').innerText = rowData.approver;//전결자
					document.getElementById('reason-write').value = rowData.reason;//결재사유내용
					//selectBox.disable();
					formDisable();
				}
			});
	
			grid5.on("click", async (ev) => {
			
				const target = ev.nativeEvent.target;
				if (ev.targetType === 'cell' && target.tagName === 'BUTTON') {
					const rowData = grid5.getRow(ev.rowKey);
					$('#approval-modal').modal('show');
					
					document.getElementById('saveBtn').style.display = "none";
					document.getElementById('attachmentBtn').style.display = "none";//첨부파일
					document.getElementById('downloadArea').style.display = "block";//다운로드
					document.getElementById('approvalCompanionBtn').style.display = "inline-block";//반려
					document.getElementById('approvalCheckBtn').style.display = "inline-block";//결재확인
					// 문서 열릴때 approvalId에 현재 열린 문서id 저장
					approvalId = rowData.approval_id;
					getApprovalDocFileData(approvalId);
					// 문서 열릴때 현재 결재권자(approval) 저장
					currentApprover = rowData.approver;
					
					document.getElementById('Drafting').innerText = rowData.approval_title;
					document.getElementById('DraftingHidden').value = rowData.approval_title;
					document.getElementById('today-date').innerText = rowData.created_date.split('T')[0] ;//결재 작성날짜 = 결재시작일
					//document.getElementById('approval-title').value = rowData.approval_title;
					document.getElementById('form-menu').value = rowData.form_type;//양식종류//양식종류form-menu
					document.getElementById('approver-name').value  = rowData.emp_id;//결재자명
					
					const createdDate = rowData.created_date;
					document.getElementById('create-date').value = toDateStr(rowData.created_date);//결재시작일 =결재 작성날짜 
					document.getElementById('finish-date').value = toDateStr(rowData.finish_date);//결재완료날짜
					//휴가 연차신청서 
					document.getElementById('start-date').value = toDateStr(rowData.start_date); //휴가시작날짜
					document.getElementById('end-date').value = toDateStr(rowData.end_date); //휴가종료날짜
					//document.getElementById('leave-radio').value = rowData.leave_type;// 연차유형 라디오- 없앳음 -휴가종류로 들어감
					document.getElementById('leave-type').value = rowData.leave_type;//휴가종류
					document.getElementById('position').value = rowData.to_pos_code;//변경직급
					document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서
					document.getElementById('expnd-type').value = rowData.expnd_type;//지출종류EXPND_TYPE
					//document.getElementById('approver').value = rowData.approver;//결재권한자
					const approverList = await getApproverList(approvalId);
						
						let sortedList; 
						
						if(approverList.length > 0) {
							sortedList = approverList.sort((a, b) => {
								return Number (a.orderApprovers) - Number(b.orderApprovers);
							});
							
							window.count = 0;
							approverDiv.innerHTML = "";
												
							for (const approver of sortedList) {
								selectBox.select(approver.empId);
								print("default", selectBox.getSelectedItem()?.label);
							}
							
						}
					//document.getElementById('approver').innerText = rowData.approver;//전결자
					document.getElementById('reason-write').value = rowData.reason;//결재사유내용
					//selectBox.disable();
					formChange(rowData.form_type);
					formDisable();
				}
			});
	
			return itemData;
		} catch (error) {
			console.error('Error fetching data:', error);
		}
	}	

	// f- 결재양식에따른 form 활성화/비활성화 함수
	function formChange(formType){
		if(formType == '지출결의서'){//attachmentBtn
			document.getElementById('expndTypeForm').style.display = 'flex';//지출종류
			document.getElementById('leavePeriodForm').style.display = 'none';// 휴가기간
			document.getElementById('leaveTypeForm').style.display = 'none';//휴가종류	
			document.getElementById('positionForm').style.display = 'none';//직급
			document.getElementById('toDeptForm').style.display = 'none'; //발령부서
		}else if(formType == '연차신청서'){
			document.getElementById('expndTypeForm').style.display = 'none';//지출종류
			document.getElementById('leavePeriodForm').style.display = 'flex';// 휴가기간
			document.getElementById('leaveTypeForm').style.display = 'flex';//휴가종류	
			document.getElementById('positionForm').style.display = 'none';//직급
			document.getElementById('toDeptForm').style.display = 'none'; //발령부서
		}else if(formType == '반차신청서'){
			document.getElementById('expndTypeForm').style.display = 'none';//지출종류
			document.getElementById('leavePeriodForm').style.display = 'flex';// 휴가기간
			document.getElementById('leaveTypeForm').style.display = 'flex';//휴가종류	
			document.getElementById('positionForm').style.display = 'none';//직급
			document.getElementById('toDeptForm').style.display = 'none'; //발령부서
		}else if(formType == '인사발령신청서'){
			document.getElementById('expndTypeForm').style.display = 'none';//지출종류
			document.getElementById('leavePeriodForm').style.display = 'none';// 휴가기간
			document.getElementById('leaveTypeForm').style.display = 'none';//휴가종류	
			document.getElementById('positionForm').style.display = 'flex';//직급
			document.getElementById('toDeptForm').style.display = 'flex'; //발령부
		}else if(formType == '자유양식결재서'){
			document.getElementById('expndTypeForm').style.display = 'none';//지출종류
			document.getElementById('leavePeriodForm').style.display = 'none';// 휴가기간
			document.getElementById('leaveTypeForm').style.display = 'none';//휴가종류	
			document.getElementById('positionForm').style.display = 'none';//직급
			document.getElementById('toDeptForm').style.display = 'none'; //발령부서
		}
	}
	// f- 그리드 클릭시 상세보기 document.getElementById('myInput').disabled = true;
	function formDisable(){
		document.getElementById('approval-title').disabled = true;
		document.getElementById('approver-name').disabled = true;
		document.getElementById('form-menu').disabled = true;
		document.getElementById('today-date').disabled = true;
		document.getElementById('Drafting').disabled = true;
		document.getElementById('DraftingHidden').disabled = true;
		document.getElementById('create-date').disabled = true;
		document.getElementById('finish-date').disabled = true;
		document.getElementById('start-date').disabled = true;
		document.getElementById('end-date').disabled = true;
		document.getElementById('leave-type').disabled = true;
		document.getElementById('position').disabled = true;
		document.getElementById('to-dept-id').disabled = true;
		document.getElementById('expnd-type').disabled = true;
		document.getElementById('reason-write').disabled = true;
		
	}
	//f- 기안서작성 클릭시 활성화 시켜주는 함수
	function formEnable(){
		document.getElementById('approval-title').disabled = false;
		document.getElementById('approver-name').disabled = false;
		document.getElementById('form-menu').disabled = false;
		document.getElementById('today-date').disabled = false;
		document.getElementById('Drafting').disabled = false;
		document.getElementById('DraftingHidden').disabled = false;
		document.getElementById('create-date').disabled = false;
		document.getElementById('finish-date').disabled = false;
		document.getElementById('start-date').disabled = false;
		document.getElementById('end-date').disabled = false;
		document.getElementById('leave-type').disabled = false;
		document.getElementById('to-dept-id').disabled = false;
		document.getElementById('expnd-type').disabled = false;
		document.getElementById('reason-write').disabled = false;
	}

	//f- 모달 첨부파일
	document.addEventListener('DOMContentLoaded', function() {
    	const attachBtn = document.getElementById('attachmentBtn');
    	const fileInput = document.getElementById('realFileInput');
    	const listContainer = document.getElementById('fileListContainer');

    	attachBtn.addEventListener('click', () => fileInput.click());
		fileInput.addEventListener('change', updateFileListDisplay);

		function resetAttachments() {
    	    fileInput.value = ''; // input[type=file]의 파일 목록을 초기화
    	    updateFileListDisplay(); // 화면 목록 갱신 (목록을 비우고 "선택된 파일 없음" 표시)
    	}
		// 파일 목록을 화면에 갱신하는 함수
		function updateFileListDisplay() {
		    listContainer.innerHTML = '';
		    const files = fileInput.files;
		    // '선택된 파일 없음' 문구 표시/숨김
		   //fileNameDisp.style.display = files.length > 0 ? 'none' : 'block';
	
		    Array.from(files).forEach((file, index) => {
		        const item = document.createElement('div');
				item.style.cssText = 'border-radius: 15px; display: flex; align-items: center; margin: 5px;';
		
		        // 미리보기/아이콘 영역 생성
		        const preview = createPreviewElement(file);
		        item.appendChild(preview);
		        // 파일 정보 영역 생성
		        const info = document.createElement('div');
		
		        // 파일 이름 (innerText 사용)
		        const nameSpan = document.createElement('span');
		        nameSpan.innerText = file.name;
		        info.appendChild(nameSpan);
		        // 삭제 버튼 생성 (innerText 사용 및 이벤트 연결)
		        const deleteBtn = document.createElement('button');
				deleteBtn.innerText = '×'; 
				deleteBtn.type = 'button';

				deleteBtn.style.cssText = 'border: none; background: transparent; padding: 0; font-size: 18px; cursor: pointer;';
		        deleteBtn.onclick = () => removeFile(index); 
		        info.appendChild(deleteBtn);
		        item.appendChild(info);
		        listContainer.appendChild(item);
		    });
		}
		// 파일 유형에 따른 미리보기/아이콘 요소 생성
		function createPreviewElement(file) {
		    const previewArea = document.createElement('div');
		    previewArea.style.cssText = 'width: 50px; height: 50px; border: none; overflow: hidden; display: flex; justify-content: center; align-items: center;';
		    if (file.type.startsWith('image/')) {
		        const reader = new FileReader();
		        reader.onload = (e) => {
		            const img = document.createElement('img');
					img.src = e.target.result;
		            img.style.cssText = 'width: 100%; height: 100%; object-fit: cover;';
		            previewArea.appendChild(img);
					
		        };
		        reader.readAsDataURL(file);
		    } else if (file.type === 'application/pdf') {
		        previewArea.innerHTML = '<span style="font-size: 30px;">📄</span>';
		    } else {
		        previewArea.innerHTML = '<span style="font-size: 30px;">📎</span>';
		    }
		    return previewArea;
		}
		// 파일 삭제 로직 (DataTransfer 사용)
		function removeFile(indexToRemove) {
		    const dt = new DataTransfer();
		    const files = fileInput.files;
		    for (let i = 0; i < files.length; i++) {
		        if (i !== indexToRemove) {
		            dt.items.add(files[i]);
		        }
		    }
		
		    fileInput.files = dt.files;
		    updateFileListDisplay(); 
		}

		window.resetAttachments = resetAttachments;
	});

	// 파일 링크 생성 헬퍼 함수 downloadArea영역에생성되는 a태그
	const createFileLink = (fileId, fileName) => {
		const link = document.createElement('a');
		link.href = `/files/download/${fileId}`;
		link.download = fileName;
		link.textContent = `📎 ${fileName}`;
		Object.assign(link.style, {
			display: 'block',
			margin: '5px 0',
			color: '#007bff',
			textDecoration: 'none',
			cursor: 'pointer'
		});
		return link;
	};

	// 결재 문서 첨부파일 로드 및 렌더링
	async function loadAndRenderFiles(docId) {
		const container = document.getElementById('downloadArea');
		if (!container) return console.error('다운로드 영역을 찾을 수 없습니다.');

		container.innerHTML = '파일 목록을 불러오는 중...';

		try {
			const response = await fetch(`/approval/file/${docId}`);
			if (!response.ok) throw new Error(`상태: ${response.status}`);

			const files = await response.json();
			container.innerHTML = '';

			if (!files.length) {
				container.textContent = '첨부된 파일이 없습니다.';
				return;
			}

			files.forEach(file => {
				const fileId = file.fileId;
				const fileName = file.originFileName || file.fileName;
				if (fileId && fileName) container.appendChild(createFileLink(fileId, fileName));
			});

		} catch (error) {
			console.error('첨부파일 로드 실패:', error);
			container.innerHTML = `⚠️ 파일을 불러올 수 없습니다. (${error.message})`;
		}
	}

	// 결재 문서 파일 데이터 로드
	const getApprovalDocFileData = (approvalId) => loadAndRenderFiles(approvalId);

    	
	//f- 등록버튼,폼 결재권한자 데이터 말아서 보내는 함수
	document.getElementById('modal-doc').addEventListener('submit', async function(event) {
    	// 폼의 기본 제출 동작 방지
    	event.preventDefault();

    	// FormData 객체를 사용하여 폼 데이터 수집
    	const formData = new FormData(this);

			//결재문서
		if(approverArr.length != 0){ //결재권한자가 있으면
 			formData.append('docStatus', '1차대기');//문서상태
			formData.append('docApprover', approverArr[0].empId);//결재권한자//1차 empId
		}

		if (approverArr.length === 0) {
        	console.log("결재자 배열이 비어있습니다.");
        	return;
    	}
		//결재권한자 3명까지
		if(approverArr.length > 0){

			//결재권한자 사번,오더순서,열람여부,전결상태
			if(approverArr[0] !== undefined) 
				formData.append('approverEmpIdOVD1', approverArr[0].empId +","
					+ approverArr[0].approverOrder + "," + "Y" +","+approverArr[0].delegateStatus); //결재권한자 아이디 3게
			if(approverArr[1] !== undefined) 
				formData.append('approverEmpIdOVD2', approverArr[1].empId +","
					+ approverArr[1].approverOrder +"," + "N"+","+approverArr[1].delegateStatus);
			if(approverArr[2] !== undefined) 
				formData.append('approverEmpIdOVD3', approverArr[2].empId +"," 
					+ approverArr[2].approverOrder +"," + "N"+"," + approverArr[2].delegateStatus);
			//formData.append('approvalStatus', false);//권한자상태 필요없음
		}


    	// FormData를 일반 JavaScript 객체로 변환
    	//const dataObject = Object.fromEntries(formData.entries());

    	await fetch("/approval/approval_doc", {
				method: 'POST', 
				headers: {
					[csrfHeader]: csrfToken
				},
				body:  formData // 요청 본문에 JSON 데이터 포함
			})
			.then(response => response.text()) // 서버 응답을 JSON으로 파싱
			.then(data => {
				console.log('성공:', data);
				alert('기안서 작성이 완료되었습니다.');
			})
			.catch((error) => {
				console.error('오류:', error);
				alert('기안서 작성을 실패했습니다.');
			});
	});

	//f- 결재권한자변경(전결자) 라디오버튼에 관련된 함수
	document.addEventListener("change", function(event) {

        if (!event.target.matches('input[name="radioJeongyeolja"]')) return;

        const selectBoxElement = document.getElementById('delegetedApprover');
        const selectedEmpName = selectBoxElement.options[selectBoxElement.selectedIndex].text;
        const targetDiv = document.getElementById(`approver_${elemApproverIdNum}`);
        const selectedValue = event.target.value;
        console.log("선택된 이름", selectedEmpName);
        console.log("선택된 전결 상태:", selectedValue);
		console.log("클릭될때", event.target.clicked);
        //console.log("targetDiv");
        if (selectedValue === 'N') {// 결재권한자변경상태가 없음일때
                
            approverArr.forEach(approver => {
            
                if (targetDiv) {
                    targetDiv.querySelectorAll('span').forEach(span => span.remove());
                }
                approver.delegateStatus = 'N';
                approver.empId = approver.originalEmpId;  // 원래 사번 복구
                
                console.log(`결재권한자 ${approver.approverOrder} delegateStatus = N`);
            });
			document.getElementById('delegetedApprover').style.display = "none";
			document.getElementById(`approvalBtn_${elemApproverIdNum}`).style.display = "none";
	 	}else{
			document.getElementById('delegetedApprover').style.display = "block";
			document.getElementById(`approvalBtn_${elemApproverIdNum}`).style.display = "block";
		}
	});

	//f- 결재권한자 변경/전결 적용 함수
	function applyDelegateChange(button) {

		console.log("적용 버튼이 클릭되었습니다.");
		const count = Number(button.dataset.count); // 버튼 자체의 data-count 사용
		console.log();

		//전결에 필요한 로직추가 approverArr 배열에 delegateStatus 값 변경
		// 라디오 버튼값 가져오기
		const radioJeongyeolja = document.getElementsByName('radioJeongyeolja');
		const targetDiv = document.getElementById(`approver_${elemApproverIdNum}`);

		let selectedValue;
		for (const radio of radioJeongyeolja) {
			// console.log("radio value:", radio.value, "checked:", radio.checked);
			if (radio.checked) {
				selectedValue = radio.value;
				break;
			}
		}

    	//alert(count + "번 결재권한자를 전결자로 지정\n부모 div id: " + id);
    	// console.log("적용 버튼 클릭 div id:", id);
    	// console.log("적용 버튼 클릭 div count:", count);
		// console.log("선택된 전결 상태:", selectedValue);

		// console.log("이전의 approverArr:", approverArr);

		// toastui selectbox에서 선택된 사번 가져오기#select-box
		const selectBoxElement = document.getElementById('delegetedApprover');
		const selectedEmpId = selectBoxElement.value;//선택된 사번
		const selectedEmpName = selectBoxElement.options[selectBoxElement.selectedIndex].text;
		
		if (selectedValue == 'N') {
				
	        approverArr.forEach(approver => {
			
	            if (targetDiv) {
	                targetDiv.querySelectorAll('span').forEach(span => span.remove());
	            }
	            approver.delegateStatus = 'N';
				approver.empId = approver.originalEmpId;  // 원래 사번 복구
				
	            console.log(`결재권한자 ${approver.approverOrder} delegateStatus = N`);
	        });
	    }
		if(targetDiv) {
			// 새로운 전결자 표시
			if(selectedValue != 'N') {
				targetDiv.querySelectorAll('span').forEach(span => span.remove());
				targetDiv.innerHTML += `<span style="color:blue;"> ${selectedValue} <br> ${selectedEmpName} </span>`;
			}
		}
		approverArr.forEach((value,key) => {
			console.log("비교 중인 approverOrder:", value.approverOrder, "==", count);
			if(value.approverOrder === count && selectedValue != 'N') {
				// 선택된 전결 상태에 따라 delegateStatus 값 설정
				value.empId = selectedEmpId;//셀렉트 박스 값을 가져와서 넣어야함
				value.delegateStatus = selectedValue;//전결상태 변경
			
				console.log("매핑된 결재권한자:", value);
				console.log(`결재권한자 순서 ${count}의 전결상태가 ${selectedValue}로 변경되었습니다.`);

			}
		});
		console.log("Updated approverArr:", approverArr);
	}

	const Grid = tui.Grid;
	// g- 결재사항
	const grid1 = new Grid({
		  el: document.getElementById('approvalGrid'), 
		  columns: [
	
		    {header: '순번' ,name: 'row_no' ,align: 'center'}
			,{header: '문서id' ,name: 'approval_id' ,align: 'center',hidden: true}
			,{header: '문서제목' ,name: 'approval_title' ,align: 'center',width: 200}
			,{header: '양식' ,name: 'form_type' ,align: 'center'}
			,{header: '사원번호' ,name: 'emp_id' ,align: 'center'}
			,{header: '기안자' ,name: 'emp_name' ,align: 'center'}
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '부서코드' ,name: 'dept_id' ,align: 'center',hidden: true}
			,{header: '부서명' ,name: 'dept_name' ,align: 'center'}
			,{header: '결재권한자id' ,name: 'approver' ,align: 'center',hidden: true}
			,{header: '결재권한자' ,name: 'approver_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center'}
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center',hidden: true}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center',hidden: true}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center',hidden: true}
			,{header: '변경직급' ,name: 'to_pos_code' ,align: 'center',hidden: true}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center',hidden: true}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center',hidden: true}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center',hidden: true}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center', width: 100
				,formatter: (rowInfo) => {
 					return `<button type='button' class='btn btn-primary btn-sm' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}              
		  ],
		  data: []
		  ,bodyHeight: 500 // 그리드 본문의 높이를 픽셀 단위로 지정. 스크롤이 생김.
		  ,height:100
		  ,columnOptions: {
        		resizable: true
      	  }
		  ,pageOptions: {
        		useClient: true,
        		perPage: 10
      	  }
		});
	
		
	// g- 전체결재
	const grid2 = new Grid({
	    el: document.getElementById('allApprovalGrid'), // 전체결재
	    columns: [
	
		   {header: '결재순번' ,name: 'row_no' ,align: 'center'}
			,{header: '문서id' ,name: 'approval_id' ,align: 'center',hidden: true}
			,{header: '문서제목' ,name: 'approval_title' ,align: 'center',width: 200}
			,{header: '양식' ,name: 'form_type' ,align: 'center'}
			,{header: '사원번호' ,name: 'emp_id' ,align: 'center'}
			,{header: '기안자' ,name: 'emp_name' ,align: 'center'}
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '부서코드' ,name: 'dept_id' ,align: 'center',hidden: true}
			,{header: '부서명' ,name: 'dept_name' ,align: 'center'}
			,{header: '결재권한자id' ,name: 'approver' ,align: 'center',hidden: true}
			,{header: '결재권한자' ,name: 'approver_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center'}
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center',hidden: true}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center',hidden: true}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center',hidden: true}
			,{header: '변경직급' ,name: 'to_pos_code' ,align: 'center',hidden: true}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center',hidden: true}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center',hidden: true}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center',hidden: true}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center'
				,formatter: (rowInfo) => {
 					return `<button type='button' class='btn btn-primary btn-sm' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
		  ,bodyHeight: 500
		  ,columnOptions: {
        	resizable: true
      	  }
		  ,pageOptions: {
        		useClient: true,
        		perPage: 10
      	  }
	});
	//g- 내결재목록
	const grid3 = new Grid({
	    el: document.getElementById('myApprovalGrid'), // 내 결재목록
	    columns: [
	
		    {header: '결재순번' ,name: 'row_no' ,align: 'center'}
			,{header: '문서id' ,name: 'approval_id' ,align: 'center',hidden: true}
			,{header: '문서제목' ,name: 'approval_title' ,align: 'center',width: 200}
			,{header: '양식' ,name: 'form_type' ,align: 'center'}
			,{header: '사원번호' ,name: 'emp_id' ,align: 'center'}
			,{header: '기안자' ,name: 'emp_name' ,align: 'center'}
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '부서코드' ,name: 'dept_id' ,align: 'center',hidden: true}
			,{header: '부서명' ,name: 'dept_name' ,align: 'center'}
			,{header: '결재권한자id' ,name: 'approver' ,align: 'center',hidden: true}
			,{header: '결재권한자' ,name: 'approver_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center'}
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center',hidden: true}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center',hidden: true}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center',hidden: true}
			,{header: '변경직급' ,name: 'to_pos_code' ,align: 'center',hidden: true}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center',hidden: true}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center',hidden: true}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center',hidden: true}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center'
				,formatter: function(rowInfo) {
 					return `<button type='button' class='btn btn-primary btn-sm' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
		  ,bodyHeight: 500
		  ,columnOptions: {
        	resizable: true
      	  }
		  ,pageOptions: {
        		useClient: true,
        		perPage: 10
      	  }
	});
	//g- 결재대기
	const grid4 = new Grid({
	    el: document.getElementById('waitingApprovalGrid'), //결재대기
	    columns: [
	
		    {header: '결재순번' ,name: 'row_no' ,align: 'center'}
			,{header: '문서id' ,name: 'approval_id' ,align: 'center',hidden: true}
			,{header: '문서제목' ,name: 'approval_title' ,align: 'center',width: 200}
			,{header: '양식' ,name: 'form_type' ,align: 'center'}
			,{header: '사원번호' ,name: 'emp_id' ,align: 'center'}
			,{header: '기안자' ,name: 'emp_name' ,align: 'center'}
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '부서코드' ,name: 'dept_id' ,align: 'center',hidden: true}
			,{header: '부서명' ,name: 'dept_name' ,align: 'center'}
			,{header: '결재권한자id' ,name: 'approver' ,align: 'center',hidden: true}
			,{header: '결재권한자' ,name: 'approver_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center'}
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center',hidden: true}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center',hidden: true}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center',hidden: true}
			,{header: '변경직급' ,name: 'to_pos_code' ,align: 'center',hidden: true}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center',hidden: true}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center',hidden: true}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center',hidden: true}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center'
				,formatter: function(rowInfo) {
 					return `<button type='button' class='btn btn-primary btn-sm' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
		  ,bodyHeight: 500
		  ,columnOptions: {
        	resizable: true
      	  }
		  ,pageOptions: {
        	useClient: true,
        	perPage: 10
      	  }
	});
	//g- 결재완료
	const grid5 = new Grid({
	    el: document.getElementById('doneApprovalGrid'), //결재완료
	    columns: [
	
		    {header: '결재순번' ,name: 'row_no' ,align: 'center'}
			,{header: '문서id' ,name: 'approval_id' ,align: 'center',hidden: true}
			,{header: '문서제목' ,name: 'approval_title' ,align: 'center',width: 200}
			,{header: '양식' ,name: 'form_type' ,align: 'center'}
			,{header: '사원번호' ,name: 'emp_id' ,align: 'center'}
			,{header: '기안자' ,name: 'emp_name' ,align: 'center'}
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '부서코드' ,name: 'dept_id' ,align: 'center',hidden: true}
			,{header: '부서명' ,name: 'dept_name' ,align: 'center'}
			,{header: '결재권한자id' ,name: 'approver' ,align: 'center',hidden: true}
			,{header: '결재권한자' ,name: 'approver_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center'}
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center',hidden: true}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center',hidden: true}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center',hidden: true}
			,{header: '변경직급' ,name: 'to_pos_code' ,align: 'center',hidden: true}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center',hidden: true}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center',hidden: true}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center',hidden: true}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center'
				,formatter: function(rowInfo) {
 					return `<button type='button' class='btn btn-primary btn-sm' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
		  ,bodyHeight: 500
		  ,columnOptions: {
        	resizable: true
      	  }
		  ,pageOptions: {
        		useClient: true,
        		perPage: 10
      	  }
	});
	
	Grid.applyTheme('clean'); // Call API of static method
	//f- 날짜,기안자,문서양식 조회 불러오는 함수
	function AllGridSearch() {
		console.log("AllGridSearch()-----> 해당함수 로딩시실행잘되나??");
			const params = {
				
			 	createDate: document.getElementById("searchStartDate").value ?? "",
	    	 	finishDate: document.getElementById("searchEndDate").value ?? "",
	    	 	empName: document.getElementById("searchEmpIdAndformType").value ?? "",
				approvalTitle: document.getElementById("searchEmpIdAndformType").value ?? ""
			};

			fetch('/approval/searchAllGrids', {
				method: 'POST',
				headers:{
					[csrfHeader]: csrfToken,
					'Content-Type': 'application/json'
				},
				body: JSON.stringify(params)
			})
        	.then(res => {
      			if (!res.ok) {
            		throw new Error(`HTTP error! status: ${res.status}`);
        		}
        		return res.json();
    		})
        	.then(data => {
            	grid1.resetData(data.grid1Data);
				grid2.resetData(data.grid2Data);
				grid3.resetData(data.grid3Data);
				grid4.resetData(data.grid4Data);
				grid5.resetData(data.grid5Data);
				console.log("검색데이터:",data);
        	})
        	.catch(err => {
            	console.error("조회오류", err);
				grid1.resetData([]);
				grid2.resetData([]);
				grid3.resetData([]);
				grid4.resetData([]);
				grid5.resetData([]);
        	});
			console.log("params:",params);

	}
	const searchBtn = document.getElementById("searchBtn");
    if (searchBtn) {
        searchBtn.addEventListener("click", (ev) => {
		
		});

    }
	
	// 서버에서 받아온 default 결재권자 담을 변수
	let formList = [];
	// 선택한 양식을 담을 변수
	let selectedForm = null;

	// f- default 결재권자 가져오는 함수
	async function defalutapprover() {
		const res = await fetch("/api/approvals/defaultApprover", {method: "GET"});
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패!");
		}
		
		formList = await res.json();
	}

	//모달창 코드
	//f- 기안서 셀렉트 박스 변경시 모달창에 텍스트 변경함수
	function draftValFn(ev){
		let draft_doc = ev.value;

		document.getElementById('saveBtn').style.display = "block";//등록
		document.getElementById('attachmentBtn').style.display = 'block';//첨부파일
		document.getElementById('downloadArea').style.display = "none";//다운로드
		//document.getElementById('DraftingHidden').value = draft_doc;

		// html에서 th:data-formname="${item.formName}" 값을 가져와서 이름으로 사용
		const formName = ev.selectedOptions[0].dataset.formname;
		console.log("ev.selectedOptions[0].dataset.formname",ev.selectedOptions[0].dataset.formname);
		
		document.getElementById('DraftingHidden').value = formName;
		document.getElementById('Drafting').innerText = formName;
		// 선택한 결재 양식과 서버에서 받아온 데이터 중 일치하는 값 찾기
		selectedForm = formList.find(item => item.formName === draft_doc);
		console.log("draft_doc",draft_doc);
		//양식종류에따라 보여지는 화면이 다름
		document.getElementById('approvalCompanionBtn').style.display = "none";//반려
		document.getElementById('approvalCheckBtn').style.display = "none";//결재확인
		formChange(draft_doc);
		
		formReset();
		defaultPrint();
	}
	//f- 양식 모달 리셋함수
	function formReset(ev){
    
    	// Null 체크 추가
    	const draftingElement = document.getElementById('Drafting');
    	if (draftingElement) { // draftingElement가 null인지 체크
    	    draftingElement.innerText = selectedForm.formName;
    	}
	
    	// Null 체크 추가
    	const draftingHiddenElement = document.getElementById("DraftingHidden");
    	if (draftingHiddenElement) { // draftingHiddenElement가 null인지 체크
    	    draftingHiddenElement.value = selectedForm.formName; // 양식종류 숨은값
    	}

		//document.getElementById("DraftingHidden").value = selectedForm.formName;//양식종류 숨은값
		//document.getElementById('Drafting').innerText = selectedForm.formName;
		//document.getElementById("DraftingHidden").value = selectedForm.formName;//양식종류 숨은값

		document.getElementById("approval-title").value = "";//문서제목
		//document.getElementById("approver-name").value ="";//결재자명 - 로그인정보에서 불러옴
		document.getElementById("create-date").value = null;//문서 생성일자
		document.getElementById("finish-date").value = null;//결재완료기간
		document.getElementById("start-date").value = null;//휴가신청서 시작날짜
		document.getElementById("end-date").value = null;//휴가신청서 종료날짜
		document.getElementById("leave-type").selectedIndex = 0;//휴가종류
		document.getElementById("to-dept-id").selectedIndex = 0;//발령부서
		document.getElementById("expnd-type").selectedIndex = 0;//지출종류
		document.getElementById("reason-write").value = "";//사유내용
		//selectBox.resetItems();
		//selectBox.setItems(itemData);

		//const originalSelect = document.getElementById('select-box');
		//originalSelect.value = '';
		//selectBox.select(null);
	}
	
	let today = new Date();   
	let year = today.getFullYear(); // 년도
	let month = today.getMonth() + 1;  // 월
	let date = today.getDate();  // 날짜
	let day = today.getDay();  // 요일

	const formattedDate = `${year}년 ${month}월 ${date}일`;
	document.getElementById("today-date").textContent = formattedDate;

	let jeongyeoljaDiv = document.querySelector('#jeongyeolja');
	let jeongyeoljaContent = document.querySelector("#jeongyeolja-content");
	let approverDivClose = document.getElementById("approverDiv-close");
	
    this.count = 0; //결재권한자 label count
    let defalutapproverArr = ["d-이사랑","d-미미미누","d-김경란"];
    let approverArr = [];//결재권한자 배열 
	let writeBtn = document.getElementById("writeBtn");
	
	//모달이 닫힐떄 첨부파일 리셋
	const approvalModal = document.getElementById('approval-modal');
	approvalModal.addEventListener('hidden.bs.modal', function (event) {
		resetAttachments(); 
	});

	//f- 기안서작성 모달이 열리기전에 이벤트를 감지
	$('#approval-modal').on('show.bs.modal', function (e) {
		// e.relatedTarget이 null/undefined이면 .dataset 접근을 멈추고 actionType에 undefined 할당
    	let actionType = e.relatedTarget?.dataset?.action; 

    	// actionType이 유효할 때만 로직을 실행합니다.
    	if (!selectedForm && actionType === 'create') { 
    	    e.preventDefault();
    	    alert("양식을 선택해주세요.");  
    	} else {
    	    console.log(" 모달 열기 진행");
    	}
	 });
			
	//f- 작성 버튼 클릭 시 실행되는 함수
  	function defaultPrint(){
		// 모달을 닫고 다시 작성 버튼을 클릭하면 이전 데이터가 남아있어서 초기화 진행
		approverDiv.innerHTML = "";
		formReset();
		formEnable();
		//selectBox.enable();
		window.count = 0;
		approverArr = [];
		// selectedForm 값이 없을 경우 에러가 생길 수 있어서 에러 처리
		//<option selected>기안서</option> 해당구문 없앨시에 마지막인덱스로됨
		if (!selectedForm) {
			console.log('모달을 열 수 없습니다.');
			return;

			// document.getElementById('leavePeriodForm').style.display = 'flex';
			// document.getElementById('leaveTypeForm').style.display = 'flex';
			// document.getElementById('expndTypeForm').style.display = 'flex';
			// document.getElementById('toDeptForm').style.display = 'flex';
		}
		
		defalutapproverArr = []; //디폴트 결재권한자 초기화
		for (let i = 1; i <= 3; i++) {
		// selectedForm의 approver1, approver2, approver3을 가져오기 위해서 템플릿 문자열 사용
		    const approver = selectedForm[`approver${i}`] + " " + selectedForm[`approver${i}Name`];

			// 결재권자가 없으면 화면에 출력되지 않도록 처리
			if (selectedForm[`approver${i}`] == null) {
			 	break;
			}
			
		    if (approver) {//디폴트 결재권한자 라벨이 null이 아닐때
				defalutapproverArr.push(approver);
				console.log("defalutapproverArr",defalutapproverArr);
		    }

			console.log("추출된 기본 결재자:", defalutapproverArr);

    	}
		// 4. 기본 결재 라인 설정 (this.count가 0일 때만 실행)
    	// 이 로직은 결재 라인에 아무도 없을 때만 기본값을 넣어주기 위한 로직입니다.
    	if (window.count === 0) {
	
        	defalutapproverArr.forEach(approver => {

				const approverParts = approver.split(" ");
				const approverEmpId = approverParts[0];
				print("defalut", approver);

				approverArr.push({
					empId: approverEmpId,
					approverOrder: window.count,
					delegateStatus: 'N', 
					originalEmpId: approverEmpId
				});
        	});
			console.log("approverArr 실행 후:", approverArr);
		}	

	}

	defalutapprover();
	//f- 결재권한자 div 버튼 생성 함수
	function print(type, text) {
    	
    	if(this.count < 3){
    		this.count++;
    		approverDiv.innerHTML +='<div class="btn btn-success"'
    		                      +'style="width:250px;height:200px; margin:5px; padding: 5px 0px 0px 0px;">'
    		                      +'<p onclick="approverDivclose(this,' + "'"+ type + "'"+ ','+ count +')" style="float:right;margin-right: 8px;">&times;</p>'
    		                      +'<p id="approver_'+count+'" onclick="approvalNo('+ (this.count)+','+ "'"+ text + "'" +')" style="margin-top:30px;height: 129px;font-size:22px;">'+(this.count) + '차 결재권한자 '+'<br>'+ text + '<br>' + '</p>'
    		                    	+'</div>';
		}
    }

	
	//f- 결재권한자 버튼 클릭시 결재권한자변경 div 태그 생성//전결자
	function approvalNo(count, text) {
		elemApproverIdNum = count;
	    let type = "change";
	    if (jeongyeoljaDiv) {
	        // div 초기화
	        jeongyeoljaDiv.innerHTML = `
	            <button type="button" onClick="approverDivclose(this, '${type}', ${count})" class="btn-close" style="float:right;margin-right: 8px;"></button>
	            <h5>${count}차 결재권한자 : ${text} 변경</h5>
	            ${jeongyeoljaContent.innerHTML}
	            <button id="approvalBtn_${count}" 
	                    type="button" class="btn btn-primary" 
	                    data-count="${count}" 
	                    onclick="applyDelegateChange(this)"
						style="display:none;">
	                전결자로 지정
	            </button>
	        `;
	        jeongyeoljaDiv.style.display = 'block';
	    }
	}
	//f- 결재권한자,결재권한자변경(전결자) 닫기버튼
	function approverDivclose(buttonDiv,type,count){
		const divElement = buttonDiv.parentNode; // 버튼의 부모인 div를 찾음
		console.log("type",type);
		
		jeongyeoljaDiv.style.display = 'none';
		//defalut 태그 닫기 버튼시 
		if (buttonDiv.parentElement.id === "" || type === "defalut") {//결재권한자
		    divElement.remove(); //자신의 div 제거
		
		    if(divElement.innerText !== null){ //defalut 태그가 있을때
				approverArr = approverArr.filter((ev) => ev.approverOrder !== count);
		    }
			approverArr = approverArr.filter((ev) => ev !== count);
			this.count = count-1; //제거 라벨 카운트 원상복기
		}
		if(type === "close"){ //전결자 변경 닫기버튼시
			divElement.remove(); //자신의 div 제거
			//전결자 변경시 결재권한자 배열에서 해당 결재권한자 제거
			approverArr = approverArr.filter((ev) => ev.approverOrder !== count);
		}
		if(approverArr.length === 0){
	    	this.count = 0;
	    }	
	}

	//에디터-없앰
	// const editor = new toastui.Editor({
	// 	el: document.querySelector('#editor'),
	//   	height: '500px',
	//   	initialEditType: 'markdown',
	//   	previewStyle: 'vertical'
	// });
	
	// editor.getMarkdown();

	
	//모달 움직이게 하기
	const modalHeader = document.querySelector(".modal-header");
	const modalDialog = document.querySelector(".modal-dialog");
	let isDragging = false;
	let mouseOffset = { x: 0, y: 0 };
	let dialogOffset = { left: 0, right: 0 };
	
	modalHeader.addEventListener("mousedown", function (event) {
	  isDragging = true;
	  mouseOffset = { x: event.clientX, y: event.clientY };
	  dialogOffset = {
	    left: modalDialog.style.left === '' ? 0 : Number(modalDialog.style.left.replace('px', '')),
	    right: modalDialog.style.top === '' ? 0 : Number(modalDialog.style.top.replace('px', ''))
	  }
	});
	
	document.addEventListener("mousemove", function (event) {
	  if (!isDragging) {
	    return;
	  }
	  let newX = event.clientX - mouseOffset.x;
	  let newY = event.clientY - mouseOffset.y;
	
	  modalDialog.style.left = `${dialogOffset.left + newX}px`
	  modalDialog.style.top = `${dialogOffset.right + newY}px`
	});
	
	document.addEventListener("mouseup", function () {
	  isDragging = false;
	});
