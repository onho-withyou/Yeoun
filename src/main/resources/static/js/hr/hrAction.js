// 인사 발령 JS

let empGrid = null;
let originalEmpList = [];

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
	
});

// 0. 검색 실행 함수
function onSearchEmp() {
  const dept = document.getElementById('searchDept').value;
  const pos  = document.getElementById('searchPos').value;
  const keyword = document.getElementById('searchKeyword').value.trim().toLowerCase();

  let filtered = originalEmpList;

  if (dept) filtered = filtered.filter(emp => emp.deptName === dept);
  if (pos)  filtered = filtered.filter(emp => emp.posName === pos);

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
