document.addEventListener("DOMContentLoaded", () => {

    /* ============================================
       1) 제품 목록 추가
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
                    <select class="form-select" name="items[][prdId]" required>
                        <option value="">-- 선택 --</option>
                        ${productList
                            .map(p => `<option value="${p.prdId}">${p.prdName}</option>`)
                            .join("")}
                    </select>
                </td>
                <td>
                    <input type="number" class="form-control" name="items[][qty]" min="1" required>
                </td>
                <td>
                    <input type="text" class="form-control" name="items[][memo]">
                </td>
                <td class="text-center">
                    <button type="button" class="btn btn-sm btn-danger delBtn">X</button>
                </td>
            `;

            tbody.appendChild(row);

            row.querySelector(".delBtn").addEventListener("click", () => row.remove());
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

                    autoList.innerHTML = list
                        .map(c => `
                            <button type="button"
                                class="list-group-item list-group-item-action auto-item"
                                data-client-id="${c.clientId}"
                                data-client-name="${c.clientName}">
                                ${c.clientName}
                            </button>
                        `)
                        .join("");

                    autoList.classList.remove("d-none");
                })
                .catch(err => console.error("검색 오류", err));
        });
    }

    /* ============================================
       3) 목록 외부 클릭 시 자동완성 숨기기
    ============================================ */
    document.addEventListener("click", (e) => {
        if (!e.target.closest("#clientAutoList") &&
            e.target.id !== "clientSearch") {
            autoList.classList.add("d-none");
        }
    });

    /* ============================================
       4) 자동완성 항목 클릭 → 상세조회 + 정보 표시
    ============================================ */
    document.addEventListener("click", (e) => {
        if (!e.target.classList.contains("auto-item")) return;

        const clientId = e.target.dataset.clientId;
        const clientName = e.target.dataset.clientName;

        // 선택값 입력
        document.getElementById("clientSearch").value = clientName;
        document.getElementById("clientId").value = clientId;

        // 목록 숨기기
        autoList.classList.add("d-none");

        // 상세정보 조회
        fetch(`/sales/client/detail/${clientId}`)
            .then(res => res.json())
            .then(data => {
                const infoBox = document.getElementById("clientInfoBox");
                infoBox.classList.remove("d-none");

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
