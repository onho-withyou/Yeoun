const csrf = document.querySelector('meta[name="_csrf_token"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_headerName"]').getAttribute('content');

// 출/퇴근 버튼 클릭 시 사원번호를 전달해서 출/퇴근 기록 요청
async function attendance(empId) {
	const PROCESS_ATTENDANCE = `/attendance/toggle/${empId}`;
	const response = await fetch(PROCESS_ATTENDANCE, { 
		method: "POST",
		headers: {
			[csrfHeader]: csrf, 
			"Content-Type": "application/json"
		}
	});
	const data = await response.json();
	
	return data; // {success: true, status: status} 로 반환
}

const handleAttendanceToggle = async (empId) => {
	const result =  await attendance(empId);
	const attendanceBtn = document.querySelector("#attendance");
	
	let msg = "";
	
	// attendance함수의 반환값이 data의 status로 알림 변경
	if (result.status === "WORKIN") {
		attendanceBtn.innerText = "퇴근";
		msg = "출근했습니다.";
	} else if (result.status === "WORK_OUT") {
		attendanceBtn.innerText = "출근";
		msg = "퇴근했습니다.";
	} else if (result.status === "LATE") {
		msg = "지각입니다.";
	} else if (result.status === "IN") {
		msg = "복귀합니다.";
	} else {
		msg = "외출입니다.";
	}
	
	// 버튼 활성화/비활성화 적용
	if (result.buttonEnabled === false) {
		attendanceBtn.disabled = true;
	} else {
		attendanceBtn.disabled = false;
	}
	
	alert(msg);
	
	setTimeout(() => {
	    if (result.status === "WORKIN" || result.status === "WORK_OUT" || result.status === "LATE") {
	        location.reload();
	    }
	}, 10); 
	
}

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
		console.error("사원 조회 중 오류 : " , error);
		alert("사원 조회 중 오류가 발생했습니다.");
	}
}

let currentMode = "regist";
let currentAttendanceId = null;

// 출퇴근 수기 등록 및 수정 모달
const openModalAttendance = async (mode, attendanceId = null) => {
	const modalTitle = document.querySelector("#modalCenterTitle");
	const saveBtn = document.querySelector("#saveBtn");
	const modalElement = document.querySelector("#modalCenter");
	const modalInstance = new bootstrap.Modal(modalElement);
	
	currentMode = mode; // 현재 모드 저장
	currentAttendanceId = attendanceId; // 수정 모드일 경우 id가 들어와서 저장
	
	const ATTENDANCE_DETAIL_URL = `/attendance/${attendanceId}`;
	
	if (mode === "edit" && attendanceId) { 	// 수정 버튼 클릭 시 동작
		modalTitle.textContent = "출/퇴근 수정";
		saveBtn.textContent = "수정";
		
		
		// 선택한 데이터 불러오기
		const response = await fetch(ATTENDANCE_DETAIL_URL);
		const data = await response.json();
		
		document.querySelector("#nameWithTitle").value = data.empId;
		
		// 사원번호로 이름 조회 로직
		await searchEmp();
		
		if (data.workIn != null) {
			document.querySelector("#inTime").value = data.workIn.slice(0, 5);
		}
		
		if (data.workOut != null) {
			document.querySelector("#endTime").value = data.workOut.slice(0, 5);
		}
		
		document.querySelector("select[name='statusCode']").value = data.statusCode;
	} else { // 등록 모드
		modalTitle.textContent = "출/퇴근 등록";
		saveBtn.textContent = "등록";
		resetModal(); // 모달 초기화
	}
	modalInstance.show();
}


// 등록 및 수정 공용 함수 
const saveAttendance = async () => {
	const empId = document.querySelector("#nameWithTitle").value;
	const workIn = document.querySelector("#inTime").value;
	const workOut = document.querySelector("#endTime").value;
	const statusCode = document.querySelector("select[name='statusCode']").value;
	
	const url = currentMode === "edit" ? `/attendance/${currentAttendanceId}` : "/attendance";
	const method = currentMode === "edit" ? "PATCH" : "POST";
	
	try {
		const response = await fetch(url, {
			method,
			headers: {
				[csrfHeader]: csrf, 
				"Content-Type": "application/json"
			},
			body: JSON.stringify({empId, workIn, workOut, statusCode}),
		});
		
		if (!response.ok) {
			const errorData = await response.json();
			throw new Error(errorData.message || "요청 처리 중 오류가 발생했습니다.");
		}
		
		// 정상 응답일 경우
		const result = await response.json();
		alert(result.message || "정상적으로 처리되었습니다.");
	
		// fetch 응답 전 리로드 되지 않도록 약 0.3초 지연
		setTimeout(() => {
			location.reload();
		}, 300);
	} catch (error) {
		console.error("에러 : " + error);
		alert(error.message || "서버와 통신 중 오류가 발생했습니다.");
	}
}

// 모달 초기화
function resetModal() {
	document.querySelector("#nameWithTitle").value = "";
	document.querySelector("#empName").value = "";
	document.querySelector("#inTime").value = "";
	document.querySelector("#endTime").value = "";
	document.querySelector("select[name='statusCode']").value = "";
}

// -----------------------------------------------------
// 외근 등록 유효성 검사
const form = document.querySelector("#outworkForm");
const dateInput = document.querySelector("#inTime");
const typeSelect = document.querySelector("select[name='accessType']");
const outTimeInput = document.querySelector("#outTime");
const reasonTextarea = document.querySelector("#reason");

form.addEventListener("submit", (event) => {
	// 근무날짜 미선택 시 전송 안됨
	if (!dateInput.value) {
		event.preventDefault();
		dateInput.classList.add("is-invalid");
	}
	
	// 근무유형 미선택 시 전송 안됨
	if (!typeSelect.value) {
		event.preventDefault();
		typeSelect.classList.add("is-invalid");
	}
	
	// 외근 시작 시간 미입력 했을 경우 전송 안됨
	if (!outTimeInput.value) {
		event.preventDefault();
		outTimeInput.classList.add("is-invalid");
	}
	
	// 외근 사유 2글자 미만일 경우 전송 안됨
	if (reasonTextarea.value.trim().length < 2) {
		event.preventDefault();
		reasonTextarea.classList.add("is-invalid");
	}
});

// 근무 날짜 입력 또는 선택 시 에러 문구 삭제
dateInput.addEventListener("change", () => {
	if (dateInput.value) {
		dateInput.classList.remove("is-invalid");
	}
});

// 근무유형을 선택했을 경우 에러 문구 삭제
typeSelect.addEventListener("change", () => {
	if (typeSelect.value) {
		typeSelect.classList.remove("is-invalid");
	}
});

// 외근 사유 2글자 이상 입력 시 에러 문구 삭제
reasonTextarea.addEventListener("input", () => {
	if (reasonTextarea.value.trim().length >= 2) {
		reasonTextarea.classList.remove("is-invalid");
	}
});

// 외근 시작 시간 입력 시 에러 문구 삭제됨
outTimeInput.addEventListener("input", () => {
	if (outTimeInput.value) {
		outTimeInput.classList.remove("is-invalid");
	}
});
