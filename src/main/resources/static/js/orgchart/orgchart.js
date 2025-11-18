document.addEventListener('DOMContentLoaded', function () {

  const chartConfig = {
    chart: {
      container: "#orgCard",
      nodeAlign: "BOTTOM",
      connectors: { type: "step" },       // 선 모양
      node: { HTMLclass: "node-style" }   // 공통 노드 클래스
    },
    nodeStructure: {
      text: {                       // 노드 안에 들어갈 텍스트
        name: "CEO",
        title: "최고관리자"
      },
      HTMLclass: "dept-ceo",        // 색깔용 클래스
      children: [
        {
          text: {
            name: "ERP이사",
            title: "차현우"
          },
          HTMLclass: "dept-erp",
          children: [
            {
              text: { name: "인사부", title: "강준혁 / 인사부장" },
              HTMLclass: "dept-erp-sub",
              children: [
                { text: { name: "김대리", title: "인사대리" } },
                { text: { name: "박사원", title: "인사담당" } }
              ]
            },
            {
              text: { name: "개발부", title: "채용담당" },
              HTMLclass: "dept-erp-sub",
              children: [
                { text: { name: "최과장", title: "백엔드 개발" } },
                { text: { name: "한주임", title: "프론트엔드" } }
              ]
            }
          ]
        },
        {
          text: {
            name: "MES",
            title: "MES 이사"
          },
          HTMLclass: "dept-mes",
          children: [
            {
              text: { name: "생산부", title: "생산담당" },
              HTMLclass: "dept-mes-sub",
              children: [
                { text: { name: "오반장", title: "라인반장" } },
                { text: { name: "육사원", title: "포장 담당" } }
              ]
            }
          ]
        }
      ]
    }
  };

  new Treant(chartConfig);
});
