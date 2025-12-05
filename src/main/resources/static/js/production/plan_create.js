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
        suppressRowClickSelection: true
    };

    suggestGridApi = agGrid.createGrid(
        document.getElementById("suggestGrid"),
        gridOptions
    );
}

/* =========================================
   2) 추천 생산 목록 조회
========================================= */
function loadSuggestList() {
    const group = document.getElementById("productGroup").value;

    fetch(`/production/suggest?group=${group}`)
        .then(res => res.json())
        .then(data => {
            console.log("📌 조회된 데이터:", data);
            suggestGridApi.setGridOption("rowData", data);
        })
        .catch(err => {
            console.error("추천 목록 조회 오류:", err);
        });
}

/* =========================================
   3) 상세 보기
========================================= */
function showOrderItems(json) {
    const arr = JSON.parse(decodeURIComponent(json));

    let text = `📌 포함된 수주 내역\n\n`;

    arr.forEach(o => {
        text += `• 수주번호: ${o.orderId}\n`;
        text += `  수량: ${o.orderQty}\n`;
        text += `  납기일: ${o.dueDate}\n\n`;
    });

    alert(text);
}

/* =========================================
   4) 생산계획 자동 생성 (CSRF 적용 완성본)
========================================= */
function createProductionPlan(e) {

    const selected = suggestGridApi.getSelectedRows();
    if (selected.length === 0) {
        alert("📌 생산계획을 생성할 제품을 선택하세요");
        return;
    }

    // DTO 전송 구조 변환
    const payload = [];

    selected.forEach(item => {
        item.orderItems.forEach(order => {
            payload.push({
                orderItemId: order.orderItemId,
                qty: order.orderQty
            });
        });
    });

    console.log("📌 [DEBUG] 최종 Payload:", payload);

    const csrfToken = document.querySelector('meta[name="_csrf_token"]').content;

    fetch("/production/create/submit", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
        },
        body: JSON.stringify({
            items: payload,
            memo: ""
        })
    })
    .then(async res => {
        const text = await res.text();
        console.log("📌 [DEBUG] 서버 RAW:", text);

        if (text.startsWith("<") || text.includes("<html")) {
            console.error("HTML 응답(로그인 만료 or 권한 문제)");
            alert("서버가 HTML을 반환했습니다.\n로그인 만료 또는 권한 오류입니다.");
            return;
        }

        const data = JSON.parse(text);
        console.log("📌 [DEBUG] JSON:", data);

        if (data.success) {
            alert("🎉 생산계획 생성 완료! PLAN ID: " + data.planId);
            location.href = "/production/plan";
        } else {
            alert("❌ 실패: " + data.message);
        }
    })
    .catch(e => {
        console.error("Fetch 오류:", e);
        alert("서버 통신 중 오류 발생");
    });
}
