window.onload = function () {	
	productRouteSearch();//제품별 공정라우트 그리드 조회
	processCodeGridAllSearch();//공정코드 관리 그리드 조회
}

const Grid = tui.Grid;
//g-grid1 공정그리드
const grid1 = new Grid({
	  el: document.getElementById('processGrid'), 
      rowHeaders: ['rowNum','checkbox'],
	  columns: [

	    {header: '순번' ,name: 'row_no' ,align: 'center',hidden: true}
		,{header: '라우트ID' ,name: 'prd_id' ,align: 'center',hidden: true}
		,{header: '제품군' ,name: 'item_name' ,align: 'center',width: 230}
		,{header: '라우트명' ,name: 'prd_name' ,align: 'center',filter: "select"}
		,{header: '사용' ,name: 'prd_cat' ,align: 'center'}
		,{header: '공정단계 수' ,name: 'prd_unit' ,align: 'center'}
		,{header: '비고' ,name: 'unit_price' ,align: 'center'}
        ,{header: '작업' ,name: 'prd_status' ,align: 'center'}           
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
	
//g-grid2 공정코드 관리
const grid2 = new Grid({
	    el: document.getElementById('processCodeGrid'),
        rowHeaders: ['rowNum','checkbox'],
	    columns: [
	    {header: '순번' ,name: 'row_no' ,align: 'center',hidden: true}
	    ,{header: '공정ID' ,name: 'bom_id' ,align: 'center'}
	    ,{header: '공정명' ,name: 'prd_id' ,align: 'center',width: 230}
	    ,{header: '유형' ,name: 'mat_id' ,align: 'center',filter: "select"}
	    ,{header: '사용' ,name: 'mat_name' ,align: 'center',filter: "select"}
        ,{header: '설명' ,name: 'mat_qty' ,align: 'center'}
	    ,{header: '단위' ,name: 'mat_unit' ,align: 'center'}
        ,{header: '작업' ,name: 'bom_seq_no' ,align: 'center}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 '}
          
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

let processLookupModal; // 공정코드 조회 모달
document.addEventListener("DOMContentLoaded", () => {
  processLookupModal = new bootstrap.Modal(document.getElementById("processLookup-modal"));

});

// + 신규라우트 -->  공정코드 조회 2번째 모달
function openProcessLookupModal() {
    processLookupModal.show();
  }
  
//제품별 공정라우트 그리드 조회
function productRouteSearch(){
	
	const params = {

		prdId: document.getElementById("processprdId").value ?? "",
		routeName: document.getElementById("routeName").value ?? "",		
	};
	fetch('/masterData/process/list', {
		method: 'GET',
		headers: {
			[csrfHeader]: csrfToken,
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(params)
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

//공정코드 관리 그리드 조회
function processCodeGridAllSearch() {
	
}