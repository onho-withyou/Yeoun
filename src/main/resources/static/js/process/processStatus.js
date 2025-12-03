// processStatus.js
// 공정 현황 목록

// 목록 grid
document.addEventListener('DOMContentLoaded', () => {
	
	const gridEl = document.getElementById('processGrid');
    if (!gridEl) {
      console.error('processGrid 요소를 찾을 수 없습니다.');
      return;
    }

    if (!window.tui || !tui.Grid) {
      console.error('Toast UI Grid 스크립트가 로드되지 않았습니다.');
      return;
    }

    const grid = new tui.Grid({
      el: gridEl,
      bodyHeight: 400,
      rowHeaders: ['rowNum'],
      scrollX: false,
      scrollY: true,
      columns: [
        {
          header: '작업지시번호',
          name: 'orderId',
          align: 'center'
        },
        {
          header: '제품코드',
          name: 'prdId',
          align: 'center'
        },
        {
          header: '제품명',
          name: 'prdName',
          align: 'center'
        },
        {
          header: '계획수량',
          name: 'planQty',
          align: 'right'
        },
        {
          header: '상태',
          name: 'status',
          align: 'center'
        },
	    {
		  header: ' ',
		  name: 'btn',
		  width: 110,
		  align: 'center',
		  formatter: () => "<button type='button' class='btn btn-info btn-sm'>상세</button>"
	    }
      ]
    });

    // 데이터 로딩
    fetch('/process/status/data')
      .then(res => {
        if (!res.ok) {
          throw new Error('HTTP ' + res.status);
        }
        return res.json();
      })
      .then(data => {
        console.log('공정현황 데이터:', data);
        grid.resetData(data);
      })
      .catch(err => {
        console.error('공정현황 데이터 로딩 중 오류', err);
        alert('공정현황 데이터를 불러오는 중 오류가 발생했습니다.');
      });
	  
	  	
	 // 상세 버튼
	 grid.on('click', ev => {
	 if (ev.columnName !== 'btn') return;
	 
	 	openDetailModal();
	 });

});

function openDetailModal() {
	// 1) 상단 요약 영역에 더미 값(모양만) 넣기
  	const summary = document.getElementById('summaryGrid');
  	summary.innerHTML = `
    	<div class="col-md-3">
      		<div class="text-muted">작업지시번호</div>
      		<div class="fw-semibold">WO-YYYYMMDD-0001</div>
    	</div>
	    <div class="col-md-3">
	      <div class="text-muted">제품명</div>
	      <div class="fw-semibold">예시 제품명</div>
	    </div>
	    <div class="col-md-3">
	      <div class="text-muted">품번</div>
	      <div class="fw-semibold">PRD-001</div>
	    </div>
	    <div class="col-md-3">
	      <div class="text-muted">계획수량</div>
	      <div class="fw-semibold">100</div>
	    </div>
  `;

  // 2) 공정 단계 테이블에 더미 한 줄만
  const tbody = document.querySelector('#stepTable tbody');
  tbody.innerHTML = `
  <tr>
      <td>1</td>
      <td>PROC-001</td>
      <td>배합</td>
      <td>READY</td>
      <td>-</td>
      <td>-</td>
      <td>-</td>
      <td>-</td>
      <td><button class="btn btn-sm btn-outline-primary" disabled>작업</button></td>
      <td>예시용 더미 데이터</td>
  </tr>
  `;

  // 3) 모달 띄우기
  const modal = new bootstrap.Modal(document.getElementById('detailModal'));
  modal.show();
}
