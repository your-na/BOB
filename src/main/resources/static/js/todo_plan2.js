// ✅ 쿠키에서 CSRF 토큰 꺼내는 함수
function getCookie(name) {
    const value = document.cookie
        .split("; ")
        .find((row) => row.startsWith(name + "="));
    return value ? decodeURIComponent(value.split("=")[1]) : null;
}

document.addEventListener("DOMContentLoaded", function () {
    const calendarBody = document.getElementById("calendar-body");
    const currentMonth = document.getElementById("current-month");
    const prevMonthBtn = document.getElementById("prev-month");
    const nextMonthBtn = document.getElementById("next-month");
    const todoList = document.querySelector(".todo-list");
    const taskTitle = document.querySelector(".task-date-title");
    const teamId = window.teamId;

    // ✅ 사이드바 버튼 동작 추가
    const sidebarButtons = document.querySelectorAll('.sidebar-btn');
    sidebarButtons.forEach((btn) => {
        btn.addEventListener("click", () => {
            const text = btn.textContent.trim();
            if (text === "홈") {
                window.location.href = `/contesthome/${teamId}`;
            } else if (text === "WBS") {
                window.location.href = `/todocrud/${teamId}`;
            }
        });
    });

    let selectedDate = null;
    const today = new Date();
    let month = today.getMonth();
    let year = today.getFullYear();

    function renderCalendar() {
        calendarBody.innerHTML = "";
        let firstDay = new Date(year, month, 1).getDay();
        let daysInMonth = new Date(year, month + 1, 0).getDate();
        let row = document.createElement("tr");

        for (let i = 0; i < firstDay; i++) row.appendChild(document.createElement("td"));

        for (let day = 1; day <= daysInMonth; day++) {
            let cell = document.createElement("td");
            cell.textContent = day;
            cell.classList.add("calendar-cell");

            cell.addEventListener("click", function () {
                if (selectedDate) selectedDate.classList.remove("selected");
                cell.classList.add("selected");
                selectedDate = cell;

                const clickedDate = `${year}-${(month + 1).toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`;
                console.log("선택된 날짜:", clickedDate);

                fetch(`/api/todos/contest?date=${clickedDate}&teamId=${teamId}`)
                    .then(res => res.json())
                    .then(data => {
                        todoList.innerHTML = "";
                        taskTitle.textContent = `${month + 1}월 ${day}일 할 일`;

                        // ✅ 날짜 포함 여부 판단 함수
                        function isDateInRange(clickedDateStr, startDateStr, endDateStr) {
                            const clicked = new Date(clickedDateStr);
                            const start = new Date(startDateStr);
                            const end = new Date(endDateStr);
                            clicked.setHours(0, 0, 0, 0);
                            start.setHours(0, 0, 0, 0);
                            end.setHours(0, 0, 0, 0);
                            return clicked >= start && clicked <= end;
                        }

                        // ✅ 해당 날짜에 포함되는 할 일만 필터링
                        const filteredTodos = data.filter(todo =>
                            isDateInRange(clickedDate, todo.startDate, todo.endDate)
                        );

                        if (filteredTodos.length === 0) {
                            todoList.innerHTML = "<li>할 일이 없습니다.</li>";
                            return;
                        }

                        filteredTodos.forEach(todo => {
                            const li = document.createElement("li");
                            const checkbox = document.createElement("input");
                            checkbox.type = "checkbox";
                            checkbox.checked = todo.completed;

                            checkbox.addEventListener("change", function () {
                                fetch(`/api/todos/${todo.id}/complete`, {
                                    method: "PATCH",
                                    headers: {
                                        "Content-Type": "application/json",
                                        "X-XSRF-TOKEN": getCookie("XSRF-TOKEN")
                                    },
                                    credentials: "include",
                                    body: JSON.stringify({ completed: checkbox.checked })
                                }).catch(err => {
                                    alert("오류 발생: " + err);
                                    checkbox.checked = !checkbox.checked;
                                });
                            });

                            const titleSpan = document.createElement("span");
                            titleSpan.textContent = ` ${todo.title} `;

                            const ddaySpan = document.createElement("span");
                            ddaySpan.classList.add("d-day");
                            ddaySpan.textContent = `D-${getDday(todo.endDate)}`;

                            li.appendChild(checkbox);
                            li.appendChild(titleSpan);
                            li.appendChild(ddaySpan);
                            todoList.appendChild(li);
                        });
                    })
                    .catch(err => {
                        console.error("할 일 조회 실패:", err);
                        alert("할 일을 불러오는 중 오류가 발생했습니다.");
                    });
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


    function getDday(dateStr) {
        const today = new Date();
        const target = new Date(dateStr);
        const diffTime = target - today;
        const diffDay = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDay >= 0 ? diffDay : 0;
    }

    prevMonthBtn.addEventListener("click", () => { month--; if (month < 0) { month = 11; year--; } renderCalendar(); });
    nextMonthBtn.addEventListener("click", () => { month++; if (month > 11) { month = 0; year++; } renderCalendar(); });

    renderCalendar();
    loadTeamSpaces();
});

// ✅ 할 일 등록 처리
document.addEventListener("DOMContentLoaded", () => {
    const addTaskBtn = document.querySelector(".add-task-btn");
    const taskModal = document.querySelector(".task-modal");
    const cancelBtn = document.querySelector(".cancel-task-btn");
    const submitBtn = document.querySelector(".submit-task-btn");
    const taskInput = document.querySelector(".task-input");
    const startDateInput = document.querySelector(".start-date");
    const endDateInput = document.querySelector(".end-date");

    addTaskBtn.addEventListener("click", () => taskModal.classList.remove("hidden"));
    cancelBtn.addEventListener("click", () => taskModal.classList.add("hidden"));

    submitBtn.addEventListener("click", () => {
        const title = taskInput.value.trim();
        const date = startDateInput.value;
        const endDate = endDateInput.value;
        const csrfToken = getCookie("XSRF-TOKEN");

        const selectedOption = document.querySelector(".space-select").selectedOptions[0];
        const selected = selectedOption.value;
        const selectedGroupLabel = selectedOption.parentElement.label;

        if (!title || !date || !endDate) {
            alert("할 일 제목과 날짜를 모두 입력해주세요!");
            return;
        }

        const todoData = {
            title: title,
            startDate: date,
            endDate: endDate,
            assignee: document.querySelector(".member-select").value,
            workspace: selected,
            type: selected === "개인" ? "개인" : (
                selectedGroupLabel === "공모전 팀" ? "공모전" : "프로젝트"
            ),
            targetId: selectedGroupLabel === "공모전 팀" ? window.teamId : null
        };

        fetch("/api/todos", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": csrfToken
            },
            credentials: "include",
            body: JSON.stringify(todoData)
        })
            .then(res => {
                if (!res.ok) throw new Error("할 일 저장 실패");
                return res.json();
            })
            .then(() => {
                taskModal.classList.add("hidden");
                taskInput.value = "";
                startDateInput.value = "";
                endDateInput.value = "";
            })
            .catch((err) => {
                console.error(err);
                alert("저장 중 오류가 발생했습니다.");
            });
    });
});

// ✅ 공모전 팀원 목록 불러오기
function loadTeamSpaces() {
    const spaceSelect = document.querySelector(".space-select");
    const memberSelect = document.querySelector(".member-select");

    spaceSelect.innerHTML = "";

    // 공모전은 스페이스 고정
    spaceSelect.innerHTML += `<optgroup label="개인"><option value="개인">개인</option></optgroup>`;

    // ✅ 공모전 팀
    fetch("/api/todos/my-contest-teams", { credentials: "include" })
        .then(res => res.json())
        .then(contestTeams => {
            if (contestTeams.length > 0) {
                const group = document.createElement("optgroup");
                group.label = "공모전 팀";
                contestTeams.forEach(team => {
                    const option = document.createElement("option");
                    option.value = team.teamName;
                    option.textContent = team.contestTitle;
                    group.appendChild(option);
                });
                spaceSelect.appendChild(group);
            }
        });

    // ✅ 프로젝트 팀
    fetch("/api/todos/my-projects", { credentials: "include" })
        .then(res => res.json())
        .then(projects => {
            if (projects.length > 0) {
                const group = document.createElement("optgroup");
                group.label = "프로젝트 팀";
                projects.forEach(proj => {
                    const option = document.createElement("option");
                    option.value = proj.title;
                    option.textContent = proj.title;
                    group.appendChild(option);
                });
                spaceSelect.appendChild(group);
            }
        });

    spaceSelect.addEventListener("change", () => {
        const selected = spaceSelect.value;

        // 개인
        if (selected === "개인") {
            memberSelect.innerHTML = `<option value="나">나</option>`;
            return;
        }

        // 공모전 or 프로젝트 → 담당자 조회
        fetch(`/api/todos/members?workspace=${encodeURIComponent(selected)}`, {
            credentials: "include"
        })
            .then(res => res.json())
            .then(data => {
                const { creator, currentUser, members } = data;
                memberSelect.innerHTML = "";

                if (creator === currentUser) {
                    memberSelect.innerHTML += `<option value="공동">공동</option>`;
                }

                memberSelect.innerHTML += `<option value="나">나</option>`;

                members.forEach(nick => {
                    if (nick !== currentUser) {
                        memberSelect.innerHTML += `<option value="${nick}">${nick}</option>`;
                    }
                });
            });
    });
}
