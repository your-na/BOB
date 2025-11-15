// ✅ 1. 쿠키에서 CSRF 토큰 꺼내는 함수
function getCsrfTokenFromCookie() {
    const cookieValue = document.cookie
        .split("; ")
        .find(row => row.startsWith("XSRF-TOKEN="));
    return cookieValue ? decodeURIComponent(cookieValue.split("=")[1]) : null;
}

// ✅ 2. 파일명 표시 리스너 (submit 바깥에서 등록해야 정상 작동)
const fileInput = document.getElementById("fileInput");
const fileNameDisplay = document.getElementById("fileName");

fileInput.addEventListener("change", function () {
    const fileName = this.files.length > 0 ? this.files[0].name : "선택된 파일 없음";
    fileNameDisplay.textContent = fileName;
});

// ✅ 3. 폼 제출 처리
const postForm = document.getElementById("postForm");

postForm.addEventListener("submit", function (e) {
    e.preventDefault();

    const formData = new FormData(postForm);
    formData.append("writer", "임시작성자"); // 실제 로그인 연동 시 제거

    fetch("/api/posts", {
        method: "POST",
        body: formData,
        headers: {
            "X-XSRF-TOKEN": getCsrfTokenFromCookie()
        },
        credentials: "include"
    })
        .then(response => {
            if (response.ok) {
                alert("게시글 등록 성공!");
                location.href = "/boardlist";
            } else {
                return response.text().then(msg => { throw new Error(msg); });
            }
        })
        .catch(err => {
            alert("에러: " + err.message);
        });
});
