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

    // ✅ 검색 기능 (각 테이블별로 적용)
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

    // ✅ 추가 버튼 기능 (공모전/프로젝트 모두)
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
            if (row) {
                row.remove();
            }
        }
    });

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

    // ✅ 페이지 로드시 기본 탭 보이도록 설정
    const defaultSection = document.querySelector(".tab-item.active")?.getAttribute("data-target");
    if (defaultSection) {
        document.querySelectorAll(".history-section").forEach(sec => sec.style.display = "none");
        const section = document.getElementById(defaultSection);
        if (section) {
            section.style.display = "block";
        }
    }
});
