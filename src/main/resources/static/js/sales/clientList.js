/**
 * clientList.js (AG Grid v31+ 버전)
 */

let gridApi = null;

document.addEventListener("DOMContentLoaded", () => {

    /* 메시지 출력 */
    const holder = document.getElementById("clientMsgHolder");
    if (holder && holder.dataset.msg) {
        alert(holder.dataset.msg);
    }

    /* 검색 버튼 */
    document.getElementById("btnSearch")?.addEventListener("click", loadClientList);

    /* 엔터 검색 */
    document.getElementById("keyword")?.addEventListener("keydown", e => {
        if (e.key === "Enter") {
            e.preventDefault();
            loadClientList();
        }
    });

    /* 그리드 생성 */
    initClientGrid();

    /* 초기 데이터 로딩 */
    if (gridApi) {
        gridApi.setGridOption("rowData", initialClientList);
    }
});

/* ==========================================================
   1. AG Grid 최신버전 초기화 (v31+)
========================================================== */

function initClientGrid() {

    const columnDefs = [
        { headerName: "유형", field: "clientType", width: 110 },
        { headerName: "코드", field: "clientId", width: 130 },
        { headerName: "거래처명", field: "clientName", flex: 1 },
        { headerName: "사업자번호", field: "businessNo", width: 150 },
        { headerName: "대표자명", field: "ceoName", width: 140 },
        { headerName: "담당자", field: "managerName", width: 140 },
        { headerName: "연락처", field: "managerTel", width: 150 },
        { headerName: "상태", field: "statusCode", width: 120 },

        {
            headerName: "상세",
            width: 120,
            cellRenderer: p => {
                if (!p.data) return "";
                return `
                    <button class="btn btn-sm btn-outline-primary"
                        onclick="showClientDetail('${p.data.clientId}')">
                        열기
                    </button>
                `;
            },
            cellStyle: { textAlign: "center" }
        }
    ];

    const gridOptions = {
		theme: "legacy",
        columnDefs,
        defaultColDef: { resizable: true, sortable: true, filter: true },
        pagination: true,
        paginationPageSize: 20,
        rowHeight: 38,
        animateRows: true,
        onGridReady: params => {
            gridApi = params.api;
            console.log("✅ Client Grid Ready");
        }
    };

    const gridDiv = document.getElementById("clientGrid");
    if (gridDiv) {
        agGrid.createGrid(gridDiv, gridOptions);
    }
}


/* ==========================================================
   2. 목록 검색 AJAX
========================================================== */

function loadClientList() {

    const keyword = document.getElementById("keyword")?.value ?? "";
    const type = currentType ?? "CUSTOMER";

    const params = new URLSearchParams({ keyword, type });

    fetch(`/sales/client/data?${params.toString()}`)
        .then(res => res.json())
        .then(list => {
            if (!gridApi) return;
            try {
                gridApi.setGridOption("rowData", list);   // 최신 방식
            } catch {
                gridApi.setRowData(list);                 // 구버전 호환
            }
        })
        .catch(err => {
            console.error(err);
            alert("거래처 목록을 불러오는데 실패했습니다.");
        });
}

/* ==========================================================
   3. 상세조회
========================================================== */

function showClientDetail(clientId) {

    fetch(`/sales/client/${clientId}`)
        .then(res => res.json())
        .then(d => {
            // 해당 DOM 요소에 데이터 뿌리기
            document.getElementById("d-clientName").textContent = d.clientName;
            document.getElementById("d-clientType").textContent = d.clientType;
            document.getElementById("d-businessNo").textContent = d.businessNo;
            document.getElementById("d-ceoName").textContent = d.ceoName;

            document.getElementById("d-postCode").textContent = d.postCode;
            document.getElementById("d-addr").textContent = d.addr;
            document.getElementById("d-addrDetail").textContent = d.addrDetail;

            document.getElementById("d-managerName").textContent = d.managerName;
            document.getElementById("d-managerDept").textContent = d.managerDept;
            document.getElementById("d-managerTel").textContent = d.managerTel;
            document.getElementById("d-managerEmail").textContent = d.managerEmail;

            document.getElementById("d-bankName").textContent = d.bankName;
            document.getElementById("d-accountNumber").textContent = d.accountNumber;
            document.getElementById("d-accountName").textContent = d.accountName;

            new bootstrap.Modal(document.getElementById("clientDetailModal")).show();
        })
        .catch(err => {
            console.error(err);
            alert("상세 조회 오류가 발생했습니다.");
        });
}
