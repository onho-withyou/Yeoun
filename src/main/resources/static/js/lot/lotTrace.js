// lotTrace.js

document.addEventListener("DOMContentLoaded", () => {
	
	// 1. 주요 DOM 요소 캐싱
	const treeEl = document.getElementById("lotTree");		// 왼쪽 트리 전체 영역
	const detailEl = document.getElementById("lotDetail");	// 오른쪽 LOT 상세 영역
	
	// 둘 중 하나라도 없으면 실행 중단
	if (!treeEl || !detailEl) return;
	
	// 왼쪽 트리 전체 클릭 이벤트 1개로 처리
	treeEl.addEventListener("click", (e) => {
		
		// ROOT LOT 클릭 처리
		const rootLink = e.target.closest(".lot-root-link");
		
		// 그룹 토글(공정 / 자재) 클릭 처리
		const groupToggle = e.target.closest(".lot-group-toggle");
		
		// 1) ROOT LOT 클릭 -> 하위 그룹 열기/닫기 + 오른쪽 LOT 개요 로딩
		if (rootLink) {
			e.preventDefault();
			
			const lotNo = rootLink.dataset.lotNo;
			
			const li = rootLink.closest("li.list-group-item");
	     	const children = li.querySelector(".root-children");
			
			if(children) {
				// 다른 ROOT LOT들이 펼쳐져 있으면 전부 닫기
				treeEl.querySelectorAll(".root-children").forEach(box => {
					if (box !== children) {
						box.classList.add("d-none");
					}
				});
				
				// 지금 클릭한 LOT는 토글
				children.classList.toggle("d-none");
			}
			// 오른쪽 LOT 개요 화면 AJAX 로딩
			loadLotDetail(lotNo, null);
			return;
		}
		
		// 2) 공정 / 자재 클릭 -> 하위 리스트 토글 + 최초 1회만 AJAX 로딩
		if (groupToggle) {
			e.preventDefault();
			
			// 어떤 ROOT LOT 아래인지 (같은 lotNo 기준으로 공정/자재를 불러옴)
			const lotNo = groupToggle.dataset.lotNo;
			// process 또는 material 값이 들어있음 (HTML에서 data-group="process" 이런 식)
	      	const group = groupToggle.dataset.group;
			
			const li = groupToggle.closest("li");
	      	const children = li.querySelector(`.group-children[data-group="${group}"]`);
			
			// 열기/닫기 토글
			if (children) {
				children.classList.toggle("d-none");
			}
			
			if (!children.dataset.loaded) {
				if (group === "process") {
					// 공정 리스트 AJAX 로딩
					loadProcessList(children, lotNo);
				} else if (group === "material") {
					// 자재 리스트 AJAX 로딩
					loadMaterialList(children, lotNo);
				}
				// 로딩 완료 표시(문자열로 저장됨)
				children.dataset.loaded = "true";
			}
			return;
		}
		
		// 3) 공정/자재 항목 클릭 -> 선택 표시(active) + 상세 조회
		const childLink = e.target.closest(".tree-child-link");
		if (childLink) {
			e.preventDefault();
			
			// 1) 기존에 선택되어 있던 항목들 active 제거
			treeEl.querySelectorAll(".tree-item-active")
				.forEach(el => el.classList.remove("tree-item-active"));
			
			// 2) 현재 클릭한 항목에 active 클래스 부여	
			childLink.classList.add("tree-item-active");
			
			// 3) 공정 상세인지 확인
			const orderId = childLink.dataset.orderId;
		  	const stepSeq = childLink.dataset.stepSeq;
		  	if (orderId && stepSeq)	{
				loadProcessDetail(orderId, stepSeq);
				return;
			}
			
			// 4) 자재 상세인지 확인
			const outputLotNo = childLink.dataset.outputLotNo;
			const inputLotNo  = childLink.dataset.inputLotNo;
			
		  	if (outputLotNo && inputLotNo) {
				loadMaterialDetail(outputLotNo, inputLotNo);
			    return;
			}
		}
	});
	
	// LOT 개요 HTML 로딩 함수
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

// 공정 리스트 로딩
function loadProcessList(container, lotNo) {
	
	if (!container) return;
	
	fetch(`/lot/trace/process-list?lotNo=${encodeURIComponent(lotNo)}`)
		.then(res => res.json())
		.then(list => {
			
			container.innerHTML = "";
			
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

// 공정 상세 조회
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

// 공정 상세 렌더링
function renderProcessDetail(detail) {
	
	const panel = document.getElementById("processDetailPanel");
	panel.style.display = "block";

	// ---- 기본 정보 바인딩 ----
    document.getElementById("pd-processName").innerText = detail.processName || "-";
    document.getElementById("pd-processId").innerText = detail.processId || "-";
    document.getElementById("pd-status").innerText = detail.status || "-";
    document.getElementById("pd-deptName").innerText = detail.deptName || "-";

    document.getElementById("pd-workerId").innerText = detail.workerId || "-";
    document.getElementById("pd-workerName").innerText = detail.workerName || "-";
    document.getElementById("pd-lineId").innerText = detail.lineId || "-";

	// ---- 날짜 포맷 ----
	document.getElementById("pd-startTime").innerText = formatDateTimeMin(detail.startTime);
	document.getElementById("pd-endTime").innerText = formatDateTimeMin(detail.endTime);

	// ---- 수율/수량 ----
    document.getElementById("pd-goodQty").innerText = detail.goodQty ?? "-";
    document.getElementById("pd-defectQty").innerText = detail.defectQty ?? "-";
    document.getElementById("pd-defectRate").innerText = detail.defectRate != null
		? detail.defectRate.toFixed(1) + "%"
		: "-";
		
	// QC 결과 버튼 렌더링
	const qcActions = document.getElementById("pd-qcActions");
	if (qcActions) {
		qcActions.innerHTML = "";
		
		const isQc = detail.processId === "PRC-QC";
		const qcResultId = detail.qcResultId;
		
		if (isQc) {
			const btn = document.createElement("button");
			btn.type = "button";
		    btn.className = "btn btn-sm btn-outline-primary";
		    btn.innerText = "QC 결과 보기";
			
			if (!qcResultId) {
				btn.disabled = true;
		      	btn.title = "QC 결과가 아직 등록되지 않았습니다.";
			} else {
				btn.addEventListener("click", () => openQcViewModal(qcResultId));
			}
			qcActions.appendChild(btn);
		}
	}
	
	// 설비 목록 렌더링		
	const eqBox = document.getElementById("pd-equipments");
	if (eqBox) {
		eqBox.innerHTML = "";
		
		const eqList = detail.equipments || [];
		
		// 설비 없는 공정(QC 등)
		if (!eqList || eqList.length === 0) {
			eqBox.innerHTML = `<div class="text-muted small">설비 정보 없음 (수기 검사)</div>`;
		} else {
			// 설비 한 개씩 카드처럼 렌더링
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

// 자재 리스트 로딩
function loadMaterialList(container, lotNo) {
	
	if (!container) return;
	
	fetch(`/lot/trace/material-list?lotNo=${encodeURIComponent(lotNo)}`)
		.then(res => res.json())
	    .then(list => {
			container.innerHTML = "";
			
			if (!list || list.length === 0) {
				container.innerHTML = "<li class='small text-muted'>(자재 정보 없음)</li>";
				return;
			}
			
			list.forEach(m => {
				const li = document.createElement("li");
				
				li.classList.add("material-item");
		        li.dataset.lotNo = lotNo;
				
				// 자재 링크는 outputLotNo(완제품LOT) / inputLotNo(원자재LOT) 둘 다 필요
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

// 자재 상세 로딩
function renderMaterialDetail(d) {
	const panel = document.getElementById("materialDetailPanel");
	if (!panel) return;
	
	const procPanel = document.getElementById("processDetailPanel");
  	if (procPanel) procPanel.style.display = "none";
	
	// ---- 자재 마스터 정보 ----
  	document.getElementById("md-matName").innerText = d.matName || "-";
 	document.getElementById("md-matId").innerText = d.matId || "-";
  	document.getElementById("md-matType").innerText = d.matType || "-";
  	document.getElementById("md-matUnit").innerText = d.matUnit || "-";

	// ---- 자재 LOT 정보 ----
  	document.getElementById("md-lotNo").innerText = d.lotNo || "-";
  	document.getElementById("md-lotType").innerText = d.lotType || "-";
  	document.getElementById("md-lotStatus").innerText = d.lotStatus || "-";
  	document.getElementById("md-lotCreatedDate").innerText = formatDateTimeSec(d.lotCreatedDate);

	// ---- 사용량 ----
  	document.getElementById("md-usedQty").innerText = (d.usedQty ?? "-");

	// ---- 입고 관련 ----
  	document.getElementById("md-inboundId").innerText = d.inboundId || "-";
  	document.getElementById("md-inboundAmount").innerText = (d.inboundAmount ?? "-");
  	document.getElementById("md-inboundLocationId").innerText = d.inboundLocationId || "-";

	// ---- 재고 관련 ----
 	document.getElementById("md-ivAmount").innerText = (d.ivAmount ?? "-");
	document.getElementById("md-ivStatus").innerText = d.ivStatus || "-";
 	document.getElementById("md-inventoryLocationId").innerText = d.inventoryLocationId || "-";
  	document.getElementById("md-ibDate").innerText = formatDateTimeSec(d.ibDate);

	// ---- 제조/유통기한 ----
  	document.getElementById("md-manufactureDate").innerText = formatDate(d.manufactureDate);
  	document.getElementById("md-expirationDate").innerText = formatDate(d.expirationDate);

	// ---- 거래처 ----
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

// 날짜 포맷
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














