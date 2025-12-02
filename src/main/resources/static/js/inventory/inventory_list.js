// 재고조회 js
// 문서 로딩 후 시작
document.addEventListener('DOMContentLoaded', function () {
	let searchData = getSearchData();
	console.log(searchData);
	fetchInventoryData(searchData);
});

// 검색 데이터 설정(검색, 상세검색 입력값으로 requestBody생성)
function getSearchData() {
	function addDefaultTime(dateStr, timeStr = "00:00:00") {
		if(!dateStr) return '';
		// 시간이 없을 경우 00:00:00 추가
		if(dateStr.length === 10) {
			return `${dateStr} ${timeStr}`;
		}
		//시간이 있으면 다시반환
		return dateStr;
	}
	
	return {
		lotNo: document.getElementById('searchLotNo').value.trim(),
		prodName: document.getElementById('searchProdName').value.trim(),
		category: document.getElementById('searchCategory')?
			document.getElementById('searchCategory').value:'',
		zone: document.getElementById('searchZone')?
			document.getElementById('searchZone').value:'',
		rack: document.getElementById('searchRack')?
			document.getElementById('searchRack').value:'',
		status: document.getElementById('searchStatus')?
			document.getElementById('searchStatus').value:'',
		ibDate: document.getElementById('searchDate')?
			addDefaultTime(document.getElementById('searchDate').value,"00:00:00"):'',
		expirationDate: document.getElementById('searchExpireDate')?
			addDefaultTime(document.getElementById('searchExpireDate').value,"00:00:00"):''
	};
}

// 검색데이터에 기반하여 재고 데이터 정보 가져오기
async function fetchInventoryData(searchData) {
	const response = 
		fetch('/api/inventorys', {
			method: 'POST',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(searchData)
			
		});
	if (!response.ok) {
		throw new Error('재고데이터를 가져올 수 없습니다.')
	}
	return response.json();
} 

// 검색버튼 이벤트함수
const btnSearch = document.getElementById('btnSearch');
btnSearch.addEventListener('click', () => {
	event.preventDefault(); // 폼제출 막기
	
	const searchData = getSearchData();
	fetchInventoryData(searchData); 
});




































// 상세검색버튼
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