// qc_regist.js

let qcRegistGrid = null;

document.addEventListener("DOMContentLoaded", () => {
	
	const gridEl = document.getElementById("qcRegistGrid");
	
	qcRegistGrid = new tui.Grid({
		el: gridEl,
		rowHeaders: ["rowNum"],
		scrollX: false,
		scrollY: false,
		columnOptions: {
			resizable: true
	    },
		pageOptions: {
			useClient: true,         
			perPage: 10            
		},
		columns: [
			{
				header: '작업지시번호',
				name: 'orderId'
			},
			{
				header: '제품코드',
				name: 'prdId'
			},
			{
				header: '제품명',
				name: 'prdName'
			},
			{
				header: '지시수량',
				name: 'planQty'
			},
			{
				header: '상태',
				name: 'overallResult'
			},
			{
				header: '검사일',
				name: 'inspectionDate'
			},
			{
			  	header: " ",
			  	name: "btn",
			 	width: 90,
			  	align: "center",
			  	formatter: () =>
			    	"<button type='button' class='btn btn-info btn-sm'>검사등록</button>"
			}
		]
	});
	loadQcRegistGrid();
});

// 목록 조회
function loadQcRegistGrid() {

  fetch("/qc/regist/data")
    .then((res) => {
      if (!res.ok) {
        throw new Error("HTTP " + res.status);
      }
      return res.json();
    })
    .then((data) => {
      console.log("QC등록 목록:", data);
      if (qcRegistGrid) {
        qcRegistGrid.resetData(data);
      }
    })
    .catch((err) => {
      console.error("QC등록 데이터 로딩 중 오류", err);
      alert("QC등록 데이터를 불러오는 중 오류가 발생했습니다.");
    });
}