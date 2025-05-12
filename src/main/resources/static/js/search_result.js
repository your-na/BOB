function showTab(tabId) {
    document.querySelectorAll(".tab").forEach(tab => tab.classList.remove("active"));
    document.querySelectorAll(".tab-content").forEach(tc => tc.classList.remove("active"));

    document.querySelector(`.tab[onclick*="${tabId}"]`).classList.add("active");
    document.getElementById(tabId).classList.add("active");
}

document.addEventListener("DOMContentLoaded", () => {
    const cards = document.querySelectorAll(".profile-card");

    cards.forEach(card => {
        card.addEventListener("click", () => {
            const userId = card.getAttribute("data-user-id");
            if (userId) {
                // 사용자의 프로필 상세 페이지로 이동
                window.location.href = `/userprofile?userId=${userId}`;
            } else {
                alert("사용자 정보를 찾을 수 없습니다.");
            }
        });
    });
});