// empList.js 
// 사원 목록 테이블 그리드

  let empGrid = null;
  let empDetailModal = null;
  let currentEmpId = null;

  document.addEventListener('DOMContentLoaded', () => {
    empDetailModal = new bootstrap.Modal(document.getElementById('empDetailModal'));
	
	// 수정 버튼 클릭 이벤트 등록
	 const editBtn = document.getElementById('editBtn');
	 if (editBtn) {
	   editBtn.addEventListener('click', () => {
	     if (!currentEmpId) {
	       alert('선택된 사원이 없습니다.');
	       return;
	     }
	     // 수정 화면으로 이동
	     window.location.href = `/emp/edit/${currentEmpId}`;
	     // 컨텍스트 경로 있으면: window.location.href = `${window.contextPath}/emp/edit/${currentEmpId}`;
	   });
	 }
	
   // 1) 서버에서 내려준 initialEmpList로 바로 그리드 만들기
   if (typeof initialEmpList !== 'undefined') {
     makeGrid(initialEmpList);
   } else {
     // 혹시라도 initialEmpList가 없는 경우에만 Ajax로 로딩
     loadEmpList();
   }
 });
 
  // 사원 목록 가져오기
  function loadEmpList() {
    fetch('/emp/list/data')
      .then(res => res.json())
      .then(rows => {
        if (!empGrid) {
          makeGrid(rows);
        } else {
          empGrid.resetData(rows);
        }
      })
      .catch(() => alert('사원 목록 불러오기 실패'));
  }
	
  // Toast Grid 생성 함수
  function makeGrid(rows) {
	  empGrid = new tui.Grid({
      el: document.getElementById('grid'), // grid가 들어갈 div
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
	
	// 버튼 클릭 시 상세조회
	empGrid.on('click', ev => {
		if (ev.columnName !== 'btn') return;
	    const row = empGrid.getRow(ev.rowKey);
		if (!row || !row.empId) return;
	    showEmpDetail(row.empId);
	  });
	}

	// 사원 상세보기
	function showEmpDetail(empId) {
	  currentEmpId = empId;	// 수정에 쓸 현재 사번 저장
		
	  fetch(`/emp/detail/${empId}`)
	    .then(res => res.json())
	    .then(d => {
	      // 간단히 값 채우기
	      document.getElementById('d-empName').textContent = d.empName;
	      document.getElementById('d-empId').textContent = d.empId;
	      document.getElementById('d-deptName').textContent = d.deptName;
	      document.getElementById('d-posName').textContent = d.posName;
	      document.getElementById('d-gender').textContent = d.gender === 'M' ? '남' : d.gender === 'F' ? '여' : '—';
	      document.getElementById('d-hireDate').textContent = d.hireDate;
	      document.getElementById('d-mobile').textContent = d.mobile;
	      document.getElementById('d-email').textContent = d.email;
	      document.getElementById('d-address').textContent = d.address;
	      document.getElementById('d-rrn').textContent = d.rrnMasked;
	      document.getElementById('d-bank').textContent = d.bankInfo;

	      const photo = document.getElementById('d-photo');
		  photo.onerror = () => { photo.src = '/img/default-profile.png'; };
	      photo.src = d.photoPath || '/img/default-profile.png';

	      empDetailModal.show();
	    })
	    .catch(() => alert('상세정보 불러오기 실패'));
	}
	
