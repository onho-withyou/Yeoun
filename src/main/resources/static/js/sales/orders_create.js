document.addEventListener("DOMContentLoaded", () => {

  /* ============================================
     ✅ 수주일자 / 납기일자 즉시 검증 + 버튼 비활성화
  ============================================ */
  const form = document.getElementById("orderForm");
  const btnSave = document.getElementById("btnSaveOrder");

  const orderDateInput = document.getElementById("orderDate");
  const deliveryDateInput = document.getElementById("deliveryDate");
  const deliveryErr = document.getElementById("deliveryDateError");

  function showDeliveryError(msg) {
    if (!deliveryErr) return;
    if (!msg) {
      deliveryErr.classList.add("d-none");
      deliveryErr.textContent = "";
    } else {
      deliveryErr.classList.remove("d-none");
      deliveryErr.textContent = msg;
    }
  }

  function setSaveEnabled(enabled) {
    if (!btnSave) return;
    btnSave.disabled = !enabled;
  }

  // 영업일 더하기(주말 제외)
  function addBusinessDays(startDate, days) {
    const date = new Date(startDate);
    let added = 0;
    while (added < days) {
      date.setDate(date.getDate() + 1);
      const day = date.getDay();
      if (day !== 0 && day !== 6) added++;
    }
    return date;
  }

  // ✅ 납기일 검증(버튼 잠금까지)
  function validateDeliveryDate() {
    if (!orderDateInput || !deliveryDateInput) return true;

    // 납기일 미선택 → 저장 막기
    if (!deliveryDateInput.value) {
      showDeliveryError("납기일자를 선택하세요.");
      setSaveEnabled(false);
      return false;
    }

    const orderDate = new Date(orderDateInput.value);
    const deliveryDate = new Date(deliveryDateInput.value);

    // 주말 금지
    const day = deliveryDate.getDay();
    if (day === 0 || day === 6) {
      showDeliveryError("납기일은 평일만 선택 가능합니다.");
      setSaveEnabled(false);
      return false;
    }

    // 최소 5영업일 이후
    const minDate = addBusinessDays(orderDate, 5);
    // 시간값 때문에 하루 밀리는 거 방지 (날짜만 비교)
    minDate.setHours(0,0,0,0);
    deliveryDate.setHours(0,0,0,0);

    if (deliveryDate < minDate) {
      showDeliveryError("납기일은 평일 기준 최소 5영업일 이후여야 합니다.");
      setSaveEnabled(false);
      return false;
    }

    // 통과
    showDeliveryError("");
    setSaveEnabled(true);
    return true;
  }

  if (orderDateInput && deliveryDateInput) {
    const today = new Date();
    const todayStr = today.toISOString().split("T")[0];

    // 수주일자 오늘 고정
    orderDateInput.value = todayStr;
    orderDateInput.min = todayStr;
    orderDateInput.readOnly = true;

    // 납기일 min 세팅(5영업일)
    const minDeliveryDate = addBusinessDays(today, 5);
    const minDeliveryStr = minDeliveryDate.toISOString().split("T")[0];
    deliveryDateInput.min = minDeliveryStr;

    // ✅ 입력 즉시 검증 (선택/직접입력 둘 다 커버)
    deliveryDateInput.addEventListener("input", validateDeliveryDate);
    deliveryDateInput.addEventListener("change", validateDeliveryDate);

    // 처음 로딩 시에도 버튼 잠그기
    validateDeliveryDate();
  }

  // ✅ 마지막 방어: 혹시 버튼이 눌려도 submit 못하게
  if (form) {
    form.addEventListener("submit", (e) => {
      if (!validateDeliveryDate()) {
        e.preventDefault();
        e.stopPropagation();
      }
    });
  }

    /* ============================================
       1) 제품 목록 추가 버튼
    ============================================ */
	
	addItemBtn.addEventListener("click", () => {

	    const tbody = document.getElementById("itemBody");
	    const index = tbody.querySelectorAll("tr").length;

	    const row = document.createElement("tr");
	    row.innerHTML = `
	        <td>
	            <select class="form-select prd-select"
	                    name="items[${index}][prdId]" required>
	                <option value="">-- 선택 --</option>
	                ${productList.map(p =>
	                    `<option value="${p.prdId}"
	                             data-price="${p.unitPrice}"
	                             data-minqty="${p.minQty}"
	                             data-unit="${p.prdUnit}">
	                        ${p.prdName}
	                    </option>`).join("")}
	            </select>
	        </td>

	        <td>
	            <input type="number" class="form-control price-input"
	                   name="items[${index}][unitPrice]" readonly>
	        </td>

	        <td>
	            <input type="number" class="form-control minqty-input"
	                   name="items[${index}][minQty]" readonly>
	        </td>

	        <td>
	            <input type="text" class="form-control unit-input"
	                   name="items[${index}][unit]" readonly>
	        </td>

	        <td>
	            <input type="number" class="form-control qty-input"
	                   name="items[${index}][qty]" required>
	        </td>

	        <td>
	            <input type="number" class="form-control amount-input"
	                   name="items[${index}][amount]" readonly>
	        </td>

	        <td class="text-center">
	            <button type="button" class="btn btn-sm btn-danger delBtn">X</button>
	        </td>
	    `;

	    tbody.appendChild(row);

	    row.querySelector(".delBtn").addEventListener("click", () => row.remove());

	    // 요소 참조
	    const prdSelect    = row.querySelector(".prd-select");
	    const priceInput   = row.querySelector(".price-input");
	    const minQtyInput  = row.querySelector(".minqty-input");
	    const unitInput    = row.querySelector(".unit-input");
	    const qtyInput     = row.querySelector(".qty-input");
	    const amountInput  = row.querySelector(".amount-input");

	    /* 제품 선택 → 자동 입력 */
	    prdSelect.addEventListener("change", () => {
	        const opt = prdSelect.selectedOptions[0];

	        const unitPrice = parseInt(opt.dataset.price) || 0;
	        const minQty    = parseInt(opt.dataset.minqty) || 0;
	        const unit      = opt.dataset.unit ?? "";

	        priceInput.value  = unitPrice;
	        minQtyInput.value = minQty;
	        unitInput.value   = unit;

	        let qty = parseInt(qtyInput.value) || 0;

	        if (qty < minQty) qty = minQty;
	        if (qty % 10 !== 0) qty = Math.ceil(qty / 10) * 10;

	        qtyInput.value = qty;
	        amountInput.value = qty * unitPrice;
	    });

	    /* 수량 입력 */
	    qtyInput.addEventListener("input", () => {

	        let qty = parseInt(qtyInput.value) || 0;
	        const minQty = parseInt(minQtyInput.value) || 0;

	        if (qty < minQty) qty = minQty;
	        if (qty % 10 !== 0) qty = Math.ceil(qty / 10) * 10;

	        qtyInput.value = qty;
	        amountInput.value = qty * (parseInt(priceInput.value) || 0);
	    });
	});


    /* ============================================
       2) 거래처 자동완성 검색
    ============================================ */
    const clientSearch = document.getElementById("clientSearch");
    const autoList = document.getElementById("clientAutoList");

    if (clientSearch && autoList) {
        clientSearch.addEventListener("input", () => {
            const keyword = clientSearch.value.trim();

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

    /* 외부 클릭 시 자동완성 닫기 */
    document.addEventListener("click", (e) => {
        if (!e.target.closest("#clientAutoList") && e.target.id !== "clientSearch") {
            autoList.classList.add("d-none");
        }
    });


    /* ============================================
       3) 거래처 선택시 detail 자동 세팅
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

                document.getElementById("clientCeo").value         = data.ceoName ?? "";
                document.getElementById("clientManager").value     = data.managerName ?? "";
                document.getElementById("clientManagerTel").value  = data.managerTel ?? "";
                document.getElementById("clientManagerEmail").value = data.managerEmail ?? "";
                document.getElementById("clientBizNo").value       = data.businessNo ?? "";

				document.getElementById("clientPostcode").value =
				    data.postCode ?? data.dPostcode ?? data.postcode ?? data.zonecode ?? "";
                document.getElementById("clientAddr").value          = data.addr ?? "";
                document.getElementById("clientAddrDetail").value    = data.addrDetail ?? "";
            })
            .catch(err => console.error("거래처 정보 조회 오류", err));
    });


/* ============================================
   거래처 정보 초기화
============================================ */
function resetClientInfo() {
    document.getElementById("clientInfoBox").classList.add("d-none");

    const fields = [
        "clientCeo", "clientManager", "clientManagerTel",
        "clientManagerEmail",
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

		        // 🔥 새 주소 → 기존 주소 덮어쓰기
		        document.getElementById("clientPostcode").value = data.zonecode;
		        document.getElementById("clientAddr").value = addr;

		        // 🔥 기존 상세주소는 유지되지 않음 → 새 주소니까 다시 입력해야 함
		        document.getElementById("clientAddrDetail").value = "";

		        document.getElementById("clientAddrDetail").focus();
		    }
		}).open();

    });
}
//영업일 계산
function addBusinessDays(startDate, days) {
    const date = new Date(startDate);
    let added = 0;

    while (added < days) {
        date.setDate(date.getDate() + 1);
        const day = date.getDay();
        if (day !== 0 && day !== 6) {
            added++;
        }
    }
    return date;
}
});
