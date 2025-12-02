
document.addEventListener('DOMContentLoaded', function () {

    // 1. Initialize Toast Grid
    const grid = new tui.Grid({
        el: document.getElementById('grid'),
        scrollX: false,
        scrollY: false,
        columns: [
            { header: 'LOT 번호', name: 'lotNo', align: 'center', width: 150 },
            { header: '상품명', name: 'prodName', minWidth: 200 },
            { header: '카테고리', name: 'category', align: 'center', width: 100 },
            { header: 'Zone', name: 'zone', align: 'center', width: 80 },
            { header: 'Rack', name: 'rack', align: 'center', width: 80 },
            { header: 'Row', name: 'row', align: 'center', width: 80 },
            { header: 'Column', name: 'column', align: 'center', width: 80 },
            { header: '수량', name: 'qty', align: 'right', width: 100 },
            { header: '유통기한', name: 'expiryDate', align: 'center', width: 120 },
            { header: '상태', name: 'status', align: 'center', width: 100 },
            {
                header: '상세',
                name: 'btn',
                align: 'center',
                width: 80,
                formatter: function () {
                    return '<button class="btn btn-sm btn-primary btn-detail">상세</button>';
                }
            }
        ],
        data: [
            { lotNo: 'LOT-20231101-01', prodName: '에탄올 90% 1L', category: '원자재', zone: 'A', rack: '01', row: '1', column: '1', qty: '50ea', expiryDate: '2024-11-01', status: '정상' },
            { lotNo: 'LOT-20231015-05', prodName: '300ml 상품 포장박스 ', category: '부자재', zone: 'B', rack: '03', row: '2', column: '5', qty: '20ea', expiryDate: '2024-04-15', status: '정상' },
            { lotNo: 'LOT-20230520-02', prodName: '바닐라향료 10L', category: '원자재', zone: 'A', rack: '02', row: '1', column: '3', qty: '5ea', expiryDate: '2023-12-01', status: '임박' },
            { lotNo: 'LOT-20230110-99', prodName: '공병 300ml', category: '부자재', zone: 'B', rack: '05', row: '3', column: '1', qty: '1000ea', expiryDate: '-', status: '정상' }
        ]
    });

    // 2. Event Handlers

    // Toggle Advanced Search
    document.getElementById('btnToggleAdvanced').addEventListener('click', function () {
        const advancedArea = document.getElementById('advancedSearch');
        const icon = this.querySelector('i');

        if (advancedArea.style.display === 'none') {
            advancedArea.style.display = 'block';
            icon.classList.replace('bx-chevron-down', 'bx-chevron-up');
        } else {
            advancedArea.style.display = 'none';
            icon.classList.replace('bx-chevron-up', 'bx-chevron-down');
        }
    });

    // Grid Click Event (Detail Button)
    grid.on('click', function (ev) {
        if (ev.targetType === 'cell' && ev.columnName === 'btn') {
            const rowKey = ev.rowKey;
            const rowData = grid.getRow(rowKey);
            openDetailModal(rowData);
        }
    });

    // Open Detail Modal
    function openDetailModal(data) {
        console.log('openDetailModal data:', data); // Debugging
        if (!data) {
            console.error('No data received for modal');
            return;
        }

        document.getElementById('detailProdName').value = data.prodName;
        document.getElementById('detailLotNo').value = data.lotNo;
        document.getElementById('detailCategory').value = data.category;
        document.getElementById('detailQty').value = data.qty;

        // Format Location: Zone-Rack-Row-Col
        const locationStr = `${data.zone}-${data.rack}-${data.row}-${data.column}`;
        document.getElementById('detailLocation').value = locationStr;

        document.getElementById('detailExpiry').value = data.expiryDate;

        // Populate Same LOT Locations (Dummy Data)
        const tbody = document.getElementById('sameLotTableBody');
        tbody.innerHTML = ''; // Clear existing

        // Dummy data for same LOT in different locations
        const dummyLocations = [
            { zone: 'A', rack: '01', row: '1', col: '2', qty: '30' },
            { zone: 'B', rack: '02', row: '3', col: '1', qty: '20' }
        ];

        dummyLocations.forEach(loc => {
            const row = `
                <tr>
                    <td>${loc.zone}</td>
                    <td>${loc.rack}</td>
                    <td>${loc.row}</td>
                    <td>${loc.col}</td>
                    <td>${loc.qty}</td>
                </tr>
            `;
            tbody.insertAdjacentHTML('beforeend', row);
        });

        const detailModal = new bootstrap.Modal(document.getElementById('detailModal'));
        detailModal.show();
    }
});
