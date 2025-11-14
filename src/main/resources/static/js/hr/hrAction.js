// 인사 발령 JS

document.addEventListener('DOMContentLoaded', function() {
	// 페이지 로드되면 사원 목록 불러오기
	loadEmpListForAction();
});


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
		tr.dataset.posNaem = emp.posName;
		
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















