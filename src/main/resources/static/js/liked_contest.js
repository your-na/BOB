// 검색 기능
function filterContests() {
    const searchKeyword = document.getElementById("search-box").value.toLowerCase();
    const allCards = document.querySelectorAll(".contest-card");

    allCards.forEach(card => {
        const title = card.querySelector(".contest-title").textContent.toLowerCase();
        if (title.includes(searchKeyword)) {
            card.style.display = "block";
        } else {
            card.style.display = "none";
        }
    });
}

// 전체 선택 기능
function toggleSelectAll() {
    const isChecked = document.getElementById("select-all").checked;
    const checkboxes = document.querySelectorAll(".contest-checkbox");

    checkboxes.forEach(checkbox => {
        checkbox.checked = isChecked;
    });
}

// 선택된 항목 삭제
function deleteSelected() {
    const selected = document.querySelectorAll(".contest-checkbox:checked");

    if (selected.length === 0) {
        alert("삭제할 공모전을 선택해주세요.");
        return;
    }

    if (!confirm("선택한 공모전을 삭제하시겠습니까?")) return;

    selected.forEach(checkbox => {
        checkbox.closest(".contest-card").remove();
    });

    // 백엔드 연동 (예제: 실제 요청 시 서버에서 데이터 삭제)
    const contestIds = Array.from(selected).map(checkbox => checkbox.dataset.contestId);
    console.log("삭제 요청 ID 목록:", contestIds);

    // fetch('/delete-liked-contests', {
    //     method: 'POST',
    //     headers: { 'Content-Type': 'application/json' },
    //     body: JSON.stringify({ ids: contestIds })
    // }).then(response => {
    //     if (response.ok) {
    //         alert("삭제 완료!");
    //     } else {
    //         alert("삭제 실패!");
    //     }
    // });
}
