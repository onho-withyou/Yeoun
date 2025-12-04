
let planGridApi = null;

document.addEventListener("DOMContentLoaded", () => {
    initPlanGrid();
    loadPlanList();
});

function initPlanGrid() {

    const columnDefs = [
        { headerName: "계획ID", field: "planId", width: 200 },
        { headerName: "작성일", field: "createdAt", width: 200 },
        { headerName: "제품명", field: "itemName", width: 180 },
        { headerName: "총수량", field: "totalQty", width: 200 },
        { headerName: "상태", field: "status", width: 200 },
        {
            headerName: "상세",
            width: 100,
            cellRenderer: () => `<button class="btn btn-sm btn-primary">보기</button>`
        }
    ];

    const gridOptions = {
        columnDefs,
        rowSelection: "single",
    };

    planGridApi = agGrid.createGrid(document.getElementById("planGrid"), gridOptions);
}

function loadPlanList() {
    fetch("/production/list")
        .then(res => res.json())
        .then(data => {
            console.log("📌 서버에서 받아온 데이터:", data);
            planGridApi.setGridOption("rowData", data);
        })
        .catch(err => console.error("📌 에러 발생:", err));
}

