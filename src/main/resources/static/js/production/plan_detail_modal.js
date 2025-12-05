/* ===============================
   생산계획 상세 모달 호출 함수
================================= */
function openPlanDetail(planId) {

    fetch(`/production/plan/detail/${planId}`)
        .then(res => res.json())
        .then(data => {

            // ========== 1. PLAN 기본 정보 ==========
            document.getElementById("d_planId").innerText = data.plan.planId;
            document.getElementById("d_createdAt").innerText = data.plan.createdAt;
            document.getElementById("d_dueDate").innerText = data.plan.dueDate;
            document.getElementById("d_status").innerText = data.plan.status;
            document.getElementById("d_memo").innerText = data.plan.memo ?? "-";

            // ========== 2. PLAN_ITEM GRID ==========
            modalPlanItemGridApi.setGridOption("rowData", data.planItems);

            // ORDER 초기화
            modalOrderItemGridApi.setGridOption("rowData", []);

            // 클릭 시 수주 데이터 표시
            modalPlanItemGridApi.setGridOption("onRowClicked", (e) => {
                const list = data.orderItemMap[e.data.prdId] || [];
                modalOrderItemGridApi.setGridOption("rowData", list);
            });

            // ========== 3. 모달 오픈 ==========
            const modal = new bootstrap.Modal(document.getElementById("planDetailModal"));
            modal.show();
        })
        .catch(err => console.error("상세 조회 에러:", err));
}


/* ===============================
   AG GRID 초기화
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

const modalOrderItemGridApi = agGrid.createGrid(
    document.getElementById("modalOrderItemGrid"),
    {
        columnDefs: [
            { headerName:"수주번호", field:"orderId", width:140 },
            { headerName:"거래처", field:"clientName", width:150 },
            { headerName:"주문수량", field:"orderQty", width:120 },
            { headerName:"수주일자", field:"orderDate", width:140 },
            { headerName:"납기일", field:"deliveryDate", width:140 }
        ],
        rowData: []
    }
);
