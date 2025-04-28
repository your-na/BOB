// ✅ 쿠키에서 CSRF 토큰 꺼내는 함수
function getCookie(name) {
    const value = document.cookie
        .split("; ")
        .find(row => row.startsWith(name + "="));
    return value ? decodeURIComponent(value.split("=")[1]) : null;
}

document.addEventListener("DOMContentLoaded", function () {
    var filter = document.getElementById("task-filter");

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

    // 마우스 우클릭 시 컨텍스트 메뉴 표시
    popupList.addEventListener("contextmenu", function (e) {
        const targetLi = e.target.closest(".task-item");
        if (!targetLi) return;

        e.preventDefault(); // 기본 우클릭 메뉴 막기

        // 기존 삭제 메뉴 있으면 제거
        const existingMenu = document.querySelector(".context-menu");
        if (existingMenu) existingMenu.remove();

        // 삭제 메뉴 생성
        const menu = document.createElement("div");
        menu.className = "context-menu";
        menu.textContent = "삭제";
        menu.style.position = "absolute";
        menu.style.top = `${e.pageY}px`;
        menu.style.left = `${e.pageX}px`;
        menu.style.background = "#fff";
        menu.style.border = "1px solid #ccc";
        menu.style.padding = "5px 10px";
        menu.style.cursor = "pointer";
        menu.style.zIndex = "9999";

        // 삭제 클릭 시
        menu.addEventListener("click", () => {
            const todoId = targetLi.getAttribute("data-id");

            if (!todoId) {
                alert("할 일 ID가 없습니다.");
                menu.remove();
                return;
            }

            if (!confirm("이 할 일을 삭제하시겠습니까?")) {
                menu.remove();
                return;
            }

            fetch(`http://localhost:8888/api/todos/${todoId}`, {
                method: "DELETE",
                headers: {
                    "X-XSRF-TOKEN": getCookie("XSRF-TOKEN")
                },
                credentials: "include"
            })
                .then(res => {
                    if (res.ok) {
                        console.log(`✅ 삭제 완료: ${todoId}`);
                        targetLi.remove();
                    } else {
                        alert("삭제 실패");
                    }
                })
                .catch(err => {
                    alert("삭제 중 오류 발생: " + err);
                })
                .finally(() => {
                    menu.remove();
                });
        });

        document.body.appendChild(menu);

        // 다른 곳 클릭 시 메뉴 제거
        document.addEventListener("click", function closeMenu() {
            menu.remove();
            document.removeEventListener("click", closeMenu);
        });
    });
});
