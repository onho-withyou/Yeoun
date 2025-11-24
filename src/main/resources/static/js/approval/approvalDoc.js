//결재.js
	// 현재 로그인한 사용자 EMP_ID
	const LOGIN_USER_ID = document.getElementById('currentUserId').value;
	const LOGIN_USER_NAME = document.getElementById('currentUserName').value;
	// 현재 열린 문서의 approvalId
	let approvalId;
	// 현재 열린 문서의 결재권자(approval) 
	let currentApprover;
	// 모달의 결재확인 버튼
	
	const csrfToken = document.querySelector('meta[name="_csrf_token"]')?.content;
	const csrfHeaderName = document.querySelector('meta[name="_csrf_headerName"]')?.content;
	// 결제확인 버튼
	const approvalCheckBtn = document.getElementById('approvalCheckBtn');
	// 반려 버튼
	const approvalCompanionBtn = document.getElementById('approvalCompanionBtn');
	
	// ========================================================
	// v - 결재권한자
	let elemApproverIdNum = null;//결재권한자 count
	// ========================================================
	
	// f - 결재확인 버튼 눌렀을때 동작할 함수
	approvalCheckBtn.addEventListener('click', () => {
		patchApproval("accept");
	});
	
	// 반려버튼 눌렀을때 동작할 함수
	approvalCompanionBtn.addEventListener('click', () => {
		patchApproval("deny")		
	});
	
	// null-safe 날짜 변환 함수
	function toDateStr(value) {
	  if (!value) return '';              // null, undefined, '' 전부 빈 문자열 처리
	  return String(value).split('T')[0]; // 혹시 문자열 아니어도 방어
	}
	
		
	// 현재 로그인한 사용자와 결재권자 비교
	function checkApprover() {
		if(currentApprover != LOGIN_USER_ID) {
			alert("승인 또는 반려권한이 없습니다."); 
			return true;
		}
	}
	
	// 결재 패치 보내기 함수
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
					[csrfHeaderName]: csrfToken
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
	
	//결제상세보기 => 결제권자 정보 불러오기함수
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
		fetchPendingApprovalDocs().then(data => {
			grid1.resetData(data);
		});
		fetchApprovalDocs().then(data => {
			grid2.resetData(data);
		});
		fetchMyApprovalDocs().then(data => {
			grid3.resetData(data);
		});
		fetchWaitingApprovalDocs().then(data => {
			grid4.resetData(data);
		});
		fetchDoneApprovalDocs().then(data =>{
			grid5.resetData(data);
		});		
		empData();

		
	}
	
	let approverDiv = document.querySelector('#approver');
	let selectBox;
	let itemData;
	//selectbox - 인사정보 불러오기
	async function empData() {
		try {
			const response = await fetch("/approval/empList");
			const data = await response.json();
			itemData  = [];
			let obj ={};
			//console.log(data);
			data.map((item,index)=>{
				obj["value"] = item[0]; //사번
				obj["label"] = (index+1) +" : "+item[1]+"("+item[0]+")"; //이름(사번)
				itemData.push(obj);
				obj = {};
			});
			
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
				const rowData = grid1.getRow(ev.rowKey);
				$('#approval-modal').modal('show');
				//formReset();
				document.getElementById('saveBtn').style.display = "none";
				//document.getElementById('approvalCompanionBtn').style.display = "block";
				//document.getElementById('approvalCheckBtn').style.display = "block";
				// 문서 열릴때 approvalId에 현재 열린 문서id 저장
				approvalId = rowData.approval_id;
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
				document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서,디비잘못넣음
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
						print("default", selectBox.getSelectedItem().label);
					}
					
				}
				//document.getElementById('approver').innerText = rowData.approver;//전결자
				document.getElementById('reason-write').value = rowData.reason;//결재사유내용
				//selectBox.disable();
				formDisable();
				
			});

			grid2.on("click", async (ev) => {
		
				const target = ev.nativeEvent.target;
				
				const rowData = grid2.getRow(ev.rowKey);
				$('#approval-modal').modal('show');
				
				document.getElementById('saveBtn').style.display = "none";
			
				// 문서 열릴때 approvalId에 현재 열린 문서id 저장
				approvalId = rowData.approval_id;
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
			
				document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서,디비잘못넣음
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
						print("default", selectBox.getSelectedItem().label);
					}
					
				}
				//document.getElementById('approver').innerText = rowData.approver;//전결자
				document.getElementById('reason-write').value = rowData.reason;//결재사유내용
				//selectBox.disable();
				formDisable();
			});


			grid3.on("click", async (ev) => {
		
				const target = ev.nativeEvent.target;
				const rowData = grid3.getRow(ev.rowKey);
				$('#approval-modal').modal('show');
				
				document.getElementById('saveBtn').style.display = "none";
			
				// 문서 열릴때 approvalId에 현재 열린 문서id 저장
				approvalId = rowData.approval_id;
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
			
				document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서,디비잘못넣음
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
						print("default", selectBox.getSelectedItem().label);
					}
					
				}
				//document.getElementById('approver').innerText = rowData.approver;//전결자
				document.getElementById('reason-write').value = rowData.reason;//결재사유내용
				//selectBox.disable();
				formDisable();
			});

			grid4.on("click", async (ev) => {
			
			const target = ev.nativeEvent.target;
			const rowData = grid4.getRow(ev.rowKey);
			$('#approval-modal').modal('show');
			
			document.getElementById('saveBtn').style.display = "none";
			
			// 문서 열릴때 approvalId에 현재 열린 문서id 저장
			approvalId = rowData.approval_id;
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
		
			document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서,디비잘못넣음
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
						print("default", selectBox.getSelectedItem().label);
					}
					
				}
			//document.getElementById('approver').innerText = rowData.approver;//전결자
			document.getElementById('reason-write').value = rowData.reason;//결재사유내용
			//electBox.disable();
			formDisable();
			});
	
			grid5.on("click", async (ev) => {
			
				const target = ev.nativeEvent.target;
				
				const rowData = grid5.getRow(ev.rowKey);
				$('#approval-modal').modal('show');
				
				document.getElementById('saveBtn').style.display = "none";
				// 문서 열릴때 approvalId에 현재 열린 문서id 저장
				approvalId = rowData.approval_id;
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
			
				document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서,디비잘못넣음
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
							print("default", selectBox.getSelectedItem().label);
						}
						
					}
				//document.getElementById('approver').innerText = rowData.approver;//전결자
				document.getElementById('reason-write').value = rowData.reason;//결재사유내용
				//selectBox.disable();
				formDisable();
			});
	
			return itemData;
		} catch (error) {
			console.error('Error fetching data:', error);
		}
	}	
	//그리드 클릭시 상세보기 document.getElementById('myInput').disabled = true;
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
		document.getElementById('to-dept-id').disabled = true;
		document.getElementById('expnd-type').disabled = true;
		document.getElementById('reason-write').disabled = true;
		if (selectBox) { 
			selectBox.disable();
		}
	}
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
		
		if (selectBox) { // 인스턴스가 존재하는지 확인
        	selectBox.enable(); 
    	}
	}

	//폼 결재권한자 데이터 말아서 보내는 함수
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
			//formData.append('orderApprovers', null);//결재권한자 순서 3개
		}


    	// FormData를 일반 JavaScript 객체로 변환
    	const dataObject = Object.fromEntries(formData.entries());

    	await fetch("/approval/approval_doc", {
				method: 'POST', // POST 메소드 지정
				headers: {
					[csrfHeaderName]: csrfToken
					,'Content-Type': 'application/json' // Content-Type 헤더를 application/json으로 설정
				},
				body:  JSON.stringify(dataObject) // 요청 본문에 JSON 데이터 포함
			})
			.then(response => response.text()) // 서버 응답을 JSON으로 파싱
			.then(data => {
				console.log('성공:', data);
				alert('데이터 전송 성공!');
			})
			.catch((error) => {
				console.error('오류:', error);
				alert('데이터 전송 중 오류 발생');
			});
	});


	document.addEventListener("change", function(event) {

	    if (!event.target.matches('input[name="radioJeongyeolja"]')) return;

		const selectBoxElement = document.getElementById('delegetedApprover');
		const selectedEmpName = selectBoxElement.options[selectBoxElement.selectedIndex].text;
		const targetDiv = document.getElementById(`approver_${elemApproverIdNum}`);
	    const radio = event.target;
	    const selectedValue = radio.value;
		console.log("선택된 이름", selectedEmpName);
	    console.log("선택된 전결 상태:", selectedValue);
		//console.log("targetDiv");
	    if (selectedValue == 'N') {
		
	       //alert("전결자가 없습니다.");
		
	        approverArr.forEach(approver => {
			
	            if (targetDiv) {
	                targetDiv.querySelectorAll('span').forEach(span => span.remove());
	            }
	            approver.delegateStatus = 'N';
				approver.empId = approver.originalEmpId;  // 원래 사번 복구
				
	            console.log(`결재권한자 ${approver.approverOrder} delegateStatus = N`);
	        });
			//console.log(" approverArr----------------->:", approverArr);
	    }
		// console.log("targetDiv:------------------------------------>", targetDiv);
		if(targetDiv) {
			// 새로운 전결자 표시
			if(selectedValue != 'N') {
				targetDiv.querySelectorAll('span').forEach(span => span.remove());
				targetDiv.innerHTML += `<span style="color:red;"> ${selectedValue} : ${selectedEmpName} </span>`;
			}
		}
	});
	// 결재권한자 변경/전결 적용 함수
	function applyDelegateChange(button) {

		console.log("적용 버튼이 클릭되었습니다.");
		console.log("button.dataset.count ---------->", button.dataset.count);
		const count = Number(button.dataset.count); // 버튼 자체의 data-count 사용
		const parentDiv = button.parentNode;        // 부모 div
		const id = parentDiv.id || "jeongyeoljaDiv"; // 부모 div id
		console.log();

		//전결에 필요한 로직추가 approverArr 배열에 delegateStatus 값 변경
		// 라디오 버튼값 가져오기
		const radioJeongyeolja = document.getElementsByName('radioJeongyeolja');
		const targetDiv = document.getElementById(`approver_${elemApproverIdNum}`);

		let selectedValue;
		for (const radio of radioJeongyeolja) {
			console.log("radio value:", radio.value, "checked:", radio.checked);
			if (radio.checked) {
				selectedValue = radio.value;
				break;
			}
		}

    	//alert(count + "번 결재권한자를 전결자로 지정\n부모 div id: " + id);
    	console.log("적용 버튼 클릭 div id:", id);
    	console.log("적용 버튼 클릭 div count:", count);
		console.log("선택된 전결 상태:", selectedValue);

		console.log("이전의 approverArr:", approverArr);

		// toastui selectbox에서 선택된 사번 가져오기#select-box
		const selectBoxElement = document.getElementById('delegetedApprover');
		console.log("selectBoxElement:---------------->", selectBoxElement);
		const selectedEmpId = selectBoxElement.value;
		//const selectedEmpName = selectBoxElement.options[selectBoxElement.selectedIndex].text;
		
		console.log("선택된 사번:", selectedEmpId);
		
	
		approverArr.forEach((value,key) => {
			console.log("비교 중인 approverOrder:", value.approverOrder, "==", count);
			if(value.approverOrder === count) {
				// 선택된 전결 상태에 따라 delegateStatus 값 설정
				value.empId = selectedEmpId;//셀렉트 박스 값을 가져와서 넣어야함
				value.delegateStatus = selectedValue;
			
				console.log("매핑된 결재권한자:", value);
				console.log(`결재권한자 순서 ${count}의 전결상태가 ${selectedValue}로 변경되었습니다.`);

			}
			
			return approver;
		});
		console.log("Updated approverArr:", approverArr);
	}

	//1. 결재사항 불러오기
	async function fetchPendingApprovalDocs() {
		try {
			const response = await fetch('/approval/pendingApprovalDocGrid');
			const data = await response.json();
			let colData = [];
			let obj = {};
			// console.log("grid1 fetch-data----->:",data);
			data.map((item, index) => {
				obj["row_no"] = item[0]; //결재순번
				obj["approval_id"] = item[1]; //문서id
				obj["approval_title"] = item[2]; //문서제목
				obj["form_type"] = item[3];	//폼양식
				obj["emp_id"] = item[4]; //사원번호
				obj["emp_name"] = item[5]; //기안자
				obj["dept_id"] = item[6]; //부서코드
				obj["dept_name"] = item[7]; //부서명
				obj["approver"] = item[8]; //결재권한자id
				obj["approver_name"] = item[9]; //결재권한자 이름
				obj["pos_code"] = item[10]; //직급코드
				obj["pos_name"] = item[11]; //직급
				obj["created_date"] = toDateStr(item[12]); //생성일
				obj["finish_date"]  = toDateStr(item[13]); //결재완료일자
				obj["start_date"]   = toDateStr(item[14]); //휴가시작일자
				obj["end_date"]     = toDateStr(item[15]); //휴가종료일자
				obj["leave_type"] = item[16]; //	
				obj["to_dept_id"] = item[17]; //	
				obj["expnd_type"] = item[18]; //
				obj["reason"] = item[19]; //	
				obj["doc_status"] = item[20]; //상태
				colData.push(obj);
				obj = {};
			});
			return colData;
		} catch (error) {
			console.error('Error fetching approval documents:', error);
		}
	}
	//2. 전체결재 목록 불러오기
	async function fetchApprovalDocs() {
		try {
			const response = await fetch('/approval/approvalDocGrid');
			const data = await response.json();
			let colData = [];
			let obj = {};

			data.map((item, index) => {
				obj["row_no"] = item[0]; //결재순번
				obj["approval_id"] = item[1]; //문서id
				obj["approval_title"] = item[2]; //문서제목
				obj["form_type"] = item[3];	
				obj["emp_id"] = item[4]; //사원번호
				obj["emp_name"] = item[5]; //기안자
				obj["dept_id"] = item[6]; //부서코드
				obj["dept_name"] = item[7]; //부서명
				obj["approver"] = item[8]; //결재권한자
				obj["approver_name"] = item[9]; //결재권한자 이름
				obj["pos_code"] = item[10]; //직급코드
				obj["pos_name"] = item[11]; //직급
				obj["created_date"] = toDateStr(item[12]); //생성일
				obj["finish_date"]  = toDateStr(item[13]); //결재완료일자
				obj["start_date"]   = toDateStr(item[14]); //휴가시작일자
				obj["end_date"]     = toDateStr(item[15]); //휴가종료일자
				obj["leave_type"] = item[16]; //	
				obj["to_dept_id"] = item[17]; //	
				obj["expnd_type"] = item[18]; //
				obj["reason"] = item[19]; //	
				obj["doc_status"] = item[20]; //상태
				colData.push(obj);
				obj = {};
			});
			return colData;
		} catch (error) {
			console.error('Error fetching approval documents:', error);
		}
	}
	//3.내 결재목록 불러오기
	async function fetchMyApprovalDocs() {
		try {
			const response = await fetch('/approval/myApprovalDocGrid');
			const data = await response.json();
			// console.log("grid3 fetch-data----->:",data);
			let colData = [];
			let obj = {};

			data.map((item, index) => {
				obj["row_no"] = item[0]; //결재순번
				obj["approval_id"] = item[1]; //문서id
				obj["approval_title"] = item[2]; //문서제목
				obj["form_type"] = item[3];//폼양식	
				obj["emp_id"] = item[4]; //사원번호
				obj["emp_name"] = item[5]; //기안자
				obj["dept_id"] = item[6]; //부서코드
				obj["dept_name"] = item[7]; //부서명
				obj["approver"] = item[8]; //결재권한자
				obj["approver_name"] = item[9]; //결재권한자 이름
				obj["pos_code"] = item[10]; //직급코드
				obj["pos_name"] = item[11]; //직급
				obj["created_date"] = toDateStr(item[12]); //생성일
				obj["finish_date"]  = toDateStr(item[13]); //결재완료일자
				obj["start_date"]   = toDateStr(item[14]); //휴가시작일자
				obj["end_date"]     = toDateStr(item[15]); //휴가종료일자
				obj["leave_type"] = item[16]; //	
				obj["to_dept_id"] = item[17]; //	
				obj["expnd_type"] = item[18]; //
				obj["reason"] = item[19]; //	
				obj["doc_status"] = item[20]; //상태
				colData.push(obj);
				obj = {};
			});
			return colData;
		} catch (error) {
			console.error('Error fetching approval documents:', error);
		}
	}
	//4.결재대기 불러오기 -- 1차반려,2차반려,3차반려,1차완료,2차완료,3차완료, 종료
	async function fetchWaitingApprovalDocs() {
		try {
			const response = await fetch('/approval/waitingApprovalDocGrid');
			const data = await response.json();
			//console.log("grid4 fetch-data----->:", data);
			let colData = [];
			let obj = {};

			data.map((item, index) => {
				obj["row_no"] = item[0]; //결재순번
				obj["approval_id"] = item[1]; //문서id
				obj["approval_title"] = item[2]; //문서제목	
				obj["form_type"] = item[3];
				obj["emp_id"] = item[4]; //사원번호
				obj["emp_name"] = item[5]; //기안자
				obj["dept_id"] = item[6]; //부서코드
				obj["dept_name"] = item[7]; //부서명
				obj["approver"] = item[8]; //결재권한자
				obj["approver_name"] = item[9]; //결재권한자 이름
				obj["pos_code"] = item[10]; //직급코드
				obj["pos_name"] = item[11]; //직급
				obj["created_date"] = toDateStr(item[12]); //생성일
				obj["finish_date"]  = toDateStr(item[13]); //결재완료일자
				obj["start_date"]   = toDateStr(item[14]); //휴가시작일자
				obj["end_date"]     = toDateStr(item[15]); //휴가종료일자
				obj["leave_type"] = item[16]; //	
				obj["to_dept_id"] = item[17]; //	
				obj["expnd_type"] = item[18]; //
				obj["reason"] = item[19]; //	
				obj["doc_status"] = item[20]; //상태
				colData.push(obj);
				obj = {};
			});

			//console.log("grid4 map------>:", colData);
			return colData;
		} catch (error) {
			console.error('Error fetching approval documents:', error);
		}
	}
	//5.결재완료 불러오기
	async function fetchDoneApprovalDocs() {
		try {
			const response = await fetch('/approval/finishedApprovalDocGrid');
			const data = await response.json();
			//console.log("grid5 fetch-data----->:", data);
			let colData = [];
			let obj = {};

			data.map((item, index) => {
				obj["row_no"] = item[0]; //결재순번
				obj["approval_id"] = item[1]; //문서id
				obj["approval_title"] = item[2]; //문서제목	
				obj["form_type"] = item[3];
				obj["emp_id"] = item[4]; //사원번호
				obj["emp_name"] = item[5]; //기안자
				obj["dept_id"] = item[6]; //부서코드
				obj["dept_name"] = item[7]; //부서명
				obj["approver"] = item[8]; //결재권한자
				obj["approver_name"] = item[9]; //결재권한자 이름
				obj["pos_code"] = item[10]; //직급코드
				obj["pos_name"] = item[11]; //직급
				obj["created_date"] = toDateStr(item[12]); //생성일
				obj["finish_date"]  = toDateStr(item[13]); //결재완료일자
				obj["start_date"]   = toDateStr(item[14]); //휴가시작일자
				obj["end_date"]     = toDateStr(item[15]); //휴가종료일자
				obj["leave_type"] = item[16]; //	
				obj["to_dept_id"] = item[17]; //	
				obj["expnd_type"] = item[18]; //
				obj["reason"] = item[19]; //	
				obj["doc_status"] = item[20]; //상태
				colData.push(obj);
				obj = {};
			});

			//console.log("grid5 map------>:", colData);
			return colData;
		} catch (error) {
			console.error('Error fetching approval documents:', error);
		}
	}

	const Grid = tui.Grid;
	const grid1 = new Grid({
		  el: document.getElementById('approvalGrid'), // 결재사항
		  columns: [
	
		    {header: '결재순번' ,name: 'row_no' ,align: 'center'}
			,{header: '문서id' ,name: 'approval_id' ,align: 'center',hidden: true}
			,{header: '문서제목' ,name: 'approval_title' ,align: 'center'}
			,{header: '양식' ,name: 'form_type' ,align: 'center'}
			,{header: '사원번호' ,name: 'emp_id' ,align: 'center'}
			,{header: '기안자' ,name: 'emp_name' ,align: 'center'}
			,{header: '부서코드' ,name: 'dept_id' ,align: 'center',hidden: true}
			,{header: '부서명' ,name: 'dept_name' ,align: 'center'}
			,{header: '결재권한자id' ,name: 'approver' ,align: 'center'}
			,{header: '결재권한자 이름' ,name: 'approver_name' ,align: 'center'}
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center'}
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center',hidden: true}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center',hidden: true}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center',hidden: true}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center',hidden: true}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center',hidden: true}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center',hidden: true}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center', width: 100
				,formatter: (rowInfo) => {
 					return `<button type='button' class='btn btn-primary me-2' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
		  ,bodyHeight: 500 // 그리드 본문의 높이를 픽셀 단위로 지정. 스크롤이 생김.
		  ,columnOptions: {
        		resizable: true
      		}
		});
	
	// 	Grid.applyTheme('clean'); // Call API of static method
	const grid2 = new Grid({
	    el: document.getElementById('allApprovalGrid'), // 전체결재
	    columns: [
	
		   {header: '결재순번' ,name: 'row_no' ,align: 'center'}
			,{header: '문서id' ,name: 'approval_id' ,align: 'center',hidden: true}
			,{header: '문서제목' ,name: 'approval_title' ,align: 'center'}
			,{header: '양식' ,name: 'form_type' ,align: 'center'}
			,{header: '사원번호' ,name: 'emp_id' ,align: 'center'}
			,{header: '기안자' ,name: 'emp_name' ,align: 'center'}
			,{header: '부서코드' ,name: 'dept_id' ,align: 'center',hidden: true}
			,{header: '부서명' ,name: 'dept_name' ,align: 'center'}
			,{header: '결재권한자' ,name: 'approver' ,align: 'center'}
			,{header: '결재권한자 이름' ,name: 'approver_name' ,align: 'center'}
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center'}
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center',hidden: true}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center',hidden: true}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center',hidden: true}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center',hidden: true}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center',hidden: true}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center',hidden: true}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center'
				,formatter: (rowInfo) => {
 					return `<button type='button' class='btn btn-primary me-2' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
		  ,bodyHeight: 500
		  ,columnOptions: {
        	resizable: true
      		}
	});

	const grid3 = new Grid({
	    el: document.getElementById('myApprovalGrid'), // 내 결재목록
	    columns: [
	
		    {header: '결재순번' ,name: 'row_no' ,align: 'center'}
			,{header: '문서id' ,name: 'approval_id' ,align: 'center',hidden: true}
			,{header: '문서제목' ,name: 'approval_title' ,align: 'center'}
			,{header: '양식' ,name: 'form_type' ,align: 'center'}
			,{header: '사원번호' ,name: 'emp_id' ,align: 'center'}
			,{header: '기안자' ,name: 'emp_name' ,align: 'center'}
			,{header: '부서코드' ,name: 'dept_id' ,align: 'center',hidden: true}
			,{header: '부서명' ,name: 'dept_name' ,align: 'center'}
			,{header: '결재권한자' ,name: 'approver' ,align: 'center'}
			,{header: '결재권한자 이름' ,name: 'approver_name' ,align: 'center'}
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center',hidden: true}
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center',hidden: true}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center',hidden: true}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center',hidden: true}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center',hidden: true}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center',hidden: true}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center',hidden: true}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center'
				,formatter: (rowInfo) => {
 					return `<button type='button' class='btn btn-primary me-2' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
		  ,bodyHeight: 500
		  ,columnOptions: {
        	resizable: true
      	  }
	});

	const grid4 = new Grid({
	    el: document.getElementById('waitingApprovalGrid'), //결재대기
	    columns: [
	
		    {header: '결재순번' ,name: 'row_no' ,align: 'center'}
			,{header: '문서id' ,name: 'approval_id' ,align: 'center',hidden: true}
			,{header: '문서제목' ,name: 'approval_title' ,align: 'center'}
			,{header: '양식' ,name: 'form_type' ,align: 'center'}
			,{header: '사원번호' ,name: 'emp_id' ,align: 'center'}
			,{header: '기안자' ,name: 'emp_name' ,align: 'center'}
			,{header: '부서코드' ,name: 'dept_id' ,align: 'center',hidden: true}
			,{header: '부서명' ,name: 'dept_name' ,align: 'center'}
			,{header: '결재권한자' ,name: 'approver' ,align: 'center'}
			,{header: '결재권한자 이름' ,name: 'approver_name' ,align: 'center'}
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center',hidden: true}
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center',hidden: true}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center',hidden: true}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center',hidden: true}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center',hidden: true}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center',hidden: true}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center',hidden: true}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center'
				,formatter: (rowInfo) => {
 					return `<button type='button' class='btn btn-primary me-2' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
		  ,bodyHeight: 500
		  ,draggable: true
	});

	const grid5 = new Grid({
	    el: document.getElementById('doneApprovalGrid'), //결재완료
	    columns: [
	
		    {header: '결재순번' ,name: 'row_no' ,align: 'center'}
			,{header: '문서id' ,name: 'approval_id' ,align: 'center',hidden: true}
			,{header: '문서제목' ,name: 'approval_title' ,align: 'center'}
			,{header: '양식' ,name: 'form_type' ,align: 'center'}
			,{header: '사원번호' ,name: 'emp_id' ,align: 'center'}
			,{header: '기안자' ,name: 'emp_name' ,align: 'center'}
			,{header: '부서코드' ,name: 'dept_id' ,align: 'center',hidden: true}
			,{header: '부서명' ,name: 'dept_name' ,align: 'center'}
			,{header: '결재권한자' ,name: 'approver' ,align: 'center'}
			,{header: '결재권한자 이름' ,name: 'approver_name' ,align: 'center'}
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center'}
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center',hidden: true}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center',hidden: true}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center',hidden: true}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center',hidden: true}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center',hidden: true}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center',hidden: true}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center'
				,formatter: (rowInfo) => {
 					return `<button type='button'  class='btn btn-primary me-2' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
		  ,bodyHeight: 500
		  ,columnOptions: {
        	resizable: true
      	  }
	});
	
	
	//조회버튼
	const searchBtn = document.getElementById("searchBtn");
    if (searchBtn) {
        searchBtn.addEventListener("click", (ev) => {
			//alert(" 날짜,기안자,문서이름조회 구현중")
			console.log(ev);
			const params = new URLSearchParams({
	    
			 	start_date: document.getElementById("searchStartDate").value ?? "",
	    	 	end_date: document.getElementById("searchEndDate").value ?? "",
	    	 	emp_name: document.getElementById("searchEmpIdAndformType").value ?? "",
				approval_title: document.getElementById("searchEmpIdAndformType").value ?? ""
			});

			fetch('/approval/search?' + params.toString())
        	.then(res => res.json())
        	.then(data => {
            	grid2.resetData(data);
        	})
        	.catch(err => {
            	console.error("조회오류", err);
        	});
			
		});

    }
	
	// 서버에서 받아온 default 결재권자 담을 변수
	let formList = [];
	// 선택한 양식을 담을 변수
	let selectedForm = null;

	// default 결재권자 가져오는 함수
	async function defalutapprover() {
		const res = await fetch("/api/approvals/defaultApprover", {method: "GET"});
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패!");
		}
		
		formList = await res.json();
	}

	//모달창 코드
	//기안서 셀렉트 박스 변경시 모달창에 텍스트 변경함수
	function draftValFn(ev){
		let draft_doc = ev.value;

		document.getElementById('saveBtn').style.display = "block";
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
		if(draft_doc == "지출결의서"){
			 $('#approval-modal').modal('show');
			document.getElementById('expndTypeForm').style.display = 'flex';
			document.getElementById('leavePeriodForm').style.display = 'none';
			document.getElementById('leaveTypeForm').style.display = 'none';
			document.getElementById('toDeptForm').style.display = 'none';
			
		}else if(draft_doc == "연차신청서"){
			 $('#approval-modal').modal('show');
			document.getElementById('leavePeriodForm').style.display = 'flex';
			document.getElementById('leaveTypeForm').style.display = 'flex';
			document.getElementById('expndTypeForm').style.display = 'none';
			document.getElementById('toDeptForm').style.display = 'none';
		}else if(draft_doc == "반차신청서"){
			 $('#approval-modal').modal('show');
			document.getElementById('leaveTypeForm').style.display = 'flex';
			document.getElementById('leavePeriodForm').style.display = 'none';
			document.getElementById('expndTypeForm').style.display = 'none';
			document.getElementById('toDeptForm').style.display = 'none';
		}else if(draft_doc == "인사발령신청서"){
			 $('#approval-modal').modal('show');
			document.getElementById('leavePeriodForm').style.display = 'none';
			document.getElementById('leaveTypeForm').style.display = 'none';
			document.getElementById('expndTypeForm').style.display = 'none';
			document.getElementById('toDeptForm').style.display = 'flex';
		}else if(draft_doc == "자유양식결재서"){//결재권한자만없음
			 $('#approval-modal').modal('show');
			document.getElementById('leavePeriodForm').style.display = 'none';
			document.getElementById('leaveTypeForm').style.display = 'none';
			document.getElementById('expndTypeForm').style.display = 'none';
			document.getElementById('toDeptForm').style.display = 'none';
		}
		if(draft_doc == "기안서"){//결재권한자만없음
			alert("양식을 선택해주세요.");
			// $('#approval-modal').on('show.bs.modal', function (e) {
				
			// 		// 모달을 열지 않도록 강제로 닫기
			// 		e.preventDefault();
			// 		console.log('모달을 열 수 없습니다.');
				
			// });
		}
		formReset();
		defaultPrint();
	}
	//양식 모달 리셋함수
	function formReset(ev){
		//console.log("formReset ev:",ev);
		//console.log("formReset selectedForm:",selectedForm);
		//document.getElementById('Drafting').innerText = ev.value;
		//document.getElementById("DraftingHidden").value = ev.value;//양식종류 숨은값
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
	

	// 작성 버튼 클릭 시 실행되는 함수
  	function defaultPrint(){
		// 모달을 닫고 다시 작성 버튼을 클릭하면 이전 데이터가 남아있어서 초기화 진행
		approverDiv.innerHTML = "";
		formReset();
		formEnable();
		selectBox.enable();
		window.count = 0;
		approverArr = [];
		
		// selectedForm 값이 없을 경우 에러가 생길 수 있어서 에러 처리
		if (!selectedForm) {
		    console.log("선택된 양식이 없습니다.")
			
				document.getElementById('leavePeriodForm').style.display = 'flex';
				document.getElementById('leaveTypeForm').style.display = 'flex';
				document.getElementById('expndTypeForm').style.display = 'flex';
				document.getElementById('toDeptForm').style.display = 'flex';
			
		    return;
		}
		
		defalutapproverArr = []; //기존 배열 초기화
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

	function print(type, text) {
    	// 결재권한자변경 div 버튼 생성 
		console.log("this-------->",this);
    	if(this.count < 3){
    		this.count++;
    		approverDiv.innerHTML +='<div class="btn btn-success"'
    		                      +'style="width:200px;height:200px; margin:5px; padding: 5px 0px 0px 0px;">'
    		                      +'<p onclick="approverDivclose(this,' + "'"+ type + "'"+ ','+ count +')" style="float:right;margin-right: 8px;">&times;</p>'
    		                      +'<p id="approver_'+count+'" onclick="approvalNo('+ (this.count)+','+ "'"+ text + "'" +')" style="margin-top:50px;height: 129px;">'+(this.count) + '차 결재권한자 : (직급)' + text + ' 변경</p>'
    		                    	+'</div>';
		}
    }

	
	//결재권한자 버튼 클릭시 결재권한자변경 div 태그 생성//전결자
	function approvalNo(count, text) {
		elemApproverIdNum = count;
		console.log("count ------------------------------------------>",count, ", approvalIdNum : ", elemApproverIdNum);
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
	                    onclick="applyDelegateChange(this)">
	                전결자로 지정
	            </button>
	        `;
	        jeongyeoljaDiv.style.display = 'block';
	    }
	}
	//결재권한자,결재권한자변경(전결자) 닫기버튼
	function approverDivclose(buttonDiv,type,count){
		const divElement = buttonDiv.parentNode; // 버튼의 부모인 div를 찾음
		console.log("type",type);
		
		jeongyeoljaDiv.style.display = 'none';
		//defalut 태그 닫기 버튼시 
		if (buttonDiv.parentElement.id === "" || type === "defalut") {//결재권한자
		    divElement.remove(); //자신의 div 제거
		
		    if(divElement.innerText !== null){ //defalut 태그가 있을때
				// console.log("defalutapproverArr",defalutapproverArr);
				approverArr = approverArr.filter((ev) => ev.approverOrder !== count);
				// console.log("approverArr defalut 닫기후:", approverArr);	
		    }
			approverArr = approverArr.filter((ev) => ev !== count);
			this.count = count-1; //제거 라벨 카운트 원상복기
		}
		if(type === "close"){ //전결자 변경 닫기버튼시
			divElement.remove(); //자신의 div 제거
			//전결자 변경시 결재권한자 배열에서 해당 결재권한자 제거
			console.log("count:",count-1);
			approverArr = approverArr.filter((ev) => ev.approverOrder !== count);
			console.log("approverArr 닫기후:", approverArr);
		}
		if(approverArr.length === 0){
	    	this.count = 0;
	    }	
	}

	//에디터-없앰
	const editor = new toastui.Editor({
		el: document.querySelector('#editor'),
	  	height: '500px',
	  	initialEditType: 'markdown',
	  	previewStyle: 'vertical'
	});
	
	editor.getMarkdown();
	
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
