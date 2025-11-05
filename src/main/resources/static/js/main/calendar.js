const uri = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getHoliDeInfo?Servicekey";
const myApiKey = "3bb524dc5656794ff51462c21245e81ffd44e902f5c3220a4d89b540465280e9";

// 캘린더 표시 할 위치지정
const calendarEl = document.getElementById('calendar');
// 캘린더 객체 생성
const calendar = new tui.Calendar(calendarEl, {
    defaultView: 'month',
    useCreationPopup: true,
    useDetailPopup: true,
	isReadOnly: false,
	month: {
        visibleEventCount: 4  // 이 값을 넉넉히 올려 보세요!
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


let holidayData = null;
let calendarYear = null;
let scheduleData = null;

// 현재날짜 표시 할 위치 지정
const currentDateEl = document.getElementById('current-date');

// 현재 날짜 표시 업데이트 함수
function updateCurrentDate() {
    const currentDate = calendar.getDate();
    const viewName = calendar.getViewName();
    
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth() + 1;
    const day = currentDate.getDate();
    
	if(!calendarYear) { //처음 캘린더 생성
		calendarYear = year;
		InstallHoliday(year);
	} else if(calendarYear != year) { // 선택된 년도가 바뀔때
		calendarYear = year;
		InstallHoliday(year);
	}
	
    if (viewName === 'month') {// 월단위 캘린더 볼때(default)
    	currentDateEl.textContent = `${year}년 ${month}월`;
    } else if (viewName === 'week') { // 주단위 캘린더 볼떄
		
        const startDate = calendar.getDateRangeStart();
        const endDate = calendar.getDateRangeEnd();
        const start = new Date(startDate);
        const end = new Date(endDate);
        
        const startMonth = start.getMonth() + 1;
        const startDay = start.getDate();
        const endMonth = end.getMonth() + 1;
        const endDay = end.getDate();
        
        currentDateEl.textContent = `${year}년 ${startMonth}월 ${startDay}일 - ${endMonth}월 ${endDay}일`;
		
    }
}

// 이전 버튼
document.getElementById('btn-prev').onclick = function() {
	calendar.prev();
	updateCurrentDate();
	
};

// 오늘 버튼
document.getElementById('btn-today').onclick = function() {
    calendar.today();
    updateCurrentDate();
};

// 다음 버튼
document.getElementById('btn-next').onclick = function() {
    calendar.next();
    updateCurrentDate();
};

//	 // 주간 보기 버튼
//	 document.getElementById('btn-week').onclick = function() {
//	     calendar.changeView('week');
//	     updateCurrentDate();
//	 };
//
//	 // 월간 보기 버튼
//	 document.getElementById('btn-month').onclick = function() {
//	     calendar.changeView('month');
//	     updateCurrentDate();
//	 };


// 휴일정보 받아오기
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
// 받아온 휴일정보로 캘린더 템플릿 지정
function InstallHoliday(year){
	yearHoliday(year);
}

// DOMContentLoaded에서 renderCalendar() 함수 호출
document.addEventListener('DOMContentLoaded', function () {
	updateCurrentDate();
//	yearHoliday(2025)

//	renderCalendar();
});