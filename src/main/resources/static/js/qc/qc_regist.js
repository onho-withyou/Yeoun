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
	
	// ✅ 전체 판정에 따라 불합격 사유 활성/비활성 (readonly 버전)
	const overallResultSelect = document.getElementById("overallResult");
	const failReasonTextarea = document.getElementById("failReason");

	function updateFailReasonState() {
	  if (!overallResultSelect || !failReasonTextarea) return;

	  const val = overallResultSelect.value;

	  if (val === "FAIL") {
	    // FAIL일 때: 입력 가능
	    failReasonTextarea.removeAttribute("readonly");
	  } else {
	    // PASS 또는 미선택: 값 지우고 읽기 전용 + 회색
	    failReasonTextarea.value = "";
	    failReasonTextarea.setAttribute("readonly", "readonly");
	  }
	}

	if (overallResultSelect) {
	  overallResultSelect.addEventListener("change", updateFailReasonState);
	  // 초기 상태도 한 번 세팅
	  updateFailReasonState();
	}
	
	// qc 등록 저장 버튼 클릭 이벤트
	const btnSave = document.getElementById("btnQcSave");
	if (btnSave) {
		btnSave.addEventListener("click", onClickSaveQcResult);
	}
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
	
	// FAIL 사유 필드 비활성화 + 회색 처리
	const overallResultSelect = document.getElementById("overallResult");
	const failReasonTextarea = document.getElementById("failReason");
	if (overallResultSelect && failReasonTextarea) {
	  overallResultSelect.value = "";
	  failReasonTextarea.value = "";
	  failReasonTextarea.setAttribute("readonly", "readonly"); // ✅ readonly
	}


	
	// 추가: 수량/비고 초기화
	const goodInput = document.getElementById("qcGoodQty");
	const defectInput = document.getElementById("qcDefectQty");
	const remarkInput = document.getElementById("qcRemark");

	if (goodInput)   goodInput.value = "";
	if (defectInput) defectInput.value = "";
	if (remarkInput) remarkInput.value = "";


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

function collectDetailRowsFromTable() {
	const tbody = document.getElementById("qcDetailTbody");
	const trs = tbody.querySelectorAll("tr");
	const detailRows = [];
	
	trs.forEach((tr) => {
		const qcResultDtlId = tr.querySelector('input[name$=".qcResultDtlId"]').value;
	    const qcItemId = tr.querySelector('input[name$=".qcItemId"]').value;
	    const measureValue = tr.querySelector('input[name$=".measureValue"]').value;
	    const result = tr.querySelector('select[name$=".result"]').value;
	    const remark = tr.querySelector('input[name$=".remark"]').value;

		detailRows.push({
			qcResultDtlId,
			qcItemId,
	        measureValue,
	        result,
	        remark
		});
	});
	
	return detailRows;
}


function onClickSaveQcResult() {
  const qcResultId = document.getElementById("qcResultId").value;
  if (!qcResultId) {
    alert("QC 결과 ID가 없습니다.");
    return;
  }

  // 1) 디테일 행 수집
  const detailRows = collectDetailRowsFromTable();
  if (!detailRows || detailRows.length === 0) {
    alert("저장할 QC 항목이 없습니다.");
    return;
  }
  
  // 2) 헤더 영역 값 읽기
    const goodQtyVal   = document.getElementById("qcGoodQty")?.value;
    const defectQtyVal = document.getElementById("qcDefectQty")?.value;
    const remark       = document.getElementById("qcRemark")?.value || "";

	const overallResultEl = document.getElementById("overallResult");
	const failReasonEl    = document.getElementById("failReason");

	const overallResult = overallResultEl ? overallResultEl.value : "";
	const failReason    = failReasonEl ? failReasonEl.value.trim() : "";

	// ✅ FAIL인데 불합격 사유가 없으면 막기
	if (overallResult === "FAIL" && failReason === "") {
	  alert("전체 판정이 FAIL인 경우, 불합격 사유를 입력해주세요.");
	  if (failReasonEl) {
	    failReasonEl.removeAttribute("readonly"); // 이미 FAIL이면 어차피 풀려있지만 혹시 몰라서
	    failReasonEl.focus();
	  }
	  return;
	}


    const goodQty   = goodQtyVal   !== "" ? Number(goodQtyVal)   : null;
    const defectQty = defectQtyVal !== "" ? Number(defectQtyVal) : null;

    if (goodQty !== null && isNaN(goodQty)) {
      alert("양품 수량이 숫자가 아닙니다.");
      return;
    }
    if (defectQty !== null && isNaN(defectQty)) {
      alert("불량 수량이 숫자가 아닙니다.");
      return;
    }

    // 3) 서버로 보낼 payload (QcSaveRequestDTO와 동일 구조)
    const payload = {
      qcResultId: Number(qcResultId),
      goodQty: goodQty,
      defectQty: defectQty,
      failReason: failReason,
      remark: remark,
      detailRows: detailRows
    };

    // 4) CSRF 토큰
    const csrfTokenMeta  = document.querySelector('meta[name="_csrf_token"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_headerName"]');

    const csrfToken     = csrfTokenMeta ? csrfTokenMeta.content : null;
    const csrfHeaderName = csrfHeaderMeta ? csrfHeaderMeta.content : null;

    // 5) fetch 호출 (⚠️ body: payload 로 변경!)
    fetch(`/qc/${qcResultId}/save`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(csrfToken && csrfHeaderName ? { [csrfHeaderName]: csrfToken } : {})
      },
      body: JSON.stringify(payload)
    })
      .then((res) => {
        if (!res.ok) {
          throw new Error("HTTP " + res.status);
        }
        return res.json();
      })
      .then((data) => {
        console.log("QC 저장 결과:", data);

        if (data.success) {
          alert(data.message || "QC 검사 결과가 저장되었습니다.");

          // 모달 닫고 목록 새로고침
          qcRegModal.hide();
          loadQcRegistGrid();
        } else {
          alert(data.message || "QC 저장 중 오류가 발생했습니다.");
        }
      })
      .catch((err) => {
        console.error("QC 저장 오류:", err);
        alert("QC 저장 중 오류가 발생했습니다.");
      });
  }