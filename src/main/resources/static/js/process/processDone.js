// processDone.js (완료 처리 탭 전용)
let doneGrid = null;

document.addEventListener("DOMContentLoaded", () => {
  const gridEl = document.getElementById("doneGrid");
  if (!gridEl) return;

  doneGrid = new tui.Grid({
    el: gridEl,
    bodyHeight: 400,
    rowHeaders: ["rowNum"],
    scrollX: true,
    scrollY: true,
    columnOptions: { resizable: true },
    pageOptions: { useClient: true, perPage: 10 },
    columns: [
      { header: "작업지시번호", name: "orderId", width: 150 },
      { header: "라인", name: "lineName" },
      { header: "제품명", name: "prdName" },
      { header: "계획수량", name: "planQty" },
      { header: "양품", name: "goodQty" },
      { header: "불량", name: "defectQty" },
      {
        header: "처리결과",
        name: "status",
        align: "center",
        formatter: ({ value }) => {
          if (value === "COMPLETED") return "<span class='badge bg-success'>완료</span>";
          if (value === "SCRAPPED")  return "<span class='badge bg-danger'>폐기</span>";
          return value || "-";
        }
      },
      { header: "경과시간", name: "elapsedTime" },
      {
        header: " ",
        name: "btn",
        width: 90,
        align: "center",
        formatter: () => "<button type='button' class='btn btn-info btn-sm'>상세</button>"
      }
    ]
  });

  document.getElementById("btnSearchDone")?.addEventListener("click", loadDoneGrid);
  loadDoneGrid();

  doneGrid.on("click", (ev) => {
	if (ev.columnName !== "btn") return;

   	const row = doneGrid.getRow(ev.rowKey);
   	if (!row || !row.orderId) return;

   	openDetailModal(row.orderId);
  });
});

function loadDoneGrid() {
  const workDate = document.getElementById("doneDate")?.value || "";
  const searchKeyword = document.getElementById("doneKeyword")?.value || "";

  const params = new URLSearchParams();
  if (workDate) params.append("workDate", workDate);
  if (searchKeyword) params.append("searchKeyword", searchKeyword);

  fetch("/process/status/done/data?" + params.toString())
    .then(res => {
      if (!res.ok) throw new Error("HTTP " + res.status);
      return res.json();
    })
    .then(data => doneGrid?.resetData(data))
    .catch(err => {
      console.error(err);
      alert("완료 데이터를 불러오는 중 오류가 발생했습니다.");
    });
}
