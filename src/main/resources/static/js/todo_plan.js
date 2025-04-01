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

    let selectedDate = null;
    let month = 11;
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

            cell.addEventListener("click", function () {
                if (selectedDate) {
                    selectedDate.classList.remove("selected");
                }
                cell.classList.add("selected");
                selectedDate = cell;

                const clickedDate = `${year}-${(month + 1).toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`;
                console.log("선택된 날짜:", clickedDate);

                fetch(`http://localhost:8888/api/todos?date=${clickedDate}`)
                    .then((res) => res.json())
                    .then((data) => {
                        console.log("📦 불러온 할 일 전체:", data);
                        todoList.innerHTML = "";
                        if (taskTitle) {
                            taskTitle.textContent = `${month + 1}월 ${day}일 할 일`;
                        }

                        if (data.length === 0) {
                            todoList.innerHTML = "<li>할 일이 없습니다.</li>";
                            return;
                        }

                        data.forEach(todo => {
                            console.log("📝 개별 todo:", todo);
                            const li = document.createElement("li");
                            const checkbox = document.createElement("input");
                            checkbox.type = "checkbox";
                            checkbox.checked = todo.completed;

                            checkbox.addEventListener("change", function () {
                                console.log("📦 체크박스 변경됨 - ID:", todo.id, "✅ 상태:", checkbox.checked);
                                fetch(`http://localhost:8888/api/todos/${todo.id}/complete`, {
                                    method: "PATCH",
                                    headers: {
                                        "Content-Type": "application/json",
                                        "X-XSRF-TOKEN": getCookie("XSRF-TOKEN")
                                    },
                                    credentials: "include",
                                    body: JSON.stringify({ completed: checkbox.checked })
                                }).then(res => {
                                    if (!res.ok) {
                                        alert("상태 업데이트 실패");
                                        checkbox.checked = !checkbox.checked;
                                    }
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

    prevMonthBtn.addEventListener("click", function () {
        month--;
        if (month < 0) {
            month = 11;
            year--;
        }
        renderCalendar();
    });

    nextMonthBtn.addEventListener("click", function () {
        month++;
        if (month > 11) {
            month = 0;
            year++;
        }
        renderCalendar();
    });

    renderCalendar();
    loadMyProjects();
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

    addTaskBtn.addEventListener("click", () => {
        taskModal.classList.remove("hidden");
    });

    cancelBtn.addEventListener("click", () => {
        taskModal.classList.add("hidden");
    });

    submitBtn.addEventListener("click", () => {
        const title = taskInput.value.trim();
        const date = startDateInput.value;
        const endDate = endDateInput.value;
        const csrfToken = getCookie("XSRF-TOKEN");

        if (!title || !date || !endDate) {
            alert("할 일 제목과 날짜를 모두 입력해주세요!");
            return;
        }

        const todoData = {
            title: title,
            startDate: date,
            endDate: endDate,
            assignee: document.querySelector(".member-select").value,   // ✅ 담당자
            workspace: document.querySelector(".space-select").value    // ✅ 스페이스
        };

        fetch("http://localhost:8888/api/todos", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": csrfToken
            },
            credentials: "include",
            body: JSON.stringify(todoData)
        })
            .then((res) => {
                if (!res.ok) {
                    throw new Error("할 일 저장 실패");
                }
                return res.json();
            })
            .then((data) => {
                console.log("할 일 저장 완료:", data);
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

// ✅ 프로젝트 목록 불러오기 (완료된 프로젝트 제외)
function loadMyProjects() {
    const spaceSelect = document.querySelector(".space-select");

    fetch("http://localhost:8888/api/my-projects", {
        method: "GET",
        credentials: "include"
    })
        .then((res) => res.json())
        .then((projects) => {
            // "완료" 상태인 프로젝트를 제외
            const filteredProjects = projects.filter(project => project.status !== "완료");

            spaceSelect.innerHTML = "";

            filteredProjects.forEach((project) => {
                const option = document.createElement("option");
                option.value = project.title;
                option.textContent = project.title;
                spaceSelect.appendChild(option);
            });

            const personalOption = document.createElement("option");
            personalOption.value = "개인";
            personalOption.textContent = "개인";
            spaceSelect.appendChild(personalOption);

            spaceSelect.dispatchEvent(new Event("change"));
        })
        .catch((err) => {
            console.error("프로젝트를 불러오는 중 오류가 발생했습니다.", err);
            alert("프로젝트를 불러오는 중 오류가 발생했습니다.");
        });
}


// ✅ 담당자 목록 동적으로 변경
// ✅ 담당자 목록 동적으로 변경
document.addEventListener("DOMContentLoaded", () => {
    const spaceSelect = document.querySelector(".space-select");
    const memberSelect = document.querySelector(".member-select");

    spaceSelect.addEventListener("change", () => {
        const selectedTitle = spaceSelect.value;

        console.log("✅ 선택된 프로젝트 제목:", selectedTitle);

        if (selectedTitle === "개인") {
            memberSelect.innerHTML = `<option value="나">나</option>`;
            return;
        }

        fetch(`http://localhost:8888/api/project-members?title=${selectedTitle}`, {
            method: "GET",
            credentials: "include"
        })
            .then((res) => res.json())
            .then((data) => {
                console.log("📦 서버로부터 받은 data:", data);

                const creator = data.creator;
                const currentUser = data.currentUser;
                const members = data.members;

                console.log("👑 주최자:", creator);
                console.log("🙋 현재 로그인 유저:", currentUser);
                console.log("👥 팀원 목록:", members);

                // ✅ 기존 옵션 초기화
                memberSelect.innerHTML = "";

                // ✅ 주최자일 때만 '공동' 옵션 추가
                if (creator === currentUser) {
                    memberSelect.innerHTML += `<option value="공동">공동</option>`;
                }

                // ✅ '나'는 항상 추가
                memberSelect.innerHTML += `<option value="나">나</option>`;

                // ✅ 주최자일 경우 팀원 추가
                if (creator === currentUser) {
                    members.forEach((member) => {
                        if (member !== currentUser) {
                            memberSelect.innerHTML += `<option value="${member}">${member}</option>`;
                        }
                    });
                }
            })
            .catch((err) => {
                console.error("❌ 팀원 목록 로딩 실패:", err);
            });
    });
});
