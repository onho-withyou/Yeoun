// orgchart.js

$(function () {
  const orgData = {
	name: 'CEO',
	title: '대표이사',
	className: 'dept-ceo',
	children: [
	    {
	      name: '개발팀장',
	      title: '개발팀장',
	      className: 'dept-dev'
	    },
	    {
	      name: '인사팀장',
	      title: '인사팀장',
	      className: 'dept-hr',
	      children: [
	        {
	          name: '인사담당',
	          title: '인사담당',
	          className: 'dept-hr'
	        },
	        {
	          name: '채용담당',
	          title: '채용담당',
	          className: 'dept-hr'
	        }
	      ]
	    },
	    {
	      name: '영업팀장',
	      title: '영업팀장',
	      className: 'dept-sales'
	    }
	  ]
	};

  $('#chart-container').orgchart({
    data: orgData,
    nodeContent: 'title',
	toggleSiblingsResp: false
  });
});