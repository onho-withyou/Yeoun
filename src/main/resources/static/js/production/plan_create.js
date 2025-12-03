// static/js/prod/plan_create.js

let orderGridApi = null;

document.addEventListener("DOMContentLoaded", () => {
    initOrderGrid();

    document.getElementById("btnLoadOrders")
        .addEventListener("click", loadOrderList);

    document.getElementById("btnCreatePlan")
        .addEventListener("click", createPlan);
});

/* --------------------------------------
   1) GRID 초기화
-------------------------------------- */
function initOrderGrid() {
    const columnDefs = [
        { headerName: "선택", checkboxSelection: true, width: 60 },
        { headerName: "수주번호", field: "orderId", width: 140 },
        { headerName: "제품명", field: "itemName", width: 200 },
        { headerName: "제품유형", field: "itemType", width: 120 },
        { headerName: "수량", field: "qty", width: 100 },
        { headerName: "납기일", field: "dueDate", width: 140 },
        { headerName: "고객사", field: "clientName", width: 160 }
    ];

    const gridOptions = {
        columnDefs,
        rowSelection: "multiple",
        animateRows: true
    };

    const gridDiv = document.getElementById("orderGrid");
    orderGridApi = agGrid.createGrid(gridDiv, gridOptions);
}

/* --------------------------------------
   2) 수주목록 로드
-------------------------------------- */
function loadOrderList() {
    const productGroup = document.getElementById("productGroup").value;

    fetch(`/prod/plan/orders?group=${productGroup}`)
        .then(res => res.json())
        .then(data => {
            orderGridApi.setGridOption("rowData", data);
        })
        .catch(err => console.error(err));
}

/* --------------------------------------
   3) 생산계획 생성
-------------------------------------- */
function createPlan() {
    const selected = orderGridApi.getSelectedRows();

    if (selected.length === 0) {
        alert("생산계획을 생성할 수주를 선택하세요.");
        return;
    }

    const payload = selected.map(row => ({
        orderId: row.orderId,
        orderItemId: row.orderItemId,
        qty: row.qty
    }));

    fetch("/prod/plan/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(res => res.json())
        .then(result => {
            alert("생산계획서가 생성되었습니다.");
            window.location.href = "/prod/plan";
        })
        .catch(err => console.error(err));
}
