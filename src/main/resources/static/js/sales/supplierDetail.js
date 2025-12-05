/* ==========================================================
   supplierDetail.js - 협력사 상세 (취급 품목)
========================================================== */

let itemGridApi = null;        // 협력사 취급 품목 Grid
let clientId = null;

let selectedProducts = [];     // STEP A 선택 리스트
let MATERIAL_MASTER = [];      // 원자재 목록

document.addEventListener("DOMContentLoaded", () => {

    clientId = window.clientId;

    initItemGrid();
    loadItemGrid();

    // STEP A/B 모달 버튼 연결
    document.getElementById("btnNextStep")?.addEventListener("click", goToStepB);
    document.getElementById("btnSaveItems")?.addEventListener("click", saveVendorItems);

    // STEP A 처음 로딩: 원자재 목록 가져오기
    loadMaterialList();
});


/* ==========================================================
   1) 협력사 취급품목 목록 AG-Grid
========================================================== */
function initItemGrid() {

    const columnDefs = [
        { headerName: "품목ID", field: "itemId", width: 120 },
        { headerName: "자재코드", field: "materialId", width: 150 },
        { headerName: "품명", field: "materialName", flex: 1 },
        { headerName: "단위", field: "unit", width: 80 },
        { headerName: "단가", field: "unitPrice", width: 120 },
        { headerName: "MOQ", field: "moq", width: 120 },
        {
            headerName: "공급여부",
            field: "supplyAvailable",
            width: 110,
            cellRenderer: p => (p.value === "Y" ? "가능" : "불가")
        },
        {
            headerName: "관리",
            width: 150,
            cellRenderer: p => `
                <button class="btn btn-sm btn-primary" onclick="editItem('${p.data.itemId}')">수정</button>
                <button class="btn btn-sm btn-danger ms-1" onclick="deleteItem('${p.data.itemId}')">삭제</button>
            `
        }
    ];

    itemGridApi = agGrid.createGrid(
        document.getElementById("supplierItemGrid"),
        {
            columnDefs,
            rowData: [],
            pagination: true,
            paginationPageSize: 20,
            defaultColDef: { resizable: true }
        }
    );
}

function loadItemGrid() {
    itemGridApi.setGridOption("rowData", window.initialItemList);
}


/* ==========================================================
   2) STEP A : 품목 선택 모달
========================================================== */
function openItemSelect() {
    loadMaterialList();
    document.getElementById("modalItemSelect").style.display = "flex";
}

function closeItemSelect() {
    document.getElementById("modalItemSelect").style.display = "none";
    selectedProducts = [];
}


// 원자재 목록 불러오기
function loadMaterialList() {
	fetch("/sales/client/material/data")
        .then(res => res.json())
        .then(data => {
            MATERIAL_MASTER = data;
            renderMaterialList(data);
        });
}


// STEP A 목록 렌더링
function renderMaterialList(list) {
    const tbody = document.getElementById("materialListArea");
    tbody.innerHTML = list.map(m => `
        <tr>
            <td><input type="checkbox" onchange="toggleSelect('${m.materialId}')"></td>
            <td>${m.materialName}</td>
            <td>${m.category || '-'}</td>
            <td>${m.unit}</td>
        </tr>
    `).join("");
}

// 선택 토글
function toggleSelect(id) {
    if (selectedProducts.includes(id)) {
        selectedProducts = selectedProducts.filter(x => x !== id);
    } else {
        selectedProducts.push(id);
    }
}


// 검색
function filterMaterialList() {
    const key = document.getElementById("searchProductInput").value.trim();
    const filtered = MATERIAL_MASTER.filter(m => m.materialName.includes(key));
    renderMaterialList(filtered);
}


/* ==========================================================
   3) STEP B : 공급 조건 입력
========================================================== */
function goToStepB() {

    if (selectedProducts.length === 0) {
        alert("한 개 이상 선택해주세요.");
        return;
    }

    const selected = MATERIAL_MASTER.filter(p => selectedProducts.includes(p.materialId));

    const tbody = document.getElementById("selectedInputTable");
    tbody.innerHTML = selected.map(m => `
        <tr data-id="${m.materialId}">
            <td>${m.materialName}</td>
            <td>${m.unit}</td>
            <td><input type="number" class="input-mini" placeholder="단가"></td>
            <td><input type="number" class="input-mini" placeholder="MOQ"></td>
            <td><input type="checkbox" checked></td>
        </tr>
    `).join("");

    closeItemSelect();
    document.getElementById("modalItemInput").style.display = "flex";
}

function closeItemInput() {
    document.getElementById("modalItemInput").style.display = "none";
}


/* ==========================================================
   4) STEP B 저장 → DB 저장
========================================================== */
function saveVendorItems() {

    const rows = document.querySelectorAll("#selectedInputTable tr");

    const payload = Array.from(rows).map(r => ({
        materialId: r.dataset.id,
        unit: r.children[1].innerText,
        unitPrice: r.children[2].children[0].value || 0,
        moq: r.children[3].children[0].value || 0,
        supplyAvailable: r.children[4].children[0].checked ? "Y" : "N"
    }));

    fetch(`/sales/client/${clientId}/items`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(payload)
    })
        .then(r => r.text())
        .then(msg => {
            alert("취급 품목이 저장되었습니다.");
            closeItemInput();
            location.reload();
        });
}


/* ==========================================================
   5) 수정 / 삭제 (다음 단계)
========================================================== */
function editItem(itemId) {
    alert("수정 기능은 개발 예정입니다. ID=" + itemId);
}

function deleteItem(itemId) {
    alert("삭제 기능은 개발 예정입니다. ID=" + itemId);
}
