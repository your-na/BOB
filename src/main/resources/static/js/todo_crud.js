document.addEventListener("DOMContentLoaded", () => {
    const weekRow = document.getElementById("week-row");
    const monthRow = document.getElementById("month-row");
    const crudBody = document.getElementById("crud-body");
    const addMonthBtn = document.querySelector(".add-month");
    const addCategoryBtn = document.getElementById("add-category");

    let monthCount = 3;
    const weeksPerMonth = 5;
    let isMouseDown = false;

    document.addEventListener("mousedown", () => isMouseDown = true);
    document.addEventListener("mouseup", () => isMouseDown = false);

    function renderWeeks() {
        weekRow.innerHTML = '';
        for (let i = 0; i < monthCount; i++) {
            for (let w = 1; w <= weeksPerMonth; w++) {
                const td = document.createElement("td");
                td.textContent = w;
                weekRow.appendChild(td);
            }
        }
    }

    function makeEditable(td) {
        td.addEventListener("dblclick", () => {
            const input = document.createElement("input");
            input.type = "text";
            input.value = td.textContent;
            input.style.width = "90%";
            td.innerHTML = '';
            td.appendChild(input);
            input.focus();

            input.addEventListener("blur", () => {
                td.textContent = input.value;
            });

            input.addEventListener("keydown", (e) => {
                if (e.key === "Enter") td.textContent = input.value;
            });
        });
    }

    function addTaskRow(categoryTd, taskName = "작업명", insertBeforeRow = null) {
        const tr = document.createElement("tr");
        const taskTd = document.createElement("td");
        taskTd.textContent = taskName;
        taskTd.classList.add("task-cell");
        makeEditable(taskTd);
        tr.appendChild(taskTd);

        for (let i = 0; i < monthCount * weeksPerMonth; i++) {
            const td = document.createElement("td");
            td.classList.add("clickable-cell");
            tr.appendChild(td);
        }

        if (!insertBeforeRow) {
            // 카테고리Td 기준으로 new-entry 전 줄 찾아서 그 앞에 삽입
            let current = categoryTd.parentElement.nextElementSibling;
            let newEntryRow = null;

            while (current) {
                if (current.querySelector(".category-cell")) break;
                if (current.querySelector(".new-entry")) {
                    newEntryRow = current;
                    break;
                }
                current = current.nextElementSibling;
            }

            if (newEntryRow) {
                crudBody.insertBefore(tr, newEntryRow);
            } else {
                // 혹시라도 못 찾으면 카테고리 줄 바로 뒤에 넣기 (fallback)
                crudBody.insertBefore(tr, categoryTd.parentElement.nextElementSibling);
            }
        } else {
            crudBody.insertBefore(tr, insertBeforeRow);
        }

        categoryTd.rowSpan += 1;
    }

    function addNewEntryRow(categoryTd) {
        const newRow = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = monthCount * weeksPerMonth + 1;
        td.className = "new-entry";
        td.textContent = "새로 만들기";

        td.addEventListener("click", () => {
            addTaskRow(categoryTd, "작업명", newRow);
        });

        newRow.appendChild(td);

        // ✔️ 카테고리 내부 작업명 끝 위치 뒤에 삽입
        let currentRow = categoryTd.parentElement;
        while (
            currentRow.nextElementSibling &&
            !currentRow.nextElementSibling.querySelector(".category-cell")
            ) {
            currentRow = currentRow.nextElementSibling;
        }

        crudBody.insertBefore(newRow, currentRow.nextElementSibling);
    }

    function countTaskRowsInCategory(categoryTd) {
        let count = 0;
        let row = categoryTd.parentElement.nextElementSibling;
        while (row && !row.querySelector(".category-cell") && !row.querySelector(".new-entry")) {
            count++;
            row = row.nextElementSibling;
        }
        return count;
    }

    function renderCategory(category, taskList = ["작업명"]) {
        const firstRow = document.createElement("tr");

        const categoryTd = document.createElement("td");
        categoryTd.classList.add("category-cell");
        categoryTd.textContent = category;
        categoryTd.rowSpan = 1;
        makeEditable(categoryTd);

        const firstTaskTd = document.createElement("td");
        firstTaskTd.textContent = taskList[0];
        firstTaskTd.classList.add("task-cell");
        makeEditable(firstTaskTd);

        firstRow.appendChild(categoryTd);
        firstRow.appendChild(firstTaskTd);

        for (let i = 0; i < monthCount * weeksPerMonth; i++) {
            const td = document.createElement("td");
            td.classList.add("clickable-cell");
            firstRow.appendChild(td);
        }

        // ✅ 항상 tbody의 마지막에 추가 (새 카테고리는 기존 카테고리 끝 다음)
        crudBody.appendChild(firstRow);

        // 나머지 작업
        for (let i = 1; i < taskList.length; i++) {
            addTaskRow(categoryTd, taskList[i], null);
        }

        // 새로 만들기 줄도 카테고리 내부에 위치
        addNewEntryRow(categoryTd);
    }

    crudBody.addEventListener("click", (e) => {
        if (e.target.classList.contains("clickable-cell")) {
            e.target.classList.toggle("active-cell");
        }
    });

    crudBody.addEventListener("mouseover", (e) => {
        if (isMouseDown && e.target.classList.contains("clickable-cell")) {
            e.target.classList.toggle("active-cell");
        }
    });

    addMonthBtn.addEventListener("click", () => {
        monthCount++;
        const newTh = document.createElement("th");
        newTh.colSpan = weeksPerMonth;
        newTh.textContent = `${monthCount}월`;
        monthRow.insertBefore(newTh, addMonthBtn);

        renderWeeks();

        crudBody.querySelectorAll("tr").forEach(tr => {
            if (!tr.querySelector(".new-entry")) {
                for (let i = 0; i < weeksPerMonth; i++) {
                    const td = document.createElement("td");
                    td.classList.add("clickable-cell");
                    tr.appendChild(td);
                }
            }
        });
    });

    addCategoryBtn.addEventListener("click", () => {
        const name = prompt("카테고리 이름 입력:");
        if (name) {
            renderCategory(name);
        }
    });


    crudBody.addEventListener("contextmenu", (e) => {
        e.preventDefault();
        const td = e.target.closest("td");
        const tr = e.target.closest("tr");
        if (!td || !tr || td.classList.contains("new-entry")) return;

        document.querySelector(".context-menu")?.remove();

        const menu = document.createElement("div");
        menu.className = "context-menu";
        menu.textContent = "행 삭제";
        menu.style.top = `${e.pageY}px`;
        menu.style.left = `${e.pageX}px`;
        document.body.appendChild(menu);

        menu.addEventListener("click", () => {
            const isCategory = td.classList.contains("category-cell");

            if (isCategory) {
                let next = tr.nextElementSibling;
                while (next && !next.querySelector(".category-cell")) {
                    const temp = next.nextElementSibling;
                    next.remove();
                    next = temp;
                }
                tr.remove();
            } else {
                const categoryTd = tr.querySelector(".category-cell");

                if (categoryTd) {
                    // 👉 첫 작업명이면서 카테고리 포함
                    const nextTr = tr.nextElementSibling;

                    if (nextTr && !nextTr.querySelector(".category-cell")) {
                        const firstTaskTd = nextTr.querySelector("td");
                        nextTr.insertBefore(categoryTd, firstTaskTd);
                    }

                    tr.remove();
                } else {
                    // 👉 일반 작업명 삭제
                    const prevCategoryTd = (() => {
                        let prev = tr.previousElementSibling;
                        while (prev) {
                            const cat = prev.querySelector(".category-cell");
                            if (cat) return cat;
                            prev = prev.previousElementSibling;
                        }
                        return null;
                    })();

                    if (prevCategoryTd) prevCategoryTd.rowSpan -= 1;
                    tr.remove();
                }
            }

            menu.remove();
        });


        document.addEventListener("click", () => {
            menu.remove();
        }, { once: true });
    });


    // 초기
    renderWeeks();
    renderCategory("설계", ["DB", "기능도", "UI"]);
    renderCategory("프론트", ["회원가입/로그인", "프로필 수정"]);
    renderCategory("백엔드", ["회원 관리", "게시판 CRUD", "공모전 구현"]);

    makeEditable(document.getElementById("project-title"));

});
