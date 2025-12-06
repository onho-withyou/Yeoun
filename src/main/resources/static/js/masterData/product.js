


window.onload = function () {	
	productGridAllSearch();//완제품 그리드 조회
	materialGridAllSearch()//원재료 그리드 조회

}


document.querySelectorAll('button[data-bs-toggle="tab"]').forEach(tab => {
    tab.addEventListener('shown.bs.tab', function (e) {
        const targetId = e.target.getAttribute('data-bs-target');

        if (targetId === '#navs-product-tab') {//완제품탭
            grid1.refreshLayout();
        } else if (targetId === '#navs-material-tab') {//원재료 탭
            grid2.refreshLayout();
        }
    });
});



const Grid = tui.Grid;
//g-grid1 완제품(상위품번)
const grid1 = new Grid({
	  el: document.getElementById('productGrid'), 
      rowHeaders: ['rowNum','checkbox'],
	  columns: [

	    {header: '순번' ,name: 'row_no' ,align: 'center',hidden: true}
		,{header: '품번' ,name: 'prdId' ,align: 'center'}
		,{header: '품목명' ,name: 'itemName' ,align: 'center',width: 230}
		,{header: '제품명' ,name: 'prdName' ,align: 'center',filter: "select"}
		,{header: '제품유형' ,name: 'prdCat' ,align: 'center'}
		,{header: '단위' ,name: 'prdUnit' ,align: 'center'}
		,{header: '단가' ,name: 'unitPrice' ,align: 'center'}
        ,{header: '상태' ,name: 'prdStatus' ,align: 'center'}
		,{header: '유효일자' ,name: 'effectiveDate' ,align: 'center'}
        ,{header: '제품상세설명' ,name: 'prdSpec' ,align: 'center'}
        ,{header: '생성자ID' ,name: 'createdId' ,align: 'center'}
        ,{header: '생성일자' ,name: 'createdDate' ,align: 'center'}
        ,{header: '수정자ID' ,name: 'updatedId' ,align: 'center'}
        ,{header: '수정일시' ,name: 'updatedDate' ,align: 'center'}           
	  ],
	  data: []
	  ,bodyHeight: 500 // 그리드 본문의 높이를 픽셀 단위로 지정. 스크롤이 생김.
	  ,height:100
	  ,columnOptions: {
    		resizable: true
  	  }
	  ,pageOptions: {
    		useClient: true,
    		perPage: 20
  	  }
	});
	
//g-grid2 원재료(하위품번)
const grid2 = new Grid({
	    el: document.getElementById('materialGrid'),
        rowHeaders: ['rowNum','checkbox'],
	    columns: [
		    {header: '원재료ID' ,name: 'matId' ,align: 'center'}
		    ,{header: '원재료 품목명' ,name: 'matName' ,align: 'center'}//
		    ,{header: '원재료 유형' ,name: 'matType' ,align: 'center',filter: "select"}
		    ,{header: '단위' ,name: 'matUnit' ,align: 'center'}
	        ,{header: '유효일자' ,name: 'effectiveDate' ,align: 'center'}
	        ,{header: '상세설명(원재료)' ,name: 'matDesc' ,align: 'center',width: 280}
	        ,{header: '생성자ID' ,name: 'createdId' ,align: 'center'}
	        ,{header: '생성일자' ,name: 'createdDate' ,align: 'center'}
	        ,{header: '수정자ID' ,name: 'updatedId' ,align: 'center',hidden: true}
	        ,{header: '수정일시' ,name: 'updatedDate' ,align: 'center',hidden: true}           
	    ],
	    data: []
	    ,bodyHeight: 500 // 그리드 본문의 높이를 픽셀 단위로 지정. 스크롤이 생김.
	    ,height:100
	    ,columnOptions: {
	    	resizable: true
        }
	    ,pageOptions: {
	    	useClient: true,
	    	perPage: 20
        }
});
	
function productGridAllSearch() {

	fetch('/masterData/product/list', {
		method: 'GET',
		headers: {
			[csrfHeader]: csrfToken,
			'Content-Type': 'application/json'
		},
		
	})
	.then(res => {
	    if (!res.ok) {
	        throw new Error(`HTTP error! status: ${res.status}`);
	    }
	    
	    // 💡 추가된 로직: 응답 본문이 비어 있는지 확인
	    const contentType = res.headers.get("content-type");
	    if (!contentType || !contentType.includes("application/json")) {
	        // Content-Type이 JSON이 아니거나, 200 OK인데 본문이 비어있다면 (Empty)
	        if (res.status === 204 || res.headers.get("Content-Length") === "0") {
	             return []; // 빈 배열 반환하여 grid 오류 방지
	        }
	        // JSON이 아닌 다른 데이터(HTML 오류 등)가 있다면 텍스트로 읽어 오류 발생
	        return res.text().then(text => {
	            throw new Error(`Expected JSON but received: ${text.substring(0, 100)}...`);
	        });
	    }

	    return res.json(); // 유효한 JSON일 때만 파싱 시도
	})
		.then(data => {
			
			console.log("검색데이터:", data);
			grid1.resetData(data);
		})
		.catch(err => {
			console.error("조회오류", err);
			//grid1.resetData([]);
		
		});

}

function materialGridAllSearch() {

	fetch('/material/list', {
		method: 'GET',
		headers: {
			[csrfHeader]: csrfToken,
			'Content-Type': 'application/json'
		},
		
	})
	.then(res => {
	    if (!res.ok) {
	        throw new Error(`HTTP error! status: ${res.status}`);
	    }
	    
	    // 💡 추가된 로직: 응답 본문이 비어 있는지 확인
	    const contentType = res.headers.get("content-type");
	    if (!contentType || !contentType.includes("application/json")) {
	        // Content-Type이 JSON이 아니거나, 200 OK인데 본문이 비어있다면 (Empty)
	        if (res.status === 204 || res.headers.get("Content-Length") === "0") {
	             return []; // 빈 배열 반환하여 grid 오류 방지
	        }
	        // JSON이 아닌 다른 데이터(HTML 오류 등)가 있다면 텍스트로 읽어 오류 발생
	        return res.text().then(text => {
	            throw new Error(`Expected JSON but received: ${text.substring(0, 100)}...`);
	        });
	    }

	    return res.json(); // 유효한 JSON일 때만 파싱 시도
	})
		.then(data => {
			
			console.log("검색데이터:", data);
			grid2.resetData(data);
		})
		.catch(err => {
			console.error("조회오류", err);
			grid2.resetData([]);
		
		});

}