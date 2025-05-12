// 구직 성공률 차트
const jobSuccessChart = new Chart(document.getElementById("jobSuccessChart"), {
    type: 'doughnut',
    data: {
        labels: ["성공률", "실패"],
        datasets: [{
            data: [73, 27],
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
                labels: [{ text: "73%", font: { size: 20, weight: 'bold' } }]
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
            const text = "73%";
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
