
//===============================
//  소켓 연결 후 방 구독
//===============================

connectWebSocket(() => {
    subscribeEvent();
});

//===============================
//  구독
//===============================

function subscribeEvent() {

    // STOMP 연결 전이면
    if (!connected) {
        console.warn("STOMP가 아직 연결되지않았습니다. 구독 불가.");
        return;
    }

    // 1) 메시지 수신 구독
    stompClient.subscribe(`/user/queue/messenger`, (message) => {
        receiveNewMessage(JSON.parse(message.body));
    });
	
	// 2) 페이지별 알림, 개인 알림 구독
    const section = getSectionPath();
    autoSubscribeByPath(section);
    subscribePersonalAlarm();

    // 3) 전자결재 알림 결재사항 (실시간 처리)
    stompClient.subscribe(`/user/approval`, async (message) => {
        console.log('approval push received:', message);
        // 안전하게 body 파싱
        let payload = null;
        try {
            payload = (typeof message.body === 'string') ? JSON.parse(message.body) : (message.body || message);
        } catch (e) {
            payload = message.body || message;
        }

        // items: 배열 형태로 통일
        let items = [];
        if (Array.isArray(payload)) items = payload;
        else if (payload && Array.isArray(payload.items)) items = payload.items;
        else if (payload && Array.isArray(payload.grid1Data)) items = payload.grid1Data;
        else if (payload) items = [payload];

        // 드롭다운 렌더링: renderApprovalDropdown이 있으면 items로 바로 렌더
        try {
            if (typeof renderApprovalDropdown === 'function') {
                renderApprovalDropdown(items);
            } else {
                // 없으면 전체 재조회 후 렌더 시도
                const fresh = await AllApprovalSearch();
                if (typeof renderApprovalDropdown === 'function') renderApprovalDropdown(fresh);
            }
        } catch (e) {
            console.warn('Failed to render approval dropdown on push, will fallback to AllApprovalSearch', e);
            try { const fresh = await AllApprovalSearch(); if (typeof renderApprovalDropdown === 'function') renderApprovalDropdown(fresh); } catch(_){}
        }

        // 배지 갱신: 서버가 전체 카운트를 보냈다면 우선 사용, 없으면 items.length 사용
        try {
            const badgeEl = document.getElementById('badge_approval');
            let count = null;
            if (payload && (payload.count || payload.totalCount || payload.approvalCount)) {
                count = Number(payload.count || payload.totalCount || payload.approvalCount);
            } else if (Array.isArray(items)) {
                count = items.length;
            }
            if (count !== null && !isNaN(count)) {
                if (badgeEl) {
                    if (count > 0) {
                        badgeEl.style.display = 'unset';
                        badgeEl.textContent = count >= 30 ? '30+' : count;
                    } else {
                        badgeEl.style.display = 'none';
                    }
                }
            } else {
                // fallback: DOM 기반으로 동기화
                if (typeof updatePaymentBadgeFromDOM === 'function') updatePaymentBadgeFromDOM();
            }
        } catch (e) {
            console.error('Error updating badge on approval push', e);
        }
    });

	
}


//===============================
//	메시지 수신 이벤트
//===============================
const chatBadge = document.querySelector("#badge_chat");
function receiveNewMessage(req){
	chatBadge.style.display = "flex";
}


//===============================
// 클릭 시 사라지는 이벤트 공통으로 빼기
//===============================
function hideElement(el) {
  if (!el) return;
  el.style.display = "none";
}

chatBadge.addEventListener("click", () => {
  console.log("클릭");
  hideElement(badge);
});

// ====================================================================

// -----------------------------------------------------------
// 현재 URL의 상위주소 받아오기
function getSectionPath() {
    const path  = window.location.pathname;
    const parts = path.split("/").filter(Boolean);
    if (parts.length === 0) return null;
    return parts[0];
}

//  URL의 상위주소 받아 공통 알림 채널 구독
function autoSubscribeByPath(sectionPath) {
    if (!sectionPath) return;

    stompClient.subscribe(`/alarm/${sectionPath}`, message => {
//        const payload = JSON.parse(message.body);
        const payload = message.body
        showRefreshBadge(payload);  // 섹션 공통 새로고침 뱃지 표시 함수
    });
}


function showRefreshBadge(payload) {
    // 레이아웃에있는 공통 뱃지 요소
    const badge = document.getElementById("refresh-badge");
    if (!badge) {
        console.warn("inventory-refresh-badge 요소가 없습니다.");
        return;
    }
	// 뱃지에 웹소켓으로 부터 받은 메세지를 표현할 스판요소
	const textSpan = badge.querySelector(".badge-text");
	if (textSpan) {
	    textSpan.textContent = payload || "데이터가 변경되었습니다. 새로고침 해주세요.";
	}
	// 뱃지보이기
    badge.style.display = "inline-flex";
	
	// 뱃지에 data-bound가 없을경우만이벤트 등록
	if (!badge.dataset.bound) {
		const refreshBtn = badge.querySelector(".badge-refresh-btn");
		const closeBtn   = badge.querySelector(".badge-close-btn");

		if (refreshBtn) {
			refreshBtn.addEventListener("click", () => {
				location.reload();
			});
	  	}
	
		if (closeBtn) {
			closeBtn.addEventListener("click", () => {
				badge.style.display = "none";
			});
		}
		// 뱃지에 data-bound = true 생성(이벤트중복생성방지)
	    badge.dataset.bound = "true";
	}
}

// 개인 알림이 도착했을 때 종에 표시, 드랍다운 알림에 new 표시
function subscribePersonalAlarm() {
	// '/alarm/${Username}' 주소로 개인 구독 주소 설정
    stompClient.subscribe("/user/alarm", message => {
//        const text = message.body;     
        showNewAlarm();
		showNewAlarmAtDropdown();
    });
}

// 헤더 종모양에 뱃지표시
function showNewAlarm() {
    const badgeBell = document.getElementById("badge_bell");
    if (!badgeBell) return;

//    badgeBell.textContent = "●";
    badgeBell.style.display = "inline-flex";

    // 클릭 시 숨기고, 알림 목록 열기까지 묶고 싶으면
    if (!badgeBell.dataset.bound) {
        const bell = document.getElementById("bell");
        if (bell) {
            bell.addEventListener("click", () => {
                badgeBell.style.display = "none";
            });
        }
        badgeBell.dataset.bound = "true";
    }
}
// 종클릭시 나오는 드랍다운에 new 표시
function showNewAlarmAtDropdown() {
	const alarmNewBadge = document.querySelector(".alarm-new-badge");
	alarmNewBadge.style.display = "inline";
}


// 읽지않은 알림 데이터 가져오기
async function getAlarmReadStatus() {
    try {
        const response = await fetch(`/alarm/status`, {
            method: "GET",
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error("알림 데이터 로드 실패");
        }
        
        const alarmData = await response.json();
        console.log("알림 데이터:", alarmData);
        
        return alarmData;
        
    } catch (error) {
        console.error("알림 조회 오류:", error);
        alert("알림 목록을 불러오지 못했습니다.");
    }
}
// 페이지 로드시 안읽은 알림이 있는경우 알림뱃지 설정
(async () => {
	const alarmData = await getAlarmReadStatus();
	console.log('init alarmData:', alarmData, alarmData.length);
    if(alarmData.length != 0) {
		showNewAlarm();
		showNewAlarmAtDropdown();
	}
})();
// ----------------------------------------------------------------------
//전자결재 알림
(async () => {
    const alarmData = await AllApprovalSearch();
    console.log('AllApprovalSearch:', alarmData, alarmData && alarmData.length);

    // badge_approval에 개수 표시
    try {
        const badgeApprovalEl = document.getElementById('badge_approval');
        const count = Array.isArray(alarmData) ? alarmData.length : (alarmData && alarmData.length ? alarmData.length : 0);
        if (badgeApprovalEl) {
            if (count > 0) {
                badgeApprovalEl.style.display = 'unset';
                badgeApprovalEl.textContent = count >= 30 ? '30+' : count;
            } else {
                badgeApprovalEl.style.display = 'none';
            }
        }
    } catch (e) {
        console.error('badge_approval update error', e);
    }

    // 렌더링 함수가 정의되어 있으면 동일한 방식으로 드롭다운에 항목 채우기
    try {
        if (typeof renderApprovalDropdown === 'function') {
            renderApprovalDropdown(alarmData || []);
        }
    } catch (e) {
        console.warn('renderApprovalDropdown 호출 실패', e);
    }
})();


//결재사항
async function AllApprovalSearch() {
    const params = {
        createDate: "",
        finishDate: "",
        empName: "",
        approvalTitle: ""
    };

    try {
        const res = await fetch('/approval/searchAllGrids', {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(params)
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const data = await res.json();
        return data && data.grid1Data ? data.grid1Data : [];
    } catch (err) {
        console.error("결재 목록 조회오류", err);
        return [];
    }
}

// 렌더링: 드롭다운에 결재 항목 넣기
function renderApprovalDropdown(items) {
	if (!paymentDropdown) return;
	// 기본 틀: 첫 두 항목(헤더/구분선)은 고정으로 남기고, 나머지는 동적으로 삽입
	const maxItems = 5;
	const toShow = Array.isArray(items) ? items.slice(0, maxItems) : [];

	// 빌드할 HTML
	let html = '';
	if (toShow.length === 0) {
		html = '<li class="dropdown-placeholder"><a class="dropdown-item" href="#">결재 내역이 없습니다.</a></li>';
	} else {
		toShow.forEach((it, idx) => {
			const title = it.approval_title || it.approvalTitle || it.title || '';
			const displayTitle = truncateText(title, 25);
			const id = it.id || it.approvalId || '';
			const href = id ? (`/approval/view/${id}`) : '#';
			html += `<li><a class="dropdown-item" href="${href}">${escapeHtml(displayTitle)}</a></li>`;
		});
		if (items.length > maxItems) {
			html += '<li><hr class="dropdown-divider"></li>';
			html += '<li><a class="dropdown-item" href="/approval/approval_doc">더보기</a></li>';
		}
	}

	// 바꾸기
	paymentDropdown.innerHTML = html;
}

// 간단한 HTML 이스케이프
function escapeHtml(str) {
	if (!str) return '';
	return String(str)
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;')
		.replace(/'/g, '&#39;');
}

// 문자열을 안전하게 자르기 (유니코드 보존)
function truncateText(str, maxLen) {
	if (!str) return '';
	const arr = [...String(str)];
	if (arr.length <= maxLen) return arr.join('');
	return arr.slice(0, maxLen).join('') + '...';
}