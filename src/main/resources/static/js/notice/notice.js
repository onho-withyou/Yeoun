/**
	공지사항 JavaScript 
**/
// 공지사항 조회 모달설정
const showNoticeModal = document.getElementById('show-notice');

// 공지사항 조회모달 열기 이벤트
showNoticeModal.addEventListener('show.bs.modal', function(event){
	const button = event.relatedTarget;
	console.log(button);
	const noticeId = button.getAttribute('data-notice-id');
	console.log(noticeId);
	fetch('/api/notices/' + noticeId)
		.then(response => { // response가 200이 아닐때
			if (!response.ok) throw new Error('공지사항을 불러올 수 없습니다.');
	    	
			return response.json();
		})
		.then(data => {
			console.log(data);
			document.getElementById('notice-id-read').value = data.noticeId;
			document.getElementById('notice-title-read').value = data.noticeTitle;
			const fixedCheck = document.getElementById('fixed-check')
			data.noticeYN === 'Y' ? fixedCheck.checked = true : fixedCheck.checked = false;
			document.getElementById('notice-writer-read').textContent = data.createdUser;
			document.getElementById('notice-createdDate-read').textContent = data.createdDate;
			document.getElementById('notice-updatedDate-read').textContent = data.updatedDate;
			document.getElementById('notice-content-read').textContent = data.noticeContent;
			document.getElementById('notice-modify').add = data.noticeContent;
	    	
		})
		.catch(error => console.error('Error:', error));
});


// 공지사항 등록 모달설정
const createNoticeModal = document.getElementById('create-notice');

// 공지사항 등록 모달 열기 이벤트
createNoticeModal.addEventListener('show.bs.modal', function(event){
	conosole.log("공지등록모달 열기")	
});