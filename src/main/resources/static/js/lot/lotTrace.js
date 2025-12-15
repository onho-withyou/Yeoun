// lotTrace.js 

document.addEventListener("DOMContentLoaded", () => {
  const treeEl = document.getElementById("lotTree");
  const detailEl = document.getElementById("lotDetail");

  if (!treeEl || !detailEl) return;

  treeEl.addEventListener("click", (e) => {
    const rootLink = e.target.closest(".lot-root-link");
    const groupToggle = e.target.closest(".lot-group-toggle");

    // 1) ROOT 클릭 -> ROOT 하위 그룹(공정/자재/설비) 열기 + 오른쪽 LOT 개요
    if (rootLink) {
      e.preventDefault();

      const lotNo = rootLink.dataset.lotNo;

      // 같은 li 안의 root-children 찾기
      const li = rootLink.closest("li.list-group-item");
      const children = li.querySelector(".root-children");
      if (children) {
		
		// 1) 다른 LOT들의 children은 전부 접기
	    treeEl.querySelectorAll(".root-children").forEach(box => {
	      if (box !== children) {
	        box.classList.add("d-none");
	      }
	    });

	    // 2) 현재 클릭한 LOT만 토글 (같은 거 또 누르면 접어지게)
        children.classList.toggle("d-none");
      }

      // 오른쪽 LOT 전체 개요 (stepSeq 없이)
      loadLotDetail(lotNo, null);
      return;
    }

    // 2) 그룹(공정/자재/설비) 클릭 -> 해당 그룹 children 토글
    if (groupToggle) {
      e.preventDefault();

      const lotNo = groupToggle.dataset.lotNo;
      const group = groupToggle.dataset.group; // process / material / equipment

      const li = groupToggle.closest("li");
      const children = li.querySelector(`.group-children[data-group="${group}"]`);
      if (children) {
        children.classList.toggle("d-none");
      }
	  
	  if (!children.dataset.loaded) {
         if (group === "process") {
           loadProcessList(children, lotNo);     // 공정
         } else if (group === "material") {
           loadMaterialList(children, lotNo);    // 자재
         }
         children.dataset.loaded = "true";
       }
		   
      return;
    }
	
	// 3) 공정/자재 항목 클릭 -> 선택 표시(active) + 상세 조회
	const childLink = e.target.closest(".tree-child-link");
	if (childLink) {
	  e.preventDefault();

	  // 1) 기존 선택 해제
	  treeEl.querySelectorAll(".tree-item-active")
	    .forEach(el => el.classList.remove("tree-item-active"));

	  // 2) 현재 선택 표시
	  childLink.classList.add("tree-item-active");

	  // 3) 공정 상세
	  const orderId = childLink.dataset.orderId;
	  const stepSeq = childLink.dataset.stepSeq;
	  if (orderId && stepSeq) {
	    loadProcessDetail(orderId, stepSeq);
	    return;
	  }

	  // 4) 자재 상세
	  const outputLotNo = childLink.dataset.outputLotNo;
	  const inputLotNo  = childLink.dataset.inputLotNo;
	  if (outputLotNo && inputLotNo) {
	    // 기존 너 코드(아래 document.addEventListener 클릭) 안 쓰고 여기서 바로 호출하게
	    loadMaterialDetail(outputLotNo, inputLotNo);
	    return;
	  }
	}
  });

  function loadLotDetail(lotNo, stepSeq) {
    if (!lotNo) return;

    detailEl.innerHTML = "<div class='text-muted'>로딩 중...</div>";

    let url = `/lot/trace/detail?lotNo=${encodeURIComponent(lotNo)}`;
    if (stepSeq != null) {
      url += `&stepSeq=${encodeURIComponent(stepSeq)}`;
    }

	fetch(url)
	  .then(res => {
	    if (!res.ok) {
	      throw new Error("서버 오류: " + res.status);
	    }
	    return res.text();
	  })
	  .then(html => {
	    detailEl.innerHTML = html;
	  })
	  .catch(err => {
	    console.error(err);
	    detailEl.innerHTML =
	      "<div class='text-danger'>LOT 상세를 불러오는 중 오류가 발생했습니다.</div>";
	  });
  }
});

// 공정 리스트
function loadProcessList(container, lotNo) {

  if (!container) return;

  fetch(`/lot/trace/process-list?lotNo=${encodeURIComponent(lotNo)}`)
    .then(res => res.json())
    .then(list => {

      container.innerHTML = ""; // 기존 내용 비우기

      if (!list || list.length === 0) {
        container.innerHTML = "<li class='small text-muted'>(공정 정보 없음)</li>";
        return;
      }

      list.forEach(p => {
        const li = document.createElement("li");

        li.classList.add("process-item");
        li.dataset.lotNo = lotNo;
        li.dataset.stepSeq = p.stepSeq;
		li.dataset.orderId = p.orderId;

        li.innerHTML = `
          <a href="#" class="tree-child-link small"
		  	 data-order-id="${p.orderId}"
			 data-step-seq="${p.stepSeq}">
            ${p.processName} / ${p.processId}
            <span class="text-muted"> (${p.status})</span>
          </a>
        `;

        container.appendChild(li);
      });
    })
    .catch(err => {
      console.error(err);
      container.innerHTML =
        "<li class='small text-danger'>(공정 목록을 불러오는 중 오류가 발생했습니다)</li>";
    });
}
function loadProcessDetail(orderId, stepSeq) {
  const url = `/lot/trace/process-detail?orderId=${encodeURIComponent(orderId)}&stepSeq=${encodeURIComponent(stepSeq)}`;

  fetch(url)
    .then(res => {
      if (!res.ok) {
        throw new Error("공정 상세 조회 실패: " + res.status);
      }
      return res.json();
    })
    .then(detail => {
      renderProcessDetail(detail);
    })
    .catch(err => {
      console.error(err);
      const panel = document.getElementById("processDetailPanel");
      if (panel) {
        panel.style.display = "block";
        panel.innerHTML = `
          <div class="text-danger small">
            공정 상세 정보를 불러오는 중 오류가 발생했습니다.
          </div>`;
      }
    });
}

function renderProcessDetail(detail) {
    const panel = document.getElementById("processDetailPanel");
    panel.style.display = "block";

    document.getElementById("pd-processName").innerText = detail.processName || "-";
    document.getElementById("pd-processId").innerText = detail.processId || "-";
    document.getElementById("pd-status").innerText = detail.status || "-";
    document.getElementById("pd-deptName").innerText = detail.deptName || "-";

    document.getElementById("pd-workerId").innerText = detail.workerId || "-";
    document.getElementById("pd-workerName").innerText = detail.workerName || "-";
    document.getElementById("pd-lineId").innerText = detail.lineId || "-";

	document.getElementById("pd-startTime").innerText = formatDateTimeMin(detail.startTime);
	document.getElementById("pd-endTime").innerText = formatDateTimeMin(detail.endTime);

    document.getElementById("pd-goodQty").innerText = detail.goodQty ?? "-";
    document.getElementById("pd-defectQty").innerText = detail.defectQty ?? "-";
    document.getElementById("pd-defectRate").innerText = detail.defectRate != null
        ? detail.defectRate.toFixed(1) + "%"
        : "-";
		
	// =========================
	// 설비 목록 렌더링
	// =========================
	const eqBox = document.getElementById("pd-equipments");
	if (eqBox) {
	  eqBox.innerHTML = "";

	  const eqList = detail.equipments || [];

	  // QC 같이 설비 없는 공정
	  if (!eqList || eqList.length === 0) {
	    eqBox.innerHTML = `<div class="text-muted small">설비 정보 없음 (수기 검사)</div>`;
	  } else {
	    eqList.forEach(eq => {
	      const row = document.createElement("div");
	      row.className = "p-2 border rounded bg-white";

	      // 상태 뱃지
	      const status = eq.status || "-";
	      const badgeClass =
	        status === "가동" ? "bg-success" :
	        status === "정지" ? "bg-secondary" :
	        status === "고장" ? "bg-danger" :
	        status === "점검" ? "bg-warning text-dark" :
	        "bg-light text-dark";

	      row.innerHTML = `
	        <div class="d-flex justify-content-between align-items-center">
	          <div class="fw-semibold fs-6">${eq.equipName || "-"}</div>
	          <span class="badge ${badgeClass}">${status}</span>
	        </div>
	        <div class="small text-muted mt-1">
	          <span class="me-2">${eq.equipCode || "-"}</span>
	          <span>${eq.koName || "-"}</span>
	        </div>
	        <div class="small text-muted">
	          ${eq.stdName || ""}
	        </div>
	      `;

	      eqBox.appendChild(row);
	    });
	  }
	}

}

// 자재 리스트
function loadMaterialList(container, lotNo) {

  if (!container) return;

  fetch(`/lot/trace/material-list?lotNo=${encodeURIComponent(lotNo)}`)
    .then(res => res.json())
    .then(list => {

      container.innerHTML = ""; // 기존 내용 비우기

      if (!list || list.length === 0) {
        container.innerHTML = "<li class='small text-muted'>(자재 정보 없음)</li>";
        return;
      }

      list.forEach(m => {
        const li = document.createElement("li");

        li.classList.add("material-item");
        li.dataset.lotNo = lotNo;

        li.innerHTML = `
		<a href="#" class="tree-child-link small"
		     data-output-lot-no="${lotNo}"
		     data-input-lot-no="${m.lotNo}">
            ${m.displayName}
            <span class="text-muted"> (${m.usedQty}${m.unit ? " " + m.unit : ""})</span>
          </a>
        `;

        container.appendChild(li);
      });
    })
    .catch(err => {
      console.error(err);
      container.innerHTML =
        "<li class='small text-danger'>(자재 목록을 불러오는 중 오류가 발생했습니다)</li>";
    });
}

function renderMaterialDetail(d) {
  const panel = document.getElementById("materialDetailPanel");
  if (!panel) return;

  // 공정 패널 있으면 숨김(선택)
  const procPanel = document.getElementById("processDetailPanel");
  if (procPanel) procPanel.style.display = "none";

  // 값 채우기
  document.getElementById("md-matName").innerText = d.matName || "-";
  document.getElementById("md-matId").innerText = d.matId || "-";
  document.getElementById("md-matType").innerText = d.matType || "-";
  document.getElementById("md-matUnit").innerText = d.matUnit || "-";

  document.getElementById("md-lotNo").innerText = d.lotNo || "-";
  document.getElementById("md-lotType").innerText = d.lotType || "-";
  document.getElementById("md-lotStatus").innerText = d.lotStatus || "-";
  document.getElementById("md-lotCreatedDate").innerText = formatDateTimeSec(d.lotCreatedDate);

  document.getElementById("md-usedQty").innerText = (d.usedQty ?? "-");

  document.getElementById("md-inboundId").innerText = d.inboundId || "-";
  document.getElementById("md-inboundAmount").innerText = (d.inboundAmount ?? "-");
  document.getElementById("md-inboundLocationId").innerText = d.inboundLocationId || "-";

  document.getElementById("md-ivAmount").innerText = (d.ivAmount ?? "-");
  document.getElementById("md-ivStatus").innerText = d.ivStatus || "-";
  document.getElementById("md-inventoryLocationId").innerText = d.inventoryLocationId || "-";
  document.getElementById("md-ibDate").innerText = formatDateTimeSec(d.ibDate);

  document.getElementById("md-manufactureDate").innerText = formatDate(d.manufactureDate);
  document.getElementById("md-expirationDate").innerText = formatDate(d.expirationDate);

  document.getElementById("md-clientName").innerText = d.clientName || "-";
  document.getElementById("md-clientId").innerText = d.clientId || "-";
  document.getElementById("md-businessNo").innerText = d.businessNo || "-";
  document.getElementById("md-managerTel").innerText = d.managerTel || "-";

  // 패널 표시
  panel.style.display = "block";
}

async function loadMaterialDetail(outputLotNo, inputLotNo) {
  try {
    const res = await fetch(
      `/lot/trace/material-detail?outputLotNo=${encodeURIComponent(outputLotNo)}&inputLotNo=${encodeURIComponent(inputLotNo)}`
    );
    if (!res.ok) throw new Error("자재 상세 조회 실패");

    const detail = await res.json();
    renderMaterialDetail(detail);

  } catch (err) {
    console.error(err);
    alert("자재 상세 조회 중 오류가 발생했습니다.");
  }
}

// -------------------------------
// 날짜 포맷
// -------------------------------
function formatDateTimeSec(dt) {
  if (!dt) return "-";
  return dt.replace("T", " ").split(".")[0]; // YYYY-MM-DD HH:mm:ss
}

function formatDateTimeMin(dt) {
  if (!dt) return "-";
  const s = dt.replace("T", " ").split(".")[0];
  return s.substring(0, 16); // YYYY-MM-DD HH:mm
}

function formatDate(dt) {
  if (!dt) return "-";
  return dt.split("T")[0].split(" ")[0]; // YYYY-MM-DD
}
