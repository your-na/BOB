let jobPostId = null;
document.addEventListener("DOMContentLoaded", function () {
    jobPostId = new URLSearchParams(window.location.search).get("jobPostId");
    if (!jobPostId) {
        alert("❌ 공고 ID가 없습니다.");
        return;
    }

    // 지원자 목록 가져오기
    fetch(`/api/applications/jobpost/${jobPostId}/applicants`)
        .then(res => res.json())
        .then(data => {
            console.log("✅ 지원자 목록:", data);

            // 지원자 수 표시
            document.getElementById("applicant-count").textContent = data.length;

            // 테이블 바디 영역
            const tbody = document.getElementById("applicant-tbody");
            tbody.innerHTML = "";

            // 각 지원자 정보를 테이블에 추가
            data.forEach(applicant => {
                const row = document.createElement("tr");
                row.innerHTML = `
    <td>${applicant.userName}</td>
    <td>${applicant.appliedAt}</td>
    <td>
        <button class="view-resume-btn"
            data-resume-id="${applicant.resumeId ?? ''}"
            data-my-resume-id="${applicant.myResumeId ?? ''}">
            이력서 열기
        </button>
    </td>
`;

                const btn = row.querySelector(".view-resume-btn");
                btn.addEventListener("click", function () {
                    // 문자열 ''이면 null로 처리, 값이 있으면 숫자로 변환
                    let resumeId = this.dataset.resumeId === '' ? null : Number(this.dataset.resumeId);
                    let myResumeId = this.dataset.myResumeId === '' ? null : Number(this.dataset.myResumeId);

                    viewResume(resumeId, myResumeId);
                });


                tbody.appendChild(row);
            });
        })
        .catch(err => {
            console.error("❌ 지원자 목록 불러오기 실패:", err);
            alert("지원자 정보를 불러오지 못했습니다.");
        });

    // 공고 정보 가져오기
    fetch(`/api/cojobs/${jobPostId}`)
        .then(res => res.json())
        .then(post => {
            console.log("📄 공고 정보:", post);
            document.getElementById("job-title").textContent = post.title;
            document.getElementById("job-period").textContent = `${post.startDate} ~ ${post.endDate}`;
        })
        .catch(err => {
            console.error("❌ 공고 정보 불러오기 실패:", err);
        });
});

function viewResume(resumeId, myResumeId) {
    // 문자열 'null' 처리 + null/undefined 처리
    resumeId = (resumeId === null || resumeId === 'null') ? null : resumeId;
    myResumeId = (myResumeId === null || myResumeId === 'null') ? null : myResumeId;

    console.log("📌 클릭 시 resumeId:", resumeId, "myResumeId:", myResumeId);

    if (myResumeId) {
        // 나만의 이력서 상세보기
        window.location.href = `/myresume/${myResumeId}`;
    } else if (resumeId) {
        // 기업 이력서 상세보기
        window.open(`/resume/detail?jobPostId=${jobPostId}&resumeId=${resumeId}`, '_blank'); // 새 탭에서 열기

    } else {
        alert("이력서 정보가 없습니다.");
    }
}



