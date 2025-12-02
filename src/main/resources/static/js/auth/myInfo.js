// myInfo.js
// 메인 상단바 프로필 

/* 
 * 로그아웃 여부
 */
document.querySelectorAll('.logout-link').forEach(link => {
	link.addEventListener('click', function (e) {
		e.preventDefault();
		if (confirm('로그아웃 하시겠습니까?')) {
			document.getElementById('hiddenLogoutForm').submit();
		}
	});
});

/**
 * 내 정보 모달 열기
 */
document.getElementById('btnMyInfo')?.addEventListener('click', function (e) {
	e.preventDefault();
	
	fetch('/my/info')
		.then(res => {
			if (!res.ok) throw new Error('HTTP' + res.status);
			return res.json();
		})
		.then(data => {
			const titleEl = document.getElementById('empDetailModalTitle');
			const modalEl = document.getElementById('empDetailModal');
			const editBtn = document.getElementById('editBtn');
			
			if (!modalEl || !titleEl) {
				alert('상세 보기 모달을 찾을 수 없습니다. 레이아웃 설정을 확인해주세요.');
				return;
			}
			
			titleEl.innerText = '내 정보';
			
			// 수정 버튼 숨기기
			if (editBtn) editBtn.style.display = 'none';
			
			// 텍스트 필드 세팅
			document.getElementById('d-empName').innerText = data.empName ?? '';
			document.getElementById('d-empId').innerText = data.empId ?? '';
			document.getElementById('d-deptName').innerText = data.deptName ?? '';
			document.getElementById('d-posName').innerText = data.posName ?? '';
			document.getElementById('d-gender').innerText = data.gender === 'M' ? '남' : data.gender === 'F' ? '여' : '—';
			document.getElementById('d-hireDate').innerText = data.hireDate ?? '';
			document.getElementById('d-mobile').innerText = data.mobile ?? '';
			document.getElementById('d-email').innerText = data.email ?? '';
			document.getElementById('d-address').innerText = data.address ?? '';
			document.getElementById('d-rrn').innerText = data.rrnMasked ?? '';
			document.getElementById('d-bank').innerText = data.bankInfo ?? '';
			
			// 사진도 목록 상세랑 똑같이 처리
			const photo = document.getElementById('d-photo');
			if (photo) {
				photo.onerror = () => {
					photo.src = '/img/default-profile.png';
				};
				photo.src = data.photoPath || '/img/default-profile.png';
			}
			
			const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
			modal.show();
		})
		.catch(() => {
			alert("내 정보 불러오기 실패");
		});
});

/**
 * 정보 수정 버튼
 */
document.getElementById('editBtn')?.addEventListener('click', function () {
	if (window.detailMode === 'self') {
		window.location.href = '/my/info/edit';
	}
});
























