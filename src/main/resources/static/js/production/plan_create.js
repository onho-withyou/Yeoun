let orderGridApi = null;

document.addEventListener("DOMContentLoaded", () => {
    initOrderGrid();

    document.getElementById("btnLoadOrders").addEventListener("click", loadOrders);
    document.getElementById("btnCreatePlan").addEventListener("click", createPlan);
});

/* =========================================
   AG Grid 초기화
========================================= */
function initOrderGrid() {

    const columnDefs = [
        { headerName: "선택", checkboxSelection: true, headerCheckboxSelection: true, width: 60 },
        { headerName: "수주번호", field: "orderId", width: 140 },
        { headerName: "상세ID", field: "orderItemId", width: 120 },
        { headerName: "제품명", field: "productName", width: 200 },
        { headerName: "제품ID", field: "prdId", width: 130 },
        { headerName: "주문수량", field: "orderQty", width: 100 },
        { headerName: "납기일", field: "dueDate", width: 120 }
    ];

    const gridOptions = {
        columnDefs,
        rowSelection: "multiple",
    };

    orderGridApi = agGrid.createGrid(document.getElementById("orderGrid"), gridOptions);
}

/* =========================================
   1) 수주 목록 조회
========================================= */
function loadOrders() {
    const group = document.getElementById("productGroup").value;

    fetch(`/sales/order-items?group=${group}`)
        .then(res => res.json())
        .then(data => {
            orderGridApi.setGridOption("rowData", data);
        })
        .catch(err => console.error("수주 조회 실패:", err));
}

/* =========================================
   2) 생산계획 생성 요청
========================================= */
function createPlan() {
    const selected = orderGridApi.getSelectedRows();

    if (selected.length === 0) {
        alert("📌 생산계획에 포함할 수주 항목을 선택하세요.");
        return;
    }

    const prdSet = new Set(selected.map(r => r.prdId));
    if (prdSet.size > 1) {
        alert("⚠️ 서로 다른 제품은 함께 생산계획을 만들 수 없습니다.");
        return;
    }

    const items = selected.map(row => ({
        orderItemId: row.orderItemId,
        qty: row.orderQty,
        orderId: row.orderId
    }));

    const payload = {
        memo: "자동 생성된 생산계획",
        items: items
    };

    fetch("/production/create", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    })
        .then(res => res.text())
        .then(planId => {
            alert(`📌 생산계획 생성 완료\n계획 ID: ${planId}`);
            location.href = "/production/plan";
        })
        .catch(err => console.error("생산계획 생성 오류:", err));
}
