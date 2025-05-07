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

    // ✅ 서버에서 내가 작성한 공고 목록을 불러온다
    fetch("/api/cojobs/my-posts")
        .then(response => response.json())  // 👉 응답을 JSON 형식으로 변환
        .then(data => renderJobPosts(data)) // 👉 공고 목록을 화면에 렌더링
        .catch(error => console.error("공고 불러오기 실패:", error)); // ❌ 오류 처리

    // ✅ 받아온 공고 목록을 동적으로 HTML로 생성해 렌더링
    function renderJobPosts(posts) {
        const container = document.querySelector("#job-list-container"); // ✅ 공고만 들어갈 전용 div

        posts.forEach(post => {
            // 📅 날짜 정보 가공
            const startDate = new Date(post.startDate);
            const endDate = new Date(post.endDate);
            const today = new Date();

            // 🔄 모집 상태에 따라 텍스트/클래스 설정
            let statusLabel = "";
            let statusClass = "";
            if (today < startDate) {
                statusLabel = "모집전";
                statusClass = "status-waiting";
            } else if (today > endDate) {
                statusLabel = "마감";
                statusClass = "status-closed";
            } else {
                statusLabel = "모집중";
                statusClass = "status-open";
            }

            // ⏳ 마감일까지 남은 일수 계산
            const dDay = Math.ceil((endDate - today) / (1000 * 60 * 60 * 24));

            // 🧱 공고 하나의 HTML 카드 생성
            const jobCard = `
            <div class="job-card">
                <div class="job-status ${statusClass}">${statusLabel}</div>
                <div class="job-info">
                    <h3 class="job-title">${post.title}</h3>
                    <p class="job-dates">${post.startDate} ~ ${post.endDate}</p>
                </div>
                <div class="job-extra">
                    <div class="d-day">D-${dDay}</div>
                    <div class="applicants">지원자 수: 알 수 없음</div>
                </div>
            </div>
        `;

            // 📥 생성한 HTML을 페이지에 추가
            container.insertAdjacentHTML("beforeend", jobCard);
        });
    }

    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener("click", (e) => {
        if (!document.querySelector(".dropdown").contains(e.target)) {
            menu.style.display = "none";
        }
    });
});
