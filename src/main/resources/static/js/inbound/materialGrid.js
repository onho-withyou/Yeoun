const grid = new tui.Grid({
	el: document.getElementById("materialGrid"),
	columns: [
		{
			header: "입고번호",
			name: "inboundId",
		},
		{
			header: "회사명",
			name: "clientName",
		},
		{
			header: "담당자",
			name: "clientName",
		},
		{
			header: "입고예정일",
			name: "expectArrivalDate",
		},
		{
			header: "상태",
			name: "inboundStatus",
		},
		{
			header: "",
			name: "",
			formatter: (rowInfo) => {
				return `<button class="btn btn-primary btn-sm" data-id="${rowInfo.row.id}">상세</button>`
			}
		}
	]
});

let keyword = null;

// 원재료 정보 불러오기
async function loadMaterialInbound(startDate, endDate, keyword) {
	const MATERIAL_INBOUND_LIST = `/inventory/inbound/materialList/data?startDate=${startDate}&endDate=${endDate}&keyword=${keyword}`
	try {
		const res = await fetch(MATERIAL_INBOUND_LIST, {method: "GET"});
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패!");
		}
		
		let data = await res.json();
		
		// 데이터가 없을 경우 빈배열 반환
		if (!data || data.length === 0) {
			grid.resetData([]);
		}
		
		grid.resetData(data);
		
	} catch (error) {
		console.error(error);
	}
}

document.addEventListener("DOMContentLoaded", async () => {
	// 오늘 날짜 구하기
	const today = new Date();
	const year = today.getFullYear();
	const month = today.getMonth() + 1;
	const day = today.getDate();
	
	// 이번 달 1일과 오늘 날짜 계산
	const startDate = `${year}-${String(month).padStart(2, "0")}-01`;
	const endDate = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
	
	// 날짜 input 기본값 설정
	document.querySelector("#startDate").value = startDate;
	document.querySelector("#endDate").value = endDate;
	
	await loadMaterialInbound(startDate, endDate, null);
});

// 날짜 조회 후 데이터 가져오기
document.querySelector("#searchbtn").addEventListener("click", async () => {
	const startDate = document.querySelector("#startDate").value;
	const endDate = document.querySelector("#endDate").value;
	
	if (!startDate || !endDate) {
		alert("조회할 기간을 선택해주세요!");
		return;
	}
	
	await loadMaterialInbound(startDate, endDate, null);
});