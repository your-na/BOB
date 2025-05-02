document.addEventListener("DOMContentLoaded", () => {
    const toggle = document.querySelector(".dropdown-toggle");
    const menu = document.querySelector(".dropdown-menu");

    toggle.addEventListener("click", () => {
        menu.style.display = menu.style.display === "block" ? "none" : "block";
    });

    menu.querySelectorAll("li").forEach(item => {
        item.addEventListener("click", () => {
            toggle.textContent = item.textContent;
            menu.style.display = "none";
            // 🔄 필터링 로직은 나중에 백엔드와 연동
            console.log("선택한 필터:", item.dataset.value);
        });
    });

    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener("click", (e) => {
        if (!document.querySelector(".dropdown").contains(e.target)) {
            menu.style.display = "none";
        }
    });
});
