// 전역변수
let inventoryInfo;
let inventorySafetyStockInfo;
let todayInboundData;
let chartData;

const today = new Date();

document.addEventListener('DOMContentLoaded', async function () {
	inventoryInfo = await fetchInventoryData();
//	console.log("@@@@@@@@@@@@@@@@", inventoryInfo)
	inventorySafetyStockInfo = await fetchInventorySafetyStockData();
//	console.log("@@@!@#!@#!@#!@#!@#!@", inventorySafetyStockInfo);
	todayInboundData = await fetchTodayInboundData();
//	console.log("######################", todayInboundData);
	chartData = await fetchIvHistoryData();
	console.log("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$",chartData);

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
	
    const trendChart = new ApexCharts(document.querySelector("#trendChart"), trendOptions);
    trendChart.render();
});

// ------------------------------------
// 차트옵션
// 2. Trend Chart (Bar + Line)
const trendOptions = {
    series: [{
        name: '입고',
        type: 'column',
        data: [23, 11, 22, 27, 13, 22, 37, 21, 44, 22, 30]
    }, {
        name: '출고',
        type: 'area',
        data: [44, 55, 41, 67, 22, 43, 21, 41, 56, 27, 43]
    }, {
        name: '폐기',
        type: 'line',
        data: [30, 25, 36, 30, 45, 35, 64, 52, 59, 36, 39]
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
    labels: ['01/01', '02/01', '03/01', '04/01', '05/01', '06/01', '07/01', '08/01', '09/01', '10/01', '11/01'],
    markers: {
        size: 0
    },
    xaxis: {
        type: 'datetime'
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










