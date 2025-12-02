//전역변수
let currentIvid;

// 수량 조절 모달 초기화
function resetAdjustQtyModal() {
    // 기본 조절 유형은 증가로
    document.getElementById('adjustInc').checked = true;
    document.getElementById('adjustDec').checked = false;

    // 수량, 사유 초기화
    document.getElementById('adjustQty').value = 1;
    document.getElementById('adjustReason').value = '';
}

// 수량 조절 모달 열기 (필요하면 현재 재고 정보도 같이 받을 수 있게 파라미터 둠)
function openAdjustQtyModal(rowData) {
	resetAdjustQtyModal();
	
	currentIvid = rowData.ivId;
	
	const modalEl = document.getElementById('adjustModal');
	const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
	modal.show();
}

// 수량조절 음수 입력 불가설정
const qtyInput = document.getElementById('adjustQty');

qtyInput.addEventListener('keydown', (e) => {
	if (e.key === '-' || e.key === '+') {
		e.preventDefault();
	}
});

qtyInput.addEventListener('input', () => {
	const val = Number(qtyInput.value);
	
	if (Number.isNaN(val)) {
	    qtyInput.value = 1;
	    return;
	}
	
	if (val < 1) {
	    qtyInput.value = 1;
	}
});

// 저장버튼 클릭시 들어갈 데이터
function getAdjustData() {
	return {
//		ivId: currentIvid,
		adjustType: document.querySelector('input[name="adjustType"]:checked').value,
		adjustQty: Number(document.getElementById('adjustQty').value || 0),
		reason: document.getElementById('adjustReason').value.trim() || ''
	}
}

// 저장버튼 클릭이벤트
const saveBtn = document.getElementById('adjustSave');
saveBtn.addEventListener('click', async () => {
	
	if (Number(document.getElementById('adjustQty').value) < 1) {
		alert("변경 수량은 1보다 커야합니다.")
	}
	
	const adjustData = getAdjustData();
	
	const response = 
	await fetch(`/api/inventorys/${currentIvid}/adjustQty`, {
		method: 'POST',
		headers: {
			[csrfHeader]: csrfToken,
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(adjustData)
	});
	
	if (!response.ok) {
		alert("수량조절에 실패하였습니다.")
	    throw new Error('수량 조절에 실패했습니다.');
	}
	
	alert("수량 조절 완료");
	window.location.reload();
});