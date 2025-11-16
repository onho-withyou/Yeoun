// hrList.js
// 인사 발령 목록 그리드

let hrGrid = null;  

document.addEventListener('DOMContentLoaded', () => {
  loadHrActionList();
});

// ===============================
// 1. 발령 목록 가져오기
// ===============================
function loadHrActionList() {
  fetch('/api/hr/actions')
    .then(res => {
      console.log('응답 상태:', res.status, res);
      if (!res.ok) {
        // 200이 아니면 여기서 일부러 에러 던져서 catch로 보냄
        throw new Error('HTTP 오류 상태 코드: ' + res.status);
      }
      return res.json();
    })
    .then(rows => {
      console.log('받은 데이터:', rows);
      makeActionGrid(rows);
    })
    .catch(err => {
      console.error('발령 목록 조회 에러:', err);
      alert('인사 발령 목록 불러오기 실패');
    });
}


// ===============================
// 2. Toast Grid 생성 함수
// ===============================
function makeActionGrid(rows) {
  // 이미 그리드가 있으면 파괴 후 재생성 (필요하면)
  if (hrGrid) {
    hrGrid.destroy();
  }

  hrGrid = new tui.Grid({
    el: document.getElementById('grid'),
    rowHeaders: ['rowNum'],
    scrollX: true,
    scrollY: true,
    pageOptions: {
      useClient: true, 
      perPage: 10
    },
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
      },
      { 
        header: '결재 상태',
        name: 'status',
        align: 'center'
      }
    ],
  });

  // JSON 데이터 삽입
  hrGrid.resetData(rows);
}
