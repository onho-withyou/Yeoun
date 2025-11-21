/**
 * [ 웹소켓 설정 ]
 */

// ===============================
//  전역 변수
// ===============================

let roomId			= window.roomId		 	?? null;	
let senderId 		= window.senderId	 	?? null;	// 로그인 사용자 ID
let targetEpId  	= window.targetEmpId 	?? null;
let csrfToken		= window.csrfToken	 	?? '';
let csrfHeaderName  = window.csrfHeaderName ?? '';

if (!senderId) {
	console.warn("senderId가 설정되지 않았습니다. chat.html에서 window.senderId 설정 필요")
}

//===============================
//  방 구독
//===============================
function subscribeRoom(roomId) {
	if (!stompClient || !connected) {
		console.warn("STOMP가 아직 연결되지않았습니다. subscribeRoom 지연.");
		return;
	}
	if (!roomId) {
		console.warn("roomId가 없습니다. 구독 불가.");
	}
	
	stompClient.subscribe(`/topic/chat/room/${roomId}`, function (message) {
	    const msg = JSON.parse(message.body);
	    renderReceivedMessage(msg);
	});
	
	console.log("방 구독 완료! : ", roomId);
}

//===============================
//  내 메시지 렌더링
//===============================
function renderMyMessage(text) {
    const chatBody = document.getElementById("chat-body");
    if (!chatBody) return;

    const row = document.createElement("div");
    row.className = "msg-row right";

    row.innerHTML = `
        <div class="msg-meta">
            <span>${formatTimeFull()}</span>
            <i class="bi bi-check2 read-check"></i>
        </div>
        <div class="msg-bubble msg-right">${text}</div>
    `;

    chatBody.appendChild(row);
    chatBody.scrollTop = chatBody.scrollHeight;
    
    // 나중에 여기서 읽음표시 업데이트
}

//===============================
//  메시지 전송
//===============================
function sendChatMessage() {
	const input = document.getElementById("message");
	console.log("input : " , input);
	if (!input) return;
	
	const text = input.value.trim();
	console.log("text : " , text);
	if (!text) return;
	
	// 1) 신규 방인 경우, 먼저 방 생성하기
	if (!roomId) {
		if (!targetEmpId) {
			console.error("targetEmpId가 없습니다. 신규 방 생성 불가.");
			return;
		}		// ====================> 여기서부터 다시 작성하기
	}
	
	
	// 내 메시지 화면에 즉시 표시
	renderMyMessage(text);
	
	// 소켓에 보낼 페이로드
	const payload = {
	    roomId: roomId,
	    senderId: senderId,
	    message: text
	};
	
	// 소켓 전송
	stompClient.send("/app/chat/send", {}, JSON.stringify(payload));
	
	// 입력창 비우기
	input.value = "";
}

//===============================
//  받은 메시지 DOM 렌더링
//===============================
function renderReceivedMessage(dto) {
if (dto.senderId === senderId) return;
const chatBody = document.getElementById("chat-body");

const row = document.createElement("div");
row.className = "msg-row left";

    row.innerHTML = `
        <img th:src="|@{/img/msg_img_}${dto.msgProfile}.png|" class="profile-img">
        <div class="msg-main">
            <div class="sender-name">${dto.senderName}</div>
            <div class="msg-bubble msg-left">${dto.msgContent}</div>
        </div>
        <div class="msg-time msg-time-left">${dto.sentDateFormatted}</div>
    `;

	chatBody.appendChild(row);
	chatBody.scrollTop = chatBody.scrollHeight;
}

//===============================
//  시간 표시 변환
//===============================
function formatTime(datetimeString) {
	const d = new Date(datetimeString);
	const h = d.getHours();
	const m = d.getMinutes().toString().padStart(2, '0');
	const ampm = h >= 12 ? '오후' : '오전';
	const hh = h % 12 || 12;
	return `${ampm} ${hh}:${m}`;
}

//===============================
//  이벤트 바인딩
//===============================
document.getElementById("send").addEventListener("click", sendChatMessage);

document.getElementById("message").addEventListener("keydown", function (e) {
 if (e.key === "Enter" && !e.shiftKey) {
     e.preventDefault();
     sendChatMessage();
 }
});

//===============================
//  첫 연결 시작
//===============================
connectChat();
