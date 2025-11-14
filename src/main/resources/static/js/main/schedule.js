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
function updateCurrentDate() {
	// 현재날짜 표시 할 위치 지정
	const currentDateEl = document.getElementById('calendar-date');
    currentDate = calendar.getDate();
    const viewName = calendar.getViewName();
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth() + 1;
    const day = currentDate.getDate();
    
	if(!calendarYear) { //처음 캘린더 생성
		yearHoliday(year);
	} else if(calendarYear != year) { // 선택된 년도가 바뀔때
		yearHoliday(year);
	}
	
	dateController.setDate(new Date(currentDate));
	currentDateEl.textContent = `${year}년 ${month}월`;
	if(calendarYear != year || calendarMonth != month) {
		loadMonthSchedule();
	}
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
	
	// 만약 이미 달력이 있으면 제거
	if (calendar) {
	    calendar.destroy();
	    calendar = null;
	}
	
	// 캘린더 객체 생성
	calendar = new tui.Calendar(calendarEl, {
	    defaultView: 'month',
	    useCreationPopup: false,
	    useDetailPopup: true,
		isReadOnly: false,
		useDetailPopup: false,
		month: {
	        visibleEventCount: 4  //월타입 달력에 보여줄 스케줄의 최대 개수
	    },
		calendars: [  // 달력 카테고리
		    {
		        id: 'holiday',
		        name: '공휴일',
		        color: '#fff',
		        backgroundColor: '#fdebe8',
		        borderColor: '#e74c3c'
		    },
			{
		      id: 'company',
		      name: '회사',
		      backgroundColor: '#00a9ff',
		      borderColor: '#00a9ff'
		    },
		    {
		      id: 'personal',
		      name: '개인',
		      backgroundColor: '#03bd9e',
		      borderColor: '#03bd9e'
		    },
		    {
		      id: 'department',
		      name: '부서',
		      backgroundColor: '#ff5583',
		      borderColor: '#ff5583'
		    }
		]
	});
	
	// 이전에 선택된 날짜가 있으면 설정
	if(currentDate) calendar.setDate(currentDate);
//	yearHoliday(calendar.getDate().getFullYear());
	
	updateCurrentDate();
}

// 휴일정보 받아온후 캘린더에 반영
function yearHoliday(year){
	fetch(`${uri}=${myApiKey}&solYear=${year}&numOfRows=30&pageNo=1&_type=json`)
		.then(response => {
			if (!response.ok) throw new Error('Network response was not ok.');
			return response.json();
		})
		.then(data => {
			holidayData = data.response.body.items.item;
			const schedule = convertHolidayDataToSchedules(holidayData);
			// 캘린더 템플릿 지정
			calendar.setOptions({
//			    template: {
//			        milestone(event) {
//			            if (event.isHoliday) {
//			                return `<span style="color: red;">${event.title}</span>`;
//			            }
//			            return `<span>${event.title}</span>`;
//			        }
//			    }
			});
			// 스케줄 캘린더에 추가
//			console.log("스케줄데이터", schedule);
			calendar.clear();
            calendar.createEvents(schedule);
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
            id: String(idx + 1),
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
		showAlways: true,
		autoClose: false,
		openers: true,
		calendar: {
			showToday : false,
		}
	});
}
// ----------------------------------------------------

//달력 타입 검사 함수
function checkCalendarType() {
	if(calendarType == 'month') {
		dateController.close();
	} else {
		dateController.open();
	}
}

//해당월의 달력일정 불러오기
function loadMonthSchedule() {
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
	const params = new URLSearchParams({
		startDate: formatLocalDateTime(startDate)
		, endDate: formatLocalDateTime(endDate)
	});
	
	getScheduleData(params);
	
	
}

// 현재 달력이 선택한 월의 일정 정보 불러오기
function getScheduleData(params) {
	fetch(`/api/schedules?${params.toString()}`, {method: 'GET'})
	.then(response => {
		if (!response.ok) throw new Error(response.text());
		return response.json();  //JSON 파싱
	})
	.then(data => { // response가 ok일때
		console.log(data);
		// 조회한 월단위 일정을 캘린더 데이터로 변환
		const monthlySchedule = convertScheduleDataToSchedules(data);
		if(!calendar) initCalendar();
		console.log(calendar);
		console.log(typeof calendar.getEvents);
		//공휴일로 등록된이벤트 제외하고 삭제
		calendar.getEvents().forEach(ev => {
			if(ev.calendarId !== "holiday") {
				calendar.deleteEvent(ev.id, ev.calendarId);
			}
		});
		calendar.createEvents(monthlySchedule);
	}).catch(error => {
		console.error('에러', error)
		alert("데이터 조회 실패");
	});
}

function convertScheduleDataToSchedules(monthScheduleData) {
	return monthScheduleData.map(item => {
		const isAllday = item.alldayYN == "Y";
		return {
			id: String(item.scheduleId),
			calendarId: getCalendarId(item.scheduleType, item.deptId),
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
		console.log(eventInfo);
		if (event.calendarId === 'leave') {
//			openMyCustomModal(event);
			alert("커스텀모달함수등록예정")
		}
	});

	// datepicker날짜 선택시 캐린더 날짜 변경
	dateController.on('change', function() {
		var selectedDate = dateController.getDate();
		var formattedDate = formatDateToYYYYMMDD(selectedDate);
		calendar.setDate(formattedDate);
		console.log("체인지실행")
		updateCurrentDate();
	});
	
});// DOM로드 끝

	
	
	
	
