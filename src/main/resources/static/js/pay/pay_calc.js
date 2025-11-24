console.log("🔥 pay_calc.js 로드됨");


/* =====================================================
   🔥 전역 에러 캡처
===================================================== */
window.addEventListener("error", function(event) {
    console.error("[JS ERROR]", event.message, event.filename, event.lineno);
});



/* =====================================================
   공통 유틸
===================================================== */
const onlyDigits    = (s)=> (s||'').replace(/[^\d]/g,'');
const onlyDigitsDot = (s)=> (s||'').replace(/[^\d.]/g,'');
const formatAmount  = (v)=> (v==''||v==null) ? '' : Number(v).toLocaleString('ko-KR');
const parseAmount   = (v)=> Number(onlyDigits(v));



/* =====================================================
   공통 검증 함수 (등록/수정)
===================================================== */
function validateCalcRule(form) {

    const itemCode   = form.querySelector('select[name="item.itemCode"]');
    const ruleType   = form.querySelector('select[name="ruleType"]');
    const priority   = form.querySelector('input[name="priority"]');
    const startDate  = form.querySelector('input[name="startDate"]');
    const status     = form.querySelector('select[name="status"]');
    const targetType = form.querySelector('select[name="targetType"]');
    const targetCode = form.querySelector('[name="targetCode"]');

    const valueNum   = form.querySelector('input[name="valueNum"]');
    const calcFormula= form.querySelector('textarea[name="calcFormula"]');

    let msg = null;

    if(!itemCode?.value) msg = "항목 코드는 필수입니다.";
    else if(!ruleType?.value) msg = "규칙 유형은 필수입니다.";
    else if(!priority?.value) msg = "우선순위는 필수입니다.";
    else if(!startDate?.value) msg = "시작일은 필수입니다.";
    else if(!status?.value) msg = "상태는 필수입니다.";
    else if(!targetType?.value) msg = "대상구분은 필수입니다.";
	// targetCode 는 뒤에서 처리됨
    else if(ruleType.value !== "FORMULA" && !valueNum.value)
      msg = "금액/비율 규칙일 경우 숫자값은 필수입니다.";
    else if(!calcFormula?.value)
      msg = "계산공식은 필수입니다.";

	    if(!msg && targetType.value === "EMP") {
	        if(!targetCode?.value || targetCode.value.length !== 7) {
	            msg = "사원코드는 7자리여야 합니다.";
	        }
	    }

    return msg;
}



/* =====================================================
   대상구분 스위처 (등록/수정 공통)
===================================================== */
function bindTargetSwitcher(prefix) {

    const typeSel  = document.getElementById(`${prefix}-target-type`);
    const inputEl  = document.getElementById(`${prefix}-target-code-input`);
    const deptSel  = document.getElementById(`${prefix}-target-dept`);
    const gradeSel = document.getElementById(`${prefix}-target-grade`);

    if(!typeSel) return;

    console.log(`🎯 bindTargetSwitcher 실행됨: ${prefix}`);

    const updateUI = () => {
        const type = typeSel.value;

        // 모든 요소 숨기고 name 제거
        [inputEl, deptSel, gradeSel].forEach(el=>{
            if(el){
                el.classList.add("d-none");
                el.removeAttribute("name");
            }
        });

        // 1) EMP 선택 → 입력칸만 보여주고 name 설정 (값 절대 건드리지 않음)
        if(type === "EMP"){
            inputEl.classList.remove("d-none");
            inputEl.setAttribute("name", "targetCode");
            return;
        }

        // 2) DEPT 선택 → deptSelect 표시 + 사번칸 비우기
        if(type === "DEPT"){
            deptSel.classList.remove("d-none");
            deptSel.setAttribute("name", "targetCode");
            if(inputEl) inputEl.value = "";
            return;
        }

        // 3) GRADE 선택 → gradeSelect 표시 + 사번칸 비우기
        if(type === "GRADE"){
            gradeSel.classList.remove("d-none");
            gradeSel.setAttribute("name", "targetCode");
            if(inputEl) inputEl.value = "";
            return;
        }

        // 4) ALL 선택 → 사번칸 비우기 + 모두 숨김
        if(inputEl) inputEl.value = "";
    };

    updateUI();
    typeSel.addEventListener("change", updateUI);
}



/* =====================================================
   등록 모달
===================================================== */
document.addEventListener("DOMContentLoaded", () => {

    const createModal = document.getElementById("calcCreateModal");

    if(createModal){
        const form = document.getElementById("createRuleForm");

        createModal.addEventListener("input", (e)=>{
            if(e.target.matches(".amount-input")){
                const rt = form.querySelector('select[name="ruleType"]').value;
                if(rt === "AMT"){
                    e.target.value = formatAmount(parseAmount(e.target.value));
                } else {
                    e.target.value = onlyDigitsDot(e.target.value);
                }
            }
        });

        form.addEventListener("submit", (e)=>{
            const msg = validateCalcRule(form);
            if(msg){
                e.preventDefault();
                const box = document.getElementById("create-error-box");
                box.classList.remove("d-none");
                box.querySelector("span").innerText = msg;
                return;
            }
        });
    }

    bindTargetSwitcher("create");
});



/* =====================================================
   수정 모달 (여기서 진짜 핵심!)
===================================================== */
document.addEventListener("show.bs.modal", (evt)=>{

    const modal = evt.target;
    const id = modal.getAttribute("id");
    if(!id || !id.startsWith("calcEditModal-")) return;

    const ruleId = id.replace("calcEditModal-", "");
    
    console.log(`🔧 수정 모달 표시됨: ruleId=${ruleId}`);

    /* 대상 스위처 활성화 */
    bindTargetSwitcher(`edit-${ruleId}`);

    /* 금액/비율 입력 처리 */
    const form = modal.querySelector("form");

    modal.addEventListener("input", (e)=>{
        if(e.target.matches(".amount-input")){
            const rt = form.querySelector('select[name="ruleType"]').value;
            if(rt === "AMT"){
                e.target.value = formatAmount(parseAmount(e.target.value));
            } else {
                e.target.value = onlyDigitsDot(e.target.value);
            }
        }
    });

    /* 저장 검증 */
    form.addEventListener("submit", (e)=>{
        const msg = validateCalcRule(form);

        // targetType 이 ALL 아닌데 name="targetCode" 이 없으면 에러
        const targetType = form.querySelector('select[name="targetType"]').value;
        const targetCode = form.querySelector('[name="targetCode"]');

        if(targetType !== "ALL" && !targetCode){
            e.preventDefault();
            const box = modal.querySelector(".modal-error-box");
            box.classList.remove("d-none");
            box.querySelector("span").innerText = "대상구분이 전체가 아닐 경우 대상코드는 필수입니다.";
            return;
        }

        if(msg){
            e.preventDefault();
            const box = modal.querySelector(".modal-error-box");
            box.classList.remove("d-none");
            box.querySelector("span").innerText = msg;
            return;
        }
    });

});

/* =====================================================
   저장 직전 콤마 제거 (등록 + 수정 공통)
===================================================== */
document.addEventListener("submit", (e) => {

    const form = e.target;
    if (!form.matches("form")) return; // 폼이 아니면 무시

    console.log("💾 submit 시 콤마 제거 실행");

    const valueInput = form.querySelector("input[name='valueNum']");
    if (valueInput && valueInput.value) {
        // 콤마 제거
        valueInput.value = valueInput.value.replace(/,/g, "");
        console.log("➡ valueNum cleaned:", valueInput.value);
    }
});

