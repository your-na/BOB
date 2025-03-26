document.addEventListener("DOMContentLoaded", () => {
    const sections = document.querySelectorAll(".resume-section");
    const jobInput = document.getElementById("job-input");
    const jobTagContainer = document.querySelector(".job-tags");
    const multiOnBtn = document.getElementById("multi-on");
    const multiOffBtn = document.getElementById("multi-off");
    let multiSelect = true;

    // 섹션 클릭 시 강조
    sections.forEach(section => {
        section.addEventListener("click", () => {
            sections.forEach(s => s.classList.remove("selected"));
            section.classList.add("selected");
        });
    });

    // 태그 클릭 처리
    document.querySelectorAll(".tag-list").forEach(container => {
        const isSingleMode = container.dataset.single === "true";

        container.addEventListener("click", e => {
            const tag = e.target.closest(".tag");
            if (!tag || e.target.classList.contains("tag-remove")) return;

            const isDirect = tag.classList.contains("direct-input-tag");

            // 단일선택: 항상 하나만 선택되게
            if (isSingleMode || (!isSingleMode && !multiSelect)) {
                container.querySelectorAll(".tag").forEach(t => t.classList.remove("selected-tag"));
                container.querySelectorAll("input.custom-input").forEach(i => i.remove());
            }

            tag.classList.toggle("selected-tag");

            // 직접입력 처리
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
                // 직접입력 태그는 해제 및 input 제거
                container.querySelectorAll(".direct-input-tag").forEach(dtag => {
                    dtag.classList.remove("selected-tag");
                    const input = dtag.nextElementSibling;
                    if (input && input.classList.contains("custom-input")) {
                        input.remove();
                    }
                });
            }
        });

        // ✕ 버튼으로 삭제
        container.addEventListener("click", e => {
            if (e.target.classList.contains("tag-remove")) {
                const tag = e.target.closest(".tag");
                if (tag) tag.remove();
            }
        });
    });

    // 복수선택 토글
    multiOnBtn.addEventListener("click", () => {
        multiSelect = true;
        multiOnBtn.classList.add("selected-tag");
        multiOffBtn.classList.remove("selected-tag");
    });

    multiOffBtn.addEventListener("click", () => {
        multiSelect = false;
        multiOnBtn.classList.remove("selected-tag");
        multiOffBtn.classList.add("selected-tag");

        const selected = jobTagContainer.querySelectorAll(".selected-tag");
        if (selected.length > 1) {
            selected.forEach((tag, i) => i > 0 && tag.classList.remove("selected-tag"));
        }
    });

    // 희망직무 입력
    jobInput.addEventListener("keydown", e => {
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

    // 삭제 버튼
    document.querySelectorAll(".delete-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            const section = btn.closest(".resume-section");
            if (section && section.id !== "section1") section.remove();
        });
    });

    // 목차 클릭 → 섹션 스크롤
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

    // 저장 버튼
    document.querySelector(".save-btn")?.addEventListener("click", () => {
        const title = document.getElementById("resumeTitle")?.value;
        if (!title?.trim()) {
            alert("제목을 입력해주세요!");
        } else {
            alert("저장되었습니다.");
        }
    });
});
