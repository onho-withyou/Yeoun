window.onload = function () {	
	qcItemGridAllSearch();//품질항목기준
}


const Grid = tui.Grid;

//g-grid1 품질항목기준
const grid1 = new Grid({
	  el: document.getElementById('qcItemGrid'), 
      rowHeaders: ['rowNum','checkbox'],
	  columns: [

		{header: 'QC 항목 ID' ,name: 'qcItemId' ,align: 'center'}
		,{header: '항목명' ,name: 'itemName' ,align: 'center'}
		,{header: '대상구분' ,name: 'targetType' ,align: 'center',width: 110,filter: "select"
			,renderer:{ type: StatusModifiedRenderer}
		}
		,{header: '단위' ,name: 'unit' ,align: 'center'
			,renderer:{ type: StatusModifiedRenderer}
		}
		,{header: '기준 텍스트' ,name: 'stdText' ,align: 'center',width: 230}
		,{header: 'MIN' ,name: 'minValue' ,align: 'center'}
        ,{header: 'MAX' ,name: 'maxValue' ,align: 'center'}
		,{header: '사용여부' ,name: 'useYn' ,align: 'center',width: 100
			,renderer:{ type: StatusModifiedRenderer
				,options: {
					isSelect: false   // ⭐ 이걸로 구분
				}
			}
			,editor: {
				type: 'select', // 드롭다운 사용
				options: {
					// value는 실제 데이터 값, text는 사용자에게 보이는 값
					listItems: [
						{value: 'Y', text: '활성'},
						{value: 'N', text: '비활성'}
					]
				}
			}
		}  
		,{header: '정렬순서' ,name: 'sortOrder' ,align: 'center',hidden: true}
		,{header: '생성자id' ,name: 'createdId' ,align: 'center',hidden: true} 
		,{header: '생성자이름' ,name: 'createdByName' ,align: 'center',hidden: true}
		,{header: '생성일시' ,name: 'createdDate' ,align: 'center',hidden: true}  
		,{header: '수정자id' ,name: 'updatedId' ,align: 'center',hidden: true}  
		,{header: '수정자이름' ,name: 'updatedByName' ,align: 'center',hidden: true}
		,{header: '수정일시' ,name: 'updatedDate' ,align: 'center',hidden: true}
		,{
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
    		perPage: 20
  	  }
	});

//qcitem  품질항목관리 조회
function qcItemGridAllSearch(){

	const params = {
		qcItemId: document.getElementById("qcItemId").value ?? "",
	};
	const queryString = new URLSearchParams(params).toString();
	fetch(`/masterData/qc_item/list?${queryString}`, {
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
				
				console.log("검색데이터:", data);
				const camelCaseData = transformKeys(data);
				console.log("camelCaseData",camelCaseData);
				grid1.resetData(camelCaseData);
			})
			.catch(err => {
				console.error("조회오류", err);
				//grid1.resetData([]);
			
			});
	 
}


const toCamelCase = (snakeCaseString) => {
  if (!snakeCaseString || typeof snakeCaseString !== 'string') {
    return snakeCaseString;
  }

  // 1. 소문자로 변환
  // 2. 언더스코어(_)를 기준으로 문자열을 분리
  // 3. reduce를 사용하여 카멜 케이스로 조합
  return snakeCaseString.toLowerCase().split('_').reduce((acc, part) => {
    // 첫 번째 파트는 그대로 사용 (created)
    if (acc === '') {
      return part;
    }
    // 두 번째 파트부터는 첫 글자를 대문자로 변환 후 뒤에 붙임 (ByName)
    return acc + part.charAt(0).toUpperCase() + part.slice(1);
  }, '');
};

const transformKeys = (data) => {
  if (Array.isArray(data)) {
    // 배열이면 배열의 모든 요소에 대해 재귀 호출
    return data.map(transformKeys);
  }

  if (data !== null && typeof data === 'object') {
    // 객체이면 키를 순회하며 변환
    const newObject = {};
    for (const key in data) {
      if (Object.prototype.hasOwnProperty.call(data, key)) {
        const newKey = toCamelCase(key);
        // 값도 객체나 배열일 수 있으므로 재귀적으로 처리
        newObject[newKey] = transformKeys(data[key]);
      }
    }
    return newObject;
  }

  // 객체나 배열이 아니면 값 그대로 반환 (문자열, 숫자, null 등)
  return data;
};


// IME 및 문자 입력 보조: 전역 헬퍼 사용 (안전한 등록 — 로드 순서와 무관)
(function registerGridIme() {
	const gridsToRegister = [
		{ id: 'qcItemGrid', grid: grid1, containerId: 'qcItemGrid' }
	];
	if (typeof initGridImeSupport === 'function') {
		initGridImeSupport(gridsToRegister);
	} else {
		window.__pendingGridImeGrids = window.__pendingGridImeGrids || [];
		window.__pendingGridImeGrids.push(...gridsToRegister);
		console.debug('Queued grids for initGridImeSupport (will initialize when helper loads)');
	}
})();

grid1.on('afterChange', (ev) => {
  ev.changes.forEach(change => {
    // 특정 컬럼이 바뀌었을 때만 즉시 저장
    if (change.columnName === 'useYn') {
      const rowData = grid1.getRow(change.rowKey);
      //console.log("rowData useYn--->",rowData.useYn);
	  //활성/비활성 즉각 변화 저장
	  // 행의 qcItemId와 변경된 useYn 값만 전송
	  if (rowData && rowData.qcItemId) {
		  saveQcItemQuick(rowData.qcItemId, rowData.useYn);
	  }
    }
  });
});

// useYn 컬럼 즉시 저장(간단한 폼 데이터 전송)
function saveQcItemQuick(qcItemId, useYn) {
		const params = new URLSearchParams();
		params.append('mode', 'modify');
		params.append('qcItemId', qcItemId);
		params.append('useYn', useYn || 'N');

		fetch('/masterData/qcItem/save', {
				method: 'POST',
				credentials: 'same-origin',
				headers: {
						[csrfHeader]: csrfToken,
						'Content-Type': 'application/x-www-form-urlencoded',
						'X-Requested-With': 'XMLHttpRequest'
				},
				body: params.toString()
		})
		.then(res => res.text())
		.then(result => {
				// 기존의 응답 처리 재사용
				handleQcResponse(result);
		})
		.catch(err => {
				console.error('useYn 저장 오류', err);
				alert('사용여부 저장 중 오류가 발생했습니다.');
		});
}


grid1.on("click", async (ev) => {

	const target = ev.nativeEvent.target;
	// const targetElement = ev.nativeEvent.target; 이 줄이 빠진 경우
	if (ev.targetType === 'cell' && target.tagName === 'BUTTON') {
		console.log('Button in cell clicked, rowKey:', ev.rowKey);
		
		const rowData = grid1.getRow(ev.rowKey);
		console.log('rowData data:', rowData);
		
		// 예: 모달 열기, 상세 정보 표시 등		
		$('#qcItem-modal').modal('show');
		document.getElementById('qcmodalTilte').innerText= 'QC 항목 상세';
		document.getElementById('modalQcItemId').value = rowData.qcItemId;//QC 항목 ID
		document.getElementById('itemName').value = rowData.itemName;//항목명
		document.getElementById('targetType').value = rowData.targetType;//대상구분
		document.getElementById('unit').value = rowData.unit;//단위
		document.getElementById('stdText').value = rowData.stdText;//기준텍스트
		document.getElementById('minValue').value = rowData.minValue;//최소값
		document.getElementById('maxValue').value = rowData.maxValue;//최대값
		document.getElementById('sortOrder').value = rowData.sortOrder;//정렬순서
		document.getElementById('useYn').value = rowData.useYn;//사용여부
		document.getElementById('createdId').value = rowData.createdByName;//생성자
		document.getElementById('createdDate').value = rowData.createdDate;//생성일시
		document.getElementById('updatedId').value = rowData.updatedByName;//수정자
		document.getElementById('updatedDate').value = rowData.updatedDate;//수정일시
		
		document.getElementById('qcItemId').readOnly = true;
		document.getElementById('userAndDate').style.display = 'flex';

		qcItemGridAllSearch();
	}

});

/**
 * 1. 응답 처리 함수
 * (가장 먼저 정의하거나, 최소한 호출하는 곳보다 위에 배치하는 것이 안전합니다)
 */
const handleQcResponse = (result) => {
    console.log('서버 응답:', result);
    const lowerResult = (result || '').toLowerCase();

    // 에러 메시지 처리
    if (lowerResult.startsWith('error')) {
        const errorMsg = result.replace(/^error:\s*/i, '').trim();
        alert(errorMsg);
        return;
    }

    // 성공 처리
    if (lowerResult.includes('success')) {
        //alert('저장되었습니다.');
        const closeBtn = document.querySelector('#qcItem-modal .modal-footer [data-bs-dismiss="modal"]');
        if (closeBtn) closeBtn.click();
        
        // 함수 존재 여부 확인 후 실행
        if (typeof qcModalreset === 'function') qcModalreset();
        if (typeof qcItemGridAllSearch === 'function') qcItemGridAllSearch();
    } else {
        alert(result || '알 수 없는 오류가 발생했습니다.');
    }
};

/**
 * 2. 전송 실행 함수
 */
function saveQcItem(form) {
    // 모드 설정 로직
    const modalTitleElem = document.getElementById('qcmodalTilte');
    const modeValue = (modalTitleElem && modalTitleElem.innerText === 'QC 항목 등록') ? 'new' : 'modify';
    
    let modeInput = form.querySelector('input[name="mode"]');
    if (!modeInput) {
        modeInput = document.createElement('input');
        modeInput.type = 'hidden';
        modeInput.name = 'mode';
        form.appendChild(modeInput);
    }
    modeInput.value = modeValue;

    const formData = new FormData(form);
    const params = new URLSearchParams(formData);

    fetch(form.action, {
        method: form.method || 'POST',
        credentials: 'same-origin',
        headers: {
            [csrfHeader]: csrfToken, // 상단에 선언되어 있어야 함
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: params.toString()
    })
    .then(res => {
        if (!res.ok) return res.text().then(t => { throw new Error(`HTTP ${res.status}: ${t || res.statusText}`); });
        return res.text();
    })
    .then(result => {
        // 여기서 위에서 정의한 함수 호출
        handleQcResponse(result);
    })
    .catch(err => {
        console.error('QC 저장 오류', err);
        alert(err.message || '저장 중 오류가 발생했습니다.');
    });
}

/**
 * 3. 이벤트 바인딩 (DOM이 로드된 후 실행)
 */
document.addEventListener('DOMContentLoaded', () => {
    const qcItemForm = document.querySelector('#qcItem-modal form');
    if (qcItemForm) {
        qcItemForm.addEventListener('submit', function (ev) {
            ev.preventDefault();
            saveQcItem(ev.target);
        });
    }
});
window.addEventListener('keydown', (e) => e.stopPropagation(), true);


// 항목 등록
const qcItemRegistBtn = document.getElementById('qcItemRegistBtn');
qcItemRegistBtn.addEventListener("click", function() {
	document.getElementById('qcmodalTilte').innerText= 'QC 항목 등록';
	qcModalreset();
	document.getElementById('modalQcItemId').value = 'QC-';
	document.getElementById('qcItemId').readOnly = false;
	document.getElementById('userAndDate').style.display ='none';//생성자
	
});

function qcModalreset() {
	document.getElementById('modalQcItemId').value = '';//QC 항목 ID
	document.getElementById('itemName').value = '';//항목명
	document.getElementById('targetType').value = '';//대상구분
	document.getElementById('unit').value = '';//단위
	document.getElementById('stdText').value = '';//기준텍스트
	document.getElementById('minValue').value = '';//최소값
	document.getElementById('maxValue').value = '';//최대값
	document.getElementById('sortOrder').value = '';//정렬순서
	document.getElementById('useYn').value = '';//사용여부
	document.getElementById('createdId').value = '';//생성자
	document.getElementById('createdDate').value = '';//생성일시
	document.getElementById('updatedId').value = '';//수정자
	document.getElementById('updatedDate').value = '';//수정일시
	qcItemGridAllSearch();//공정코드 관리 그리드 조회
}


// 품질항목관리 삭제
const deleteQcRowBtn = document.getElementById('deleteQcRowBtn');
deleteQcRowBtn.addEventListener('click', async function() {

	// 체크된 rowKey들 수집
	let rowKeysToDelete = [];
	try {
		if (typeof grid1.getCheckedRowKeys === 'function') {
			rowKeysToDelete = grid1.getCheckedRowKeys() || [];
		} else if (typeof grid1.getCheckedRows === 'function') {
			const checkedRows = grid1.getCheckedRows() || [];
			rowKeysToDelete = checkedRows.map(r => r && (r.rowKey || r.qcItemId)).filter(Boolean);
		}
	} catch (e) {
		console.warn('체크된 행 조회 실패', e);
	}
	if (!Array.isArray(rowKeysToDelete) || rowKeysToDelete.length === 0) {
		alert('삭제할 행을 선택(체크)해주세요.');
		return;
	}
	// 간결한 방식으로 각 rowKey로부터 qcItemId(또는 식별 가능한 ID)를 수집
	const getAllData = () => (typeof grid1.getData === 'function' ? grid1.getData() : (grid1.data || []));
	const qcItemIds = rowKeysToDelete.map(key => {
		try {
			const row = (typeof grid1.getRow === 'function' && grid1.getRow(key)) ||
				getAllData().find(d => d && (String(d.rowKey) === String(key) || String(d.qcItemId) === String(key)));
			return row && row.qcItemId ? String(row.qcItemId) : String(key);
		}
		catch (e) {
			console.warn('삭제 ID 수집 중 오류', e);
			return String(key);
		}
	}).filter(Boolean);

	if (!confirm('선택한 항목을 삭제하시겠습니까?')) return;
	fetch('/masterData/qcItem/delete', {
		method: 'POST',
		credentials: 'same-origin',
		headers: {
			[csrfHeader]: csrfToken,
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(qcItemIds)
	})
	.then(res => {
		if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
		const ct = (res.headers.get('content-type') || '').toLowerCase();
		if (ct.includes('application/json')) return res.json();
		return res.text();
	}
	)
	.then(parsed => {
		console.log('삭제 응답:', parsed);
		const okTexts = ['success','ok','true'];
		if (typeof parsed === 'string') {
			if (!okTexts.includes(parsed.trim().toLowerCase())) throw new Error('Unexpected response: ' + parsed);
		}
		else if (!(parsed && (parsed.status === 'success' || okTexts.includes((parsed.message||'').toString().toLowerCase())))) {
			throw new Error('삭제 실패: ' + JSON.stringify(parsed));
		}
		// 서버 삭제 성공 시 그리드 재조회
		qcItemGridAllSearch();
	})
	.catch(err => {
		console.error('삭제 중 오류', err);
		try { alert('삭제 중 오류가 발생했습니다. ' + (err && err.message ? err.message : '')); } catch (e) {}
	});
});






