const grid = new tui.Grid({
	el: document.getElementById("grid"),
	columns: [
		{
			header: "부서",
			name : "deptName",
			sortable: true,
			filter: { type: 'text', showApplyBtn: true, showClearBtn: true }
		},
		{
			header: "직급",
			name : "posName",
			filter: { type: 'text', showApplyBtn: true, showClearBtn: true }
		},
		{
			header: "사원번호",
			name : "empId",
			sortable: true,
			filter: {
				type: 'date', options: {format: 'yyyy-MM-dd'}
			}
		},
		{
			header: "이름",
			name : "empName",
			sortable: true,
		},
		{
			header: "총연차",
			name : "totalDays"
		},
		{
			header: "사용연차",
			name : "usedDays"
		},
		{
			header: "잔여연차",
			name : "remainDays",
		},
		{
			header: "",
			name : "",
			formatter: (rowInfo) => {
				return  `<button class="btn btn-primary" data-id="${rowInfo.row.id}">수정</button>`
			}
		},
	],
		
});

// 데이터 가져오기
async function loadLeaveList(empId = null) {
	// 사원번호 검색 여부에 따라 쿼리파라미터 다르게 보냄
	const LEAVE_LIST = empId
	    ? `/leave/list/data?empId=${empId}`
	    : `/leave/list/data`;
		
	try {
		const res = await fetch(LEAVE_LIST, {method: "GET"});
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패!");
		}
		
		let data = await res.json();
		
		grid.resetData(data);
	} catch(error) {
		console.error(error);
		alert("데이터를 불러오는 중 오류가 발생했습니다.");	
	}
}

// 사원번호 조회 버튼 클릭 이벤트
document.querySelector("#searchBtn").addEventListener("click", () => {
	// 입력한 사원 번호
	let empId = document.querySelector("#searchId").value.trim();
	
	if (empId) {
		loadLeaveList(empId);
	} else {
		loadLeaveList();
	}
})

// 페이지 로드 시 전체 조회
loadLeaveList();