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

    // ✅ 추가 버튼 기능
    document.addEventListener("click", function (event) {
        if (event.target.classList.contains("add-project-btn")) {
            const section = event.target.closest(".history-section");
            const tableBody = section.querySelector(".history-table tbody");
            const templateRow = section.querySelector(".new-entry-row");

            if (templateRow && tableBody) {
                const newRow = templateRow.cloneNode(true);
                newRow.style.display = "table-row";
                newRow.classList.remove("new-entry-row");
                tableBody.appendChild(newRow);
            }
        }
    });

    // ✅ 삭제 버튼
    document.addEventListener("click", function (event) {
        if (event.target.classList.contains("delete-btn")) {
            const row = event.target.closest("tr");
            const id = event.target.getAttribute("data-id");

            if (!id) {
                alert("❌ 삭제할 항목의 ID가 없습니다.");
                return;
            }

            if (confirm("⚠정말 삭제하시겠습니까?\n 삭제하면 마이프로젝트에서도 사라지며, 복구할 수 없습니다.")) {
                fetch(`/project-history/${id}`, {
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/json",
                        [getCsrfHeader()]: getCsrfToken()
                    }
                })
                    .then(response => {
                        if (response.ok) {
                            alert("✅ 삭제 완료!");
                            if (row) row.remove();
                        } else {
                            alert("❌ 삭제 실패!");
                        }
                    })
                    .catch(error => {
                        console.error("삭제 중 오류 발생:", error);
                        alert("❌ 서버 오류가 발생했습니다.");
                    });
            }
        }
    });

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
});
