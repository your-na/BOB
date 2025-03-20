// 필터링 함수 (검색 + 미제출 필터 동시 적용)
function filterContests() {
    const showUnsubmittedOnly = document.getElementById("filter-unsubmitted").checked; // 미제출만 보기 체크 여부
    const searchKeyword = document.getElementById("search-box").value.toLowerCase(); // 검색어 입력 값
    const allCards = document.querySelectorAll(".contest-card"); // ✅ 모든 공모전 카드 가져오기

    allCards.forEach(card => {
        const titleElement = card.querySelector(".contest-info h3"); // ✅ 제목 가져오기
        const title = titleElement ? titleElement.textContent.toLowerCase() : "";
        const isSubmitted = card.classList.contains("submitted"); // ✅ 제출된 공모전인지 확인

        // 🔹 검색 조건: 제목이 검색어를 포함하는지 확인
        const matchesSearch = title.includes(searchKeyword);

        // 🔹 미제출 필터 조건: '미제출만 보기' 체크박스가 활성화되었을 때, 'submitted' 클래스가 없어야 함
        const matchesSubmissionFilter = !showUnsubmittedOnly || !isSubmitted;

        // ✅ 검색과 미제출 필터가 **모두 충족**될 때만 보이도록 설정
        if (matchesSearch && matchesSubmissionFilter) {
            card.style.display = "";  // ✅ 기본값을 유지하도록 `display` 속성 초기화
        } else {
            card.style.display = "none"; // ✅ 필터에 맞지 않으면 숨김
        }
    });
}

// ✅ 검색어 입력 시 필터링 실행
document.getElementById("search-box").addEventListener("keyup", filterContests);

// ✅ 미제출 필터 체크박스 변경 시 필터링 실행
document.getElementById("filter-unsubmitted").addEventListener("change", filterContests);

// ✅ 페이지 로드 시 필터 적용
document.addEventListener("DOMContentLoaded", filterContests);
