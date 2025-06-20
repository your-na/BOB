
// ✅ CSRF 관련 함수
function getCsrfToken() {
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    return csrfMeta ? csrfMeta.getAttribute("content") : "";
}

function getCsrfHeader() {
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    return csrfHeaderMeta ? csrfHeaderMeta.getAttribute("content") : "X-CSRF-TOKEN";
}

// ✅ 파일 업로드 시 파일보기 링크 활성화
document.addEventListener("change", function (event) {
    if (event.target.classList.contains("file-upload")) {
        const fileInput = event.target;
        const fileView = fileInput.closest("td").querySelector(".file-view");

        if (fileInput.files.length > 0 && fileView) {
            fileView.style.display = "inline";
            fileView.href = URL.createObjectURL(fileInput.files[0]);
            fileView.textContent = "파일보기";
        }
    }
});
document.addEventListener("DOMContentLoaded", function () {
    const tabs = document.querySelectorAll(".tab-item");
    const sections = document.querySelectorAll(".history-section");

    // ✅ 탭 클릭 시 해당 섹션 표시
    tabs.forEach(tab => {
        tab.addEventListener("click", function () {
            tabs.forEach(t => t.classList.remove("active"));
            sections.forEach(s => s.style.display = "none");

            this.classList.add("active");
            const targetId = this.getAttribute("data-target");
            const targetSection = document.getElementById(targetId);
            if (targetSection) {
                targetSection.style.display = "block";
            }
        });
    });

    // ✅ 검색 기능
    document.addEventListener("keyup", function (event) {
        if (event.target.classList.contains("searchbar")) {
            const searchKeyword = event.target.value.toLowerCase();
            const section = event.target.closest(".history-section");
            const rows = section.querySelectorAll(".history-table tbody tr:not(.new-entry-row)");

            rows.forEach(row => {
                const titleCell = row.querySelector("td:nth-child(5)");
                if (!titleCell) return;
                const title = titleCell.textContent.toLowerCase();
                row.style.display = title.includes(searchKeyword) ? "table-row" : "none";
            });
        }
    });


    // ✅ 삭제 버튼 처리 (섹션별 메시지 + URL 다르게 적용)
    document.addEventListener("click", function (event) {
        if (event.target.classList.contains("delete-btn")) {
            const section = event.target.closest(".history-section");
            const row = event.target.closest("tr");
            const id = event.target.getAttribute("data-id");

            if (!id) {
                alert("❌ 삭제할 항목의 ID가 없습니다.");
                return;
            }

            let deleteUrl = "";
            let message = "정말 삭제하시겠습니까?";
            let isHandled = true;

            switch (section?.id) {
                case "project-history":
                    deleteUrl = `/project-history/${id}`;
                    message = "⚠정말 삭제하시겠습니까?\n삭제하면 마이프로젝트에서도 사라지며, 복구할 수 없습니다.";
                    break;
                case "simple-history":
                    deleteUrl = `/api/basic-info/${id}`;
                    break;
                case "school-history":
                    deleteUrl = `/api/education-history/delete/${id}`;
                    break;
                default:
                    isHandled = false;
            }

            if (!isHandled || !deleteUrl) return;

            if (!confirm(message)) return;

            fetch(deleteUrl, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                    [getCsrfHeader()]: getCsrfToken()
                }
            })
                .then(res => {
                    if (res.ok) {
                        alert("✅ 삭제되었습니다!");
                        row.remove();
                    } else {
                        alert("❌ 삭제 실패");
                    }
                })
                .catch(err => {
                    console.error("❌ 삭제 중 오류 발생:", err);
                    alert("❌ 서버 오류가 발생했습니다.");
                });
        }
    });



    // ✅ 셀 더블클릭 시 수정 (자격증 + 구직 상태 드롭다운 포함)
    document.addEventListener("dblclick", function (event) {
        const target = event.target;

        if (!target.matches("td") || target.querySelector("input, select")) return;

        const row = target.closest("tr");
        const section = target.closest(".history-section");
        const sectionId = section?.id;
        const colIndex = target.cellIndex;
        const originalText = target.textContent.trim();

        const editableMap = {
            "career-history": {
                editableCols: [1, 2, 3],
                dateCols: [3],
                selectCols: []
            },
            "job-history": {
                editableCols: [1, 2, 3, 4, 5],
                dateCols: [2, 3],
                selectCols: [1] // ✅ 상태만 select로
            }
        };

        if (!editableMap[sectionId] || !editableMap[sectionId].editableCols.includes(colIndex)) return;

        // ✅ 종료일(3번째 셀)인데 상태가 '재직'이면 수정 금지
        if (sectionId === "job-history" && colIndex === 3) {
            const statusCell = row.querySelector("td:nth-child(2)");
            const statusText = statusCell?.textContent.trim();
            if (statusText === "재직") return;
        }


        const isDate = editableMap[sectionId].dateCols.includes(colIndex);
        const isSelect = editableMap[sectionId].selectCols.includes(colIndex);

        let input;

        if (isSelect) {
            input = document.createElement("select");
            ["재직", "퇴직"].forEach(opt => {
                const option = document.createElement("option");
                option.value = opt;
                option.textContent = opt;
                if (opt === originalText) option.selected = true;
                input.appendChild(option);
            });
        } else {
            input = document.createElement("input");
            input.type = isDate ? "date" : "text";
            input.value = isDate && !isNaN(Date.parse(originalText))
                ? new Date(originalText).toISOString().split("T")[0]
                : originalText;
        }

        input.className = "editable-input";
        input.onblur = () => {
            target.textContent = input.value.trim() || originalText;
        };
        input.onkeydown = (e) => {
            if (e.key === "Enter") input.blur();
        };

        target.innerHTML = "";
        target.appendChild(input);
        input.focus();
    });

    // ✅ 기본 탭 표시
    const defaultSection = document.querySelector(".tab-item.active")?.getAttribute("data-target");
    if (defaultSection) {
        document.querySelectorAll(".history-section").forEach(sec => sec.style.display = "none");
        const section = document.getElementById(defaultSection);
        if (section) section.style.display = "block";
    }
    loadJobHistories();
    loadEducations();
});

// ✅ 구직 내역 초기 로딩 (GET 요청)
function loadJobHistories() {
    const tbody = document.querySelector("#job-history .history-table tbody");

    // ✅ 기존의 .new-entry-row 백업
    const templateRow = tbody.querySelector(".new-entry-row");

    // ✅ tbody 안을 비우되 템플릿은 살려둠
    tbody.innerHTML = "";

    // ✅ 템플릿 다시 추가
    if (templateRow) {
        tbody.appendChild(templateRow);
        templateRow.style.display = "none"; // 템플릿은 안보이게 유지
    }



    fetch("/api/job-history", {
        headers: { [getCsrfHeader()]: getCsrfToken() }
    })
        .then(response => response.json())
        .then(data => {
            data.forEach((item, index) => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${index + 1}</td>
                    <td>${item.status}</td>
                    <td>${item.startDate || ""}</td>
                    <td>${item.endDate || ""}</td>
                    <td>${item.workplace || ""}</td>
                    <td>${item.jobTitle || ""}</td>
                    <td><button class="delete-btn" data-id="${item.id}">삭제</button></td>
                `;
                tbody.appendChild(row);
            });
        });
}

// ✅ 여기서부터 loadEducations() 시작!
function loadEducations() {
    const tbody = document.querySelector("#school-history .history-table tbody");
    const templateRow = tbody.querySelector(".new-entry-row");

    tbody.innerHTML = "";
    if (templateRow) {
        tbody.appendChild(templateRow);
        templateRow.style.display = "none";
    }

    fetch("/api/education-history/list", {
        headers: { [getCsrfHeader()]: getCsrfToken() }
    })
        .then(res => res.json())
        .then(data => {
            data.forEach((edu, index) => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${index + 1}</td>
                    <td>${edu.schoolName}</td>
                    <td>${edu.status}</td>
                    <td>${edu.startDate}</td>
                    <td>${edu.endDate}</td>
                    <td><button class="delete-btn" data-id="${edu.id}">삭제</button></td>
                `;
                tbody.insertBefore(row, templateRow);
            });
        })
        .catch(err => console.error("❌ 학력 불러오기 실패:", err));
}

// ✅ 새 경력 추가 시 서버로 POST 요청
document.addEventListener("click", function (event) {
    if (
        event.target.classList.contains("add-project-btn") &&
        event.target.closest("#job-history")
    ) {
        const section = event.target.closest(".history-section");
        const newRow = section.querySelector(".new-entry-row").cloneNode(true);
        newRow.style.display = "table-row";
        newRow.classList.remove("new-entry-row");

        updateEndDateState(newRow); // 새로 만들자마자 상태 검사해서 종료일 비활성화


        const inputs = newRow.querySelectorAll("input, select");

        // ✅ 입력 후 자동 저장
        inputs.forEach(input => {
            input.addEventListener("change", function () {
                if (input.classList.contains("status-select")) {
                    updateEndDateState(newRow); // 상태 변경될 때마다 실행
                }
                const status = newRow.querySelector(".status-select").value;
                const startDate = newRow.querySelector(".start-date").value;
                const workplace = newRow.querySelector(".workplace").value;
                const jobTitle = newRow.querySelector(".job-title").value;

                let endDate = null;
                if (status !== "재직") {
                    endDate = newRow.querySelector(".end-date").value;
                }


                // 유효성 검사 (근무지, 직무는 있어야 저장)
                if (!workplace || !jobTitle) return;

                const data = {
                    status,
                    startDate,
                    endDate,
                    workplace,
                    jobTitle
                };

                fetch("/api/job-history", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        [getCsrfHeader()]: getCsrfToken()
                    },
                    body: JSON.stringify(data)
                })
                    .then(response => response.json())
                    .then(saved => {
                        // 저장 성공 시 ID를 부여하고 삭제버튼에 data-id 설정
                        const delBtn = newRow.querySelector(".delete-btn");
                        delBtn.setAttribute("data-id", saved.id);
                        alert("✅ 저장되었습니다!");

                        loadJobHistories(); // ✅ 새로고침하여 저장한 데이터 포함해 전체 다시 불러오기
                    })
                    .catch(err => console.error("❌ 저장 실패:", err));
            });
        });

        section.querySelector("tbody").appendChild(newRow);
    }
});

// ✅ 구직 내역 수정 시 자동 저장 (기존 row 더블클릭 수정 후 blur 시)
document.addEventListener("blur", function (event) {
    if (event.target.classList.contains("editable-input")) {
        const input = event.target;
        const td = input.closest("td");
        const row = td.closest("tr");
        const id = row.querySelector(".delete-btn")?.getAttribute("data-id");

        if (!id) return;

        // 현재 row에서 값 읽기
        const cells = row.querySelectorAll("td");
        const status = cells[1].textContent.trim();
        const startDate = cells[2].textContent.trim();
        const workplace = cells[4].textContent.trim();
        const jobTitle = cells[5].textContent.trim();
        let endDate = null;
        if (status !== "재직") {
            endDate = cells[3].textContent.trim();
        }


        const data = {
            status,
            startDate,
            endDate,
            workplace,
            jobTitle
        };

        fetch(`/api/job-history/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                [getCsrfHeader()]: getCsrfToken()
            },
            body: JSON.stringify(data)
        })
            .then(res => res.ok && console.log("자동 저장 완료"))
            .catch(err => console.error("자동 저장 실패", err));
    }
}, true); // useCapture: true

// ✅ 상태에 따라 종료일 비활성화 함수
function updateEndDateState(row) {
    const statusSelect = row.querySelector(".status-select");
    const endDateInput = row.querySelector(".end-date");

    if (!statusSelect || !endDateInput) return;

    if (statusSelect.value === "재직") {
        endDateInput.value = "";
        endDateInput.disabled = true;
        endDateInput.placeholder = "재직 중";
    } else {
        endDateInput.disabled = false;
        endDateInput.placeholder = "YYYY-MM-DD";
    }
}

// ✅ 기본 사항 추가
document.addEventListener("click", function (e) {
    if (e.target.closest("#simple-history")?.classList.contains("history-section") &&
        e.target.classList.contains("add-project-btn")) {

        const section = e.target.closest(".history-section");
        const newRow = section.querySelector(".new-entry-row").cloneNode(true);
        newRow.style.display = "table-row";
        newRow.classList.remove("new-entry-row");

        const inputs = newRow.querySelectorAll("input");

        let isSaved = false; // 중복 저장 방지

        inputs.forEach(input => {
            input.addEventListener("change", () => {
                if (isSaved) return;

                const data = {
                    name: newRow.querySelector(".input-name").value.trim(),
                    birthDate: newRow.querySelector(".input-birth").value.trim(),
                    region: newRow.querySelector(".input-region").value.trim(),
                    email: newRow.querySelector(".input-email").value.trim(),
                    phone: newRow.querySelector(".input-phone").value.trim()
                };

                // ✅ 모든 항목이 입력되었을 때만 저장
                const allFilled = data.name && data.birthDate && data.region && data.email && data.phone;
                if (!allFilled) return;

                fetch("/api/basic-info", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        [getCsrfHeader()]: getCsrfToken()
                    },
                    body: JSON.stringify(data)
                })
                    .then(res => res.json())
                    .then(saved => {
                        isSaved = true;
                        newRow.querySelector(".delete-btn").setAttribute("data-id", saved.id);
                        alert("✅ 기본 정보 저장됨");
                    })
                    .catch(err => {
                        console.error("❌ 저장 실패:", err);
                        alert("❌ 저장 실패");
                    });
            });
        });

        section.querySelector("tbody").appendChild(newRow);
    }
});

// ✅ 기본 정보 삭제 (simple-history 섹션)
document.addEventListener("click", function (event) {
    if (event.target.classList.contains("delete-btn") &&
        event.target.closest("#simple-history")) {

        const row = event.target.closest("tr");
        const id = event.target.getAttribute("data-id");

        if (!id) {
            row.remove(); // 새로 추가된 미저장 행이면 그냥 제거
            return;
        }

        if (!confirm("정말 삭제하시겠습니까?")) return;

        fetch(`/api/basic-info/${id}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
                [getCsrfHeader()]: getCsrfToken()
            }
        })
            .then(res => {
                if (res.ok) {
                    alert("✅ 삭제되었습니다!");
                    row.remove();
                } else {
                    alert("❌ 삭제 실패");
                }
            })
            .catch(err => {
                console.error("❌ 오류 발생:", err);
                alert("❌ 서버 오류가 발생했습니다.");
            });
    }
});



// ✅ 학력 추가
document.addEventListener("click", function (e) {
    if (e.target.closest("#school-history")?.classList.contains("history-section") &&
        e.target.classList.contains("add-project-btn")) {

        const section = e.target.closest(".history-section");
        const newRow = section.querySelector(".new-entry-row").cloneNode(true);
        newRow.style.display = "table-row";
        newRow.classList.remove("new-entry-row");

        const inputs = newRow.querySelectorAll("input, select");

        // ✅ 한번 저장된 row 중복 저장 방지
        let isSaved = false;

        inputs.forEach(input => {
            input.addEventListener("change", () => {
                if (isSaved) return; // 중복 저장 방지

                const data = {
                    schoolName: newRow.querySelector(".input-school").value.trim(),
                    status: newRow.querySelector(".input-status").value,
                    startDate: newRow.querySelector(".input-start").value,
                    endDate: newRow.querySelector(".input-end").value
                };

                // ✅ 모든 항목이 채워져 있을 때만 저장
                const allFilled = data.schoolName && data.status && data.startDate && data.endDate;
                if (!allFilled) return;

                fetch("/api/education-history/save", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        [getCsrfHeader()]: getCsrfToken()
                    },
                    body: JSON.stringify(data)
                })
                    .then(res => res.json())
                    .then(saved => {
                        isSaved = true;
                        newRow.querySelector(".delete-btn").setAttribute("data-id", saved.id);
                        alert("✅ 학력 저장됨");

                        loadEducations(); // ✅ 이 줄 추가!

                    })
                    .catch(err => {
                        console.error("❌ 저장 실패:", err);
                        alert("❌ 저장 실패");
                    });
            });
        });

        section.querySelector("tbody").appendChild(newRow);
    }


});


