    document.addEventListener('DOMContentLoaded', function () {

      // ================== 1) Grid 생성 ==================
      const grid = new tui.Grid({
        el: document.getElementById('empGrid'),
        scrollX: false,
        scrollY: true,
        rowHeight: 36,
        bodyHeight: 360,
        rowHeaders: ['rowNum'],
        columns: [
          { header: '사번', name: 'empId', width: 110, align: 'center' },
          { header: '이름', name: 'empName', width: 90, align: 'center' },
          { header: '부서', name: 'deptName', minWidth: 120 },
          { header: '직급', name: 'posName', width: 80, align: 'center' }
        ],
        data: {
          api: {
            readData: {
              // Thymeleaf로 URL 렌더링
              url: /*[[@{/auth/manage/emp-data}]]*/ '/auth/manage/data',
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
        fetch(/*[[@{/auth/manage/}]]*/'/auth/manage/' + empId + '/roles')
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

      // ================== 4) 권한 저장 버튼 클릭 (AJAX or form submit) ==================
      btnSaveRole.addEventListener('click', function () {
        if (!formEmpId.value) {
          alert('사원을 먼저 선택해주세요.');
          return;
        }
        document.getElementById('roleAssignForm').submit();
      });

    });
