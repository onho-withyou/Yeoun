/**
	공지사항 JavaScript 
**/
document.addEventListener('DOMContentLoaded', function() {
	let selectedNoticeId = null;
	// 공지사항 조회 모달설정
	const showNoticeModal = document.getElementById('show-notice');
	const showNoticeForm = document.getElementById("notice-form-read");
	const deleteNoticeBtn = document.getElementById('notice-delete');
	const fixedCheck = document.getElementById('fixed-check') // 체크박스
	// 공지사항 조회모달 열기 이벤트
	showNoticeModal.addEventListener('show.bs.modal', function(event){
		const button = event.relatedTarget;
		const noticeId = button.getAttribute('data-notice-id');
		selectedNoticeId = noticeId;
		
		fetch('/api/notices/' + noticeId)
			.then(response => { // response가 200이 아닐때
				if (!response.ok) throw new Error('공지사항을 불러올 수 없습니다.');
		    	
				return response.json();
			})
			.then(data => {
				// 날짜 문자열을 Date 객체로 변환
				const createdDate = new Date(data.createdDate);
				const updatedDate = new Date(data.updatedDate);
				
				document.getElementById('notice-id-read').value = data.noticeId;
				document.getElementById('notice-title-read').value = data.noticeTitle;
				if(data.noticeYN === 'Y') {
					fixedCheck.checked = true;
					fixedCheck.value = "Y";
				} else {
					fixedCheck.checked = false;
					fixedCheck.value = "N";
				}
				document.getElementById('notice-writer-read').textContent = data.createdUser;
				document.getElementById('notice-createdDate-read').textContent = formatDate(createdDate);
				document.getElementById('notice-updatedDate-read').textContent = formatDate(updatedDate);
				document.getElementById('notice-content-read').textContent = data.noticeContent;
				document.getElementById('notice-modify').add = data.noticeContent;
		    	
			})
			.catch(error => console.error('Error:', error));
	});
	
	//공지사항 조회 - 수정버튼
	showNoticeForm.addEventListener('submit', function(event) {
		event.preventDefault(); //기본제출 막기
		
		fetch('/notices/' + selectedNoticeId, {
			method: 'PATCH'
			, body: new FormData(showNoticeForm)
		})
		.then(response => {
			if (!response.ok) throw new Error('수정에 실패했습니다.');
			return response.json();  // REST컨트롤러 아니므로 JSON 파싱
		})
		.then(response => { // response가 ok일때
			alert(response.msg);
			location.reload();
		}).catch(error => {
			console.error('에러', error)
			alert("제목, 내용은 필수입력 사항입니다.");
		});
	});
	
	//공지사항 조회 - 삭제버튼
	deleteNoticeBtn.addEventListener('click', function(event) {
		event.preventDefault(); //기본제출 막기
		
		alert("정말 삭제하시겠습니까?");
		
		fetch('/notices/' + selectedNoticeId, {
			method: 'DELETE'			
		})
		.then(response => {
			if (!response.ok) throw new Error('삭제에 실패했습니다.');
			return response.json();  // REST컨트롤러 아니므로 JSON 파싱
		})
		.then(response => { // response가 ok일때
			alert(response.msg);
			location.reload();
		}).catch(error => {
			console.error('에러', error)
			alert("제목, 내용은 필수입력 사항입니다.");
		});
	});
	
	
	
	// 공지사항 등록 모달설정
	const createNoticeModal = document.getElementById('create-notice');
	const createNoticeForm = document.getElementById("notice-write-form");
	// 공지사항 등록 모달 열릴때 폼 초기화
	createNoticeModal.addEventListener('show.bs.modal', function(event){
		createNoticeForm.reset();
	});
	// 공지사항 등록 버튼 함수
	createNoticeForm.addEventListener('submit', function(event) {
		event.preventDefault(); //기본제출 막기
		
		fetch('/notices', {
			method: 'POST'
			, body: new FormData(createNoticeForm)
		})
		.then(response => {
			if (!response.ok) throw new Error('등록에 실패했습니다.');
			return response.json();  // REST컨트롤러 아니므로 JSON 파싱
		})
		.then(response => { // response가 ok일때
			alert(response.msg);
			location.reload();
		}).catch(error => {
			console.error('에러', error)
			alert("제목, 내용은 필수입력 사항입니다.");
		});
	});

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
		
}); // DOM로드 끝

//공지사항 수정 체크박스 값 변경
function toggleValue(checkbox){
	console.log(checkbox.value, "ddddddddddddddddd");
	if (checkbox.checked) {
		checkbox.value = "Y";
	} else {
		checkbox.value = "N"
	}
	console.log(checkbox.value, "eeeeeeeeeeeeeee");
}

// 포맷 함수 (예: yyyy-MM-dd HH:mm)
function formatDate(date) {
    const yyyy = date.getFullYear();
    const MM = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    const HH = String(date.getHours()).padStart(2, '0');
    const mm = String(date.getMinutes()).padStart(2, '0');
    return `${yyyy}-${MM}-${dd} ${HH}:${mm}`;
}

//파라미터값받아오기
function getUrlParameter(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

// 정렬버튼 클릭 함수
function allineTable(thElement) {
	// 지금 누른 orderKey 받아오기	
    const key = thElement.getAttribute('data-key');
	
	//현재 orderKey, orderMethod 
    let currentOrderKey = getUrlParameter('orderKey');
	let currentOrderMethod = getUrlParameter('orderMethod') || 'asc';
	// 새 orderMethod
    let newOrderMethod = 'asc';
    // 지금 누른 오더키가 원래의 오더키 일때 
	if (currentOrderKey === key) {
		// 새 오더메서드 방법 변경
        newOrderMethod = (currentOrderMethod === 'asc') ? 'desc' : 'asc';
    }
	//현재 검색 타입, 검색어 존재하면 유지 없으면 널스트링
    const searchKeyword = getUrlParameter('searchKeyword') || '';
    const page = getUrlParameter('page') || '';
	
	//요청할 url 조합
	let url = `${window.location.pathname}?orderKey=${key}&orderMethod=${newOrderMethod}`;
    	if(searchKeyword) url += `&searchKeyword=${encodeURIComponent(searchKeyword)}`;
    	if(page) url += `&page=${encodeURIComponent(page)}`;
	
	// 이동요청
	window.location.href = url;
}
