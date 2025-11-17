document.addEventListener("DOMContentLoaded", () => {

  /* ===== 공통 유틸 ===== */
  const onlyDigitsDot = (s)=> (s||'').replace(/[^\d.]/g,'');
  const onlyDigits    = (s)=> (s||'').replace(/[^\d]/g,'');
  const formatAmount  = (v)=> (v===''||v==null) ? '' : Number(v).toLocaleString('ko-KR');
  const parseAmount   = (v)=> Number(onlyDigits(v));

  /* ===== show/hide ===== */
  const showEl = (el)=>{
    if(!el) return;
    el.classList.remove("d-none");
    el.hidden = false;
    el.style.display = "";
  };
  const hideEl = (el)=>{
    if(!el) return;
    el.classList.add("d-none");
    el.hidden = true;
    el.style.display = "none";
  };

  /* ============================
     [등록] 모달 초기화
     ============================ */
  const createModal = document.getElementById("calcCreateModal");
  if(createModal){
    const form       = document.getElementById("calc-create-form");
    const typeSel    = createModal.querySelector('select[name="ruleType"]');
    const valueBox   = createModal.querySelector(".create-value");
    const formulaBox = createModal.querySelector(".create-formula");
    const startEl    = createModal.querySelector('input[name="startDate"]');
    const endEl      = createModal.querySelector('input[name="endDate"]');

    function syncCreateFields(){
      const t = typeSel?.value;
      if(t === "FORMULA"){
        hideEl(valueBox);
        showEl(formulaBox);
      } else {
        hideEl(formulaBox);
        showEl(valueBox);
      }
    }

    syncCreateFields();
    typeSel?.addEventListener("change", syncCreateFields);

    // 입력 처리
    createModal.addEventListener("input", (e)=>{
      if(e.target.matches(".amount-input")){
        if(typeSel?.value === "AMT"){
          e.target.value = formatAmount(parseAmount(e.target.value));
        } else {
          e.target.value = onlyDigitsDot(e.target.value);
        }
      }

      if(e.target.name === "startDate" || e.target.name === "endDate"){
        if(startEl?.value && endEl){
          endEl.min = startEl.value;
        }
      }
    });

    // submit 시 처리
    form?.addEventListener("submit", (e)=>{
      const t = typeSel?.value;
      const v = form.querySelector('input[name="valueNum"], .amount-input');

      if(v){
        if(t === "AMT")  v.value = onlyDigits(v.value);
        if(t === "RATE") v.value = onlyDigitsDot(v.value);
      }

      if(startEl?.value && endEl?.value && endEl.value < startEl.value){
        e.preventDefault();
        e.stopPropagation();
        endEl.setCustomValidity("종료일은 시작일 이후여야 합니다.");
      } else {
        endEl?.setCustomValidity("");
      }

      if(!form.checkValidity()){
        e.preventDefault();
        e.stopPropagation();
        form.classList.add("was-validated");
      }
    });
  }

  /* ============================
     [수정] 모달 초기화 (여러 개)
     ============================ */
  document.addEventListener("shown.bs.modal", (evt)=>{
    const modal = evt.target;
    if(!modal.classList.contains("modal")) return;

    const typeSel    = modal.querySelector('select[name="ruleType"]');
    const valueBox   = modal.querySelector(".edit-value");
    const formulaBox = modal.querySelector(".edit-formula");
    const startEl    = modal.querySelector('input[name="startDate"]');
    const endEl      = modal.querySelector('input[name="endDate"]');

    function syncEditFields(){
      const t = typeSel?.value;
      if(t === "FORMULA"){
        hideEl(valueBox);
        showEl(formulaBox);
      } else {
        hideEl(formulaBox);
        showEl(valueBox);
      }
    }

    syncEditFields();
    typeSel?.addEventListener("change", syncEditFields);

    modal.addEventListener("input", (e)=>{
      if(e.target.matches(".amount-input")){
        if(typeSel?.value === "AMT"){
          e.target.value = formatAmount(parseAmount(e.target.value));
        } else {
          e.target.value = onlyDigitsDot(e.target.value);
        }
      }

      if(e.target.name === "startDate" || e.target.name === "endDate"){
        if(startEl?.value && endEl){
          endEl.min = startEl.value;
        }
      }
    });

    const editForm = modal.querySelector("form");
    editForm?.addEventListener("submit", (e)=>{
      const v = editForm.querySelector('input[name="valueNum"], .amount-input');

      if(v){
        if(typeSel?.value === "AMT")  v.value = onlyDigits(v.value);
        if(typeSel?.value === "RATE") v.value = onlyDigitsDot(v.value);
      }

      if(startEl?.value && endEl?.value && endEl.value < startEl.value){
        e.preventDefault();
        e.stopPropagation();
        endEl.setCustomValidity("종료일은 시작일 이후여야 합니다.");
      } else {
        endEl.setCustomValidity("");
      }

      if(!editForm.checkValidity()){
        e.preventDefault();
        e.stopPropagation();
        editForm.classList.add("was-validated");
      }
    });
  });

});
