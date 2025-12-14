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
	'DAYS_COVER': '일수기반',
	
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
    const isSelect = props.columnInfo.renderer?.options?.isSelect;

    const koreanText = StatusModifiedRenderer.getKoreanText(value);

    // 신규 행 여부
    let isCreated = false;
    let isUpdated = false;

    if (this.grid) {
        const { createdRows, updatedRows } = this.grid.getModifiedRows();
        isCreated = createdRows.some(r => String(r.rowKey) === String(rowKey));
        isUpdated = updatedRows.some(r => String(r.rowKey) === String(rowKey));
    }

    const hasValue = value !== null && value !== undefined && value !== '';
    const displayText = (!hasValue && isCreated) ? '' : koreanText;

    // UI
    if (isSelect) {
        this.el.innerHTML = `
          <div style="
            width:100%;
            height:100%;
            padding:0px 10px;
            box-sizing:border-box;
            display:flex;
            justify-content:space-between;
            align-items:center;
            background:transparent;
            cursor:pointer;
          ">
            <span>${displayText}</span>
            <span style="font-size:10px;opacity:0.6;">▼</span>
          </div>
        `;
    } else {
        this.el.textContent = koreanText;
    }

    // 🎨 색상 조건 수정
    const shouldHighlight = isUpdated || (isCreated && hasValue);

    if (shouldHighlight) {
        this.el.style.backgroundColor = '#c3f2ffff';
        this.el.style.color = '#007aff';
    } else {
        this.el.style.backgroundColor = '';
        this.el.style.color = '';
    }
 
}

}



