// /js/production/plan_detail_modal.js

/* ===============================
   생산계획 상세 모달 호출 함수
================================= */
function openPlanDetail(planId) {

    console.log("상세조회 요청:", planId);

    fetch(`/production/plan/detail/${planId}`)
        .then(res => res.json())
        .then(data => {
            console.log("상세조회 데이터:", data);

            /* ================================
               1. PLAN 기본 정보 세팅
               (서버 JSON 구조에 맞게 수정됨!)
            ================================= */
            document.getElementById("d_planId").innerText   = data.planId;
            document.getElementById("d_createdAt").innerText = data.createdAt;
            document.getElementById("d_dueDate").innerText   = data.dueDate ?? "-";
            document.getElementById("d_status").innerText    = data.status;
            document.getElementById("d_memo").innerText      = data.memo ?? "-";

            /* ================================
               2. PLAN_ITEM GRID 세팅
            ================================= */
            modalPlanItemGridApi.setGridOption("rowData", data.planItems);

            // OrderItemGrid 초기화
            modalOrderItemGridApi.setGridOption("rowData", []);

            /* ================================
               PLAN_ITEM 클릭 시 → ORDER_ITEM 표시
            ================================= */
            modalPlanItemGridApi.setGridOption("onRowClicked", (e) => {
                const list = data.orderItemMap[e.data.prdId] || [];
                modalOrderItemGridApi.setGridOption("rowData", list);
            });

            /* ================================
               3. 모달 오픈
            ================================= */
            const modal = new bootstrap.Modal(document.getElementById("planDetailModal"));
            modal.show();
        })
        .catch(err => console.error("상세 조회 에러:", err));
}

/* ===============================
   전역 등록 (onclick에서 사용)
================================= */
window.openPlanDetail = openPlanDetail;


/* ===============================
   AG GRID – 상세 아이템
================================= */
const modalPlanItemGridApi = agGrid.createGrid(
    document.getElementById("modalPlanItemGrid"),
    {
        columnDefs: [
            { headerName: "제품ID", field: "prdId", width: 120 },
            { headerName: "제품명", field: "prdName", width: 180 },
            { headerName: "계획수량", field: "planQty", width: 120 },
            { headerName: "BOM부족", field: "bomStatus", width: 120 },
            { headerName: "상태", field: "status", width: 120 }
        ],
        rowData: [],
        rowSelection: "single"
    }
);

/* ===============================
   AG GRID – 수주 상세 항목
================================= */
const modalOrderItemGridApi = agGrid.createGrid(
    document.getElementById("modalOrderItemGrid"),
    {
        columnDefs: [
            { headerName:"수주번호",  field:"orderId",      width:140 },
            { headerName:"거래처",    field:"clientName",   width:150 },
            { headerName:"주문수량",  field:"orderQty",     width:120 },
            { headerName:"수주일자",  field:"orderDate",    width:140 },
            { headerName:"납기일",    field:"deliveryDate", width:140 }
        ],
        rowData: []
    }
);
