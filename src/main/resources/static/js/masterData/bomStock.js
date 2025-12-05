
const Grid = tui.Grid;
//g-grid1 공정그리드
const grid1 = new Grid({
	  el: document.getElementById('bomGrid'), 
      rowHeaders: ['rowNum','checkbox'],
	  columns: [

	    {header: '순번' ,name: 'row_no' ,align: 'center',hidden: true}
		,{header: 'QC항목ID' ,name: 'prd_id' ,align: 'center',hidden: true}
		,{header: '항목명' ,name: 'item_name' ,align: 'center',width: 230}
		,{header: '대상구분' ,name: 'prd_name' ,align: 'center',filter: "select"}
		,{header: '단위' ,name: 'prd_cat' ,align: 'center'}
		,{header: '기준 텍스트' ,name: 'prd_unit' ,align: 'center'}
		,{header: 'MIN' ,name: 'unit_price' ,align: 'center'}
        ,{header: 'MAX' ,name: 'prd_status' ,align: 'center'}
		,{header: '사용' ,name: 'prd_status' ,align: 'center'}           
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
	

//g-grid1 공정그리드
const grid2 = new Grid({
		  el: document.getElementById('safetyStockGrid'), 
	      rowHeaders: ['rowNum','checkbox'],
		  columns: [

		    {header: '순번' ,name: 'row_no' ,align: 'center',hidden: true}
			,{header: 'QC항목ID' ,name: 'prd_id' ,align: 'center',hidden: true}
			,{header: '항목명' ,name: 'item_name' ,align: 'center',width: 230}
			,{header: '대상구분' ,name: 'prd_name' ,align: 'center',filter: "select"}
			,{header: '단위' ,name: 'prd_cat' ,align: 'center'}
			,{header: '기준 텍스트' ,name: 'prd_unit' ,align: 'center'}
			,{header: 'MIN' ,name: 'unit_price' ,align: 'center'}
	        ,{header: 'MAX' ,name: 'prd_status' ,align: 'center'}
			,{header: '사용' ,name: 'prd_status' ,align: 'center'}           
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