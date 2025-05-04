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

// 메뉴 항목 처리
function handleChatOption(option) {
    const chatId = document.getElementById("chatContextMenu").dataset.chatId;
    switch (option) {
        case "open":
            alert(`채팅방(${chatId}) 열기`);
            break;
        case "rename":
            alert(`채팅방(${chatId}) 이름 변경`);
            break;
        case "pin":
            alert(`채팅방(${chatId}) 상단 고정`);
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
    window.open(`/chatroom?roomId=${roomId}`, "ChatWindow", "width=400,height=600,resizable=yes");
}
