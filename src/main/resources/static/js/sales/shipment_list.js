let gridApi = null;

/* =========================================================
   1) 페이지 로드 시 GRID 초기화 + 목록 조회
========================================================= */
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
            e.preventDefault();

            const target = e.currentTarget;

            document.querySelector("#shipmentTabs .active")?.classList.remove("active");
            target.classList.add("active");

            const status = target.dataset.status;
            loadShipmentList(status);
        });
    });

});



/* =========================================================
   2) GRID 컬럼 정의
========================================================= */
function initGrid() {

    const columnDefs = [
        { headerName: "선택", checkboxSelection: true, width: 60 },
        { headerName: "수주번호", field: "orderId", width: 150 },
        { headerName: "거래처명", field: "clientName", width: 130 },
        { headerName: "제품명", field: "prdName", width: 130 },
        { headerName: "수주수량", field: "orderQty", width: 100 },
        { headerName: "현재재고", field: "stockQty", width: 100 },
        { headerName: "납기요청일", field: "dueDate", width: 120 },

        {
            headerName: "출하상태",
            field: "status",
            width: 120,
            cellRenderer: params => renderStatusBadge(params.value)
        },

        {
            headerName: "출하예약",
            width: 120,
            cellRenderer: params => {
                if (params.data.status === "WAITING") {
                    return `
                        <button class="btn btn-sm btn-success"
                                onclick="reserveShipment('${params.data.shipmentId}')">
                            예약
                        </button>
                    `;
                }
                return "-";
            }
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



/* =========================================================
   3) 상태 배지
========================================================= */
function renderStatusBadge(status) {
    switch (status) {
        case "RESERVED":
            return `<span class="badge bg-primary">예약</span>`;
        case "LACK":
            return `<span class="badge bg-danger">부족</span>`;
        case "SHIPPED":
            return `<span class="badge bg-warning text-dark">출하완료</span>`;
        default:
            return `<span class="badge bg-secondary">대기</span>`;
    }
}



/* =========================================================
   4) 현재 선택된 탭 상태 읽기
========================================================= */
function getSelectedStatus() {
    const active = document.querySelector("#shipmentTabs .active");
    return active ? active.dataset.status : "ALL";
}



/* =========================================================
   5) 출하 목록 조회
========================================================= */
function loadShipmentList(status) {

    const param = {
        status,
        startDate: document.getElementById("startDate").value,
        endDate: document.getElementById("endDate").value,
        keyword: document.getElementById("keyword").value
    };

    fetch("/sales/shipment/list", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(param)
    })
        .then(res => res.json())
        .then(data => {
            gridApi.setGridOption("rowData", data);
        })
        .catch(err => {
            console.error("❌ 조회 오류:", err);
            alert("출하 목록 조회 중 오류가 발생했습니다.");
        });
}



/* =========================================================
   6) 출하 예약 요청 AJAX
========================================================= */
function reserveShipment(shipmentId) {

    if (!confirm("해당 주문을 출하 예약하시겠습니까?")) return;

    fetch(`/sales/shipment/reserve?shipmentId=${shipmentId}`, {
        method: "POST"
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert("✔ 출하 예약이 완료되었습니다.");
                loadShipmentList(getSelectedStatus());
            } else {
                alert("❌ 실패: " + data.message);
            }
        })
        .catch(err => {
            console.error("❌ 예약 오류:", err);
            alert("출하 예약 처리 중 오류가 발생했습니다.");
        });
}



/* =========================================================
   7) 상세 보기 (추후 모달 추가 예정)
========================================================= */
function openDetail(orderId) {
    alert("상세 페이지 준비 중: " + orderId);
}
