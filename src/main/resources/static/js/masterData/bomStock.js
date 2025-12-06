window.onload = function () {	
	bomGridAllSearch();// bom그리드 조회
	safetyStockGridAllSearch()//안전재고 그리드 조회

}
const Grid = tui.Grid;
//g-grid1 bom그리드
const grid1 = new Grid({
	  el: document.getElementById('bomGrid'), 
      rowHeaders: ['rowNum','checkbox'],
	  columns: [

	    {header: 'BOMId' ,name: 'bomId' ,align: 'center'}
		,{header: '완제품 id' ,name: 'prdId' ,align: 'center'}
		,{header: '원재료 id' ,name: 'matId' ,align: 'center',width: 230}
		,{header: '원재료 사용량' ,name: 'matQty' ,align: 'center',filter: "select"}
		,{header: '사용단위' ,name: 'matUnit' ,align: 'center'}
		,{header: '순서' ,name: 'bomSeqNo' ,align: 'center'}
		,{header: '생성자ID' ,name: 'createdId' ,align: 'center'}
		,{header: '생성일자' ,name: 'createdDate' ,align: 'center'}
		,{header: '수정자ID' ,name: 'updatedId' ,align: 'center'}
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
    		perPage: 10
  	  }
	});
	

//g-grid2 안전재고 그리드
const grid2 = new Grid({
		  el: document.getElementById('safetyStockGrid'), 
	      rowHeaders: ['rowNum','checkbox'],
		  columns: [

		    {header: '품목코드' ,name: 'itemId' ,align: 'center'}
			,{header: '품목종류' ,name: 'itemType' ,align: 'center'}
			,{header: '품목명' ,name: 'itemName' ,align: 'center',width: 230}
			,{header: '용량' ,name: 'volume' ,align: 'center',filter: "select"}
			,{header: '단위' ,name: 'itemUnit' ,align: 'center'}
			,{header: '정책방식' ,name: 'policyType' ,align: 'center'}
			,{header: '정책일수' ,name: 'policyDays' ,align: 'center'}
	        ,{header: '일별 안전재고 수량' ,name: 'safetyStockQtyDaily' ,align: 'center'}
			,{header: '총 안전재고 수량' ,name: 'safetyStockQty' ,align: 'center'}
			,{header: '상태' ,name: 'status' ,align: 'center'}
			,{header: '비고' ,name: 'remark' ,align: 'center'}           
		  ],
		  data: []
		  ,bodyHeight: 500 // 그리드 본문의 높이를 픽셀 단위로 지정. 스크롤이 생김.
		  ,height:100
		  ,columnOptions: {
	    		resizable: true
	  	  }
		  ,pageOptions: {
	    		useClient: true,
	    		perPage: 10
	  	  }
});


function bomGridAllSearch() {

	fetch('/bom/list', {
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
			grid1.resetData([]);
		
		});

}

function safetyStockGridAllSearch() {

	fetch('/safetyStock/list', {
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
