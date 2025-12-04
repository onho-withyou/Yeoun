let suggestGridApi = null;

document.addEventListener("DOMContentLoaded", () => {
    initSuggestGrid();

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
                return `<button class="btn btn-sm btn-secondary" onclick='showOrderItems("${json}")'>
                            보기
                        </button>`;
            }
        }
    ];

    const gridOptions = {
        columnDefs,
        rowSelection: "multiple",
        suppressRowClickSelection: true,
    };

    suggestGridApi = agGrid.createGrid(
        document.getElementById("suggestGrid"),
        gridOptions
    );
}

/* =========================================
   2) 추천 생산 목록 조회 (API 호출)
========================================= */
function loadSuggestList() {
    const group = document.getElementById("productGroup").value;

    fetch(`/production/plan/suggest?group=${group}`)
        .then(res => {
            if (!res.ok) throw new Error("API 오류");
            return res.json();
        })
        .then(data => {
            suggestGridApi.setGridOption("rowData", data);
        })
        .catch(err => {
            console.error("추천 생산 목록 조회 오류:", err);
            alert("추천 목록 조회 중 오류 발생");
        });
}

/* =========================================
   3) 상세 보기 (orderItems)
========================================= */
function showOrderItems(json) {

    const orderItems = JSON.parse(decodeURIComponent(json));

    let text = `📌 포함된 수주 내역\n\n`;

    orderItems.forEach(o => {
        text += `• 수주번호: ${o.orderId}\n`;
        text += `  수량: ${o.orderQty}\n`;
        text += `  납기일: ${o.dueDate}\n\n`;
    });

    alert(text);
}

/* =========================================
   4) 생산계획 자동 생성
========================================= */
function createProductionPlan() {
    const selected = suggestGridApi.getSelectedRows();

    if (selected.length === 0) {
        alert("📌 생산계획을 생성할 제품을 선택하세요.");
        return;
    }

    const payload = selected.map(item => ({
        prdId: item.prdId,
        planQty: item.shortageQty > 0 ? item.shortageQty : item.totalOrderQty,
        orderItems: item.orderItems
    }));

    fetch("/production/auto-create-plan", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(res => res.text())
        .then(msg => {
            alert("📌 생산계획 생성 완료!\n" + msg);
            location.href = "/production/plan";
        })
        .catch(err => {
            console.error("생산계획 생성 오류:", err);
            alert("생산계획 생성 중 오류 발생");
        });
}
