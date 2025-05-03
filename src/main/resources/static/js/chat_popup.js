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

function openChatWindow(roomId) {
    window.open(`/chatroom?roomId=${roomId}`, "ChatWindow", "width=400,height=600,resizable=yes");
}
