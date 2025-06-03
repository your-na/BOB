// 관리자 통계 API에서 회원 수 받아서 화면에 반영하는 함수
function loadAdminStatistics() {
    fetch('/api/admin/statistics')
        .then(response => response.json())
        .then(data => {
            // 받아온 데이터로 회원 수 영역을 바꿈
            document.getElementById('totalUsersCount').innerText = data.totalUsers.toLocaleString() + '명';
            document.getElementById('generalUsersCount').innerText = data.generalMemberCount.toLocaleString() + '명';
            document.getElementById('companyUsersCount').innerText = data.companyMemberCount.toLocaleString() + '명';
        })
        .catch(err => {
            console.error('관리자 통계 불러오기 실패:', err);
        });
}

// 페이지 로드되면 관리자 통계 데이터 가져오기 실행
document.addEventListener('DOMContentLoaded', () => {
    loadAdminStatistics();
    loadJobSuccessRate();

});

// 구직 성공률 API에서 데이터 받아서 차트 업데이트 함수
function loadJobSuccessRate() {
    fetch('/api/admin/statistics/job-success-rate')
        .then(response => response.json())
        .then(rate => {
            // 차트 데이터 업데이트
            const failRate = 100 - rate;
            jobSuccessChart.data.datasets[0].data = [rate, failRate];

            // 차트 도넛 중앙 텍스트 업데이트 (커스텀 플러그인 이용 중)
            jobSuccessChart.options.plugins.doughnutlabel.labels[0].text = rate.toFixed(2) + '%';

            jobSuccessChart.update();
        })
        .catch(err => {
            console.error('구직 성공률 불러오기 실패:', err);
        });
}

// 구직 성공률 차트
const jobSuccessChart = new Chart(document.getElementById("jobSuccessChart"), {
    type: 'doughnut',
    data: {
        labels: ["성공률", "실패"],
        datasets: [{
            data: [0, 100],  // 초기값 0% 성공률, 100% 실패
            backgroundColor: ["#4CAF50", "#e0e0e0"],
            borderWidth: 0
        }]
    },
    options: {
        cutout: "70%",
        plugins: {
            tooltip: { enabled: false },
            legend: { display: false },
            doughnutlabel: {
                labels: [{ text: "0%", font: { size: 20, weight: 'bold' } }]  // 초기 텍스트 0%
            }
        }
    },
    plugins: [{
        id: 'doughnutlabel',
        beforeDraw: function(chart) {
            const width = chart.width, height = chart.height;
            const ctx = chart.ctx;
            ctx.restore();
            const fontSize = 20;
            ctx.font = `${fontSize}px sans-serif`;
            ctx.textBaseline = "middle";

            // 여기서 텍스트를 차트 옵션의 현재 텍스트로 읽어옴 (동적 업데이트 반영)
            const text = chart.options.plugins.doughnutlabel.labels[0].text;

            const textX = Math.round((width - ctx.measureText(text).width) / 2);
            const textY = height / 2;
            ctx.fillText(text, textX, textY);
            ctx.save();
        }
    }]
});


// 일반 회원 활동률
new Chart(document.getElementById("generalMemberChart"), {
    type: 'doughnut',
    data: {
        labels: ["활동", "비활동"],
        datasets: [{
            data: [80, 20],
            backgroundColor: ["#FFEB3B", "#e0e0e0"],
            borderWidth: 0
        }]
    },
    options: {
        cutout: "70%",
        plugins: {
            tooltip: { enabled: false },
            legend: { display: false }
        }
    }
});

// 기업 회원 활동률
new Chart(document.getElementById("companyMemberChart"), {
    type: 'doughnut',
    data: {
        labels: ["활동", "비활동"],
        datasets: [{
            data: [63, 37],
            backgroundColor: ["#2196F3", "#e0e0e0"],
            borderWidth: 0
        }]
    },
    options: {
        cutout: "70%",
        plugins: {
            tooltip: { enabled: false },
            legend: { display: false }
        }


    }


});
