// empList.js 
// 사원 목록 테이블 그리드

let empGrid = null;
let empDetailModal = null;
let currentEmpId = null;

// 페이징 상태
let currentPage = 0;   // 0부터 시작 (Spring Page와 맞춤)
const pageSize = 10;

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

  // 검색 버튼 이벤트
  const searchBtn = document.getElementById('btnSearch');
  if (searchBtn) {
    searchBtn.addEventListener('click', () => {
      loadEmpList(0);   // 검색 시 항상 첫 페이지부터
    });
  }
  
  // 엔터 눌렀을 때도 검색 실행
    const keywordInput = document.getElementById('keyword');
    if (keywordInput) {
      keywordInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
          e.preventDefault();   // 폼 submit 막기
          loadEmpList(0);       // 첫 페이지부터 검색
        }
      });
    }

  // 부서 선택 변경 시 자동 검색
  const deptSelect = document.getElementById('deptId');
  if (deptSelect) {
    deptSelect.addEventListener('change', () => {
      loadEmpList(0);
    });
  }

  // 초기 그리드 + 첫 페이지 데이터 로딩
  initEmpGrid();
  loadEmpList(0);
});

// ================================
//  사원 목록 불러오기 (서버 페이징)
// ================================
function loadEmpList(page) {
  const keywordInput = document.getElementById('keyword');
  const deptSelect = document.getElementById('deptId');

  const keyword = keywordInput ? keywordInput.value : '';
  const deptId = deptSelect ? deptSelect.value : '';

  const params = new URLSearchParams({
    page: page,
    size: pageSize,
    keyword: keyword,
    deptId: deptId
  });

  fetch('/emp/data?' + params.toString())
    .then(res => res.json())
    .then(data => {
      // data = EmpPageResponse (content, page, size, totalElements, totalPages)
      currentPage = data.page;

      // 그리드 데이터 갱신
      empGrid.resetData(data.content);

      // 페이징 렌더링
      renderPagination(data.totalPages);
    })
    .catch(() => alert('사원 목록 불러오기 실패'));
}

// ================================
//  Toast Grid 생성 함수
// ================================
function initEmpGrid() {
  empGrid = new tui.Grid({
    el: document.getElementById('grid'), // grid가 들어갈 div
    rowHeaders: [],                      // 왼쪽 번호/체크박스 없음
    scrollX: true,                       // 가로 스크롤
    scrollY: true,                       // 세로 스크롤
    editable: false,                     // 읽기 전용
//    pageOptions: { 
//      useClient: false,   // 서버 페이징
//      perPage: pageSize
//    },
    columns: [
      { header: '입사일자', name: 'hireDate', align: 'center', sortable: true },
      { header: '사원번호', name: 'empId',    align: 'center', sortable: true },
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

  // 버튼 클릭 시 상세조회
  empGrid.on('click', ev => {
    if (ev.columnName !== 'btn') return;
    const row = empGrid.getRow(ev.rowKey);
    if (!row || !row.empId) return;
    showEmpDetail(row.empId);
  });
}

// ================================
//  페이징 렌더링 (부트스트랩 pagination 사용 가정)
// ================================
function renderPagination(totalPages) {
  const container = document.getElementById('empPagination');
  if (!container) return;

  let html = '';

  // 이전
  html += `
    <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
      <button class="page-link" type="button" onclick="loadEmpList(${currentPage - 1})"><</button>
    </li>
  `;

  // 페이지 번호
  for (let i = 0; i < totalPages; i++) {
    html += `
      <li class="page-item ${i === currentPage ? 'active' : ''}">
        <button class="page-link" type="button" onclick="loadEmpList(${i})">${i + 1}</button>
      </li>
    `;
  }

  // 다음
  html += `
    <li class="page-item ${currentPage + 1 >= totalPages ? 'disabled' : ''}">
      <button class="page-link" type="button" onclick="loadEmpList(${currentPage + 1})">></button>
    </li>
  `;

  container.innerHTML = html;
}

// ================================
//  사원 상세보기
// ================================
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
