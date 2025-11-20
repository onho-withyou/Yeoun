//결재.js
	// 현재 로그인한 사용자 EMP_ID
	const LOGIN_USER_ID = document.getElementById('currentUserId').value;
	const LOGIN_USER_NAME = document.getElementById('currentUserName').value;
	// 현재 열린 문서의 approvalId
	let approvalId;
	// 현재 열린 문서의 결재권자(approval) 
	let currentApprover;
	// 모달의 결재확인 버튼
	const approvalCheckBtn = document.getElementById('approvalCheckBtn');
	
	const csrfToken = document.querySelector('meta[name="_csrf_token"]')?.content;
	const csrfHeaderName = document.querySelector('meta[name="_csrf_headerName"]')?.content;

	
	// 결재확인 버튼 눌렀을때 동작할 함수
	approvalCheckBtn.addEventListener('click', () => {
		
		console.log(currentApprover, LOGIN_USER_ID);
		// 현재 로그인한 사용자와 결재권자 비교
		if(currentApprover != LOGIN_USER_ID) {
			alert("결재권한이 없습니다."); 
			return;
		}
		
		// 결재권한자와 사용자가 동일인물일 때
		if(confirm("승인하시겠습니까?")) {
			//결재 확인 동작함수
			fetch('/api/approvals/' + approvalId, {
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
		
	})
	
	
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
	
	let approvarDiv = document.querySelector('#approvar');

	// let width = 150;
    // let height = 70;
    // let grid = createGrid(width, height);
	
	//selectbox - 인사정보 불러오기
	async function empData() {
		try {
			const response = await fetch("/approval/empList");
			const data = await response.json();
			let itemData  = [];
			let obj ={};
			data.map((item,index)=>{
				obj["value"] = item[0]; //사번
				obj["label"] = (index+1) +" : "+item[1]+"("+item[0]+")"; //이름(사번)
				itemData.push(obj);
				obj = {};
			});
			
			//셀렉트박스 - 토스트유아이
			let selectBox = new tui.SelectBox('#select-box', {
			  data: itemData
			});
			//셀렉트박스 닫힐때
			selectBox.on('close',(ev)=>{
				
				let selectlabel = selectBox.getSelectedItem().label;
				if(selectlabel != null && approvarArr.length < 3){//셀렉트 라벨선택시 3번까지만셈
					print(ev.type, selectlabel);
					approvarArr.push(this.count);
				}
				
			});
			//const modal = document.getElementById('approval-modal');
			//그리드 1클릭시 상세버튼
			grid1.on("click", (ev) => {
		
				console.log("ev ------>",ev);
				const target = ev.nativeEvent.target;
				console.log("target ---->",target);
				
				const rowData = grid1.getRow(ev.rowKey);
				console.log("rowData ----->",rowData);//로우데이터는 이걸로 불러오면됨
				$('#approval-modal').modal('show');
				
				// 문서 열릴때 approvalId에 현재 열린 문서id 저장
				approvalId = rowData.approval_id;
				// 문서 열릴때 현재 결재권자(approval) 저장
				currentApprover = rowData.approver;
				
				document.getElementById('Drafting').innerText = rowData.approval_title;
				document.getElementById('today-date').innerText = rowData.created_date.split('T')[0] ;//결재 작성날짜 = 결재시작일
				document.getElementById('approval-title').value = rowData.approval_title;
				//양식종류 form-menu
				document.getElementById('approver-name').value  = rowData.emp_id;//결재자명
				console.log("rowData.created_date",rowData.created_date.split('T')[0] );
				const createdDate = rowData.created_date;
				document.getElementById('create-date').value = rowData.created_date.split('T')[0];//결재시작일 =결재 작성날짜 
				document.getElementById('finish-date').value = rowData.finish_date.split('T')[0];//결재완료날짜
				//휴가 연차신청서 
				document.getElementById('start-date').value = rowData.start_date.split('T')[0]; //휴가시작날짜
				document.getElementById('end-date').value = rowData.end_date.split('T')[0]; //휴가종료날짜
				//document.getElementById('leave-radio').value = rowData.leave_type;// 연차유형 라디오- 없앳음 -휴가종류로 들어감
				document.getElementById('leave-type').value = rowData.leave_type;//휴가종류
			
				console.log("rowData.to_dept_id",rowData.to_dept_id);
				document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서,디비잘못넣음
				document.getElementById('expnd-type').value = rowData.expnd_type;//지출종류EXPND_TYPE
				//document.getElementById('approvar').value = rowData.approver;//결재권한자
				
				if(rowData.approver != null){
					selectBox.select(rowData.approver);
					print("defalut", selectBox.getSelectedItem().label);
				}
				//document.getElementById('approvar').innerText = rowData.approver;//전결자
				document.getElementById('reason-write').value = rowData.reason;//결재사유내용
				
			});

			grid2.on("click", (ev) => {
		
				console.log("ev ------>",ev);
				const target = ev.nativeEvent.target;
				console.log("target ---->", );
				
				const rowData = grid2.getRow(ev.rowKey);
				console.log("rowData ----->",rowData);//로우데이터는 이걸로 불러오면됨
				$('#approval-modal').modal('show');
				
				// 문서 열릴때 approvalId에 현재 열린 문서id 저장
				approvalId = rowData.approval_id;
				// 문서 열릴때 현재 결재권자(approval) 저장
				currentApprover = rowData.approver;
				
				document.getElementById('Drafting').innerText = rowData.approval_title;
				document.getElementById('today-date').innerText = rowData.created_date.split('T')[0] ;//결재 작성날짜 = 결재시작일
				//document.getElementById('approval-title').value = rowData.approval_title;
				//양식종류form-menu
				document.getElementById('approver-name').value  = rowData.emp_id;//결재자명
				console.log("rowData.created_date",rowData.created_date.split('T')[0] );
				const createdDate = rowData.created_date;
				document.getElementById('create-date').value = rowData.created_date.split('T')[0];//결재시작일 =결재 작성날짜 
				document.getElementById('finish-date').value = rowData.finish_date.split('T')[0];//결재완료날짜
				//휴가 연차신청서 
				document.getElementById('start-date').value = rowData.start_date.split('T')[0]; //휴가시작날짜
				document.getElementById('end-date').value = rowData.end_date.split('T')[0]; //휴가종료날짜
				//document.getElementById('leave-radio').value = rowData.leave_type;// 연차유형 라디오- 없앳음 -휴가종류로 들어감
				document.getElementById('leave-type').value = rowData.leave_type;//휴가종류
			
				document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서,디비잘못넣음
				document.getElementById('expnd-type').value = rowData.expnd_type;//지출종류EXPND_TYPE
				//document.getElementById('approvar').value = rowData.approver;//결재권한자
				
				if(rowData.approver != null){
					selectBox.select(rowData.approver);
					console.log(selectBox.getSelectedItem().label);
					print("defalut", selectBox.getSelectedItem().label);
				}
				//document.getElementById('approvar').innerText = rowData.approver;//전결자
				document.getElementById('reason-write').value = rowData.reason;//결재사유내용
			});


			grid3.on("click", (ev) => {
		
				console.log("ev ------>",ev);
				const target = ev.nativeEvent.target;
				console.log("target ---->",target);
				
				const rowData = grid3.getRow(ev.rowKey);
				console.log("rowData ----->",rowData);//로우데이터는 이걸로 불러오면됨
				$('#approval-modal').modal('show');
				
				// 문서 열릴때 approvalId에 현재 열린 문서id 저장
				approvalId = rowData.approval_id;
				// 문서 열릴때 현재 결재권자(approval) 저장
				currentApprover = rowData.approver;
				
				document.getElementById('Drafting').innerText = rowData.approval_title;
				document.getElementById('today-date').innerText = rowData.created_date.split('T')[0] ;//결재 작성날짜 = 결재시작일
				//document.getElementById('approval-title').value = rowData.approval_title;
				//양식종류form-menu
				document.getElementById('approver-name').value  = rowData.emp_id;//결재자명
				console.log("rowData.created_date",rowData.created_date.split('T')[0] );
				const createdDate = rowData.created_date;
				document.getElementById('create-date').value = rowData.created_date.split('T')[0];//결재시작일 =결재 작성날짜 
				document.getElementById('finish-date').value = rowData.finish_date.split('T')[0];//결재완료날짜
				//휴가 연차신청서 
				document.getElementById('start-date').value = rowData.start_date.split('T')[0]; //휴가시작날짜
				document.getElementById('end-date').value = rowData.end_date.split('T')[0]; //휴가종료날짜
				//document.getElementById('leave-radio').value = rowData.leave_type;// 연차유형 라디오- 없앳음 -휴가종류로 들어감
				document.getElementById('leave-type').value = rowData.leave_type;//휴가종류
			
				document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서,디비잘못넣음
				document.getElementById('expnd-type').value = rowData.expnd_type;//지출종류EXPND_TYPE
				//document.getElementById('approvar').value = rowData.approver;//결재권한자
				if(rowData.approver != null){
					selectBox.select(rowData.approver);
					print("defalut", selectBox.getSelectedItem().label);
				}
				//document.getElementById('approvar').innerText = rowData.approver;//전결자
				document.getElementById('reason-write').value = rowData.reason;//결재사유내용
			});

		grid4.on("click", (ev) => {
			
			console.log("ev ------>",ev);
			const target = ev.nativeEvent.target;
			console.log("target ---->",target);
			
			const rowData = grid4.getRow(ev.rowKey);
			console.log("rowData ----->",rowData);//로우데이터는 이걸로 불러오면됨
			$('#approval-modal').modal('show');
			
			// 문서 열릴때 approvalId에 현재 열린 문서id 저장
			approvalId = rowData.approval_id;
			// 문서 열릴때 현재 결재권자(approval) 저장
			currentApprover = rowData.approver;
			
			document.getElementById('Drafting').innerText = rowData.approval_title;
			document.getElementById('today-date').innerText = rowData.created_date.split('T')[0] ;//결재 작성날짜 = 결재시작일
			//document.getElementById('approval-title').value = rowData.approval_title;
			//양식종류form-menu
			document.getElementById('approver-name').value  = rowData.emp_id;//결재자명
			console.log("rowData.created_date",rowData.created_date.split('T')[0] );
			const createdDate = rowData.created_date;
			document.getElementById('create-date').value = rowData.created_date.split('T')[0];//결재시작일 =결재 작성날짜 
			document.getElementById('finish-date').value = rowData.finish_date.split('T')[0];//결재완료날짜
			//휴가 연차신청서 
			document.getElementById('start-date').value = rowData.start_date.split('T')[0]; //휴가시작날짜
			document.getElementById('end-date').value = rowData.end_date.split('T')[0]; //휴가종료날짜
			//document.getElementById('leave-radio').value = rowData.leave_type;// 연차유형 라디오- 없앳음 -휴가종류로 들어감
			document.getElementById('leave-type').value = rowData.leave_type;//휴가종류
		
			document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서,디비잘못넣음
			document.getElementById('expnd-type').value = rowData.expnd_type;//지출종류EXPND_TYPE
			//document.getElementById('approvar').value = rowData.approver;//결재권한자
			if(rowData.approver != null){
				selectBox.select(rowData.approver);

				print("defalut", selectBox.getSelectedItem().label);

			}
			//document.getElementById('approvar').innerText = rowData.approver;//전결자
			document.getElementById('reason-write').value = rowData.reason;//결재사유내용
		});
	
		grid5.on("click", (ev) => {
			
			console.log("ev ------>",ev);
			const target = ev.nativeEvent.target;
			console.log("target ---->",target);
			
			const rowData = grid5.getRow(ev.rowKey);
			console.log("rowData ----->",rowData);//로우데이터는 이걸로 불러오면됨
			$('#approval-modal').modal('show');
			
			// 문서 열릴때 approvalId에 현재 열린 문서id 저장
			approvalId = rowData.approval_id;
			// 문서 열릴때 현재 결재권자(approval) 저장
			currentApprover = rowData.approver;
			
			document.getElementById('Drafting').innerText = rowData.approval_title;
			document.getElementById('today-date').innerText = rowData.created_date.split('T')[0] ;//결재 작성날짜 = 결재시작일
			//document.getElementById('approval-title').value = rowData.approval_title;
			//양식종류form-menu
			document.getElementById('approver-name').value  = rowData.emp_id;//결재자명
			console.log("rowData.created_date",rowData.created_date.split('T')[0] );
			const createdDate = rowData.created_date;
			document.getElementById('create-date').value = rowData.created_date.split('T')[0];//결재시작일 =결재 작성날짜 
			document.getElementById('finish-date').value = rowData.finish_date.split('T')[0];//결재완료날짜
			//휴가 연차신청서 
			document.getElementById('start-date').value = rowData.start_date.split('T')[0]; //휴가시작날짜
			document.getElementById('end-date').value = rowData.end_date.split('T')[0]; //휴가종료날짜
			//document.getElementById('leave-radio').value = rowData.leave_type;// 연차유형 라디오- 없앳음 -휴가종류로 들어감
			document.getElementById('leave-type').value = rowData.leave_type;//휴가종류
		
			document.getElementById('to-dept-id').value = rowData.to_dept_id;//발령부서,디비잘못넣음
			document.getElementById('expnd-type').value = rowData.expnd_type;//지출종류EXPND_TYPE
			//document.getElementById('approvar').value = rowData.approver;//결재권한자
			if(rowData.approver != null){
				selectBox.select(rowData.approver);
				print("defalut", selectBox.getSelectedItem().label);
			}
			//document.getElementById('approvar').innerText = rowData.approver;//전결자
			document.getElementById('reason-write').value = rowData.reason;//결재사유내용
		});
	


			return itemData;
		} catch (error) {
			console.error('Error fetching data:', error);
		}
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
				obj["emp_id"] = item[3]; //사원번호
				obj["emp_name"] = item[4]; //기안자
				obj["dept_id"] = item[5]; //부서코드
				obj["dept_name"] = item[6]; //부서명
				obj["approver"] = item[7]; //결재권한자id
				obj["approver_name"] = item[8]; //결재권한자 이름
				obj["pos_code"] = item[9]; //직급코드
				obj["pos_name"] = item[10]; //직급
				obj["created_date"] = item[11].split('T')[0]; //생성일
				obj["finish_date"] = item[12].split('T')[0]; //결재완료일자
				obj["start_date"] = item[13].split('T')[0]; //
				obj["end_date"] = item[14].split('T')[0]; //	
				obj["leave_type"] = item[15]; //	
				obj["to_dept_id"] = item[16]; //	
				obj["expnd_type"] = item[17]; //
				obj["reason"] = item[18]; //	
				obj["doc_status"] = item[19]; //상태
				obj["view_details"] = item[20]; //상세보기undefined
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
				obj["emp_id"] = item[3]; //사원번호
				obj["emp_name"] = item[4]; //기안자
				obj["dept_id"] = item[5]; //부서코드
				obj["dept_name"] = item[6]; //부서명
				obj["approver"] = item[7]; //결재권한자
				obj["approver_name"] = item[8]; //결재권한자 이름
				obj["pos_code"] = item[9]; //직급코드
				obj["pos_name"] = item[10]; //직급
				obj["created_date"] = item[11].split('T')[0]; //생성일
				obj["finish_date"] = item[12].split('T')[0]; //결재완료일자
				obj["start_date"] = item[13].split('T')[0]; //
				obj["end_date"] = item[14].split('T')[0]; //	
				obj["leave_type"] = item[15]; //	
				obj["to_dept_id"] = item[16]; //	
				obj["expnd_type"] = item[17]; //
				obj["reason"] = item[18]; //	
				obj["doc_status"] = item[19]; //상태
				obj["view_details"] = item[20]; //상세보기undefined
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
				obj["emp_id"] = item[3]; //사원번호
				obj["emp_name"] = item[4]; //기안자
				obj["dept_id"] = item[5]; //부서코드
				obj["dept_name"] = item[6]; //부서명
				obj["approver"] = item[7]; //결재권한자
				obj["approver_name"] = item[8]; //결재권한자 이름
				obj["pos_code"] = item[9]; //직급코드
				obj["pos_name"] = item[10]; //직급
				obj["created_date"] = item[11].split('T')[0]; //생성일
				obj["finish_date"] = item[12].split('T')[0]; //결재완료일자
				obj["start_date"] = item[13].split('T')[0]; //
				obj["end_date"] = item[14].split('T')[0]; //	
				obj["leave_type"] = item[15]; //	
				obj["to_dept_id"] = item[16]; //	
				obj["expnd_type"] = item[17]; //
				obj["reason"] = item[18]; //	
				obj["doc_status"] = item[19]; //상태
				obj["view_details"] = item[20]; //상세보기undefined
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
				obj["emp_id"] = item[3]; //사원번호
				obj["emp_name"] = item[4]; //기안자
				obj["dept_id"] = item[5]; //부서코드
				obj["dept_name"] = item[6]; //부서명
				obj["approver"] = item[7]; //결재권한자
				obj["approver_name"] = item[8]; //결재권한자 이름
				obj["pos_code"] = item[9]; //직급코드
				obj["pos_name"] = item[10]; //직급
				obj["created_date"] = item[11].split('T')[0]; //생성일
				obj["finish_date"] = item[12].split('T')[0]; //결재완료일자
				obj["start_date"] = item[13].split('T')[0]; //
				obj["end_date"] = item[14].split('T')[0]; //	
				obj["leave_type"] = item[15]; //	
				obj["to_dept_id"] = item[16]; //	
				obj["expnd_type"] = item[17]; //
				obj["reason"] = item[18]; //	
				obj["doc_status"] = item[19]; //상태
				obj["view_details"] = item[20]; //상세보기undefined
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
				obj["emp_id"] = item[3]; //사원번호
				obj["emp_name"] = item[4]; //기안자
				obj["dept_id"] = item[5]; //부서코드
				obj["dept_name"] = item[6]; //부서명
				obj["approver"] = item[7]; //결재권한자
				obj["approver_name"] = item[8]; //결재권한자 이름
				obj["pos_code"] = item[9]; //직급코드
				obj["pos_name"] = item[10]; //직급
				obj["created_date"] = item[11].split('T')[0]; //생성일
				obj["finish_date"] = item[12].split('T')[0]; //결재완료일자
				obj["start_date"] = item[13].split('T')[0]; //
				obj["end_date"] = item[14].split('T')[0]; //	
				obj["leave_type"] = item[15]; //	
				obj["to_dept_id"] = item[16]; //	
				obj["expnd_type"] = item[17]; //
				obj["reason"] = item[18]; //	
				obj["doc_status"] = item[19]; //상태
				obj["view_details"] = item[20]; //상세보기undefined
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
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center'}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center'}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center'}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center'}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center'}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center'}
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
			,{header: '사원번호' ,name: 'emp_id' ,align: 'center'}
			,{header: '기안자' ,name: 'emp_name' ,align: 'center'}
			,{header: '부서코드' ,name: 'dept_id' ,align: 'center',hidden: true}
			,{header: '부서명' ,name: 'dept_name' ,align: 'center'}
			,{header: '결재권한자' ,name: 'approver' ,align: 'center'}
			,{header: '결재권한자 이름' ,name: 'approver_name' ,align: 'center'}
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center',width:300}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center'}
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center'}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center'}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center'}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center'}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center'}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center'}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center'
				,formatter: (rowInfo) => {
					console.log("rowInfo.row.rowKey------->",rowInfo.row.rowKey);
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
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center'}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center'}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center'}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center'}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center'}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center'}
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
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center'}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center'}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center'}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center'}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center'}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center'}
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
			,{header: '휴가시작일자' ,name: 'start_date' ,align: 'center'}
			,{header: '휴가종료일자' ,name: 'end_date' ,align: 'center'}
			,{header: '연차유형' ,name: 'leave_type' ,align: 'center'}
			,{header: '발령부서' ,name: 'to_dept_id' ,align: 'center'}
			,{header: '지출종류' ,name: 'expnd_type' ,align: 'center'}
			,{header: '결재사유내용' ,name: 'reason' ,align: 'center'}
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
	//모달창 코드
	//기안서 셀렉트 박스 변경시 모달창에 텍스트 변경함수
	function draftValFn(ev){
		
		let draft_doc = ev.value;
		document.getElementById('Drafting').innerText = draft_doc;
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
	let approvarDivClose = document.getElementById("approvarDiv-close");
	
    this.count = 0; //결재권한자 label count
    let defalutApprovarArr = ["d-이사랑","d-미미미누","d-김경란"];
    let approvarArr = [];//결재권한자 배열 
	
    function defaultPrint(){
    	if(this.count === 0){
	    	for(var i=0;i<defalutApprovarArr.length;i++){
	    		
		    	print("defalut",defalutApprovarArr[i]);
    			approvarArr.push(defalutApprovarArr[i]);
	    	}
    	}
    }
    function print(type, text) {
		// 결재권한자변경 div 버튼 생성
    	if(this.count < 3){
		this.count++;
    	approvarDiv.innerHTML +='<div class="btn btn-success"'
      						+'style="width:200px;height:200px; margin:5px; padding: 5px 0px 0px 0px;">'
      						+'<p onclick="approvarDivclose(this,' + "'"+ type + "'"+ ','+ count +')" style="float:right;margin-right: 8px;">&times;</p>'
      						+'<p onclick="approvalNo('+ (this.count)+','+ "'"+ text + "'" +')" style="margin-top:50px;height: 129px;">'+(this.count) + '차 결재권한자 : (직급)' + text + ' 변경</p>'
    						+'</div>';
    	}
    } 
	
	//결재권한자 버튼 클릭시 결재권한자변경 div 태그 생성
	function approvalNo(count,text){
	   //해당태그 클릭시 전결자 div 나오기
		let type = "change";
		jeongyeoljaDiv.innerHTML = '<button onClick="approvarDivclose(this,'+ "'"+ type + "'"+','+ count +')" class="btn-close" style="float:right;margin-right: 8px;"></button>'
	   		 			+ '<h5>'+ count + "차 결재권한자 : "+ text +" 변경"+'</h5>';
		jeongyeoljaDiv.innerHTML += jeongyeoljaContent.innerHTML;	
	   
	   	if(jeongyeoljaDiv.style.display === 'none'){
	  	jeongyeoljaDiv.style.display = 'block';  
	   	  						  
		}	
    } 
	
	//결재권한자,결재권한자변경(전결자) 닫기버튼
	function approvarDivclose(buttonDiv,type,count){
		const divElement = buttonDiv.parentNode; // 버튼의 부모인 div를 찾음
		
		// console.log("type",type);
		// console.log("jeongyeoljaDiv.style.display",jeongyeoljaDiv.style.display);
		jeongyeoljaDiv.style.display = 'none';
		//defalut 태그 닫기 버튼시 
		if (buttonDiv.parentElement.id === '' || type === 'defalut') {//결재권한자
	        divElement.remove(); //자신의 div 제거
	        
	        if(divElement.innerText !== null){ //defalut 태그가 있을때
	        	approvarArr = approvarArr.filter((ev) => ev !== defalutApprovarArr[count-1]);	
	        }
			approvarArr = approvarArr.filter((ev) => ev !== count);
			this.count = count-1; //제거 라벨 카운트 원상복기
	    }
		
		
		if(approvarArr.length === 0){
    		this.count = 0;
    	}	
	}

	//에디터
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
	 
