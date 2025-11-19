/**
 * 사원 등록 화면 전용 JS (emp.js)
 * 
 * 1. 조직/직무 관련 JS  (상위부서 → 하위부서 연계)
 * 2️. 주소검색 API (다음 우편번호)
 */

  /**
   * ============================================================
   * 조직/직무 관련 JS
   * ------------------------------------------------------------
   * - 상위부서(topDept) 선택 시 하위부서(deptId) 자동 필터링
   * - 부서 선택 유지 / 복원 로직 포함
   * ============================================================
   */
  function initOrgSelects() {
    const topSel  = document.getElementById('topDept');  // 본부 (상위부서)
    const deptSel = document.getElementById('deptId');   // 부서
    const posSel  = document.getElementById('posCode');  // 직급

    if (!topSel || !deptSel || !posSel) return; // 다른 화면 대비 가드
	
	if (topSel.disabled) {
	    return;
	  }

    // 부서 option 백업 (placeholder 제외)
    const allDept = Array.from(deptSel.querySelectorAll('option')).filter(o => o.value);

    // 공통: 셀렉트 초기화 + placeholder + disabled 설정 (부서용)
    function resetDept(phText, disabled = true) {
      deptSel.innerHTML = '';
      const ph = document.createElement('option');
      ph.value = '';
      ph.textContent = phText;
      ph.disabled = true;
      ph.selected = true;
      deptSel.appendChild(ph);
      deptSel.disabled = disabled;
    }

    // 직급 셀렉트의 활성/비활성만 제어 (목록은 그대로 둠)
    function updatePosState(deptId) {
      const firstOpt = posSel.querySelector('option[value=""]') || posSel.options[0];

      if (!deptId) {
        // 부서가 없으면 직급 막기
        if (firstOpt) firstOpt.textContent = '먼저 부서를 선택하세요';
        posSel.value = '';
        posSel.disabled = true;
      } else {
        // 부서가 선택되면 직급 활성화
        if (firstOpt) firstOpt.textContent = '직급을 선택하세요';
        posSel.disabled = false;
      }
    }

    // 상위부서 선택에 따라 부서 필터링
    function filterDeptByTop(topId) {
      const prevDept = deptSel.value;

      if (!topId) {
        // 본부 안 고른 상태
        resetDept('먼저 본부를 선택하세요', true);
        updatePosState(null); // 직급도 막기
        return;
      }

      // 본부 선택됨 -> 부서 활성화 + 해당 상위부서의 하위부서만 노출
      resetDept('부서를 선택하세요', false);

      allDept
        .filter(o => o.getAttribute('data-parent') === topId)  // data-parent = parentDeptId
        .forEach(o => deptSel.appendChild(o.cloneNode(true)));

      // 이전에 선택한 부서가 여전히 목록에 있으면 복원
      if ([...deptSel.options].some(o => o.value === prevDept)) {
        deptSel.value = prevDept;
        updatePosState(prevDept);
      } else {
        updatePosState(null); // 유효한 부서가 없으면 직급도 막기
      }
    }

    // 최초 로드 시 (수정 폼 대비)
    const selectedDeptOpt = deptSel.querySelector('option:checked[value]');

    if (selectedDeptOpt) {
      // 폼에 이미 부서가 선택되어 있는 경우 → 상위부서/부서/직급 상태 맞춰주기
      const parentDeptId = selectedDeptOpt.getAttribute('data-parent');
      if (parentDeptId) {
        topSel.value = parentDeptId;
        filterDeptByTop(parentDeptId);
        deptSel.value = selectedDeptOpt.value;
        updatePosState(selectedDeptOpt.value);
      } else {
        // parentDeptId 정보가 없으면 그냥 현재 topDept 기준으로만 처리
        filterDeptByTop(topSel.value);
        updatePosState(deptSel.value);
      }
    } else {
      // 신규 등록 등 -> 기본 상태
      filterDeptByTop(topSel.value);
      updatePosState(deptSel.value);
    }

    // 이벤트 바인딩
    topSel.addEventListener('change', e => filterDeptByTop(e.target.value));
    deptSel.addEventListener('change', e => updatePosState(e.target.value));
  }

  // DOMContentLoaded 시 초기화
  document.addEventListener('DOMContentLoaded', initOrgSelects);




  /**
   * ============================================================
   * 주소검색 API (다음 우편번호)
   * ------------------------------------------------------------
   * - HTML에서 onclick="btnSearchZip()"으로 호출됨
   * - daum Postcode 라이브러리 필요 (HTML <script>로 로드)
   * ============================================================
   */
  window.btnSearchZip = function() {
    if (!window.daum || !daum.Postcode) {
      alert('우편번호 스크립트가 아직 로드되지 않았습니다. 잠시 후 다시 시도하세요.');
      return;
    }

    new daum.Postcode({
      oncomplete: function(data) {
        // 기본 주소(도로명/지번) 선택 타입에 따라 결정
        var addr = '';             // 최종 주소
        var extraRoadAddr = '';    // 참고항목 (법정동/건물명 등)

        if (data.userSelectedType === 'R') { // 도로명
          addr = data.roadAddress;

          // 참고항목 생성
          if (data.bname !== '' && /[동|로|가]$/g.test(data.bname)) {
            extraRoadAddr += data.bname;
          }
          if (data.buildingName !== '' && data.apartment === 'Y') {
            extraRoadAddr += (extraRoadAddr !== '' ? ', ' + data.buildingName : data.buildingName);
          }
          if (extraRoadAddr !== '') {
            addr += ' (' + extraRoadAddr + ')';
          }
        } else { // J: 지번주소
          addr = data.jibunAddress;
        }

        // 값 세팅
        document.getElementById('zipcode').value = data.zonecode; // 우편번호
        document.getElementById('address').value = addr;          // 기본주소
        document.getElementById('addressDetail').focus();         // 상세주소로 커서 이동
      }
    }).open();
  };
  
  /**
     * ============================================================
     * 연락처 자동 하이픈
     * ============================================================
     */
  document.addEventListener("DOMContentLoaded", () => {
    const phoneInput = document.getElementById("phone");
    if (!phoneInput) return;

    phoneInput.addEventListener("input", () => {
      let value = phoneInput.value.replace(/[^0-9]/g, ""); // 숫자만 추출

      // 최대 11자리까지만 유지
      value = value.substring(0, 11);

      // 하이픈 삽입
      if (value.length < 4) {
        phoneInput.value = value;
      } else if (value.length < 8) {
        phoneInput.value = `${value.slice(0, 3)}-${value.slice(3)}`;
      } else {
        phoneInput.value = `${value.slice(0, 3)}-${value.slice(3, 7)}-${value.slice(7)}`;
      }
    });
  });
  
  
  
  /**
     * ============================================================
     * 주민등록번호
     * ============================================================
     */
  document.addEventListener("DOMContentLoaded", () => {
    const el = document.getElementById("rrn");
    if (!el) return;

    el.addEventListener("input", () => {
      let v = el.value.replace(/\D/g, "");   // 숫자만
      v = v.slice(0, 13);                    // 최대 13자리(하이픈 제외)
      if (v.length > 6) v = v.slice(0, 6) + "-" + v.slice(6);
      el.value = v;                           // ######-####### 형태
    });
  });
  
  
