
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


function openProcessLookupModal() {
    processLookupModal.show();
  }