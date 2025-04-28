document.addEventListener("DOMContentLoaded", function () {
    const resumeButtons = document.querySelectorAll(".resume-tab");
    const modal = document.getElementById("resume-modal");
    const modalTitle = document.getElementById("modal-title");
    const modalBody = document.getElementById("modal-body");
    const modalClose = document.getElementById("modal-close");
    const addButton = document.getElementById("addResumeBtn");
    const resumeOutput = document.getElementById("resume-output");
    const addResumeText = document.querySelector(".add-resume-text");

    fetch('/api/coresumes')
        .then(res => res.json())
        .then(data => {
            const container = document.getElementById("savedResumeList");
            container.innerHTML = ''; // 기존 내용 비우기

            data.forEach(resume => {
                const div = document.createElement("div");
                div.className = "resume-tab";
                div.setAttribute("draggable", "true");
                div.dataset.id = resume.id;
                div.textContent = resume.title;

                container.appendChild(div);
            });
        })
        .catch(error => {
            console.error("이력서 불러오기 실패:", error);
        });

    const resumeTemplates = {
        "백엔드 모집용 이력서": "Java, Spring Boot, MySQL 등 백엔드 기술 중심의 이력서입니다.",
        "프론트 모집용 이력서": "HTML, CSS, JS, React 등을 포함한 프론트엔드 이력서입니다.",
        "인턴 모집용 이력서": "실습 경험 위주의 이력서입니다. 학력 및 동아리 중심."
    };

    // 모달 열기 & 드래그 등록
    document.getElementById("savedResumeList").addEventListener("click", (e) => {
        if (e.target.classList.contains("resume-tab")) {
            const resumeId = e.target.dataset.id;

            fetch(`/api/coresumes/${resumeId}`)
                .then(res => res.json())
                .then(data => {
                    const modal = document.getElementById("resume-modal");
                    const modalTitle = document.getElementById("modal-title");
                    const modalBody = document.getElementById("modal-body");

                    modalTitle.textContent = data.title;

                    let html = `<div class="resume-preview-wrapper">`;

                    data.sections.forEach((section, index) => {
                        html += `
      <section class="resume-section preview-mode">
        <div class="section-header">
          <span>${index + 1}. ${section.title}</span>
        </div>

        ${section.comment ? `<p class="section-note">${section.comment}</p>` : ''}

        ${section.conditions?.length ? `
          <div class="tag-list">
            ${section.conditions.map(cond => `<span class="tag condition">${cond}</span>`).join("")}
          </div>
        ` : ''}

        ${section.tags?.length ? `
          <div class="tag-list">
            ${section.tags.map(tag => `<span class="tag">${tag}</span>`).join("")}
          </div>
        ` : ''}

        ${section.content ? `
          <div class="section-content">
            <textarea readonly class="preview-textarea">${section.content}</textarea>
          </div>
        ` : ''}
      </section>
    `;
                    });



                    html += `</div>`;
                    modalBody.innerHTML = html;
                    modal.style.display = "flex";
                });
        }
    });


// 드래그 이벤트도 여기에 추가
    document.getElementById("savedResumeList").addEventListener("dragstart", (e) => {
        if (e.target.classList.contains("resume-tab")) {
            const title = e.target.textContent;
            e.dataTransfer.setData("text/plain", title);
        }
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

    // ✅ 작성 버튼 클릭 시 구인글 저장
    document.querySelector(".submit-btn").addEventListener("click", function (e) {
        e.preventDefault();

        // 1. 기본 입력값 수집
        const data = {
            title: document.querySelector(".title-input").value,
            companyIntro: document.querySelector("textarea[name='companyIntro']").value,
            email: document.querySelector("input[name='email']").value,
            phone: document.querySelector("input[name='phone']").value,
            companyLink: document.querySelector("input[name='companyLink']").value,
            career: document.querySelector("input[name='career']").value,
            education: document.querySelector("input[name='education']").value,
            preference: document.querySelector("input[name='preference']").value,
            salary: document.querySelector("input[name='salary']").value,
            time: document.querySelector("input[name='time']").value,
            startDate: document.querySelector("#startDate").value,
            endDate: document.querySelector("#endDate").value,
            employmentTypes: [],
            resumeIds: []
        };

        // 2. 고용형태 체크박스
        document.querySelectorAll("input[name='employmentType']:checked").forEach(cb => {
            data.employmentTypes.push(cb.value);
        });

        // 3. 드래그로 추가된 이력서 → title로 매칭해서 id 추출
        const resumeItems = document.querySelectorAll("#resume-output .resume-item span");
        const savedResumeMap = {}; // 텍스트 → ID 매핑을 위해
        document.querySelectorAll("#savedResumeList .resume-tab").forEach(tab => {
            savedResumeMap[tab.textContent] = tab.dataset.id;
        });
        resumeItems.forEach(span => {
            const id = savedResumeMap[span.textContent];
            if (id) data.resumeIds.push(Number(id));
        });

        // 4. 서버 전송 (POST)
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        fetch("/api/cojobs", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken      // 👉 CSRF 헤더 추가!
            },
            body: JSON.stringify(data)
        })

            .then(res => {
                if (!res.ok) throw new Error("서버 오류 발생");
                return res.text();  // ✅ 문자열로 받아야 함
            })
            .then(message => {
                alert(message);     // 서버에서 받은 메시지 출력
                location.reload();
            })

            .catch(err => {
                console.error(err);
                alert("저장 중 오류 발생!");
            });
    });


});
