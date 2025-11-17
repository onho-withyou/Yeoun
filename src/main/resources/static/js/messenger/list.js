/**
 *  messenger/list 파일의 js
 */

// ==========================
// DOM 요소 참조 / 전역 상태
// ==========================
const tabFriends		= document.getElementById('tab-friends'); // 친구목록 탭
const tabChats 		= document.getElementById('tab-chats'); // 대화목록 탭
const friendsPanel 	= document.getElementById('friends-panel'); // 친구패널
const chatsPanel 		= document.getElementById('chats-panel'); // 대화패널
const headerTitle 		= document.getElementById('header-title'); // 헤더(친구목록-대화목록 텍스트 전환)

const searchInput		    = document.querySelector('.chat-search input'); // 검색창
const searchButton		    = document.querySelector('.chat-search span'); // 검색버튼

const statusIndicator	= document.getElementById('status-indicator');	// 내 상태
const statusText		= document.getElementById('status-text'); // 내 상태 텍스트
const workStatus		= document.getElementById('work-status'); // 수동 근무 상태


//현재 모드: 'friend' / 'chat'
let currentMode = 'friend';



// ==========================
// 각 친구의 상태
// ==========================
document.querySelectorAll('.friend-item').forEach(item => {
  const dot = item.querySelector('.status-dot'); // 친구 상태를 나타내는 점
  const status = dot.dataset.status; // friend.status 값 ("ONLINE","OFFLINE","AWAY","BUSY")

  switch (status) {
    case "ONLINE":  // 온라인
      dot.classList.add("online");
      break;

    case "OFFLINE": // 오프라인
      dot.classList.add("offline");
      break;

    case "AWAY":    // 자리비움
      dot.classList.add("away");
      break;

    case "BUSY":    // 다른 용무중
      dot.classList.add("busy");
      break;

    default:		// 디폴트 = 오프라인
      dot.classList.add("offline");
  }
});

// ==========================
// 검색 로직
// ==========================
	
// 친구 목록 필터링 (이름 및 부서)
function filterFriends()  {
    const keyword = searchInput.value.trim().toLowerCase();
    const items = document.querySelectorAll('.friend-item');

    items.forEach(item => {
      const name = item.querySelector('p').textContent.toLowerCase();
      const dept = item.querySelector('small').textContent.toLowerCase();
      
      const visible = name.includes(keyword) || dept.includes(keyword);
      item.style.display = visible ? 'flex' : 'none';
  });
}

// 대화 목록 필터링 (대화 상세내용)
function filterChats() {
    const keyword = searchInput.value.trim().toLowerCase();
    const items = document.querySelectorAll('.chat-item');
    
    items.forEach(item => {
        const name = item.querySelector('p').textContent.toLowerCase();
        const preview = item.querySelector('small').textContent.toLowerCase();
        
        const visible = name.includes(keyword) || preview.includes(keyword);
        item.style.display = visible ? 'flex' : 'none';
    });
}


// 입력 변화 시: 친구 모드일 때만 실시간 필터링
searchInput.addEventListener('input', () => {
	if (currentMode === 'friend'){
		filterFriends();
	}
});

// 키보드로 검색 실행: 대화 모드에서만 Enter로 검색
searchInput.addEventListener('keydown', (event) => {
	if (currentMode === 'chat' && event.key === 'Enter') {
		filterChats();
	}
});

// 돋보기 클릭: 대화 모드일 때만 검색
searchButton.addEventListener('click', () => {
	if (currentMode === 'chat') {
 		filterChats();
 	}
 });

// ==========================
// 탭 전환
// ==========================

function activeFriendsTab() {
	currentMode = 'friend';
	
	tabFriends.classList.add('active');
	tabChats.classList.remove('active');
	
	friendsPanel.classList.remove('d-none');
	chatsPanel.classList.add('d-none');
	
	headerTitle.textContent = '친구 목록';
	
	// 검색창 초기화
	searchInput.value = '';
	searchInput.placeholder = '이름/부서로 검색';
	
	// 친구가 다시 보이도록 함
	document.querySelectorAll('.friend-item').forEach(item => {
		item.style.display = 'flex';
	});
}

function activeChatsTab() {
	currentMode = 'chat';
	
	tabChats.classList.add('active');
	tabFriends.classList.remove('active');
	
	chatsPanel.classList.remove('d-none');
	friendsPanel.classList.add('d-none');
	
	headerTitle.textContent = '대화 목록';
	
	// 검색창 초기화
	searchInput.value = '';
	searchInput.placeholder = '대화 상대/내용으로 검색';
	
	// 대화가 다시 보이도록 함
	document.querySelectorAll('.chat-item').forEach(item => {
		item.style.display = 'flex';
	});
}
	
// 친구 탭 클릭
tabFriends.addEventListener('click', () => {
  activeFriendsTab();
  console.log("친구목록!!!");
});

// 대화 탭 클릭
tabChats.addEventListener('click', () => {
  activeChatsTab();
  console.log("대화목록!!!");
});


// ==========================
// 더블클릭 시 채팅창 팝업
// ==========================
document.querySelectorAll('.friend-item, .chat-item').forEach(item => {
  item.addEventListener('dblclick', () => {
    const name = item.querySelector('p').textContent;
    const targetId = item.dataset.id;
    window.open(
      'target/' + targetId,
      '_blank',
      'width=500,height=700,resizable=no,scrollbars=no');
  });
});


// ==========================
// 상태 토글
// ==========================

let statuses = [
    { color: '#4CAF50', text: '온라인' },
    { color: '#FFC107', text: '자리비움' },
    { color: '#F44336', text: '다른 용무중' }
];

let current = 0;
let manuallySet = false;  // 수동 상태 변경 여부

// =====================
// 수동 상태 변경
// =====================
statusIndicator.addEventListener('click', () => {
    manuallySet = true; // 자동 변경 잠시 차단

    current = (current + 1) % statuses.length;
    statusIndicator.style.backgroundColor = statuses[current].color;
    statusText.textContent = statuses[current].text;
});


// =====================
// 자동 자리비움
// =====================
let idleTime = 0;
let autoAwayTime = 30 * 1000; // 30초

setInterval(() => {
    idleTime += 1000;

    // 수동 상태일 때는 자동 자리비움 X
    if (manuallySet) return;

    if (idleTime >= autoAwayTime) {
        statusIndicator.style.backgroundColor = '#FFC107';
        statusText.textContent = '자리비움';
    }
}, 1000);


// =====================
// 사용자 활동 감지 → 자동 ONLINE
// =====================
['mousemove', 'keydown', 'click', 'scroll'].forEach(evt => {
    document.addEventListener(evt, () => {
        idleTime = 0;

        // 수동 상태면 자동복귀 금지
        if (manuallySet) return;

        statusIndicator.style.backgroundColor = '#4CAF50';
        statusText.textContent = '온라인';
    });
});



// ==========================
// 즐겨찾기 핸들러
// ==========================
document.addEventListener("click", (event) => {
  if (!event.target.classList.contains("star-btn")) return;
  
  event.preventDefault();   // GET 방지
  event.stopPropagation();  // 버블링 방지
  
  const starBtn = event.target;
  const friendItem = event.target.closest(".friend-item");
  const container = document.querySelector("#friends-panel");
  
  const wasActive = starBtn.classList.contains("active");	// 토글 전 상태

  // 1. 별 토글 
  starBtn.classList.toggle("active");
  starBtn.classList.toggle("bi-star");
  starBtn.classList.toggle("bi-star-fill");
  
  const isNowActive = starBtn.classList.contains("active");	// 토글 후 상태
  
  // 2. 위치 이동 로직
  
	//==========================
	// 즐겨찾기 ON -> 맨 위로 이동
	//==========================
  if (!wasActive && isNowActive) {
	container.prepend(friendItem);
  } 
 
	//==========================
	// 즐겨찾기 OFF
	//==========================
  else if (wasActive && !isNowActive) {
	  const allItems = Array.from(container.querySelectorAll(".friend-item"));
	  const normalFriends = allItems.filter(item => 
	  	!item.querySelector(".star-btn").classList.contains("active")
	  );

	  normalFriends.sort((a, b) => {
		  const nameA = a.querySelector("p").textContent.trim();
		  const nameB = b.querySelector("p").textContent.trim();
		  return nameA.localeCompare(nameB, "ko");
	  });
	  
	  normalFriends.forEach(item => container.appendChild(item));
  }

  // 3. 서버에 즐겨찾기 상태 전송
  
  const id = starBtn.dataset.id;
  fetch(`/messenger/favorite/${id}`, {
    method: "PATCH",
    headers: {
    	[csrfHeaderName] : csrfToken
    },
    credentials: "include"
  })
  .then(res => res.text())
  .then(msg => {
      console.log("서버 응답", msg);
  })
  .catch(err => {
      console.error("즐겨찾기 오류:", err);
      // 실패 시 UI 롤백 가능
      starBtn.classList.toggle("active");
      starBtn.classList.toggle("bi-star");
      starBtn.classList.toggle("bi-star-fill");
  });

});


// ==========================
// 초기 진입 시 : 친구 탭 활성화
// ==========================
activeFriendsTab();

