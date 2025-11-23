// empList.js 
// 사원 목록 테이블 그리드 (Toast Grid 클라이언트 페이징 버전)

let empGrid = null;
let empDetailModal = null;
let currentEmpId = null;
let detailMode   = null; 
let posSortAsc = true; 

document.addEventListener('DOMContentLoaded', () => {
	const holder = document.getElementById('empMsgHolder');
	if (holder) {
		const msg = holder.dataset.msg;
		if(msg) {
			alert(msg);
		}
	}
	
	empDetailModal = new bootstrap.Modal(document.getElementById('empDetailModal'));
	
	// 수정 버튼 클릭 이벤트
	const editBtn = document.getElementById('editBtn');
	if (editBtn) {
		editBtn.addEventListener('click', () => {
			// 사원 목록에서 뜬 상세 모달
			if (!currentEmpId) {
				alert('선택된 사원이 없습니다.');
				return;
			}
			// 수정 화면으로 이동
			window.location.href = `/emp/edit/${currentEmpId}`;
		});
	}
	
	// 검색 버튼
	const searchBtn = document.getElementById('btnSearch');
	if (searchBtn) {
		searchBtn.addEventListener('click', () => {
			loadEmpList();   // 다시 전체 불러오기
		});
	}	
	
	// 엔터 검색
	const keywordInput = document.getElementById('keyword');
	if (keywordInput) {
		keywordInput.addEventListener('keydown', (e) => {
			if (e.key === 'Enter') {
				e.preventDefault();
		        loadEmpList();
			}
		});
	}
	
	// 부서 변경
	const deptSelect = document.getElementById('deptId');
	if (deptSelect) {
		deptSelect.addEventListener('change', () => {
			loadEmpList();
		});
	}
	
	initEmpGrid();
	loadEmpList();
});

// ================================
//  서버에서 전체 리스트 받아오기
// ================================
function loadEmpList() {
	const keywordInput = document.getElementById('keyword');
  	const deptSelect   = document.getElementById('deptId');

	const keyword = keywordInput ? keywordInput.value : '';
 	const deptId  = deptSelect   ? deptSelect.value   : '';

  	const params = new URLSearchParams({
    	keyword: keyword,
    	deptId: deptId
  	});

  	fetch('/emp/data?' + params.toString())
    	.then(res => res.json())
    	.then(data => {
     	 	// data = List<EmpListDTO>
      		empGrid.resetData(data);
		})
    	.catch(err => {
      		console.error(err);
      		alert('사원 목록 불러오기 실패');
    	});
}

// ================================
//  Toast Grid 생성 (클라이언트 페이징)
// ================================
function initEmpGrid() {
	empGrid = new tui.Grid({
		el: document.getElementById('grid'),
    	rowHeaders: [],
    	scrollX: true,
    	scrollY: true,
    	editable: false,
    	columnOptions: {
      		resizable: true,
	  		useClientSort: true
    	},
    	pagination: true,         
    	pageOptions: {
      		useClient: true,         
      		perPage: 10            
    	},
    	columns: [
      	{ 
			header: '입사일자', 
			name: 'hireDate', 
			align: 'center', 
			sortable: true 
		},
      	{ 
			header: '사원번호', 
			name: 'empId',    
			align: 'center', 
			sortable: true 
		},
      	{ 
			header: '성명',     
			name: 'empName',  
			align: 'center', 
			sortable: true 
		},
      	{ 
			header: '부서',     
			name: 'deptName', 
			align: 'center', 
			sortable: true 
		},
      	{ 
			header: '직급서열',     
			name: 'rankOrder',  
			hidden: true,  
			sortable: true 
		},
      	{ 
			header: '직급',     
			name: 'posName',  
			align: 'center', 
			sortable: true 
		},
      	{ 
			header: '상태',     
			name: 'statusName', 
			align: 'center' 
		},
      	{ 
			header: '전화번호', 
			name: 'mobile',   
			align: 'center' 
		},
      	{ 
			header: 'Email',    
			name: 'email',    
			width: 220 
		},
      	{
        	header: ' ',
        	name: 'btn',
        	width: 110,
        	align: 'center',
        	formatter: () => "<button type='button' class='btn btn-info btn-sm'>상세</button>"
      	}
    	]
  });

  // 상세 버튼
  empGrid.on('click', ev => {
	if (ev.columnName !== 'btn') return;
    const row = empGrid.getRow(ev.rowKey);
    if (!row || !row.empId) return;
    showEmpDetail(row.empId);
  });

  // 직급 헤더 클릭하면 rankOrder 기준으로 정렬
  empGrid.on('beforeSort', ev => {
    const { columnName } = ev;

    if (columnName !== 'posName') {
      // 다른 컬럼은 토스트 기본 정렬 그대로 사용
      return;
    }

    // 기본 posName 정렬 막기
    ev.stop();

    // 방향 토글 (클릭할 때마다 오름/내림 바꾸기)
    posSortAsc = !posSortAsc;

    // 현재 데이터 가져와서 복사
    const data = empGrid.getData().slice();

    // rankOrder 숫자 기준으로 정렬
    data.sort((a, b) => {
      const ao = Number(a.rankOrder ?? 0);
      const bo = Number(b.rankOrder ?? 0);

      return posSortAsc ? (ao - bo) : (bo - ao);
    });

    // 정렬된 데이터로 다시 세팅
    empGrid.resetData(data);
  });

}

// ================================
//  사원 상세보기 (그대로 유지)
// ================================
function showEmpDetail(empId) {
  currentEmpId = empId;

  const editBtn = document.getElementById('editBtn');
  if (editBtn) editBtn.style.display = '';

  fetch(`/emp/detail/${empId}`)
    .then(res => res.json())
    .then(d => {
      document.getElementById('empDetailModalTitle').innerText = '사원 상세';

      document.getElementById('d-empName').textContent = d.empName;
      document.getElementById('d-empId').textContent = d.empId;
      document.getElementById('d-deptName').textContent = d.deptName;
      document.getElementById('d-posName').textContent = d.posName;
      document.getElementById('d-gender').textContent = d.gender === 'M' ? '남' : d.gender === 'F' ? '여' : '—';
      document.getElementById('d-hireDate').textContent = d.hireDate;
      document.getElementById('d-mobile').textContent = d.mobile;
      document.getElementById('d-email').textContent = d.email;
      document.getElementById('d-address').textContent = d.address;
      document.getElementById('d-rrn').textContent = d.rrnMasked;
      document.getElementById('d-bank').textContent = d.bankInfo;

      const photo = document.getElementById('d-photo');
      photo.onerror = () => { photo.src = '/img/default-profile.png'; };
      photo.src = d.photoPath || '/img/default-profile.png';

      empDetailModal.show();
    })
    .catch(() => alert('상세정보 불러오기 실패'));
}
