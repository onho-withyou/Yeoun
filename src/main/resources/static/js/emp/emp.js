/**
 * 사원 등록 화면 전용 JS (emp.js)
 * 
 * 1. 조직/직무 관련 JS  (상위부서 → 하위부서 연계)
 * 2️. 주소검색 API (다음 우편번호)
 */

(function() {

  /**
   * ============================================================
   * 조직/직무 관련 JS
   * ------------------------------------------------------------
   * - 상위부서(topDept) 선택 시 하위부서(deptId) 자동 필터링
   * - 부서 선택 유지 / 복원 로직 포함
   * ============================================================
   */
  function initOrgSelects() {
    const topSel  = document.getElementById('topDept');
    const deptSel = document.getElementById('deptId');
    if (!topSel || !deptSel) return; // 다른 화면에서 안전하게 넘어가도록 가드

    // 폼에 렌더된 모든 하위부서 option(placeholder 제외) 백업
    const allDept = Array.from(deptSel.querySelectorAll('option')).filter(o => o.value);

    function resetDept(phText = '선택') {
      deptSel.innerHTML = '';
      const ph = document.createElement('option');
      ph.value = '';
      ph.textContent = phText;
      deptSel.appendChild(ph);
    }

    function filterDeptByTop(topId) {
      const prev = deptSel.value;  // 이전 선택 복원용
      resetDept();

      if (!topId) return; // 상위부서 미선택이면 비워둠 (원하면 전체 노출로 변경 가능)

      allDept
        .filter(o => o.getAttribute('data-parent') === topId)  // data-parent 기준
        .forEach(o => deptSel.appendChild(o.cloneNode(true)));

      // 이전 선택 복원
      if ([...deptSel.options].some(o => o.value === prev)) {
        deptSel.value = prev;
      }
    }

    // 최초 로드 시: deptId가 이미 선택되어 있으면 그 parent로 topDept 자동 설정
    const selected = deptSel.querySelector('option:checked');
    if (selected && selected.value) {
      const parentId = selected.getAttribute('data-parent');
      if (parentId) {
        topSel.value = parentId;          // 상위부서 자동 지정
        filterDeptByTop(parentId);        // 하위부서 목록 필터
        deptSel.value = selected.value;   // 선택 복원
        return;
      }
    }
    // 선택 없으면 현재 topDept 값 기준으로만 필터
    filterDeptByTop(topSel.value);

    // 이벤트 바인딩
    topSel.addEventListener('change', e => filterDeptByTop(e.target.value));
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

})();