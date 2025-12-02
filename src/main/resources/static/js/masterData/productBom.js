
const Grid = tui.Grid;
//g-grid1 완제품(상위품번)
const grid1 = new Grid({
	  el: document.getElementById('productGrid'), 
      rowHeaders: ['rowNum','checkbox'],
	  columns: [

	    {header: '순번' ,name: 'row_no' ,align: 'center',hidden: true}
		,{header: '품번' ,name: 'prd_id' ,align: 'center',hidden: true}
		,{header: '품목명' ,name: 'item_name' ,align: 'center',width: 230}
		,{header: '제품명' ,name: 'prd_name' ,align: 'center',filter: "select"}
		,{header: '제품유형' ,name: 'prd_cat' ,align: 'center'}
		,{header: '단위' ,name: 'prd_unit' ,align: 'center'}
		,{header: '단가' ,name: 'unit_price' ,align: 'center'}
        ,{header: '상태' ,name: 'prd_status' ,align: 'center'}
		,{header: '유효일자' ,name: 'effective_date' ,align: 'center'}
        ,{header: '제품상세설명' ,name: 'prd_spec' ,align: 'center'}
        ,{header: '생성자ID' ,name: 'created_id' ,align: 'center'}
        ,{header: '생성일자' ,name: 'created_date' ,align: 'center'}
        ,{header: '수정자ID' ,name: 'updated_id' ,align: 'center'}
        ,{header: '수정일시' ,name: 'updated_date' ,align: 'center'}           
	  ],
	  data: []
	  ,bodyHeight: 200 // 그리드 본문의 높이를 픽셀 단위로 지정. 스크롤이 생김.
	  ,height:100
	  ,columnOptions: {
    		resizable: true
  	  }
	  ,pageOptions: {
    		useClient: true,
    		perPage: 10
  	  }
	});
	
//g-grid2 BOM(하위품번) (원재료+BOM조인필요)
const grid2 = new Grid({
	    el: document.getElementById('bomGrid'),
        rowHeaders: ['rowNum','checkbox'],
	    columns: [
	    {header: '순번' ,name: 'row_no' ,align: 'center',hidden: true}
	    ,{header: 'BOMID' ,name: 'bom_id' ,align: 'center'}
	    ,{header: '제품ID' ,name: 'prd_id' ,align: 'center',width: 230}
	    ,{header: '원재료ID' ,name: 'mat_id' ,align: 'center',filter: "select"}
	    ,{header: '원재료이름' ,name: 'mat_name' ,align: 'center',filter: "select"}
        ,{header: '용량' ,name: 'mat_qty' ,align: 'center'}
	    ,{header: '단위' ,name: 'mat_unit' ,align: 'center'}
        ,{header: '순서' ,name: 'bom_seq_no' ,align: 'center'}
        ,{header: '상세설명(원재료)' ,name: 'mat_spec' ,align: 'center'}
        ,{header: '생성자ID' ,name: 'created_id' ,align: 'center'}
        ,{header: '생성일자' ,name: 'created_date' ,align: 'center'}
        ,{header: '수정자ID' ,name: 'updated_id' ,align: 'center'}
        ,{header: '수정일시' ,name: 'updated_date' ,align: 'center'}           
	    ],
	    data: []
	    ,bodyHeight: 300 // 그리드 본문의 높이를 픽셀 단위로 지정. 스크롤이 생김.
	    ,height:100
	    ,columnOptions: {
	    	resizable: true
        }
	    ,pageOptions: {
	    	useClient: true,
	    	perPage: 10
        }
});