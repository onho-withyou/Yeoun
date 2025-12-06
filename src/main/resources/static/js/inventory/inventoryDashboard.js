// 전역변수
let inventoryInfo; // 창고정보
let inventorySafetyStockInfo; // 안전재고 정보
let todayInboundData; // 입고정보
let chartData; // 가공한차트데이터
let viewMode = 'month'; // 차트 뷰모드
let trendChart; // 차트객체
let safetyStockGrid; // 안전재고그리드객체
let expireDisposalGrid; // 유통기한관리그리드객체

const today = new Date();



document.addEventListener('DOMContentLoaded', async function () {
	inventoryInfo = await fetchInventoryData();
	console.log("@@@@@@@@@@@@@@@@", inventoryInfo)
	inventorySafetyStockInfo = await fetchInventorySafetyStockData();
//	console.log("@@@!@#!@#!@#!@#!@#!@", inventorySafetyStockInfo);
	todayInboundData = await fetchTodayInboundData();
//	console.log("######################", todayInboundData);
	const RawChartData = await fetchIvHistoryData();
//	console.log("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$",RawChartData);
//	console.log("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$",RawChartData.data);
	chartData = normalizeIvHistory(RawChartData);
//	console.log("#############################", chartData);
	
	// 차트타입 버튼 active 설정
	document.querySelectorAll('.chart-type').forEach(btn => {
	    btn.addEventListener('click', onChartTypeClick);
	});

// -----------------------------------------------------------------------
// 상단 카드 
	// 금일 입고 예정양
	const todayInboundTotalEl = document.getElementById('todayInboundTotal');
	todayInboundTotalEl.innerHTML = todayInboundData.length;
	// 금일입고 처리완료 수 조회
	let completeCnt = 0;
	todayInboundData.forEach(inbound => {
		if(inbound.inboundStatus ==='COMPLETED') completeCnt++;
	});
	// 금일입고처리완료수 보이기 
	const todayInboundCompleteEl = document.getElementById('todayInboundComplete');
	todayInboundCompleteEl.innerHTML = `<i class='bx bx-up-arrow-alt'></i>처리 : ${completeCnt}`;
	
	
	//안전재고 미달 수량 표시
	let lowStockCnt = 0;
	inventorySafetyStockInfo.forEach(stock => {
		// 안전재고수량보다 예상재고량(재고량 - 출고예정량)이 작을경우
		if(stock.safetyStockQty > stock.expectIvQty) lowStockCnt++;
	})
	const orderEl = document.getElementById('orderCnt');
//	console.log(lowStockCnt);
	orderEl.innerHTML = lowStockCnt;
	
	const orderStatusEl = document.getElementById('orderStatus');
	if (lowStockCnt > 0) {
	    orderStatusEl.className = 'text-warning fw-semibold';
	    orderStatusEl.innerHTML = '발주 필요';
	} else {
	    orderStatusEl.className = 'text-success', 'fw-semibold'; 
	    orderStatusEl.innerHTML = '정상';
	}
	// 안전재고 미달목록표시
	renderSafetyStockGrid();
	
	//유통기한 임박 수량 표시
	let expireCnt = 0;
	let disposalCnt = 0;
	inventoryInfo.forEach(iv => {
		if(iv.ivStatus === 'DISPOSAL_WAIT') expireCnt++;
		if(iv.ivStatus === 'EXPIRED') disposalCnt++;
	})
	
	const expireEl = document.getElementById('expireCnt');
	expireEl.innerHTML = `임박 : ${expireCnt}`
	const disposalEl = document.getElementById('disposalCnt');
	disposalEl.innerHTML = `폐기 : ${disposalCnt}`
	
	// 유통기한관리 목록 표시
	renderExpireDisposalGrid();
	
// -------------------------------------------------------------------------------
// 입출고 차트 데이터 입력
	// 차트옵션설정, 차트생성
	trendChart = new ApexCharts(document.querySelector("#trendChart"), trendOptions);
	trendChart.render();
	
	const aggregated = aggregateByMode(chartData, viewMode);
	const { labels, series } = buildChartSeries(aggregated);
	
	trendChart.updateOptions({
	    labels: labels,
	    xaxis: { type: 'category' },
	    series: series
	}, false, true);
	
});

// ------------------------------------
// 차트옵션
// 2. Trend Chart (Bar + Line)
const trendOptions = {
    series: [{
        name: '입고',
        type: 'column',
		data: []
    }, {
        name: '출고',
        type: 'area',
		data: []
    }, {
        name: '폐기',
        type: 'line',
		data: []
    }],
    chart: {
        height: 350,
        type: 'line',
        stacked: false,
    },
    stroke: {
        width: [0, 2, 5],
        curve: 'smooth'
    },
    plotOptions: {
        bar: {
            columnWidth: '50%'
        }
    },
    fill: {
        opacity: [0.85, 0.25, 1],
        gradient: {
            inverseColors: false,
            shade: 'light',
            type: "vertical",
            opacityFrom: 0.85,
            opacityTo: 0.55,
            stops: [0, 100, 100, 100]
        }
    },
    markers: {
        size: 0
    },
    yaxis: {
        title: {
            text: '수량',
        },
        min: 0
    },
    tooltip: {
        shared: true,
        intersect: false,
        y: {
            formatter: function (y) {
                if (typeof y !== "undefined") {
                    return y.toFixed(0) + " 건";
                }
                return y;

            }
        }
    }
};

// -------------------------------------------------------------
// 재고내역데이터를 차트에 사용할 수있는 데이터로 가공

// 재고내역이 없는 날자 생성
function buildDateList(startDate, endDate) {
	const dates = [];
	const d = new Date(startDate);
	const end = new Date(endDate);
	// 시작일이 종료일이 될때까지 날자 입력('YYYY-MM-DD')
	while (d <= end) {
		dates.push(d.toISOString().slice(0, 10));
		d.setDate(d.getDate() + 1);
	}
	return dates;
}

// 재고내역 데이터를 없는날자에 0을넣어서 생성(rawData : 재고데이터fetchIvHistoryData결과)
function normalizeIvHistory(rawData) {
	// 워크타입 설정
	const WORK_TYPES = ['INBOUND', 'OUTBOUND', 'DISPOSE'];
	// buildDateList를 통해 오늘부터 1년전까지 날자리스트 생성
	const dateList = buildDateList(rawData.startDate, rawData.endDate);
	
	// rowData를 가공해서 맵으로 저장할 객체 생성
	const map = new Map();
	// rowData의 날자_워크타입을 키로 데이터저장
	rawData.data.forEach(row => {
	    const key = `${row.createdDate}_${row.workType}`;
	    map.set(key, row);
	});
	
	// 재고내역 데이터를 가공하여 리턴할 객체 생성
	const result = [];
	
	// 일자리스트 객체별 반복
	dateList.forEach(date => {
		// 일자별 워크타입 반복
	    WORK_TYPES.forEach(type => {
			// 키 : 날자_워크타입
	        const key = `${date}_${type}`;
			// 위에서 저장한 날자별 워크타입데이터 지정
	        const found = map.get(key);
			// 리턴객체에 데이터 저장
	        result.push({
	            createdDate: date,
	            workType: type,
	            sumCurrent: found ? found.sumCurrent : 0,
	            sumPrev: found ? found.sumPrev : 0,
	        });
	    });
	});

	return result;
}

// 뷰모드에 따른 그룹키설정
function getGroupKey(dateStr, mode) {
    const d = new Date(dateStr); // 'YYYY-MM-DD'
	//일별차트는 가공없이 리턴
    if (mode === 'day') { 
        return dateStr;
    }
	
	// 월별차트는 YYYY-MM 형태로 가공후 리턴
    if (mode === 'month') { 
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        return `${y}-${m}`;
    }
	
	// 주별차트는 YY MMdd-MMdd로 설정
	if (mode === 'week') {
	    const base = new Date(dateStr);
	    const day = base.getDay(); // 0(일)~6(토)

	    // 월요일 시작 기준
	    const diffToMonday = (day === 0 ? -6 : 1 - day);
	    // 입력받은 날짜의 주 시작일 설정
		const weekStart = new Date(base);
	    weekStart.setDate(base.getDate() + diffToMonday);
		// 입력받은 날짜의 주 마지막일 설정
	    const weekEnd = new Date(weekStart);
	    weekEnd.setDate(weekStart.getDate() + 6);
		
		// 리턴할 정보로 가공
	    const y2 = String(weekStart.getFullYear()).slice(2); // '25'
		
	    const startMonth = String(weekStart.getMonth() + 1).padStart(2, '0');
	    const startDay   = String(weekStart.getDate()).padStart(2, '0');

	    const endMonth = String(weekEnd.getMonth() + 1).padStart(2, '0');
	    const endDay   = String(weekEnd.getDate()).padStart(2, '0');

	    return `${y2} ${startMonth}${startDay}-${endMonth}${endDay}`;
	}
}

// 재집계 함수
function aggregateByMode(chartData, mode) {
    const groups = new Map(); // key: groupKey + '_' + workType

    chartData.forEach(row => {
		// chartData의 각 날을 차트모드에 맞게 변형후 그룹키로 설정
        const groupKey = getGroupKey(row.createdDate, mode);
		// 각 날자별 입고,출고,폐기 구분하여 키 설정
        const key = `${groupKey}_${row.workType}`;
		// groups에 해당날자의 입고,출고,폐기 키가 존재하지않으면 키설정
        if (!groups.has(key)) {
            groups.set(key, {
                groupKey, // 날자
                workType: row.workType, // 입고,출고,폐기
                sumCurrent: 0, // 현재수량합계 = 0으로 설정
                sumPrev: 0 // 이전수량 합계 = 0으로 설정
            });
        }
		// 0으로 설정했던 수량합계에 값 넣어주기
        const g = groups.get(key);
        g.sumCurrent += row.sumCurrent;
        g.sumPrev    += row.sumPrev;
    });

    return Array.from(groups.values());
}

// 재집계함수를 토대로 차트에 넣어줄 데이터 생성함수
// aggregatedData : aggregateByMode(chartData, mode)
function buildChartSeries(aggregatedData) {
	// 라벨설정 : 재집계함수의 groupKey = mode별로 가공된 날짜데이터
    const labels = [...new Set(aggregatedData.map(row => row.groupKey))];
	// 날짜데이터 정렬
    labels.sort();
	
	// 재집계함수를 필요한 데이터로 가공하기 위한 맵 객체
    const map = new Map();
	
    aggregatedData.forEach(row => {
		// 키설정 : 날짜_워크타입
        const key = `${row.groupKey}_${row.workType}`;
		// 입고,출고,폐기 수량을 저장할 value
        let value = 0;
		
        if (row.workType === 'INBOUND') {
			// 입고의경우 현재수량 - 이전수량
            value = row.sumCurrent - row.sumPrev;
        } else if (row.workType === 'OUTBOUND' || row.workType === 'DISPOSE') {
			// 출고, 폐기의경우 이전수량 - 현재수량 
            value = row.sumPrev - row.sumCurrent;
        }
        map.set(key, value);
    });
	// 차트에 입력할 입고,출고,폐기 데이터리스트 객체
    const inboundData = [];
    const outboundData = [];
    const disposeData = [];
	
	// 각 날짜에 해당 입고,출고,폐기 데이터를 입력 
    labels.forEach(key => {
        inboundData.push(map.get(`${key}_INBOUND`)  ?? 0);
        outboundData.push(map.get(`${key}_OUTBOUND`) ?? 0);
        disposeData.push(map.get(`${key}_DISPOSE`)  ?? 0);
    });
	
	// 차트에 필요한 정보(그래프 이름, 타입, 데이터) series에 저장
    const series = [
        { name: '입고', type: 'column', data: inboundData },
        { name: '출고', type: 'area',   data: outboundData },
        { name: '폐기', type: 'line',   data: disposeData }
    ];

    return { labels, series };
}

// ------------------------------------------------------------------
// 차트 타입 변경 함수
function onChartTypeClick(event) {
	// 선택된 버튼
    const clicked = event.currentTarget;
    const mode = clicked.id === 'btnMonth'
        ? 'month'
        : clicked.id === 'btnWeek'
        ? 'week'
        : 'day';

    viewMode = mode;

    //모든 chart-type 버튼에서 active 제거
    document.querySelectorAll('.chart-type').forEach(btn => {
        btn.classList.remove('active');
    });

    //클릭된 버튼에만 active 추가
    clicked.classList.add('active');
	
	// 재고내역 데이터를 비어있는 날자를 채워서 재가공
	const aggregated = aggregateByMode(chartData, viewMode);
	// 차트를 그리기위한 날짜,옵션,데이터 설정
	const { labels, series } = buildChartSeries(aggregated);
	
	// 차트 옵션,데이터 업데이트
	trendChart.updateOptions({
	    labels: labels,
	    xaxis: { type: 'category' },
	    series: series
	}, false, true);
}

// ------------------------------------------------------------------
// 안전재고 그리드 함수

// 안전재고미달데이터 필터
function getLowStockRows() {
    return inventorySafetyStockInfo.filter(stock => {
        // 예상재고량 = ivQty - planOutQty (지금 구조 기준)
        const expectIvQty = stock.ivQty - stock.planOutQty;
        return stock.safetyStockQty > expectIvQty;
    });
}

// 안전재고 미달목록 그리드 그리기
function renderSafetyStockGrid() {
	// 안전재고 미달 데이터 
    const lowStocks = getLowStockRows();
	// 그리드 보이는 카드
    const cardEl = document.getElementById('safetyStockCard');

    if (lowStocks.length === 0) {
        // 미달 없으면 카드 숨김
        cardEl.style.display = 'none';
        return;
    }

    // 미달 있으면 카드 표시
    cardEl.style.display = 'block';

    const gridEl = document.getElementById('safetyStockGrid');
	const Grid = tui.Grid;
    // 기존 그리드가 있으면 destroy 후 다시 생성
    if (safetyStockGrid) {
        safetyStockGrid.destroy();
    }
	
	gridLangSet(Grid)
	
	// 안전재고 그리드
    safetyStockGrid = new Grid({
        el: gridEl,
//        scrollX: false,
//        scrollY: true,
        bodyHeight: 220,
        rowHeaders: ['rowNum'],
		pageOptions: {
		    useClient: true, 
		    perPage: 5 
		},
        columns: [
            { header: '품목명',   name: 'itemName', minWidth: 160 },
//            { header: '품목코드', name: 'itemId', width: 110, align: 'center' },
            { header: '유형',     name: 'itemType', width: 80,  align: 'center' },
            { header: '현재고',   name: 'ivQty', width: 90, align: 'right', 
			  formatter: ({value}) => value.toLocaleString() },
            { header: '출고예정', name: 'planOutQty', width: 90, align: 'right', 
			  formatter: ({value}) => value.toLocaleString() },
            { header: '출고후재고', name: 'expectIvQty', width: 90, align: 'right', 
              formatter: ({row}) => (row.ivQty - row.planOutQty).toLocaleString() },
            { header: '안전재고', name: 'safetyStockQty', width: 100, align: 'right', 
			  formatter: ({value}) => value.toLocaleString() },
            { header: '발주필요', name: 'safetyStockQtyDaily', width: 90, align: 'right', 
			  formatter: ({row}) => row.safetyStockQty - row.expectIvQty},
			{ header: '상세',      name: "btn", width: 100, align: "center",
			  formatter: (cellInfo) => "<button type='button' class='btn-detail btn-primary btn-sm' data-row='${cellInfo.rowKey}' >발주</button>"
			}
        ],
        data: lowStocks
    });
	
	// 발주 버튼 동작
	safetyStockGrid	.on("click", (event) => {
		if(event.columnName == "btn") {
			const target = event.nativeEvent.target;
			if (target && target.tagName === "BUTTON") {
				
				const rowData = safetyStockGrid.getRow(event.rowKey);
				console.log(rowData);
			}
		}
	});
}
// ----------------------------------------------------------------------
// 유통기한 목록 리스트
// 임박, 폐기 재고 데이터필터함수
function getExpireAndDisposalRows() {
    return inventoryInfo.filter(iv =>  iv.ivStatus === 'DISPOSAL_WAIT' || iv.ivStatus === 'EXPIRED');
}

function renderExpireDisposalGrid() {
    // 임박 + 폐기 같이 필터
    const rows = getExpireAndDisposalRows();

    const cardEl = document.getElementById('expireDisposalCard');
    const gridEl = document.getElementById('expireDisposalGrid');
	
	const Grid = tui.Grid;
	gridLangSet(Grid);
	
    if (rows.length === 0) {
        cardEl.style.display = 'none';
        return;
    }

    cardEl.style.display = 'block';

    if (expireDisposalGrid) {
        expireDisposalGrid.destroy();
    }

    expireDisposalGrid = new Grid({
        el: gridEl,
        scrollX: false,
        scrollY: true,
        bodyHeight: 220,
        rowHeaders: ['rowNum'],
		pageOptions: {
		    useClient: true,
		    perPage: 5
		},
        columns: [
//            { header: '품목코드', name: 'itemId', width: 110, align: 'center' },
            { header: '품목명',   name: 'prodName', minWidth: 160 },
            { header: '유형',     name: 'itemType', width: 80,  align: 'center',
			  formatter: ({ value }) => {
				switch (value) {
					case 'RAW':  return '원자재';
					case 'SUB':  return '부자재';
					case 'FG':   return '상품';
					case 'PKG':  return '포장재';
					default:     return value ?? '';
				}
			  }
			},
            { header: 'LOT번호',  name: 'lotNo', minWidth: 220 },
            { header: '재고수량', name: 'ivAmount', width: 90, align: 'right',
              formatter: ({ value }) => value.toLocaleString() },
            { header: '출고예정', name: 'expectObAmount', width: 90, align: 'right',
              formatter: ({ value }) => (value ?? 0).toLocaleString() },
            { header: '유통기한', name: 'expirationDate', minWidth: 140, align: 'center',
			  formatter: ({ value }) => {
				if (!value) return '';
				    const date = new Date(value);
				    const year = date.getFullYear();
				    const month = String(date.getMonth() + 1).padStart(2, '0');
				    const day = String(date.getDate()).padStart(2, '0');
				    
				    return `${year}-${month}-${day}`;
			  }
			},
            { header: '상태',     name: 'ivStatus', width: 90, align: 'center',
			  formatter: ({ value }) => {
				switch(value) {
					case 'NORMAL'          : return '정상';
					case 'EXPIRED'         : return '만료';
					case 'DISPOSAL_WAIT': return '임박';
					default:     return value ?? '';
			    }
			  }
			},
			{
				header: '상세',      name: "btn", width: 100, align: "center",
				formatter: (cellInfo) => "<button type='button' class='btn-detail btn-primary btn-sm' data-row='${cellInfo.rowKey}' >상세</button>"
			}
        ],
        data: rows
    });
	
	expireDisposalGrid.on("click", (event) => {
		if(event.columnName == "btn") {
			const target = event.nativeEvent.target;
			if (target && target.tagName === "BUTTON") {
				
				const rowData = expireDisposalGrid.getRow(event.rowKey);
				console.log(rowData);
				// 같은 LOT, 같은 상품(itemId)만 필터
				const sameLotList = inventoryInfo.filter(item =>
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


// ----------------------------------------------------------------------
// 데이터 정보 가져오기

// 재고정보 가져오기
async function fetchInventoryData() {
	const response = 
		await fetch('/api/inventories', {
			method: 'POST',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			}
		});
//	console.log(response);
	if (!response.ok) {
		throw new Error('재고데이터를 가져올 수 없습니다.')
	}
	return await response.json();
} 

// 안전재고/재고 비교 정보 데이터
async function fetchInventorySafetyStockData() {
	const response = 
		await fetch('/api/inventories/inventorySafetyStockCheckInfo', {
			method: 'GET',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			}
		});
//	console.log(response);
	if (!response.ok) {
		throw new Error('재고와 안전재고 비교 데이터를 가져올 수 없습니다.')
	}
	return await response.json();
}

// 오늘 입고정보 조회함수
async function fetchTodayInboundData() {
	
	const startDate = today.toISOString().slice(0, 10);
	const endDate = today.toISOString().slice(0, 10);
	 
	const MATERIAL_INBOUND_LIST = 
		`/inventory/inbound/materialList/data` +
		`?startDate=${startDate}` +
		`&endDate=${endDate}` + 
		`&searchType=` +
		`&keyword=`
			
	const response = 
		await fetch(MATERIAL_INBOUND_LIST, {
			method: 'GET',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			}
		});

	if (!response.ok) {
		throw new Error('입고 정보를 조회할 수 없습니다.')
	}
	
	return await response.json();
}

// 재고내역 조회
async function fetchIvHistoryData() {
	const response = await fetch('/api/inventories/ivHistoryGroup', {
		method: 'GET',
		headers: {
			[csrfHeader]: csrfToken,
			'Content-Type': 'application/json'
		}
	});
	
	if (!response.ok) {
		throw new Error('입출고 추이 데이터를 조회할 수 없습니다.')
	}
	
	return await response.json();
}


// 그리드 랭기지 설정
function gridLangSet(grid) {
	// 1. 그리드 한글 언어셋 설정 (필터 및 각종 텍스트 한글화)
	grid.setLanguage('ko', {
	    display: {
	        noData: '데이터가 없습니다.',
	        loadingData: '데이터를 불러오는 중입니다.',
	        resizeHandleGuide: '마우스 드래그를 통해 너비를 조정할 수 있습니다.',
	    },
	    net: {
	        confirmCreate: '생성하시겠습니까?',
	        confirmUpdate: '수정하시겠습니까?',
	        confirmDelete: '삭제하시겠습니까?',
	        confirmModify: '저장하시겠습니까?',
	        noDataToCreate: '생성할 데이터가 없습니다.',
	        noDataToUpdate: '수정할 데이터가 없습니다.',
	        noDataToDelete: '삭제할 데이터가 없습니다.',
	        noDataToModify: '수정할 데이터가 없습니다.',
	        failResponse: '데이터 요청 중에 에러가 발생하였습니다.'
	    },
	    filter: {
	        // 문자열 필터 옵션
	        contains: '포함',
	        eq: '일치',
	        ne: '불일치',
	        start: '시작 문자',
	        end: '끝 문자',
	        
	        // 날짜/숫자 필터 옵션
	        after: '이후',
	        afterEq: '이후 (포함)',
	        before: '이전',
	        beforeEq: '이전 (포함)',

	        // 버튼 및 기타
	        apply: '적용',
	        clear: '초기화',
	        selectAll: '전체 선택'
	    }
	});
}







