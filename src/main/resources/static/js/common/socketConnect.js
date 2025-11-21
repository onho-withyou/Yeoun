/**
 * 
 * 소켓 연결에 사용 - 공통
 * 
 */

// ===============================
//  전역 변수
// ===============================
let stompClient = null;
let connected   = false;


//===============================
//	STOMP 연결 설정
//===============================
function connectWebSocket(onConnectedCallback) {
	const socket = new SockJS("/websocket");  
	stompClient = Stomp.over(socket);

	stompClient.connect({
		// CSRF 토큰 정보를 헤더에 포함
		"[[${_csrf.headerName}]]" : "[[${_csrf.token}]]"
	}, function () {
	    connected = true;
	    console.log("STOMP 연결 성공!!!!!!!!!!!!!");
	    
	    if(onConnectedCallback) onConnectedCallback();
	    
	}, function (error) {
	    connected = false;
	    console.error("❌ STOMP 연결 오류!!!!!!!!!!!:", error);
	
	    // 재연결 시도
	    setTimeout(() => connectWebSocket(onConnectedCallback), 3000);
	});
}
