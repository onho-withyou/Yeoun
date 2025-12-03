// processStatus.js
// 공정 현황 목록 + 상세 모달 + 공정 단계 시작/종료/메모 저장

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
        header: '양품수량',
        name: 'goodQty',
        align: 'right'
      },
      {
        header: '진행률',
        name: 'progressRate',
        align: 'center'
      },
      {
        header: '현재공정',
        name: 'currentProcess',
        align: 'center'
      },
      {
        header: '상태',
        name: 'status',
        align: 'center'
      },
      {
        header: '경과시간',
        name: 'elapsedTime',
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

  // 2) 검색 버튼 이벤트
  const btnSearch = document.getElementById('btnSearchProcess');
  if (btnSearch) {
    btnSearch.addEventListener('click', () => loadProcessGrid(grid));
  }

  // 3) 페이지 최초 진입 시 전체 목록 로딩
  loadProcessGrid(grid);

  // 4) 상세 버튼 클릭 이벤트
  grid.on('click', ev => {
    if (ev.columnName !== 'btn') return;

    const row = grid.getRow(ev.rowKey);
    if (!row || !row.orderId) return;

    openDetailModal(row.orderId);
  });
});

/**
 * 공정현황 목록 조회
 * - 검색 조건(form) 값 읽어서 쿼리스트링으로 전달
 */
function loadProcessGrid(grid) {

  const workDate      = document.getElementById('workDate')?.value || '';
  const searchProduct = document.getElementById('searchProduct')?.value || '';
  const searchHStatus = document.getElementById('searchHStatus')?.value || '';
  const searchKeyword = document.getElementById('searchKeyword')?.value || '';

  const query = new URLSearchParams({
    workDate,
    productId: searchProduct,
    status: searchHStatus,
    keyword: searchKeyword
  });

  fetch('/process/status/data?' + query.toString())
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
 * - GET /process/status/detail/{orderId}
 * - 응답 예시: { wop: {...}, steps: [...] }
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

	        // 상태 뱃지 HTML
	        let statusBadge = '-';
	        if (step.status === 'READY') {
	          statusBadge = `<span class="badge bg-label-secondary">대기</span>`;
	        } else if (step.status === 'IN_PROGRESS') {
	          statusBadge = `<span class="badge bg-label-warning text-dark">진행중</span>`;
	        } else if (step.status === 'DONE') {
	          statusBadge = `<span class="badge bg-label-success">완료</span>`;
	        }

	        // 작업 버튼 HTML (canStart / canFinish 기준)
	        let workBtnHtml = '';
	        if (step.canStart) {
	          workBtnHtml += `
	            <button type="button"
	                    class="btn btn-primary btn-sm btn-step-start"
	                    data-order-id="${summary.orderId}"
	                    data-step-seq="${step.stepSeq}">
	              시작
	            </button>
	          `;
	        }
	        if (step.canFinish) {
	          workBtnHtml += `
	            <button type="button"
	                    class="btn btn-success btn-sm btn-step-finish ms-1"
	                    data-order-id="${summary.orderId}"
	                    data-step-seq="${step.stepSeq}">
	              종료
	            </button>
	          `;
	        }
	        if (!step.canStart && !step.canFinish) {
	          workBtnHtml = '-';
	        }

	        // 특이사항 입력 인풋
	        const memoInputHtml = `
	          <input type="text"
	                 class="form-control form-control-sm step-memo-input"
	                 data-order-id="${summary.orderId}"
	                 data-step-seq="${step.stepSeq}"
	                 value="${step.memo ? step.memo : ''}">
	        `;

	        tr.innerHTML = `
	          <td>${step.stepSeq}</td>
	          <td>${step.processId}</td>
	          <td>
	            <span class="badge rounded-pill bg-label-primary">
	              ${step.processName}
	            </span>
	          </td>
	          <td>${statusBadge}</td>
	          <td>${step.startTime || '-'}</td>
	          <td>${step.endTime || '-'}</td>
	          <td>${step.goodQty ?? '-'}</td>
	          <td>${step.defectQty ?? '-'}</td>
	          <td>${workBtnHtml}</td>
	          <td>${memoInputHtml}</td>
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

// -----------------------------
// 공정 단계 작업(시작/종료) & 메모 저장 이벤트
// -----------------------------

// 시작/종료 버튼
document.addEventListener('click', function (e) {
  // 시작 버튼
  if (e.target.classList.contains('btn-step-start')) {
    const btn = e.target;
    const orderId = btn.dataset.orderId;
    const stepSeq = btn.dataset.stepSeq;
    handleStartStep(orderId, stepSeq, btn);
  }

  // 종료 버튼
  if (e.target.classList.contains('btn-step-finish')) {
    const btn = e.target;
    const orderId = btn.dataset.orderId;
    const stepSeq = btn.dataset.stepSeq;
    handleFinishStep(orderId, stepSeq, btn);
  }
});

// 메모 입력 변경 시 서버 저장 (change 이벤트 기준)
document.addEventListener('change', function (e) {
  if (e.target.classList.contains('step-memo-input')) {
    const input = e.target;
    const orderId = input.dataset.orderId;
    const stepSeq = input.dataset.stepSeq;
    const memo = input.value;
    handleSaveStepMemo(orderId, stepSeq, memo, input);
  }
});

/**
 * 공정 단계 시작 처리
 * - POST /process/status/step/start
 * - 요청: { orderId, stepSeq }
 * - 응답 예시: { success: true, message: '...', updatedStep: {...} }
 */
function handleStartStep(orderId, stepSeq, btnEl) {
  if (!confirm('해당 공정을 시작 처리하시겠습니까?')) {
    return;
  }

  fetch('/process/status/step/start', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
      // TODO: CSRF 토큰 쓰면 여기 추가
    },
    body: JSON.stringify({ orderId, stepSeq })
  })
    .then(res => res.json())
    .then(result => {
      if (!result.success) {
        alert(result.message || '공정 시작 처리 중 오류가 발생했습니다.');
        return;
      }

      alert(result.message || '공정을 시작 처리했습니다.');

      const updated = result.updatedStep;
      if (updated) {
        updateStepRowInModal(updated);
      }
    })
    .catch(err => {
      console.error('공정 시작 처리 오류', err);
      alert('공정 시작 처리 중 오류가 발생했습니다.');
    });
}

/**
 * 공정 단계 종료 처리
 * - POST /process/status/step/finish
 * - 요청: { orderId, stepSeq }
 * - 응답 예시: { success: true, message: '...', updatedStep: {...} }
 */
function handleFinishStep(orderId, stepSeq, btnEl) {
  if (!confirm('해당 공정을 종료 처리하시겠습니까?')) {
    return;
  }

  fetch('/process/status/step/finish', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
      // TODO: CSRF 토큰 쓰면 여기 추가
    },
    body: JSON.stringify({ orderId, stepSeq })
  })
    .then(res => res.json())
    .then(result => {
      if (!result.success) {
        alert(result.message || '공정 종료 처리 중 오류가 발생했습니다.');
        return;
      }

      alert(result.message || '공정을 종료 처리했습니다.');

      const updated = result.updatedStep;
      if (updated) {
        updateStepRowInModal(updated);
      }
    })
    .catch(err => {
      console.error('공정 종료 처리 오류', err);
      alert('공정 종료 처리 중 오류가 발생했습니다.');
    });
}

/**
 * 공정 단계 메모 저장
 * - POST /process/status/step/memo
 * - 요청: { orderId, stepSeq, memo }
 * - 응답 예시: { success: true, message: '...' }
 */
function handleSaveStepMemo(orderId, stepSeq, memo, inputEl) {
  fetch('/process/status/step/memo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
      // TODO: CSRF 토큰 쓰면 여기 추가
    },
    body: JSON.stringify({ orderId, stepSeq, memo })
  })
    .then(res => res.json())
    .then(result => {
      if (!result.success) {
        alert(result.message || '메모 저장 중 오류가 발생했습니다.');
        return;
      }

      // 성공 시 살짝 표시만 (필수는 아님)
      console.log('메모 저장 완료');
    })
    .catch(err => {
      console.error('메모 저장 오류', err);
      alert('메모 저장 중 오류가 발생했습니다.');
    });
}

/**
 * 상세 모달 내 특정 단계(tr) 갱신
 * - updatedStep: 서버에서 돌려준 최신 단계 정보
 *   (stepSeq, processId, processName, status, startTime, endTime, goodQty, defectQty, canStart, canFinish, memo ...)
 */
function updateStepRowInModal(updatedStep) {
  const tbody = document.querySelector('#stepTable tbody');
  if (!tbody) return;

  const rows = Array.from(tbody.querySelectorAll('tr'));
  const targetRow = rows.find(tr => {
    const seqCell = tr.querySelector('td:first-child');
    return seqCell && seqCell.textContent.trim() === String(updatedStep.stepSeq);
  });

  if (!targetRow) return;

  // 상태 뱃지 다시 계산
  let statusBadge = '-';
  if (updatedStep.status === 'READY') {
    statusBadge = `<span class="badge bg-label-secondary">대기</span>`;
  } else if (updatedStep.status === 'IN_PROGRESS') {
    statusBadge = `<span class="badge bg-label-warning text-dark">진행중</span>`;
  } else if (updatedStep.status === 'DONE') {
    statusBadge = `<span class="badge bg-label-success">완료</span>`;
  }

  // 버튼
  let workBtnHtml = '';
  if (updatedStep.canStart) {
    workBtnHtml += `
      <button type="button"
              class="btn btn-primary btn-sm btn-step-start"
              data-order-id="${updatedStep.orderId}"
              data-step-seq="${updatedStep.stepSeq}">
        시작
      </button>
    `;
  }
  if (updatedStep.canFinish) {
    workBtnHtml += `
      <button type="button"
              class="btn btn-success btn-sm btn-step-finish ms-1"
              data-order-id="${updatedStep.orderId}"
              data-step-seq="${updatedStep.stepSeq}">
        종료
      </button>
    `;
  }
  if (!updatedStep.canStart && !updatedStep.canFinish) {
    workBtnHtml = '-';
  }

  // 메모 인풋
  const memoInputHtml = `
    <input type="text"
           class="form-control form-control-sm step-memo-input"
           data-order-id="${updatedStep.orderId}"
           data-step-seq="${updatedStep.stepSeq}"
           value="${updatedStep.memo ? updatedStep.memo : ''}">
  `;

  // tr 전체 다시 그리기
  targetRow.innerHTML = `
    <td>${updatedStep.stepSeq}</td>
    <td>${updatedStep.processId}</td>
    <td>
      <span class="badge rounded-pill bg-label-primary">
        ${updatedStep.processName}
      </span>
    </td>
    <td>${statusBadge}</td>
    <td>${updatedStep.startTime || '-'}</td>
    <td>${updatedStep.endTime || '-'}</td>
    <td>${updatedStep.goodQty ?? '-'}</td>
    <td>${updatedStep.defectQty ?? '-'}</td>
    <td>${workBtnHtml}</td>
    <td>${memoInputHtml}</td>
  `;
}
