// 전역변수
let inventoryInfo;

document.addEventListener('DOMContentLoaded', async function () {
	inventoryInfo = await fetchInventoryData();
	console.log("@@@@@@@@@@@@@@@@", inventoryInfo)
    // 1. Warehouse Usage Chart (Donut) -> Zone Usage
    const warehouseOptions = {
        series: [44, 13, 33],
        chart: {
            width: 380,
            type: 'donut',
        },
        labels: ['A존-원자재', 'B존-부자재', 'C존-완제품'],
        responsive: [{
            breakpoint: 480,
            options: {
                chart: {
                    width: 200
                },
                legend: {
                    position: 'bottom'
                }
            }
        }],
        colors: ['#696cff', '#71dd37', '#ff3e1d']
    };

    const warehouseChart = new ApexCharts(document.querySelector("#warehouseUsageChart"), warehouseOptions);
    warehouseChart.render();


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

    const trendChart = new ApexCharts(document.querySelector("#trendChart"), trendOptions);
    trendChart.render();
});

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
			},
			body: JSON.stringify()
		});
//	console.log(response);
	if (!response.ok) {
		throw new Error('재고데이터를 가져올 수 없습니다.')
	}
	return await response.json();
} 

// 상품별 재고 정보(그룹화)
async function fetchInventoryData() {
	const response = 
		await fetch('/api/inventories/summary', {
			method: 'GET',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			},
		});
//	console.log(response);
	if (!response.ok) {
		throw new Error('재고데이터를 가져올 수 없습니다.')
	}
	return await response.json();
}