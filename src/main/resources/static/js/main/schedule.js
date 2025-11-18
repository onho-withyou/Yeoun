const uri = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getHoliDeInfo?Servicekey";
const myApiKey = "3bb524dc5656794ff51462c21245e81ffd44e902f5c3220a4d89b540465280e9";

const currentUserId = document.getElementById('currentUserId')?.value;
const currentUserName = document.getElementById('currentUserName')?.value;
const csrfToken = document.querySelector('meta[name="_csrf_token"]')?.content;
const csrfHeaderName = document.querySelector('meta[name="_csrf_headerName"]')?.content;


const container = document.getElementById('tui-date-picker');
const input = document.getElementById('datepicker-input');
let dateController = null;

let calendar = null; // calendar 객체 선언

let holidayData = null; // 휴일데이터 저장
let monthlyScheduleData = null; //달별 스케줄 데이터
let monthlyLeaveData = null;

let calendarYear = null; // 현재 날짜 년 저장
let calendarMonth = null; // 현재 날짜 월 저장
let scheduleData = null; // 일정 데이터 저장
let currentDate = null; // 현재날짜 저장

let calendarType = 'month'; //현재 달력 타입 지정

const prevMonthBtn = document.getElementById("prevMonth");
const nextMonthBtn = document.getElementById("nextMonth");

// 캘린더위치지정
const calendarEl = document.getElementById('calendar');

// 달력 월 변경 버튼 함수
prevMonthBtn.addEventListener('click', function() {
	calendar.prev();
	updateCurrentDate();
});

nextMonthBtn.addEventListener('click', function() {
	calendar.next();
	updateCurrentDate();
});

//일정등록버튼 함수
document.getElementById('open-add-schedule-modal-btn').addEventListener('click', function() {
	openModal('add');
});

// 현재 날짜 표시 업데이트 함수
async function updateCurrentDate() {
	// 현재날짜 표시 할 위치 지정
	const currentDateEl = document.getElementById('calendar-date');
    // 현재날짜 저장
	currentDate = calendar.getDate();
	
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth() + 1;

	// 스케줄러 위 중앙에 현재 날짜 년월 표시
	currentDateEl.textContent = `${year}년 ${month}월`;
//    const day = currentDate.getDate();
    
	//현재 해의 공휴일정보 받아오기
	if(!calendarYear) { //처음 캘린더 생성
		await yearHoliday(year);
	} else if(calendarYear != year) { // 선택된 년도가 바뀔때
		await yearHoliday(year);
	}
	
	// 기존의 년월과 현재 업데이트하는 년월이 다를경우
	// 그달의 스케줄 정보 불러오기
	if(calendarYear != year || calendarMonth != month) { 
		await loadMonthSchedule();
	}
	
	// 바뀐 년월 정보 저장
	calendarYear = year;
	calendarMonth = month;
}
	
// 버튼 함수 지정
function changeCalendarType(type) {
	calendar.changeView(type, true);
	calendarType = type;
}

// MONTH, WEEK, DAY, LIST 버튼 이벤트리스너
document.getElementById('type-month').addEventListener('click', function() {
	// 만약 이미 달력이 없으면 생성
	if (!calendar) initCalendar();
    changeCalendarType('month');
	updateCurrentDate();
	checkCalendarType();
});

document.getElementById('type-week').addEventListener('click', function() {
	// 만약 이미 달력이 없으면 생성
	if (!calendar) initCalendar();
    changeCalendarType('week');	
	updateCurrentDate();
	checkCalendarType();
});

document.getElementById('type-day').addEventListener('click', function() {
	// 만약 이미 달력이 없으면 생성
	if (!calendar) initCalendar();
    changeCalendarType('day');
	updateCurrentDate();
	checkCalendarType();
});

// list 버튼 클릭시 페이지 이동
document.getElementById('type-list').addEventListener('click', function() {
	location.href = "/main/schedule"
});

// 타입에 맞춰 달력 보이기
function showCalendarView(type) {
    // 달력이 없으면 생성
    if (!calendar) {
        initCalendar();
    }
	
    calendar.changeView(type, true);
}

// 캘린더버튼 동작 함수 끝


// ---------------------------------
// 캘린더 생성함수
function initCalendar() {
	calendarEl.innerHTML = ""; // 기존 내용 제거
	console.log('window.Calendar:', window.Calendar);
	console.log('window.tui:', window.tui);
	// 만약 이미 달력이 있으면 제거
	if (calendar) {
	    calendar.destroy();
	    calendar = null;
	}
	
	// 캘린더 객체 생성
	calendar = new tui.Calendar(calendarEl, {
	    defaultView: 'month',
		template: {
		    milestone(schedule) {
		        if (schedule.calendarId == 'leave' && schedule.raw && Array.isArray(schedule.raw.leaves)) {
					const namesArr = schedule.raw.leaves.map(leave => leave.emp_name);
					const firstName = namesArr[0] || '';
					const leaveCount = schedule.raw.leaves.length;
					
					if(schedule.raw.leaves.length > 1) {
			            return `<span style="font-size:10px; color:#1e7e34;">
			                👤${firstName} 외 ${leaveCount - 1}명
			            </span>`;
					} else if(schedule.raw.leaves.length = 1) {
						return `<span style="font-size:13px; color:#1e7e34;">
			                👤${firstName}
			            </span>`;
					}
		        }
				return schedule.title;
		    }
		},
	    useCreationPopup: false,
	    useDetailPopup: true,
		isReadOnly: false,
		useDetailPopup: false,
		month: {
	        visibleEventCount: 4  //월타입 달력에 보여줄 스케줄의 최대 개수
	    },
		week: {
		  // 시간 09:00~18:00만
		  hourStart: 9,
		  hourEnd: 18, 
		  taskView: ['milestone'],  
		  scheduleView: ['allday', 'time']
		},
		day: {
		  hourStart: 9,
		  hourEnd: 18,
		  taskView: ['milestone'],
		  scheduleView: ['allday', 'time']
		},
		calendars: [
		    {
		        id: 'holiday',
		        name: '공휴일',
		        color: '#fff',
		        backgroundColor: '#fdebe8',
		        borderColor: '#e74c3c'
		    },
			{
                id: 'personal',
                name: '개인',
                color: '#000',
                backgroundColor: '#ffbb3b',
                dragBackgroundColor: 'rgba(255,187,59,0.6)',
                borderColor: '#111111',
                isDraggable: false,
                isResizable: false
            },
            {
                id: 'department',
                name: '부서',
                color: '#fff',
                backgroundColor: '#00a9ff',
                dragBackgroundColor: 'rgba(0,169,255,0.6)',
                borderColor: '#111111',
                isDraggable: false,
                isResizable: false
            },
            {
                id: 'company',
                name: '회사',
                color: '#fff',
                backgroundColor: '#ff5583',
                dragBackgroundColor: 'rgba(255,85,131,0.6)',
                borderColor: '#111111',
                isDraggable: false,
                isResizable: false
            },
			{
			    id: 'leave',
			    name: '연차',
			    color: '#333',
			    backgroundColor: '#b7f3c4',           // 초록 계열 예시
			    dragBackgroundColor: 'rgba(40,167,69,0.6)',
			    borderColor: '#1e7e34',
			    isDraggable: false,
			    isResizable: false
			},
			{
			    id: 'leave',
			    name: '연차',
			    color: '#333',
			    backgroundColor: '#b7f3c4',
			    borderColor: '#1e7e34',
			    isDraggable: false,
			    isResizable: false
			}
		]
	});
	// 이전에 선택된 날짜가 있으면 설정
	if(currentDate) calendar.setDate(currentDate);
//	yearHoliday(calendar.getDate().getFullYear());
	
	updateCurrentDate();
}

// 휴일정보 받아온후 캘린더에 반영
async function yearHoliday(year){
	await fetch(`${uri}=${myApiKey}&solYear=${year}&numOfRows=30&pageNo=1&_type=json`)
		.then(response => {
			if (!response.ok) throw new Error('Network response was not ok.');
			return response.json();
		})
		.then(data => {
			const beforeConvert = data.response.body.items.item;
			holidayData = convertHolidayDataToSchedules(beforeConvert);
			// 스케줄 캘린더에 추가
//			console.log("스케줄데이터", holidayData);
//			calendar.clear();
//            calendar.createEvents(holidayData);
		})
		.catch(console.error);
}

// 커스텀모달 등록
function openAddScheduleModal(data) {
	//모달열기
	openModal('add');
	// data로받아서 등록모달 날짜 지정하기
	var start = new Date(data.start);
	var end = new Date(start)
	end.setDate(start.getDate() + 1);
	picker.setStartDate(start ? new Date(start) : today);
	picker.setEndDate(end ? new Date(end): nextDay);
}

// ----------------------------------------- 
// 공휴일 데이터 스케줄템플릿 형식에 맞게 변환

// start, end 날짜형식맞추기
function formatDate(locdate) {
    const str = locdate.toString();
    return `${str.slice(0,4)}-${str.slice(4,6)}-${str.slice(6,8)}`;
}

// HolidayData > calendar.setOptions({ template: {}})
function convertHolidayDataToSchedules(holidayData) {
    return holidayData
        .filter(item => item.isHoliday === 'Y')
        .map((item, idx) => ({
            id: String("holiday" + idx + 1 ),
            calendarId: 'holiday',
            title: item.dateName,
            category: 'milestone',
            isAllDay: true,
//            isHoliday: true,
			isReadOnly: true,
            start: formatDate(item.locdate), //'2025-01-28' 형식 
            end: formatDate(item.locdate), //'2025-01-28' 형식
			color: "#e74c3c",         // 텍스트 색 (빨강 예시)
		    backgroundColor: "#fdebe8", // 배경색 (연한 빨강)
			borderColor: "#e74c3c"    // 테두리색 (빨강)
        }));
}

// ----------------------------------------------------
// getDate의 값을 YYYY-MM-DD 형식으로 변형
function formatDateToYYYYMMDD(date) {
	var year = date.getFullYear();
	var month = String(date.getMonth() + 1).padStart(2, '0');
	var day = String(date.getDate()).padStart(2, '0');
	return year + '-' + month + '-' + day;
}

// ----------------------------------------------------
function createDatePicker() {
	// DatePicker 생성
	dateController = new tui.DatePicker('#tui-datepicker',
	    {
	    language: 'ko',
	    date: new Date(),
		showAlways: false,
		autoClose: true,
		openers: true,
		calendar: {
			showToday : true,
		},
		showToday: true,
		showJumpButtons: true,
		type: 'date'
//		input: {
//			element: '#tui-datepicker-input',
//			format: 'yyyy-MM-dd'
//		}
	});
}


// ----------------------------------------------------

//달력 타입 검사 함수
function checkCalendarType() {
	if(calendarType == 'month') {
//		dateController.close();
		console.log("먼쓰");
	} else {
//		dateController.open();
		console.log("먼쓰아님");
	}
}

function checkFilter() {
	const companyFilter = document.getElementById('filter-company')
	const departmentFilter = document.getElementById('filter-department')
	const personalFilter = document.getElementById('filter-personal')
	
	companyFilter.checked ? calendar.setCalendarVisibility('company', true) : calendar.setCalendarVisibility('company', false);
	departmentFilter.checked ? calendar.setCalendarVisibility('department', true) : calendar.setCalendarVisibility('department', false);
	personalFilter.checked ? calendar.setCalendarVisibility('personal', true) : calendar.setCalendarVisibility('personal', false);
}

//해당월의 달력일정 불러오기
async function loadMonthSchedule() {
	// 현재 바뀐 날짜 정보에서 그해의 월초, 월말 정보 저장
	const loadDate = calendar.getDate();
	const startDate = new Date(
		loadDate.getFullYear(),
		loadDate.getMonth(),
		1,
		0, 0, 0, 0 
	); 
	const endDate = new Date(
		loadDate.getFullYear(),
		loadDate.getMonth() + 1,
		0,
		23, 59, 59, 999
	);
	// 그해의 월초, 월말 정보 params에 저장
	const params = new URLSearchParams({
		startDate: formatLocalDateTime(startDate)
		, endDate: formatLocalDateTime(endDate)
	});
	
	// 해당 월초~월말 정보를 가지고 스케줄데이터 가져오기
	await getScheduleData(params); // 그달의 스케줄 가져오기
	await getLeaveData(params); // 그달의 연차 데이터 가져오기
	
	// 스케줄러 초기화
	await calendar.clear();
	// 저장된 그해의 휴일데이터 입력
	await calendar.createEvents(holidayData);
	// 저장된 그해의 휴일데이터 입력
	await calendar.createEvents(monthlyLeaveData);
	// 저장된 그달의 일정데이터 입력
	await calendar.createEvents(monthlyScheduleData);
	checkFilter();
	
}

// 현재 달력이 선택한 월의 일정 정보 불러오기
async function getScheduleData(params) {
	await fetch(`/api/schedules?${params.toString()}`, {method: 'GET'})
	.then(response => {
		if (!response.ok) throw new Error(response.text());
		return response.json();  //JSON 파싱
	})
	.then(data => { // response가 ok일때
//		console.log(data);
		// 조회한 월단위 일정을 캘린더 데이터로 변환
		monthlyScheduleData = convertScheduleDataToSchedules(data);
	}).catch(error => {
		console.error('에러', error)
		alert("일정 데이터 조회 실패");
	});
}

function convertScheduleDataToSchedules(monthScheduleData) {
	return monthScheduleData.map(item => {
		const isAllday = item.alldayYN == "Y";
		return {
			id: String(item.scheduleId),
			calendarId: getCalendarId(item.scheduleType),
			title: item.scheduleTitle,
			body: item.scheduleContent || "",
			start: item.scheduleStart.replace(" ", "T"),
			end: item.scheduleFinish.replace(" ", "T"),
			category: isAllday ? "allday" : "time",
			isAllday
//			raw: { ...item } // 기타등등 넣을정보
		};
	});
}

function getCalendarId(type, deptId) {
	if(type === "회사") return "company";
	if(type === "개인") return "personal";
	return "department";
}

// 현재 달력이 선택한 월의 연차 정보 불러오기
async function getLeaveData(params) {
	await fetch(`/api/schedules/leaves?${params.toString()}`, {method: 'GET'})
	.then(response => {
		if (!response.ok) throw new Error(response.text());
		return response.json();  //JSON 파싱
	})
	.then(data => { // response가 ok일때
		// 연차데이터 날짜별로 그룹화
		const dateLeaveMap = groupLeavesByDate(data);
		// 스케줄에 넣을 데이터로 변환
		monthlyLeaveData = convertGroupedLeavesToSchedules(dateLeaveMap);

		// 조회한 월단위 일정을 캘린더 데이터로 변환
//		monthlyScheduleData = convertScheduleDataToSchedules(data);
	}).catch(error => {
		console.error('에러', error)
		alert("연차 데이터 조회 실패");
	});
}

//연차정보를 날짜별로 그룹핑
function groupLeavesByDate(leaves) {
    const result = {};

    leaves.forEach(item => {
        const start = new Date(item.startDate);
        const end = new Date(item.endDate);

        let current = new Date(start);
        while (current <= end) {
            // YYYY-MM-DD 형태로 포맷
            const yyyyMMdd = current.toISOString().slice(0, 10);

            if (!result[yyyyMMdd]) {
                result[yyyyMMdd] = [];
            }
            result[yyyyMMdd].push(item);

            // 다음 날로 증가
            current.setDate(current.getDate() + 1);
        }
    });

    return result;
}

// 날짜별 그룹화 연차 정보를 스케줄러 주입하게 변환
function convertGroupedLeavesToSchedules(dateLeaveMap) {
    return Object.entries(dateLeaveMap).map(([date, leaves]) => {
        // 당일 연차자 이름만 모아서 표시
        const names = leaves.map(leave => leave.emp_name);
        const title = `연차: ${names.join(', ')} (${leaves.length}명)`;
        return {
            id: `leave-summary-${date}`,
            calendarId: 'leave',
            title: title,
            category: 'milestone',
            isAllDay: true,
            isReadOnly: true,
            start: date,
            end: date,
            color: '#333',
            backgroundColor: '#b7f3c4',
            borderColor: '#1e7e34',
            raw: {leaves}
        };
    });
}


//===============================================================
// DOM LOAD
document.addEventListener('DOMContentLoaded', function () {
	
	createDatePicker(); // 데이트피커 생성
	initCalendar(); //달력 생성
	// 해당월의 달력 일정 불러오기
//	loadMonthSchedule();
	// 달력 타입을 검사하여 데이트피커 보이고 안보이고 판단
	checkCalendarType();
	
	// 스케줄 추가시 커스텀 모달 열기
	calendar.on('selectDateTime', (event) => {
		const today = new Date();
		const startDate = new Date(event.start);
		formattedStartDate = formatDateToYYYYMMDD(startDate);
		formattedToday = formatDateToYYYYMMDD(today);
		
		//일정등록모달 열기
		openAddScheduleModal(event);
		// 달력 선택 색 초기화
		calendar.clearGridSelections();
	});

	// 일정 클릭시 이벤트
	calendar.on('clickEvent', (eventInfo) => {
		const event = eventInfo.event;
		console.log(event);
		if (event.calendarId === 'holiday') {
			alert("휴일입니다.");
		} else if (event.calendarId === 'leave') {
//			openMyCustomModal(event);
			alert("커스텀모달함수등록예정")
		} else {
			const scheduleId = event.id;
			
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

	// datepicker날짜 선택시 캐린더 날짜 변경
	dateController.on('change', function() {
		var selectedDate = dateController.getDate();
		var formattedDate = formatDateToYYYYMMDD(selectedDate);
		calendar.setDate(formattedDate);
		updateCurrentDate();
	});
	
	// 캘린더 날짜 클릭하여 데이트피커열기
	const calendarDateEl = document.getElementById('calendar-date');
	
	calendarDateEl.addEventListener('click', function() {
		dateController.open();
	});
	
	getLastNoticeList();
	
	document.querySelectorAll('input.calendar-filter').forEach((checkbox) => {
		checkbox.addEventListener('change', (event) => {
			checkFilter();
		});
	});
	

});// DOM로드 끝

async function getLastNoticeList() {
	await fetch(`/api/notices/last-notice`, {method: 'GET'})
	.then(response => {
		if (!response.ok) throw new Error(response.text());
		return response.json();  //JSON 파싱
	}).then(data => {
		console.log(data);
		initNoticeGrid(data);
	}).catch(error => {
		console.error('에러', error)
		alert("공지 데이터 조회 실패");
	});
}

let grid = null;

// 그리드 불러오기 함수
function initNoticeGrid(data) {
	const Pagination = tui.Pagination;
	
	if(!grid){
		createGrid();
	} else {
		grid.destroy();
		createGrid();
	}
	
	function createGrid() {
		grid = new tui.Grid({
			el: document.getElementById("noticeGrid"),
			editable: true,
			columns: [
				{
					header: '제목',
					name: 'noticeTitle'
				},
//				{
//					header: '등록일',
//					name: 'createdDate'
//				},
//				{
//					header: ' '
//					, name: "btn"
//					, width: 100 // 너비 설정
//					, align: "center"
//					, formatter: (cellInfo) => "<button type='button' class='btn-detail' data-row='${cellInfo.rowKey}' >상세정보</button>"
//				}
			],
			rowHeaders: ['rowNum'],
		});
		grid.resetData(data);
	}
	
	
	// 상세보기 버튼 이벤트
	grid.on("click", (event) => {
//		console.log(event);
		const rowData = grid.getRow(event.rowKey);
		const noticeId = rowData.noticeId;
		
		selectedNoticeId = noticeId;
		const modalEl = document.getElementById('show-notice');
		new bootstrap.Modal(modalEl).show();
			

	});
}
	
	
	
