// orgchart.js

$(function () {
	const orgData = {
	  name: 'CEO',
	  title: '최고관리자',
	  className: 'dept-ceo',
	  children: [
	    // ───────── ERP 라인 ─────────
	    {
	      name: 'ERP',
	      title: '홍길동 / ERP 이사',
	      className: 'dept-erp',
	      children: [
	        {
	          name: '인사부',
	          title: '이순신 / 인사부장',
	          className: 'dept-hr',
	          children: [
	            { // 여기서부터는 "직급별 팀원"
	              name: '김대리',
	              title: '인사대리',
	              className: 'rank-staff'
	            },
	            {
	              name: '박사원',
	              title: '인사담당',
	              className: 'rank-staff'
	            }
	          ]
	        },
	        {
	          name: '개발부',
	          title: '채용담당',
	          className: 'dept-dev',
	          children: [
	            {
	              name: '최과장',
	              title: '백엔드 개발',
	              className: 'rank-staff'
	            },
	            {
	              name: '한주임',
	              title: '프론트엔드',
	              className: 'rank-staff'
	            }
	          ]
	        }
	      ]
	    },

	    // ───────── MES 라인 ─────────
	    {
	      name: 'MES',
	      title: 'MES 이사',
	      className: 'dept-mes',
	      children: [
	        {
	          name: '생산부',
	          title: '생산담당',
	          className: 'dept-prod',
	          children: [
	            {
	              name: '오반장',
	              title: '라인반장',
	              className: 'rank-staff'
	            },
	            {
	              name: '육사원',
	              title: '포장 담당',
	              className: 'rank-staff'
	            }
	          ]
	        }
	      ]
	    }
	  ]
	};


  $('#chart-container').orgchart({
    data: orgData,
    nodeContent: 'title',
	toggleSiblingsResp: false
  });
});