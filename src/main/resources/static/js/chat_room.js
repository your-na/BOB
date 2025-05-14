function goBack() {
    window.location.href = "/chatting"; // 또는 location.href = "/chatlist"; 처럼 경로 지정도 가능
}

function getRoomIdFromURL() {
    const params = new URLSearchParams(window.location.search);
    return params.get("roomId");
}

document.addEventListener("DOMContentLoaded", function () {
    const chatType = document.querySelector("meta[name='chat-type']")?.content || "private";

    const topicPrefix = chatType === "group" ? "/topic/grouproom." : "/topic/room.";
    const sendPrefix = chatType === "group" ? "/app/groupchat.send/" : "/app/chat.send/";

    const opponentNick = chatType === "group"
        ? document.querySelector("meta[name='room-name']")?.content || "그룹채팅"
        : document.querySelector("meta[name='opponent-nick']")?.content || "상대";

    let rawOpponentUrl = document.querySelector("meta[name='opponent-profile-url']")?.content || "/images/user.png";

    if (!rawOpponentUrl.startsWith("/")) {
        rawOpponentUrl = "/" + rawOpponentUrl;
    }

    // ✅ encodeURI 제거
    const opponentProfileUrl = rawOpponentUrl;


    console.log("🔥 최종 상대 프로필 URL:", opponentProfileUrl);

    const currentUserNick = document.querySelector("meta[name='current-user']").content.trim();
    const currentUserId = parseInt(document.querySelector("meta[name='current-user-id']").content);

    document.getElementById("chat-partner-name").textContent = opponentNick;

    const input = document.getElementById("chat-input");
    const chatBox = document.querySelector('.chat-box');
    const sendBtn = document.querySelector(".send-button");
    chatBox.scrollTop = chatBox.scrollHeight;

    const roomId = getRoomIdFromURL();
    const socket = new SockJS("/ws-chat");
    const stompClient = Stomp.over(socket);

    loadMessages(roomId);

    stompClient.connect({}, () => {
        stompClient.subscribe(`${topicPrefix}${roomId}`, (msg) => {
            const payload = JSON.parse(msg.body);
            const isMine = parseInt(payload.senderId) === currentUserId;
            const type = isMine ? "user" : "partner";
            const sender = chatType === "group" ? payload.senderId : payload.senderName;
            appendMessage(type, sender, payload.message);
        });
    });

    sendBtn.addEventListener("click", sendMessage);
    input.addEventListener("keydown", function (e) {
        if (e.key === "Enter") sendMessage();
    });

    // 메세지 추가 함수
    function appendMessage(type, senderIdOrName, text) {
        const messageRow = document.createElement("div");
        messageRow.className = `message-row ${type}`;
        const messageContent = document.createElement("div");
        messageContent.className = "message-content";
        const messageBubble = document.createElement("div");
        messageBubble.className = `message ${type}`;
        messageBubble.textContent = text;

        if (type === "user") {
            messageContent.appendChild(messageBubble);
            messageRow.appendChild(messageContent);
        } else {
            let profileImg = document.createElement("img");
            profileImg.className = "profile-image";

            let nicknameSpan = document.createElement("span");
            nicknameSpan.className = "nickname";

            if (chatType === "group") {
                const key = String(senderIdOrName);
                const senderInfo = userMap?.[key] || { nick: `유저#${key}`, image: "/images/user.png" };
                profileImg.src = senderInfo.image;
                nicknameSpan.textContent = senderInfo.nick;
            } else {
                // 단일 채팅은 senderIdOrName이 nickname 자체임
                profileImg.src = opponentProfileUrl;
                nicknameSpan.textContent = senderIdOrName;
            }

            profileImg.alt = "프로필";
            messageContent.appendChild(nicknameSpan);
            messageContent.appendChild(messageBubble);
            messageRow.appendChild(profileImg);
            messageRow.appendChild(messageContent);
        }

        document.querySelector(".chat-box").appendChild(messageRow);
        document.querySelector(".chat-box").scrollTop = chatBox.scrollHeight;
    }

    // ✅ 메시지 불러오기 부분 수정
    function loadMessages(roomId) {
        const endpoint = chatType === "group"
            ? `/group/messages?roomId=${roomId}`
            : `/chat/messages?roomId=${roomId}`;

        fetch(endpoint)
            .then(response => response.json())
            .then(messages => {
                console.log("📨 메시지 리스트:", messages);
                messages.forEach(msg => {
                    const isMine = parseInt(msg.senderId) === currentUserId;
                    const type = isMine ? "user" : "partner";
                    const sender = chatType === "group" ? msg.senderId : msg.senderName;
                    appendMessage(type, msg.senderId, msg.message);
                });
            });
    }

    function sendMessage() {
        const text = input.value.trim();
        if (text === "") return;

        const payload = {
            message: text
        };

        // ✅ 그룹 채팅일 경우에만 sender 정보 추가
        if (chatType === "group") {
            payload.senderId = currentUserId;
            payload.senderName = currentUserNick;
        }

        stompClient.send(`${sendPrefix}${roomId}`, {}, JSON.stringify(payload));
        input.value = "";
    }




    window.receiveMessage = function (sender, text) {
        appendMessage("partner", sender, text);
    };
});

// ✅ 이 코드는 DOMContentLoaded 바깥에 존재해야 합니다
function toggleAttachMenu() {
    const menu = document.getElementById('attachMenu');
    menu.style.display = menu.style.display === 'flex' ? 'none' : 'flex';
}

// 파일 선택 처리도 밖에 정의해야 HTML에서 사용할 수 있어요
function handleFileSelect(event, type) {
    const file = event.target.files[0];
    if (!file) return;

    if (type === 'image') {
        alert(`📷 선택된 이미지: ${file.name}`);
    } else {
        alert(`📁 선택된 파일: ${file.name}`);
    }

    document.getElementById('attachMenu').style.display = 'none';
}

function toggleChatMenu() {
    const menu = document.getElementById("chatDropdownMenu");
    menu.style.display = menu.style.display === "block" ? "none" : "block";
}

// 바깥 클릭 시 메뉴 닫기
document.addEventListener("click", function (event) {
    const menu = document.getElementById("chatDropdownMenu");
    const button = document.querySelector(".chat-menu-btn");

    if (!menu.contains(event.target) && !button.contains(event.target)) {
        menu.style.display = "none";
    }
});

// ✅ 초대 모달 열기 함수
function inviteUsers() {
    document.getElementById("inviteModal").style.display = "block";
}

// ✅ 초대 모달 닫기
function closeInviteModal() {
    document.getElementById("inviteModal").style.display = "none";
}

// ✅ 나가기 모달 열기 함수
function openLeaveModal() {
    const opponentNick = document.querySelector("meta[name='opponent-nick']")?.content || "상대";
    let profileUrl = document.querySelector("meta[name='opponent-profile-url']")?.content || "/images/user.png";
    if (!profileUrl.startsWith("/")) profileUrl = "/" + profileUrl;

    document.getElementById("leaveNickname").textContent = opponentNick;
    document.getElementById("leaveProfileImage").src = profileUrl;

    document.getElementById("leaveModal").style.display = "flex";
}

// ✅ 나가기 모달 닫기
function closeLeaveModal() {
    document.getElementById("leaveModal").style.display = "none";
}

