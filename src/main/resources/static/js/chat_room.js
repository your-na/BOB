function goBack() {
    window.location.href = "/chatting"; // 또는 location.href = "/chatlist"; 처럼 경로 지정도 가능
}

function getRoomIdFromURL() {
    const params = new URLSearchParams(window.location.search);
    return params.get("roomId");
}

document.addEventListener("DOMContentLoaded", function () {
    const currentUserNick = document.querySelector("meta[name='current-user']").content.trim();
    const opponentNick = document.querySelector("meta[name='opponent-user']").content;
    const currentUserId = parseInt(document.querySelector("meta[name='current-user-id']").content);

    document.getElementById("chat-partner-name").textContent = opponentNick;

    const input = document.getElementById("chat-input");
    const chatBox = document.querySelector('.chat-box');
    const sendBtn = document.querySelector(".send-button");
    chatBox.scrollTop = chatBox.scrollHeight;

    const roomId = getRoomIdFromURL();
    const socket = new SockJS("/ws-chat");
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        stompClient.subscribe(`/topic/room.${roomId}`, (msg) => {
            const payload = JSON.parse(msg.body);
            console.log("[수신]", payload);
            const isMine = parseInt(payload.senderId) === currentUserId;
            const type = isMine ? "user" : "partner";
            appendMessage(type, payload.senderName, payload.message);
        });

        loadMessages(roomId);
    });

    sendBtn.addEventListener("click", sendMessage);
    input.addEventListener("keydown", function (e) {
        if (e.key === "Enter") sendMessage();
    });

    // 메세지 추가 함수
    function appendMessage(type, sender, text) {
        const message = document.createElement("div");
        message.classList.add("message", type);
        message.textContent = text;
        chatBox.appendChild(message);
        chatBox.scrollTop = chatBox.scrollHeight;
    }


    function loadMessages(roomId) {
        fetch(`/chat/messages?roomId=${roomId}`)
            .then(response => response.json())
            .then(messages => {
                messages.forEach(msg => {
                    const isMine = parseInt(msg.senderId) === currentUserId;
                    const type = isMine ? "user" : "partner";
                    appendMessage(type, msg.senderName, msg.message);
                });
            });
    }

    function sendMessage() {
        const text = input.value.trim();
        if (text !== "") {
            stompClient.send(`/app/chat.send/${roomId}`, {}, JSON.stringify({
                roomId: roomId,
                message: text
            }));
            input.value = "";
        }
    }


    window.receiveMessage = function (sender, text) {
        appendMessage("partner", sender, text);
    };
});
