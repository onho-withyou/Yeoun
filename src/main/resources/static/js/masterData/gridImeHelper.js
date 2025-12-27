// Reusable IME and printable-key handling for tui.Grid instances
// tui.Grid 인스턴스에 대해 IME(한글 등)와 일반 문자 입력 처리를 재사용 가능하게 구현합니다.
// 사용 예시: initGridImeSupport([{ id: 'productGrid', grid: grid1, containerId: 'productGrid' }, ...])
function initGridImeSupport(grids) {
  // 마지막 포커스 정보를 저장합니다. (어떤 그리드, 행, 컬럼이 포커스인지)
  let lastFocused = { gridId: null, rowKey: null, columnName: null, grid: null, containerId: null };
  // 에디터 DOM에 붙인 리스너 중복 방지 (WeakSet으로 요소 추적)
  const attachedEditorEls = new WeakSet();

  // 전역 keydown 캡처 핸들러
  const keydownHandler = function (ev) {
    const { gridId, rowKey, columnName, grid, containerId } = lastFocused;
    console.debug('initGridImeSupport: keydown', { gridId, rowKey, columnName, key: ev.key, code: ev.keyCode, repeat: ev.repeat });
    if (!grid || rowKey == null) return;
    // 컬럼 정보와 NumberOnlyEditor 여부를 먼저 확인하여 편집 중에도 숫자 컬럼은 처리할 수 있게 함
    let isNumberEditor = false;
    try {
      const colInfoPre = (typeof grid.getColumn === 'function') ? grid.getColumn(columnName) : null;
      const editorDefPre = colInfoPre && colInfoPre.editor;
      isNumberEditor = (typeof NumberOnlyEditor !== 'undefined') && (
        editorDefPre === NumberOnlyEditor ||
        (editorDefPre && editorDefPre.type === NumberOnlyEditor) ||
        (editorDefPre && editorDefPre.type && editorDefPre.type.name === 'NumberOnlyEditor')
      );
    } catch (e) { /* ignore */ }
    if (typeof grid.getFocusedCell === 'function' && grid.getFocusedCell().editing && !isNumberEditor) return;

    const isPrintable = ev.key && ev.key.length === 1 && !ev.ctrlKey && !ev.metaKey && !ev.altKey;
    const isIme = (ev.keyCode === 229) || ev.key === 'Process' || ev.key === 'Unidentified';
    if (!(isPrintable || isIme)) return;

    if (isIme) {
      grid.startEditing(rowKey, columnName);
      return;
    }

    const keyChar = ev.key;
    // 키를 계속 눌러서 발생하는 반복 이벤트는 무시합니다 (ev.repeat이 true)
    if (ev.repeat) {
      ev.preventDefault();
      return;
    }
    const container = document.getElementById(containerId || gridId);

    // NumberOnlyEditor 전용 로직 시도
    try {
      const colInfo = (typeof grid.getColumn === 'function') ? grid.getColumn(columnName) : null;
      const editorDef = colInfo && colInfo.editor;

      const isNumberEditor = (typeof NumberOnlyEditor !== 'undefined') && (
        editorDef === NumberOnlyEditor ||
        (editorDef && editorDef.type === NumberOnlyEditor) ||
        (editorDef && editorDef.type && editorDef.type.name === 'NumberOnlyEditor')
      );
        if (isNumberEditor) {
            grid.startEditing(rowKey, columnName);

            setTimeout(() => {
                const inputEl = document.querySelector('.tui-grid-layer-editing input');
                
                if (inputEl) {
                    // [핵심] 여기서 inputEl.value = combinedVal; 이 부분을 지워야 합니다!
                    // 에디터가 이미 열렸으므로 포커스만 잡아줍니다.
                    inputEl.focus();
                    
                    // 커서만 맨 뒤로 보냅니다.
                    const len = inputEl.value.length;
                    inputEl.setSelectionRange(len, len);
                    
                    console.debug('헬퍼: 편집 모드 활성화 완료 (값은 에디터가 처리)');
                }
            }, 50);
        }
       
    } catch (e) { /* ignore */ }

    // 일반 에디터용 폴백: startEditing 후 DOM 입력 시도(재시도)
    grid.startEditing(rowKey, columnName);
    let attempts = 0;
    const tryInsert = () => {
      attempts++;
      const editorWrap = container && container.querySelector('.tui-grid-editor');
      const editorInput = editorWrap && editorWrap.querySelector('input, textarea, [contenteditable="true"]');
      if (editorInput) {
        try {
          const tag = (editorInput.tagName || '').toUpperCase();
          const type = (editorInput.type || '').toLowerCase();
          const isTextLike = editorInput.isContentEditable || tag === 'TEXTAREA' || (tag === 'INPUT' && ['text','search','tel','url','email','password','number'].includes(type));
          if (!isTextLike) {
            console.debug('initGridImeSupport: found non-text editorInput, skipping insertion', tag, type);
          } else {
            console.debug('initGridImeSupport: editorInput found, valueBefore=', editorInput.value);
            editorInput.focus();
            const start = typeof editorInput.selectionStart === 'number' ? editorInput.selectionStart : (editorInput.value || '').length;
            const val = editorInput.value || '';
            editorInput.value = val.slice(0, start) + keyChar + val.slice(start);
            const pos = start + keyChar.length;
            if (typeof editorInput.setSelectionRange === 'function') editorInput.setSelectionRange(pos, pos);
            editorInput.dispatchEvent(new Event('input', { bubbles: true }));
            console.debug('initGridImeSupport: editorInput valueAfter=', editorInput.value);
            ev.preventDefault();
          }
        } catch (e) { console.debug('initGridImeSupport: editorInput insertion error', e); }
        return;
      }
      if (attempts < 5) setTimeout(tryInsert, 20);
    };
    setTimeout(tryInsert, 0);
  };

  // 전달받은 각 그리드에 대해 focusChange 이벤트를 등록하여 포커스를 추적합니다.
  grids.forEach(g => {
    if (!g || !g.grid) return;
    const gid = g.id || g.containerId || '';
    const containerId = g.containerId || gid;
    if (g.grid && typeof g.grid.on === 'function') {
      g.grid.on('focusChange', (ev) => {
        lastFocused = { gridId: gid, rowKey: ev.rowKey, columnName: ev.columnName, grid: g.grid, containerId };
        console.debug('initGridImeSupport: focusChange ->', gid, ev.rowKey, ev.columnName);
      });
      console.debug('initGridImeSupport: registered grid', gid);
    } else {
      console.debug('initGridImeSupport: grid has no .on', gid);
    }
  });

    // 전달받은 각 그리드에 대해 focusChange 이벤트를 등록하여 포커스를 추적합니다.
  grids.forEach(g => {
    if (!g || !g.grid) return;
    const gid = g.id || g.containerId || '';
    if (g.grid && typeof g.grid.on === 'function') {
      // single click으로 select 에디터 즉시 열기
      g.grid.on('click', (ev) => {
        console.log("")
        try {
          if (!ev || ev.rowKey == null || !ev.columnName) return;
          // 이미 편집중이면 무시
          const focused = (typeof g.grid.getFocusedCell === 'function') ? g.grid.getFocusedCell() : null;
          if (focused && focused.editing) return;
          const col = (typeof g.grid.getColumn === 'function') ? g.grid.getColumn(ev.columnName) : null;
          const isSelectEditor = (col.editor.type.name === 'SelectEditor') ? true: false;
          //console.log("col.editor",col.editor );
          if(isSelectEditor){
            g.grid.startEditing(ev.rowKey, ev.columnName); 
          }
        } catch (e) { /* ignore */ }
      });

    } else {
      console.debug('initGridImeSupport: grid has no .on', gid);
    }
  });

  // 전역 캡처 단계에서 키 이벤트를 가로챕니다.
  document.addEventListener('keydown', keydownHandler, true);

  // 해제 함수 반환: 필요 시 리스너 제거용
  return function dispose() {
    document.removeEventListener('keydown', keydownHandler, true);
  };
}

// 브라우저 전역에서 사용 가능하도록 window에 노출
// (모듈 시스템이 있으면 별도 export가 필요할 수 있음)
if (typeof window !== 'undefined') window.initGridImeSupport = initGridImeSupport;
