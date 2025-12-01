/**
 * clientList.js
 * 거래처/협력사 목록 + 등록 + 상세조회
 */

let clientGrid = null;
let clientDetailModal = null;

document.addEventListener("DOMContentLoaded", () => {

    /* 메시지 출력 */
    const holder = document.getElementById("clientMsgHolder");
    if (holder) {
        const msg = holder.dataset.msg;
        if (msg) alert(msg);
    }

    /* 상세 모달 */
    clientDetailModal = new bootstrap.Modal(document.getElementById("clientDetailModal"));

    /* 검색 버튼 */
    const searchBtn = document.getElementById("btnSearch");
    if (searchBtn) {
        searchBtn.addEventListener("click", loadClientList);
    }

    /* 엔터 검색 */
    const keyword = document.getElementById("keyword");
    if (keyword) {
        keyword.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                loadClientList();
            }
        });
    }

    /* 그리드 생성 */
    initClientGrid();

    /* 초기 데이터 로딩 */
    clientGrid.resetData(initialClientList);

    /* 등록 이벤트 설정 */
    initRegisterEvent();
});


/* =======================================================
    1. AG GRID 생성
======================================================= */
function initClientGrid() {
    clientGrid = new tui.Grid({
        el: document.getElementById("clientGrid"),
        scrollX: true,
        scrollY: true,
        rowHeaders: [],
        columns: [
            { header: "구분", name: "clientType", width: 90, align: "center" },
            { header: "거래처명", name: "clientName", width: 180 },
            { header: "사업자번호", name: "businessNo", width: 140, align: "center" },
            { header: "대표자명", name: "ceoName", width: 120, align: "center" },
            { header: "담당자명", name: "managerName", width: 120, align: "center" },
            { header: "연락처", name: "managerTel", width: 150 },
            {
                header: " ",
                name: "btn",
                width: 80,
                align: "center",
                formatter: () => `<button class="btn btn-sm btn-info">상세</button>`
            }
        ],
        pagination: true,
        pageOptions: {
            perPage: 10,
            useClient: true
        }
    });

    /* 상세 버튼 클릭 */
    clientGrid.on("click", (ev) => {
        if (ev.columnName !== "btn") return;
        const row = clientGrid.getRow(ev.rowKey);
        if (!row || !row.clientId) return;

        showClientDetail(row.clientId);
    });
}


/* =======================================================
    2. 목록 AJAX 조회
======================================================= */
function loadClientList() {

    const keyword = document.getElementById("keyword").value;
    const type = currentType; // CUSTOMER / SUPPLIER

    const params = new URLSearchParams({
        keyword: keyword,
        type: type
    });

    fetch(`/sales/client/data?${params.toString()}`)
        .then(res => res.json())
        .then(data => {
            clientGrid.resetData(data);
        })
        .catch(err => {
            console.error(err);
            alert("거래처 목록을 불러오는데 실패했습니다.");
        });
}


/* =======================================================
    3. 상세 모달 조회
======================================================= */
function showClientDetail(clientId) {

    fetch(`/sales/client/detail?clientId=${clientId}`)
        .then(res => res.json())
        .then(d => {
            // 기본 정보
            document.getElementById("d-clientName").textContent = d.clientName;
            document.getElementById("d-clientType").textContent = d.clientType;
            document.getElementById("d-businessNo").textContent = d.businessNo;
            document.getElementById("d-ceoName").textContent = d.ceoName;

            // 주소
            document.getElementById("d-postCode").textContent = d.postCode;
            document.getElementById("d-addr").textContent = d.addr;
            document.getElementById("d-addrDetail").textContent = d.addrDetail;

            // 담당자
            document.getElementById("d-managerName").textContent = d.managerName;
            document.getElementById("d-managerDept").textContent = d.managerDept;
            document.getElementById("d-managerTel").textContent = d.managerTel;
            document.getElementById("d-managerEmail").textContent = d.managerEmail;

            // 계좌
            document.getElementById("d-bankName").textContent = d.bankName;
            document.getElementById("d-accountNumber").textContent = d.accountNumber;
            document.getElementById("d-accountName").textContent = d.accountName;

            // 상태
            document.getElementById("d-status").textContent = d.statusCode ?? "Y";

            clientDetailModal.show();
        })
        .catch(err => {
            console.error(err);
            alert("상세 조회 오류가 발생했습니다.");
        });
}

