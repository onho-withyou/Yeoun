
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
	
	// 2)
	
}


//===============================
//	메시지 수신 이벤트
//===============================
function receiveNewMessage(req){
	const chatBadge = document.querySelector("#badge_chat");
	chatBadge.style.display = "flex";
}


//===============================
// 클릭 시 사라지는 이벤트 공통으로 빼기
//===============================
function hideElement(el) {
  if (!el) return;
  el.style.display = "none";
}

chatsPanel.addEventListener("click", (e) => {
  const item = document.querySelector("#badge_chat");
  if (!item) return;

  hideElement(badge);
});
