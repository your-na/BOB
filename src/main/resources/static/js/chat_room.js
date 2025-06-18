let stompClient;
let selectedFile = null;
let selectedFileType = null;

const chatType = document.querySelector("meta[name='chat-type']")?.content || "private";
const topicPrefix = chatType === "group" ? "/topic/grouproom." : "/topic/room.";
const sendPrefix = chatType === "group" ? "/app/groupchat.send/" : "/app/chat.send/";
const currentUserNick = document.querySelector("meta[name='current-user']").content.trim();
const currentUserId = parseInt(document.querySelector("meta[name='current-user-id']").content);

const opponentNick = chatType === "group"
    ? document.querySelector("meta[name='room-name']")?.content || "그룹채팅"
    : document.querySelector("meta[name='opponent-nick']")?.content || "상대";

const opponentProfileUrl = "/" + (document.querySelector("meta[name='opponent-profile-url']")?.content || "/images/user.png").replace(/^\/?/, "");


document.addEventListener("DOMContentLoaded", function () {

    document.getElementById("chat-partner-name").textContent = opponentNick;
    const input = document.getElementById("chat-input");
    const chatBox = document.querySelector('.chat-box');
    const sendBtn = document.querySelector(".send-button");
    const roomId = new URLSearchParams(window.location.search).get("roomId");
    const socket = new SockJS("/ws-chat");

    stompClient = Stomp.over(socket);
    loadMessages(roomId);

    stompClient.connect({}, () => {
        stompClient.subscribe(`${topicPrefix}${roomId}`, (msg) => {
            const payload = JSON.parse(msg.body);
            const isMine = parseInt(payload.senderId) === currentUserId;
            const type = isMine ? "user" : "partner";
            const sender = chatType === "group" ? payload.senderId : payload.senderName;

            let displayText = payload.message || "";
            if (payload.type === "image") displayText = "[image]";
            else if (payload.type === "file") displayText = "[file]";

            appendMessage(type, sender, displayText, payload.fileUrl, payload.fileName);
        });
    });

    sendBtn.addEventListener("click", sendMessage);
    input.addEventListener("keydown", function (e) {
        if (e.key === "Enter") sendMessage();
    });

    function appendMessage(type, senderIdOrName, text, fileUrl = null, fileName = null) {
        const messageRow = document.createElement("div");
        messageRow.className = `message-row ${type}`;
        const messageContent = document.createElement("div");
        messageContent.className = "message-content";
        const messageBubble = document.createElement("div");
        messageBubble.className = `message ${type}`;

        if (text === "[image]" && fileUrl) {
            const img = document.createElement("img");
            img.src = fileUrl;
            img.alt = "전송된 이미지";
            img.style.maxWidth = "200px";
            img.style.borderRadius = "8px";
            messageBubble.innerHTML = "";
            messageBubble.appendChild(img);
        } else if (text === "[file]" && fileUrl) {
            const fileBox = document.createElement("div");
            fileBox.className = "file-message";

            const icon = document.createElement("span");
            icon.textContent = "📄";
            icon.className = "file-icon";

            const filenameElem = document.createElement("span");
            filenameElem.textContent = fileName || "파일";
            filenameElem.className = "file-name";

            const downloadBtn = document.createElement("a");
            downloadBtn.href = fileUrl;
            downloadBtn.download = fileName || "download";
            downloadBtn.className = "download-btn";
            downloadBtn.textContent = "다운로드";

            fileBox.appendChild(icon);
            fileBox.appendChild(filenameElem);
            fileBox.appendChild(downloadBtn);

            messageBubble.innerHTML = "";
            messageBubble.appendChild(fileBox);
        } else {
            messageBubble.textContent = text;
        }

        if (type === "user") {
            messageContent.appendChild(messageBubble);
            messageRow.appendChild(messageContent);
        } else {
            const profileImg = document.createElement("img");
            profileImg.className = "profile-image";
            const nicknameSpan = document.createElement("span");
            nicknameSpan.className = "nickname";

            if (chatType === "group") {
                const key = String(senderIdOrName);
                const senderInfo = userMap?.[key] || { nick: `유저#${key}`, image: "/images/user.png" };
                profileImg.src = "/" + senderInfo.image.replace(/^\/?/, "");
                nicknameSpan.textContent = senderInfo.nick;
            } else {
                profileImg.src = opponentProfileUrl;
                nicknameSpan.textContent = senderIdOrName;
            }

            profileImg.alt = "프로필";
            messageContent.appendChild(nicknameSpan);
            messageContent.appendChild(messageBubble);
            messageRow.appendChild(profileImg);
            messageRow.appendChild(messageContent);
        }

        chatBox.appendChild(messageRow);
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    function loadMessages(roomId) {
        const endpoint = chatType === "group"
            ? `/group/messages?roomId=${roomId}`
            : `/chat/messages?roomId=${roomId}`;

        fetch(endpoint)
            .then(response => response.json())
            .then(messages => {
                messages.forEach(msg => {
                    const isMine = parseInt(msg.senderId) === currentUserId;
                    const type = isMine ? "user" : "partner";
                    const sender = chatType === "group" ? msg.senderId : msg.senderName;
                    appendMessage(type, sender, msg.message, msg.fileUrl, msg.fileName);
                });
            });
    }

    function sendMessage() {
        const text = input.value.trim();
        if (!text) return;

        const payload = { message: text };
        if (chatType === "group") {
            payload.senderId = currentUserId;
            payload.senderName = currentUserNick;
        }

        stompClient.send(`${sendPrefix}${roomId}`, {}, JSON.stringify(payload));
        input.value = "";
    }
});

// ✅ 파일 선택 → 모달 띄우기
function handleFileSelect(event, type) {
    selectedFile = event.target.files[0];
    selectedFileType = type;
    if (selectedFile) {
        document.getElementById("fileModal").style.display = "block";
    }
}

function closeFileModal() {
    document.getElementById("fileModal").style.display = "none";
    selectedFile = null;
    selectedFileType = null;
}

// ✅ 모달 → 실제 업로드
function confirmSendFile() {
    const roomId = new URLSearchParams(window.location.search).get("roomId");
    const csrfToken = document.querySelector("meta[name=_csrf]").content;
    const csrfHeader = document.querySelector("meta[name=_csrf_header]").content;

    const formData = new FormData();
    formData.append("file", selectedFile);
    formData.append("roomId", roomId);
    formData.append("type", selectedFileType);

    fetch("/chat/upload", {
        method: "POST",
        body: formData,
        headers: {
            [csrfHeader]: csrfToken
        }
    })
        .then(res => res.json())
        .then(data => {
            const fileUrl = data.fileUrl;
            const fileName = data.fileName;
            const currentUserId = parseInt(document.querySelector("meta[name='current-user-id']").content);
            const currentUserNick = document.querySelector("meta[name='current-user']").content;
            const chatType = document.querySelector("meta[name='chat-type']").content;
            const sendPrefix = chatType === "group" ? "/app/groupchat.send/" : "/app/chat.send/";

            const payload = {
                type: selectedFileType,
                message: selectedFileType === "image" ? "[image]" : "[file]",
                fileUrl,
                fileName,
                roomId,
                senderId: currentUserId,
                senderName: currentUserNick
            };

            if (chatType === "group") {
                payload.senderId = currentUserId;
                payload.senderName = currentUserNick;
            }

            stompClient.send(`${sendPrefix}${roomId}`, {}, JSON.stringify(payload));
        })
        .catch(err => {
            console.error("❌ 파일 전송 실패:", err);
            alert("파일 전송 실패");
        })
        .finally(() => closeFileModal());
}

function toggleAttachMenu() {
    const menu = document.getElementById('attachMenu');
    menu.style.display = menu.style.display === 'flex' ? 'none' : 'flex';
}

// ✅ HTML에서 호출할 수 있도록 전역 등록
window.toggleAttachMenu = toggleAttachMenu;
window.handleFileSelect = handleFileSelect;
window.confirmSendFile = confirmSendFile;
window.closeFileModal = closeFileModal;


