document.addEventListener("DOMContentLoaded", function () {
    const filterBtn = document.querySelector(".filter-btn");
    const filterMenu = document.querySelector(".filter-menu");
    const searchInput = document.getElementById("search-input");
    const detailButtons = document.querySelectorAll(".detail-btn");

    // 필터 버튼 클릭 시 메뉴 보이기
    filterBtn.addEventListener("click", function () {
        filterMenu.style.display = filterMenu.style.display === "block" ? "none" : "block";
    });

    // 필터 선택 시 텍스트 변경
    filterMenu.querySelectorAll("li").forEach(item => {
        item.addEventListener("click", function () {
            filterBtn.textContent = this.textContent + " ▼";
            filterMenu.style.display = "none";
        });
    });

    // 검색 기능 (간단한 필터링)
    searchInput.addEventListener("input", function () {
        const query = searchInput.value.toLowerCase();
        document.querySelectorAll("#contest-body tr").forEach(row => {
            const host = row.children[1].textContent.toLowerCase();
            row.style.display = host.includes(query) ? "" : "none";
        });
    });

    // 상세보기 버튼 클릭 시 페이지 이동
    detailButtons.forEach(button => {
        button.addEventListener("click", function () {
            const contestId = this.getAttribute("data-id");
            window.location.href = `/contest/${contestId}`; // ✅ 백엔드 라우팅 경로에 맞게 수정
        });
    });

    // 페이지네이션 기능 (추가 필요)
});
