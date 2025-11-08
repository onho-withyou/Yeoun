// 출/퇴근 버튼 클릭 시 사원번호를 전달해서 출/퇴근 기록 요청
async function attendance(empId) {
	const PROCESS_ATTENDANCE = `/attendance/toggle/${empId}`;
	const response = await fetch(PROCESS_ATTENDANCE, { method: "POST"});
	const data = await response.json();
	
	return data; // {success: true, status: status} 로 반환
}

const handleAttendanceToggle = async () => {
	const result =  await attendance(1);
	
	let msg = "";
	
	// attendance함수의 반환값이 data의 status로 알림 변경
	if (result.status === "IN") {
		document.querySelector("#attendance").innerText = "퇴근";
		msg = "출근했습니다.";
	} else if (result.status === "OUT") {
		document.querySelector("#attendance").innerText = "출근";
		msg = "퇴근했습니다.";
	} else if (result.status === "ALREADY_OUT") {
		msg = "이미 퇴근 처리된 상태입니다.";
	}
	
	alert(msg);
}

// 2511584
// 사원번호로 사원 조회

const searchEmp = async () => {
	const empId = document.querySelector("#nameWithTitle").value;
	const empName = document.querySelector("#empName");
	
	const SEARCH_EMP = `/attendance/search?empId=${empId}`;
	
	try {
		const response = await fetch(SEARCH_EMP, {method: "GET"});
		
		if (!response.ok) {
			const errorData = await response.json;
			alert(errorData.message || "사원 정보를 찾을 수 없습니다.");
			return;
		}		
		
		const data = await response.json();

		if (data) {
			empName.value = data.empName;
		}
	} catch (error) {
		console.erro("사원 조회 중 오류 : " , error);
		alert("사원 조회 중 오류가 발생했습니다.");
	}
}

