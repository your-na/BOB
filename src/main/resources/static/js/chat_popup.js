document.addEventListener("DOMContentLoaded", () => {
    fetch("/api/chat/rooms", {
        method: "GET",
        credentials: "include"
    })
        .then(res => res.json())
        .then(data => renderChatRooms(data))
        .catch(err => console.error("❌ 채팅방 목록 가져오기 실패:", err));
});

// ✅ 우클릭 메뉴 열기
function openContextMenu(event, element) {
    event.preventDefault();
    const menu = document.getElementById("chatContextMenu");
    menu.style.display = "block";
    menu.style.top = `${event.clientY}px`;
    menu.style.left = `${event.clientX}px`;
    menu.dataset.chatId = element.dataset.chatId;
}

// ✅ 외부 클릭 시 우클릭 메뉴 닫기
document.addEventListener("click", () => {
    document.getElementById("chatContextMenu").style.display = "none";
});

// ✅ 채팅방 렌더링
function renderChatRooms(rooms) {
    const container = document.querySelector(".chat-container");
    container.innerHTML = "";

    // 상단 고정된 방 먼저, 나머지는 아래
    const pinned = rooms.filter(r => r.pinned);
    const normal = rooms.filter(r => !r.pinned);
    const sorted = [...pinned, ...normal];

    sorted.forEach(room => {
        const roomDiv = document.createElement("div");
        roomDiv.className = "chat-room";
        roomDiv.dataset.chatId = room.roomId;

        const profileDiv = document.createElement("div");
        profileDiv.className = "profile";
        let imageUrl = room.opponentProfileUrl || "/images/user.png";
        if (!imageUrl.startsWith("/")) imageUrl = "/" + imageUrl;
        profileDiv.style.backgroundImage = `url('${imageUrl}')`;

        const infoDiv = document.createElement("div");
        infoDiv.className = "info";
        infoDiv.onclick = () => openChatWindow(room.roomId, room.chatType || "private");

        const name = document.createElement("strong");
        name.textContent = room.roomName || room.opponentNick;

        const message = document.createElement("p");
        message.textContent = room.lastMessage || "";

        infoDiv.appendChild(name);
        infoDiv.appendChild(message);

        // 핀 아이콘
        if (room.pinned) {
            const pin = document.createElement("span");
            pin.textContent = "📌";
            pin.className = "pin-icon";
            pin.style.marginLeft = "8px";
            pin.style.color = "#999";
            name.appendChild(pin);
        }

        roomDiv.appendChild(profileDiv);
        roomDiv.appendChild(infoDiv);
        roomDiv.oncontextmenu = (e) => openContextMenu(e, roomDiv);

        container.appendChild(roomDiv);
    });
}

// ✅ 메뉴 항목 처리
function handleChatOption(option) {
    const chatId = document.getElementById("chatContextMenu").dataset.chatId;
    switch (option) {
        case "open":
            openChatWindow(chatId);
            break;
        case "rename":
            const chatRoomElement = document.querySelector(`[data-chat-id="${chatId}"]`);
            const currentName = chatRoomElement?.querySelector(".info strong")?.childNodes[0]?.textContent || "";
            openRenameModal(chatId, currentName);
            break;
        case "pin":
            fetch(`/api/chat/room/${chatId}/pin`, {
                method: "POST"
            }).then(() => {
                alert("채팅방 고정 상태가 변경되었습니다.");
                location.reload();
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

document.addEventListener("click", function (event) {
    const menu = document.getElementById("addChatMenu");
    const btn = document.querySelector(".add-chat-btn");
    if (!menu.contains(event.target) && !btn.contains(event.target)) {
        menu.style.display = "none";
    }
});

function openChatWindow(roomId, type = "private") {
    if (!roomId) return;
    const url = type === "group"
        ? `/chat/chatroom?roomId=${roomId}&type=group`
        : `/chat/chatroom?roomId=${roomId}`;
    window.open(url, "_blank", "width=500,height=700,resizable=yes");
}

let selectedUsers = [];
let allUsers = [];
let isGroupMode = false;

function openInviteModal(isGroup = false) {
    isGroupMode = isGroup;
    document.getElementById("inviteModal").style.display = "block";
    document.getElementById("searchInput").value = "";
    renderUserList(lastSearchResults);
    renderSelectedUsers();
}

function closeInviteModal() {
    document.getElementById("inviteModal").style.display = "none";
    selectedUsers = [];
}

function renderSelectedUsers() {
    const container = document.getElementById("selectedUserList");
    container.innerHTML = "";
    selectedUsers.forEach(userId => {
        const user = allUsers.find(u => u.id === userId);
        if (!user) return;

        const tag = document.createElement("div");
        tag.className = "selected-user-tag";
        tag.style.cssText = `
            background: #f0f0f0;
            border-radius: 20px;
            padding: 4px 10px;
            display: flex;
            align-items: center;
            font-size: 13px;
        `;

        const img = document.createElement("img");
        img.src = user.avatar || "/images/user.png";
        img.alt = "profile";
        img.style.width = "24px";
        img.style.height = "24px";
        img.style.borderRadius = "50%";
        img.style.marginRight = "6px";

        const name = document.createElement("span");
        name.textContent = user.nickname;

        const removeBtn = document.createElement("span");
        removeBtn.textContent = "×";
        removeBtn.style.cssText = `
            margin-left: 8px;
            cursor: pointer;
            color: #999;
            font-weight: bold;
        `;
        removeBtn.onclick = () => {
            selectedUsers = selectedUsers.filter(id => id !== userId);
            renderSelectedUsers();
            renderUserList(lastSearchResults);
        };

        tag.appendChild(img);
        tag.appendChild(name);
        tag.appendChild(removeBtn);
        container.appendChild(tag);
    });
}

function searchUsers() {
    const keyword = document.getElementById("searchInput").value.trim();
    if (!keyword) {
        renderUserList([]);
        return;
    }

    fetch(`/api/users/search?keyword=${encodeURIComponent(keyword)}`)
        .then(res => res.json())
        .then(data => {
            allUsers = data;
            renderUserList(data);
        })
        .catch(err => {
            console.error("유저 검색 실패:", err);
        });
}

function toggleUserSelect(checkbox) {
    const id = parseInt(checkbox.value);
    if (checkbox.checked) {
        if (!selectedUsers.includes(id)) selectedUsers.push(id);
    } else {
        selectedUsers = selectedUsers.filter(uid => uid !== id);
    }
    renderSelectedUsers();
}

const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

function confirmInvite() {
    if (selectedUsers.length === 0) return alert("최소 한 명 이상 선택해주세요.");

    if (!isGroupMode) {
        if (selectedUsers.length > 1) return alert("1:1 채팅은 한 명만 선택할 수 있습니다.");
        const selectedUser = allUsers.find(u => u.id === selectedUsers[0]);
        createPrivateChat(selectedUser.nickname);
    } else {
        if (selectedUsers.length < 2) return alert("그룹 채팅은 두 명 이상 선택해야 합니다.");
        const roomName = prompt("그룹 채팅방 이름을 입력하세요", "새 그룹채팅");
        if (!roomName) return;
        fetch("/api/group-chat", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({
                roomName: roomName,
                userIds: selectedUsers
            })
        })
            .then(res => res.json())
            .then(data => {
                const roomId = data.roomId;
                window.open(`/chat/chatroom?roomId=${roomId}&type=group`, "_blank", "width=500,height=700");
            })
            .catch(err => {
                alert("그룹 채팅방 생성 실패");
                console.error(err);
            });
    }

    selectedUsers = [];
    renderSelectedUsers();
    closeInviteModal();
}

function createPrivateChat(opponentNick) {
    const currentUserNick = document.querySelector("meta[name='current-user']")?.content;
    if (!currentUserNick || !csrfToken || !csrfHeader) {
        alert("인증 정보를 불러오지 못했습니다.");
        return;
    }

    fetch("/api/chat/room/create", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken
        },
        credentials: "include",
        body: JSON.stringify({
            userNickA: currentUserNick,
            userNickB: opponentNick
        })
    })
        .then(res => res.json())
        .then(data => {
            const roomId = typeof data === "object" ? data.roomId ?? data : data;
            if (!roomId || roomId === "undefined") {
                alert("채팅방 생성 실패: 잘못된 응답");
                return;
            }
            window.open(`/chat/chatroom?roomId=${roomId}`, "_blank", "width=500,height=700");
        })
        .catch(err => {
            alert("채팅방 생성 실패");
            console.error(err);
        });
}

// ✅ 채팅방 이름 변경 기능
let currentChatIdForRename = null;

function openRenameModal(chatId, currentName = "") {
    currentChatIdForRename = chatId;
    const modal = document.getElementById("renameModal");
    const input = document.getElementById("chatRoomNameInput");
    const count = document.getElementById("charCount");
    const confirmBtn = document.getElementById("renameConfirmBtn");

    input.value = currentName;
    count.textContent = input.value.length;
    confirmBtn.disabled = input.value.trim().length === 0;
    modal.style.display = "block";

    input.oninput = () => {
        const length = input.value.length;
        count.textContent = length;
        confirmBtn.disabled = length === 0;
    };
}

function closeRenameModal() {
    document.getElementById("renameModal").style.display = "none";
}

document.getElementById("renameConfirmBtn").addEventListener("click", () => {
    const newName = document.getElementById("chatRoomNameInput").value.trim();
    if (!newName) return;

    fetch(`/api/chat/room/${currentChatIdForRename}/rename`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({ name: newName })
    })
        .then(res => {
            if (!res.ok) throw new Error("변경 실패");
            return res.text();
        })
        .then(() => {
            alert("채팅방 이름이 변경되었습니다.");
            location.reload();
        })
        .catch(err => alert("오류: " + err.message));
});

function renderUserList(users) {
    lastSearchResults = users;
    const list = document.getElementById("userList");
    list.innerHTML = "";

    users.forEach(user => {
        const isChecked = selectedUsers.includes(user.id);
        const item = document.createElement("div");
        item.className = "user-item";

        // 1:1 모드에서는 1명 이상 선택 방지
        const disabled = isGroupMode ? "" : (selectedUsers.length >= 1 && !isChecked ? "disabled" : "");

        item.innerHTML = `
            <img src="${user.avatar || "/images/user.png"}" alt="avatar" class="user-avatar">
            <span>${user.nickname}</span>
            <input type="checkbox" value="${user.id}" ${isChecked ? "checked" : ""} ${disabled} onchange="toggleUserSelect(this)">
        `;
        list.appendChild(item);
    });
}

let lastSearchResults = [];
