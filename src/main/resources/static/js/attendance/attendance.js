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
