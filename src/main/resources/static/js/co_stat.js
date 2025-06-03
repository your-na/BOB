// 📊 채용 통계 API 불러와서 페이지에 반영
function fetchCompanyStats() {
    fetch('/api/cojobs/statistics/company')
        .then(response => response.json())
        .then(data => {
            // ✅ 요약 통계 표시
            document.getElementById('total-job').innerText = data.totalJobCount;
            document.getElementById('total-applicant').innerText = data.totalApplicants;
            document.getElementById('total-accepted').innerText = data.totalAccepted;
            document.getElementById('total-rejected').innerText = data.totalRejected;
            document.getElementById('total-canceled').innerText = data.totalCanceled;

            // ✅ 공고별 통계 리스트 비우고 다시 채움
            const list = document.querySelector('.chart-list');
            list.innerHTML = '';

            data.jobSummaries.forEach(item => {
                const li = document.createElement('li');
                li.innerHTML = `
    <span class="job-title clickable-title" data-job-id="${item.jobId}">
        ${item.title}
    </span>
    <span class="status">지원자 ${item.applicantCount}명 / 채용 ${item.acceptedCount}명</span>
`;
                list.appendChild(li);
            });

            // 🖱️ 제목 클릭 시 상세 페이지 이동
            document.querySelectorAll('.clickable-title').forEach(el => {
                el.addEventListener('click', function () {
                    const jobPostId = this.getAttribute('data-job-id');
                    // ✅ 뷰 페이지로 이동
                    window.location.href = `/jobdetail?id=${jobPostId}`;
                });
            });

        })
        .catch(err => {
            console.error('📛 채용 통계 불러오기 실패:', err);
        });
}

// ✅ 페이지 로드 시 실행
document.addEventListener("DOMContentLoaded", function () {
    fetchCompanyStats();

    // 월별 선택 시 현재는 알림만 (추후 기능 연결 가능)
    const monthSelect = document.getElementById("month-select");
    monthSelect.addEventListener("change", function () {
        const selectedMonth = this.value;
        alert(`${selectedMonth} 월 통계는 아직 준비 중입니다 🛠️`);
    });
});
