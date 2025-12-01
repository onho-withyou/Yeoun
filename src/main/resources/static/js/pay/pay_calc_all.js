/* ============================
   월 선택 시 페이지와 hidden input 업데이트
============================ */
document.addEventListener("DOMContentLoaded", () => {
    const selectMonth = document.getElementById("calc_month");
    const hiddenSim = document.getElementById("calcMonth");
    const hiddenConfirm = document.getElementById("calcMonthConfirm");

    if (selectMonth) {
        selectMonth.addEventListener("change", (e) => {
            const mm = e.target.value;

            // 서버로 전달하는 hidden 값 갱신
            if (hiddenSim) hiddenSim.value = mm;
            if (hiddenConfirm) hiddenConfirm.value = mm;

            // 🔥 스피너 표시
            showLoader();

            // 🔥 렌더링 시간 확보 후 페이지 이동
            setTimeout(() => {
                location.href = `/pay/calc?yyyymm=${mm}`;
            }, 50);
        });
    }

    // ✅ 페이지 로드 완료 후 스피너 숨김
    hideLoader();
});


/* ================================
   AG GRID 초기 설정
================================ */

const slips = window.slipsData ?? [];
let gridApi = null; // ✅ Grid API 전역 변수로 관리

const columnDefs = [
     { headerName: "사번", field: "empId", sortable: true, filter: true, width: 120 },
     { headerName: "이름", field: "empName", sortable: true, filter: true, width: 140 },
     { headerName: "부서", field: "deptName", sortable: true, filter: true, width: 160 },

  { headerName: "기본급", field: "baseAmt", width: 120, 
    valueFormatter: p => numberWithCommas(p.value),
    cellClass: 'text-end'
  },
  { headerName: "수당합계", field: "alwAmt", width: 120,
    valueFormatter: p => numberWithCommas(p.value),
    cellClass: 'text-end'
  },
  { headerName: "총지급", field: "totAmt", width: 120,
    valueFormatter: p => numberWithCommas(p.value),
    cellClass: 'text-end'
  },
  { headerName: "공제", field: "dedAmt", width: 120,
    valueFormatter: p => numberWithCommas(p.value),
    cellClass: 'text-end'
  },
  { headerName: "실수령", field: "netAmt", width: 140,
    valueFormatter: p => numberWithCommas(p.value),
    cellStyle: { color:"#0d6efd", fontWeight:"600", textAlign:"right" }
  },

  {
    headerName: "상태",
    field: "calcStatus",
    width: 120,
    cellRenderer: (p)=>renderStatusBadge(p.value),
    cellStyle: { textAlign:"center" }
  },

  {
    headerName: "상세",
    width: 120,
    cellRenderer: (p)=>`
      <button class="btn btn-sm btn-outline-primary"
              onclick="openDetail('${p.data.empId}')">상세보기</button>
    `,
    cellStyle: { textAlign:"center" }
  }
];

// 🔹 Grid 옵션 
const gridOptions = {
  columnDefs,
  rowData: slipsData,
  defaultColDef: { resizable:true, sortable:true, filter: true, flex: 1, minWidth:100 },
  pagination: true,
  paginationPageSize: 10,
  paginationPageSizeSelector: [10, 20, 50, 100],
  animateRows: true,
  rowHeight: 35,
  onGridReady: (params) => {
    gridApi = params.api; // ✅ API 저장
    console.log("✅ AG Grid 초기화 완료");
  }
};

document.addEventListener("DOMContentLoaded", () => {
  const gridDiv = document.querySelector("#payslipGrid");
  if (gridDiv) {
    agGrid.createGrid(gridDiv, gridOptions);
  }
});


/* ================================
   공통 함수
================================ */
function numberWithCommas(x) {
  if (!x) return "0";
  return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function renderStatusBadge(v) {
  const cls = {
    CONFIRMED: 'bg-success',
    CALCULATED: 'bg-info text-dark',
    SIMULATED: 'bg-warning text-dark',
    READY: 'bg-secondary'
  }[v] ?? 'bg-secondary';

  const label = {
    CONFIRMED: "확정",
    CALCULATED: "계산완료",
    SIMULATED: "가계산",
    READY: "미계산"
  }[v] ?? v;

  return `<span class="badge ${cls}">${label}</span>`;
}


/* ================================
   상세 조회 모달
================================ */
async function openDetail(empId) {
  const mm = document.getElementById("calc_month").value;

  const res = await fetch(`/pay/calc/detail?yyyymm=${mm}&empId=${empId}`);
  const data = await res.json();

  // 기본정보
  document.getElementById("d-empId").innerText = data.empId;
  document.getElementById("d-empName").innerText = data.empName;
  document.getElementById("d-deptName").innerText = data.deptName;

  document.getElementById("d-baseAmt").innerText = numberWithCommas(data.baseAmt) + " 원";
  document.getElementById("d-alwAmt").innerText  = numberWithCommas(data.alwAmt) + " 원";
  document.getElementById("d-dedAmt").innerText  = numberWithCommas(data.dedAmt) + " 원";
  document.getElementById("d-netAmt").innerText  = numberWithCommas(data.netAmt) + " 원";

  // 지급/공제 리스트
  renderItemTable("payItemsBody", data.payItems);
  renderItemTable("dedItemsBody", data.dedItems);

  new bootstrap.Modal(document.getElementById("detailModal")).show();
}

function renderItemTable(target, list) {
  const el = document.getElementById(target);
  el.innerHTML = "";

  (list ?? []).forEach(it => {
    el.innerHTML += `
      <tr>
        <td>${it.itemName}</td>
        <td class="text-end">${numberWithCommas(it.amount)}</td>
      </tr>
    `;
  });
}

/* ================================
   상태 갱신 AJAX
================================ */
async function refreshStatus(mm) {
  const res = await fetch(`/pay/calc/status?yyyymm=${mm}`);
  if (!res.ok) return;

  const s = await res.json();
  const fmt = n => (n ?? 0).toLocaleString();

  const elMonth = document.getElementById("statMonth");
  if (elMonth) elMonth.textContent = mm;

  const statusMap = {
    CONFIRMED: "확정 완료",
    CALCULATED: "계산 완료",
    SIMULATED: "가계산 완료",
    READY: "미계산"
  };

  const elCalc = document.getElementById("statCalc");
  if (elCalc) elCalc.textContent = statusMap[s.calcStatus];

  const elCount = document.getElementById("statCount");
  if (elCount) elCount.textContent = `건수 ${s.totalCount}`;

  const elTot = document.getElementById("statTot");
  if (elTot) elTot.textContent = fmt(s.totAmt);

  const elDed = document.getElementById("statDed");
  if (elDed) elDed.textContent = fmt(s.dedAmt);

  const elNet = document.getElementById("statNet");
  if (elNet) elNet.textContent = fmt(s.netAmt);
}


/*전체 가계산*/
document.getElementById("btnSimulateAll")?.addEventListener("click", () => {
    const yyyymm = document.getElementById("calc_month").value;
    if (!yyyymm) return alert("월을 선택하세요.");

    showLoader();

    fetch(`/pay/calc/simulateJson`, {
        method: "POST",
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            [csrfHeader]: csrfToken
        },
        body: new URLSearchParams({ yyyymm })
    })
    .then(res => res.json())
    .then(r => {
        if (!r.success) {
            hideLoader();
            alert("❌ 오류: " + r.message);
            return;
        }

        refreshStatus(yyyymm);
        loadGridData(yyyymm);
    })
    .catch(err => {
        console.error("가계산 오류:", err);
        hideLoader();
        alert("❌ 서버 오류가 발생했습니다.");
    });
});


/*전체 확정*/
document.getElementById("btnConfirmAll")?.addEventListener("click", () => {
    const yyyymm = document.getElementById("calc_month").value;
    if (!yyyymm) return alert("월을 선택하세요.");

    if (!confirm("해당 월 급여를 전체 확정하시겠습니까?")) return;

    showLoader();

    fetch(`/pay/calc/confirmJson`, {
        method: "POST",
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            [csrfHeader]: csrfToken
        },
        body: new URLSearchParams({ yyyymm })
    })
    .then(res => res.json())
    .then(r => {
        if (!r.success) {
            hideLoader();
            alert("❌ 오류: " + r.message);
            return;
        }

        refreshStatus(yyyymm);
        loadGridData(yyyymm);
    })
    .catch(err => {
        console.error("확정 오류:", err);
        hideLoader();
        alert("❌ 서버 오류가 발생했습니다.");
    });
});


/* AG-Grid 데이터를 다시 로드 */
/* AG-Grid 데이터를 다시 로드 */
function loadGridData(yyyymm) {
    fetch(`/pay/calc/list?yyyymm=${yyyymm}`)
        .then(res => res.json())
        .then(list => {
            if (gridApi) {
                // ✅ v31+ 호환 방식으로 데이터 업데이트
                try {
                    // v31 이상
                    gridApi.setGridOption('rowData', list);
                } catch (e) {
                    // v30 이하 폴백
                    gridApi.setRowData(list);
                }
                hideLoader();
                console.log("✅ Grid 데이터 업데이트 완료");
            } else {
                // ✅ API 준비 대기 (최대 5초)
                let attempts = 0;
                const maxAttempts = 100; // 5초 (50ms * 100)

                const waitApi = setInterval(() => {
                    attempts++;

                    if (gridApi) {
                        try {
                            gridApi.setGridOption('rowData', list);
                        } catch (e) {
                            gridApi.setRowData(list);
                        }
                        clearInterval(waitApi);
                        hideLoader();
                        console.log("✅ Grid 데이터 업데이트 완료");
                    } else if (attempts >= maxAttempts) {
                        // ✅ 타임아웃 처리
                        clearInterval(waitApi);
                        hideLoader();
                        console.error("❌ Grid API 초기화 실패 - 타임아웃");
                        alert("화면 로딩 중 문제가 발생했습니다. 페이지를 새로고침해주세요.");
                    }
                }, 50);
            }
        })
        .catch(err => {
            console.error("데이터 로드 오류:", err);
            hideLoader();
            alert("❌ 데이터를 불러오는데 실패했습니다.");
        });
}
/* 로딩스피너 */
function showLoader() {
    const loader = document.getElementById("loading-overlay");
    if (loader) {
        loader.classList.remove("d-none");
        console.log("🔄 로딩 시작");
    }
}

function hideLoader() {
    const loader = document.getElementById("loading-overlay");
    if (loader) {
        loader.classList.add("d-none");
        console.log("✅ 로딩 완료");
    }
}