// 📊 채용 통계 API 불러와서 페이지에 반영
function fetchCompanyStats(month) {
    let url = '/api/cojobs/statistics/company';
    if (month) {
        url += `?month=${month}`;
    }

    fetch(url)
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
    loadMonthOptions();
    fetchCompanyStats();

    // 📅 월별 필터 옵션 동적 생성 함수 (여기에 붙여넣기)
    function loadMonthOptions() {
        fetch('/api/cojobs/statistics/months')
            .then(response => response.json())
            .then(months => {
                const monthSelect = document.getElementById('month-select');
                monthSelect.innerHTML = ''; // 기존 옵션 초기화

                // 전체 기간 옵션 추가
                const allOption = document.createElement('option');
                allOption.value = '';
                allOption.innerText = '전체 기간';
                monthSelect.appendChild(allOption);

                // 서버에서 받아온 월별 옵션 추가
                months.forEach(month => {
                    const option = document.createElement('option');
                    option.value = month;
                    const [year, mon] = month.split('-');
                    option.innerText = `${year}년 ${parseInt(mon, 10)}월`;
                    monthSelect.appendChild(option);
                });
            })
            .catch(err => {
                console.error('📛 월별 옵션 불러오기 실패:', err);
            });
    }
    // 월별 선택 시 현재는 알림만 (추후 기능 연결 가능)
    const monthSelect = document.getElementById("month-select");
    monthSelect.addEventListener("change", function () {
        const selectedMonthStr = this.value; // 예: "2025-06" 또는 "" (전체 기간)
        if (selectedMonthStr) {
            // "2025-06" -> 6 (월 숫자만 추출)
            const monthNum = parseInt(selectedMonthStr.split('-')[1], 10);
            fetchCompanyStats(monthNum);
        } else {
            // 전체 기간 선택 시 null 넘김
            fetchCompanyStats(null);
        }
    });


});
