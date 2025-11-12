// empList.js 
// 사원 목록 테이블 그리드

  // 전역 상태
  let empGrid = null;        // 그리드 인스턴스
  let originalRows = [];     // 정렬 해제 시 복원할 원본 데이터
  
  // 1️. DOM 로드 후 올바른 함수 호출
  document.addEventListener('DOMContentLoaded', initEmpListPage);

  // ======================================
  // 2️. 데이터 로드 → 그리드 생성
  function initEmpListPage() {
    fetch('/emp/list/data', { credentials: 'same-origin' })
      .then(res => res.json())
      .then(data => {
        originalRows = Array.isArray(data) ? data.slice() : []; // 원본 백업
        buildEmpGrid(originalRows); // rows로 넘김
      })
      .catch(() => alert('사원 목록 요청 실패!'));
  }

  // ======================================
  // 3️. Toast Grid 생성 함수
  function buildEmpGrid(rows) {
	  empGrid = new tui.Grid({
      el: document.getElementById('grid'), // grid가 들어갈 div
      bodyHeight: 540,                     // 표 높이
      rowHeaders: [],                      // 왼쪽 번호/체크박스 없음
      scrollX: true,                       // 가로 스크롤
      scrollY: true,                       // 세로 스크롤
      editable: false,                     // 읽기 전용
      pageOptions: { useClient: true, perPage: 10 },
      columns: [
        { header: '입사일자', name: 'hireDate', align: 'center', sortable: true },
        { header: '사번',     name: 'empId',    align: 'center', sortable: true },
        { header: '성명',     name: 'empName',  align: 'center', sortable: true },
        { header: '부서',     name: 'deptName', align: 'center', sortable: true },
        { header: '직급',     name: 'posName',  align: 'center', sortable: true },
        { header: '전화번호', name: 'mobile',   align: 'center' },
        { header: 'Email',    name: 'email',    width: 220 },
        {
          header: ' ',
          name: 'btn',
          width: 110,
          align: 'center',
          formatter: () => "<button type='button' class='btn btn-info btn-sm'>상세</button>"
        }
      ],
    });

    // JSON 데이터 삽입
    empGrid.resetData(rows);
    
    // 버튼 클릭 이벤트
    empGrid.on('click', (ev) => {
      if (ev.columnName !== 'btn') return;
      const row = empGrid.getRow(ev.rowKey);
      if (row?.empId) location.href = '/emp/detail/' + row.empId;
    });
    
  }