document.addEventListener("DOMContentLoaded", () => {
    const deleteBtn = document.querySelector(".delete-btn");
    const jobPostId = new URLSearchParams(window.location.search).get("jobPostId"); // URL에서 jobPostId 추출

    if (deleteBtn && jobPostId) {
        deleteBtn.addEventListener("click", (e) => {
            e.preventDefault();

            const confirmed = confirm("정말로 지원을 취소하시겠습니까?");
            if (!confirmed) return;

            fetch(`/api/user/resumes/cancel?jobPostId=${jobPostId}`, {
                method: "DELETE",
            })
                .then((res) => {
                    if (res.ok) {
                        alert("✅ 지원이 취소되었습니다.");
                        location.href = "/job-application"; // 지원 목록 페이지로 이동
                    } else {
                        return res.text().then((msg) => {
                            alert("❌ 취소 실패: " + msg);
                        });
                    }
                })
                .catch((err) => {
                    alert("⚠️ 서버 오류로 취소에 실패했습니다.");
                    console.error(err);
                });
        });
    }
});
