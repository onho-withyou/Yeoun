/**
	일정게시판 JavaScript 
**/

const csrfToken = document.querySelector('meta[name="_csrf_token"]')?.content;
const csrfHeaderName = document.querySelector('meta[name="_csrf_headerName"]')?.content;

const currentUserId = document.getElementById('currentUserId')?.value;
const currentUserName = document.getElementById('currentUserName')?.value;

let picker_list = null;

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
	
	// ---------------------------------------------------------------
	// 초기 TUI 그리드 불러오기
	getScheduleData();
	
	// Search버튼 눌러 그리드 불러오기
	const searchForm = document.getElementById('schedule-search-form');
	searchForm.addEventListener('submit', function async (event) {
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
//			console.log(data);
			initGrid(data);
		}).catch(error => {
			console.error('에러', error)
			alert("데이터 조회 실패");
		});
	}
	
	// ---------------------------------------------------------------
	// 일정등록 버튼이벤트
	document.getElementById('add-schedule').addEventListener('click', () => {
		openScheduleModal('add');
	})
	
	
	
	
	
	
		
});// DOM로드 끝

let grid = null;
// 그리드 불러오기 함수
function initGrid(data) {
	const Pagination = tui.Pagination;
	
	if(!grid){
		createGrid();
	} else {
		grid.destroy();
		createGrid();
	}
	
	function createGrid() {
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
					name: 'empName',
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
					header: '상세'
					, name: "btn"
					, width: 100 // 너비 설정
					, align: "center"
					// formatter 속성에 화살표 함수를 활용하여 원하는 태그를 해당 셀에 삽입 가능(각 셀에 반복 삽입됨)
	//				, formatter: () => "<button type='button' class='btn-detail' >상세정보</button>"
					, formatter: (cellInfo) => "<button type='button' class='btn-detail btn-primary btn-sm' data-row='${cellInfo.rowKey}' >상세</button>"
				}
			],
			rowHeaders: ['rowNum'],
			pageOptions: {
				useClient: true,
				perPage: 5
			},
			columnOptions: {
			    resizable: true
			}
		});
		grid.resetData(data);
		grid.sort('scheduleStart', true); // true: 오름차순 false: 내림차순
	}
	
	
	// 상세보기 버튼 이벤트
	grid.on("click", (event) => {
		if(event.columnName == "btn") {
			const target = event.nativeEvent.target;
			if (target && target.tagName === "BUTTON") {
				
				const rowData = grid.getRow(event.rowKey);
				const scheduleId = rowData.scheduleId;
				
				fetch(`/api/schedules/${scheduleId}`, {method: 'GET'})
				.then(response => {
					if (!response.ok) throw new Error(response.text());
					return response.json();  //JSON 파싱
				})
				.then(data => { // response가 ok일때
					openScheduleModal("edit", data);
				}).catch(error => {
					console.error('에러', error)
					alert("데이터 조회 실패");
				});
			}
		}
		
	});
}
































