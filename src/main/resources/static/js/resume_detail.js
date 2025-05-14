document.addEventListener("DOMContentLoaded", () => {
    const deleteBtn = document.querySelector(".delete-btn");

    if (deleteBtn) {
        deleteBtn.addEventListener("click", (e) => {
            e.preventDefault();
            const confirmed = confirm("정말로 지원을 취소하시겠습니까?");
            if (confirmed) {
                // TODO: 지원 취소 요청 보내기 (API 필요)
                alert("지원 취소가 처리되었습니다.");
            }
        });
    }
});
