document.addEventListener("DOMContentLoaded", function () {
    const menuLinks = document.querySelectorAll(".menu li a");
    const iframe = document.getElementById("main-frame");

    // ✅ 메뉴 클릭 시 iframe에 페이지 로드
    menuLinks.forEach(link => {
        link.addEventListener("click", function (event) {
            event.preventDefault();

            const pageUrl = this.getAttribute("data-page");
            if (!pageUrl) return;

            // ✅ iframe에 Thymeleaf 페이지 로드
            iframe.src = pageUrl;

            // ✅ 현재 메뉴 강조
            menuLinks.forEach(link => link.classList.remove("active"));
            this.classList.add("active");
        });
    });

    // ✅ 첫 번째 기본 페이지 자동 로드
    iframe.src = "/adcontest"; // 기본: 전체 공모전
});
