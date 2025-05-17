document.addEventListener("DOMContentLoaded", () => {
    const deleteBtn = document.querySelector(".delete-btn");
    const jobPostId = document.getElementById("jobPostId")?.value;


    // ✅ CSRF 토큰과 헤더 이름 가져오기
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    if (deleteBtn && jobPostId && csrfToken && csrfHeader) {
        deleteBtn.addEventListener("click", (e) => {
            e.preventDefault();

            const confirmed = confirm("정말로 지원을 취소하시겠습니까?");
            if (!confirmed) return;

            fetch(`/api/user/resumes/cancel?jobPostId=${jobPostId}`, {
                method: "DELETE",
                credentials: "include",
                headers: {
                    [csrfHeader]: csrfToken
                }
            })
                .then((res) => {
                    if (res.ok) {
                        alert("✅ 지원이 취소되었습니다.");
                        location.href = "/jobapplication";
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

    // ✅ 합격 처리 버튼과 모달 로직
    const passBtn = document.querySelector(".pass-btn");
    const passModal = document.getElementById("passModal");
    const closeModalBtn = document.querySelector(".close");
    const submitPassBtn = document.getElementById("submitPassBtn");

    if (passBtn && passModal) {
        passBtn.addEventListener("click", () => {
            passModal.style.display = "block";
        });

        closeModalBtn?.addEventListener("click", () => {
            passModal.style.display = "none";
        });

        submitPassBtn?.addEventListener("click", () => {
            const message = document.getElementById("passMessage").value.trim();
            if (!message) {
                alert("메시지를 입력해 주세요.");
                return;
            }

            if (!jobPostId || !csrfToken || !csrfHeader) {
                alert("필수 정보가 누락되었습니다.");
                return;
            }

            fetch(`/api/job/pass`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({
                    jobPostId: jobPostId,
                    message: message
                })
            })
                .then(res => {
                    if (res.ok) {
                        alert("✅ 합격 처리가 완료되었습니다.");
                        passModal.style.display = "none";
                    } else {
                        return res.text().then(msg => alert("❌ 실패: " + msg));
                    }
                })
                .catch(err => {
                    alert("⚠️ 서버 오류");
                    console.error(err);
                });
        });
    }

});
