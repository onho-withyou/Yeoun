
window.onload = function () {	
	productGridAllSearch();//완제품 그리드 조회
	materialGridAllSearch()//원재료 그리드 조회

}

document.querySelectorAll('button[data-bs-toggle="tab"]').forEach(tab => {
    tab.addEventListener('shown.bs.tab', function (e) {
        const targetId = e.target.getAttribute('data-bs-target');

        if (targetId === '#navs-product-tab') {//완제품탭
            grid1.refreshLayout();
        } else if (targetId === '#navs-material-tab') {//원재료 탭
            grid2.refreshLayout();
        }
    });
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

//g-grid1 완제품(상위품번)
const grid1 = new Grid({
	  el: document.getElementById('productGrid'),
      rowHeaders: [
			{ type: 'rowNum', header: 'No.'},
			{ type: 'checkbox' } // 체크박스를 사용한다면 이것도 추가
		],
	  columns: [
		{header: '품번' ,name: 'prdId' ,align: 'center',editor: 'text',width: 100
			,renderer:{ type: StatusModifiedRenderer}	
		}
		,{header: '품목명' ,name: 'itemName' ,align: 'center',editor: 'text',width: 100
			,renderer:{ type: StatusModifiedRenderer}
		}
		,{header: '제품명' ,name: 'prdName' ,align: 'center',editor: 'text'
			,renderer:{ type: StatusModifiedRenderer}
		}
		,{header: '제품유형' ,name: 'prdCat' ,align: 'center',filter: "select"
			,renderer:{ type: StatusModifiedRenderer}
			,editor: {
				type: 'select', // 드롭다운 사용
				options: {
					// value는 실제 데이터 값, text는 사용자에게 보이는 값
					listItems: [
						{ text: '완제품', value: '완제품' },
						{ text: '반제품', value: '반제품' }
					]
				}
			}
		}
		,{header: '단위' ,name: 'prdUnit' ,align: 'center'
			,renderer:{ type: StatusModifiedRenderer}
			,editor: {
				type: 'select', // 드롭다운 사용
				options: {
					// value는 실제 데이터 값, text는 사용자에게 보이는 값
					listItems: [
						{ text: 'g', value: 'g' },
						{ text: 'ml', value: 'ml' },
						{ text: 'EA', value: 'EA' }
					]
				}
			}
		}
		,{header: '단가' ,name: 'unitPrice' ,align: 'center',editor: 'text'
			,renderer:{ type: StatusModifiedRenderer}
		}
        ,{header: '상태' ,name: 'prdStatus' ,align: 'center'
			,renderer:{ type: StatusModifiedRenderer}
			,editor: {
				type: 'select', // 드롭다운 사용
				options: {
					// value는 실제 데이터 값, text는 사용자에게 보이는 값
					listItems: [
						{ text: 'ACTIVE', value: 'ACTIVE' },//활성
						{ text: 'INACTIVE', value: 'INACTIVE' },//비활성
						{ text: 'DISCONTINUED', value: 'DISCONTINUED' },//단종
						{ text: 'SEASONAL', value: 'SEASONAL' },//시즌상품
						{ text: 'OUT_OF_STOCK', value: 'OUT_OF_STOCK' }//단종
					]
				}
			}
		}
		,{header: '유효일자' ,name: 'effectiveDate' ,align: 'center',editor: 'text'
			,renderer:{ type: StatusModifiedRenderer}
		}
        ,{header: '제품상세설명' ,name: 'prdSpec' ,align: 'center',editor: 'text'
			,renderer:{ type: StatusModifiedRenderer}
		}
        ,{header: '생성자ID' ,name: 'createdId' ,align: 'center',editor: 'text'
			,renderer:{ type: StatusModifiedRenderer}
		}
        ,{header: '생성일자' ,name: 'createdDate' ,align: 'center',editor: 'text'
			,renderer:{ type: StatusModifiedRenderer}
		}
        ,{header: '수정자ID' ,name: 'updatedId' ,align: 'center',editor: 'text'
			,renderer:{ type: StatusModifiedRenderer}
		}
        ,{header: '수정일시' ,name: 'updatedDate' ,align: 'center',editor: 'text'
			,renderer:{ type: StatusModifiedRenderer}
		}           
	  ]
	  ,data: []
	  ,bodyHeight: 500 // 그리드 본문의 높이를 픽셀 단위로 지정. 스크롤이 생김.
	  ,height:100
	  ,columnOptions: {
    		resizable: true
  	  }
	  ,pageOptions: {
			useClient: true
			,perPage: 20
		}
});

//g-grid2 원재료(하위품번)
const grid2 = new Grid({
	    el: document.getElementById('materialGrid'),
        rowHeaders: ['rowNum','checkbox'],
	    columns: [
		    {header: '원재료ID' ,name: 'matId' ,align: 'center'}
		    ,{header: '원재료 품목명' ,name: 'matName' ,align: 'center'}//
		    ,{header: '원재료 유형' ,name: 'matType' ,align: 'center',filter: "select"}
		    ,{header: '단위' ,name: 'matUnit' ,align: 'center'}
	        ,{header: '유효일자' ,name: 'effectiveDate' ,align: 'center'}
	        ,{header: '상세설명(원재료)' ,name: 'matDesc' ,align: 'center',width: 280}
	        ,{header: '생성자ID' ,name: 'createdId' ,align: 'center'}
	        ,{header: '생성일자' ,name: 'createdDate' ,align: 'center'}
	        ,{header: '수정자ID' ,name: 'updatedId' ,align: 'center',hidden: true}
	        ,{header: '수정일시' ,name: 'updatedDate' ,align: 'center',hidden: true}           
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

function productGridAllSearch() {

	fetch('/masterData/product/list', {
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
			grid1.resetData(data);
		})
		.catch(err => {
			console.error("조회오류", err);
			//grid1.resetData([]);
		
		});

}

function materialGridAllSearch() {

	fetch('/material/list', {
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

	    return res.json(); // 유효한 JSON일 때만 파싱 시도
	})
		.then(data => {
			
			console.log("검색데이터:", data);
			grid2.resetData(data);
		})
		.catch(err => {
			console.error("조회오류", err);
			grid2.resetData([]);
		
		});

}

//row 추가
const addProductRowBtn = document.getElementById('addProductRowBtn');
addProductRowBtn.addEventListener('click', function() {
   grid1.prependRow();
});

//row 저장: POST JSON형식으로 서버에 요청
const saveProductRowBtn = document.getElementById('saveProductRowBtn');
saveProductRowBtn.addEventListener('click', function() {

	const modifiedData = (typeof grid1.getModifiedRows === 'function') ? (grid1.getModifiedRows() || {}) : {};
	const updatedRows = Array.isArray(modifiedData.updatedRows) ? modifiedData.updatedRows : [];
	const createdRows = Array.isArray(modifiedData.createdRows) ? modifiedData.createdRows : [];
	

	if (updatedRows.length === 0 && createdRows.length === 0) {
		alert('수정된 내용이 없습니다.');
		return;
	}

	fetch('/masterData/product/save', {
		method: 'POST',
		credentials: 'same-origin',
		headers: {
			[csrfHeader]: csrfToken,
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(modifiedData)
	})
	.then(res => {
		if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
		return res.text();
	})
	.then(parsed => {
		if (typeof parsed === 'string') {
			const trimmed = parsed.trim().toLowerCase();
			const okTexts = ['success', 'ok', 'true'];
			if (!okTexts.includes(trimmed)) throw new Error('Unexpected response: ' + parsed);
			console.log('저장 성공 (text):', parsed);
		} else {
			console.log('저장 성공 (json):', parsed);
		}
		productGridAllSearch();
	})
	.catch(err => {
		console.error('저장 오류', err);
		try { alert('저장 중 오류가 발생했습니다. ' + (err && err.message ? err.message : '')); } catch (e) {}
	});
});

//row 삭제: POST JSON형식으로 서버에 요청
const deleteProductRowBtn = document.getElementById('deleteProductRowBtn');
deleteProductRowBtn.addEventListener('click', async function() {
	if (!confirm('선택된 행을 삭제하시겠습니까?')) return;

	// 체크된 rowKey들 수집
	let rowKeysToDelete = [];
	try {
		if (typeof grid1.getCheckedRowKeys === 'function') {
			rowKeysToDelete = grid1.getCheckedRowKeys() || [];
		} else if (typeof grid1.getCheckedRows === 'function') {
			const checkedRows = grid1.getCheckedRows() || [];
			rowKeysToDelete = checkedRows.map(r => r && (r.rowKey || r.prdId)).filter(Boolean);
		}
	} catch (e) {
		console.warn('체크된 행 조회 실패', e);
	}

	if (!Array.isArray(rowKeysToDelete) || rowKeysToDelete.length === 0) {
		alert('삭제할 행을 선택(체크)해주세요.');
		return;
	}

	// 간결한 방식으로 각 rowKey로부터 prdId(또는 식별 가능한 ID)를 수집
	const getAllData = () => (typeof grid1.getData === 'function' ? grid1.getData() : (grid1.data || []));
	const prdIds = rowKeysToDelete.map(key => {
		try {
			const row = (typeof grid1.getRow === 'function' && grid1.getRow(key)) ||
				getAllData().find(d => d && (String(d.rowKey) === String(key) || String(d.prdId) === String(key)));
			return row && row.prdId ? String(row.prdId) : String(key);
		} catch (e) {
			console.warn('삭제 ID 수집 중 오류', e);
			return String(key);
		}
	}).filter(Boolean);

		fetch('/masterData/product/delete', {
			method: 'POST',
			credentials: 'same-origin',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(prdIds)
		})
		.then(res => {
			if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
			const ct = (res.headers.get('content-type') || '').toLowerCase();
			if (ct.includes('application/json')) return res.json();
			return res.text();
		})
		.then(parsed => {
			console.log('삭제 응답:', parsed);
			if (typeof parsed === 'string') {
				const trimmed = parsed.trim().toLowerCase();
				const okTexts = ['success', 'ok', 'true'];
				if (!okTexts.includes(trimmed)) throw new Error('Unexpected response: ' + parsed);
				productGridAllSearch();
				return;
			}
			if (parsed && parsed.status === 'success') {
				productGridAllSearch();
			} else {
				const msg = parsed && parsed.message ? parsed.message : JSON.stringify(parsed);
				throw new Error('삭제 실패: ' + msg);
			}
		})
		.catch(err => {
			console.error('삭제 중 오류', err);
			try { alert('삭제 중 오류가 발생했습니다. ' + (err && err.message ? err.message : '')); } catch (e) {}
		});
	
});


