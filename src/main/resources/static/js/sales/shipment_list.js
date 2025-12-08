let gridApi = null;

// =========================================
// 1) 페이지 로드 후 GRID 초기화 + 목록 조회
// =========================================
document.addEventListener("DOMContentLoaded", () => {

    initGrid();
    loadShipmentList("ALL");

    // 검색 버튼
    document.getElementById("btnSearch")?.addEventListener("click", () => {
        loadShipmentList(getSelectedStatus());
    });

    // 초기화 버튼
    document.getElementById("btnReset")?.addEventListener("click", () => {
        document.getElementById("startDate").value = "";
        document.getElementById("endDate").value = "";
        document.getElementById("keyword").value = "";
        loadShipmentList("ALL");
    });

    // 탭 클릭 이벤트
    document.querySelectorAll("#shipmentTabs .nav-link")?.forEach(tab => {
        tab.addEventListener("click", (e) => {

            document.querySelector("#shipmentTabs .active")?.classList.remove("active");
            e.target.classList.add("active");

            const status = e.target.dataset.status;
            loadShipmentList(status);
        });
    });

});


// =========================================
// 2) GRID 컬럼 정의
// =========================================
function initGrid() {
    const columnDefs = [
        { headerName: "선택", checkboxSelection: true, width: 60 },
        { headerName: "수주번호", field: "orderId", width: 150 },
        { headerName: "거래처명", field: "clientName", width: 130 },
        { headerName: "제품명", field: "prdName", width: 130 },
        { headerName: "수주수량", field: "orderQty", width: 100 },
        { headerName: "현재재고", field: "currentStock", width: 100 },
        { headerName: "납기요청일", field: "deliveryDate", width: 120 },

        {
            headerName: "출하상태",
            field: "shipmentStatus",
            width: 120,
            cellRenderer: params => renderStatusBadge(params.value)
        },

        {
            headerName: "상세",
            width: 100,
            cellRenderer: params => `
                <button class="btn btn-outline-primary btn-sm"
                        onclick="openDetail('${params.data.orderId}')">
                    상세
                </button>
            `
        }
    ];

    gridApi = agGrid.createGrid(
        document.getElementById("shipmentGrid"),
        {
            columnDefs,
            rowSelection: "multiple",
            suppressRowClickSelection: true,
            rowData: []
        }
    );
}


// =========================================
// 3) 상태 배지 렌더링
// =========================================
function renderStatusBadge(status) {
    switch (status) {
        case "RESERVED":
            return `<span class="badge bg-primary">예약</span>`;
        case "LACK":
            return `<span class="badge bg-danger">부족</span>`;
        case "COMPLETED":
            return `<span class="badge bg-warning text-dark">출하완료</span>`;
        default:
            return `<span class="badge bg-secondary">대기</span>`;
    }
}


// =========================================
// 4) 선택된 탭 상태값 읽기
// =========================================
function getSelectedStatus() {
    const active = document.querySelector("#shipmentTabs .active");
    return active ? active.dataset.status : "ALL";
}


// =========================================
// 5) 출하 목록 조회 AJAX
// =========================================
function loadShipmentList(status) {

    const param = {
        status,
        startDate: document.getElementById("startDate").value,
        endDate: document.getElementById("endDate").value,
        keyword: document.getElementById("keyword").value
    };

    const csrfToken  = document.querySelector('meta[name="_csrf_token"]')?.content || "";
    const csrfHeader = document.querySelector('meta[name="_csrf_headerName"]')?.content || "";

    const headers = {
        "Content-Type": "application/json"
    };

    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    fetch("/sales/shipment/list", {
        method: "POST",
        headers: headers,
        body: JSON.stringify(param)
    })
    .then(async res => {
        const text = await res.text();

        if (!res.ok) {
            console.error("❌ 서버 오류:", text);
            alert("출하 목록 조회 중 오류 발생");
            return [];
        }

        try {
            return JSON.parse(text);
        } catch (e) {
            console.error("❌ JSON 파싱 실패:", text);
            alert("서버 응답이 올바르지 않습니다.");
            return [];
        }
    })
    .then(data => {
        gridApi.setGridOption("rowData", data);
    })
    .catch(err => {
        console.error("❌ fetch 오류:", err);
        alert("서버와 통신 중 문제가 발생했습니다.");
    });
}


// =========================================
// 6) 상세 보기 (추후 모달 예정)
// =========================================
function openDetail(orderId) {
    alert("상세 페이지 준비 중: " + orderId);
}
