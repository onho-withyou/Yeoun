// 인사 발령 JS

let originalEmpList = [];

document.addEventListener('DOMContentLoaded', function() {
	// 페이지 로드되면 사원 목록 불러오기
	loadEmpListForAction();
	
	// 검색 버튼
	document.getElementById('btnSearchEmp')
		.addEventListener('click', onSearchEmp);
});

// 0. 검색 실행 함수
function onSearchEmp() {
	
	const dept = document.getElementById('searchDept').value;
	const pos = document.getElementById('searchPos').value;
	const keyword = document.getElementById('searchKeyword').value.trim();
	
	let filterd = originalEmpList;
	
	// 부서 필터
	if (dept) {
		filterd = filterd.filter(emp => emp.deptName === dept);
	}
	
	// 직급 필터
	if (pos) {
		filterd = filterd.filter(emp => emp.posName === pos);
	}
	
	// 이름 또는 사번 키워드
	if (keyword) {
		const lower = keyword.toLowerCase();
		filterd = filterd.filter(emp =>
			emp.empName.toLowerCase().includes(lower) ||
			emp.empId.includes(keyword)
		);
	}
	
	renderEmpTable(filterd);
}

// 1. 사원 목록 불러오기
function loadEmpListForAction() {
	
	const url = '/api/hr/employees';
	
	fetch(url)
		.then(res => {
			if (!res.ok) {
				throw new Error('사원 목록 API 오류');
			}
			return res.json();
		})
		.then(data => {
			originalEmpList = data;
			renderEmpTable(data);
		})
		.catch(err => {
			console.error(err);
			alert('사원 목록을 불러오는 중 오류가 발생했습니다.');
		});
}


// 2. 테이블에 사원 목록 렌더링
function renderEmpTable(empList) {
	const tbody = document.querySelector('tbody');
	if (!tbody) return;
	
	tbody.innerHTML = '';
	
	empList.forEach(emp => {
		const tr = document.createElement('tr');
		tr.classList.add('text-center', 'cursor-pointer');
		
		// 선택 시 사용할 데이터 속성
		tr.dataset.empId = emp.empId;
		tr.dataset.empName = emp.empName;
		tr.dataset.deptName = emp.deptName;
		tr.dataset.posName = emp.posName;
		
		tr.innerHTML = `
			<td>${emp.empId}</td>
			<td>${emp.empName}</td>
			<td>${emp.deptName ?? ''}</td>
			<td>${emp.posName ?? ''}</td>
			<td>${emp.mobile ?? ''}</td>
		`;
		
		// 행 클릭하면 오른쪽 선택 정보에 반영
		tr.addEventListener('click', function () {
			onSelectEmpRow(tr);
		});
		
		tbody.appendChild(tr);
	});
}

// 3. 사원 선택 시 오른쪽 정보 세팅
function onSelectEmpRow(row) {
	const empId = row.dataset.empId;
	const empName = row.dataset.empName;
	const deptName = row.dataset.deptName;
	const posName = row.dataset.posName;
	
	// 오른쪽 카드 텍스트 업데이트
	document.getElementById('sel-emp-id').textContent = empId || '-';
	document.getElementById('sel-emp-name').textContent = empName || '-';
	document.getElementById('sel-emp-dept').textContent = deptName || '-';
	document.getElementById('sel-emp-pos').textContent = posName || '-';
	
	// 히든에 값 세팅
	const empIdInput = document.getElementById('empId');
	if (empIdInput) {
		empIdInput.value = empId;
	}
	
	// 선택된 행 하이라이트
	const tbody = row.parentElement;
	Array.from(tbody.querySelectorAll('tr')).forEach(tr => tr.classList.remove('table-active'));
	row.classList.add('table-active');
}






































