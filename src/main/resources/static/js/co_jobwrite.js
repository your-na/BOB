document.addEventListener("DOMContentLoaded", function () {
    const resumeButtons = document.querySelectorAll(".resume-tab");
    const modal = document.getElementById("resume-modal");
    const modalTitle = document.getElementById("modal-title");
    const modalBody = document.getElementById("modal-body");
    const modalClose = document.getElementById("modal-close");
    const addButton = document.getElementById("addResumeBtn");
    const resumeOutput = document.getElementById("resume-output");
    const addResumeText = document.querySelector(".add-resume-text");

    const resumeTemplates = {
        "백엔드 모집용 이력서": "Java, Spring Boot, MySQL 등 백엔드 기술 중심의 이력서입니다.",
        "프론트 모집용 이력서": "HTML, CSS, JS, React 등을 포함한 프론트엔드 이력서입니다.",
        "인턴 모집용 이력서": "실습 경험 위주의 이력서입니다. 학력 및 동아리 중심."
    };

    // 모달 열기 & 드래그 등록
    resumeButtons.forEach(button => {
        const title = button.textContent;
        const content = resumeTemplates[title] || "내용 없음";

        button.addEventListener("click", () => {
            modalTitle.textContent = title;
            modalBody.textContent = content;
            modal.style.display = "flex";
        });

        button.setAttribute("draggable", "true");
        button.addEventListener("dragstart", (e) => {
            e.dataTransfer.setData("text/plain", title);
        });
    });

    modalClose.addEventListener("click", () => {
        modal.style.display = "none";
    });

    // 이력서 추가 텍스트 클릭 이벤트
    addResumeText.addEventListener("click", () => {
        alert("이력서 추가 기능은 추후 구현 예정입니다.");
    });

    // 드롭 처리
    addButton.addEventListener("dragover", (e) => {
        e.preventDefault();
        addButton.style.borderColor = "green";
    });

    addButton.addEventListener("dragleave", () => {
        addButton.style.borderColor = "#aaa";
    });

    addButton.addEventListener("drop", (e) => {
        e.preventDefault();
        addButton.style.borderColor = "#aaa";

        const title = e.dataTransfer.getData("text/plain");

        if (title) {
            // 이미 같은 템플릿이 추가되었는지 확인
            const existing = [...resumeOutput.querySelectorAll(".resume-item span")];
            const isDuplicate = existing.some(span => span.textContent === title);

            if (isDuplicate) {
                alert(`'${title}' 템플릿은 이미 추가되었습니다.`);
                return;
            }

            // 새 항목 추가
            const li = document.createElement("li");
            li.className = "resume-item";
            li.innerHTML = `
      <span>${title}</span>
      <button class="delete-btn" title="삭제">🗑️</button>
    `;
            resumeOutput.appendChild(li);
        }
    });

    // 이벤트 위임 방식으로 삭제 처리
    resumeOutput.addEventListener("click", function (e) {
        if (e.target.classList.contains("delete-btn")) {
            const li = e.target.closest("li");
            if (li) li.remove();
        }
    });

});
