const uri = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getHoliDeInfo?Servicekey";
const myApiKey = "3bb524dc5656794ff51462c21245e81ffd44e902f5c3220a4d89b540465280e9";

let calendar = null; // calendar 객체 선언
let currentDate = null; // 현재날짜 저장
// 캘린더위치지정
const calendarEl = document.getElementById('calendar');

// 캘린더 생성함수
function createCalendar() {
	calendarEl.innerHTML = ""; // 기존 내용 제거
//	if(!calendar) return; // calendar 객체가 없으면 종료
	
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
		    }
		]
	});
	
	// 이전에 선택된 날짜가 있으면 설정
	if(currentDate) calendar.setDate(currentDate);
//	yearHoliday(calendar.getDate().getFullYear());
	
	updateCurrentDate();
}

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
		calendarYear = year;
		yearHoliday(year);
	} else if(calendarYear != year) { // 선택된 년도가 바뀔때
		calendarYear = year;
		yearHoliday(year);
	}
	currentDateEl.textContent = `${year}년 ${month}월`;
}
	
// 버튼 함수 지정
function changeCalendarType(type) {
	calendar.changeView(type, true);
}
// MONTH, WEEK, DAY, LIST 버튼 이벤트리스너
document.getElementById('type-month').addEventListener('click', function() {
	// 만약 이미 달력이 없으면 생성
	if (!calendar) createCalendar();
    changeCalendarType('month');
	updateCurrentDate();	
});

document.getElementById('type-week').addEventListener('click', function() {
	// 만약 이미 달력이 없으면 생성
	if (!calendar) createCalendar();
    changeCalendarType('week');	
	updateCurrentDate();
});

document.getElementById('type-day').addEventListener('click', function() {
	// 만약 이미 달력이 없으면 생성
	if (!calendar) createCalendar();
    changeCalendarType('day');
	updateCurrentDate();
		calendar.createEvents([
          {
			  calendarId: 'leave',         // 사용할 캘린더 id
			  title: '연차',
			  category: 'time',              // 시간 기반 이벤트
			  start: '2025-10-07T09:00:00',  // 시작 날짜/시간 (ISO 또는 Date 객체 가능)
			  end: '2025-10-11T18:00:00',    // 종료 날짜/시간
			  isAllDay: false                // 종일 여부
			  // 필요에 따라 color, backgroundColor, location, etc. 추가 가능
          },
          {
			  calendarId: 'leave',         // 사용할 캘린더 id
			  title: '연차',
			  category: 'time',              // 시간 기반 이벤트
			  start: '2025-10-07T09:00:00',  // 시작 날짜/시간 (ISO 또는 Date 객체 가능)
			  end: '2025-10-07T18:00:00',    // 종료 날짜/시간
			  isAllDay: false                // 종일 여부
			  // 필요에 따라 color, backgroundColor, location, etc. 추가 가능
          }
		]);		
});

document.getElementById('type-list').addEventListener('click', function() {
	// 만약 이미 달력이 있으면 제거
	if (calendar) {
	    calendar.destroy();
	    calendar = null;
	}
	document.getElementById('calendar').innerHTML = `
		<h1> 일정 목록 표시 </h1>
	`;
});

function showCalendarView(type) {
    // 달력이 없으면 생성
    if (!calendar) {
        createCalendar();
    }
	
    calendar.changeView(type, true);
}

// 캘린더버튼 동작 함수 끝
// ---------------------------------
// 커스텀 모달 등록 

function openMyCustomModal(data) {
	console.log(data)
	// 예시: Bootstrap 모달 띄우기
	new bootstrap.Modal(document.getElementById('addScheduleModal')).show();
	openAddScheduleModal();
		
	document.getElementById('startpicker-input').value = formatDateToYYYYMMDD(data.start).toString();
	document.getElementById('endpicker-input').value = formatDateToYYYYMMDD(data.end).toString();
	document.getElementById('allDayCheckbox').checked = data.isAllDay;
}

function openAddScheduleModal() {
	var today = new Date();
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
            inputType: 'spinbox'
        }
	});
}




let holidayData = null;
let calendarYear = null;
let scheduleData = null;


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
			    template: {
			        allday(event) {
			            if (event.isHoliday) {
			                return `<span style="color: red;">${event.title}</span>`;
			            }
			            return `<span>${event.title}</span>`;
			        }
			    }
			});
			// 스케줄 캘린더에 추가
//			console.log("스케줄데이터", schedule);
			calendar.clear()
            calendar.createEvents(schedule);
		})
		.catch(console.error);
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
            category: 'allday',
            isAllDay: true,
            isHoliday: true,
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

document.addEventListener('DOMContentLoaded', function () {
	createCalendar();

	// DatePicker 생성
	const container = document.getElementById('tui-date-picker');
	const input = document.getElementById('datepicker-input');
	
	var dateController = new tui.DatePicker('#tui-datepicker',
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
	
	// ----------------------------------------------------
	// datepicker 클릭 calendar 연동
	dateController.on('change', function() {
		var selectedDate = dateController.getDate();
		var formattedDate = formatDateToYYYYMMDD(selectedDate);
//		console.log("선택날짜", formattedDate);
		calendar.setDate(formattedDate);
		updateCurrentDate();
	});

	
	document.getElementById('add-schedule-btn').addEventListener('click', function() {
//		console.log("모달열기완료");
		openAddScheduleModal()
		
		picker.on('change:end', () => {
		    console.log(123);
		})
	});
	
	// 스케줄 추가시 커스텀 모달 열기
	calendar.on('selectDateTime', (event) => {
		const today = new Date();
		const startDate = new Date(event.start);
		formattedStartDate = formatDateToYYYYMMDD(startDate);
		formattedToday = formatDateToYYYYMMDD(today);
		
		if(formattedStartDate < formattedToday){
			alert("과거 날짜에는 일정을 추가할 수 없습니다.");
		} else {
			// 기존 'scheduleDetailClick' 역할
			openMyCustomModal(event); // event에 정보 있음
		}
		calendar.clearGridSelections();
	});
	// 일정 클릭시 이벤트
	calendar.on('clickEvent', (eventInfo) => {
	  const event = eventInfo.event;

	  if (event.calendarId === 'leave') {
	    // 커스텀 카테고리인 경우 => 커스텀 모달 열기
	    openMyCustomModal(event);
	  } else {
	    // 그 외 일반 카테고리는 기본 모달(Open Detail Popup) 열기
	    calendar.openDetailPopup(event);
	  }
	});

	
	
	
	
});