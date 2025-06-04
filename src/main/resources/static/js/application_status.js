// ✅ 검색 및 필터링 함수
function filterApplications() {
    const searchKeyword = document.getElementById("search-box").value.toLowerCase();
    const filterValue = document.getElementById("filter-select").value;
    const allCards = document.querySelectorAll(".application-card");

    allCards.forEach(card => {
        const title = card.querySelector("h3").textContent.toLowerCase();
        const matchesSearch = title.includes(searchKeyword);
        const matchesFilter = (filterValue === "all" || card.classList.contains(filterValue));

        if (matchesSearch && matchesFilter) {
            card.style.display = "block";
        } else {
            card.style.display = "none";
        }
    });
}

// ✅ 구직 지원 현황 카드 동적 렌더링
function fetchJobApplications() {
    fetch("/api/applications/me")
        .then(res => res.json())
        .then(data => {
            const listContainer = document.querySelector(".application-list");

            data.forEach(item => {
                if (item.status === "SUBMITTED") {
                    const card = document.createElement("div");
                    card.className = "application-card job";

                    card.innerHTML = `
                        <h3>[구직] ${item.jobTitle}</h3>
                        <p class="date">📅 제출일 ${item.appliedDate}</p>
                        <button class="status-button pending">⏳ 지원 후 대기중</button>
                    `;

                    listContainer.appendChild(card);
                }
            });

            filterApplications(); // 필터 재적용
        })
        .catch(err => {
            console.error("구직 지원 내역 불러오기 실패:", err);
        });
}

// ✅ 페이지 로드 시 실행
document.addEventListener("DOMContentLoaded", function () {
    filterApplications();
    fetchJobApplications(); // ✅ 구직 카드 불러오기
});

document.addEventListener("DOMContentLoaded", function () {
    const projectCards = document.querySelectorAll(".application-card.project");

    projectCards.forEach(card => {
        card.addEventListener("click", function () {
            const projectId = this.getAttribute("data-id");
            if (projectId) {
                // ✅ 실제 이동할 신청서 상세 페이지 URL로 변경하세요
                window.location.href = `/projapplication2?projectId=${projectId}`;
            }
        });
    });
});
