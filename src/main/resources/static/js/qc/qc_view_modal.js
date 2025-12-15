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
				header: '상태',
				name: 'overallResult',
				formatter: ({ value }) => {
				    switch (value) {
				      case "PASS":
				        return "합격";
				      case "FAIL":
				        return "불합격";
				      default:
				        return value || "-";
				    }
			    }
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

  const modalEl = document.getElementById("qcViewModal");
  if (!modalEl) {
    console.error("qcViewModal 요소를 찾을 수 없습니다.");
    return;
  }

  fetch(`/qc/result/${qcResultId}`)
    .then(res => {
      if (!res.ok) {
        console.error("QC 결과 조회 HTTP 에러:", res.status, res.statusText);
        throw new Error("HTTP " + res.status);
      }
      return res.json();
    })
    .then(data => {
      console.log("QC 결과 응답:", data);

      // ===== 타이틀 영역 =====
      const titleOrderEl       = document.getElementById("qcViewTitleOrder");
      const titleProductNameEl = document.getElementById("qcViewTitleProductName");
      const titleProductCodeEl = document.getElementById("qcViewTitleProductCode");

      if (titleOrderEl)       titleOrderEl.innerText      = data.orderId || "";
      if (titleProductNameEl) titleProductNameEl.innerText = data.productName || "";
      if (titleProductCodeEl) titleProductCodeEl.innerText = data.productCode || "";

      // ===== 기본 작업 정보 =====
      const orderTextEl   = document.getElementById("qcViewOrderIdText");
      const productTextEl = document.getElementById("qcViewProductText");
      const planQtyTextEl = document.getElementById("qcViewPlanQtyText");
      const lotNoTextEl   = document.getElementById("qcViewLotNoText");

      if (orderTextEl)   orderTextEl.innerText   = data.orderId || "";
      if (productTextEl) productTextEl.innerText =
        `${data.productName || ""} (${data.productCode || ""})`;
      if (planQtyTextEl) planQtyTextEl.innerText = (data.planQty ?? "") + " EA";
      if (lotNoTextEl)   lotNoTextEl.innerText   = data.lotNo || "-";

      // ===== 검사/수량 정보 =====
      const inspDateEl   = document.getElementById("qcViewInspectionDateText");
      const inspectorEl  = document.getElementById("qcViewInspectorNameText");
      const goodQtyEl    = document.getElementById("qcViewGoodQtyText");
      const defectQtyEl  = document.getElementById("qcViewDefectQtyText");

      if (inspDateEl)  inspDateEl.innerText  = data.inspectionDate || "";
      if (inspectorEl) inspectorEl.innerText = data.inspectorName || "";
      if (goodQtyEl)   goodQtyEl.innerText   = data.goodQty ?? "";
      if (defectQtyEl) defectQtyEl.innerText = data.defectQty ?? "";

      // ===== 전체 판정 / 사유 / 비고 =====
      const overall       = data.overallResult || "-";
      const overallBadge  = document.getElementById("qcViewOverallResultBadge");
      const failReasonEl  = document.getElementById("qcViewFailReason");
      const remarkEl      = document.getElementById("qcViewRemark");

      if (overallBadge) {
        overallBadge.textContent = overall;
        overallBadge.className = "badge w-100 py-2 fs-6 text-center";

        if (overall === "PASS") {
          overallBadge.classList.add("bg-success");
        } else if (overall === "FAIL") {
          overallBadge.classList.add("bg-danger");
        } else {
          overallBadge.classList.add("bg-secondary");
        }
      }

      if (failReasonEl) failReasonEl.value = data.failReason || "";
      if (remarkEl)     remarkEl.value     = data.remark || "";

      // 래퍼(col) 찾기 – 이제 둘 다 col-md-4임
      const failReasonWrapper = failReasonEl ? failReasonEl.closest(".col-md-4") : null;
      const remarkWrapper     = remarkEl     ? remarkEl.closest(".col-md-4")     : null;

      // PASS면 불합격 사유 숨기기
      if (failReasonWrapper) {
        failReasonWrapper.style.display = (overall === "FAIL") ? "" : "none";
      }

      // 비고 없으면 비고 영역 숨기기
      if (remarkWrapper) {
        if (data.remark && data.remark.trim() !== "") {
          remarkWrapper.style.display = "";
        } else {
          remarkWrapper.style.display = "none";
        }
      }

      // ===== 디테일 리스트 =====
      const tbody = document.getElementById("qcViewDetailTbody");
      if (tbody) {
        tbody.innerHTML = "";

        (data.details || []).forEach(row => {
          const badgeHtml =
            row.result === "PASS"
              ? "<span class='badge bg-success'>PASS</span>"
              : row.result === "FAIL"
                ? "<span class='badge bg-danger'>FAIL</span>"
                : "";

          const attachCell = "";  // 나중에 첨부파일 목록 붙일 자리

          const tr = `
            <tr>
              <td>${row.itemName || ""}</td>
              <td>${row.unit || ""}</td>
              <td>${row.stdText || ""}</td>
              <td>${row.measureValue || ""}</td>
              <td>${badgeHtml}</td>
              <td>${row.remark || ""}</td>
			  <td>
		          <div class="qc-attach-list small text-start"
		               data-dtl-id="${row.qcResultDtlId}">
		            <span class="text-muted">첨부된 파일 없음</span>
		          </div>
		      </td>
            </tr>
          `;
          tbody.insertAdjacentHTML("beforeend", tr);
		  
		  if (row.qcResultDtlId) {
		      loadQcDetailFileList(row.qcResultDtlId);
		  }
        });
      }

      qcViewModal.show();
    })
    .catch(err => {
      console.error("QC 결과 조회 오류:", err);
      alert("QC 결과 조회 중 오류가 발생했습니다.");
    });
}

// 특정 QC 상세(qcResultDtlId)의 첨부파일 목록 조회
function loadQcDetailFileList(qcResultDtlId) {
  if (!qcResultDtlId) return;

  fetch(`/qc/detail/${qcResultDtlId}/files`)
    .then(res => {
      if (!res.ok) {
        throw new Error("HTTP " + res.status);
      }
      return res.json();
    })
    .then(fileList => {
      renderQcDetailFileList(qcResultDtlId, fileList);
    })
    .catch(err => {
      console.error("QC 첨부파일 목록 조회 오류:", err);
    });
}

// 조회된 파일 목록을 결과 모달 테이블 안에 뿌려주기
function renderQcDetailFileList(qcResultDtlId, fileList) {
  const container = document.querySelector(
    `.qc-attach-list[data-dtl-id="${qcResultDtlId}"]`
  );
  if (!container) return;

  if (!fileList || fileList.length === 0) {
    container.innerHTML = `<span class="text-muted small">첨부된 파일 없음</span>`;
    return;
  }

  container.innerHTML = `<div class="small fw-semibold mb-1">첨부된 파일</div>`;

  fileList.forEach(file => {
    const fileName = file.originFileName || file.fileName || "파일";
	const iconUrl = "/img/download-icon.png";
    const downloadUrl = `/files/download/${file.fileId}`;

    const row = document.createElement("div");
    row.className = "small d-flex align-items-center gap-1";

    row.innerHTML = `
      <span class="text small">
        ${fileName}
      </span>
      <a href="${downloadUrl}" class="ms-1" title="다운로드" download>
        <img src="${iconUrl}" alt="다운로드" style="width:18px;height:18px;">
      </a>
    `;

    container.appendChild(row);
  });
}
