/**
	공지사항모달 JavaScript 
**/
let selectedNoticeId = null;
// 공지사항 조회 모달설정
const showNoticeModal = document.getElementById('show-notice');
const showNoticeForm = document.getElementById("notice-form-read");
const fixedCheck = document.getElementById('fixed-check') // 체크박스
const deleteNoticeBtn = document.getElementById('notice-delete');
const modifyNoticeBtn = document.getElementById('notice-modify');


//돔로드시작
document.addEventListener('DOMContentLoaded', function() {
	// 공지사항 조회모달 열기 이벤트
	showNoticeModal.addEventListener('show.bs.modal', function(event){
		getNoticeData(selectedNoticeId);
	});
	
	//공지사항 조회 - 수정버튼
	showNoticeForm.addEventListener('submit', function(event) {
		event.preventDefault(); //기본제출 막기
		
		fetch('/notices/' + selectedNoticeId, {
			method: 'PATCH'
			, headers: {
				[csrfHeaderName]: csrfToken
			}
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
			, headers: {
				[csrfHeaderName]: csrfToken
			}			
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
			, headers: {
				[csrfHeaderName]: csrfToken
			}
			, body: new FormData(createNoticeForm)
		})
		.then(response => {
			if (!response.ok) throw new Error('등록에 실패했습니다.');
			return response.json();  //JSON 파싱
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
	if (checkbox.checked) {
		checkbox.value = "Y";
	} else {
		checkbox.value = "N"
	}
}

// 포맷 함수 (예: yyyy-MM-dd HH:mm)
function NoticeDetailFormatDate(date) {
	let inputDate = date instanceof Date ? date : new Date(date);

    if (isNaN(inputDate.getTime())) {
		console.log("실패");
        // 파싱 실패 시 fallback (원본 값 그대로 쓰거나, 빈 문자열 등)
        return '';
    }
	
    const yyyy = inputDate.getFullYear();
    const MM = String(inputDate.getMonth() + 1).padStart(2, '0');
    const dd = String(inputDate.getDate()).padStart(2, '0');
    const HH = String(inputDate.getHours()).padStart(2, '0');
    const mm = String(inputDate.getMinutes()).padStart(2, '0');
    return `${yyyy}-${MM}-${dd} ${HH}:${mm}`;
}



// 조회할 공지 데이터 불러오기
async function getNoticeData(noticeId) {
	await fetch('/api/notices/' + noticeId)
	.then(response => { // response가 200이 아닐때
		if (!response.ok) throw new Error('공지사항을 불러올 수 없습니다.');
    	
		return response.json();
	})
	.then(data => {
		console.log(data,"데이어어어어");
		inputReadData(data);
		
	})
	.catch(error => console.error('Error:', error));
}
	
// 공지조회모달 데이터 인풋함수
function inputReadData(data){
	// 날짜 문자열을 Date 객체로 변환
	console.log(data.createdDate);
	const createdDate = new Date(data.createdDate);
	console.log("craetedDate : ", createdDate);
	const updatedDate = new Date(data.updatedDate);
	console.log("updatedDate : ", updatedDate);
	const createdUser = data.createdUser;
	const createdUserName = data.empName;
	const deptName = data.deptName;
	
	document.getElementById('notice-id-read').value = data.noticeId;
	document.getElementById('notice-createdUser-read').value = createdUser;
	document.getElementById('notice-title-read').value = data.noticeTitle;
	
	if(data.noticeYN === 'Y') {
		fixedCheck.checked = true;
		fixedCheck.value = "Y";
	} else {
		fixedCheck.checked = false;
		fixedCheck.value = "N";
	}
	
	document.getElementById('notice-writer-read').textContent = `(${deptName})${createdUserName}`
	document.getElementById('notice-createdDate-read').textContent = NoticeDetailFormatDate(createdDate);
	console.log("formatDate(createdDate) : ", formatDate(createdDate));
	document.getElementById('notice-updatedDate-read').textContent = NoticeDetailFormatDate(updatedDate);
	console.log("formatDate(updatedDate) : ", formatDate(updatedDate));
	document.getElementById('notice-content-read').textContent = data.noticeContent;
	document.getElementById('notice-modify').add = data.noticeContent;
	
	initReadModal(data.createdUser);
}

// 공지조회 모달 열때 글쓴이와 접속자 동일인물 판별
function initReadModal(createdUser) {
	if(currentUserId == createdUser) { // 로그인직원과 글쓴이가 동일인물
		Array.from(showNoticeForm.elements).forEach(el => {
			if(el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
				el.readOnly = false;
			}
			if(el.tagName === 'SELECT' || el.type === 'checkbox' || el.type === 'file'){
				el.disabled = false;
			}
		});
		deleteNoticeBtn.disabled = false;
		modifyNoticeBtn.disabled = false;
	} else if(currentUserId != createdUser) { //일치하지 않을떄 수정,삭제 불가능
		Array.from(showNoticeForm.elements).forEach(el => {	
			if(el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
				el.readOnly = true;
			}
			if(el.tagName === 'SELECT' || el.type === 'checkbox' || el.type === 'file'){
				el.disabled = true;
			}
		});
		deleteNoticeBtn.disabled = true;
		modifyNoticeBtn.disabled = true;
	}
}

// 공지사항 등록시 첨부파일 이벤트
const msg = "[[${errorMessage}]]";
if(msg && msg.trim() != "") {
	alert(msg);
}

const fileInput = document.getElementById('notice-files-write');
// 첨부파일 변화시 동작이벤트
fileInput.addEventListener('change', function(event) {
	const files = event.target.files;
	
	const fileCountLimit = 5;
	
	if(files.length > fileCountLimit) { // 파일 갯수가 5개보다 많을 경우
		alert("최대 첨부 가능한 갯수 : " + fileCountLimit);
		files = imageFiles.slice(0, fileCountLimit);
	}
	// 미리보기1) 이미지 프리뷰 영역 초기화
	document.getElementById("imgPreviewArea").html = "";
	
	// 기존 파일 선택 영역을 교체할 새로운 파일 목록 객체 변경에 사용되는 DataTransfer 객체 생성
	let newFiles = new DataTransfer();
	
	// 이미지 파일 갯수만큼 반복문 수행
	$.each(files, function(index, file) {
		// 현재 반복중인 파일 1개를 새로운 DataTransfer 객체에 추가
		newFiles.items.add(file);
		// ------------------------
		// 미리보기2) FileReader 객체 생성
		let reader = new FileReader();
	
		// 미리보기3) FileReader 객체의 onload 이벤트 핸들링
		// => 아래쪽의 reader.readAsDataURL(file) 코드가 실행되어 이미지 파일을 로딩 완료할 경우 이벤트 발생함
		reader.onload = function(e) {
			console.log(e.target.result);
			console.log(e);
			
			// 미리보기 3-1) 미리보기 HTML 요소 생성
			// => <img> 태그를 활용하여 읽어온 이미지(e.target.result)를 src 속성에 전달
			const imgPreview = $(`<div class="div-img-preview">`)
								.append(`
									<img src="${e.target.result}" alt="미리보기" class="img-preview">
									<span>${file.name}</span>	
								`);
								
			
			// 미리보기 3-2) 미리보기 영역에 요소 추가
			$("#imgPreviewArea").append(imgPreview);
		}
		
		// 미리보기4) 이미지 파일 읽어오기
		// => 이미지 파일 같은 Base64 형식의 인코딩 된 데이터를 읽어오기 위해 
		//    FileReader 객체의 readAsDataURL() 메서드 활용
		//    (이미지 미리보기나 AJAX 를 활용한 파일 업로드 등에 활용 가능)
		// => 실제 동작 순서는 readAsDataURL() 메서드를 통해 파일 읽어온 후 onload 이벤트 호출됨
		reader.readAsDataURL(file);						
	});
	// --------------------------------------------------
	// 파일 선택 요소 객체의 files 속성에 접근하여 새로운 파일 목록 객체로 업데이트
	$("#custom-file-input")[0].files = newFiles.files;
});










