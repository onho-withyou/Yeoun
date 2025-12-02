document.addEventListener("DOMContentLoaded", () => {

    /* ==========================
       1) 제품 목록 추가 버튼
    =========================== */
    const addItemBtn = document.getElementById("addItemBtn");
    if (addItemBtn) {

        const productList = window.productList ?? [];

        addItemBtn.addEventListener("click", () => {
            const tbody = document.getElementById("itemBody");
            if (!tbody) return;   // tbody 없으면 그냥 종료 (에러 방지)

            const row = document.createElement("tr");
            row.innerHTML = `
                <td>
                    <select class="form-select" name="items[][prdId]" required>
                        <option value="">-- 선택 --</option>
                        ${productList.map(p =>
                            `<option value="${p.prdId}">${p.prdName}</option>`
                        ).join("")}
                    </select>
                </td>
                <td><input type="number" class="form-control" name="items[][qty]" min="1" required></td>
                <td><input type="text" class="form-control" name="items[][memo]"></td>
                <td class="text-center">
                    <button type="button" class="btn btn-sm btn-danger delBtn">X</button>
                </td>
            `;

            tbody.appendChild(row);

            row.querySelector(".delBtn").addEventListener("click", () => row.remove());
        });
    }


    /* ==========================
       2) 거래처 자동완성
    =========================== */
    const clientSearch = document.getElementById("clientSearch");
    const autoList     = document.getElementById("clientAutoList");

    if (clientSearch && autoList) {

        clientSearch.addEventListener("input", function () {
            const keyword = this.value.trim();

            if (keyword.length < 1) {
                autoList.innerHTML = "";
                autoList.classList.add("d-none");   // 🔴 숨기기
                return;
            }

            fetch(`/sales/orders/search-customer?keyword=` + encodeURIComponent(keyword))
                .then(r => r.json())
                .then(list => {

                    if (!list || list.length === 0) {
                        autoList.innerHTML = "";
                        autoList.classList.add("d-none");   // 🔴 결과 없으면 숨기기
                        return;
                    }

                    let html = "";
                    list.forEach(c => {
                        html += `
                            <button type="button"
                                    class="list-group-item list-group-item-action auto-item"
                                    data-id="${c.clientId}"
                                    data-name="${c.clientName}">
                                ${c.clientName}
                            </button>
                        `;
                    });
                    autoList.innerHTML = html;
                    autoList.classList.remove("d-none");    // ✅ 여기서 보여주기

                    document.querySelectorAll(".auto-item").forEach(item => {
                        item.addEventListener("click", () => {
                            clientSearch.value = item.dataset.name;
                            document.getElementById("clientId").value = item.dataset.id;
                            autoList.innerHTML = "";
                            autoList.classList.add("d-none"); // 선택 후 다시 숨기기
                        });
                    });
                })
                .catch(err => {
                    console.error("검색 오류", err);
                    autoList.innerHTML = "";
                    autoList.classList.add("d-none");
                });
        });
    }

});
