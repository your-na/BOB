document.addEventListener("DOMContentLoaded", () => {
    let sections = document.querySelectorAll(".resume-section");
    const jobInput = document.getElementById("job-input");
    const jobTagContainer = document.querySelector(".job-tags");
    const multiOnBtn = document.getElementById("multi-on");
    const multiOffBtn = document.getElementById("multi-off");
    const addBtn = document.getElementById("add-section");
    const popup = document.getElementById("section-popup");

    // ➕ 버튼 클릭 시 팝업 위치 설정
    addBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        const rect = addBtn.getBoundingClientRect();
        popup.style.top = `${rect.bottom + window.scrollY + 5}px`;
        popup.style.left = `${rect.left + window.scrollX}px`;
        popup.style.display = popup.style.display === "block" ? "none" : "block";
    });

    document.addEventListener("click", (e) => {
        if (!popup.contains(e.target) && e.target !== addBtn) {
            popup.style.display = "none";
        }
    });

    document.querySelectorAll(".popup-option").forEach(option => {
        option.addEventListener("click", () => {
            const type = option.textContent.trim();
            const sectionIndex = document.querySelectorAll(".resume-section").length + 1;

            const newSection = document.createElement("section");
            newSection.className = "resume-section";
            newSection.id = `section${sectionIndex}`;

            let content = "";
            switch (type) {
                case "선택형":
                    content = `
                        <div class="section-header">
                            <span class="section-title-text">${sectionIndex}. 제목 입력</span>
                            <input type="text" class="section-title-input" value="제목 입력" style="display: none;">
                            <label>선택 방식</label>
                            <select><option>선택형</option></select>
                            <button class="delete-btn">✕</button>
                        </div>
                        <input type="text" id="ohcomment" placeholder="설명 입력">
                        <div class="tag-mode">
                            <button class="mode-btn selected-tag">복수선택 ⭕</button>
                            <button class="mode-btn">복수선택 ❌</button>
                        </div>
                        <div class="tag-list job-tags"></div>
                        <input class="tag-input" type="text" placeholder="항목 입력 후 엔터">
                    `;
                    break;
                case "서술형":
                    content = `
                        <div class="section-header">
                            <span class="section-title-text">${sectionIndex}. 제목 입력</span>
                            <input type="text" class="section-title-input" value="제목 입력" style="display: none;">
                            <button class="delete-btn">✕</button>
                        </div>
                        <input type="text" id="ohcomment" placeholder="설명 입력">
                        <textarea placeholder="구직자 답변 입력란"></textarea>
                    `;
                    break;
                case "사진 첨부":
                case "파일 첨부":
                    content = `
                        <div class="section-header">
                            <span class="section-title-text">${sectionIndex}. 제목 입력</span>
                            <input type="text" class="section-title-input" value="제목 입력" style="display: none;">
                            <button class="delete-btn">✕</button>
                        </div>
                        <input type="text" id="ohcomment" placeholder="설명 입력">
                        <textarea placeholder="해당 내용을 입력하세요"></textarea>
                    `;
                    break;
            }

            newSection.innerHTML = content;
            document.querySelector(".add-section").before(newSection);
            popup.style.display = "none";

            // ✅ 새로 추가된 섹션에서도 복수선택 버튼 토글 작동하게 만들기
            const multiBtns = newSection.querySelectorAll(".mode-btn");
            multiBtns.forEach(btn => {
                btn.addEventListener("click", () => {
                    multiBtns.forEach(b => b.classList.remove("selected-tag"));
                    btn.classList.add("selected-tag");
                });
            });


            const outlineList = document.querySelector(".outline-list");
            const tocItem = document.createElement("li");
            const tocLink = document.createElement("a");
            tocLink.href = `#section${sectionIndex}`;
            tocLink.textContent = `${sectionIndex}. 제목 입력`;
            tocItem.appendChild(tocLink);
            outlineList.appendChild(tocItem);

            const titleInput = newSection.querySelector(".section-title-input");
            titleInput?.addEventListener("input", () => {
                tocLink.textContent = `${sectionIndex}. ${titleInput.value || "제목 입력"}`;
            });

            sections = document.querySelectorAll(".resume-section");
        });
    });

    // 태그 입력 처리
    document.addEventListener("keydown", (e) => {
        if (e.target.classList.contains("tag-input") && e.key === "Enter") {
            e.preventDefault();
            const value = e.target.value.trim();
            if (!value) return;

            const tagList = e.target.previousElementSibling;
            const tag = document.createElement("span");
            tag.className = "tag";
            tag.innerHTML = `<span class="tag-label">${value}</span><span class="tag-remove">✕</span>`;
            tagList.appendChild(tag);
            e.target.value = "";
        }
    });

    // 섹션 강조 표시
    sections.forEach(section => {
        section.addEventListener("click", () => {
            sections.forEach(s => s.classList.remove("selected"));
            section.classList.add("selected");
        });
    });

    // 복수선택 on/off
    multiOnBtn?.addEventListener("click", () => {
        multiOnBtn.classList.add("selected-tag");
        multiOffBtn.classList.remove("selected-tag");
        document.querySelectorAll(".resume-section").forEach(section => {
            section.setAttribute("data-multi-select", "true");
        });
    });

    multiOffBtn?.addEventListener("click", () => {
        multiOnBtn.classList.remove("selected-tag");
        multiOffBtn.classList.add("selected-tag");
        document.querySelectorAll(".resume-section").forEach(section => {
            section.setAttribute("data-multi-select", "false");
        });
    });

    // 희망 직무 태그
    jobInput?.addEventListener("keydown", e => {
        if (e.key === "Enter" && jobInput.value.trim()) {
            e.preventDefault();
            const text = jobInput.value.trim();
            const tag = document.createElement("span");
            tag.className = "tag";
            tag.innerHTML = `<span class="tag-label">${text}</span><span class="tag-remove">✕</span>`;
            jobTagContainer.appendChild(tag);
            jobInput.value = "";
        }
    });

    // 태그 선택/삭제
    document.addEventListener("click", (e) => {
        const tag = e.target.closest(".tag");
        if (tag && !e.target.classList.contains("tag-remove")) {
            tag.classList.toggle("selected-tag");
        }
        if (e.target.classList.contains("tag-remove")) {
            tag?.remove();
        }
    });

    // 저장 버튼
    document.querySelector(".save-btn")?.addEventListener("click", () => {
        const title = document.getElementById("resumeTitle")?.value.trim();
        if (!title) return alert("제목을 입력해주세요!");

        const sectionsData = [];
        document.querySelectorAll(".resume-section").forEach(section => {
            const headerText = section.querySelector(".section-header span")?.textContent.trim();
            const sectionTitle = headerText?.split(". ")[1] || "제목 없음";
            const comment = section.querySelector("#ohcomment")?.value || "";
            const textarea = section.querySelector("textarea");
            const content = textarea ? textarea.value : "";

            const selectedTags = Array.from(section.querySelectorAll(".tag-list .tag-label"))
                .map(tag => tag.textContent.trim());
            const multiSelect = section.querySelector(".mode-btn.selected-tag")?.textContent.includes("⭕") || false;

            sectionsData.push({
                type: section.querySelector("select") ? "선택형" : "서술형",
                title: sectionTitle,
                comment,
                tags: selectedTags,
                content,
                multiSelect
            });
        });

        const jobTags = Array.from(jobTagContainer.querySelectorAll(".tag .tag-label"))
            .map(tag => tag.textContent.trim());

        const resumeData = { title, sections: sectionsData, jobTags };

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        fetch("/api/coresumes", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(resumeData)
        })
            .then(res => res.json())
            .then(() => alert("저장 완료!"))
            .catch(err => {
                alert("저장 중 오류 발생");
                console.error(err);
            });
    });

    // 제목 수정
    document.addEventListener("click", function (e) {
        const titleSpan = e.target.closest(".section-title-text");
        if (titleSpan) {
            const header = titleSpan.closest(".section-header");
            const input = header.querySelector(".section-title-input");
            const number = titleSpan.textContent.split(".")[0];
            const currentTitle = titleSpan.textContent.replace(`${number}. `, "");
            input.value = currentTitle;
            titleSpan.style.display = "none";
            input.style.display = "inline-block";
            input.focus();
        }
    });

    document.addEventListener("keydown", function (e) {
        if (e.target.classList.contains("section-title-input") && e.key === "Enter") {
            e.preventDefault();
            const input = e.target;
            const header = input.closest(".section-header");
            const span = header.querySelector(".section-title-text");
            const number = span.textContent.split(".")[0];
            const newTitle = input.value.trim() || "제목 없음";
            span.textContent = `${number}. ${newTitle}`;
            input.style.display = "none";
            span.style.display = "inline-block";

            const section = input.closest(".resume-section");
            const tocLink = document.querySelector(`.outline-list a[href="#${section.id}"]`);
            if (tocLink) tocLink.textContent = `${number}. ${newTitle}`;
        }
    });

    // 삭제
    document.addEventListener("click", (e) => {
        if (e.target.classList.contains("delete-btn")) {
            const section = e.target.closest(".resume-section");
            const sectionId = section.id;
            section.remove();
            const tocLink = document.querySelector(`.outline-list a[href="#${sectionId}"]`);
            tocLink?.closest("li")?.remove();
        }
    });
});


// 목차 클릭 → 스크롤
    document.querySelectorAll(".outline-list a").forEach(link => {
        link.addEventListener("click", e => {
            e.preventDefault();
            const targetId = e.target.getAttribute("href").substring(1);
            const target = document.getElementById(targetId);
            if (target) {
                target.scrollIntoView({ behavior: "smooth", block: "center" });
            }
        });
    });

    // 섹션 및 목차 동기화
function reorderSectionsAndToc() {
    const allSections = document.querySelectorAll(".resume-section");
    const allTocLinks = document.querySelectorAll(".outline-list a");

    allSections.forEach((section, idx) => {
        const newNumber = idx + 1;
        section.id = `section${newNumber}`;

        const header = section.querySelector(".section-header");
        const span = header.querySelector(".section-title-text");
        const input = header.querySelector(".section-title-input");
        const currentTitle = input?.value || span?.textContent.replace(/^\d+\.\s*/, "") || "제목 입력";

        span.textContent = `${newNumber}. ${currentTitle}`;
        if (input) input.value = currentTitle;

        const tocLink = allTocLinks[idx];
        if (tocLink) {
            tocLink.textContent = `${newNumber}. ${currentTitle}`;
            tocLink.setAttribute("href", `#section${newNumber}`);
        }
    });

    // ✅ 기존 섹션에서도 복수선택 버튼 토글 작동
    document.querySelectorAll(".resume-section").forEach(section => {
        const btns = section.querySelectorAll(".mode-btn");
        btns.forEach(btn => {
            btn.addEventListener("click", () => {
                btns.forEach(b => b.classList.remove("selected-tag"));
                btn.classList.add("selected-tag");
            });
        });
    });
    }

