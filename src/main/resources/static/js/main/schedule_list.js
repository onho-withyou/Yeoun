/**
	일정게시판 JavaScript 
**/

const csrfToken = document.querySelector('meta[name="_csrf_token"]')?.content;
const csrfHeaderName = document.querySelector('meta[name="_csrf_headerName"]')?.content;

const currentUserId = document.getElementById('currentUserId')?.value;
const currentUserName = document.getElementById('currentUserName')?.value;

let picker_list = null;
let picker = null;

document.addEventListener('DOMContentLoaded', function() {
	
	//일정목록 데이트피커 객체 생성
	picker_list = tui.DatePicker.createRangePicker({
	    startpicker: {
	        date: today,
	        input: '#startpicker-input-list',
	        container: '#startpicker-container-list'
	    },
	    endpicker: {
	        date: nextYear,
	        input: '#endpicker-input-list',
	        container: '#endpicker-container-list'
	    },
	    format: 'YYYY-MM-dd'
	});
	
	// 일정등록 데이트피커 객체 생성
	picker = tui.DatePicker.createRangePicker({
	    startpicker: {
	        date: today,
	        input: '#startpicker-input',
	        container: '#startpicker-container'
	    },
	    endpicker: {
	        date: today,
	        input: '#endpicker-input',
	        container: '#endpicker-container'
	    },
	    selectableRanges: [
	        [today, new Date(today.getFullYear() + 1, today.getMonth(), today.getDate())]
	    ],
	    format: 'YYYY-MM-dd HH:mm',
		timepicker: {
            layoutType: 'tab',
            inputType: 'spinbox',
			showMeridiem: false
        }
	});
	
	
	// ---------------------------------------------------------------
	// 초기 TUI 그리드 불러오기
	getScheduleData();
	
	// Search버튼 눌러 그리드 불러오기
	const searchForm = document.getElementById('schedule-search-form');
	searchForm.addEventListener('submit', function(event) {
		event.preventDefault();
		getScheduleData();
	});
	
	// 시작날자, 끝날자 받아서 data불러오기
	function getScheduleData(){
		const startDate = picker_list.getStartDate();
		const endDate = picker_list.getEndDate();
		
		const params = new URLSearchParams({
			startDate: formatLocalDateTime(startDate)
			, endDate: formatLocalDateTime(endDate)
		});
				
		fetch(`/api/schedules?${params.toString()}`, {method: 'GET'})
		.then(response => {
			if (!response.ok) throw new Error(response.text());
			return response.json();  //JSON 파싱
		})
		.then(data => { // response가 ok일때
			initGrid(data);
		}).catch(error => {
			console.error('에러', error)
			alert("데이터 조회 실패");
		});
	}
	
	// ---------------------------------------------------------------
	// 일정등록 버튼이벤트
	document.getElementById('add-schedule').addEventListener('click', () => {
		openModal('add');
	})
	
	
	//일정등록 모달 등록버튼 이벤트
	const addScheduleForm = document.getElementById('add-schedule-form')
	const addScheduleBtn = document.getElementById('add-schedule-btn');
	
	addScheduleForm.addEventListener('submit', function(event) {
		event.preventDefault(); // 기본제출 막기
		
		// 일정 등록 일때 구분
		if(addScheduleBtn.value == 'add') {
			// 등록일때는 scheduleId의 name값 제거 [jpa에 id가 null이어야 자동생성가능]
			addScheduleForm.scheduleId.removeAttribute('name');
			
			fetch('/main/schedule', {
				method: 'POST'
				, headers: {
					[csrfHeaderName]: csrfToken
				}
				, body: new FormData(addScheduleForm)
			})
			.then(response => {
				if (!response.ok) {
					throw new Error(response.msg);
				}
				return response.json();  //JSON 파싱
			})
			.then(response => { // response가 ok일때
				alert(response.msg);
				location.reload();
			}).catch(error => {
				alert("제목, 시작,종료 일시, 내용은 필수입력 사항입니다.");
				location.reload();
			});
		} else if(addScheduleBtn.value == 'edit') {

			if(confirm("수정하시겠습니까?")){
				// 수정일때는 scheduleId 포함해서 보내기
				addScheduleForm.scheduleId.setAttribute('name', 'scheduleId');
				console.log("ddddddd");
				fetch('/main/schedule', {
					method: 'PATCH'
					, headers: {
						[csrfHeaderName]: csrfToken
					}
					, body: new FormData(addScheduleForm)
				})
				.then(response => {
					if (!response.ok) {
						throw new Error(response.msg);
					}
					return response.json();  //JSON 파싱
				})
				.then(response => { // response가 ok일때
					alert(response.msg);
					location.reload();
				}).catch(error => {
					alert("수정에 실패하였습니다.");
					location.reload();
				});
			}
		}
		
	}); // 일정등록함수 끝
	
	
	
	
		
});// DOM로드 끝

let grid = null;
// 그리드 불러오기 함수
function initGrid(data) {
	const Pagination = tui.Pagination;
	if(!grid){
		grid = new tui.Grid({
			el: document.getElementById("grid"),
			editable: true,
			columns: [
				{
					header: '일정제목',
					name: 'scheduleTitle',
					sortable: true,
					width: 150,
					filter: { type: 'text', showApplyBtn: true, showClearBtn: true }
				},
				{
					header: '종류',
					name: 'scheduleType',
					sortable: true,
					width: 70,
					filter: 'select'
				},
				{
					header: '일정시작',
					name: 'scheduleStart',
					sortable: true,
					width:130,
					filter: {
						type: 'date', options: {format: 'yyyy.MM.dd'}
					}
				},
				{
					header: '일정마감',
					name: 'scheduleFinish',
					sortable: true,
					width:130,
					filter: {
						type: 'date', options: {format: 'yyyy.MM.dd'}
					}
				},
				{
					header: '작성자',
					name: 'createdUser',
					sortable: true,
					width: 70,
					filter: {
						type: 'text',
						operator: 'OR'
					}
				},
				{
					header: '내용',
					name: 'scheduleContent',
					sortable: true,
					filter: { type: 'text', showApplyBtn: true, showClearBtn: true }
				},
				{
					header: ' '
					, name: "btn"
					, width: 100 // 너비 설정
					, align: "center"
					// formatter 속성에 화살표 함수를 활용하여 원하는 태그를 해당 셀에 삽입 가능(각 셀에 반복 삽입됨)
	//				, formatter: () => "<button type='button' class='btn-detail' >상세정보</button>"
					, formatter: (cellInfo) => "<button type='button' class='btn-detail' data-row='${cellInfo.rowKey}' >상세정보</button>"
				}
			],
			rowHeaders: ['rowNum'],
			pageOptions: {
				useClient: true,
				perPage: 5
			}
		});
		grid.resetData(data);
		grid.sort('scheduleStart', true); // true: 오름차순 false: 내림차순
	} else {
		grid.resetData(data);
		grid.sort('scheduleStart', true); // true: 오름차순 false: 내림차순
	}
	
	// 상세보기 버튼 이벤트
	grid.on("click", (event) => {
		if(event.columnName == "btn") {
			const rowData = grid.getRow(event.rowKey);
			const scheduleId = rowData.scheduleId;
			
			fetch(`/api/schedules/${scheduleId}`, {method: 'GET'})
			.then(response => {
				if (!response.ok) throw new Error(response.text());
				return response.json();  //JSON 파싱
			})
			.then(data => { // response가 ok일때
				openModal("edit", data);
			}).catch(error => {
				console.error('에러', error)
				alert("데이터 조회 실패");
			});
		}
	});
}


// 모달열기함수
function openModal(mode, data = null) {
	const modal = new bootstrap.Modal(document.getElementById('add-schedule-modal'));
	const form = document.getElementById('add-schedule-form');
	const modalTitle = document.getElementById('modalCenterTitle');
	const deleteBtn = document.getElementById('delete-schedule-btn');
	const submitBtn = document.getElementById('add-schedule-btn');
	const select = document.getElementById('schedule-type');
	const createdUser = document.getElementById('schedule-writer')
	const startpickerInput = document.getElementById('startpicker-input');
	const endpickerInput = document.getElementById('endpicker-input');
	const schedueId = document.getElementById('schedule-id');
	const createdUserName = document.getElementById('createdUserName');
	
	if(mode === 'add') {
		modalTitle.textContent = '일정등록';
		deleteBtn.classList.add('d-none');
	    submitBtn.textContent = '등록';
		submitBtn.value ='add';
		form.reset();
		//셀렉트박스 초기화
		select.innerHTML = '';
		
		const option1 = document.createElement('option');
		option1.value = 'company';
		option1.text = '회사'
		select.appendChild(option1);
		
		const option2 = document.createElement('option');
		option2.value = 'private'
		option2.text = '개인'
		select.appendChild(option2);
		
		select.value = 'company';
		
		// 부서 목록 조회 
		fetch('/api/schedules/departments')
		    .then(response => response.json())
		    .then(data => {
				// 셀릭트박스
		        const select = document.getElementById('schedule-type');
				
				// 셀렉트박스에 부서목록 추가
		        data.forEach(department => {
		            const option = document.createElement('option');
		            option.value = department.deptId
		            option.text = department.deptName; // 옵션명
		            select.appendChild(option);
		        });
		});
		// 날짜 초기값
		picker.setStartDate(today);
		picker.setEndDate(today);
		// 종일 체크 해제
		form.alldayYN.checked = false;

	} else if (mode === 'edit' && data) {
		
		modalTitle.textContent = '일정조회';
		deleteBtn.classList.remove('d-none');
	    submitBtn.textContent = '수정';
		submitBtn.value ='edit';
		form.scheduleId.value = data.scheduleId || '';
		form.scheduleTitle.value = data.scheduleTitle || '';
		form.createdUser.value = data.createdUser || '';
		//셀렉트박스 초기화
		select.innerHTML = '';
		
		const option1 = document.createElement('option');
		option1.value = 'company';
		option1.text = '회사';
		select.appendChild(option1);

		const option2 = document.createElement('option');
		option2.value = currentUserId;
		option2.text = '개인';
		select.appendChild(option2);
		
		// 부서 목록 조회 
		fetch('/api/schedules/departments')
		    .then(response => response.json())
		    .then(deptData => {
				// 셀릭트박스
		        const select = document.getElementById('schedule-type');
				
				// 셀렉트박스에 부서목록 추가
		        deptData.forEach(department => {
		            const option = document.createElement('option');
		            option.value = department.deptId
		            option.text = department.deptName; // 옵션명
		            select.appendChild(option);
		        });
				select.value = data.scheduleType;
			});
		
		
		// 날짜 초기값
		picker.setStartDate(data.scheduleStart ? new Date(data.scheduleStart) : today);
		picker.setEndDate(data.scheduleFinish ? new Date(data.scheduleFinish): today);
		// 종일 체크 해제
		form.alldayYN.checked = data.alldayYN === 'Y';
		form.scheduleContent.value = data.scheduleContent || '';
	}
	
	modal.show();
} // 모달열기 함수 끝





var today = new Date();
var nextYear = new Date(today);

nextYear.setFullYear(nextYear.getFullYear() + 1);

function pad(n) {
    return n < 10 ? '0' + n : n;
}

function formatDateTime(date) {
	var year   = date.getFullYear();
	const month = (date.getMonth() + 1).toString().padStart(2, '0');
  	const day = date.getDate().toString().padStart(2, '0');
	var hour   = pad(date.getHours());
	var minute = pad(date.getMinutes());

	var formatted = year + '-' + month + '-' + day + ' ' + hour + ':' + minute;
	
	return formatted;
}

function formatLocalDateTime(date) {
	var year   = date.getFullYear();
	const month = (date.getMonth() + 1).toString().padStart(2, '0');
  	const day = date.getDate().toString().padStart(2, '0');
	var hour   = pad(date.getHours());
	var minute = pad(date.getMinutes());
	var second = pad(date.getSeconds());

	var formatted = year + '-' + month + '-' + day + 'T' + hour + ':' + minute + ':' + second;
	
	return formatted;
}

function formatDate(date) {
	const year = date.getFullYear();
	const month = (date.getMonth() + 1).toString().padStart(2, '0');
	const day = date.getDate().toString().padStart(2, '0');
	return `${year}-${month}-${day}`;
}






























