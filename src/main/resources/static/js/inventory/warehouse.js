// Konva Stage 생성
// 대시보드 너비에 맞춤 (가로형 배치)
const containerBlock = document.getElementById('container');
const stageWidth = containerBlock.offsetWidth || 1000;
const stageHeight = 400;

const stage = new Konva.Stage({
    container: "container",
    width: stageWidth,
    height: stageHeight,
    draggable: true // 화면이 좁을 경우 드래그로 이동 가능
});

const layer = new Konva.Layer();
stage.add(layer);

// 디자인 상수 (플랫 디자인)
const CONFIG = {
    // 랙(Zone) 카드 스타일
    cardBg: "#ffffff",
    cardStroke: "#e2e8f0",
    cardShadow: {
        color: 'black',
        blur: 10,
        offset: { x: 2, y: 5 },
        opacity: 0.05
    },
    
    // 셀 스타일
    cellSize: 26,     // 셀 크기
    cellGap: 4,       // 셀 간격
    
    // 색상 팔레트
    colors: {
        headerBg: "#2d3748", // Zone 헤더 배경 (진한 남색)
        headerText: "#ffffff",
        cellEmpty: "#edf2f7", // 빈 셀 (회색)
        cellFill: "#667eea",  // 재고 있음 (산뜻한 파란색)
        cellHover: "#f6ad55", // 마우스 오버 (오렌지)
        text: "#718096"
    }
};

// 2D Zone 카드 그리기 함수
function drawZoneCard(startX, startY, zoneName, cells) {
    const group = new Konva.Group({
        x: startX,
        y: startY
    });

    let realMaxCol = 0; // 실제 데이터의 최대 열
    let realMaxRow = 0; // 실제 데이터의 최대 행

    if (cells.length > 0) {
        realMaxCol = Math.max(...cells.map(c => c.colIdx));
        realMaxRow = Math.max(...cells.map(c => c.rowIdx));
    }
    
    // 레이아웃 크기 설정 (최소 크기 유지)
    const layoutMaxCol = Math.max(realMaxCol, 4); 
    const layoutMaxRow = Math.max(realMaxRow, 3);

    const unitSize = CONFIG.cellSize + CONFIG.cellGap;

    // 카드 내부 컨텐츠(셀 영역)의 전체 너비
    const contentWidth = layoutMaxCol * unitSize - CONFIG.cellGap;
    const contentHeight = layoutMaxRow * unitSize - CONFIG.cellGap;
    
    const padding = 20;
    const headerHeight = 40;
    
    const cardWidth = contentWidth + (padding * 2);
    const cardHeight = contentHeight + (padding * 2) + headerHeight;

    // (카드설정너비 - 실제데이터너비) / 2
    const realContentWidth = realMaxCol * unitSize - CONFIG.cellGap;
    const centerOffsetX = (contentWidth - realContentWidth) / 2;

    // 카드 배경
    const cardBg = new Konva.Rect({
        width: cardWidth,
        height: cardHeight,
        fill: CONFIG.cardBg,
        stroke: CONFIG.cardStroke,
        strokeWidth: 1,
        cornerRadius: 8,
        shadowColor: CONFIG.cardShadow.color,
        shadowBlur: CONFIG.cardShadow.blur,
        shadowOffset: CONFIG.cardShadow.offset,
        shadowOpacity: CONFIG.cardShadow.opacity
    });

    // 헤더
    const headerBg = new Konva.Rect({
        width: cardWidth,
        height: headerHeight,
        fill: CONFIG.colors.headerBg,
        cornerRadius: [8, 8, 0, 0]
    });

    const headerText = new Konva.Text({
        x: 0,
        y: 12,
        width: cardWidth,
        text: `ZONE ${zoneName}`,
        fontSize: 16,
        fontStyle: 'bold',
        fill: CONFIG.colors.headerText,
        align: 'center'
    });

    group.add(cardBg);
    group.add(headerBg);
    group.add(headerText);

    // 셀 그리기
    cells.forEach(cell => {
        // 좌표 계산 시 centerOffsetX를 더하기
        const cx = padding + centerOffsetX + (cell.colIdx - 1) * unitSize;
        
        // Y축 관련 아래에서부터 쌓이도록 (layoutMaxRow 기준)
        const cy = headerHeight + padding + (layoutMaxRow - cell.rowIdx) * unitSize;

        const isFull = true; 

        const cellRect = new Konva.Rect({
            x: cx,
            y: cy,
            width: CONFIG.cellSize,
            height: CONFIG.cellSize,
            fill: isFull ? CONFIG.colors.cellFill : CONFIG.colors.cellEmpty,
            cornerRadius: 4
        });

        const cellText = new Konva.Text({
            x: cx,
            y: cy + 7, // 폰트 크기에 따라 중앙 미세 조정
            width: CONFIG.cellSize,
            text: cell.rackCol.toString(),
            fontSize: 10,
            fill: isFull ? '#fff' : CONFIG.colors.text,
            align: 'center'
        });

        const cellGroup = new Konva.Group();
        cellGroup.add(cellRect);
        cellGroup.add(cellText);

        // 이벤트 리스너
        cellGroup.on('mouseenter', () => {
            document.body.style.cursor = 'pointer';
            cellRect.fill(CONFIG.colors.cellHover);
            layer.batchDraw();
        });
        cellGroup.on('mouseleave', () => {
            document.body.style.cursor = 'default';
            cellRect.fill(isFull ? CONFIG.colors.cellFill : CONFIG.colors.cellEmpty);
            layer.batchDraw();
        });
        cellGroup.on('click', () => {
            alert(`[Zone ${zoneName}]\n위치: ${cell.rackRow}-${cell.rackCol}`);
        });

        group.add(cellGroup);
    });

    return { group, width: cardWidth };
}

// 데이터 로드 및 배치
async function getLocationData() {
    try {
        const response = await fetch('/api/inventories/locations');
        if (!response.ok) throw new Error("Err");
        return await response.json();
    } catch (e) { return []; }
}

document.addEventListener("DOMContentLoaded", async () => {
    
    const rawData = await getLocationData();

    if (!rawData || rawData.length === 0) return;

    // 데이터 그룹화
    const zones = {};
    rawData.forEach(d => {
        if (!zones[d.zone]) zones[d.zone] = [];
        const colNum = parseInt(d.rackCol, 10);
        const rowNum = d.rackRow.toUpperCase().charCodeAt(0) - 64; 
        zones[d.zone].push({ ...d, colIdx: colNum, rowIdx: rowNum });
    });

    // 배치 시작 (가로 한 줄 배치)
    let currentX = 20; // 시작 X 좌표
    const startY = 40; // 시작 Y 좌표
    const gapX = 30;   // 카드 사이 간격

    Object.keys(zones).sort().forEach(zoneName => {
        const cells = zones[zoneName];
        
        const result = drawZoneCard(currentX, startY, zoneName, cells);
        
        layer.add(result.group);
        
        // 다음 카드를 위해 X좌표 이동
        currentX += result.width + gapX;
    });

    layer.draw();
});
