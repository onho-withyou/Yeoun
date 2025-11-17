//결재.js

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
		
	}
	//1. 결재사항 불러오기
	function fetchPendingApprovalDocs() {
		return fetch('/approval/pendingApprovalDocGrid')
			.then(response => response.json())
			.then(data => {
				
				let colData = [];
				let obj = {};
				// console.log("grid1 fetch-data----->:",data);
				data.map((item,index) => {
					obj["row_no"] = item[0]; //결재순번
					obj["approval_id"]	 = item[1];	//문서id
					obj["approval_title"] = item[2]; //문서제목	
					obj["emp_id"] = item[3];	//사원번호
					obj["emp_name"] = item[4];	//기안자
					obj["dept_id"] = item[5];	//부서코드
					obj["dept_name"] = item[6];	//부서명
					obj["approver"] = item[7];	//결재권한자id
					obj["approver_name"] = item[8];	//결재권한자 이름
					obj["pos_code"] = item[9];	//직급코드
					obj["pos_name"] = item[10];	//직급
					obj["created_date"] = item[11];	//생성일
					obj["finish_date"] = item[12]; //결재완료일자
					obj["doc_status"] = item[13]; //상태
					obj["view_details"] = item[14];//상세보기undefined
					colData.push(obj);
					obj = {};
				});

				//console.log("objectArray outside map:",colData);
				
				return colData;
			})
			.catch(error => {
				console.error('Error fetching approval documents:', error);
			});
	}
	//2. 전체결재 목록 불러오기
	function fetchApprovalDocs() {
		return fetch('/approval/approvalDocGrid')
			.then(response => response.json())
			.then(data => {
				
				let colData = [];
				let obj = {};

				data.map((item,index) => {
					obj["row_no"] = item[0]; //결재순번
					obj["approval_id"]	 = item[1];	//문서id
					obj["approval_title"] = item[2]; //문서제목	
					obj["emp_id"] = item[3];	//사원번호
					obj["emp_name"] = item[4];	//기안자
					obj["dept_id"] = item[5];	//부서코드
					obj["dept_name"] = item[6];	//부서명
					obj["approver"] = item[7];	//결재권한자
					obj["pos_code"] = item[8];	//직급코드
					obj["pos_name"] = item[9];	//직급
					obj["created_date"] = item[10];	//생성일
					obj["finish_date"] = item[11]; //결재완료일자
					obj["doc_status"] = item[12]; //상태
					obj["view_details"] = item[13]; //상세보기undefined
					colData.push(obj);
					obj = {};
				});

				//console.log("objectArray outside map:",colData);
				
				return colData;
			})
			.catch(error => {
				console.error('Error fetching approval documents:', error);
			});
	}
	//3.내 결재목록 불러오기
	function fetchMyApprovalDocs() {
		return fetch('/approval/myApprovalDocGrid')
			.then(response => response.json())
			.then(data => {
				// console.log("grid3 fetch-data----->:",data);
				let colData = [];
				let obj = {};

				data.map((item,index) => {
					obj["row_no"] = item[0]; //결재순번
					obj["approval_id"]	 = item[1];	//문서id
					obj["approval_title"] = item[2]; //문서제목	
					obj["emp_id"] = item[3];	//사원번호
					obj["emp_name"] = item[4];	//기안자
					obj["dept_id"] = item[5];	//부서코드
					obj["dept_name"] = item[6];	//부서명
					obj["approver"] = item[7];	//결재권한자
					obj["pos_code"] = item[8];	//직급코드
					obj["pos_name"] = item[9];	//직급
					obj["created_date"] = item[10];	//생성일
					obj["finish_date"] = item[11]; //결재완료일자
					obj["doc_status"] = item[12]; //상태
					obj["viewing"] = item[13]; //열람여부
					obj["view_details"] = item[14]; //상세보기undefined
					colData.push(obj);
					obj = {};
				});

				// console.log("grid3 map------>:",colData);
				
				return colData;
			})
			.catch(error => {
				console.error('Error fetching approval documents:', error);
			});
	}
	//4.결재대기 불러오기 -- 1차반려,2차반려,3차반려,1차완료,2차완료,3차완료, 종료
	function fetchWaitingApprovalDocs() {
		return fetch('/approval/waitingApprovalDocGrid')
			.then(response => response.json())
			.then(data => {
				console.log("grid4 fetch-data----->:",data);
				let colData = [];
				let obj = {};

				data.map((item,index) => {
					obj["row_no"] = item[0]; //결재순번
					obj["approval_id"]	 = item[1];	//문서id
					obj["approval_title"] = item[2]; //문서제목	
					obj["emp_id"] = item[3];	//사원번호
					obj["emp_name"] = item[4];	//기안자
					obj["dept_id"] = item[5];	//부서코드
					obj["dept_name"] = item[6];	//부서명
					obj["approver"] = item[7];	//결재권한자
					obj["pos_code"] = item[8];	//직급코드
					obj["pos_name"] = item[9];	//직급
					obj["created_date"] = item[10];	//생성일
					obj["finish_date"] = item[11]; //결재완료일자
					obj["doc_status"] = item[12]; //상태
					obj["view_details"] = item[13]; //상세보기undefined
					colData.push(obj);
					obj = {};
				});

				console.log("grid4 map------>:",colData);
				
				return colData;
			})
			.catch(error => {
				console.error('Error fetching approval documents:', error);
			});
	}
	//5.결재완료 불러오기
	function fetchDoneApprovalDocs() {
		return fetch('/approval/finishedApprovalDocGrid')
			.then(response => response.json())
			.then(data => {
				console.log("grid5 fetch-data----->:",data);
				let colData = [];
				let obj = {};

				data.map((item,index) => {
					obj["row_no"] = item[0]; //결재순번
					obj["approval_id"]	 = item[1];	//문서id
					obj["approval_title"] = item[2]; //문서제목	
					obj["emp_id"] = item[3];	//사원번호
					obj["emp_name"] = item[4];	//기안자
					obj["dept_id"] = item[5];	//부서코드
					obj["dept_name"] = item[6];	//부서명
					obj["approver"] = item[7];	//결재권한자
					obj["pos_code"] = item[8];	//직급코드
					obj["pos_name"] = item[9];	//직급
					obj["created_date"] = item[10];	//생성일
					obj["finish_date"] = item[11]; //결재완료일자
					obj["doc_status"] = item[12]; //상태
					obj["view_details"] = item[13];//상세보기undefined
					colData.push(obj);
					obj = {};
				});

				console.log("grid5 map------>:",colData);
				
				return colData;
			})
			.catch(error => {
				console.error('Error fetching approval documents:', error);
			});
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
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center', width: 100
				,formatter: (rowInfo) => {
 					return `<button type='button' class='btn btn-primary me-2' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
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
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center'}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center'
				,formatter: (rowInfo) => {
 					return `<button type='button' class='btn btn-primary me-2' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
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
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center'}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center'
				,formatter: (rowInfo) => {
 					return `<button type='button' class='btn btn-primary me-2' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
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
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center'}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center'
				,formatter: (rowInfo) => {
 					return `<button type='button' class='btn btn-primary me-2' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
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
			,{header: '직급코드' ,name: 'pos_code' ,align: 'center',hidden: true}
			,{header: '직급' ,name: 'pos_name' ,align: 'center'}
			,{header: '생성일' ,name: 'created_date' ,align: 'center'}
			,{header: '결재완료일자' ,name: 'finish_date' ,align: 'center'}
			,{header: '상태' ,name: 'doc_status' ,align: 'center'}
			,{header: '상세보기' ,name: 'view_details' ,align: 'center'
				,formatter: (rowInfo) => {
 					return `<button type='button' class='btn btn-primary me-2' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}}
		  ],
		  data: []
	});
	
	//모달창 코드
	//셀렉트 박스 변경시 모달창에 텍스트 변경함수
	function draftValFn(e){
		
		let draft_doc = e.value;
		document.getElementById('Drafting').innerText = draft_doc;
	}
	
	let today = new Date();   

	let year = today.getFullYear(); // 년도
	let month = today.getMonth() + 1;  // 월
	let date = today.getDate();  // 날짜
	let day = today.getDay();  // 요일

	const formattedDate = `${year}년 ${month}월 ${date}일`;

	document.getElementById("today-date").textContent = formattedDate;

	//셀렉트박스
	var approvarDiv = document.querySelector('#approvar');
	var deptList = "[[${deptList}]]";
	console.log("deptList",deptList);
	var selectBox = new tui.SelectBox('#select-box', {
      data: [
        {
          label: '인사팀',
          data: [{ label: '한가인', value: '한가인'}, 
        	  	{ label: '정지훈', value: '정지훈' ,selected: true }]
        },
        {
          label: '개발팀',
          data: [
            { label: '이정현', value: '이정현'},
            { label: '마이클', value: '마이클'},
            { label: '기안', value: '기안'},
            { label: '이지영', value: '이지영'},
            { label: '박지운', value: '박지운'}
          ]
        }
      ]
    });

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
	//셀렉트박스 닫힐때
    selectBox.on('close',(ev)=>{
    	
    	let selectlabel = selectBox.getSelectedItem().label;
    	if(selectlabel != null && approvarArr.length < 3){//셀렉트 라벨선택시 3번까지만셈
    		print(ev.type, selectlabel);
    		approvarArr.push(this.count);
    	}
	})
   	//리셋버튼 만들기 하다가잠
    
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
	 
