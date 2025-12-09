// qc_result.js

let qcResultGrid = null;
let qcViewModal = null;

document.addEventListener("DOMContentLoaded", () => {
	
	// 모달 초기화
	const modalEl = document.getElementById("qcViewModal");
	qcViewModal = new bootstrap.Modal(modalEl);
	
	// 그리드 초기화
	const gridEl = document.getElementById("qcResultGrid");
	
	qcResultGrid = new tui.Grid({
		el: gridEl,
		rowHeaders: ["rowNum"],
		scrollX: true,
		scrollY: true,
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
	    if (!row || !row.qcResultId) return;
		
        openQcViewModal(row.qcResultId);
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
function openQcViewModal(qcResultId) {
	
	fetch(`/qc/result/${qcResultId}`)
		.then(res => res.json())
		.then(data => {
			
			document.getElementById("qcViewTitleOrder").innerText = data.orderId;
            document.getElementById("qcViewTitleProductName").innerText = data.productName || "";
            document.getElementById("qcViewTitleProductCode").innerText = data.productCode || "";

            document.getElementById("qcViewOrderIdText").innerText = data.orderId;
            document.getElementById("qcViewProductText").innerText =
				`${data.productName || ""} (${data.productCode || ""})`;
			document.getElementById("qcViewPlanQtyText").innerText =
                (data.planQty ?? "") + " EA";
            document.getElementById("qcViewLotNoText").innerText = data.lotNo || "-";

            document.getElementById("qcViewInspectionDate").value =
                data.inspectionDate || "";
            document.getElementById("qcViewInspectorName").value =
                data.inspectorName || "";

            document.getElementById("qcViewOverallResult").value =
                data.overallResult || "";
            document.getElementById("qcViewFailReason").value =
                data.failReason || "";
				
			// --- 디테일 리스트 채우기 ---
			const tbody = document.getElementById("qcViewDetailTbody");
            tbody.innerHTML = "";
			
			data.details.forEach(row => {
				const badge =
					row.result === "PASS"
                        ? "<span class='badge bg-success'>PASS</span>"
                        : row.result === "FAIL"
                            ? "<span class='badge bg-danger'>FAIL</span>"
                            : "";
				const tr = `
					<tr>
						<td>${row.itemName || ""}</td>
                        <td>${row.unit || ""}</td>
                        <td>${row.stdText || ""}</td>
                        <td>${row.measureValue || ""}</td>
                        <td>${badge}</td>
                        <td>${row.remark || ""}</td>
					</tr>
				`;
				
				tbody.insertAdjacentHTML("beforeend", tr);
			});
			
			qcViewModal.show();
		})
		.catch(() => alert("QC 결과 조회 중 오류가 발생했습니다."));
}