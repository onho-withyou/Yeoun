
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
}