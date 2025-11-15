function getCsrfTokenFromCookie() {
    const name = "XSRF-TOKEN=";
    const decodedCookie = decodeURIComponent(document.cookie);
    const cookies = decodedCookie.split(';');

    for (let i = 0; i < cookies.length; i++) {
        const c = cookies[i].trim();
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

// 게시글 ID
const postId = new URLSearchParams(window.location.search).get("id");

// 좋아요 하트 토글
document.querySelector('.post-status .like img').addEventListener('click', function () {
    this.src = this.src.includes('heart2.png') ? '/images/pinkheart.png' : '/images/heart2.png';
});

// 메뉴 토글
const menuIcon = document.querySelector(".menu-icon");
const menuPopup = document.querySelector(".menu-popup");
menuIcon.addEventListener("click", e => {
    e.stopPropagation();
    menuPopup.style.display = (menuPopup.style.display === "block") ? "none" : "block";
});
document.addEventListener("click", () => menuPopup.style.display = "none");

// 게시글 상세 데이터 가져오기
fetch(`/api/posts/${postId}`)
    .then(res => {
        if (!res.ok) throw new Error("게시글을 불러오지 못했습니다");
        return res.json();
    })
    .then(post => {
        document.querySelector(".post-title").textContent = `[${post.category}] ${post.title}`;
        document.querySelector(".writer").textContent = post.writer;
        window.postWriter = post.writer; // ✅ 전역으로 저장해서 댓글 비교에 사용
        document.querySelector(".post-date").textContent = post.createdAt;
        document.querySelector(".post-content").textContent = post.content;

        // 첨부파일 처리
        if (post.filePath) {
            const fileDiv = document.querySelector(".file-preview");
            const fileName = post.filePath.split("/").pop();

            if (post.filePath.match(/\.(jpg|jpeg|png|gif)$/i)) {
                fileDiv.innerHTML = `
  <img
    src="${post.filePath}"
    alt="첨부 이미지"
    style="max-width: 600px; max-height: 400px; width: 100%; height: auto; margin-top: 10px; border-radius: 8px;"
  >
`;
            } else {
                fileDiv.innerHTML = `<a href="${post.filePath}" download style="margin-top:10px; display:inline-block;">📎 ${fileName}</a>`;
            }
        }
    })
    .catch(err => {
        alert(err.message);
        location.href = "/boardlist";
    });

// ✅ 댓글 목록 불러오기
function loadComments() {
    fetch(`/api/comments/${postId}`)
        .then(res => {
            if (!res.ok) throw new Error("댓글 불러오기 실패");
            return res.json();
        })
        .then(comments => {
            const box = document.querySelector(".comment-box");
            box.innerHTML = ""; // 초기화

            comments.forEach(comment => {
                const div = document.createElement("div");
                div.className = "comment-item";
                const isPostWriter = comment.writer === postWriter;
                const writerLabel = isPostWriter ? "작성자" : comment.writer;
                const writerStyle = isPostWriter ? 'color: green; font-weight: bold;' : '';

                div.innerHTML = `
  <div class="comment-writer" style="${writerStyle}">${writerLabel}</div>
  <div class="comment-content">${comment.content}</div>
  <div class="comment-date">${comment.createdAt}</div>
  <img src="/images/heart2.png" class="comment-like">
`;


                // 하트 토글 (각 댓글에 대해)
                const heart = div.querySelector(".comment-like");
                heart.addEventListener("click", () => {
                    heart.src = heart.src.includes("heart2.png") ? "/images/pinkheart.png" : "/images/heart2.png";
                });

                box.appendChild(div);
            });
        })
        .catch(err => alert(err.message));
}

// ✅ 댓글 등록 처리
document.querySelector(".comment-submit").addEventListener("click", () => {
    const input = document.querySelector(".comment-input");
    const content = input.value.trim();

    if (!content) {
        alert("댓글을 입력해주세요.");
        return;
    }

    fetch(`/api/comments`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-XSRF-TOKEN": getCsrfTokenFromCookie()
        },
        body: JSON.stringify({ content, postId }),  // ✅ postId 포함해서 전송
        credentials: "include"
    })
        .then(res => {
            if (!res.ok) throw new Error("댓글 등록 실패");
            return res.text();
        })
        .then(() => {
            input.value = "";      // 입력창 초기화
            loadComments();        // 댓글 목록 새로고침
        })
        .catch(err => alert(err.message));
});

// 페이지 로드시 댓글 불러오기
loadComments();




