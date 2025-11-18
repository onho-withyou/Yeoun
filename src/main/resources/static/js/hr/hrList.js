// hrList.js
// 인사 발령 목록 그리드 + 검색/페이징

let hrGrid = null;
let currentPage = 0;
const pageSize = 10;

document.addEventListener('DOMContentLoaded', () => {

  // 1) 그리드 먼저 세팅
  initHrGrid();

  // 2) 검색 버튼
  const btnSearch = document.getElementById('btnSearch');
  if (btnSearch) {
    btnSearch.addEventListener('click', () => loadHrActionList(0));
  }

  // 3) 발령 구분 선택 시 자동 검색
  const actionTypeSelect = document.getElementById('actionType');
  if (actionTypeSelect) {
    actionTypeSelect.addEventListener('change', () => loadHrActionList(0));
  }

  // 4) 키워드 엔터 시 검색
  const keywordInput = document.getElementById('keyword');
  if (keywordInput) {
    keywordInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        loadHrActionList(0);
      }
    });
  }

  // 5) 초기 1페이지 로딩
  loadHrActionList(0);
});

// ===============================
// 1. 발령 목록 가져오기 (서버 페이징)
// ===============================
function loadHrActionList(page) {
  const keyword = document.getElementById('keyword')?.value || '';
  const actionType = document.getElementById('actionType')?.value || '';
  const startDate = document.getElementById('startDate')?.value || '';
  const endDate = document.getElementById('endDate')?.value || '';

  const params = new URLSearchParams({
    page,
    size: pageSize,
    keyword,
    actionType,
    startDate,
    endDate
  });

  fetch('/api/hr/actions?' + params.toString())
    .then(res => {
      console.log('응답 상태:', res.status, res);
      if (!res.ok) {
        throw new Error('HTTP 오류 상태 코드: ' + res.status);
      }
      return res.json();
    })
    .then(data => {
      console.log('받은 데이터:', data);
      currentPage = data.page;

      // JSON 구조: { content, page, size, totalElements, totalPages }
      hrGrid.resetData(data.content);

      renderHrPagination(data.totalPages);
    })
    .catch(err => {
      console.error('발령 목록 조회 에러:', err);
      alert('인사 발령 목록 불러오기 실패');
    });
}

// ===============================
// 2. Toast Grid 생성 함수
// ===============================
function initHrGrid() {

  if (hrGrid) {
    hrGrid.destroy();
  }

  hrGrid = new tui.Grid({
    el: document.getElementById('grid'),
    rowHeaders: ['rowNum'],
    scrollX: true,
    scrollY: true,
    columns: [
      { 
        header: '사원번호',
        name: 'empId',
        align: 'center'
      },
      { 
        header: '성명',
        name: 'empName',
        align: 'center'
      },
      { 
        header: '발령구분',
        name: 'actionTypeName',  
        align: 'center'
      },
      { 
        header: '발령일자',
        name: 'effectiveDate',  
        align: 'center'
      },
      { 
        header: '부서(이전)',
        name: 'fromDeptName',
        align: 'center'
      },
      { 
        header: '부서(이후)',
        name: 'toDeptName',
        align: 'center'
      },
      { 
        header: '직급(이전)',
        name: 'fromPosName',
        align: 'center'
      },
      { 
        header: '직급(이후)',
        name: 'toPosName',
        align: 'center'
      }
//      { 
//        header: '결재 상태',
//        name: 'status',
//        align: 'center'
//      }
    ]
  });
}

// ===============================
// 3. 페이지네이션 렌더링
// ===============================
function renderHrPagination(totalPages) {
  const container = document.getElementById('hrPagination');
  if (!container) return;

  let html = '';

  // 이전
  html += `
    <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
      <button class="page-link" type="button" onclick="loadHrActionList(${currentPage - 1})"><</button>
    </li>
  `;

  // 페이지 번호
  for (let i = 0; i < totalPages; i++) {
    html += `
      <li class="page-item ${i === currentPage ? 'active' : ''}">
        <button class="page-link" type="button" onclick="loadHrActionList(${i})">${i + 1}</button>
      </li>
    `;
  }

  // 다음
  html += `
    <li class="page-item ${currentPage + 1 >= totalPages ? 'disabled' : ''}">
      <button class="page-link" type="button" onclick="loadHrActionList(${currentPage + 1})">></button>
    </li>
  `;

  container.innerHTML = html;
}
