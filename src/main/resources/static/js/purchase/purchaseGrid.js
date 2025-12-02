const clientInput = document.querySelector("#clientSearch"); // 거래처명 입력하는 input 태그
const clientResultBox = document.querySelector("#clientResult"); // 거래처명 검색하고 나오는 리스트 ul 태그
const itemSelect = document.querySelector("#itemSelect"); // 발주 품목 선택할 select 태그
const dueDateInput = document.querySelector("#dueDate"); // 입고예정일 날짜 input 태그
const orderTableBody = document.querySelector("#orderTable tbody"); // 선택한 품목이 등록될 table 태그
const hiddenId = document.querySelector("#clientId");

// 거래처 검색 더미 데이터
const dummyClients = [
    {
        clientId: "C001",
        clientName: "삼성상사",
        managerName: "김영업",
        items: [
            {
                itemId: "I001",
                itemName: "원두 1kg",
                supplyYn: "Y",
                minOrder: 10,
                unitPrice: 15000,
                orderUnit: 10,
                supplyUnit: "KG",
                leadTime: 3
            },
            {
                itemId: "I002",
                itemName: "컵 12oz",
                supplyYn: "Y",
                minOrder: 50,
                unitPrice: 120,
                orderUnit: 50,
                supplyUnit: "EA",
                leadTime: 2
            }
        ]
    },
    {
        clientId: "C002",
        clientName: "현대무역",
        managerName: "박상무",
        items: [
            {
                itemId: "I003",
                itemName: "빨대",
                supplyYn: "N", // 공급 불가 → 화면에서는 제외해야 함
                minOrder: 100,
                unitPrice: 10,
                orderUnit: 100,
                supplyUnit: "EA",
                leadTime: 1
            },
            {
                itemId: "I004",
                itemName: "포장지",
                supplyYn: "Y",
                minOrder: 50,
                unitPrice: 40,
                orderUnit: 50,
                supplyUnit: "EA",
                leadTime: 4
            }
        ]
    },
    {
        clientId: "C003",
        clientName: "롯데상회",
        managerName: "이유통",
        items: [
            {
                itemId: "I005",
                itemName: "설탕(대용량)",
                supplyYn: "Y",
                minOrder: 20,
                unitPrice: 7000,
                orderUnit: 20,
                supplyUnit: "KG",
                leadTime: 2
            }
        ]
    }
];

// 거래처 검색 이벤트
clientInput.addEventListener("input", async function() {
	// input에 입력한 검색어
	const keyword = clientInput.value.trim().toLowerCase();
	
	// 검색한게 없을 경우 리스트 보이지 않음
	if (keyword.length === 0) {
		clientResultBox.classList.add("d-none");
		return;
	}
	
	// 더미 데이터에서 검색
	const filtered = dummyClients.filter(item => 
		item.clientName.toLowerCase().includes(keyword)
	);
	
	clientResultBox.innerHTML = "";
	
	// 검색 결과가 없을 경우 리스트 보이지 않음
	if (filtered.length === 0) {
		clientResultBox.classList.add("d-none");
		return;
	}
	
	// 검색된 결과들을 하나씩 꺼내서 리스트에 추가하는 로직
	filtered.forEach(item => {
		// 검색된 결과를 저장할 li 태그 생성
		const li = document.createElement("li");
		
		li.className = "list-group-item list-group-item-action";
		li.textContent = item.clientName;
		
		// 검색 후 나오는 리스트에서 거래처 이름 클릭 이벤트
		li.addEventListener("click", () => {
			clientInput.value = item.clientName;
			hiddenId.value = item.clientId;
			clientResultBox.classList.add("d-none");
			
			// 선택된 거래처 객체 찾기
			const selectedClient = dummyClients.find(client => client.clientId === item.clientId);
			
			// 담당자 이름 설정
			document.querySelector("#managerName").value = selectedClient.managerName;
			
			// 공급품목 중 공급가능여부가 Y인 것만 표시
			const availableItems = selectedClient.items.filter(item => item.supplyYn === "Y");
			
			itemSelect.innerHTML = `<option value="">품목 선택</option>`;
			
			availableItems.forEach(item => {
				const option = document.createElement("option");
				
				option.value = item.itemId;
				option.textContent = item.itemName;
				option.dataset.minOrder = item.minOrder;
				option.dataset.unitPrice  = item.unitPrice;
				option.dataset.orderUnit  = item.orderUnit;
				option.dataset.supplyUnit  = item.supplyUnit;
				option.dataset.leadTime   = item.leadTime;
				
				itemSelect.appendChild(option);
			});
			
			itemSelect.disabled = false;
		});
		
		// 검색 결과 보여주는 곳에 검색된 결과 추가
		clientResultBox.appendChild(li);
	});
	
	clientResultBox.classList.remove("d-none");
});

// 품목 선택 후 테이블 추가 및 납기일 계산 
itemSelect.addEventListener("change", () => {
	const optoin = itemSelect.options[itemSelect.selectedIndex];
	
	if (!optoin.value) return;
	
	// 선택한 품목의 리드타임 적용(오늘 날짜 + 리드타임)
	const leadTime = parseInt(option.dataset.leadTime, 10);
	const today = new Date();
	today.setDate(today.getDate() + leadTime);
	
	const yyyy = today.getFullYear();
	const mm = String(today.getMonth() + 1).padStart(2, "0");
	const dd = String(today.getDate()).padStart(2, "0");
	
	dueDateInput.value = `${yyyy}-${mm}-${dd}`;
	
	// 테이블 행 추가
	const itemName = optoin.textContent;
	const minOrder = parseInt(option.dataset.minOrder);
	const unitPrice = parseInt(option.dataset.unitPrice);
	
});











