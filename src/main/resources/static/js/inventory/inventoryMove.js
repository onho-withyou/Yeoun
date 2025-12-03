//전역변수

// 재고 이동 모달 초기화 함수
function resetMoveModal() {
    const moveZone = document.getElementById('moveZone');
    const moveRack = document.getElementById('moveRack');
    const moveRow = document.getElementById('moveRow');
    const moveColumn = document.getElementById('moveColumn');
    const moveQty = document.getElementById('moveQty');
	const ivQtyMove = document.getElementById('ivQtyMove');
	const exPectObQtyMove = document.getElementById('exPectObQtyMove');
    const canMoveAmount = document.getElementById('canMoveAmount');
	
    // 모든 입력값 초기화
    moveZone.value = 'A';    // Zone 기본값 A
    moveRack.value = '';     // Rack 초기화
    moveRow.value = '';      // Row 초기화
    moveColumn.value = '';   // Column 초기화
    moveQty.value = '0';     // 수량 0으로 초기화
	ivQtyMove.value = '';
	exPectObQtyMove.value = '';
	canMoveAmount.value = '';
}

// 재고이동 모달 열기
function openMoveModal(rowData) {
	resetMoveModal();
	
	document.getElementById('ivQtyMove').value = rowData.ivAmount;
	document.getElementById('exPectObQtyMove').value = rowData.expectObAmount;
	document.getElementById('canMoveAmount').value = rowData.ivAmount - rowData.expectObAmount;
	
	const modalEl = document.getElementById('moveModal');
	const bsModal = bootstrap.Modal.getOrCreateInstance(modalEl);
	bsModal.show();
}





















