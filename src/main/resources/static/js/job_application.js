document.addEventListener("DOMContentLoaded", function () {
    const tabs = document.querySelectorAll(".tab");
    const listContainer = document.querySelector(".application-list");

    const dummyData = {
        online: [
            { id: 1, date: "2025.03.01", title: "7Days COFFEE", desc: "주말 미들 근무 하실분 모집 (마감)" },
            { id: 2, date: "2025.03.05", title: "GS편의점", desc: "야간 근무자 모집합니다." }
        ],
        other: [
            { id: 3, date: "2025.02.20", title: "배민B마트", desc: "피킹포장 야간근무 (합격)" }
        ],
        other2: [
            { id: 4, date: "2025.01.10", title: "스타벅스", desc: "주중 바리스타 모집 (불합격)" }
        ],
        hidden: [
            { id: 5, date: "2024.12.12", title: "이디야커피", desc: "오픈 근무자 모집 (숨김)" }
        ]
    };

    let currentTab = "online";

    function renderList(type) {
        currentTab = type;
        listContainer.innerHTML = "";

        const data = dummyData[type] || [];

        if (data.length === 0) {
            listContainer.innerHTML = "<p style='padding: 20px; color: #888;'>해당 내역이 없습니다.</p>";
            return;
        }

        data.forEach(item => {
            const card = document.createElement("div");
            card.className = "application-card";
            card.setAttribute("data-id", item.id);
            card.innerHTML = `
                <div class="left">
                    <div class="apply-date">${item.date}</div>
                    <div class="job-title">${item.title}</div>
                    <div class="job-desc">${item.desc}</div>
                </div>
                <div class="right">
                    <button class="open-menu-btn" onclick="toggleMenu(this)">⋯</button>
                    <ul class="dropdown-menu">
                        <li onclick="viewDetail(${item.id})">지원내역</li>
                        <li onclick="cancelApply(${item.id})">지원취소</li>
                        ${
                type === "hidden"
                    ? `<li onclick="unhideItem(${item.id})">숨기기 취소</li>`
                    : `<li onclick="hideItem(${item.id}, '${type}')">숨기기</li>`
            }
                    </ul>
                </div>
            `;
            listContainer.appendChild(card);
        });
    }

    tabs.forEach(tab => {
        tab.addEventListener("click", () => {
            tabs.forEach(t => t.classList.remove("active"));
            tab.classList.add("active");

            const type = tab.getAttribute("data-type");
            renderList(type);
        });
    });

    document.addEventListener("click", function (e) {
        if (!e.target.closest(".application-card")) {
            document.querySelectorAll(".dropdown-menu").forEach(menu => menu.style.display = "none");
        }
    });

    renderList("online");

    // 숨기기
    window.hideItem = function (id, fromType) {
        const itemIndex = dummyData[fromType].findIndex(item => item.id === id);
        if (itemIndex !== -1) {
            const [item] = dummyData[fromType].splice(itemIndex, 1);
            if (!item.desc.includes("(숨김)")) {
                item.desc += " (숨김)";
            }
            dummyData.hidden.push(item);
            renderList(currentTab);
        }
    };

    // 숨기기 취소
    window.unhideItem = function (id) {
        const itemIndex = dummyData.hidden.findIndex(item => item.id === id);
        if (itemIndex !== -1) {
            const [item] = dummyData.hidden.splice(itemIndex, 1);
            item.desc = item.desc.replace(" (숨김)", "");
            dummyData.online.push(item); // 기본적으로 online에 복원
            renderList(currentTab);
        }
    };

    // 지원내역 보기
    window.viewDetail = function (id) {
        alert(`📄 ${id}번 항목 상세보기`);
    };

    // 지원취소
    window.cancelApply = function (id) {
        alert(`❌ ${id}번 항목 지원취소`);
    };
});

// 메뉴 토글
function toggleMenu(button) {
    const menu = button.nextElementSibling;
    document.querySelectorAll(".dropdown-menu").forEach(m => {
        if (m !== menu) m.style.display = "none";
    });
    menu.style.display = menu.style.display === "block" ? "none" : "block";
}
