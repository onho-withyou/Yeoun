document.addEventListener("DOMContentLoaded", () => {

  /* =====================================================
      공통 유틸
  ===================================================== */
  const onlyDigits    = (s)=> (s||'').replace(/[^\d]/g,'');           // 정수
  const onlyDigitsDot = (s)=> (s||'').replace(/[^\d.]/g,'');         // 소수점 허용
  const formatAmount  = (v)=> (v==''||v==null) ? '' : Number(v).toLocaleString('ko-KR');
  const parseAmount   = (v)=> Number(onlyDigits(v));

  const showEl = (el)=>{ if(el) { el.classList.remove("d-none"); el.hidden=false; el.style.display=""; } };
  const hideEl = (el)=>{ if(el) { el.classList.add("d-none"); el.hidden=true; el.style.display="none"; } };


  /* =====================================================
      [등록] 모달 처리
  ===================================================== */
  const createModal = document.getElementById("calcCreateModal");

  if(createModal){
    const form       = document.getElementById("calc-create-form");
    const typeSel    = createModal.querySelector('select[name="ruleType"]');
    const valueBox   = createModal.querySelector(".create-value");
    const formulaBox = createModal.querySelector(".create-formula");
    const startEl    = createModal.querySelector('input[name="startDate"]');
    const endEl      = createModal.querySelector('input[name="endDate"]');
    const valueNumEl = createModal.querySelector('input[name="valueNum"]');

    /** RULE_TYPE 에 맞춰 보여줄 영역 전환 */
    function syncCreateFields(){
      const t = typeSel.value;
      if(t === "FORMULA"){
        hideEl(valueBox);
        showEl(formulaBox);
      } else {
        hideEl(formulaBox);
        showEl(valueBox);
      }
    }

    syncCreateFields();

    typeSel.addEventListener("change", ()=>{
      syncCreateFields();
      if(valueNumEl){
        valueNumEl.value = "";
        valueNumEl.dispatchEvent(new Event("input"));
      }
    });

    /* 입력 이벤트 */
    createModal.addEventListener("input", (e)=>{
      if(e.target.matches(".amount-input")){
        const type = typeSel.value;
        if(type === "AMT"){
          e.target.value = formatAmount(parseAmount(e.target.value));
        } else {
          e.target.value = onlyDigitsDot(e.target.value);
        }
      }

      if(e.target.name === "startDate" || e.target.name === "endDate"){
        if(startEl.value){
          endEl.min = startEl.value;
        }
      }
    });


    /* 제출 처리 */
    form.addEventListener("submit", (e)=>{
      const type = typeSel.value;

      if(valueNumEl){
        if(type === "AMT")  valueNumEl.value = onlyDigits(valueNumEl.value);
        if(type === "RATE") valueNumEl.value = onlyDigitsDot(valueNumEl.value);
      }

      if(startEl.value && endEl.value && endEl.value < startEl.value){
        e.preventDefault();
        e.stopPropagation();
        endEl.setCustomValidity("종료일은 시작일 이후여야 합니다.");
      } else {
        endEl.setCustomValidity("");
      }

      if(!form.checkValidity()){
        e.preventDefault();
        e.stopPropagation();
        form.classList.add("was-validated");
      }
    });
  }



  /* =====================================================
      [수정] 모달 처리 (여러 개)
  ===================================================== */
  document.addEventListener("shown.bs.modal", (evt)=>{
    const modal = evt.target;
    if(!modal.classList.contains("modal")) return;

    const typeSel    = modal.querySelector('select[name="ruleType"]');
    const valueBox   = modal.querySelector(".edit-value");
    const formulaBox = modal.querySelector(".edit-formula");
    const startEl    = modal.querySelector('input[name="startDate"]');
    const endEl      = modal.querySelector('input[name="endDate"]');
    const valueNumEl = modal.querySelector('input[name="valueNum"]');

    function syncEditFields(){
      const t = typeSel.value;
      if(t === "FORMULA"){
        hideEl(valueBox);
        showEl(formulaBox);
      } else {
        hideEl(formulaBox);
        showEl(valueBox);
      }
    }

    syncEditFields();

    /* RULE_TYPE 변경 시 valueNum 필드 초기화 */
    typeSel.addEventListener("change", ()=>{
      syncEditFields();
      if(valueNumEl){
        valueNumEl.value = "";
        valueNumEl.dispatchEvent(new Event("input"));
      }
    });

    /* 입력 이벤트 */
    modal.addEventListener("input", (e)=>{
      if(e.target.matches(".amount-input")){
        const t = typeSel.value;
        if(t === "AMT"){
          e.target.value = formatAmount(parseAmount(e.target.value));
        } else {
          e.target.value = onlyDigitsDot(e.target.value);
        }
      }

      if(e.target.name === "startDate" || e.target.name === "endDate"){
        if(startEl.value){
          endEl.min = startEl.value;
        }
      }
    });


    /* 제출 처리 */
    const editForm = modal.querySelector("form");
    editForm.addEventListener("submit", (e)=>{
      const type = typeSel.value;
      const v = modal.querySelector('input[name="valueNum"]');   // 정확한 선택자

      if(v){
        if(type === "AMT")  v.value = onlyDigits(v.value);
        if(type === "RATE") v.value = onlyDigitsDot(v.value);
      }

      if(startEl.value && endEl.value && endEl.value < startEl.value){
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
