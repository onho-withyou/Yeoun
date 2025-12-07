const grid = new tui.Grid({
	el: document.getElementById("materialGrid"),
	rowHeaders: ['rowNum'],
	columns: [
		{
			header: "출고번호",
			name: "outboundId",
		},
		{
			header: "담당자",
			name: "processEmpName",
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
grid.on("click", (ev) => {
	const { columnName, rowKey } = ev;
	
	if (columnName === "btn") {
		const row = grid.getRow(rowKey);
		console.log(row);
		// 입고 상세 페이지로 이동
		location.href = `/inventory/outbound/mat/${row.outboundId}`
	}
});

const startDateInput = document.querySelector("#startDate");
const endDateInput = document.querySelector("#endDate");

// 날짜 포맷 함수
function formatDate(isoDate) {
	if (!isoDate) return "";
	
	return isoDate.split("T")[0]; // YYYY-MM-dd 형식
}

// 원재료 정보 불러오기
async function loadMaterialOutbound(startDate, endDate, keyword) {
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
		
		let data = await res.json();
		
		console.log(data);
		
		// 데이터가 없을 경우 빈배열 반환
		if (!data || data.length === 0) {
			grid.resetData([]);
		}
		
		const statusMap = {
			WAITING : "출고대기",
			COMPLETED: "출고완료"
		}
		
		// 상태값이 영어로 들어오는 것을 한글로 변환해서 기존 data에 덮어씌움
		data = data.map(item => ({
			...item,
			status: statusMap[item.status] || item.status
		}));
		
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
	startDateInput.value = startDate;
	endDateInput.value = endDate;
	
	await loadMaterialOutbound(startDate, endDate, null);
});

// 검색
document.querySelector("#searchbtn").addEventListener("click", async () => {
	const startDate = startDateInput.value;
	const endDate = endDateInput.value;
	const keyword = document.querySelector("#materialKeyword").value;
	
	if (!startDate || !endDate) {
		alert("조회할 기간을 선택해주세요!");
		return;
	}
	
	await loadMaterialOutbound(startDate, endDate, keyword);
});

// 시작날짜 클릭 시 데이터 조회
document.querySelector("#startDate").addEventListener("input", async () => {
	const startDate = startDateInput.value;
	const endDate = endDateInput.value;
	const keyword = document.querySelector("#materialKeyword").value;
	const searchType = document.querySelector("select[name='searchType']").value;
	
	await loadMaterialOutbound(startDate, endDate, keyword);
});

// 종료날짜 클릭 시 데이터 조회
document.querySelector("#endDate").addEventListener("input", async () => {
	const startDate = startDateInput.value;
	const endDate = endDateInput.value;
	const keyword = document.querySelector("#materialKeyword").value;
	const searchType = document.querySelector("select[name='searchType']").value;
	
	await loadMaterialOutbound(startDate, endDate, keyword);
});
