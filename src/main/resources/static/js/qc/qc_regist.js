// qc_regist.js

let qcRegistGrid = null;
let qcRegModal = null;

document.addEventListener("DOMContentLoaded", () => {
	
	// 모달 초기화
    const modalEl = document.getElementById("qcRegModal");
    qcRegModal = new bootstrap.Modal(modalEl);
	
	// 그리드 초기화
	const gridEl = document.getElementById("qcRegistGrid");
	
	qcRegistGrid = new tui.Grid({
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
				header: '지시수량',
				name: 'planQty'
			},
			{
				header: '상태',
				name: 'overallResult'
			},
			{
				header: '검사일',
				name: 'inspectionDate'
			},
			{
			  	header: " ",
			  	name: "btn",
			 	width: 90,
			  	align: "center",
			  	formatter: () =>
			    	"<button type='button' class='btn btn-info btn-sm'>검사등록</button>"
			}
		]
	});
	loadQcRegistGrid();
	
	qcRegistGrid.on('click', (ev) => {
        if (ev.columnName !== "btn") return;

		const row = qcRegistGrid.getRow(ev.rowKey);
	    if (!row || !row.orderId) return;
		
        openQcRegModal(row);
    });
});

// 목록 조회
function loadQcRegistGrid() {
    fetch("/qc/regist/data")
        .then(res => res.json())
        .then(data => {
            qcRegistGrid.resetData(data);
        });
}

// 모달을 열면서 데이터 넣는 함수
function openQcRegModal(rowData) {

    document.getElementById("qcModalTitleOrder").innerText = rowData.orderId;
    document.getElementById("qcModalTitleProduct").innerText = rowData.prdId + " / " + rowData.prdName;

    document.getElementById("qcOrderIdText").innerText = rowData.orderId;
    document.getElementById("qcProductText").innerText = rowData.prdId + " / " + rowData.prdName;
    document.getElementById("qcPlanQtyText").innerText = rowData.planQty + " EA";

    // hidden
    document.getElementById("orderId").value = rowData.orderId;

    qcRegModal.show();
}