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

    // ✅ 삭제 버튼 (백엔드 연동 포함 + CSRF 토큰 처리)
    document.addEventListener("click", function (event) {
        if (event.target.classList.contains("delete-btn")) {
            const row = event.target.closest("tr");
            const id = event.target.getAttribute("data-id");

            if (!id) {
                alert("❌ 삭제할 항목의 ID가 없습니다.");
                return;
            }

            if (confirm("정말 삭제하시겠습니까?")) {
                fetch(`/project-history/${id}`, {
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/json",
                        [getCsrfHeader()]: getCsrfToken()  // ✅ 동적 헤더 키 사용
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

    // ✅ CSRF 토큰 가져오기 함수
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
