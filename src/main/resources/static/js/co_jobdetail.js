// 🍪 쿠키에서 CSRF 토큰(XSRF-TOKEN) 추출
function getCsrfToken() {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; XSRF-TOKEN=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

// 이력서 모달 열기
function openResumeModal(title) {
    const modal = document.getElementById('resumeModal');
    const modalTitle = document.getElementById('modal-title');
    modalTitle.textContent = title;
    modal.style.display = 'flex';
}

// 이력서 모달 닫기
function closeResumeModal() {
    const modal = document.getElementById('resumeModal');
    modal.style.display = 'none';
}

// ESC 누르면 닫기
document.addEventListener('keydown', function(event) {
    if (event.key === "Escape") {
        closeResumeModal();
    }
});

// 배경 클릭 시 닫기
document.addEventListener('click', function(event) {
    const modal = document.getElementById('resumeModal');
    if (event.target === modal) {
        closeResumeModal();
    }
});

// 상세 공고 정보 불러오기
document.addEventListener("DOMContentLoaded", () => {
    const jobId = new URLSearchParams(window.location.search).get("id");

    if (!jobId) {
        console.error("공고 ID가 없습니다.");
        return;
    }

    fetch(`/api/cojobs/${jobId}/with-resumes`)
        .then(res => res.json())
        .then(data => {
            console.log("서버에서 받은 공고 데이터:", data);  // ✅ 이거 추가!

            // ✅ 지원서 접수 방식에 따른 이력서 영역 분기 처리
            const companyBox = document.getElementById("company-resume-box");
            const memberBox = document.getElementById("member-resume-box");

            if (data.applyType === "company") {
                companyBox.classList.remove("hidden");
                memberBox.classList.add("hidden");
            } else if (data.applyType === "member") {
                companyBox.classList.add("hidden");
                memberBox.classList.remove("hidden");
            }


            // 제목 표시
            document.getElementById("job-title").textContent = `${data.title}`;

            // ⭐ D-Day 계산 및 출력
            const badge = document.querySelector(".badge");
            const endDate = new Date(data.endDate);
            const today = new Date();
            const diffTime = endDate - today;
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

            badge.textContent = diffDays >= 0 ? `D-${diffDays}` : "마감됨";
            if (diffDays < 0) {
                badge.classList.add("expired"); // 👉 CSS에서 스타일 조절 가능
            }


            // 회사 소개
            document.querySelector(".job-desc").textContent = data.companyIntro;

            // 연락처
            const contactInfo = document.querySelector(".contact-info");
            contactInfo.innerHTML = `
                <p><img src="/images/email.png"> ${data.email}</p>
                <p><img src="/images/phone.png"> ${data.phone}</p>
            `;

            // 조건 목록 채우기
            const list = document.getElementById("condition-list");
            list.innerHTML = `
                <li><strong>경력:</strong> ${data.career}</li>
                <li><strong>학력:</strong> ${data.education}</li>
                <li><strong>고용형태:</strong> ${data.employmentTypes}</li>
                <li><strong>급여:</strong> ${data.salary}</li>
                <li><strong>시간:</strong> ${data.time}</li>
                <li><strong>우대:</strong> ${data.preference}</li>
            `;
            // 제출 서류 동적 출력
            const documentsSection = document.querySelector(".documents");
            documentsSection.innerHTML = `
    <h3>제출서류</h3>
    <p>${data.surew || '제출 서류 정보 없음'}</p>
    <p class="notice">* 필수 제출 안내 확인</p>
`;


            // 👉 이력서 양식 리스트 추가
            const resumeSection = document.querySelector(".resume-template");
            const resumeListContainer = document.createElement("div"); // 여러 개 담을 div

            data.resumeTitles.forEach(resume => {
                const item = document.createElement("div");
                item.className = "resume-item";
                item.textContent = `📄 ${resume.title}`;  // ✨ 제목만 출력되도록 수정
                item.onclick = () => openResumeModal(resume.title); // 모달에도 제목만 전달
                resumeListContainer.appendChild(item);
            });


            resumeSection.appendChild(resumeListContainer);





        })
        .catch(err => {
            console.error("상세 공고 불러오기 실패:", err);
        });

    // ✅ 삭제 버튼 클릭 시 DELETE 요청 보내기
    document.querySelector(".delete").addEventListener("click", () => {
        const jobId = new URLSearchParams(window.location.search).get("id");

        console.log("🟡 삭제하려는 공고 ID:", jobId);  // ✅ 로그 확인!

        if (!confirm("정말로 이 공고를 삭제하시겠습니까?")) {
            return;
        }

        fetch(`/api/cojobs/${jobId}`, {
            method: "DELETE",
            headers: {
                "X-XSRF-TOKEN": getCsrfToken()
            }
        })
            .then(res => {
                console.log("🟢 서버 응답 상태:", res.status); // ✅ 상태코드 로그
                if (res.ok) {
                    alert("공고가 성공적으로 삭제되었습니다.");
                    window.location.href = "/job2";
                } else {
                    return res.text().then(msg => {
                        console.error("❌ 삭제 실패 메시지:", msg);
                        alert("삭제 실패: " + msg);
                    });
                }
            })
            .catch(error => {
                console.error("🔥 삭제 중 오류 발생:", error);
                alert("오류 발생: " + error.message);
            });
    });


});
