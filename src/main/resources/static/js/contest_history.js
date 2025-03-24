document.addEventListener("DOMContentLoaded", function () {
    const tabs = document.querySelectorAll(".tab-item");
    const searchInput = document.getElementById("search_sw");  // ✅ ID 수정
    const addProjectBtn = document.querySelector(".add-project-btn");
    const tableBody = document.querySelector(".history-table tbody");

    // ✅ 탭 변경
    tabs.forEach(tab => {
        tab.addEventListener("click", function () {
            tabs.forEach(item => item.classList.remove("active"));
            this.classList.add("active");
        });
    });

    // ✅ 검색 기능
    searchInput.addEventListener("keyup", function () {
        const searchKeyword = searchInput.value.toLowerCase();
        const rows = document.querySelectorAll(".history-table tbody tr:not(.new-entry-row)");

        rows.forEach(row => {
            const title = row.querySelector("td:nth-child(5)").textContent.toLowerCase();
            if (title.includes(searchKeyword)) {
                row.style.display = "table-row";
            } else {
                row.style.display = "none";
            }
        });
    });

    // ✅ 삭제 기능
    document.addEventListener("click", function (event) {
        if (event.target.classList.contains("delete-btn")) {
            let row = event.target.closest("tr");
            row.remove();
        }
    });

    // ✅ 프로젝트 내역 추가 버튼 클릭 시 빈 행 추가
    addProjectBtn.addEventListener("click", function () {
        let templateRow = document.querySelector(".new-entry-row");

        if (templateRow) {
            let newRow = templateRow.cloneNode(true);
            newRow.style.display = "table-row";
            newRow.classList.remove("new-entry-row"); // ✅ 새로운 행에서는 템플릿 클래스 제거
            tableBody.appendChild(newRow);
        }
    });

    // ✅ 파일 업로드 시 "파일보기" 링크 활성화
    document.addEventListener("change", function (event) {
        if (event.target.classList.contains("file-upload")) {
            let fileInput = event.target;
            let fileView = fileInput.closest("td").querySelector(".file-view");

            if (fileInput.files.length > 0) {
                fileView.style.display = "inline";
                fileView.href = URL.createObjectURL(fileInput.files[0]);
                fileView.textContent = "파일보기";
            }
        }
    });

});
