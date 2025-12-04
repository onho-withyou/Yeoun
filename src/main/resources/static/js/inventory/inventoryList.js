// 재고조회 js
// 전역변수
let inventoryGrid; // 그리드 객체 변수
let inventoryData = []; // 그리드로 그려지는 데이터 저장
let locationInfo = [];
// 문서 로딩 후 시작
document.addEventListener('DOMContentLoaded', async function () {
	// 창고정보 저장
	locationInfo = await getLocationInfo();
	inputLocationInfo(locationInfo);

	initGrid();
	//최초로딩
	const firstSearchData = getSearchData();
	const firstData = await fetchInventoryData(firstSearchData);
	
	// 받아온 데이터로 그리드 생성
	inventoryGrid.resetData(firstData);
	inventoryGrid.sort('ibDate', true);
	// 그리드생성한 재고데이터를 저장
	inventoryData = firstData;	
});

// 검색 데이터 설정(검색, 상세검색 입력값으로 requestBody생성)
async function getSearchData() {
	function addDefaultTime(dateStr, timeStr = "00:00:00") {
		if(!dateStr) return '';
		// 시간이 없을 경우 00:00:00 추가
		if(dateStr.length === 10) {
			return `${dateStr} ${timeStr}`;
		}
		//시간이 있으면 다시반환
		return dateStr;
	}
	
	return {
		lotNo: document.getElementById('searchLotNo').value.trim(),
		prodName: document.getElementById('searchProdName').value.trim(),
		itemType: document.getElementById('searchCategory')?
			document.getElementById('searchCategory').value:'',
		zone: document.getElementById('searchZone')?
			document.getElementById('searchZone').value:'',
		rack: document.getElementById('searchRack')?
			document.getElementById('searchRack').value:'',
		status: document.getElementById('searchStatus')?
			document.getElementById('searchStatus').value:'',
		ibDate: document.getElementById('searchDate')?
			addDefaultTime(document.getElementById('searchDate').value,"00:00:00"):'',
		expirationDate: document.getElementById('searchExpireDate')?
			addDefaultTime(document.getElementById('searchExpireDate').value,"00:00:00"):''
	};
}

// 검색데이터에 기반하여 재고 데이터 정보 가져오기
async function fetchInventoryData(searchData) {
	const response = 
		await fetch('/api/inventories', {
			method: 'POST',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(searchData)
			
		});
//	console.log(response);
	if (!response.ok) {
		throw new Error('재고데이터를 가져올 수 없습니다.')
	}
	return await response.json();
} 

// 검색버튼 이벤트함수
const btnSearch = document.getElementById('btnSearch');

btnSearch.addEventListener('click', async () => {
	event.preventDefault(); // 폼제출 막기
	
	const searchData = await getSearchData();
	const gridData = await fetchInventoryData(searchData)
	// 받아온 데이터로 그리드 생성
	inventoryGrid.resetData(gridData);
	// 그리드생성한 재고데이터를 저장
	inventoryData = gridData;	
});

// 그리드 설정
function initGrid() {
	const inventoryGridEl = document.getElementById('inventoryGrid');
	const Grid = tui.Grid;
	inventoryGrid = new Grid({
		el: inventoryGridEl,
		bodyHeight: 'auto',
//		rowHeaders:['rowNum'],
		pageOptions: {
		    useClient: true,  // 클라이언트 사이드 페이징
		    perPage: 20       // 페이지당 20개 행
		},	
		columns: [
		  { header: 'LOT 번호',  name: 'lotNo',    minWidth: 180 },
		  { header: '상품명',    name: 'prodName', minWidth: 180 },
		  { header: '재고량',    name: 'ivAmount', width: 80, align: 'right' },
		  { header: '출고예정',    name: 'expectObAmount', width: 80, align: 'right' },
		  {
		    header: '위치', name: 'location', minWidth: 80,
		    formatter: ({ row }) => {
		      const z  = row.zone  || '';
		      const r  = row.rack  || '';
		      const rr = row.rackRow || '';
		      const rc = row.rackCol || '';
		      
		      return [z, r, rr, rc].filter(v => v).join('-'); // 예: "A-01-B-01"
		    }
		  },
		  { header: 'Zone',      name: 'zone',     minWidth: 60, hidden: true },
		  { header: 'Rack',      name: 'rack',     minWidth: 60, hidden: true },
		  { header: 'Row',       name: 'rackRow',  minWidth: 60, hidden: true },
		  { header: 'Col',       name: 'rackCol',  minWidth: 60, hidden: true },
		  { header: '입고일',    name: 'ibDate',   minWidth: 120, 
			formatter: ({ value }) => value ? value.substring(0, 16) : ''
		  },
		  { header: '유통기한',  name: 'expirationDate', minWidth: 120, 
			formatter: ({ value }) => value ? value.substring(0, 10) : '없음'
		  },
		  { header: '상태',      name: 'ivStatus', width: 80 },
		  {
		  	header: '상세',      name: "btn", width: 100, align: "center",
		  	formatter: (cellInfo) => "<button type='button' class='btn-detail btn-primary btn-sm' data-row='${cellInfo.rowKey}' >상세</button>"
		  }
		]
	});
	// 상세보기 버튼 이벤트
	inventoryGrid.on("click", (event) => {
		if(event.columnName == "btn") {
			const target = event.nativeEvent.target;
			if (target && target.tagName === "BUTTON") {
				
				const rowData = inventoryGrid.getRow(event.rowKey);
				
				// 같은 LOT, 같은 상품(itemId)만 필터
				const sameLotList = inventoryData.filter(item =>
					item.lotNo === rowData.lotNo &&
					item.itemId === rowData.itemId &&
					// 현재 행은 제외
					item.ivId !== rowData.ivId
				);

				openDetailModal(rowData, sameLotList);
			}
		}
	});
}


// 창고 ZONE, RACK, ROW, COL 가져오기
async function getLocationInfo() {
	const response = 
		await fetch('/api/inventories/locations', {
			method: 'GET',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			}
		});
		
		console.log(response);
		if (!response.ok) {
			throw new Error('창고정보를 가져올 수 없습니다.')
		}
		return await response.json();
}

// 창고 정보의 유니크값 뽑아내기
function getUniqueValues(list, key) {
    return [...new Set(list.map(item => item[key]))]; 
}
// 셀렉트박스 채우기 함수
function fillSelect(selectEl, values) {
	selectEl.length = 1; // 첫번째 option만 남김
	
	values.forEach(v => {
		const opt = document.createElement('option');
		opt.value = v;
		opt.textContent = v;
		selectEl.appendChild(opt);
	})
}

//창고 정보 셀렉트박스에 집어넣기
function inputLocationInfo(locationInfo) {
	const zones = getUniqueValues(locationInfo, 'zone');
	const racks  = getUniqueValues(locationInfo, 'rack');
	const zoneSelect = document.getElementById('searchZone')
	const rackSelect = document.getElementById('searchRack')
	
	fillSelect(zoneSelect, zones);
	fillSelect(rackSelect, racks);
}

































// 상세검색버튼
document.getElementById('btnToggleAdvanced').addEventListener('click', function () {
    const advancedArea = document.getElementById('advancedSearch');
    const icon = this.querySelector('i');

    if (advancedArea.style.display === 'none') {
        advancedArea.style.display = 'block';
        icon.classList.replace('bx-chevron-down', 'bx-chevron-up');
    } else {
        advancedArea.style.display = 'none';
        icon.classList.replace('bx-chevron-up', 'bx-chevron-down');
    }
});