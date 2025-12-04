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
                return `<button class="btn btn-sm btn-secondary" onclick='showOrderItems(${JSON.stringify(json)})'>
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

    fetch(`/production/suggest?group=${group}`)
        .then(res => {
            if (!res.ok) throw new Error("API 오류");
            return res.json();
        })
        .then(data => {
            console.log("📌 조회된 데이터:", data);  // ✅ 디버깅
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
   4) 생산계획 자동 생성 (강화된 디버깅 버전)
========================================= */
function createProductionPlan() {
    const selected = suggestGridApi.getSelectedRows();

    if (selected.length === 0) {
        alert("📌 생산계획을 생성할 제품을 선택하세요.");
        return;
    }

    console.log("📌 [DEBUG] 선택된 데이터:", selected);

    const payload = selected.map(item => ({
        prdId: item.prdId,
        planQty: item.shortageQty > 0 ? item.shortageQty : item.totalOrderQty,
        orderItems: item.orderItems
    }));

    console.log("📌 [DEBUG] 전송 Payload:", JSON.stringify(payload, null, 2));

    fetch("/production/plan/createplan", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
    .then(async res => {

        console.log("📌 [DEBUG] 응답 코드:", res.status);

        const text = await res.text();
        console.log("📌 [DEBUG] 응답 원본 텍스트:", text);  // ⭐ 반드시 출력됨

        // HTML 응답 탐지
        if (text.startsWith("<") || text.includes("<html")) {
            console.error("❌ [DEBUG] 서버가 HTML을 반환했습니다!");
            alert("서버가 HTML을 반환했습니다. 로그인 만료 또는 서버 오류입니다.");
            return;
        }

        // JSON 변환
        let json;
        try {
            json = JSON.parse(text);
        } catch (e) {
            console.error("❌ [DEBUG] JSON 파싱 오류:", e);
            return;
        }

        console.log("📌 [DEBUG] 최종 JSON 파싱 결과:", json);

        if (json.success) {
            alert("✅ 생산계획 생성 완료! 생성된 계획: " + json.planIds);
            window.location.href = "/production/plan";
        } else {
            alert("❌ 생산계획 생성 실패: " + json.message);
        }

    })
    .catch(err => {
        console.error("❌ [DEBUG] Fetch 오류:", err);
    });
}
