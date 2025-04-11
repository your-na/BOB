document.addEventListener("DOMContentLoaded", () => {
    const sections = document.querySelectorAll(".resume-section");
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
        popup.style.top = `${rect.bottom + window.scrollY + 5}px`;  // 위치 설정
        popup.style.left = `${rect.left + window.scrollX}px`;  // 위치 설정

        // 팝업 상태 토글
        if (popup.style.display === "block") {
            console.log("팝업 닫기");
            popup.style.display = "none";
        } else {
            console.log("팝업 열기");
            popup.style.display = "block";
        }
    });

    // ✅ 팝업 바깥 클릭 시 닫기
    document.addEventListener("click", function (e) {
        if (!popup.contains(e.target) && e.target !== addBtn) {
            popup.style.display = "none";
            console.log("팝업 닫기 (바깥 클릭)");
        }
    });

    // ✅ 팝업 옵션 클릭 → 섹션 추가
    document.querySelectorAll(".popup-option").forEach(option => {
        option.addEventListener("click", () => {
            const type = option.textContent.trim();  // 클릭된 옵션 가져오기
            const sectionIndex = document.querySelectorAll(".resume-section").length + 1;  // 섹션 인덱스 설정

            const newSection = document.createElement("section");
            newSection.className = "resume-section";
            newSection.id = `section${sectionIndex}`;

            let content = "";
            let multiSelect = "false";  // 기본적으로 복수선택은 비활성화

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
                    multiSelect = "true"; // "선택형" 섹션에만 복수선택을 활성화
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
                    content = `
                        <div class="section-header">
                            <span class="section-title-text">${sectionIndex}. 제목 입력</span>
                            <input type="text" class="section-title-input" value="제목 입력" style="display: none;">
                            <button class="delete-btn">✕</button>
                        </div>
                        <input type="text" id="ohcomment" placeholder="설명 입력">
                        <textarea placeholder="구직자 사진 입력란"></textarea>
                    `;
                    break;
                case "파일 첨부":
                    content = `
                        <div class="section-header">
                            <span class="section-title-text">${sectionIndex}. 제목 입력</span>
                            <input type="text" class="section-title-input" value="제목 입력" style="display: none;">
                            <button class="delete-btn">✕</button>
                        </div>
                        <input type="text" id="ohcomment" placeholder="설명 입력">
                        <textarea placeholder="구직자 파일 첨부란"></textarea>
                    `;
                    break;
            }

            newSection.innerHTML = content;
            document.querySelector(".add-section").before(newSection);  // 새 섹션 추가
            popup.style.display = "none";  // 팝업 닫기

            // 목차에 추가
            const outlineList = document.querySelector(".outline-list");
            const tocItem = document.createElement("li");
            const tocLink = document.createElement("a");
            tocLink.href = `#section${sectionIndex}`;
            tocLink.textContent = `${sectionIndex}. 제목 입력`;
            tocItem.appendChild(tocLink);
            outlineList.appendChild(tocItem);

            // 섹션 제목 수정
            const titleInput = newSection.querySelector(".section-title-input");
            titleInput?.addEventListener("input", () => {
                tocLink.textContent = `${sectionIndex}. ${titleInput.value || "제목 입력"}`;
            });

            // 새로 추가된 섹션에 대해 복수선택 활성화 여부 설정
            if (multiSelect === "true") {
                newSection.setAttribute("data-multi-select", "true");
                console.log(`선택형 섹션에 data-multi-select="true" 설정됨`);
            }
        });
    });

    // ✅ 복수선택 활성화 버튼 클릭 시
    multiOnBtn?.addEventListener("click", () => {
        multiSelect = true;  // 복수선택 활성화
        multiOnBtn.classList.add("selected-tag");
        multiOffBtn.classList.remove("selected-tag");

        // 희망직무 섹션에 대해서만 data-multi-select 값을 true로 설정
        const hopeJobSection = document.getElementById("section3");  // 희망직무 섹션
        if (hopeJobSection) {
            hopeJobSection.setAttribute("data-multi-select", "true");
            console.log("희망직무 섹션 복수선택 활성화됨");
        }
    });

// ✅ 복수선택 비활성화 버튼 클릭 시
    multiOffBtn?.addEventListener("click", () => {
        multiSelect = false;  // 복수선택 비활성화
        multiOnBtn.classList.remove("selected-tag");
        multiOffBtn.classList.add("selected-tag");

        // 희망직무 섹션에 대해서만 data-multi-select 값을 false로 설정
        const hopeJobSection = document.getElementById("section3");  // 희망직무 섹션
        if (hopeJobSection) {
            hopeJobSection.setAttribute("data-multi-select", "false");
            console.log("희망직무 섹션 복수선택 비활성화됨");
        }

        // 선택된 태그 중 첫 번째만 남기고 나머지 비활성화
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

    // ✅ 선택 항목 처리 (조건 선택)
    document.querySelectorAll(".tag-list").forEach(container => {
        container.addEventListener("click", (e) => {
            const tag = e.target.closest(".tag");
            if (!tag || e.target.classList.contains("tag-remove")) return;

            // 선택된 태그 항목은 `selected-tag` 클래스를 통해 상태를 추적
            tag.classList.toggle("selected-tag");

            const tagText = tag.textContent.trim();
            console.log(`선택된 태그: ${tagText}`);
        });
    });

    // ✅ 저장 버튼 클릭 시 데이터 준비
    document.querySelector(".save-btn")?.addEventListener("click", () => {
        const title = document.getElementById("resumeTitle")?.value.trim();
        if (!title) {
            alert("제목을 입력해주세요!");
            return;
        }

        const sectionsData = [];
        document.querySelectorAll(".resume-section").forEach(section => {
            const headerText = section.querySelector(".section-header span")?.textContent.trim();
            const sectionTitle = headerText?.split(". ")[1] || "제목 없음";
            const type = sectionTitle.includes("자기소개") || section.querySelector("textarea") ? "서술형" : "선택형";
            const comment = section.querySelector("#ohcomment")?.value || "";
            const multiSelect = section.getAttribute("data-multi-select") === "true";  // 복수선택 여부

            const selectedConditions = [];
            section.querySelectorAll(".tag-list .selected-tag").forEach(tag => {
                selectedConditions.push(tag.textContent.trim());
            });

            const selectedTags = Array.from(section.querySelectorAll(".tag-list .selected-tag"))
                .map(tag => tag.textContent.trim());

            sectionsData.push({
                type,
                title: sectionTitle,
                comment,
                selectedTags,
                conditions: selectedConditions,
                multiSelect // 복수선택 여부
            });
        });

        const jobTags = [];
        jobTagContainer.querySelectorAll(".tag").forEach(tag => {
            jobTags.push(tag.querySelector(".tag-label").textContent.trim());
        });

        const resumeData = {
            title,
            sections: sectionsData,
            jobTags
        };

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
            .then(response => response.json())
            .then(data => {
                alert("저장 완료!");
            })
            .catch(error => {
                alert("저장 중 오류가 발생했습니다.");
                console.error(error);
            });
    });

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

// ✅ 태그 삭제
document.addEventListener("click", (e) => {
    if (e.target.classList.contains("tag-remove")) {
        const tag = e.target.closest(".tag");
        if (tag) tag.remove();
    }
});

// ✅ 제목 수정
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
        const sectionId = section.id;
        const tocLink = document.querySelector(`.outline-list a[href="#${sectionId}"]`);
        if (tocLink) tocLink.textContent = `${number}. ${newTitle}`;
    }
});

// ✅ 섹션 및 목차 동기화
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
}
