// clientList.js
// 거래처 / 협력사 목록 Toast Grid + 모달

let clientGrid = null;
let clientDetailModal = null;
let currentClientId = null;

document.addEventListener("DOMContentLoaded", () => {
    // 메시지 출력
    const holder = document.getElementById('clientMsgHolder');
    if (holder) {
        const msg = holder.dataset.msg;
        if (msg) alert(msg);
    }

    // 상세 모달 초기화
    clientDetailModal = new bootstrap.Modal(document.getElementById('clientDetailModal'));

    // 검색 버튼
    const searchBtn = document.getElementById('btnSearch');
    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            loadClientList();
        });
    }

    // 엔터 검색
    const keywordInput = document.getElementById('keyword');
    if (keywordInput) {
        keywordInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                loadClientList();
            }
        });
    }

    initClientGrid();
    clientGrid.resetData(initialClientList);  // 최초 데이터 입력
});


/* ============================================================
    서버에서 목록 조회
============================================================ */
function loadClientList() {

    const keyword = document.getElementById('keyword').value;
    const type = currentType;  // CUSTOMER / SUPPLIER

    const params = new URLSearchParams({
        keyword: keyword,
        type: type
    });

    fetch('/sales/client/data?' + params.toString())
        .then(res => res.json())
        .then(data => {
            clientGrid.resetData(data);
        })
        .catch(err => {
            console.error(err);
            alert('거래처 목록을 불러오는데 실패했습니다.');
        });
}


/* ============================================================
    AG Grid 생성
============================================================ */
function initClientGrid() {

    clientGrid = new tui.Grid({
        el: document.getElementById('clientGrid'),
        rowHeaders: [],
        scrollX: true,
        scrollY: true,
        editable: false,
        pagination: true,
        pageOptions: {
            useClient: true,
            perPage: 10
        },
        columns: [
            {
                header: '구분',
                name: 'clientTypeName',
                width: 90,
                align: 'center'
            },
            {
                header: '거래처명',
                name: 'clientName',
                width: 180,
                align: 'left'
            },
            {
                header: '사업자번호',
                name: 'businessNo',
                width: 140,
                align: 'center'
            },
            {
                header: '대표자명',
                name: 'ceoName',
                width: 120,
                align: 'center'
            },
            {
                header: '담당자',
                name: 'managerName',
                width: 120,
                align: 'center'
            },
            {
                header: '연락처',
                name: 'managerEmail',
                width: 200,
                align: 'left'
            },
            {
                header: ' ',
                name: 'btn',
                width: 90,
                align: 'center',
                formatter: () => "<button class='btn btn-info btn-sm'>상세</button>"
            }
        ]
    });

    // 상세 버튼 클릭 이벤트
    clientGrid.on('click', (ev) => {
        if (ev.columnName !== 'btn') return;

        const row = clientGrid.getRow(ev.rowKey);
        if (!row || !row.clientId) return;

        showClientDetail(row.clientId);
    });
}


/* ============================================================
    상세 모달 표시
============================================================ */
function showClientDetail(clientId) {
    currentClientId = clientId;

    fetch(`/sales/client/${clientId}`)
        .then(res => res.json())
        .then(d => {

            // 기본 정보
            document.getElementById('d-clientName').textContent = d.clientName;
            document.getElementById('d-clientType').textContent = d.clientType;
            document.getElementById('d-businessNo').textContent = d.businessNo;
            document.getElementById('d-ceoName').textContent = d.ceoName;

            // 주소
            document.getElementById('d-postCode').textContent = d.postCode;
            document.getElementById('d-addr').textContent = d.addr;
            document.getElementById('d-addrDetail').textContent = d.addrDetail;

            // 담당자 정보
            document.getElementById('d-managerName').textContent = d.managerName;
            document.getElementById('d-managerDept').textContent = d.managerDept;
            document.getElementById('d-managerTitle').textContent = d.managerTitle;
            document.getElementById('d-managerEmail').textContent = d.managerEmail;

            // 계좌
            document.getElementById('d-bankName').textContent = d.bankName;
            document.getElementById('d-accountNumber').textContent = d.accountNumber;
            document.getElementById('d-accountName').textContent = d.accountName;

            // 상태
            document.getElementById('d-status').textContent = d.statusCode;

            clientDetailModal.show();
        })
        .catch(err => {
            console.error(err);
            alert('상세 조회 중 오류가 발생하였습니다.');
        });
}

/* ============================================================
   거래처 등록 처리
============================================================ */
document.addEventListener("DOMContentLoaded", () => {

    const regBtn = document.getElementById("btnClientRegSubmit");
    if (regBtn) {
        regBtn.addEventListener("click", () => {

            const form = document.getElementById("clientRegForm");
            const formData = new FormData(form);

            fetch("/sales/client/create", {
                method: "POST",
                body: formData
            })
            .then(res => res.text())
            .then(result => {
                if (result === "OK") {
                    alert("등록되었습니다.");
                    location.reload(); // 목록 다시 로딩
                } else {
                    alert("등록 실패: " + result);
                }
            })
            .catch(err => {
                console.error(err);
                alert("등록 중 오류 발생");
            });

        });
    }

});

