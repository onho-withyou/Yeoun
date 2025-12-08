let suggestGridApi = null;
let orderItemGridApi = null;  // ⭐ 모달용 Grid

document.addEventListener("DOMContentLoaded", () => {
    initSuggestGrid();
    initOrderItemGrid();   // ⭐ 추가된 부분

    document.getElementById("btnLoadSuggested")
        .addEventListener("click", loadSuggestList);

    document.getElementById("btnCreatePlan")
        .addEventListener("click", createProductionPlan);
});


/* =========================================
   1) 추천 생산 목록 GRID 초기화
========================================= */
function initSuggestGrid() {

    const columnDefs = [
        { headerName: "선택", checkboxSelection: true, headerCheckboxSelection: true, width: 60 },
        { headerName: "제품명", field: "prdName", width: 180 },
        { headerName: "총 주문수량", field: "totalOrderQty", width: 120 },
        { headerName: "현재 재고", field: "currentStock", width: 120 },
        { headerName: "부족수량", field: "shortageQty", width: 120 },

        {
            headerName: "생산 필요",
            field: "needProduction",
            width: 120,
            cellRenderer: params => {
                return params.value === "YES"
                    ? `<span style="color:#d9534f; font-weight:bold;">YES</span>`
                    : `<span style="color:#5cb85c;">NO</span>`;
            }
        },

        {
            headerName: "상세",
            width: 100,
            cellRenderer: params => {
                const json = encodeURIComponent(JSON.stringify(params.data.orderItems));
                return `
                    <button class="btn btn-sm btn-secondary"
                            onclick='showOrderItems("${json}")'>
                        보기
                    </button>`;
            }
        }
    ];

    const gridOptions = {
        columnDefs,
        rowSelection: "multiple",
        suppressRowClickSelection: true
    };

    suggestGridApi = agGrid.createGrid(
        document.getElementById("suggestGrid"),
        gridOptions
    );
}



/* =========================================
   2) 추천 생산 목록 조회
========================================= */
function loadSuggestList() {
    const group = document.getElementById("productGroup").value;

    fetch(`/production/suggest?group=${group}`)
        .then(res => res.json())
        .then(data => {
            console.log("📌 조회된 데이터:", data);
            suggestGridApi.setGridOption("rowData", data);
        })
        .catch(err => {
            console.error("추천 목록 조회 오류:", err);
        });
}



/* =========================================
   3) 상세 보기 → 모달 + AG Grid로 표시
========================================= */
function initOrderItemGrid() {

    const colDefs = [
        { headerName: "수주번호", field: "orderId", width: 150 },
        { headerName: "제품명", field: "prdName", width: 150 },
        { headerName: "주문수량", field: "orderQty", width: 120 },
        { headerName: "납기일", field: "dueDate", width: 150 },        
        { headerName: "거래처명", field: "clientName", width: 150 },
        { headerName: "담당자명", field: "managerName", width: 130 },
        { headerName: "연락처", field: "managerTel", width: 150 },
        { headerName: "이메일", field: "managerEmail", width: 180 }
    ];

    orderItemGridApi = agGrid.createGrid(
        document.getElementById("orderItemGrid"),
        {
            columnDefs: colDefs,
            defaultColDef: {
                resizable: true,
                sortable: true,
                filter: true
            }
        }
    );
}



function showOrderItems(json) {

    const arr = JSON.parse(decodeURIComponent(json));

    // ⭐ 모달 그리드에 데이터 입력
    orderItemGridApi.setGridOption("rowData", arr);

    // ⭐ 모달 열기
    const modal = new bootstrap.Modal(document.getElementById("orderItemModal"));
    modal.show();
}



/* =========================================
   4) 생산계획 자동 생성 (메모 + CSRF 적용)
========================================= */
function createProductionPlan() {

    const selected = suggestGridApi.getSelectedRows();
    if (selected.length === 0) {
        alert("📌 생산계획을 생성할 제품을 선택하세요");
        return;
    }

    // 수주 항목 변환
    const items = [];
    selected.forEach(item => {
        item.orderItems.forEach(order => {
            items.push({
                orderItemId: order.orderItemId,
                qty: order.orderQty
            });
        });
    });

    const memo = document.getElementById("planMemo")?.value || "";

    const payload = {
        items: items,
        memo: memo
    };

    console.log("📌 최종 Payload:", payload);

    fetch("/production/create/submit", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
        },
        body: JSON.stringify(payload)
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert("🎉 생산계획 생성 완료!");
                location.href = "/production/plan";
            } else {
                alert("❌ 실패: " + data.message);
            }
        });
}
