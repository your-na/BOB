document.addEventListener("DOMContentLoaded", () => {
    fetch("/api/chat/rooms", {
        method: "GET",
        credentials: "include" // 세션 기반 인증 시 필수
    })
        .then(res => res.json())
        .then(data => renderChatRooms(data))
        .catch(err => console.error("❌ 채팅방 목록 가져오기 실패:", err));
});


// 우클릭 메뉴 열기
function openContextMenu(event, element) {
    event.preventDefault();
    const menu = document.getElementById("chatContextMenu");
    menu.style.display = "block";
    menu.style.top = `${event.clientY}px`;
    menu.style.left = `${event.clientX}px`;

    // 현재 클릭된 채팅방을 기억
    menu.dataset.chatId = element.dataset.chatId || "example-id";
}

// 메뉴 외 클릭 시 닫기
document.addEventListener("click", () => {
    const menu = document.getElementById("chatContextMenu");
    menu.style.display = "none";
});

function renderChatRooms(rooms) {
    const container = document.querySelector(".chat-container");
    container.innerHTML = ""; // 기존 목록 초기화

    rooms.forEach(room => {
        const roomDiv = document.createElement("div");
        roomDiv.className = "chat-room";
        roomDiv.dataset.chatId = room.roomId;

        // 프로필 이미지
        const profileDiv = document.createElement("div");
        profileDiv.className = "profile";

        let imageUrl = room.opponentProfileUrl || "/images/user.png";

        // 업로드 이미지인 경우 경로 앞에 '/' 붙이기
        if (!imageUrl.startsWith("/")) {
            imageUrl = "/" + imageUrl;
        }

        profileDiv.style.backgroundImage = `url('${imageUrl}')`;


        // 텍스트 정보
        const infoDiv = document.createElement("div");
        infoDiv.className = "info";
        infoDiv.onclick = () => openChatWindow(room.roomId);

        const name = document.createElement("strong");
        name.textContent = room.opponentNick;

        const message = document.createElement("p");
        message.textContent = room.lastMessage;

        infoDiv.appendChild(name);
        infoDiv.appendChild(message);

        roomDiv.appendChild(profileDiv);
        roomDiv.appendChild(infoDiv);
        roomDiv.oncontextmenu = (e) => openContextMenu(e, roomDiv);

        container.appendChild(roomDiv);
    });
}


// 메뉴 항목 처리
function handleChatOption(option) {
    const chatId = document.getElementById("chatContextMenu").dataset.chatId;
    switch (option) {
        case "open":
            openChatWindow(chatId);
            break;
        case "rename":
            alert(`채팅방(${chatId}) 이름 변경`);
            break;
        case "pin":
            fetch(`/api/chat/room/${chatId}/pin`, {
                method: "POST"
            }).then(() => {
                alert("채팅방 고정 상태가 변경되었습니다.");
                location.reload(); // 새로고침해서 순서 반영
            });
            break;

        case "leave":
            alert(`채팅방(${chatId}) 나가기`);
            break;
    }
}

function toggleAddMenu() {
    const menu = document.getElementById("addChatMenu");
    menu.style.display = (menu.style.display === "block") ? "none" : "block";
}

function createNewChat() {
    alert("새 채팅 시작 기능 (백엔드 연동 예정)");
    // 예: location.href = "/chat/new";
}

function createGroupChat() {
    alert("단체 채팅 만들기 기능 (백엔드 연동 예정)");
    // 예: location.href = "/chat/group/create";
}

// 외부 클릭 시 메뉴 닫기
document.addEventListener("click", function(event) {
    const menu = document.getElementById("addChatMenu");
    const btn = document.querySelector(".add-chat-btn");
    if (!menu.contains(event.target) && !btn.contains(event.target)) {
        menu.style.display = "none";
    }
});

function openChatWindow(roomId) {
    if (!roomId) return;
    window.open(`/chat/chatroom?roomId=${roomId}`, "_blank", "width=500,height=700,resizable=yes");
}

