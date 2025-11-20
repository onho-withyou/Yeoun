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

            // 페이지 다시 로드
            location.href = `/pay/calc?yyyymm=${mm}`;
        });
    }
});

/* ================================
   AG GRID 초기 설정
================================ */

const slips = window.slipsData ?? []; // Thymeleaf에서 JSON이 주입됨

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
  defaultColDef: { resizable:true, sortable:true,   filter: true, flex: 1, minWidth:100 },
  pagination: true,
  paginationPageSize: 10,
  animateRows: true,
  rowHeight: 35,
};
 


document.addEventListener("DOMContentLoaded", () => {
  const gridDiv = document.querySelector("#payslipGrid");
  if (gridDiv) agGrid.createGrid(gridDiv, gridOptions);
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

  return `<span class="badge ${cls}">${v}</span>`;
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

  document.getElementById("statMonth").textContent = mm;

  const statusMap = {
    CONFIRMED: "확정 완료",
    CALCULATED: "계산 완료",
    SIMULATED: "가계산 완료",
    READY: "미계산"
  };

  document.getElementById("statCalc").textContent = statusMap[s.calcStatus];
  document.getElementById("statCount").textContent = `건수 ${s.count}`;
  document.getElementById("statTot").textContent = fmt(s.totalAmt);
  document.getElementById("statDed").textContent = fmt(s.dedAmt);
  document.getElementById("statNet").textContent = fmt(s.netAmt);
}

document.addEventListener("DOMContentLoaded", ()=>{
  const mm = document.getElementById("calc_month")?.value;
  if (mm) refreshStatus(mm);
});
