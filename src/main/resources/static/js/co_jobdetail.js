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

            data.resumeTitles.forEach(title => {
                const item = document.createElement("div");
                item.className = "resume-item";
                item.textContent = `📄 ${title}`;
                item.onclick = () => openResumeModal(title);
                resumeListContainer.appendChild(item);
            });

            resumeSection.appendChild(resumeListContainer);





        })
        .catch(err => {
            console.error("상세 공고 불러오기 실패:", err);
        });
});
