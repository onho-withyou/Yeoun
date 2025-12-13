/**
 * tui ui 수정스타일변경과 영어표기를 따로 만든 클래스
 */
// 영어 한글 표기 CODE_MAP
const CODE_MAP = {
	//제품유형
    'FINISHED_GOODS': '완제품',
    'SEMI_FINISHED_GOODS': '반제품',
	
	//상태코드
    'ACTIVE': '활성',
    'INACTIVE': '비활성',
    'DISCONTINUED': '단종',
    'SEASONAL': '시즌상품',
    'OUT_OF_STOCK': '재고없음',
	
	//원재료 유형
    'RAW': '원재료',
    'SUB': '부자재',
    'PKG': '포장재',
    'WIP': '공정중', // 또는 '재공품' (Work-in-Process)
    'FIN': '완제품', // 또는 '생산품' (Finished Goods)
    'BOX': '박스',
	
	//향수 유형
	'LIQUID': '고체향수', 
	'SOLID': '액체향수',
	
	//안전재고 - 정책방식
	'FIXED_QTY':'고정 계산방식',
	'COVER': '일수기반',
	
	//품질항목기준 - 대상구분
	'FINISHED_QC':'완제품'
};

class StatusModifiedRenderer {
    constructor(props) {
        const el = document.createElement('div');
        el.className = 'tui-grid-cell-content-renderer'; 
        this.el = el;
        this.grid = props.grid; 
        
        this.render(props);
    }
	
	static getKoreanText(englishValue) {
        // 분리된 PRD_STATUS_MAP을 참조합니다.
        return CODE_MAP[englishValue] || englishValue; 
    }

    getElement() {
        return this.el;
    }
	
    render(props) {
        const value = props.value;
        const rowKey = props.rowKey; 
        
		const koreanText = StatusModifiedRenderer.getKoreanText(value);
        this.el.textContent = koreanText; 
		
		
        // 💡 수정되거나 추가된 행 상태 확인 로직
        let isUpdatedOrCreated = false;
        
        if (this.grid) {
            const modifiedRows = this.grid.getModifiedRows();
            
            // 1. 수정된 행(updatedRows) 목록에서 현재 rowKey 확인
            const isUpdated = modifiedRows.updatedRows.some(row => String(row.rowKey) === String(rowKey));
			
            
            // 2. 새로 추가된 행(createdRows) 목록에서 현재 rowKey 확인
            const isCreated = modifiedRows.createdRows.some(row => String(row.rowKey) === String(rowKey));
            
            // 두 상태 중 하나라도 true이면 스타일 적용
            isUpdatedOrCreated = isUpdated || isCreated;
        }
        
        // 🎨 인라인 스타일 적용
        if (isUpdatedOrCreated) {
            // 수정되거나 추가된 행에 적용될 스타일
            this.el.style.backgroundColor = '#c3f2ffff'; 
            this.el.style.color = '#000000';         
            this.el.style.fontWeight = 'bold';
        } else {
            // 조건 불충족 시 스타일 초기화
            this.el.style.backgroundColor = '';
            this.el.style.color = '';
            this.el.style.fontWeight = '';
        }
    }
}



