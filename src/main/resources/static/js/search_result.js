// function showTab(tabId) {
//     document.querySelectorAll(".tab").forEach(tab => tab.classList.remove("active"));
//     document.querySelectorAll(".tab-content").forEach(tc => tc.classList.remove("active"));
//
//     document.querySelector(`.tab[onclick*="${tabId}"]`).classList.add("active");
//     document.getElementById(tabId).classList.add("active");
// }

function showTab(tabName) {
    // 모든 탭 버튼에서 active 클래스 제거
    document.querySelectorAll('.tab').forEach(btn => btn.classList.remove('active'));
    // 모든 탭 콘텐츠 숨기기
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));

    // 선택된 탭 버튼 및 콘텐츠에 active 클래스 추가
    document.querySelector(`button[onclick="showTab('${tabName}')"]`).classList.add('active');
    document.getElementById(tabName).classList.add('active');
}


document.addEventListener("DOMContentLoaded", () => {
    const cards = document.querySelectorAll(".profile-card");

    cards.forEach(card => {
        card.addEventListener("click", () => {
            const userId = card.getAttribute("data-user-id");
            const userNick = card.querySelector("strong")?.textContent?.trim(); // ✅ 닉네임 가져오기

            if (userId) {
                // ✅ 프로필 상세 페이지로 이동
                window.location.href = `/profileview?userNick=${encodeURIComponent(userNick)}`;
            } else {
                alert("사용자 정보를 찾을 수 없습니다.");
            }
        });
    });

    // ✅ 공모전 카드 클릭 시 이동
    const contestCards = document.querySelectorAll(".contest-card");
    contestCards.forEach(card => {
        card.addEventListener("click", () => {
            const contestId = card.getAttribute("data-id");
            if (contestId) {
                window.location.href = `/contest/${contestId}`;
            }
        });
    });

    // ✅ 프로젝트 카드 클릭 시 이동
    const projectCards = document.querySelectorAll(".project-card");
    projectCards.forEach(card => {
        card.addEventListener("click", () => {
            const projectId = card.getAttribute("data-id");
            if (projectId) {
                window.location.href = `/postproject/${projectId}`;
            }
        });
    });
});

