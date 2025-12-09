window.onload = function () {	
	productRouteSearch();//제품별 공정라우트 그리드 조회
	processCodeGridAllSearch();//공정코드 관리 그리드 조회
	routeStepCodeSearch();//신규라우트 모달 그리드 - 공정단계 조회
}

//탭 전환시 그리드 레이아웃 갱신
document.querySelectorAll('button[data-bs-toggle="tab"]').forEach(tab => {
    tab.addEventListener('shown.bs.tab', function (e) {
        const targetId = e.target.getAttribute('data-bs-target');

        if (targetId === '#navs-process-tab') {//제품별 공정라우트 탭
            grid1.refreshLayout();
        } else if (targetId === '#navs-processCode-tab') {//공정코드 관리 탭
            grid2.refreshLayout();
        }
    });
});

// 라우트단계 공정코드 조회 모달 그리드 레이아웃갱신
const routeModalElement = document.getElementById('route-modal');//신규라우트 모달
routeModalElement.addEventListener('shown.bs.modal', function () {
    grid3.refreshLayout();
});

const processLookupModalElement = document.getElementById('processLookup-modal');//공정코드 조회 모달
processLookupModalElement.addEventListener('shown.bs.modal', function () {
	grid4.refreshLayout();
});

class StatusModifiedRenderer {
    constructor(props) {
        const el = document.createElement('div');
        el.className = 'tui-grid-cell-content-renderer'; 
        this.el = el;
        this.grid = props.grid; 
        this.render(props);
    }

    getElement() {
        return this.el;
    }

    render(props) {
        const value = props.value;
        const rowKey = props.rowKey; 
        
        this.el.textContent = value; 

        // 💡 수정되거나 추가된 행 상태 확인 로직
        let isUpdatedOrCreated = false;
        
        if (this.grid) {
            const modifiedRows = this.grid.getModifiedRows();
            
            // 1. 수정된 행(updatedRows) 목록에서 현재 rowKey 확인
            const isUpdated = modifiedRows.updatedRows.some(row => String(row.rowKey) === String(rowKey));
            
            // 2. 새로 추가된 행(createdRows) 목록에서 현재 rowKey 확인
            const isCreated = modifiedRows.createdRows.some(row => String(row.rowKey) === String(rowKey));
            
            // 두 상태 중 하나라도 true이면 스타일 적용
            isUpdatedOrCreated = isUpdated || isCreated;
        }
        
        // 🎨 인라인 스타일 적용
        if (isUpdatedOrCreated) {
            // 수정되거나 추가된 행에 적용될 스타일
            this.el.style.backgroundColor = '#c3f2ffff'; 
            this.el.style.color = '#000000';         
            this.el.style.fontWeight = 'bold';
        } else {
            // 조건 불충족 시 스타일 초기화
            this.el.style.backgroundColor = '';
            this.el.style.color = '';
            this.el.style.fontWeight = '';
        }
    }
}


const Grid = tui.Grid;
//g-grid1 공정그리드
const grid1 = new Grid({
	  el: document.getElementById('processGrid'), 
      rowHeaders: ['rowNum','checkbox'],
	  columns: [

		{header: '라우트ID' ,name: 'routeId' ,align: 'center'}
		,{header: '제품코드' ,name: 'prdId' ,align: 'center'}
		,{header: '라우트명' ,name: 'routeName' ,align: 'center',width: 150,filter: "select"}
		,{header: '설명' ,name: 'description' ,align: 'center',width: 550}
		,{header: '사용여부' ,name: 'useYn' ,align: 'center',width: 90}  
		,{header: '생성자id' ,name: 'createdId' ,align: 'center',hidden: true}  
		,{header: '생성일시' ,name: 'createdDate' ,align: 'center',hidden: true}  
		,{header: '수정자id' ,name: 'updatedId' ,align: 'center',hidden: true}  
		,{header: '수정일시' ,name: 'updatedDate' ,align: 'center',hidden: true} 
		, {
			header: '상세보기', name: 'view_details', align: 'center', width: 100
			, formatter: (rowInfo) => {
				return `<button type='button' class='btn btn-primary btn-sm' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}
		}       
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
	
//g-grid2 공정코드 관리(PROCESS_MASTER 조회)
const grid2 = new Grid({
	    el: document.getElementById('processCodeGrid'),
        rowHeaders: ['rowNum','checkbox'],
	    columns: [
	    {header: '공정ID' ,name: 'processId' ,align: 'center'}
	    ,{header: '공정명' ,name: 'processName' ,align: 'center',width: 230}
	    ,{header: '공정유형' ,name: 'processType' ,align: 'center',filter: "select"}
	    ,{header: '설명' ,name: 'description' ,align: 'center',filter: "select"}
        ,{header: '사용여부' ,name: 'useYn' ,align: 'center'}
		,{header: '생성자id' ,name: 'createdId' ,align: 'center'}
		,{header: '생성일시' ,name: 'createdDate' ,align: 'center'}
		,{header: '수정자id' ,name: 'updatedId' ,align: 'center'}
		,{header: '수정일시' ,name: 'updatedDate' ,align: 'center'}
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

//g-grid3 신규라우트 모달 그리드 - 공정단계
const grid3 = new Grid({
	    el: document.getElementById('processStepGrid'),
        rowHeaders: ['rowNum','checkbox'],
	    columns: [
	    {header: '라우트단계ID' ,name: 'routeStepId' ,align: 'center', editor: 'text'
			,renderer:{ type: StatusModifiedRenderer}
		}
	    ,{header: '라우트ID' ,name: 'routeId' ,align: 'center', editor: 'text',filter: "select"
			,renderer:{ type: StatusModifiedRenderer}
		}
	    ,{header: '순번' ,name: 'stepSeq' ,align: 'center', editor: 'text',filter: "select"
			,renderer:{ type: StatusModifiedRenderer}
		}
		,{header: '공정ID' ,name: 'processId' ,align: 'center', editor: 'text',filter: "select"
			,renderer:{ type: StatusModifiedRenderer}
		}
        ,{header: 'QC 여부' ,name: 'qcPointYn' ,align: 'center'
			,renderer:{ type: StatusModifiedRenderer}
			,editor: {
				type: 'select', // 드롭다운 사용
				options: {
					// value는 실제 데이터 값, text는 사용자에게 보이는 값
					listItems: [
						{ text: 'Y', value: 'Y' },
						{ text: 'N', value: 'N' }
					]
				}
			}
		}
	    ,{header: '비고' ,name: 'remark' ,align: 'center', editor: 'text'
			,renderer:{ type: StatusModifiedRenderer}
		}
		,{header: '생성자id' ,name: 'createdId' ,align: 'center'}	
		,{header: '생성일시' ,name: 'createdDate' ,align: 'center'}	
		,{header: '수정자id' ,name: 'updatedId' ,align: 'center'}	
		,{header: '수정일시' ,name: 'updatedDate' ,align: 'center'}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
	    ],
	    data: []
	    ,bodyHeight: 200 // 그리드 본문의 높이를 픽셀 단위로 지정. 스크롤이 생김.
	    ,height:100
	    ,columnOptions: {
	    	resizable: true
        }
	    ,pageOptions: {
	    	useClient: true,
	    	perPage: 10
        }
});


//g- grid2 = grid4 신규라우트 모달 그리드 - 공정코드조회 모달(PROCESS_MASTER 조회)
const grid4 = new Grid({
	    el: document.getElementById('routeStepCodeGrid'),
        rowHeaders: ['rowNum'],
	    columns: [
	    {header: '공정ID' ,name: 'processId' ,align: 'center'}
	    ,{header: '공정명' ,name: 'processName' ,align: 'center'}
	    ,{header: '공정유형' ,name: 'processType' ,align: 'center'}
	    ,{header: '설명' ,name: 'description' ,align: 'center',width: 315}
        ,{header: '사용여부' ,name: 'useYn' ,align: 'center'}
		,{header: '생성자id' ,name: 'createdId' ,align: 'center',hidden: true}
		,{header: '생성일시' ,name: 'createdDate' ,align: 'center',hidden: true}
		,{header: '수정자id' ,name: 'updatedId' ,align: 'center',hidden: true}
		,{header: '수정일시' ,name: 'updatedDate' ,align: 'center',hidden: true}
	    ],
	    data: []
	    ,bodyHeight: 200 // 그리드 본문의 높이를 픽셀 단위로 지정. 스크롤이 생김.
	    ,height:100
	    ,columnOptions: {
	    	resizable: true
        }
});

let processLookupModal; // 공정코드 조회 모달
document.addEventListener("DOMContentLoaded", () => {
  processLookupModal = new bootstrap.Modal(document.getElementById("processLookup-modal"));
});

//신규라우트 모달 오픈
function openRouteModalForCreate(){
	routeModalreset();
	document.getElementById('userAndDate').style.display = 'none';
}

// 신규라우트 -->  공정코드 조회 2번째 모달
function openProcessLookupModal() {
    processLookupModal.show();
}
  
//제품별 공정라우트 그리드 조회
function productRouteSearch(){
	
	const params = {
		prdId: document.getElementById("processprdId").value ?? "",
		routeName: document.getElementById("routeName").value ?? "",		
	};
	
	const queryString = new URLSearchParams(params).toString();
	fetch(`/masterData/process/list?${queryString}`, {
		method: 'GET',
		headers: {
			[csrfHeader]: csrfToken,
			'Content-Type': 'application/json'
		}
	})
	.then(res => {
	    if (!res.ok) {
	        throw new Error(`HTTP error! status: ${res.status}`);
	    }
	    
	    // 💡 추가된 로직: 응답 본문이 비어 있는지 확인
	    const contentType = res.headers.get("content-type");
	    if (!contentType || !contentType.includes("application/json")) {
	        // Content-Type이 JSON이 아니거나, 200 OK인데 본문이 비어있다면 (Empty)
	        if (res.status === 204 || res.headers.get("Content-Length") === "0") {
	             return []; // 빈 배열 반환하여 grid 오류 방지
	        }
	        // JSON이 아닌 다른 데이터(HTML 오류 등)가 있다면 텍스트로 읽어 오류 발생
	        return res.text().then(text => {
	            throw new Error(`Expected JSON but received: ${text.substring(0, 100)}...`);
	        });
	    }

	    return res.json(); // 유효한 JSON일 때만 파싱 시도
	})
		.then(data => {
			console.log("검색데이터 grid1:", data);
			data.map(item => {
				item.prdId = item.product.prdId;
			});
			grid1.resetData(data);
		})
		.catch(err => {
			console.error("조회오류", err);
			grid1.resetData([]);
		
		});
	
}

//공정코드 관리 그리드 조회
function processCodeGridAllSearch() {
	fetch('/masterData/processCode/list', {
			method: 'GET',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			},
		})
		.then(res => {
		    if (!res.ok) {
		        throw new Error(`HTTP error! status: ${res.status}`);
			}
			return res.json();
		})
		.then(data => {
			console.log("검색데이터 grid2:", data);
			grid2.resetData(data);
			grid4.resetData(data);//신규라우트 모달 그리드 - 공정코드조회 모달
		})
		.catch(err => {	
			console.error("조회오류", err);
			grid2.resetData([]);
		});
}
//grid3 신규라우트 모달 그리드 - 공정단계 조회
function processStepSearch(routeId) {
	
	fetch(`/masterData/processStep/list?routeId=${routeId}`, {
			method: 'GET',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			},
		})
		.then(res => {
		    if (!res.ok) {
		        throw new Error(`HTTP error! status: ${res.status}`);
			}
			return res.json();
		})
		.then(data => {
			data.map((item) => {
				item.routeId = item.routeHeader.routeId;
				item.processId = item.process.processId;
			});
			grid3.resetData(data);
		})
		.catch(err => {	
			console.error("조회오류", err);
			grid3.resetData([]);
		});
}
grid1.on("click", async (ev) => {

	const target = ev.nativeEvent.target;
	// const targetElement = ev.nativeEvent.target; 이 줄이 빠진 경우
	if (ev.targetType === 'cell' && target.tagName === 'BUTTON') {
		console.log('Button in cell clicked, rowKey:', ev.rowKey);
		
		const rowData = grid1.getRow(ev.rowKey);
		console.log('Row data:', rowData);
		console.log('라우트ID:', rowData.routeId);
		
		// 예: 모달 열기, 상세 정보 표시 등		
		$('#route-modal').modal('show');
		document.getElementById('modalRouteId').value = rowData.routeId;//라우트 ID
		document.getElementById('modalProcessprdId').value = rowData.prdId;//제품코드
		document.getElementById('modalRouteName').value = rowData.routeName;//라우트명
		document.getElementById('modalRouteUseYn').value = rowData.useYn;//사용여부
		document.getElementById('modalRouteRemark').value = rowData.description;//비고
		document.getElementById('modalRouteCreatedId').value = rowData.createdId;//생성자
		document.getElementById('modalRouteCreatedDate').value = rowData.createdDate;//생성일시
		document.getElementById('modalRouteUpdatedId').value = rowData.updatedId;//수정자
		document.getElementById('modalRouteUpdatedDate').value = rowData.updatedDate;//수정일시
		document.getElementById('userAndDate').style.display = 'flex';
		processStepSearch(rowData.routeId);//신규라우트 모달 그리드 - 공정단계 조회
	}

});

//라우트 모달 셀렉트박스 값선택시 자동으로 routeId생성
document.getElementById('modalProcessprdId').addEventListener('change', function() {
	const prdId = this.value;
	const timestamp = Date.now(); // 현재 시간을 밀리초로 가져옴
	console.log('제품코드 선택됨:', prdId);
	console.log('타임스탬프:', timestamp);
	const generatedRouteId = `RT-${prdId}`; // 예: RT-제품코드-타임스탬프
	document.getElementById('modalRouteId').value = generatedRouteId;
});


//라우트모달 리셋
function routeModalreset() {
	document.getElementById('modalRouteId').value = '';//라우트 ID
	document.getElementById('modalProcessprdId').value = '';//제품코드
	document.getElementById('modalRouteName').value = '';//라우트명
	document.getElementById('modalRouteUseYn').value = 'Y';//사용여부
	document.getElementById('modalRouteRemark').value = '';//비고
	document.getElementById('modalRouteCreatedId').value = '';//생성자
	document.getElementById('modalRouteCreatedDate').value = '';//생성일시
	document.getElementById('modalRouteUpdatedId').value = '';//수정자
	document.getElementById('modalRouteUpdatedDate').value = '';//수정일시
	grid3.resetData([]);//신규라우트 모달 그리드 - 공정단계 조회 초기화
	processCodeGridAllSearch();//공정코드 관리 그리드 조회
}
//라우트모달 공정단계 단계추가
function addRouteStepRow(){
	grid3.appendRow();
}
// 라우트모달 공정단계 저장
const saveRouteBtn = document.getElementById('saveRouteBtn');
saveRouteBtn.addEventListener('click', function() {

	console.log('저장버튼 클릭됨');
	
	const modifiedData = (typeof grid3.getModifiedRows === 'function') ? (grid3.getModifiedRows() || {}) : {};
	const updatedRows = Array.isArray(modifiedData.updatedRows) ? modifiedData.updatedRows : [];
	let createdRows = Array.isArray(modifiedData.createdRows) ? modifiedData.createdRows : [];
	
	const routeNewData = {
		routeId: document.getElementById('modalRouteId').value ?? "",
		prdId: document.getElementById('modalProcessprdId').value ?? "",
		routeName: document.getElementById('modalRouteName').value ?? "",
		useYn: document.getElementById('modalRouteUseYn').value ?? "",
		description: document.getElementById('modalRouteRemark').value ?? ""
	};
	console.log('라우트저장데이터:', routeNewData);
	// 생성된 공정단계의 라우트ID가 라우트정보의 라우트ID와 일치하는지 확인
	createdRows.forEach(row => {
		if(row.routeId != routeNewData.routeId){
			alert( '라우트정보의 라우트ID와 생성된 공정단계의 라우트ID가 일치하지 않습니다. 라우트ID를 확인해주세요.');
		 	return;
		}
	});

	// 누락된 입력 항목들을 하나로 모아 사용자에게 알림
	const missing = [];
	if (!routeNewData.prdId || String(routeNewData.prdId).trim() === '') missing.push('제품코드');
	if (!routeNewData.routeName || String(routeNewData.routeName).trim() === '') missing.push('라우트명');
	if (missing.length > 0) {
		alert(missing.join(' 및 ') + '을(를) 입력해주세요.');
		return;
	}else{
		modifiedData.routeInfo = routeNewData;
	}
	

	// 새로 추가된 행 중 모든 필드가 비어있는(빈 행) 경우 그리드에서 제거하고 서버 전송 대상에서 제외
	const isRowEmpty = (row) => {
		if (!row) return true;
		const vals = Object.values(row);
		if (vals.length === 0) return true;
		return vals.every(v => v === null || v === undefined || (typeof v === 'string' && v.trim() === ''));
	};
	const emptyCreated = createdRows.filter(isRowEmpty);
	if (emptyCreated.length > 0) {
		emptyCreated.forEach(r => {
			try {
				const key = r && (r.rowKey || r.matId);
				if (key && typeof grid3.removeRow === 'function') {
					grid3.removeRow(key);
				} else if (key && typeof grid3.deleteRow === 'function') {
					grid3.deleteRow(key);
				}
			} catch (e) {
				console.warn('빈 행 삭제 실패', e);
			}
		});
		// 서버로 보낼 createdRows에서 빈 행 제외
		createdRows = createdRows.filter(r => !isRowEmpty(r));
		// 반영: modifiedData 객체에도 반영해 전송값 일관성 유지
		try { modifiedData.createdRows = createdRows; } catch (e) {}
	}

	if (updatedRows.length === 0 && createdRows.length === 0) {
		if(confirm('공정단계 그리드 내용이 없습니다. 계속 진행하시겠습니까?') === false) {
			return;
		}
	}

	console.log('수정된 데이터:', modifiedData);
	fetch('/masterData/process/save', {
		method: 'POST',
		credentials: 'same-origin',
		headers: {
			[csrfHeader]: csrfToken,
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(modifiedData)
	})
	.then(async res => {
	    if (!res.ok) {
	        throw new Error(`HTTP error! status: ${res.status}`);
	    }
	    // 응답 Content-Type 확인: JSON이면 파싱, 아니면 텍스트로 읽음
	    const contentType = res.headers.get('content-type') || '';
	    if (contentType.includes('application/json')) {
	        const data = await res.json();
			return ({ type: 'json', data });
	    }
	    const text = await res.text();
		return ({ type: 'text', data: text });
	})
	.then(resp => {
	    if (!resp) return;
	    if (resp.type === 'json') {
	        console.log('저장결과(JSON):', resp.data);
	        // 서버에서 JSON 형태로 상태를 보내는 경우 추가 처리 가능
	        alert('저장 완료');
	    } else {
	        const text = String(resp.data || '').trim();
	        console.log('저장결과(텍스트):', text);
	        if (text === 'success') {
	            alert('저장 완료');
	        } else if (text === 'no-data') {
	            alert('서버: 전송한 데이터가 없습니다. 내용을 확인하세요.');
	        } else if (text.startsWith('error')) {
	            alert('저장 중 오류: ' + text);
	        } else {
	            // 미확인 텍스트 응답
	            alert('저장 완료 (서버 응답: ' + text.substring(0, 200) + ')');
	        }
	    }
	})
	.catch(err => {
		console.error('저장오류', err);
		alert('저장 중 오류가 발생했습니다. 콘솔 로그를 확인하세요.');
	});
});


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