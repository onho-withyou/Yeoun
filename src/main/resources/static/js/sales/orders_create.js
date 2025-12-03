document.addEventListener("DOMContentLoaded", () => {

    /* ============================================
       1) 제품 목록 추가 버튼 클릭
    ============================================ */
    const addItemBtn = document.getElementById("addItemBtn");
    const productList = window.productList ?? [];

    if (addItemBtn) {
        addItemBtn.addEventListener("click", () => {
            const tbody = document.getElementById("itemBody");
            if (!tbody) return;

            const row = document.createElement("tr");
            row.innerHTML = `
                <td>
                    <select class="form-select prd-select" name="items[][prdId]" required>
                        <option value="">-- 선택 --</option>
                        ${productList.map(p =>
                            `<option value="${p.prdId}"
                                    data-price="${p.unitPrice}"
                                    data-minqty="${p.minQty}"
                                    data-unit="${p.prdUnit}">
                                ${p.prdName}
                            </option>`
                        ).join("")}
                    </select>
                </td>

                <td><input type="number" class="form-control price-input" name="items[][unitPrice]" readonly></td>
                <td><input type="number" class="form-control minqty-input" name="items[][minQty]" readonly></td>
                <td><input type="text" class="form-control unit-input" name="items[][unit]" readonly></td>
                <td><input type="number" class="form-control qty-input" name="items[][qty]" min="1" required></td>
                <td><input type="number" class="form-control amount-input" name="items[][amount]" readonly></td>
                
                <td class="text-center">
                    <button type="button" class="btn btn-sm btn-danger delBtn">X</button>
                </td>
            `;

            tbody.appendChild(row);

            /* 삭제 버튼 */
            row.querySelector(".delBtn").addEventListener("click", () => row.remove());

            /* 요소들 참조 */
            const prdSelect  = row.querySelector(".prd-select");
            const priceInput = row.querySelector(".price-input");
            const minQtyInput = row.querySelector(".minqty-input");
            const unitInput = row.querySelector(".unit-input");
            const qtyInput  = row.querySelector(".qty-input");
            const amountInput = row.querySelector(".amount-input");

            /* ================================
               제품 선택 시 → 최소수량/단위/단가 자동 입력
            ================================= */
            prdSelect.addEventListener("change", () => {
                const opt = prdSelect.selectedOptions[0];

                const unitPrice = parseInt(opt.dataset.price) || 0;
                const minQty    = parseInt(opt.dataset.minqty) || 0;
                const unit      = opt.dataset.unit ?? "";

                priceInput.value  = unitPrice;
                minQtyInput.value = minQty;
                unitInput.value   = unit;

                let qty = parseInt(qtyInput.value) || 0;

                // 최소수량 보정
                if (qty < minQty) {
                    qty = minQty;
                }

                // 🔥 수량 10 단위로 보정
                if (qty % 10 !== 0) {
                    qty = Math.ceil(qty / 10) * 10;
                }

                qtyInput.value = qty;
                amountInput.value = qty * unitPrice;
            });

            /* ================================
               수량 입력 시 → 최소수량 적용 + 10단위 보정
            ================================= */
            qtyInput.addEventListener("input", () => {

                let qty = parseInt(qtyInput.value) || 0;
                const minQty = parseInt(minQtyInput.value) || 0;

                if (qty < minQty) qty = minQty;
                if (qty % 10 !== 0) qty = Math.ceil(qty / 10) * 10;

                qtyInput.value = qty;

                amountInput.value = qty * (parseInt(priceInput.value) || 0);
            });
        });
    }

    /* ============================================
       2) 거래처 자동완성 검색
    ============================================ */
    const clientSearch = document.getElementById("clientSearch");
    const autoList = document.getElementById("clientAutoList");

    if (clientSearch && autoList) {
        clientSearch.addEventListener("input", () => {
            const keyword = clientSearch.value.trim();

            // 입력 시 기존 표시 정보 초기화
            resetClientInfo();

            if (keyword.length < 1) {
                autoList.innerHTML = "";
                autoList.classList.add("d-none");
                return;
            }

            fetch(`/sales/orders/search-customer?keyword=${encodeURIComponent(keyword)}`)
                .then(r => r.json())
                .then(list => {
                    if (!list || list.length === 0) {
                        autoList.innerHTML = "";
                        autoList.classList.add("d-none");
                        return;
                    }

                    autoList.innerHTML = list.map(c => `
                        <button type="button"
                            class="list-group-item list-group-item-action auto-item"
                            data-client-id="${c.clientId}"
                            data-client-name="${c.clientName}">
                            ${c.clientName}
                        </button>
                    `).join("");

                    autoList.classList.remove("d-none");
                })
                .catch(err => console.error("검색 오류", err));
        });
    }

    /* 목록 외부 클릭 시 자동완성 숨기기 */
    document.addEventListener("click", (e) => {
        if (!e.target.closest("#clientAutoList") && e.target.id !== "clientSearch") {
            autoList.classList.add("d-none");
        }
    });

    /* ============================================
       4) 거래처 선택 → 상세정보 자동표시
    ============================================ */
    document.addEventListener("click", (e) => {
        if (!e.target.classList.contains("auto-item")) return;

        const clientId = e.target.dataset.clientId;
        const clientName = e.target.dataset.clientName;

        document.getElementById("clientSearch").value = clientName;
        document.getElementById("clientId").value = clientId;

        autoList.classList.add("d-none");

        fetch(`/sales/client/detail/${clientId}`)
            .then(res => res.json())
            .then(data => {
                document.getElementById("clientInfoBox").classList.remove("d-none");

                document.getElementById("clientCeo").value = data.ceoName ?? "";
                document.getElementById("clientManager").value = data.managerName ?? "";
                document.getElementById("clientManagerTel").value = data.managerTel ?? "";
                document.getElementById("clientBizNo").value = data.businessNo ?? "";
                document.getElementById("clientAddr").value = data.addr ?? "";
                document.getElementById("clientAddrDetail").value = data.addrDetail ?? "";
            })
            .catch(err => console.error("거래처 정보 조회 오류", err));
    });

});

/* ============================================
   거래처 정보 초기화 함수
============================================ */
function resetClientInfo() {
    document.getElementById("clientInfoBox").classList.add("d-none");

    const fields = [
        "clientCeo", "clientManager", "clientManagerTel",
        "clientBizNo", "clientAddr", "clientAddrDetail"
    ];
    fields.forEach(id => document.getElementById(id).value = "");
}

/* ============================================
   카카오 주소 검색
============================================ */
const addrSearchBtn = document.getElementById("addrSearchBtn");

if (addrSearchBtn) {
    addrSearchBtn.addEventListener("click", function () {
        new daum.Postcode({
            oncomplete: function (data) {
                let addr = data.roadAddress ? data.roadAddress : data.jibunAddress;
                document.getElementById("clientAddr").value = addr;
                document.getElementById("clientAddrDetail").focus();
            }
        }).open();
    });
}
