let suggestGridApi = null;
let orderItemGridApi = null;

/* ========================================================
   INIT
======================================================== */
document.addEventListener("DOMContentLoaded", () => {
    initSuggestGrid();
    initOrderItemGrid();

    document.getElementById("btnLoadSuggested")
        .addEventListener("click", loadSuggestList);

    document.getElementById("btnCreatePlan")
        .addEventListener("click", createProductionPlan);
});


/* ========================================================
   1) 추천 생산 목록 GRID
======================================================== */
function initSuggestGrid() {

    const columnDefs = [
		{ headerName: "선택", checkboxSelection: true, width: 60 },
		    { headerName: "제품명", field: "prdName", width: 150 },
		    { headerName: "총 주문수량", field: "totalOrderQty", width: 120 },
		    { headerName: "현재 재고", field: "currentStock", width: 120 },
		    { headerName: "부족수량", field: "shortageQty", width: 120 },
		    { headerName: "수주건수", field: "orderCount", width: 100 },        
			{
			           headerName: "가장 빠른 납기",
			           field: "earliestDeliveryDate",
			           width: 140,
			           cellRenderer: p => p.value ? p.value : "-"
			       },

			       {
			           headerName: "원자재 재고",
			           field: "bomStatus",   // ⭐ 필드명 수정
			           width: 120,
			           cellRenderer: p => {
			               if (!p.value) return "-";

			               return p.value === "부족"
			                   ? "<span style='color:red;'>❌ 부족</span>"
			                   : "<span style='color:green;'>✔ 가능</span>";
			           }
			       },

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
                return `
                    <button class="btn btn-sm btn-secondary"
                            onclick='showOrderItems("${params.data.prdId}")'>
                        보기
                    </button>`;
            }
        }
    ];

	suggestGridApi = agGrid.createGrid(
	    document.getElementById("suggestGrid"),
	    {
	        columnDefs,
	        rowSelection: "multiple",
	        suppressRowClickSelection: true,
	        rowData: [],

	        // ⭐ No Rows 메시지 변경
	        localeText: {
	            noRowsToShow: "생산목록 조회 중입니다"
	        }
	    }
	);

}


/* ========================================================
   2) 추천 목록 조회
======================================================== */
function loadSuggestList() {

    const group = document.getElementById("productGroup").value;

    fetch(`/production/suggest?group=${group}`)
        .then(res => res.json())
        .then(data => {
            suggestGridApi.setGridOption("rowData", data);
        });
}


/* ========================================================
   3) 상세보기 → 서버에서 OrderItemDTO 리스트 조회 후 모달 표시
======================================================== */
function initOrderItemGrid() {

    const colDefs = [
        { headerName: "수주번호", field: "orderId", width: 150 },
		{ headerName: "거래처명", field: "clientName", width: 150 },
        { headerName: "제품명", field: "prdName", width: 150 },
        { headerName: "주문수량", field: "orderQty", width: 120 },
		{ headerName: "내부 담당자", field: "empName", width: 150 },
        { headerName: "납기일", field: "deliveryDate", width: 150 },        
        { headerName: "담당자명", field: "managerName", width: 150 },
        { headerName: "연락처", field: "managerTel", width: 150 },
        { headerName: "이메일", field: "managerEmail", width: 200 }
    ];

    orderItemGridApi = agGrid.createGrid(
        document.getElementById("orderItemGrid"),
        {
            columnDefs: colDefs,
            defaultColDef: { sortable: true, filter: true, resizable: true }
        }
    );
}


/* ⭐⭐⭐ 여기 완전히 새로 만듦 — DTO 불러오는 새로운 방식 */
function showOrderItems(prdId) {

    fetch(`/production/order-items/${prdId}`)
        .then(res => res.json())
        .then(data => {
            console.log("📌 수주 상세 데이터:", data);

            orderItemGridApi.setGridOption("rowData", data);

            const modal = new bootstrap.Modal(document.getElementById("orderItemModal"));
            modal.show();
        });
}


/* ========================================================
   4) 생산계획 생성 (메모 + CSRF 포함)
======================================================== */
function createProductionPlan() {

    const selected = suggestGridApi.getSelectedRows();
    if (selected.length === 0) {
        alert("📌 생산계획을 생성할 제품을 선택하세요");
        return;
    }

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

    const payload = { items, memo };

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

// ⭐ 생산조회 버튼 클릭 → resultSection 표시
document.getElementById("btnLoadSuggested").addEventListener("click", () => {
    document.getElementById("placeholderMessage").style.display = "none";
    document.getElementById("resultSection").style.display = "block";
});
