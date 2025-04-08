document.addEventListener("DOMContentLoaded", () => {
    const sections = document.querySelectorAll(".resume-section");
    const jobInput = document.getElementById("job-input");
    const jobTagContainer = document.querySelector(".job-tags");
    const multiOnBtn = document.getElementById("multi-on");
    const multiOffBtn = document.getElementById("multi-off");
    const addBtn = document.getElementById("add-section");
    const popup = document.getElementById("section-popup");

    let multiSelect = true;
    let sectionCount = document.querySelectorAll(".resume-section").length;

    // ✅ 섹션 클릭 시 강조
    sections.forEach(section => {
        section.addEventListener("click", () => {
            sections.forEach(s => s.classList.remove("selected"));
            section.classList.add("selected");
        });
    });

    // ✅ 태그 클릭 처리
    document.querySelectorAll(".tag-list").forEach(container => {
        const isSingleMode = container.dataset.single === "true";

        container.addEventListener("click", e => {
            const tag = e.target.closest(".tag");
            if (!tag || e.target.classList.contains("tag-remove")) return;

            const isDirect = tag.classList.contains("direct-input-tag");

            if (isSingleMode || (!isSingleMode && !multiSelect)) {
                container.querySelectorAll(".tag").forEach(t => t.classList.remove("selected-tag"));
                container.querySelectorAll("input.custom-input").forEach(i => i.remove());
            }

            tag.classList.toggle("selected-tag");

            if (isDirect) {
                const exists = tag.nextElementSibling;
                if (tag.classList.contains("selected-tag") && !(exists && exists.classList.contains("custom-input"))) {
                    const input = document.createElement("input");
                    input.type = "text";
                    input.placeholder = "직접 입력";
                    input.className = "custom-input";
                    input.style.marginLeft = "10px";
                    input.addEventListener("keydown", (ev) => {
                        if (ev.key === "Enter") ev.preventDefault();
                    });
                    tag.insertAdjacentElement("afterend", input);
                    input.focus();
                } else if (!tag.classList.contains("selected-tag")) {
                    const input = tag.nextElementSibling;
                    if (input && input.classList.contains("custom-input")) {
                        input.remove();
                    }
                }
            } else {
                container.querySelectorAll(".direct-input-tag").forEach(dtag => {
                    dtag.classList.remove("selected-tag");
                    const input = dtag.nextElementSibling;
                    if (input && input.classList.contains("custom-input")) {
                        input.remove();
                    }
                });
            }
        });

        container.addEventListener("click", e => {
            if (e.target.classList.contains("tag-remove")) {
                const tag = e.target.closest(".tag");
                if (tag) tag.remove();
            }
        });
    });

    // ✅ 복수선택 토글
    multiOnBtn?.addEventListener("click", () => {
        multiSelect = true;
        multiOnBtn.classList.add("selected-tag");
        multiOffBtn.classList.remove("selected-tag");
    });

    multiOffBtn?.addEventListener("click", () => {
        multiSelect = false;
        multiOnBtn.classList.remove("selected-tag");
        multiOffBtn.classList.add("selected-tag");

        const selected = jobTagContainer.querySelectorAll(".selected-tag");
        if (selected.length > 1) {
            selected.forEach((tag, i) => i > 0 && tag.classList.remove("selected-tag"));
        }
    });

    // ✅ 희망직무 입력
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

    // ✅ 저장 버튼
    document.querySelector(".save-btn")?.addEventListener("click", () => {
        const title = document.getElementById("resumeTitle")?.value;
        if (!title?.trim()) {
            alert("제목을 입력해주세요!");
        } else {
            alert("저장되었습니다.");
        }
    });

    // ✅ 삭제 버튼
    document.addEventListener("click", (e) => {
        if (e.target.classList.contains("delete-btn")) {
            const section = e.target.closest(".resume-section");
            if (section && section.id !== "section1") {
                const sectionId = section.id;
                section.remove();

                // 목차에서도 해당 항목 제거
                const tocLink = document.querySelector(`.outline-list a[href="#${sectionId}"]`);
                if (tocLink) {
                    tocLink.closest("li").remove();
                }

                // ✅ 번호 및 제목 동기화
                reorderSectionsAndToc();
            }
        }
    });


    // ✅ 목차 클릭 → 스크롤
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

    // ✅ ➕ 버튼 클릭 시 팝업 위치 설정
    addBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        const rect = addBtn.getBoundingClientRect();
        popup.style.top = `${rect.bottom + window.scrollY + 5}px`;
        popup.style.left = `${rect.left + window.scrollX}px`;
        popup.style.display = popup.style.display === "block" ? "none" : "block";
    });

    // ✅ 팝업 바깥 클릭 시 닫기
    document.addEventListener("click", function (e) {
        if (!popup.contains(e.target) && e.target !== addBtn) {
            popup.style.display = "none";
        }
    });

    document.querySelectorAll(".popup-option").forEach(option => {
        option.addEventListener("click", () => {
            const type = option.textContent.trim();

            // ✅ 현재 존재하는 섹션 수 기준으로 정확히 계산
            const sectionIndex = document.querySelectorAll(".resume-section").length + 1;

            const newSection = document.createElement("section");
            newSection.className = "resume-section";
            newSection.id = `section${sectionIndex}`;

            // ✅ 너의 타입에 맞게 content 조립
            let content = `
            <div class="section-header">
                <span class="section-title-text">${sectionIndex}. 제목 입력</span>
                <input type="text" class="section-title-input" value="제목 입력" style="display: none;">
                <button class="delete-btn">✕</button>
            </div>
            <input type="text" id="ohcomment" placeholder="설명 입력">
            <textarea placeholder="구직자 답변 입력란"></textarea>
        `;

            newSection.innerHTML = content;
            document.querySelector(".add-section").before(newSection);
            popup.style.display = "none";

            // ✅ 목차에 정확한 번호로 추가
            const outlineList = document.querySelector(".outline-list");
            const tocItem = document.createElement("li");
            const tocLink = document.createElement("a");
            tocLink.href = `#section${sectionIndex}`;
            tocLink.textContent = `${sectionIndex}. 제목 입력`;
            tocItem.appendChild(tocLink);
            outlineList.appendChild(tocItem);

            // ✅ 제목 실시간 반영
            const titleInput = newSection.querySelector(".section-title-input");
            titleInput?.addEventListener("input", () => {
                tocLink.textContent = `${sectionIndex}. ${titleInput.value || "제목 입력"}`;
            });
        });
    });


    // ✅ 선택형 섹션 내 태그 추가 처리
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

    // ✅ 태그 삭제
    document.addEventListener("click", (e) => {
        if (e.target.classList.contains("tag-remove")) {
            const tag = e.target.closest(".tag");
            if (tag) tag.remove();
        }
    });

    document.addEventListener("click", function (e) {
        const titleSpan = e.target.closest(".section-title-text");
        if (titleSpan) {
            const header = titleSpan.closest(".section-header");
            const input = header.querySelector(".section-title-input");

            // 현재 제목 텍스트만 숫자 빼고 추출
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
        }
    });

// 엔터로 제목 수정 마무리할 때도 목차 동기화 추가
    document.addEventListener("keydown", function (e) {
        if (e.target.classList.contains("section-title-input") && e.key === "Enter") {
            const input = e.target;
            const header = input.closest(".section-header");
            const span = header.querySelector(".section-title-text");
            const number = span.textContent.split(".")[0];
            const newTitle = input.value.trim() || "제목 없음";

            span.textContent = `${number}. ${newTitle}`;
            input.style.display = "none";
            span.style.display = "inline-block";

            // 👉 여기 추가
            const section = input.closest(".resume-section");
            const sectionId = section.id;
            const tocLink = document.querySelector(`.outline-list a[href="#${sectionId}"]`);
            if (tocLink) tocLink.textContent = `${number}. ${newTitle}`;
        }
    });
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

            // ✅ 목차도 인덱스로 동기화
            const tocLink = allTocLinks[idx];
            if (tocLink) {
                tocLink.textContent = `${newNumber}. ${currentTitle}`;
                tocLink.setAttribute("href", `#section${newNumber}`);
            }
        });
    }

});
