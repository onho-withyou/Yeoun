const productGrid = new tui.Grid({
	el: document.getElementById("productGrid"),
	rowHeaders: ['rowNum'],
	columns: [
		{
			header: "출고번호",
			name: "outboundId",
		},
		{
			header: "거래처명",
			name: "clientName",
		},
		{
			header: "출고예정일",
			name: "startDate",
			formatter: ({value}) => formatDate(value)
		},
		{
			header: "출고일",
			name: "outboundDate",
			formatter: ({value}) => formatDate(value)
		},
		{
			header: "상태",
			name: "status",
			filter: "select"
		},
		{
			header: " ",
			name: "btn",
			formatter: (rowInfo) => {
				return `<button class="btn btn-primary btn-sm" data-id="${rowInfo.row.id}">상세</button>`
			}
		}
	]
});

// 상세 버튼 클릭 동작
productGrid.on("click", (ev) => {
	const { columnName, rowKey } = ev;
	
	if (columnName === "btn") {
		const row = productGrid.getRow(rowKey);
		console.log(row);
		// 입고 상세 페이지로 이동
		location.href = `/inventory/outbound/prd/${row.outboundId}`
	}
});

const prdStartDateInput = document.querySelector("#prdStartDate");
const prdEndDateInput = document.querySelector("#prdEndDate");

// 날짜 포맷 함수
function formatDate(isoDate) {
	if (!isoDate) return "";
	
	return isoDate.split("T")[0]; // YYYY-MM-dd 형식
}

// 원재료 정보 불러오기
async function loadProductOutbound(startDate, endDate, keyword) {
	const MATERIAL_OUTBOUND_LIST = 
		`/inventory/outbound/list/data` +
		`?startDate=${startDate}` +
		`&endDate=${endDate}` +
		`&keyword=${keyword}`;
			
	try {
		const res = await fetch(MATERIAL_OUTBOUND_LIST, {method: "GET"});
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패!");
		}
		
		const data = await res.json();
		
		let prdOutboundList  = data.filter(item => item.shipmentId != null);
		
		console.log(prdOutboundList );
		
		// 데이터가 없을 경우 빈배열 반환
		if (!prdOutboundList  || prdOutboundList .length === 0) {
			productGrid.resetData([]);
		}
		
		const statusMap = {
			WAITING : "출고대기",
			COMPLETED: "출고완료"
		}
		
		// 상태값이 영어로 들어오는 것을 한글로 변환해서 기존 data에 덮어씌움
		prdOutboundList  = prdOutboundList .map(item => ({
			...item,
			status: statusMap[item.status] || item.status
		}));
		
		productGrid.resetData(prdOutboundList );
		
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
	prdStartDateInput.value = startDate;
	prdEndDateInput.value = endDate;
	
	await loadProductOutbound(startDate, endDate, null);
});

// 검색
document.querySelector("#search").addEventListener("click", async () => {
	const startDate = prdStartDateInput.value;
	const endDate = prdEndDateInput.value;
	const keyword = document.querySelector("#productKeyword").value;
	
	if (!startDate || !endDate) {
		alert("조회할 기간을 선택해주세요!");
		return;
	}
	
	await loadProductOutbound(startDate, endDate, keyword);
});

// 시작날짜 클릭 시 데이터 조회
document.querySelector("#prdStartDate").addEventListener("input", async () => {
	const startDate = prdStartDateInput.value;
	const endDate = prdEndDateInput.value;
	const keyword = document.querySelector("#productKeyword").value;
	
	await loadProductOutbound(startDate, endDate, keyword);
});

// 종료날짜 클릭 시 데이터 조회
document.querySelector("#prdEndDate").addEventListener("input", async () => {
	const startDate = prdStartDateInput.value;
	const endDate = prdEndDateInput.value;
	const keyword = document.querySelector("#productKeyword").value;
	
	await loadProductOutbound(startDate, endDate, keyword);
});


// =============================================================
// 출고 등록 로직
const shipmentSelect = document.querySelector("#shipmnetSelect");
const processByName = document.querySelector("#managerName");
const prdName = document.querySelector("#productName");
const expectDate = document.querySelector("#expectDate");
const shipTbody = document.querySelector("#shipTbody");

// 출하지시서 데이터 저장
let shipmentList = [];

// 출하지시서 정보 가져오기
async function loadShipmnetList() {
	try {
		
		// 출하지시서 데이터 가져오는 API 작성하기
		
	} catch (error) {
		console.error(error);
		return [];
	}
	
}

// 출하지시서 선택 이벤트
shipmentSelect.addEventListener("focus", async () => {
	
});