// 창고 ZONE, RACK, ROW, COL 가져오기
async function getLocationInfo() {
	const response = 
		await fetch('/api/inventories/locations', {
			method: 'GET',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			}
		});
		
		if (!response.ok) {
			throw new Error('창고정보를 가져올 수 없습니다.')
		}
		return await response.json();
}

// 창고위치 저장할 변수
let locationInfo = [];

// 같은 index에 있는 select 한번에 찾기
function getRowSelects(index) {
	return {
		zone: document.querySelector(`.moveZone[data-index="${index}"]`), 
		rack: document.querySelector(`.moveRack[data-index="${index}"]`),
		row: document.querySelector(`.moveRow[data-index="${index}"]`),
		col: document.querySelector(`.moveColumn[data-index="${index}"]`)
	}
}


// 출고 등록 버튼 이벤트
document.getElementById("completeOutboundBtn").addEventListener("click", async () => {
	const items = [];
	
	const rows = document.querySelectorAll("tbody tr");
	
	// 품목들의 데이터를 반복문을 통해서 items 배열 안에 담음
	rows.forEach((row, index) => {
		const matId = row.querySelector(".matId")?.textContent.trim();
		const lotNo = row.querySelector(".lotNo")?.textContent.trim();
		const outboundQty = Number(row.querySelector(`.outboundQty[data-index="${index}"]`)?.textContent.trim());
		const ivId = row.querySelector(".ivId")?.value;
		
		
		items.push({
			matId,
			lotNo,
			outboundQty,
			ivId
		})
	})
	
	
	
	
	const inboundId = document.querySelector("#outboundId").value;
	
	const res = await fetch("/inventory/outbound/mat/complete", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			[csrfHeader]: csrfToken
		},
		body: JSON.stringify({ 
			inboundId,
			items
		})
	});
	
	if (!res.ok) {
		alert("출고 등록을 실패했습니다.");
		return;
	}
	
	const data = await res.json();
	
	if (data.success) {
		alert("출고가 완료되었습니다.");
		setTimeout(() => {
			window.location.href = "/inventory/outbound/list";
		}, 10); 
	}
	
});