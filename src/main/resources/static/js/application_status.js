// ✅ 검색 및 필터링 함수
function filterApplications() {
    const searchKeyword = document.getElementById("search-box").value.toLowerCase();
    const filterValue = document.getElementById("filter-select").value;
    const allCards = document.querySelectorAll(".application-card");

    allCards.forEach(card => {
        const title = card.querySelector("h3").textContent.toLowerCase(); // 제목 검색
        const matchesSearch = title.includes(searchKeyword);
        const matchesFilter = (filterValue === "all" || card.classList.contains(filterValue));

        if (matchesSearch && matchesFilter) {
            card.style.display = "block";
        } else {
            card.style.display = "none";
        }
    });
}

// ✅ 페이지 로드 시 초기 필터 적용
document.addEventListener("DOMContentLoaded", filterApplications);
