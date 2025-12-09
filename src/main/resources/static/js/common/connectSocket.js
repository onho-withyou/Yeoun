/**
 * 
 * 소켓 연결에 사용 - 공통
 * 
 */

// ===============================
//  전역 변수
// ===============================
let stompClient  = null;
let connected = false;

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

function subscribePersonalAlarm() {
	// '/alarm/${Username}' 주소로 개인 구독 주소 설정
    stompClient.subscribe("/user/alarm", message => {
//        const text = message.body;     
        showNewAlarm();
    });
}

function showNewAlarm() {
    const badgeBell = document.getElementById("badge_bell");
    if (!badgeBell) return;

    badgeBell.textContent = "●";
    badgeBell.style.display = "inline-flex";

    // 클릭 시 숨기고, 알림 목록 열기까지 묶고 싶으면
    if (!badgeBell.dataset.bound) {
        const bell = document.getElementById("bell");
        if (bell) {
            bell.addEventListener("click", () => {
                badgeBell.style.display = "none";
                // 여기서 알림 목록 새로고침(기존 /alarm/getAlarm 등) 호출해도 됨
                // notification();
            });
        }
        badgeBell.dataset.bound = "true";
    }
}
// ----------------------------------------------------------------------

//===============================
//	STOMP 연결 설정
//===============================
function connectWebSocket(onConnected) {
	const socket = new SockJS("/websocket");  
	stompClient = Stomp.over(socket);
	getSectionPath()
	stompClient.connect({
		// CSRF 토큰 정보를 헤더에 포함
		[csrfHeader]: csrfToken
	}, function () {
	    connected = true;
	    console.log("STOMP 연결 성공!!!!!!!!!!!!!");
		
		setTimeout(() => {
		    const section = getSectionPath();
		    autoSubscribeByPath(section);
		    subscribePersonalAlarm();
		}, 100);
		
	    if(onConnected) onConnected();
	    
	}, function (error) {
	    connected = false;
	    console.error("❌ STOMP 연결 오류!!!!!!!!!!!:", error);
	
	    // 재연결 시도
	    setTimeout(() => connectWebSocket(onConnected), 3000);
	});
}

