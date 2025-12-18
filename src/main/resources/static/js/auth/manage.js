document.addEventListener('DOMContentLoaded', function () {

  // ================== 1) Grid 생성 ==================
  const grid = new tui.Grid({
    el: document.getElementById('empGrid'),
    scrollY: true,
    bodyHeight: 400,
    rowHeaders: ['rowNum'],
    columns: [
      { 
		header: '사번', 
		name: 'empId', 
		width: 110, 
		align: 'center' 
	  },
      { 
		header: '이름', 
		name: 'empName', 
		width: 90, 
		align: 'center' 
	  },
      { 
		header: '부서', 
		name: 'deptName', 
		minWidth: 120 
	  },
      { 
		header: '직급', 
		name: 'posName', 
		width: 80, 
		align: 'center' 
	  }
    ],
    data: {
      api: {
        readData: {
          url: '/auth/manage/data',
          method: 'GET'
        }
      }
    }
  });

  // ================== 2) 검색 폼 연동 ==================
  const searchForm = document.getElementById('empSearchForm');

  searchForm.addEventListener('submit', function (e) {
    e.preventDefault();

    const keyword = searchForm.querySelector('input[name="keyword"]').value.trim();
    const deptId  = searchForm.querySelector('select[name="deptId"]').value;

    // 1페이지부터 다시 조회
    grid.readData(1, { keyword, deptId }, true);
  });

  // 첫 로딩
  grid.readData(1, {}, true);

  // ================== 3) 행 클릭 시 오른쪽 패널에 반영 ==================
  const selEmpIdEl   = document.getElementById('sel-empId');
  const selEmpNameEl = document.getElementById('sel-empName');
  const selDeptPosEl = document.getElementById('sel-deptPos');
  const formEmpId    = document.getElementById('formEmpId');
  const btnSaveRole  = document.getElementById('btnSaveRole');
  const roleCheckboxes = document.querySelectorAll('.role-checkbox');

  grid.on('click', function (ev) {
    const rowKey = ev.rowKey;
    if (rowKey == null) return;

    const row = grid.getRow(rowKey);
    if (!row) return;

    const empId   = row.empId;
    const empName = row.empName;
    const dept    = row.deptName || '';
    const pos     = row.posName || '';

    selEmpIdEl.textContent   = empId;
    selEmpNameEl.textContent = empName;
    selDeptPosEl.textContent = dept && pos ? `${dept} / ${pos}` : (dept || pos || '—');
    formEmpId.value          = empId;

    btnSaveRole.disabled = false;

    // 사원별 역할 Ajax로 가져오기
    fetch('/auth/manage/' + empId + '/roles')
      .then(res => res.json())
      .then(roleCodes => {
        roleCheckboxes.forEach(cb => {
          cb.checked = roleCodes.includes(cb.value);
        });
      })
      .catch(err => {
        console.error('권한 조회 실패', err);
        alert('권한 정보를 불러오는 중 오류가 발생했습니다.');
      });
  });

  // ================== 4) 권한 저장: confirm -> (SYS_ADMIN 추가 confirm) -> 비번 모달 -> verify -> submit ==================
    const roleForm = document.getElementById('roleAssignForm');

    const adminPwModalEl = document.getElementById('adminPwModal');
    const adminPwModal = adminPwModalEl ? new bootstrap.Modal(adminPwModalEl) : null;

    const adminPwInput = document.getElementById('adminPassword');
    const adminPwError = document.getElementById('adminPwError');
    const btnVerifyAdminPw = document.getElementById('btnVerifyAdminPw');

    btnSaveRole.addEventListener('click', function () {
      // 사원 선택 안 했으면 방어
      if (!formEmpId.value) {
        alert('사원을 선택하세요.');
        return;
      }

      if (!confirm('선택한 권한으로 저장하시겠습니까?')) return;

      // SYS_ADMIN 선택 시 추가 확인
      const sysAdminCb = document.querySelector("input[name='roleCodes'][value='ROLE_SYS_ADMIN']");
      if (sysAdminCb && sysAdminCb.checked) {
        const ok = confirm('⚠ SYS_ADMIN은 최상위 권한입니다.\n정말 부여하시겠습니까?');
        if (!ok) return;
      }

      // 비밀번호 확인 모달
      if (!adminPwModal) {
        // 모달이 없다면 최소 fallback(원하면 막아도 됨)
        roleForm.submit();
        return;
      }

      adminPwInput.value = '';
      adminPwError.classList.add('d-none');
      adminPwModal.show();
    });

    btnVerifyAdminPw?.addEventListener('click', function () {
      const pw = (adminPwInput.value || '').trim();
      if (!pw) {
        adminPwError.textContent = '비밀번호를 입력하세요.';
        adminPwError.classList.remove('d-none');
        return;
      }

      // 비밀번호 재확인 API 호출
      fetch('/auth/manage/verify', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          ...(typeof csrfHeader !== 'undefined' && typeof csrfToken !== 'undefined'
              ? { [csrfHeader]: csrfToken }
              : {})
        },
        body: new URLSearchParams({ password: pw })
      })
      .then(res => res.json())
      .then(data => {
        if (!data || !data.success) {
          adminPwError.textContent = data?.message || '비밀번호 확인 실패';
          adminPwError.classList.remove('d-none');
          return;
        }

        adminPwModal.hide();
        roleForm.submit(); // 검증 성공 시에만 저장 요청
      })
      .catch(err => {
        console.error('verify error', err);
        adminPwError.textContent = '비밀번호 확인 중 오류가 발생했습니다.';
        adminPwError.classList.remove('d-none');
      });
    });

  });

  // 비밀번호 보기/숨기기 (manage 페이지에 없으면 추가)
  function togglePassword(inputId, iconEl) {
    const input = document.getElementById(inputId);
    if (!input) return;

    if (input.type === 'password') {
      input.type = 'text';
      iconEl.classList.remove('bx-hide');
      iconEl.classList.add('bx-show');
    } else {
      input.type = 'password';
      iconEl.classList.remove('bx-show');
      iconEl.classList.add('bx-hide');
    }
  }