// 인사 발령 JS

let empGrid = null;
let originalEmpList = [];

// CSRF 토큰 읽기 (전역)
const csrfToken = document.querySelector("meta[name='_csrf_token']")?.content;
const csrfHeader = document.querySelector("meta[name='_csrf_headerName']")?.content;

document.addEventListener('DOMContentLoaded', function() {
	
	// 페이지 로드되면 사원 목록 불러오기
	loadEmpListForHrAction();
	
	// 검색 이벤트
	const btnSearch = document.getElementById('btnSearchEmp');
	if (btnSearch) {
		btnSearch.addEventListener('click', onSearchEmp);
	}
	
	// 부서 선택 바뀌면 바로 필터링
	const selDept = document.getElementById('searchDept');
	if (selDept) {
		selDept.addEventListener('change', onSearchEmp);
	}

	// 직급 선택 바뀌면 바로 필터링
	const selPos = document.getElementById('searchPos');
	if (selPos) {
		selPos.addEventListener('change', onSearchEmp);
	}

	// 이름/사번 입력 후 엔터 누르면 검색
	const inputKeyword = document.getElementById('searchKeyword');
	if (inputKeyword) {
		inputKeyword.addEventListener('input', () => {
			onSearchEmp();
		});
	}
	
	// 발령 등록 버튼 이벤트 - DOMContentLoaded에서 바로 등록!
	const btnSubmit = document.getElementById('btnSubmit');
	if (btnSubmit) {
		btnSubmit.addEventListener('click', handleSubmitAction);
	}
});

// 0. 검색 실행 함수
function onSearchEmp() {
	const dept = document.getElementById('searchDept').value;
	const pos = document.getElementById('searchPos').value;
	const keyword = document.getElementById('searchKeyword').value.trim().toLowerCase();

	let filtered = originalEmpList;

	if (dept) filtered = filtered.filter(emp => emp.deptName === dept);
	if (pos) filtered = filtered.filter(emp => emp.posName === pos);

	if (keyword) {
		filtered = filtered.filter(emp =>
			emp.empName.toLowerCase().includes(keyword) ||
			emp.empId.includes(keyword)
		);
	}

	empGrid.resetData(filtered);
}

// 1. 사원 목록 불러오기
function loadEmpListForHrAction() {
	fetch('/api/hr/employees')
		.then(res => res.json())
		.then(data => {
			originalEmpList = data;
			buildEmpGrid(data);
		})
		.catch(err => {
			console.error('사원 목록 로드 실패:', err);
			alert('사원 목록을 불러오는데 실패했습니다.');
		});
}

// 2. 테이블에 사원 목록 렌더링
function buildEmpGrid(rows) {
	empGrid = new tui.Grid({
		el: document.getElementById('empGrid'),
		data: rows,
		scrollX: false,
		scrollY: true,
		bodyHeight: 500,
		rowHeaders: ['rowNum'],
		columns: [
			{ 
				header: '사번', 
				name: 'empId', 
				align: 'center' 
			},
			{ 
				header: '이름', 
				name: 'empName', 
				align: 'center' 
			},
			{ 
				header: '부서', 
				name: 'deptName', 
				align: 'center'
			},
			{ 
				header: '직급', 
				name: 'posName',
				align: 'center'
			},
			{ 
				header: '연락처', 
				name: 'mobile', 
				align: 'center'
			}
		]
	});

	// 행 클릭 → 오른쪽 정보 세팅
	empGrid.on('click', ev => {
		const row = empGrid.getRow(ev.rowKey);
		if (!row) return;

		document.getElementById('sel-emp-id').textContent = row.empId;
		document.getElementById('sel-emp-name').textContent = row.empName;
		document.getElementById('sel-emp-dept').textContent = row.deptName;
		document.getElementById('sel-emp-pos').textContent = row.posName;

		document.getElementById('empId').value = row.empId;
	});
}

// 3. 발령 등록 처리 함수
function handleSubmitAction(e) {
	e.preventDefault(); // 폼 기본 제출 막기
	
	// DTO 구성
	const dto = {
		empId: document.getElementById("empId").value,
		actionType: document.querySelector("select[name='actionType']").value,
		effectiveDate: document.querySelector("input[name='effectiveDate']").value,
		toDeptId: document.querySelector("select[name='toDeptId']").value,
		toPosCode: document.querySelector("select[name='toPosCode']").value,
		actionReason: document.querySelector("textarea[name='actionReason']").value,
		approverEmpId: document.querySelector("select[name='approverEmpId']").value || null
	};
	
	// 필수값 체크
	if (!dto.empId) {
		alert("사원을 선택하세요!");
		return;
	}
	if (!dto.actionType) {
		alert("발령 구분을 선택하세요!");
		return;
	}
	if (!dto.effectiveDate) {
		alert("발령일자를 입력하세요!");
		return;
	}
	if (!dto.toDeptId) {
		alert("부서를 선택하세요!");
		return;
	}
	if (!dto.toPosCode) {
		alert("직급을 선택하세요!");
		return;
	}
	
	
	// REST API POST
	fetch("/api/hr/actions", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			[csrfHeader]: csrfToken
		},
		body: JSON.stringify(dto)
	})
	.then(response => {
	    if (!response.ok) {
	        throw new Error("서버 오류 발생");
	    }
	    return response.text(); 
	})
	.then(result => {
	    alert("발령 등록이 완료되었습니다!");
	    window.location.href = "/hr/actions";  
	})
	.catch(err => {
	    console.error("발령 등록 실패:", err);
	    alert("발령 등록 중 오류가 발생했습니다.");
	});
}











