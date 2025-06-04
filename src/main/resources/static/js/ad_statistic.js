// 📊 전역 변수로 차트 보관
let generalChart, companyChart, jobSuccessChart;

// ✅ 관리자 회원 수 통계
function loadAdminStatistics() {
    fetch('/api/admin/statistics')
        .then(response => response.json())
        .then(data => {
            document.getElementById('totalUsersCount').innerText = data.totalUsers.toLocaleString() + '명';
            document.getElementById('generalUsersCount').innerText = data.generalMemberCount.toLocaleString() + '명';
            document.getElementById('companyUsersCount').innerText = data.companyMemberCount.toLocaleString() + '명';
        })
        .catch(err => {
            console.error('관리자 통계 불러오기 실패:', err);
        });
}

// ✅ 구직 성공률 차트 로딩
function loadJobSuccessRate() {
    fetch('/api/admin/statistics/job-success-rate')
        .then(response => response.json())
        .then(rate => {
            const failRate = 100 - rate;
            jobSuccessChart.data.datasets[0].data = [rate, failRate];
            jobSuccessChart.options.plugins.doughnutlabel.labels[0].text = rate.toFixed(2) + '%';
            jobSuccessChart.update();
        })
        .catch(err => {
            console.error('구직 성공률 불러오기 실패:', err);
        });
}

// ✅ 활동률 차트 로딩
function loadActiveMemberRate() {
    fetch('/api/admin/statistics/active-members')
        .then(response => response.json())
        .then(data => {
            const { activeGeneralMembers, totalGeneralMembers, activeCompanyMembers, totalCompanyMembers } = data;
            const inactiveGeneral = totalGeneralMembers - activeGeneralMembers;
            const inactiveCompany = totalCompanyMembers - activeCompanyMembers;

            // 일반 회원 차트
            if (generalChart) {
                generalChart.data.datasets[0].data = [activeGeneralMembers, inactiveGeneral];
                generalChart.update();
            } else {
                generalChart = new Chart(document.getElementById("generalMemberChart"), {
                    type: 'doughnut',
                    data: {
                        labels: ["활동", "비활동"],
                        datasets: [{
                            data: [activeGeneralMembers, inactiveGeneral],
                            backgroundColor: ["#FFEB3B", "#e0e0e0"],
                            borderWidth: 0
                        }]
                    },
                    options: {
                        cutout: "70%",
                        plugins: {
                            tooltip: { enabled: false },
                            legend: { display: false },
                            doughnutlabel: {
                                labels: [{
                                    text: ((activeGeneralMembers / totalGeneralMembers) * 100).toFixed(1) + '%',
                                    font: { size: 18, weight: 'bold' }
                                }]
                            }
                        }
                    },
                    plugins: [doughnutLabelPlugin]
                });
            }

            // 기업 회원 차트
            if (companyChart) {
                companyChart.data.datasets[0].data = [activeCompanyMembers, inactiveCompany];
                companyChart.update();
            } else {
                companyChart = new Chart(document.getElementById("companyMemberChart"), {
                    type: 'doughnut',
                    data: {
                        labels: ["활동", "비활동"],
                        datasets: [{
                            data: [activeCompanyMembers, inactiveCompany],
                            backgroundColor: ["#2196F3", "#e0e0e0"],
                            borderWidth: 0
                        }]
                    },
                    options: {
                        cutout: "70%",
                        plugins: {
                            tooltip: { enabled: false },
                            legend: { display: false },
                            doughnutlabel: {
                                labels: [{
                                    text: ((activeCompanyMembers / totalCompanyMembers) * 100).toFixed(1) + '%',
                                    font: { size: 18, weight: 'bold' }
                                }]
                            }
                        }
                    },
                    plugins: [doughnutLabelPlugin]
                });
            }
        })
        .catch(err => {
            console.error('활동률 불러오기 실패:', err);
        });
}

// ✅ 중앙 퍼센트 텍스트 플러그인
const doughnutLabelPlugin = {
    id: 'doughnutlabel',
    beforeDraw: function(chart) {
        const width = chart.width;
        const height = chart.height;
        const ctx = chart.ctx;
        ctx.restore();
        const text = chart.options.plugins.doughnutlabel.labels[0].text;
        ctx.font = '20px sans-serif';
        ctx.textBaseline = 'middle';
        const textX = Math.round((width - ctx.measureText(text).width) / 2);
        const textY = height / 2;
        ctx.fillText(text, textX, textY);
        ctx.save();
    }
};

// ✅ 구직 성공률 차트 초기화
jobSuccessChart = new Chart(document.getElementById("jobSuccessChart"), {
    type: 'doughnut',
    data: {
        labels: ["성공률", "실패"],
        datasets: [{
            data: [0, 100],
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
                labels: [{ text: "0%", font: { size: 20, weight: 'bold' } }]
            }
        }
    },
    plugins: [doughnutLabelPlugin]
});

// 🚀 페이지 로딩 시 실행
document.addEventListener('DOMContentLoaded', () => {
    loadAdminStatistics();
    loadJobSuccessRate();
    loadActiveMemberRate();
});
