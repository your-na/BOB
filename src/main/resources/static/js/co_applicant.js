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
        <button class="view-resume-btn" onclick="viewResume(${applicant.resumeId})">이력서 열기</button>
    </td>
`;

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

function viewResume(resumeId) {
    if (!resumeId) {
        alert("이력서 ID가 없습니다.");
        return;
    }

    // ✅ 이력서 상세 페이지로 이동
    location.href = `/resume/detail?jobPostId=${jobPostId}&resumeId=${resumeId}`;

}
