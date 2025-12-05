// qc_result.js

let qcResultGrid = null;
let qcResultModal = null;

document.addEventListener("DOMContentLoaded", () => {
	
	// 모달 초기화
	const modalEl = document.getElementById("qcResultModal");
	qcResultModal = new bootstrap.Modal(modalEl);
	
	// 그리드 초기화
	const gridEl = document.getElementById("qcResultGrid");
	
	qcResultGrid = new tui.Grid({
		el: gridEl,
		rowHeaders: ["rowNum"],
		scrollX: false,
		scrollY: false,
		columnOptions: {
			resizable: true
	    },
		pageOptions: {
			useClient: true,         
			perPage: 10            
		},
		columns: [
			{
				header: 'QC 결과ID',
				name: 'qcResultId'
			},
			{
				header: '작업지시번호',
				name: 'orderId'
			},
			{
				header: '제품코드',
				name: 'prdId'
			},
			{
				header: '제품명',
				name: 'prdName'
			},
			{
				header: '검사일자',
				name: 'inspectionDate'
			},
			{
				header: '전체판정',
				name: 'overallResult'
			},
			{
				header: '불합격사유',
				name: 'failReason'
			},
			{
			  	header: " ",
			  	name: "btn",
			 	width: 90,
			  	align: "center",
			  	formatter: () =>
			    	"<button type='button' class='btn btn-info btn-sm'>상세</button>"
			}
		]
	});
	loadQcResultGrid();
	
	qcResultGrid.on('click', (ev) => {
        if (ev.columnName !== "btn") return;

		const row = qcResultGrid.getRow(ev.rowKey);
	    if (!row || !row.orderId) return;
		
        openQcResultModal(row);
    });
});

// 목록 조회
function loadQcResultGrid() {
    fetch("/qc/result/data")
        .then(res => res.json())
        .then(data => {
            qcResultGrid.resetData(data);
        });
}

// 모달을 열면서 데이터 넣는 함수
function openQcResultModal(rowData) {

    document.getElementById("qcModalTitleOrder").innerText = rowData.orderId;
    document.getElementById("qcModalTitleProduct").innerText = rowData.prdId + " / " + rowData.prdName;

    document.getElementById("qcOrderIdText").innerText = rowData.orderId;
    document.getElementById("qcProductText").innerText = rowData.prdId + " / " + rowData.prdName;
    document.getElementById("qcPlanQtyText").innerText = rowData.planQty + " EA";

    // hidden
    document.getElementById("orderId").value = rowData.orderId;

    qcResultModal.show();
}