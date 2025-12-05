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
				header: 'QC_RESULT_ID',
				name: 'qcResultId',
				hidden: true
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
	document.getElementById("qcModalTitleProductName").innerText = rowData.prdName;
	document.getElementById("qcModalTitleProductCode").innerText = `(${rowData.prdId})`;

    document.getElementById("qcOrderIdText").innerText = rowData.orderId;
    document.getElementById("qcProductText").innerText = rowData.prdName;
    document.getElementById("qcPlanQtyText").innerText = rowData.planQty + " EA";

    // hidden
	document.getElementById("qcResultId").value = rowData.qcResultId;
    document.getElementById("orderId").value = rowData.orderId;
	
	// 폼/테이블 초기화
	document.getElementById("qcDetailTbody").innerHTML = "";
	document.getElementById("inspectionDate").value = new Date().toISOString().substring(0, 10);
	document.getElementById("overallResult").value = "";
	document.getElementById("failReason").value = "";

	// 상세행 조회해서 tbody 채우기
	loadQcDetailRows(rowData.qcResultId);

	// 모달 열기
	qcRegModal.show();
}

function loadQcDetailRows(qcResultId) {

  fetch(`/qc/${qcResultId}/details`)
    .then((res) => {
      if (!res.ok) {
        throw new Error("HTTP " + res.status);
      }
      return res.json();
    })
    .then((data) => {
      console.log("QC 상세행:", data);
      renderQcDetailTable(data);
    })
    .catch((err) => {
      console.error("QC 상세 로딩 오류", err);
      alert("QC 상세 정보를 불러오는 중 오류가 발생했습니다.");
    });
}

function renderQcDetailTable(detailList) {
  const tbody = document.getElementById("qcDetailTbody");
  tbody.innerHTML = "";

  detailList.forEach((row, idx) => {
    const tr = document.createElement("tr");

	tr.innerHTML = `
	  <td>
	    ${row.itemName ?? ""}
	    <input type="hidden" name="details[${idx}].qcResultDtlId" value="${row.qcResultDtlId}">
	    <input type="hidden" name="details[${idx}].qcItemId" value="${row.qcItemId}">
	  </td>
	  <td>${row.unit ?? ""}</td>
	  <td>${row.stdText ?? ""}</td>
	  <td>
	    <input type="text"
	           class="form-control form-control-sm"
	           name="details[${idx}].measureValue"
	           value="${row.measureValue ?? ""}">
	  </td>
	  <td>
	    <select class="form-select form-select-sm"
	            name="details[${idx}].result">
	      <option value="PASS" ${row.result === "PASS" ? "selected" : ""}>PASS</option>
	      <option value="FAIL" ${row.result === "FAIL" ? "selected" : ""}>FAIL</option>
	    </select>
	  </td>
	  <td>
	    <input type="text"
	           class="form-control form-control-sm"
	           name="details[${idx}].remark"
	           value="${row.remark ?? ""}">
	  </td>
	`;

    tbody.appendChild(tr);
  });
}