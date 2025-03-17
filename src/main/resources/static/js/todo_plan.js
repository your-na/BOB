document.addEventListener("DOMContentLoaded", function() {
    const calendarBody = document.getElementById("calendar-body");
    const currentMonth = document.getElementById("current-month");
    const prevMonthBtn = document.getElementById("prev-month");
    const nextMonthBtn = document.getElementById("next-month");
    let selectedDate = null; // 선택된 날짜 저장
    let month = 11; // 12월 (0부터 시작)
    let year = 2024;

    function renderCalendar() {
        calendarBody.innerHTML = "";
        let firstDay = new Date(year, month, 1).getDay();
        let daysInMonth = new Date(year, month + 1, 0).getDate();
        let row = document.createElement("tr");

        for (let i = 0; i < firstDay; i++) {
            row.appendChild(document.createElement("td"));
        }

        for (let day = 1; day <= daysInMonth; day++) {
            let cell = document.createElement("td");
            cell.textContent = day;
            cell.classList.add("calendar-cell");

            // 날짜 클릭 시 선택된 날짜 표시
            cell.addEventListener("click", function() {
                if (selectedDate) {
                    selectedDate.classList.remove("selected");
                }
                cell.classList.add("selected");
                selectedDate = cell;
                console.log(`선택된 날짜: ${year}-${month + 1}-${day}`);
            });

            row.appendChild(cell);

            if ((firstDay + day) % 7 === 0) {
                calendarBody.appendChild(row);
                row = document.createElement("tr");
            }
        }

        calendarBody.appendChild(row);
        currentMonth.textContent = `${year}년 ${month + 1}월`;
    }

    prevMonthBtn.addEventListener("click", function() {
        month--;
        if (month < 0) {
            month = 11;
            year--;
        }
        renderCalendar();
    });

    nextMonthBtn.addEventListener("click", function() {
        month++;
        if (month > 11) {
            month = 0;
            year++;
        }
        renderCalendar();
    });

    renderCalendar();
});
