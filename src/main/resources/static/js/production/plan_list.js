// /js/production/plan_list.js
console.log("✔ plan_list.js 로드됨!");

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
            // 👉 여기서 전역 함수 openPlanDetail 만 호출
            cellRenderer: (params) =>
                `<button class="btn btn-sm btn-primary"
                          onclick="openPlanDetail('${params.data.planId}')">보기</button>`
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
            if (!planGridApi) {
                console.error("📌 planGridApi가 아직 준비되지 않았습니다.");
                return;
            }
            planGridApi.setGridOption("rowData", data);
        })
        .catch(err => console.error("📌 목록 조회 에러:", err));
}
