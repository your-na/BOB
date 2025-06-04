let currentUserNick = "";  // 🔑 전역으로 이동!

// ✅ 쿠키에서 CSRF 토큰 꺼내는 함수
function getCookie(name) {
    const value = document.cookie
        .split("; ")
        .find(row => row.startsWith(name + "="));
    return value ? decodeURIComponent(value.split("=")[1]) : null;
}


document.addEventListener("DOMContentLoaded", function () {

    fetch("http://localhost:8888/api/user/me", {
        credentials: "include"
    })
        .then(res => res.json())
        .then(data => {
            currentUserNick = data.userNick;
            console.log("✅ 로그인된 사용자 닉네임:", currentUserNick);
        })
        .catch(err => {
            console.error("❌ 사용자 정보 로딩 실패:", err);
            alert("사용자 정보를 불러오지 못했습니다.");
        });

    var filter = document.getElementById("task-filter");

    // ✅ 이 부분 추가!
    const openModalBtn = document.getElementById("openTodoModal");
    if (openModalBtn) {
        openModalBtn.addEventListener("click", openTodoModal);
    }

    filter.addEventListener("change", function () {
        var category = filter.value;
        var tasks = document.querySelectorAll(".task-item");

        tasks.forEach(task => {
            if (category === "전체" || task.getAttribute("data-category") === category) {
                task.style.display = "flex";
            } else {
                task.style.display = "none";
            }
        });
    });

    // ✅ 팝업 할 일 불러오기
    loadPopupTodos();
});

document.addEventListener("DOMContentLoaded", function () {
    var filter = document.getElementById("task-filter");

    const openModalBtn = document.getElementById("openTodoModal");
    if (openModalBtn) {
        openModalBtn.addEventListener("click", openTodoModal);
    }

    filter.addEventListener("change", function () {
        // ...
    });

    // ✅ 팝업 할 일 불러오기
    loadPopupTodos();

    // ✅ 이 아래에 추가!!
    loadMyProjectsForPopup();
});


// ✅ D-day 계산 함수 재사용
function getDday(dateStr) {
    const today = new Date();
    const target = new Date(dateStr);
    const diffTime = target - today;
    const diffDay = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDay >= 0 ? `D-${diffDay}` : `D+${Math.abs(diffDay)}`;
}

// ✅ 서버에서 나에게 할당된 할 일 불러오기
function loadPopupTodos() {
    fetch("http://localhost:8888/api/todos/popup", {
        method: "GET",
        credentials: "include"
    })
        .then(res => res.json())
        .then(data => {
            console.log("📌 팝업 할 일:", data);
            const popupList = document.querySelector(".popup-todo-list");
            popupList.innerHTML = ""; // 기존 내용 초기화

            if (data.length === 0) {
                popupList.innerHTML = "<li>할 일이 없습니다.</li>";
                return;
            }

            data.forEach(todo => {
                const li = document.createElement("li");
                li.classList.add("task-item");
                li.setAttribute("data-category", todo.workspace);
                li.setAttribute("data-id", todo.id); // ✅ 이 줄 추가!


                // 체크박스 만들고 클릭 가능하게 설정
                const checkbox = document.createElement("input");
                checkbox.type = "checkbox";
                checkbox.checked = todo.completed;  // 할 일 완료 여부에 맞게 체크 상태 설정
                checkbox.style.marginRight = "10px";  // 체크박스와 텍스트 간 간격 추가
                checkbox.style.transform = "scale(1.2)"; // 체크박스 크기 조절

                // 체크박스를 클릭하면 상태가 바뀌는 이벤트 추가
                checkbox.addEventListener("change", () => {
                    const completed = checkbox.checked;

                    // 변경된 completed 값을 서버로 보내는 부분
                    fetch(`http://localhost:8888/api/todos/${todo.id}/complete`, {
                        method: "PATCH",
                        headers: {
                            "Content-Type": "application/json",
                            "X-XSRF-TOKEN": getCookie("XSRF-TOKEN"), // CSRF 토큰
                        },
                        credentials: "include",
                        body: JSON.stringify({ completed: completed })
                    })
                        .then(res => {
                            if (res.ok) {
                                console.log(`할 일 ${todo.id} 상태 업데이트 완료: ${completed}`);

                                // 서버에서 완료 상태가 업데이트 되면 UI에도 반영
                                todo.completed = completed;  // `todo`의 상태를 업데이트
                                checkbox.checked = completed;  // 체크박스 상태 변경
                            } else {
                                checkbox.checked = !checkbox.checked; // 실패 시 체크박스 상태 되돌리기
                                alert("상태 업데이트 실패");
                            }
                        })
                        .catch(err => {
                            checkbox.checked = !checkbox.checked; // 실패 시 체크박스 상태 되돌리기
                            alert("오류 발생: " + err);
                        });
                });

                const tag = document.createElement("span");
                tag.classList.add("tag");
                tag.classList.add(todo.workspace === "개인" ? "personal" : "project");
                tag.textContent = todo.workspace;

                const title = document.createElement("span");
                title.textContent = todo.title;
                title.classList.add("task-title");

                const dday = document.createElement("span");
                dday.classList.add("due-date");
                dday.textContent = getDday(todo.endDate);

                // li에 자식 요소들 추가
                li.appendChild(checkbox);  // 체크박스는 보이지만 클릭 불가
                li.appendChild(tag);
                li.appendChild(title);
                li.appendChild(dday);

                popupList.appendChild(li);
            });

        })
        .catch(err => {
            console.error("❌ 팝업 할 일 로딩 실패:", err);
        });
}

// ✅ 우클릭 메뉴 삭제 처리
document.addEventListener("DOMContentLoaded", () => {
    const popupList = document.querySelector(".popup-todo-list");
    const completedList = document.getElementById("completed-list");

    document.addEventListener("contextmenu", function (e) {
        const targetLi = e.target.closest(".task-item");
        if (!targetLi) return;

        e.preventDefault();

        const isCompleted = completedList.contains(targetLi);

        // 기존 메뉴 제거
        const existingMenu = document.querySelector(".context-menu");
        if (existingMenu) existingMenu.remove();

        // 메뉴 생성
        const menu = document.createElement("div");
        menu.className = "context-menu";
        menu.textContent = isCompleted ? "취소" : "완료";
        Object.assign(menu.style, {
            position: "absolute",
            top: `${e.pageY}px`,
            left: `${e.pageX}px`,
            background: "#fff",
            border: "1px solid #ccc",
            padding: "5px 10px",
            cursor: "pointer",
            zIndex: "9999"
        });

        menu.addEventListener("click", () => {
            const todoId = targetLi.getAttribute("data-id");
            if (!todoId) return;

            const newCompletedState = !isCompleted;

            fetch(`http://localhost:8888/api/todos/${todoId}/complete`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    "X-XSRF-TOKEN": getCookie("XSRF-TOKEN")
                },
                credentials: "include",
                body: JSON.stringify({ completed: newCompletedState })
            })
                .then(res => {
                    if (!res.ok) throw new Error("서버 응답 오류");

                    const checkbox = targetLi.querySelector("input[type='checkbox']");
                    checkbox.checked = newCompletedState;

                    // 리스트 위치 이동
                    if (newCompletedState) {
                        completedList.appendChild(targetLi);
                        completedList.style.display = "block";
                    } else {
                        popupList.appendChild(targetLi);
                    }
                })
                .catch(err => {
                    alert("상태 업데이트 실패: " + err);
                })
                .finally(() => {
                    menu.remove();
                });
        });

        document.body.appendChild(menu);

        document.addEventListener("click", function closeMenu() {
            menu.remove();
            document.removeEventListener("click", closeMenu);
        });
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const openBtn = document.getElementById("openTodoModal");
    const modal = document.getElementById("todoModal");

    if (openBtn && modal) {
        openBtn.addEventListener("click", function () {
            modal.style.display = "flex";
        });
    }

    window.closeTodoModal = function () {
        modal.style.display = "none";
    };
});

function openTodoModal() {
    document.getElementById("todoModal").style.display = "flex";
}

function closeTodoModal() {
    document.getElementById("todoModal").style.display = "none";
}

function createTodo() {
    const title = document.getElementById("todoTitle").value.trim();
    const start = document.getElementById("startDate").value;
    const end = document.getElementById("endDate").value;
    const space = document.getElementById("todoSpace").value;

    if (!title || !start || !end) {
        alert("모든 항목을 입력하세요.");
        return;
    }

    fetch("http://localhost:8888/api/todos", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-XSRF-TOKEN": getCookie("XSRF-TOKEN")
        },
        credentials: "include",
        body: JSON.stringify({
            title: title,
            startDate: start,
            endDate: end,
            workspace: space,
            assignee: currentUserNick
        })
    })
        .then(res => {
            if (res.ok) {
                alert("할 일이 성공적으로 등록되었습니다.");
                closeTodoModal();
                loadPopupTodos();
            } else {
                alert("등록 실패");
            }
        })
        .catch(err => {
            alert("에러 발생: " + err);
        });
}
function loadMyProjectsForPopup() {
    const spaceSelect = document.getElementById("todoSpace");

    fetch("http://localhost:8888/api/my-projects", {
        credentials: "include"
    })
        .then(res => res.json())
        .then(projects => {
            const filtered = projects.filter(p => p.status !== "완료");

            spaceSelect.innerHTML = "";

            filtered.forEach(p => {
                const opt = document.createElement("option");
                opt.value = p.title;
                opt.textContent = p.title;
                spaceSelect.appendChild(opt);
            });

            const personal = document.createElement("option");
            personal.value = "개인";
            personal.textContent = "개인";
            spaceSelect.appendChild(personal);

            spaceSelect.dispatchEvent(new Event("change"));
        })
        .catch(err => {
            console.error("❌ 프로젝트 불러오기 실패:", err);
            alert("프로젝트 목록을 불러오는 중 오류가 발생했습니다.");
        });
}

document.addEventListener("DOMContentLoaded", () => {
    const toggleBtn = document.getElementById("completed-toggle");
    const completedList = document.getElementById("completed-list");

    if (toggleBtn && completedList) {
        toggleBtn.addEventListener("click", () => {
            const isHidden = completedList.style.display === "none";
            completedList.style.display = isHidden ? "block" : "none";
            toggleBtn.textContent = isHidden ? "완료된 항목 ▾" : "완료된 항목 ▴";
        });
    }
});


