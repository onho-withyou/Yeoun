document.addEventListener("DOMContentLoaded", () => {

  /* =====================================================
      공통 유틸
  ===================================================== */
  const onlyDigits    = (s)=> (s||'').replace(/[^\d]/g,'');           // 정수
  const onlyDigitsDot = (s)=> (s||'').replace(/[^\d.]/g,'');         // 소수점 허용
  const formatAmount  = (v)=> (v==''||v==null) ? '' : Number(v).toLocaleString('ko-KR');
  const parseAmount   = (v)=> Number(onlyDigits(v));



  /* =====================================================
      [등록] 모달 처리
  ===================================================== */
  const createModal = document.getElementById("calcCreateModal");

  if(createModal){
    const form       = document.getElementById("calc-create-form");
    const typeSel    = createModal.querySelector('select[name="ruleType"]');
    const startEl    = createModal.querySelector('input[name="startDate"]');
    const endEl      = createModal.querySelector('input[name="endDate"]');
    const valueNumEl = createModal.querySelector('input[name="valueNum"]');

    /* 입력 이벤트 */
    createModal.addEventListener("input", (e)=>{
      if(e.target.matches(".amount-input")){
        const type = typeSel.value;

        // 금액이면 콤마 포맷
        if(type === "AMT"){
          e.target.value = formatAmount(parseAmount(e.target.value));
        } 
        // 비율이면 소수점만 허용
        else if(type === "RATE"){
          e.target.value = onlyDigitsDot(e.target.value);
        } 
        // FORMULA도 숫자 입력이면 그대로 둠
        else {
          e.target.value = onlyDigitsDot(e.target.value);
        }
      }

      // 날짜 validation
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
      [수정] 모달 처리 
  ===================================================== */
  document.addEventListener("shown.bs.modal", (evt)=>{

    const modal = evt.target;
    if(!modal.classList.contains("modal")) return;

    const typeSel    = modal.querySelector('select[name="ruleType"]');
    const startEl    = modal.querySelector('input[name="startDate"]');
    const endEl      = modal.querySelector('input[name="endDate"]');
    const valueNumEl = modal.querySelector('input[name="valueNum"]');

    /* 입력 처리 */
    modal.addEventListener("input", (e)=>{

      if(e.target.matches(".amount-input")){
        const t = typeSel.value;

        if(t === "AMT"){
          e.target.value = formatAmount(parseAmount(e.target.value));
        } 
        else if(t === "RATE"){
          e.target.value = onlyDigitsDot(e.target.value);
        }
        else {
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
      const v = valueNumEl;

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
