// orgchart.js

$(function () {
  const orgData = {
    name: '대표이사',
    title: 'CEO',
    children: [
      { name: '김개발', title: '개발팀장' },
      {
        name: '박인사', title: '인사팀장',
        children: [
          { name: '이사원', title: '인사담당' },
          { name: '최사원', title: '채용담당' }
        ]
      },
      { name: '정영업', title: '영업팀장' }
    ]
  };

  $('#chart-container').orgchart({
    data: orgData,
    nodeContent: 'title',
	toggleSiblingsResp: false
  });
});