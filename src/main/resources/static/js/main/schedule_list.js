/**
	일정게시판 JavaScript 
**/
document.addEventListener('DOMContentLoaded', function() {
	
	//일정목록 데이트피커 객체 생성
	var picker_list = tui.DatePicker.createRangePicker({
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
	var picker = tui.DatePicker.createRangePicker({
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
	// TUI 그리드 불러오기
	const startDateEl = document.getElementById('startpicker-input-list');
	const endDateEl = document.getElementById('endpicker-input-list');
	
	getScheduleData();
	
	// 시작날자, 끝날자 받아서 data불러오기
	function getScheduleData(){
		const startDate = picker_list.getStartDate();
		const endDate = picker_list.getEndDate();
		
		const params = new URLSearchParams({
			startDate: formatDate(startDate)
			, endDate: formatDate(endDate)
		});
				
		fetch(`/api/schedules?${params.toString()}`, {method: 'GET'})
		.then(response => {
			if (!response.ok) throw new Error(response.text());
			return response.json();  //JSON 파싱
		})
		.then(data => { // response가 ok일때
			console.log("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			console.log(data);
			console.log("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			initGrid(data);
	//		location.reload();
		}).catch(error => {
			console.error('에러', error)
			alert("제목, 시작,종료 일시, 내용은 필수입력 사항입니다.");
		});
	}
	
	// ---------------------------------------------------------------
	// 일정등록 모달 지정	
	const addScheduleModal = document.getElementById('add-schedule-modal')
	// 일정등록 모달 열기 이벤트
	addScheduleModal.addEventListener('show.bs.modal', function(event){
		// 폼 초기화
		const addScheduleForm = document.getElementById('add-schedule-form');
		addScheduleForm.reset();
		
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
		
		const startpickerInput = document.getElementById('startpicker-input');
		const endpickerInput = document.getElementById('endpicker-input');
		
		startpickerInput.value = formatDate(today);
		endpickerInput.value = formatDate(today);
		
	}); // 일정등록 모달 열기이벤트 끝
	
	//일정등록 모달 등록버튼 이벤트
	const addScheduleForm = document.getElementById('add-schedule-form')
	const addScheduleBtn = document.getElementById('add-schedule-btn');
	addScheduleForm.addEventListener('submit', function(event) {
		event.preventDefault(); // 기본제출 막기
		
		fetch('/main/schedule', {
			method: 'POST'
			, body: new FormData(addScheduleForm)
		})
		.then(response => {
//			if (!response.ok) throw new Error('등록에 실패했습니다.');
			if (!response.ok) throw new Error(response.msg);
			return response.json();  //JSON 파싱
		})
		.then(response => { // response가 ok일때
			alert(response.msg);
			location.reload();
		}).catch(error => {
			console.error('에러', error)
			alert("제목, 시작,종료 일시, 내용은 필수입력 사항입니다.");
		});
	});
	
	
	
	
	
	
	
	
	
	
		
});// DOM로드 끝

// 그리드 불러오기 함수
function initGrid(data) {
	
	const grid = new tui.Grid({
		el: document.getElementById("grid"),
		editable: true,
		columns: [
			{
				header: '일정시작',
				name: 'scheduleStart'
			},
			{
				header: '일정제목',
				name: 'scheduleTitle'
			},
			{
				header: '종류',
				name: 'scheduleType'
			},
			{
				header: '작성자',
				name: 'createdUser'
			},
			{
				header: '작성일',
				name: 'createdDate'
			},
		]
	
	});
	
	grid.resetData(data);
}














var today = new Date();
var nextYear = new Date(today);

nextYear.setFullYear(nextYear.getFullYear() + 1);

function pad(n) {
    return n < 10 ? '0' + n : n;
}

function formatDateTime(date) {
	var year   = today.getFullYear();
	const month = (date.getMonth() + 1).toString().padStart(2, '0');
  	const day = date.getDate().toString().padStart(2, '0');
	var hour   = pad(today.getHours());
	var minute = pad(today.getMinutes());

	var formatted = year + '-' + month + '-' + day + ' ' + hour + ':' + minute;
	
	return formatted;
}

function formatDate(date) {

		const year = date.getFullYear();
	const month = (date.getMonth() + 1).toString().padStart(2, '0');
	const day = date.getDate().toString().padStart(2, '0');
	return `${year}-${month}-${day}`;
}






























