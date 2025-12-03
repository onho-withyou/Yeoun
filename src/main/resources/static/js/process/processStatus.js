// processStatus.js
// 공정 현황 목록 + 상세 모달

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

  // 1) 그리드 생성
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
        width: 90,
        align: 'center',
        formatter: () =>
          "<button type='button' class='btn btn-info btn-sm'>상세</button>"
      }
    ]
  });

  // 2) 목록 데이터 로딩
  loadProcessGrid(grid);

  // 3) 상세 버튼 클릭 이벤트
  grid.on('click', ev => {
    if (ev.columnName !== 'btn') return;

    const row = grid.getRow(ev.rowKey);
    if (!row || !row.orderId) return;

    openDetailModal(row.orderId);
  });
});

/**
 * 공정현황 목록 조회
 */
function loadProcessGrid(grid) {
  fetch('/process/status/data')
    .then(res => {
      if (!res.ok) {
        throw new Error('HTTP ' + res.status);
      }
      return res.json();
    })
    .then(data => {
      console.log('공정현황 목록:', data);
      grid.resetData(data);
    })
    .catch(err => {
      console.error('공정현황 데이터 로딩 중 오류', err);
      alert('공정현황 데이터를 불러오는 중 오류가 발생했습니다.');
    });
}

/**
 * 상세 모달 오픈 + 데이터 바인딩
 */
function openDetailModal(orderId) {

  fetch(`/process/status/detail/${orderId}`)
    .then(res => {
      if (!res.ok) throw new Error('HTTP ' + res.status);
      return res.json();
    })
    .then(data => {
      console.log('공정 상세 데이터:', data);

      const summary = data.wop;
      const steps = data.steps || [];

      // 1) 상단 요약
      const summaryEl = document.getElementById('summaryGrid');
      summaryEl.innerHTML = `
        <div class="col-md-3">
          <div class="text-muted">작업지시번호</div>
          <div class="fw-semibold">${summary.orderId}</div>
        </div>
        <div class="col-md-3">
          <div class="text-muted">제품명</div>
          <div class="fw-semibold">${summary.prdName}</div>
        </div>
        <div class="col-md-3">
          <div class="text-muted">품번</div>
          <div class="fw-semibold">${summary.prdId}</div>
        </div>
        <div class="col-md-3">
          <div class="text-muted">계획수량</div>
          <div class="fw-semibold">${summary.planQty}</div>
        </div>
      `;

      // 2) 공정 단계 테이블
      const tbody = document.querySelector('#stepTable tbody');
      tbody.innerHTML = '';

      if (steps.length === 0) {
        tbody.innerHTML = `
          <tr>
            <td colspan="10" class="text-center text-muted py-4">
              공정 단계 정보가 없습니다.
            </td>
          </tr>
        `;
      } else {
        steps.forEach(step => {
          const tr = document.createElement('tr');

          // 나중에 상태에 따라 시작/종료 버튼 넣을 자리
          const workBtnHtml = '-';

          tr.innerHTML = `
            <td>${step.stepSeq}</td>
            <td>${step.processId}</td>
            <td>${step.processName}</td>
            <td>${step.status || '-'}</td>
            <td>${step.startTime || '-'}</td>
            <td>${step.endTime || '-'}</td>
            <td>${step.goodQty ?? '-'}</td>
            <td>${step.defectQty ?? '-'}</td>
            <td>${workBtnHtml}</td>
            <td>${step.memo || ''}</td>
          `;
          tbody.appendChild(tr);
        });
      }

      // 3) 모달 오픈
      const modalEl = document.getElementById('detailModal');
      const modal = new bootstrap.Modal(modalEl);
      modal.show();
    })
    .catch(err => {
      console.error('공정 상세 조회 오류', err);
      alert('공정 상세 정보를 불러오는 중 오류가 발생했습니다.');
    });
}
