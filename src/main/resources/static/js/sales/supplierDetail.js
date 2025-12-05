// supplierDetail.js  (조회 전용)

let itemGridApi = null;
const clientId = window.clientId ?? null;

/* =======================================================
   페이지 로딩 시 실행
======================================================= */
document.addEventListener("DOMContentLoaded", () => {
    // 콘솔에서 데이터 확인 (초기 디버깅용)
    console.log("clientId =", clientId);
    console.log("initialItemList =", window.initialItemList);

    initItemGrid();
    loadItemGrid();
});

/* =======================================================
   1) AG-GRID — 협력사 취급 품목 목록
======================================================= */
function initItemGrid() {

    const columnDefs = [
        { headerName: "품목ID", field: "itemId", width: 120 },

        // 자재코드 (materialId 또는 matId 중 있는 값 사용)
        {
            headerName: "자재코드",
            width: 140,
            valueGetter: p => p.data.materialId || p.data.matId || ""
        },

        // 품명 (materialName 또는 matName 중 있는 값 사용)
        {
            headerName: "품명",
            flex: 1,
            valueGetter: p => p.data.materialName || p.data.matName || ""
        },

        // 단위 (unit 또는 matUnit 중 있는 값 사용)
        {
            headerName: "단위",
            width: 90,
            valueGetter: p => p.data.unit || p.data.matUnit || ""
        },

        { headerName: "단가", field: "unitPrice", width: 120 },

        {
            headerName: "MOQ",
            width: 120,
            valueGetter: p => {
                // DTO에 따라 필드명이 다를 수 있어 둘 다 체크
                return p.data.moq ?? p.data.minOrderQty ?? "";
            }
        },

        {
            headerName: "공급",
            field: "supplyAvailable",
            width: 100,
            cellRenderer: p => {
                const v = p.value;
                if (v === "Y") return "가능";
                if (v === "N") return "불가";
                return v ?? "";
            }
        }
    ];

    const gridOptions = {
        columnDefs,
        rowData: [],
        defaultColDef: {
            sortable: true,
            filter: true,
            resizable: true
        },
        pagination: true,
        paginationPageSize: 20
    };

    const gridDiv = document.getElementById("supplierItemGrid");
    if (!gridDiv) {
        console.error("supplierItemGrid 요소를 찾을 수 없습니다.");
        return;
    }

    itemGridApi = agGrid.createGrid(gridDiv, gridOptions);
}

/* =======================================================
   2) 초기 데이터 로드
======================================================= */
function loadItemGrid() {
    if (!itemGridApi) return;

    const data = window.initialItemList || [];
    console.log("loadItemGrid data =", data);

    itemGridApi.setGridOption("rowData", data);
}
