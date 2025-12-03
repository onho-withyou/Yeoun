// static/js/prod/plan_list.js
let planGridApi = null;

document.addEventListener("DOMContentLoaded", () => {
    initPlanGrid();
    loadPlanList();
});

function initPlanGrid() {

    const columnDefs = [
        { headerName: "계획ID", field: "planId", width: 120 },
        { headerName: "작성일", field: "createdAt", width: 140 },
        { headerName: "제품명", field: "itemName", width: 180 },
        { headerName: "총수량", field: "totalQty", width: 100 },
        { headerName: "상태", field: "status", width: 120 },
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
    fetch("/prod/plan/list")
        .then(res => res.json())
        .then(data => {
            planGridApi.setGridOption("rowData", data);
        })
        .catch(err => console.error(err));
}
