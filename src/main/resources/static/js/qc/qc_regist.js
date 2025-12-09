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
	document.getElementById("qcLotNoText").innerText = rowData.lotNo || "-";

    // hidden
	document.getElementById("qcResultId").value = rowData.qcResultId;
    document.getElementById("orderId").value = rowData.orderId;
	document.getElementById("qcPlanQty").value  = rowData.planQty;
	
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
  const trs = document.querySelectorAll("#qcDetailTbody tr");
  const detailRows = [];

  trs.forEach((tr) => {
    const dtlInput    = tr.querySelector('input[name$=".qcResultDtlId"]');
    const itemInput   = tr.querySelector('input[name$=".qcItemId"]');
    const measureInput= tr.querySelector('input[name$=".measureValue"]');
    const resultSelect= tr.querySelector('select[name$=".result"]');
    const remarkInput = tr.querySelector('input[name$=".remark"]');

    const qcResultDtlId = dtlInput    ? dtlInput.value    : null;
    const qcItemId      = itemInput   ? itemInput.value   : null;
    const measureValue  = measureInput? measureInput.value: "";
    const result        = resultSelect? resultSelect.value: "";
    const remark        = remarkInput ? remarkInput.value : "";

    detailRows.push({
      qcResultDtlId,
      qcItemId,
      measureValue,
      result,
      remark
    });
  });

  // ✅ 측정값이 비어 있는 행이 하나라도 있는지 체크
  const emptyIndex = detailRows.findIndex(row =>
    !row.measureValue || row.measureValue.trim() === ""
  );

  if (emptyIndex !== -1) {
    // 어떤 행이 비었는지 포커스까지 주기
    const trs = document.querySelectorAll("#qcDetailTbody tr");
    const targetInput = trs[emptyIndex].querySelector('input[name$=".measureValue"]');
    if (targetInput) {
      targetInput.focus();
    }
    return null;  // → onClickSaveQcResult() 에서 알럿 뜸
  }

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
  if (!detailRows) {
    alert("모든 QC 항목의 측정값을 입력해주세요.");
    return;
  }
  if (detailRows.length === 0) {
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

	// ✅ 전체 판정 필수
	if (!overallResult) {
	  alert("전체 판정을 선택해주세요.");
	  overallResultEl?.focus();
	  return;
	}

	// ✅ FAIL인데 불합격 사유가 없으면 막기
	if (overallResult === "FAIL" && failReason === "") {
	  alert("전체 판정이 FAIL인 경우, 불합격 사유를 입력해주세요.");
	  failReasonEl?.removeAttribute("readonly");
	  failReasonEl?.focus();
	  return;
	}

	// 🔹 양품/불량 수량 필수
	if (goodQtyVal === "" || defectQtyVal === "") {
	  alert("양품 수량과 불량 수량을 모두 입력해주세요.");
	  if (goodQtyVal === "") {
	    document.getElementById("qcGoodQty")?.focus();
	  } else {
	    document.getElementById("qcDefectQty")?.focus();
	  }
	  return;
	}

	const goodQty   = Number(goodQtyVal);
	const defectQty = Number(defectQtyVal);

	if (isNaN(goodQty) || isNaN(defectQty)) {
	  alert("양품/불량 수량은 숫자만 입력 가능합니다.");
	  return;
	}
	if (goodQty < 0 || defectQty < 0) {
	  alert("양품/불량 수량은 0 이상이어야 합니다.");
	  return;
	}
	
	// (선택) good + defect = planQty 체크
	const planQtyVal = document.getElementById("qcPlanQty")?.value;
	const planQty = planQtyVal ? Number(planQtyVal) : null;

	if (planQty !== null && !isNaN(planQty)) {
	  if (goodQty + defectQty !== planQty) {
	    alert("양품 수량 + 불량 수량이 지시수량과 일치하지 않습니다.");
	    // 필요하면 여기서 return 빼고 경고만 띄우게 바꿀 수도 있어
	    return;
	  }
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