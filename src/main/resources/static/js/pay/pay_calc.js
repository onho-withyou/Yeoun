console.log("🔥 pay_calc.js 로드됨");

/* =====================================================
    전역 에러 캡처
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
    else if(ruleType.value !== "FORMULA" && !valueNum.value)
        msg = "금액/비율 규칙일 경우 숫자값은 필수입니다.";
    else if(!calcFormula?.value)
        msg = "계산공식은 필수입니다.";
    else if(/\s/.test(calcFormula.value))
        msg = "계산공식에는 공백을 포함할 수 없습니다.";

    // 사번 검증
    if(!msg && targetType.value === "EMP") {
        if(!targetCode?.value || targetCode.value.length !== 7) {
            msg = "사원코드는 7자리여야 합니다.";
        }
    }

    // ⭐ 비율 검증 추가!
    if(!msg && ruleType.value === "RATE"){
        const num = parseFloat(valueNum.value);
        if(isNaN(num) || num < 0 || num > 1){
            msg = "비율은 0~1 사이 값만 입력 가능합니다. (예: 0.1 = 10%)";
        }
    }
	
	// ⭐ 금액 검증 추가! (1000원 이상)
	if(!msg && ruleType.value === "AMT"){
	    const num = parseAmount(valueNum.value);
	    if(isNaN(num) || num < 1000){
	        msg = "금액은 1,000원 이상 입력해야 합니다.";
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

        [inputEl, deptSel, gradeSel].forEach(el=>{
            if(el){
                el.classList.add("d-none");
                el.removeAttribute("name");
            }
        });

        if(type === "EMP"){
            inputEl.classList.remove("d-none");
            inputEl.setAttribute("name", "targetCode");
            return;
        }

        if(type === "DEPT"){
            deptSel.classList.remove("d-none");
            deptSel.setAttribute("name", "targetCode");
            return;
        }

        if(type === "GRADE"){
            gradeSel.classList.remove("d-none");
            gradeSel.setAttribute("name", "targetCode");
            return;
        }
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

        /* 모달 닫힐 때 초기화 */
        createModal.addEventListener("hidden.bs.modal", () => {
            form.reset();

            const box = document.getElementById("create-error-box");
            if (box) {
                box.classList.add("d-none");
                box.querySelector("span").innerText = "";
            }
        });

        createModal.addEventListener("input", (e)=>{
            if(e.target.matches(".amount-input")){
                const rt = form.querySelector('select[name="ruleType"]').value;
                e.target.value = (rt === "AMT")
                    ? formatAmount(parseAmount(e.target.value))
                    : onlyDigitsDot(e.target.value);

                // ⭐ 실시간 비율 검증
                if(rt === "RATE"){
                    const num = parseFloat(e.target.value);
                    if(isNaN(num) || num < 0 || num > 1){
                        e.target.classList.add("is-invalid");
                    } else {
                        e.target.classList.remove("is-invalid");
                    }
                }
				// ⭐ 실시간 금액 검증 (1000 미만 → invalid)
				if(rt === "AMT"){
				    const num = parseAmount(e.target.value);
				    if(isNaN(num) || num < 1000){
				        e.target.classList.add("is-invalid");
				    } else {
				        e.target.classList.remove("is-invalid");
				    }
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
   수정 모달
===================================================== */
document.addEventListener("show.bs.modal", (evt)=>{

    const modal = evt.target;
    const id = modal.getAttribute("id");
    if(!id || !id.startsWith("calcEditModal-")) return;

    const ruleId = id.replace("calcEditModal-", "");

    console.log(`🔧 수정 모달 표시됨: ruleId=${ruleId}`);

    bindTargetSwitcher(`edit-${ruleId}`);

    const form = modal.querySelector("form");

    modal.addEventListener("input", (e)=>{
        if(e.target.matches(".amount-input")){
            const rt = form.querySelector('select[name="ruleType"]').value;
            e.target.value = (rt === "AMT")
                ? formatAmount(parseAmount(e.target.value))
                : onlyDigitsDot(e.target.value);

            // ⭐ 실시간 비율 검증
            if(rt === "RATE"){
                const num = parseFloat(e.target.value);
                if(isNaN(num) || num < 0 || num > 1){
                    e.target.classList.add("is-invalid");
                } else {
                    e.target.classList.remove("is-invalid");
                }
            }
			// ⭐ 실시간 금액 검증 (1000 미만 → invalid)
			if(rt === "AMT"){
			    const num = parseAmount(e.target.value);
			    if(isNaN(num) || num < 1000){
			        e.target.classList.add("is-invalid");
			    } else {
			        e.target.classList.remove("is-invalid");
			    }
			}

        }
    });

    form.addEventListener("submit", (e)=>{
        const msg = validateCalcRule(form);

        const box = modal.querySelector(".modal-error-box");

        if(msg){
            e.preventDefault();
            box.classList.remove("d-none");
            box.querySelector("span").innerText = msg;
            return;
        }
    });

});


/* =====================================================
   콤마 제거 (등록+수정 공통)
===================================================== */
document.addEventListener("submit", (e) => {

    const form = e.target;
    if (!form.matches("form")) return;

    console.log("💾 submit 시 콤마 제거 실행");

    const valueInput = form.querySelector("input[name='valueNum']");
    if (valueInput && valueInput.value) {
        valueInput.value = valueInput.value.replace(/,/g, "");
    }
});


/* =====================================================
   계산공식 공백 제거
===================================================== */
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("textarea[name='calcFormula']").forEach(el => {
        el.addEventListener("input", (e) => {
            e.target.value = e.target.value.replace(/\s+/g, "");
        });
    });
});


/* =====================================================
   날짜 조건 처리 (시작일 ≤ 종료일)
===================================================== */
document.addEventListener("change", (e) => {
    if (e.target.matches('input[name="startDate"]')) {

        const startInput = e.target;
        const form = startInput.closest("form");
        const endInput = form.querySelector('input[name="endDate"]');

        if (!endInput) return;

        endInput.min = startInput.value;

        if (endInput.value && endInput.value < startInput.value) {
            endInput.value = startInput.value;
        }
    }
});
