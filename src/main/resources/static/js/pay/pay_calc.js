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
async function validateCalcRule(form) {

    const itemSel    = form.querySelector('select[name="item.itemCode"]');
    const ruleType   = form.querySelector('select[name="ruleType"]');
    const priorityEl = form.querySelector('input[name="priority"]');
    const startDate  = form.querySelector('input[name="startDate"]');
    const status     = form.querySelector('select[name="status"]');
    const targetType = form.querySelector('select[name="targetType"]');
    const targetCode = form.querySelector('[name="targetCode"]');

    const valueNum   = form.querySelector('input[name="valueNum"]');
    const calcFormula= form.querySelector('textarea[name="calcFormula"]');

    let msg = null;

    // 기본 검증들 유지
    if(!itemSel?.value) msg = "항목 코드는 필수입니다.";
    else if(!ruleType?.value) msg = "규칙 유형은 필수입니다.";
    else if(!priorityEl?.value) msg = "우선순위는 필수입니다.";
    else if(!startDate?.value) msg = "시작일은 필수입니다.";
    else if(!status?.value) msg = "상태는 필수입니다.";
    else if(!targetType?.value) msg = "대상구분은 필수입니다.";
    else if(ruleType.value !== "FORMULA" && !valueNum.value)
        msg = "금액/비율 규칙일 경우 숫자값은 필수입니다.";
    else if(!calcFormula?.value)
        msg = "계산공식은 필수입니다.";
    else if(/\s/.test(calcFormula.value))
        msg = "계산공식에는 공백을 포함할 수 없습니다.";

    if (msg) return msg;

    // 금액/비율 검증
    if(ruleType.value === "RATE"){
        const num = parseFloat(valueNum.value);
        if(isNaN(num) || num < 0 || num > 1){
            return "비율은 0~1 사이 값만 입력 가능합니다. (예: 0.1 = 10%)";
        }
    }

    if(ruleType.value === "AMT"){
        const num = parseAmount(valueNum.value);
        if(isNaN(num) || num < 1000){
            return "금액은 1,000원 이상 입력해야 합니다.";
        }
    }

    // 사번 검증
    if(targetType.value === "EMP"){
        if(!targetCode?.value || targetCode.value.length !== 7){
            return "사원코드는 7자리여야 합니다.";
        }
    }

    /* =====================================================
       ⭐ 우선순위 중복 검증 AJAX
    ====================================================== */
    const itemCode = itemSel.value;
    const priority = priorityEl.value;

    // 수정모달이면 ruleId 있음
    const ruleId = form.dataset.ruleId || null;

    const url = `/pay/rule_calc/checkPriority?itemCode=${itemCode}&priority=${priority}`
              + (ruleId ? `&ruleId=${ruleId}` : "");

    try {
        const res = await fetch(url);
        const isDup = await res.json();

        if (isDup) {
            return "해당 항목에서 이미 사용 중인 우선순위입니다. 다른 번호를 입력하세요.";
        }
    } catch (e) {
        console.error("우선순위 중복 체크 실패", e);
        return "우선순위 중복 검증 중 오류가 발생했습니다.";
    }

    return null; // 성공
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

            // ⭐ 자동완성 항상 바인딩 ⭐
            bindEmpAutocomplete(inputEl);

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

		form.addEventListener("submit", async (e)=>{
		    e.preventDefault();
		    const msg = await validateCalcRule(form);

		    if(msg){
		        const box = document.getElementById("create-error-box");
		        box.classList.remove("d-none");
		        box.querySelector("span").innerText = msg;
		        return;
		    }

		    form.submit();   // ⭐ 이게 있어야 최종 제출됨
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
	
	// ⭐⭐⭐ 가장 중요한 부분 — dataset에 ruleId 주입 ⭐⭐⭐
	    form.dataset.ruleId = ruleId;

    // UI 스위치 먼저 실행
    bindTargetSwitcher(`edit-${ruleId}`);

    const form = modal.querySelector("form");

    /* ⭐ 자동완성 바인딩 추가 ⭐ */  
    const empInput = modal.querySelector("input[name='targetCode']");
    if(empInput) {
        console.log("✨ EMP 자동완성 활성화");
        bindEmpAutocomplete(empInput);
    }

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

            // ⭐ 실시간 금액 검증
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

	form.addEventListener("submit", async (e)=>{
	    e.preventDefault();
	    const msg = await validateCalcRule(form);
	    const box = modal.querySelector(".modal-error-box");

	    if(msg){
	        box.classList.remove("d-none");
	        box.querySelector("span").innerText = msg;
	        return;
	    }

	    form.submit();  
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
/* =====================================================
   사원 자동완성 (등록 / 수정)
===================================================== */
function bindEmpAutocomplete(input) {
    if (!input) return;

    const box = input.closest(".autocomplete-box");
    const listUI = box.querySelector(".autocomplete-list");

    input.addEventListener("input", async (e) => {
        const keyword = e.target.value.trim();
        if (!keyword || keyword.length < 1) {
            listUI.classList.add("d-none");
            return;
        }

        try {
            const res = await fetch(`/pay/rule_calc/searchEmployee?keyword=${encodeURIComponent(keyword)}`);
            const arr = await res.json();

            listUI.innerHTML = ""; // 초기화

            if (arr.length === 0) {
                listUI.classList.add("d-none");
                return;
            }

            arr.forEach(emp => {
                const li = document.createElement("li");
                li.className = "list-group-item list-group-item-action";
                li.style.cursor = "pointer";
                li.innerHTML = `${emp.empName} <small class="text-muted">${emp.empId}</small>`;
                li.addEventListener("click", () => {
                    input.value = emp.empId; // 사번 입력
                    listUI.classList.add("d-none");
                });
                listUI.appendChild(li);
            });

            listUI.classList.remove("d-none");

        } catch (err) {
            console.error("자동완성 오류", err);
        }
    });

    // 외부 클릭 → 자동완성 닫기
    document.addEventListener("click", (evt) => {
        if (!box.contains(evt.target)) {
            listUI.classList.add("d-none");
        }
    });
}


/* ⬇ 등록 모달에 활성화 */
document.addEventListener("DOMContentLoaded", () => {
    const createEmpInput = document.getElementById("create-target-code-input");
    bindEmpAutocomplete(createEmpInput);
});


/* ⬇ 수정 모달에 활성화 */
document.addEventListener("show.bs.modal", (evt) => {
    const modal = evt.target;
    const id = modal.getAttribute("id");
    if (!id || !id.startsWith("calcEditModal-")) return;

    const input = modal.querySelector("input[name='targetCode']");
    bindEmpAutocomplete(input);
});


/*클릭시 복사됨!*/
document.addEventListener("click", function (e) {
  if (e.target.classList.contains("copy-code")) {

    const copyText = e.target.dataset.copy;
    const originalText = e.target.dataset.original; // ← 원래 텍스트

    navigator.clipboard.writeText(copyText).then(() => {

      // 클릭한 요소를 “복사됨!” 으로 변경
      e.target.innerText = "복사됨!";
      e.target.style.color = "#198754";

      // 1초 뒤 원래 텍스트로 복구
      setTimeout(() => {
        e.target.innerText = originalText;
        e.target.style.color = "#6c757d";
      }, 800);
    });
  }
});
