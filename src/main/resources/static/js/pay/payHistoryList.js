// ===============================
// 급여 이력 조회 JS (AG Grid)
// ===============================

let gridApi = null;
let currentPage = 0;
const pageSize = 20;

// ===============================
// DOM 준비되면 초기 실행
// ===============================
document.addEventListener("DOMContentLoaded", () => {

    initGrid();
    initEvents();
    loadPayHistory(0);
});


// ===============================
// AG Grid 초기화
// ===============================
function initGrid() {

	const columnDefs = [
	    { 
	        headerName: "지급월", 
	        field: "payYymm",
	        width: 120,
	        valueFormatter: p => formatYymm(p.value),
	        cellStyle: { textAlign: "center", fontWeight: "600" }
	    },
	    { headerName: "사번", field: "empId", width: 120 },
	    { headerName: "이름", field: "empName", width: 140 },
	    { headerName: "부서", field: "deptName", width: 140 },
        {
            headerName: "총지급액",
            field: "totAmt",
            width: 140,
            valueFormatter: p => numberFormat(p.value),
            cellStyle: { textAlign: "right" }
        },
		
		{
			            headerName: "기본급",
			            field: "baseAmt",
			            width: 140,
			            valueFormatter: p => numberFormat(p.value),
			            cellStyle: { textAlign: "right" }
		        },
				{
						            headerName: "수당합계",
						            field: "alwAmt",
						            width: 140,
						            valueFormatter: p => numberFormat(p.value),
						            cellStyle: { textAlign: "right" }
						        },
								{
										            headerName: "공제액",
										            field: "dedAmt",
										            width: 140,
										            valueFormatter: p => numberFormat(p.value),
										            cellStyle: { textAlign: "right" }
										        },
        {
            headerName: "실수령액",
            field: "netAmt",
            width: 140,
            valueFormatter: p => numberFormat(p.value),
            cellStyle: { textAlign: "right", color: "#0d6efd", fontWeight: "600" }
        },
        {
            headerName: "상태",
            field: "calcStatus",
            width: 120,
            cellRenderer: p => statusBadge(p.value),
            cellStyle: { textAlign: "center" }
        }
    ];

	const gridOptions = {
	    columnDefs,
	    rowData: [],
	    pagination: true,              // ← ★ 페이지네이션 활성화
	    paginationPageSize: 20,        // ← ★ 1페이지당 20줄
	    paginationPageSizeSelector: [10, 20, 50, 100],  // 선택 변경 가능 (옵션)
	};


    const gridDiv = document.querySelector("#payslipGrid");
    if (!gridDiv) {
        console.error("❌ #payslipGrid 요소를 찾을 수 없습니다.");
        return;
    }

    gridApi = agGrid.createGrid(gridDiv, gridOptions);
}



// ===============================
// 이벤트 설정
// ===============================
function initEvents() {

    const btnSearch = document.getElementById("btnSearch");
    if (btnSearch) {
        btnSearch.addEventListener("click", () => loadPayHistory(0));
    }

    const btnReset = document.getElementById("btnReset");
    if (btnReset) {
        btnReset.addEventListener("click", () => {
            const ids = ["keyword", "deptName", "year", "month"];
            ids.forEach(id => {
                const el = document.getElementById(id);
                if (el) el.value = "";
            });
            loadPayHistory(0);
        });
    }

    const modeSelect = document.getElementById("mode");
    const deptName = document.getElementById("deptName");

    if (modeSelect && deptName) {
        modeSelect.addEventListener("change", (e) => {
            deptName.style.display = e.target.value === "dept" ? "block" : "none";
        });
    }
}



// ===============================
// 급여 이력 조회 API
// ===============================
function loadPayHistory(page) {

    currentPage = page;

	const params = new URLSearchParams({
	    page,
	    size: pageSize,
	    mode: document.getElementById("mode")?.value ?? "",
	    keyword: document.getElementById("keyword")?.value ?? "",
	    empName: document.getElementById("keyword")?.value ?? "",
	    deptName: document.getElementById("deptName")?.value ?? "",
	    year: document.getElementById("year")?.value ?? "",
	    month: document.getElementById("month")?.value ?? ""
	});


    fetch('/api/pay/history?' + params.toString())
        .then(res => res.json())
        .then(data => {
            if (!gridApi) {
                console.error("❌ Grid API가 없습니다. initGrid() 확인 필요");
                return;
            }
            // 🔥 AG Grid v31 방식
            gridApi.setGridOption('rowData', data);
           
        })
        .catch(err => {
            console.error("급여 이력 조회 오류:", err);
            alert("급여 이력 조회에 실패했습니다.");
        });
}


// ===============================
// 유틸 함수
// ===============================
function numberFormat(x) {
    if (!x) return "";
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function statusBadge(s) {
    if (!s) return "";
    return `<span class="badge bg-info">${s}</span>`;
}

// ===============================
// 지급월 포맷 (YYYYMM → YYYY-MM)
// ===============================
function formatYymm(yymm) {
    if (!yymm || yymm.length !== 6) return yymm;
    const yyyy = yymm.substring(0, 4);
    const mm = yymm.substring(4, 6);
    return `${yyyy}-${mm}`;
}

