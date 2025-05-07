document.addEventListener("DOMContentLoaded", function () {
    const monthSelect = document.getElementById("month-select");

    monthSelect.addEventListener("change", function () {
        const selectedMonth = this.value;
        alert(`${selectedMonth} 월 통계는 준비 중입니다.`);
        // TODO: 실제 AJAX 호출 등으로 동적 로딩 가능
    });
});
