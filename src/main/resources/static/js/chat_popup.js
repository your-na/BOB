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
        infoDiv.onclick = () => openChatWindow(room.roomId, room.chatType || "private");


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

// 외부 클릭 시 메뉴 닫기
document.addEventListener("click", function(event) {
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


// 모달 열기
function openInviteModal(isGroup = false) {
    isGroupMode = isGroup; // ✅ 현재 모드 저장

    document.getElementById("inviteModal").style.display = "block";
    document.getElementById("searchInput").value = "";
    renderUserList(lastSearchResults);
    renderSelectedUsers();
}


// 모달 닫기
function closeInviteModal() {
    document.getElementById("inviteModal").style.display = "none";
    selectedUsers = [];
}

// 사용자 리스트 렌더링
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
            renderSelectedUsers();       // 상단에서 제거
            renderUserList(lastSearchResults); // 하단 목록도 다시 그림
        };

        tag.appendChild(img);
        tag.appendChild(name);
        tag.appendChild(removeBtn);
        container.appendChild(tag);
    });
}


// 검색 기능
function searchUsers() {
    const keyword = document.getElementById("searchInput").value.trim();

    // ✅ 검색어 없으면 목록 비우기
    if (!keyword) {
        renderUserList([]); // 빈 배열 전달 → 화면 비움
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


// 선택 추가/제거
function toggleUserSelect(checkbox) {
    const id = parseInt(checkbox.value);
    if (checkbox.checked) {
        if (!selectedUsers.includes(id)) {
            selectedUsers.push(id);
        }
    } else {
        selectedUsers = selectedUsers.filter(uid => uid !== id);
    }

    renderSelectedUsers();
}

const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

// 확인 버튼
function confirmInvite() {
    if (selectedUsers.length === 0) {
        alert("최소 한 명 이상 선택해주세요.");
        return;
    }

    // ✅ 사용자가 '1:1 채팅' 모드를 선택한 경우
    if (!isGroupMode) {
        if (selectedUsers.length > 1) {
            alert("1:1 채팅은 한 명만 선택할 수 있습니다.");
            return;
        }

        const selectedUser = allUsers.find(u => u.id === selectedUsers[0]);
        createPrivateChat(selectedUser.nickname);
    }

    // ✅ 사용자가 '그룹 채팅' 모드를 선택한 경우
    else {
        if (selectedUsers.length < 2) {
            alert("그룹 채팅은 두 명 이상 선택해야 합니다.");
            return;
        }

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

    // ✅ 마무리
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


let lastSearchResults = [];

function renderUserList(users) {
    lastSearchResults = users;
    const list = document.getElementById("userList");
    list.innerHTML = "";

    users.forEach(user => {
        const isChecked = selectedUsers.includes(user.id);

        const item = document.createElement("div");
        item.className = "user-item";

        // 1:1 모드에서 이미 1명 선택되어 있으면 다른 항목 비활성화
        const disabled = isGroupMode ? "" : (selectedUsers.length >= 1 && !isChecked ? "disabled" : "");

        item.innerHTML = `
            <img src="${user.avatar}" alt="avatar">
            <span>${user.nickname}</span>
            <input type="checkbox" value="${user.id}" ${isChecked ? "checked" : ""} ${disabled} onchange="toggleUserSelect(this)">
        `;
        list.appendChild(item);
    });
}




