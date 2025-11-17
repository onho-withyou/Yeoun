/* ========= 공통 유틸 ========= */
function onlyDigits(str){ return (str||'').replace(/[^\d]/g,''); }
function formatAmountValue(v){ if(v===''||v==null) return ''; return Number(v).toLocaleString('ko-KR'); }
function parseAmountValue(v){ return Number(onlyDigits(v)); }
function fmtKRW(n){ if(!isFinite(n)) n=0; return '₩'+Math.round(n).toLocaleString('ko-KR'); }

function updatePreview(modal){
  if(!modal) return;
  const isCreate = modal.id === 'createModal';

  const base = parseAmountValue( modal.querySelector('input[name="baseAmt"]')?.value || '0' );
  const meal = parseAmountValue( modal.querySelector('input[name="mealAmt"]')?.value || '0' );
  const trans = parseAmountValue( modal.querySelector('input[name="transAmt"]')?.value || '0' );
  const gross = base + meal + trans;

  const pen  = Number(modal.querySelector('input[name="penRate"]')?.value || 0);
  const hlth = Number(modal.querySelector('input[name="hlthRate"]')?.value || 0);
  const emp  = Number(modal.querySelector('input[name="empRate"]')?.value || 0);
  const tax  = Number(modal.querySelector('input[name="taxRate"]')?.value || 0);

  const penAmt  = gross * pen;
  const hlthAmt = gross * hlth;
  const empAmt  = gross * emp;
  const taxAmt  = gross * tax;
  const total   = penAmt + hlthAmt + empAmt + taxAmt;

  if(isCreate){
    modal.querySelector('#create-preview-gross-pay').textContent = gross.toLocaleString('ko-KR');
    modal.querySelector('#create-preview-pen-amt').textContent   = fmtKRW(penAmt);
    modal.querySelector('#create-preview-hlth-amt').textContent  = fmtKRW(hlthAmt);
    modal.querySelector('#create-preview-emp-amt').textContent   = fmtKRW(empAmt);
    modal.querySelector('#create-preview-tax-amt').textContent   = fmtKRW(taxAmt);
    modal.querySelector('#create-preview-total-amt').textContent = fmtKRW(total);
  }else{
    modal.querySelector('.edit-preview-gross-pay').textContent = gross.toLocaleString('ko-KR');
    modal.querySelector('.edit-preview-pen-amt').textContent   = fmtKRW(penAmt);
    modal.querySelector('.edit-preview-hlth-amt').textContent  = fmtKRW(hlthAmt);
    modal.querySelector('.edit-preview-emp-amt').textContent   = fmtKRW(empAmt);
    modal.querySelector('.edit-preview-tax-amt').textContent   = fmtKRW(taxAmt);
    modal.querySelector('.edit-preview-total-amt').textContent = fmtKRW(total);
  }
}

function clampEndAfterStart(startEl, endEl){
  if(!startEl || !endEl) return;
  if(startEl.value){
    endEl.min = startEl.value;
    if(endEl.value && endEl.value < startEl.value){ endEl.value = startEl.value; }
  }else{
    endEl.removeAttribute('min');
  }
}

// 제출 전 콤마 제거
function sanitizeAmountInputs(form){
  form.querySelectorAll('.amount-input').forEach(inp=>{
    inp.dataset._formatted = inp.value;
    inp.value = onlyDigits(inp.value);
  });
}

function restoreAmountInputs(form){
  form.querySelectorAll('.amount-input').forEach(inp=>{
    if(inp.dataset._formatted){
      inp.value = inp.dataset._formatted;
      delete inp.dataset._formatted;
    }
  });
}

/* ========= 등록 모달 ========= */
const createModal = document.getElementById('createModal');
const createForm  = document.getElementById('create-rule-form');

if(createModal){
  createModal.addEventListener('shown.bs.modal', ()=>{
    createModal.querySelectorAll('.amount-input').forEach(inp=>{
      if(inp.value){ inp.value = formatAmountValue(parseAmountValue(inp.value)); }
    });
    clampEndAfterStart(document.getElementById('create-start'), document.getElementById('create-end'));
    updatePreview(createModal);
  });

  createModal.addEventListener('hidden.bs.modal', ()=>{
    createForm.reset();
    createForm.classList.remove('was-validated');
    const end = document.getElementById('create-end');
    if(end){ end.setCustomValidity(''); end.removeAttribute('min'); }
  });

  createModal.addEventListener('input', (e)=>{
    if(e.target.matches('.amount-input')){
      e.target.value = formatAmountValue(parseAmountValue(e.target.value));
    }
    if(e.target.matches('.amount-input, .rate-input')) updatePreview(createModal);

    if(e.target.id==='create-start' || e.target.id==='create-end'){
      clampEndAfterStart(document.getElementById('create-start'), document.getElementById('create-end'));
    }
  });

  createForm.addEventListener('submit', (e)=>{
    sanitizeAmountInputs(createForm);
    if(!createForm.checkValidity()){
      e.preventDefault(); e.stopPropagation();
      createForm.classList.add('was-validated');
      restoreAmountInputs(createForm);
      return;
    }

    const s = document.getElementById('create-start');
    const d = document.getElementById('create-end');

    if(s.value && d.value && d.value < s.value){
      e.preventDefault(); e.stopPropagation();
      d.setCustomValidity('종료일은 시작일 이후여야 합니다');
      d.classList.add('is-invalid');
      createForm.classList.add('was-validated');
      restoreAmountInputs(createForm);
    }
  });
}

/* ========= 수정 모달 ========= */
document.addEventListener('shown.bs.modal', (evt)=>{
  const modal = evt.target;
  if(!modal.classList.contains('modal')) return;
  if(modal.dataset.bound === '1'){ updatePreview(modal); return; }

  modal.querySelectorAll('.amount-input').forEach(inp=>{
    if(inp.value){ inp.value = formatAmountValue(parseAmountValue(inp.value)); }
  });

  const s = modal.querySelector('.edit-start');
  const d = modal.querySelector('.edit-end');
  clampEndAfterStart(s, d);
  updatePreview(modal);

  modal.addEventListener('input', (e)=>{
    if(e.target.matches('.amount-input')){
      e.target.value = formatAmountValue(parseAmountValue(e.target.value));
    }
    if(e.target.matches('.amount-input, .rate-input')) updatePreview(modal);

    if(e.target.classList.contains('edit-start') || e.target.classList.contains('edit-end')){
      clampEndAfterStart(s, d);
    }
  });

  const form = modal.querySelector('form.needs-validation');
  form?.addEventListener('submit', (e)=>{
    sanitizeAmountInputs(form);
    if(!form.checkValidity()){
      e.preventDefault(); e.stopPropagation();
      form.classList.add('was-validated');
      restoreAmountInputs(form);
      return;
    }

    if(s && d && s.value && d.value && d.value < s.value){
      e.preventDefault(); e.stopPropagation();
      d.setCustomValidity('종료일은 시작일 이후여야 합니다');
      d.classList.add('is-invalid');
      form.classList.add('was-validated');
      restoreAmountInputs(form);
    }
  });

  modal.dataset.bound = '1';
});

/* ========= 삭제 버튼 ========= */
document.addEventListener('click', (e)=>{
  const btn = e.target.closest('.btn-delete-rule');
  if(!btn) return;
  const id = btn.dataset.ruleId;
  if(confirm(`기준 ID ${id} 를 삭제하시겠습니까?`)){
    location.href = btn.dataset.href;
  }
});

/* ========= 탭 저장 ========= */
(function(){
  const key = 'payTabs.active';
  const tabsEl = document.getElementById('payTabs');
  if(!tabsEl) return;

  const saved = localStorage.getItem(key);
  if(saved){
    const trigger = document.querySelector(`[data-bs-target="${saved}"]`);
    if(trigger){
      const tab = new bootstrap.Tab(trigger);
      tab.show();
    }
  }

  tabsEl.addEventListener('shown.bs.tab', function(e){
    const target = e.target?.getAttribute('data-bs-target');
    if(target){ localStorage.setItem(key, target); }
  });
})();
