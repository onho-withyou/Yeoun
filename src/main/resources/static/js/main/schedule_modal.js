/**
	일정 모달 JavaScript 
**/

let picker = null;
let startTimePicker = null;
let endTimePicker = null;

// 일정등록 데이트피커 객체 생성
function createRangePicker() {
	picker = tui.DatePicker.createRangePicker({
	    startpicker: {
	        date: today,
	        input: '#startpicker-input',
	        container: '#startpicker-container'
	    },
	    endpicker: {
	        date: nextDay,
	        input: '#endpicker-input',
	        container: '#endpicker-container'
	    },
	    format: 'YYYY-MM-dd HH:mm',
		timepicker: {
            layoutType: 'tab',
            inputType: 'spinbox',
			showMeridiem: false
        }
	});
	
	picker.on('change:start', () => {
	  validateRangeWithAllday();
	});

	// end 날짜 변경 시 검증
	picker.on('change:end', () => {
	  validateRangeWithAllday();
	});
}

// 종일 일정이 아닌경우 start, end 같은 날로
function syncEndDateToStartDate() {
  const startDate = picker.getStartDate();   // Date 객체 또는 null
  const endDate   = picker.getEndDate();     // Date 객체 또는 null

  // start 가 아직 없으면 아무것도 못 하니까 그냥 종료
  if (!(startDate instanceof Date)) {
    return;
  }

  let newEnd;

  if (endDate instanceof Date) {
    // 기존 end 의 시간은 유지하고 날짜(년월일)만 start 기준으로 맞추기
    newEnd = new Date(endDate);
    newEnd.setFullYear(
      startDate.getFullYear(),
      startDate.getMonth(),
      startDate.getDate()
    );
  } else {
    // end 가 null 인 경우: start 를 그대로 복사해서 end 로 사용
    newEnd = new Date(startDate);
  }

  picker.setEndDate(newEnd);
}

// 두 Date가 같은 '날짜(년/월/일)'인지 확인
function isSameDay(d1, d2) {
  // 둘 중 하나라도 값이 없으면 비교 불가 → false 리턴
  if (!(d1 instanceof Date) || !(d2 instanceof Date)) {
    return false;
  }

  return d1.getFullYear() === d2.getFullYear() &&
         d1.getMonth()    === d2.getMonth() &&
         d1.getDate()     === d2.getDate();
}

// allday 상태를 보고 start/end 검증하는 함수
function validateRangeWithAllday() {
	const alldayCheck = document.getElementById('all-day-checkbox');
	const isAllDay = alldayCheck.checked;
	const startDate = picker.getStartDate();
	const endDate   = picker.getEndDate();

	if (!isAllDay) { // 종일 아닐 때만 체크

	    if (!isSameDay(startDate, endDate)) {
			// end 를 start 날짜로 맞추기 (시간까지 통일하려면 new Date(startDate) 써도 됨)
			const fixedEnd = new Date(endDate);
			fixedEnd.setFullYear(
				startDate.getFullYear(),
			 	startDate.getMonth(),
				startDate.getDate()
			);
		
			picker.setEndDate(fixedEnd);
		
			alert('종일 일정이 아닐 때는 시작일과 종료일이 같은 날이어야 합니다.\n종료일을 시작일로 변경했습니다.');
		}
	} else {
		
	}
}

document.addEventListener('DOMContentLoaded', function() {
	
	createRangePicker();
	
	const alldayCheck = document.getElementById('all-day-checkbox');

	// 종일 체크박스 변경 이벤트
	alldayCheck.addEventListener('change', function() {
		const alldayYN = document.getElementById('all-day-checkbox-value');
		alldayCheck.checked ? alldayYN.value = "Y" : alldayYN.value = "N";
		
		if (!alldayCheck.checked) {
			syncEndDateToStartDate();
		}
	});
	
	//일정등록 모달 등록, 수정버튼 이벤트
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
			});
			
		} else if(addScheduleBtn.value == 'edit') {
			if(confirm("수정하시겠습니까?")){
				// 수정일때는 scheduleId 포함해서 보내기
				addScheduleForm.scheduleId.setAttribute('name', 'scheduleId');
				
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
				});
			}
		}
	}); // 일정등록, 수정함수 끝
	
	// 일정조회 - 삭제버튼 이벤트
	document.getElementById('delete-schedule-btn').addEventListener('click', function () {
		if(confirm("삭제하시겠습니까 ?")) {
			//삭제요청보내기
			fetch('/main/schedule', {
				method: 'DELETE'
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
				alert("삭제에 실패하였습니다.");
			});
		}
	}); //일정조회 - 삭제 버튼 이벤트 끝
		
});// DOM로드 끝

// ----------------------------------------------------------
// 부서 목록 조회 함수
async function createSelect() {
	// 셀렉트박스 지정
    const select = document.getElementById('schedule-type');

	await fetch('/api/schedules/departments')
    .then(response => response.json())
    .then(data => {
		// 셀렉트박스에 부서목록 추가
        data.forEach(department => {
            const option = document.createElement('option');
            option.value = department.deptId
            option.text = department.deptName; // 옵션명
            select.appendChild(option);
        });
	});
}

// 모달열기함수
async function openScheduleModal(mode, data = null) {
	const modal = new bootstrap.Modal(document.getElementById('add-schedule-modal'));
	const form = document.getElementById('add-schedule-form');
	const modalTitle = document.getElementById('modalCenterTitle');
	const deleteBtn = document.getElementById('delete-schedule-btn');
	const submitBtn = document.getElementById('add-schedule-btn');
	const select = document.getElementById('schedule-type');
	const createdUserName = document.getElementById('createdUserName');
	const alldayCheckbox = document.getElementById('all-day-checkbox');
	
//	const createdUser = document.getElementById('schedule-writer')
//	const startpickerInput = document.getElementById('startpicker-input');
//	const endpickerInput = document.getElementById('endpicker-input');
//	const schedueId = document.getElementById('schedule-id');
	
	const sp = picker.getStartpicker(); 
	const ep = picker.getEndpicker();
	
	if(mode === 'add') {
		deleteBtn.disabled = false;
		submitBtn.disabled = false;
		//폼요소 입력가능하게 변경
		Array.from(form.elements).forEach(el => {
		    if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
		        el.readOnly = false;
		    } 
			if (el.tagName === 'SELECT' || el.type === 'checkbox') {
		        el.disabled = false;
		    }
			createdUserName.readOnly = true;
		});
		
		modalTitle.textContent = '일정등록';
		deleteBtn.classList.add('d-none');
	    submitBtn.textContent = '등록';
		submitBtn.value ='add';
		
//		form.reset(); // 폼 입력값 초기화
		form.scheduleId.value = '';
		form.scheduleTitle.value = '';
		createdUserName.value = currentUserName || '';
		form.createdUser.value = currentUserId || '';
		form.scheduleContent.value = '';
		
		sp.enable && sp.enable(); //시작날자 선택가능
		ep.enable && ep.enable(); //종료날자 선택가능
		
		// 날짜 초기값
		picker.setStartDate(new Date(today));
		picker.setEndDate(new Date(nextDay));
		
		//셀렉트박스 초기화
		select.innerHTML = '';
		// 셀렉트박스 옵션 디폴트 지정 
		const option1 = document.createElement('option');
		option1.value = 'company';
		option1.text = '회사'
		select.appendChild(option1);

		const option2 = document.createElement('option');
		option2.value = currentUserId;
		option2.text = '개인'
		select.appendChild(option2);
		
		// 셀렉트박스 목록추가
		await createSelect();
		// 등록모달은 company로 default
		select.value = 'company';

		// 종일 체크 해제
		form.alldayYN.checked = false;

	} else if (mode === 'edit' && data) {
		modalTitle.textContent = '일정조회';
		deleteBtn.classList.remove('d-none');
	    submitBtn.textContent = '수정';
		submitBtn.value ='edit';
		form.scheduleId.value = data.scheduleId || '';
		form.scheduleTitle.value = data.scheduleTitle || '';
		createdUserName.value = data.empName || '';
		form.createdUser.value = data.createdUser || '';
		//셀렉트박스 초기화
		select.innerHTML = '';
		
		// 셀렉트박스 옵션 디폴트 지정 
		const option1 = document.createElement('option');
		option1.value = 'company';
		option1.text = '회사'
		select.appendChild(option1);

		const option2 = document.createElement('option');
		option2.value = data.createdUser;
		option2.text = '개인'
		select.appendChild(option2);
		
		// 셀렉트 목록 추가
		await createSelect();
		// 조회한 일정의 일정타입으로 지정
//		console.log(data.scheduleType);
		select.value = data.scheduleType;
		
		// 날짜 초기값
		picker.setStartDate(data.scheduleStart ? new Date(data.scheduleStart) : today);
		picker.setEndDate(data.scheduleFinish ? new Date(data.scheduleFinish): nextDay);
		// 종일 체크
		alldayCheckbox.checked = data.alldayYN === 'Y'; 
		form.alldayYN.checked = data.alldayYN === 'Y'; // hidden value
		form.scheduleContent.value = data.scheduleContent || '';
		
		console.log(alldayCheckbox.checked, "체크드상태");
		console.log(form.scheduleContent.value, "체크드값");
//		console.log(form.alldayYN.checked, "체크드상태");
		
		if (data.createdUser !== currentUserId) {
		    // 권한 없음: 삭제, 수정 버튼 비활성화
		    deleteBtn.disabled = true;
		    submitBtn.disabled = true;
			// 데이트피커 비활성화
			sp.enable && sp.disable();
			ep.enable && ep.disable();

			
		    // 폼 전체의 인풋/셀렉트/체크박스 등을 읽기 전용으로 만들기
		    Array.from(form.elements).forEach(el => {
		        if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
		            el.readOnly = true;
		        }
				if (el.tagName === 'SELECT' || el.type === 'checkbox') {
		            el.disabled = true;
		        }
		    });
		} else {
		    // 권한 있는 사용자에게는 모든 기능 활성화
		    deleteBtn.disabled = false;
		    submitBtn.disabled = false;
			
			//데이트피커 비활성화
			sp.enable && sp.enable();
			ep.enable && ep.enable();
			
		    Array.from(form.elements).forEach(el => {
		        if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
		            el.readOnly = false;
		        } 
				if (el.tagName === 'SELECT' || el.type === 'checkbox') {
		            el.disabled = false;
		        }
				createdUserName.readOnly = true;
		    });
		}
	}
	
	modal.show();
} // 모달열기 함수 끝

var today = new Date();
var nextDay = new Date();
var nextYear = new Date(today);

nextYear.setFullYear(nextYear.getFullYear() + 1);
nextDay.setDate(nextDay.getDate() + 1);

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