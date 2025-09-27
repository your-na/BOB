/***********************
 * 공통 유틸
 ***********************/
function populateYearOptions(selectElement, year) {
    if (!selectElement || !year) return;
    const exists = Array.from(selectElement.options).some(opt => opt.value === year);
    if (!exists) {
        const opt = document.createElement("option");
        opt.value = year;
        opt.textContent = year;
        selectElement.appendChild(opt);
    }
}

/***********************
 * EDUCATION 드롭 바인딩 (신/구 마크업 모두 대응)
 ***********************/
function bindDropToEducationItem(item) {
    item.addEventListener('dragover', e => e.preventDefault());
    item.addEventListener('drop', e => {
        e.preventDefault();
        const text = e.dataTransfer.getData('application/json');
        if (!text) return;

        try {
            const data = JSON.parse(text);
            if (data.type !== 'EDUCATION') return;

            const schoolInput = item.querySelector('input[placeholder="학교명"]');
            const majorInput  = item.querySelector('input[placeholder="학과"]');
            const statusInput = item.querySelector('.status-input');
            const dateGroup   = item.querySelector('.date-group2');

            if (schoolInput) schoolInput.value = data.schoolName || '';
            if (majorInput)  majorInput.value  = data.majorName  || '';
            if (statusInput) statusInput.value = data.status     || '';

            // 🔥 payload에서 날짜 가져오기
            const [sy, sm = ''] = (data.startDate || '').split('-');
            const [ey, em = ''] = (data.endDate   || '').split('-');

            if (dateGroup) {
                // 새 마크업 대응
                const ySelects = dateGroup.querySelectorAll('.year-select');
                const mSelects = dateGroup.querySelectorAll('.month-select');

                if (ySelects.length >= 2) {
                    if (ySelects[0]) ySelects[0].value = sy || '';
                    if (ySelects[1]) ySelects[1].value = ey || '';
                }
                if (mSelects.length >= 2) {
                    if (mSelects[0]) mSelects[0].value = sm || '';
                    if (mSelects[1]) mSelects[1].value = em || '';
                }
            }
        } catch (err) {
            console.error("교육 drop 파싱 오류:", err);
        }
    });
}

// 경력 섹션 드롭 처리
const section4 = document.getElementById("section4");
section4.addEventListener("dragover", (e) => { e.preventDefault(); });
section4.addEventListener("drop", (e) => {
    e.preventDefault();
    const data = JSON.parse(e.dataTransfer.getData("application/json"));

    if (data.type === "JOB") {
        // 첫 번째 경력 아이템 선택
        const careerItem = section4.querySelector(".career-item");
        if (careerItem) {
            // 회사명, 직업명
            careerItem.querySelector(".workplace-input").value = data.title || "";
            if (careerItem.querySelector(".status-input")) {
                careerItem.querySelector(".status-input").value = data.status || "";
            }

            // 시작일
            if (data.startDate) {
                const [sy, sm] = data.startDate.split("-");
                careerItem.querySelectorAll(".year-input")[0].value = sy;
                careerItem.querySelectorAll(".month-input")[0].value = sm;
            }

            // 종료일
            if (data.endDate) {
                const [ey, em] = data.endDate.split("-");
                careerItem.querySelectorAll(".year-input")[1].value = ey;
                careerItem.querySelectorAll(".month-input")[1].value = em;
            }
        }
        
    }
});

// 포트폴리오 섹션 드롭 처리
const section5 = document.getElementById("section5");
section5.addEventListener("dragover", (e) => { e.preventDefault(); });
section5.addEventListener("drop", (e) => {
    e.preventDefault();
    const data = JSON.parse(e.dataTransfer.getData("application/json"));

    if (data.type === "PROJECT") {
        // 첫 번째 포트폴리오 아이템 선택
        const portfolioItem = section5.querySelector(".portfolio-item");
        if (portfolioItem) {
            // 프로젝트명
            portfolioItem.querySelector(".portfolio-title").value = data.title || "";

            // 시작일
            if (data.startDate) {
                const [sy, sm] = data.startDate.split("-");
                portfolioItem.querySelectorAll(".year-input")[0].value = sy;
                portfolioItem.querySelectorAll(".month-input")[0].value = sm;
            }

            // 종료일
            if (data.endDate) {
                const [ey, em] = data.endDate.split("-");
                portfolioItem.querySelectorAll(".year-input")[1].value = ey;
                portfolioItem.querySelectorAll(".month-input")[1].value = em;
            }
            const pathInput = portfolioItem.querySelector(".portfolio-file-path");
            if (pathInput) {
                pathInput.value = data.file || "";
            }

            const hiddenInput = portfolioItem.querySelector("input[name='filePath']");
            if (hiddenInput) {
                hiddenInput.value = data.file || "";
            }
        }



    }
});


/***********************
 * 섹션/팝업/태그/저장 등 메인 UI
 ***********************/
document.addEventListener("DOMContentLoaded", () => {
    let sections = document.querySelectorAll(".resume-section");
    const addBtn = document.getElementById("add-section");
    const popup = document.getElementById("section-popup");
    const jobTagContainer = document.querySelector(".job-tags");

    // ➕ 버튼 클릭 시 팝업 위치 설정 (가드)
    if (addBtn) {
        addBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            const rect = addBtn.getBoundingClientRect();
            popup.style.top = `${rect.bottom + window.scrollY + 5}px`;
            popup.style.left = `${rect.left + window.scrollX}px`;
            popup.style.display = popup.style.display === "block" ? "none" : "block";
        });
    }

    document.addEventListener("click", (e) => {
        const target = e.target;

        // 팝업 바깥 클릭 시 닫기
        if (popup && !popup.contains(target) && target !== addBtn) {
            popup.style.display = "none";
        }

        // 제목 클릭 → input 전환
        const titleSpan = target.closest(".section-title-text");
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

        // 태그 선택
        const tag = target.closest(".tag");
        if (tag && !target.classList.contains("tag-remove")) {
            const container = tag.closest(".tag-list");
            const isSingle = container?.dataset.single === "true";

            if (isSingle) {
                container.querySelectorAll(".tag").forEach(t => t.classList.remove("selected-tag"));
                container.querySelectorAll("input.custom-input").forEach(i => i.remove());
            }

            tag.classList.toggle("selected-tag");

            // 직접입력 input 처리
            const isDirect = tag.classList.contains("direct-input-tag");
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
                // 다른 태그 클릭 시 직접입력 input 제거
                const allDirect = container?.querySelectorAll(".direct-input-tag") || [];
                allDirect.forEach(dtag => {
                    dtag.classList.remove("selected-tag");
                    const input = dtag.nextElementSibling;
                    if (input && input.classList.contains("custom-input")) {
                        input.remove();
                    }
                });
            }
        }

        // 태그 삭제
        if (target.classList.contains("tag-remove")) {
            const t = target.closest(".tag");
            if (t) t.remove();
        }

        // 섹션 삭제
        if (target.classList.contains("delete-btn")) {
            const section = target.closest(".resume-section");
            const sectionId = section.id;
            section.remove();
            const tocLink = document.querySelector(`.outline-list a[href="#${sectionId}"]`);
            tocLink?.closest("li")?.remove();
            // 번호/목차 재정렬
            reorderSectionsAndToc();
        }
    });

    // 팝업 옵션으로 새 섹션 추가
    document.querySelectorAll(".popup-option").forEach(option => {
        option.addEventListener("click", () => {
            const type = option.textContent.trim();

            // 마지막 섹션 번호
            const allSections = document.querySelectorAll(".resume-section");
            let lastNumber = 0;
            if (allSections.length > 0) {
                const lastSection = allSections[allSections.length - 1];
                const match = lastSection.id.match(/section(\d+)/);
                if (match) lastNumber = parseInt(match[1], 10);
            }
            const sectionIndex = lastNumber + 1;

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
                    content = `
                        <div class="section-header">
                            <span class="section-title-text">${sectionIndex}. 제목 입력</span>
                            <input type="text" class="section-title-input" value="제목 입력" style="display: none;">
                            <button class="delete-btn">✕</button>
                        </div>
                        <input type="text" id="ohcomment" placeholder="설명 입력">
                        <input type="file" accept="image/*" multiple class="file-input">
                        <div class="upload-preview"></div>
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
                        <input type="file" multiple class="file-input">
                        <div class="upload-preview"></div>
                    `;
                    break;
            }

            newSection.innerHTML = content;
            const addSectionAnchor = document.querySelector(".add-section");
            if (addSectionAnchor) addSectionAnchor.before(newSection);
            if (popup) popup.style.display = "none";

            // 복수선택 토글
            const multiBtns = newSection.querySelectorAll(".mode-btn");
            multiBtns.forEach(btn => {
                btn.addEventListener("click", () => {
                    multiBtns.forEach(b => b.classList.remove("selected-tag"));
                    btn.classList.add("selected-tag");
                });
            });

            // 목차 동기화
            const outlineList = document.querySelector(".outline-list");
            if (outlineList) {
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
            }

            sections = document.querySelectorAll(".resume-section");
            reorderSectionsAndToc();
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

    // 직무 입력창에서 엔터로 태그 추가
    document.addEventListener("keydown", function (e) {
        if (e.target.id === "job-input" && e.key === "Enter") {
            e.preventDefault();
            const value = e.target.value.trim();
            if (!value) return;

            const tagList = document.querySelector(".job-tags");
            const tag = document.createElement("span");
            tag.className = "tag";
            tag.innerHTML = `
              <span class="tag-label">${value}</span>
              <span class="tag-remove">✕</span>
            `;
            tagList.appendChild(tag);
            e.target.value = "";
        }
    });

    // 저장 버튼 (개선: 상태/기간/타입 판별 보강)
    document.querySelector(".save-btn")?.addEventListener("click", () => {
        const title = document.getElementById("resumeTitle")?.value.trim();
        if (!title) return alert("제목을 입력해주세요!");

        const sectionsData = [];
        document.querySelectorAll(".resume-section").forEach(section => {
            let sectionTitle = "";
            const titleSpan = section.querySelector(".section-header span");
            const titleInput = section.querySelector(".section-title-input");

            if (titleInput && titleInput.value.trim()) {
                sectionTitle = titleInput.value.trim();
            } else if (titleSpan && titleSpan.textContent.trim()) {
                const raw = titleSpan.textContent.trim();
                sectionTitle = raw.includes(". ") ? raw.split(". ")[1] : raw;
            } else {
                sectionTitle = "제목 없음";
            }

            const comment = section.querySelector("#ohcomment")?.value || "";
            let content = "";

            if (sectionTitle.includes("학력")) {
                const school = section.querySelector('input[placeholder="학교명"]')?.value || '';
                const major  = section.querySelector('input[placeholder="학과"]')?.value  || '';
                const status =
                    section.querySelector('.status-input')?.value ||
                    section.querySelector('select')?.value ||
                    '';

                // 기간: input(year/month) 우선 → 없으면 select(연도)
                let start = '', end = '';
                const dg = section.querySelector('.date-group2');
                if (dg) {
                    const yInputs = dg.querySelectorAll('.year-input');
                    const mInputs = dg.querySelectorAll('.month-input');
                    const sy = yInputs?.[0]?.value || '';
                    const sm = mInputs?.[0]?.value || '';
                    const ey = yInputs?.[1]?.value || '';
                    const em = mInputs?.[1]?.value || '';

                    if (sy || ey || sm || em) {
                        start = [sy, sm].filter(Boolean).join('-');
                        end   = [ey, em].filter(Boolean).join('-');
                    } else {
                        const years = dg.querySelectorAll('select');
                        start = years[0]?.value || '';
                        end   = years[1]?.value || '';
                    }
                }

                content = `학교: ${school} / 학과: ${major} / 상태: ${status} / 기간: ${start} ~ ${end}`;
            } else {
                const textarea = section.querySelector("textarea");
                content = textarea ? textarea.value : "";
            }

            const selectedConditions = [];
            section.querySelectorAll(".tag-list .selected-tag").forEach(tag => {
                selectedConditions.push(tag.textContent.trim());
            });

            const selectedTags = Array.from(section.querySelectorAll(".tag-list .tag-label"))
                .map(tag => tag.textContent.trim());
            const multiSelect = section.querySelector(".mode-btn.selected-tag")?.textContent.includes("⭕") || false;

            // 타입 판별 개선
            let type = "서술형";
            if (section.querySelector(".tag-list") || section.querySelector(".tag-input")) {
                type = "선택형";
            } else if (section.querySelector('input.file-input[accept*="image"]')) {
                type = "사진 첨부";
            } else if (section.querySelector('input.file-input:not([accept*="image"])')) {
                type = "파일 첨부";
            }

            // 드래그 아이템 수집
            const dragItems = [];
            section.querySelectorAll(".uploaded-item").forEach(item => {
                const displayText = item.textContent.replace("삭제", "").trim();
                const filePath = item.dataset.file || null;
                const startDate = item.dataset.startDate || null;
                const endDate = item.dataset.endDate || null;

                dragItems.push({
                    displayText,
                    filePath,
                    startDate,
                    endDate
                });
            });

            sectionsData.push({
                type,
                title: sectionTitle,
                comment,
                tags: selectedTags,
                content,
                multiSelect,
                conditions: selectedConditions,
                dragItems
            });
        });

        // (선택) 직무 태그 사용 시 여기에 포함하고 싶다면 주석 해제
        // const jobTags = Array.from(jobTagContainer?.querySelectorAll(".tag .tag-label") || []).map(el => el.textContent.trim());

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        const memberId = document.getElementById("memberId")?.value;

        const resumeData = {
            title,
            memberId: memberId ? parseInt(memberId) : null,
            sections: sectionsData
            // , jobTags
        };

        fetch("/api/myresumes", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(resumeData)
        })
            .then(res => res.json())
            .then(() => {
                alert("저장 완료!");
                window.location.href = "/myresumelist";
            })
            .catch(err => {
                alert("저장 중 오류 발생");
                console.error(err);
            });
    });

    // 제목 인풋 Enter → span 반영
    document.addEventListener("keydown", function (e) {
        if (e.target.classList.contains("section-title-input") && e.key === "Enter") {
            e.preventDefault();
            const input = e.target;
            const header = input.closest(".section-header");
            const span = header.querySelector(".section-title-text");
            const number = span.textContent.split(".")[0];
            const newTitle = input.value.trim() || "제목 없음";
            span.textContent = `${number}. ${newTitle}`;
            input.value = newTitle;
            input.style.display = "none";
            span.style.display = "inline-block";

            const section = input.closest(".resume-section");
            const tocLink = document.querySelector(`.outline-list a[href="#${section.id}"]`);
            if (tocLink) tocLink.textContent = `${number}. ${newTitle}`;
        }
    });
});

/***********************
 * 목차 클릭 → 스크롤
 ***********************/
document.addEventListener("DOMContentLoaded", () => {
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
});

/***********************
 * 섹션/목차 동기화 & 모드버튼 토글 바인딩 보장
 ***********************/
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

    // 기존 섹션에서도 복수선택 버튼 토글 작동
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

/***********************
 * 채팅 열기/닫기
 ***********************/
document.addEventListener("DOMContentLoaded", function () {
    const chatBtn = document.getElementById("openChat");
    const chatBox = document.getElementById("chatBox");
    const closeBtn = document.getElementById("closeChat");

    if (!chatBtn || !chatBox || !closeBtn) return;

    chatBtn.addEventListener("click", () => {
        chatBox.style.display = chatBox.style.display === "flex" ? "none" : "flex";
    });

    closeBtn.addEventListener("click", () => {
        chatBox.style.display = "none";
    });
});

/***********************
 * 채팅 전송
 ***********************/
document.addEventListener("DOMContentLoaded", () => {
    const chatInput = document.getElementById("chatInput");
    const chatBody = document.getElementById("chatBody");
    const sendChat = document.getElementById("sendChat");

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    if (sendChat && chatInput && chatBody) {
        function sendMessage() {
            const message = chatInput.value.trim();
            if (!message) return;

            const userMsg = document.createElement("p");
            userMsg.className = "user";
            userMsg.textContent = message;
            chatBody.appendChild(userMsg);
            chatInput.value = "";

            fetch("/api/chat", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({ message })
            })
                .then(res => {
                    if (!res.ok) throw new Error("응답 오류");
                    return res.text();
                })
                .then(reply => {
                    const aiMsg = document.createElement("p");
                    aiMsg.className = "ai";
                    aiMsg.textContent = reply;
                    chatBody.appendChild(aiMsg);
                    chatBody.scrollTop = chatBody.scrollHeight;
                })
                .catch(err => {
                    const aiMsg = document.createElement("p");
                    aiMsg.className = "ai";
                    aiMsg.textContent = "⚠ Groq 응답 오류가 발생했습니다.";
                    chatBody.appendChild(aiMsg);
                    chatBody.scrollTop = chatBody.scrollHeight;
                    console.error("GPT 오류:", err);
                });
        }

        sendChat.onclick = sendMessage;

        chatInput.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                sendMessage();
            }
        });
    } else {
        console.error("chatInput 또는 sendChat 또는 chatBody가 null입니다.");
    }
});

/***********************
 * 임의 응답 (미사용시 무시)
 ***********************/
function getAiMockResponse(msg) {
    if (msg.includes("자기소개")) {
        return "저는 책임감 있게 성장하는 개발자입니다.";
    }
    if (msg.includes("경력")) {
        return "2023년부터 프론트엔드 개발자로 근무했습니다.";
    }
    return "죄송해요! 해당 질문은 아직 학습되지 않았어요.";
}

/***********************
 * 공통 드롭박스 세팅
 ***********************/
function setupDropBox(box) {
    if (!box) return;

    box.addEventListener('dragover', e => {
        e.preventDefault();
        box.style.border = '2px dashed #4CAF50';
    });

    box.addEventListener('dragleave', () => {
        box.style.border = '1px dashed #ccc';
    });

    box.addEventListener('drop', e => {
        e.preventDefault();
        box.style.border = '1px dashed #ccc';

        // 파일 드롭
        if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
            [...e.dataTransfer.files].forEach(file => {
                const item = document.createElement('div');
                item.className = 'uploaded-item';
                item.textContent = file.name;
                const del = document.createElement('span');
                del.className = 'delete-icon';
                del.textContent = '삭제';
                del.onclick = () => item.remove();
                item.appendChild(del);
                box.appendChild(item);
            });
            return;
        }

        // JSON 드래그(오른쪽 패널 항목)
        const jsonText = e.dataTransfer.getData("application/json");
        if (jsonText) {
            try {
                const data = JSON.parse(jsonText);
                let display = data.title || '';
                if (data.type === 'JOB') {
                    const fmt = s => s ? s.replace(/-/g, '.') : '';
                    if (data.status === '재직') display += ` (재직: ${fmt(data.startDate)} ~)`;
                    else display += ` (퇴직: ${fmt(data.startDate)} ~ ${fmt(data.endDate)})`;
                }
                const item = document.createElement('div');
                item.className = 'uploaded-item';
                item.textContent = display;

                // dataset 보존
                item.dataset.file = (data.file || '').replace(/^\/?download\//, '');
                item.dataset.startDate = data.startDate || '';
                item.dataset.endDate = data.endDate || '';
                item.dataset.displayText = display;
                if (data.id)   item.dataset.id = data.id;
                if (data.type) item.dataset.type = data.type;

                const del = document.createElement('span');
                del.className = 'delete-icon';
                del.textContent = '삭제';
                del.onclick = () => item.remove();
                item.appendChild(del);

                box.appendChild(item);
            } catch { /* 무시 */ }
            return;
        }

        // 일반 텍스트
        const title = e.dataTransfer.getData('text/plain');
        if (title) {
            const item = document.createElement('div');
            item.className = 'uploaded-item';
            item.textContent = title;
            const del = document.createElement('span');
            del.className = 'delete-icon';
            del.textContent = '삭제';
            del.onclick = () => item.remove();
            item.appendChild(del);
            box.appendChild(item);
        }
    });
}

/***********************
 * 탭 활성화
 ***********************/
function activateTab(tabName) {
    document.querySelectorAll('#tab-list .tab').forEach(t => t.classList.remove('active'));
    const activeTab = document.querySelector(`#tab-list .tab[data-tab="${tabName}"]`);
    if (activeTab) activeTab.classList.add('active');

    let isEmpty = true;
    document.querySelectorAll('.tab-content').forEach(c => {
        const show = c.dataset.content === tabName;
        c.style.display = show ? 'block' : 'none';
        if (show) {
            isEmpty = c.querySelectorAll(".award-item").length === 0;
        }
    });

    const empty = document.querySelector('.empty-content[data-content="empty"]');
    if (empty) empty.style.display = isEmpty ? 'block' : 'none';
}

/***********************
 * 우측 패널 데이터 로딩
 ***********************/
function makeDraggable(div, payload) {
    div.setAttribute('draggable', 'true');
    div.addEventListener('dragstart', e => {
        e.dataTransfer.setData('application/json', JSON.stringify(payload));
    });
}

function renderProjects() {
    return fetch('/api/user/resumes/projects')
        .then(res => res.json())
        .then(projects => {
            const cont = document.querySelector('.tab-content[data-content="portfolio"]');
            if (!cont) return;
            cont.innerHTML = '';
            if (!projects || projects.length === 0) return;

            projects.forEach(p => {
                const d = document.createElement('div');
                d.className = 'award-item';
                d.innerHTML = `${p.title}<br><small>${p.submittedDate || ''}</small>`;
                makeDraggable(d, {
                    id: p.id, type: 'PROJECT',
                    file: (p.filePath || '').replace(/^\/?download\//, ''),
                    title: p.title, startDate: p.startDate || '', endDate: p.endDate || ''
                });
                cont.appendChild(d);
            });
        })
        .catch(err => console.error('프로젝트 로드 실패:', err));
}

function renderJobs() {
    return fetch('/api/job-history')
        .then(res => res.json())
        .then(list => {
            const cont = document.querySelector('.tab-content[data-content="job"]');
            if (!cont) return;
            cont.innerHTML = '';
            if (!list || list.length === 0) return;

            list.forEach(it => {
                const start = it.startDate?.replace(/-/g, '.') || '';
                const end = it.endDate?.replace(/-/g, '.') || '';
                const period = it.status === '재직' ? `재직: ${start} ~` : `퇴직: ${start} ~ ${end}`;

                const d = document.createElement('div');
                d.className = 'award-item';
                d.innerHTML = `${it.workplace || '직무 없음'}<br><small>${period}</small>`;
                makeDraggable(d, {
                    id: it.id, type: 'JOB',
                    title: it.workplace || '직무 없음',
                    startDate: it.startDate, endDate: it.endDate, status: it.status
                });
                cont.appendChild(d);
            });
        })
        .catch(err => console.error('구직 이력 로드 실패:', err));
}

/***********************
 * 학력 탭 렌더링 (상태=텍스트, 연/월 input)
 ***********************/
function renderEducations() {
    return fetch('/api/education-history/list')
        .then(res => res.json())
        .then(list => {
            const cont = document.querySelector('.tab-content[data-content="school"]');
            if (!cont) return;
            cont.innerHTML = '';

            // 기존 학력 데이터
            if (list && list.length > 0) {
                list.forEach(edu => {
                    const fmt = d => d?.replace(/-/g, '.');
                    let line2 = '';
                    if (edu.status === '재학')
                        line2 = `재학 ${fmt(edu.startDate)} 학과 ${edu.majorName || ''}`;
                    else if (edu.status === '졸업')
                        line2 = `졸업 ${fmt(edu.startDate)} ~ ${fmt(edu.endDate)} 학과 ${edu.majorName || ''}`;
                    else
                        line2 = `${edu.status || ''} 학과 ${edu.majorName || ''}`;

                    const d = document.createElement('div');
                    d.className = 'award-item';
                    d.innerHTML = `${edu.schoolName}<br><small>${line2}</small>`;
                    makeDraggable(d, {
                        type: 'EDUCATION',
                        schoolName: edu.schoolName,
                        majorName: edu.majorName,
                        status: edu.status,
                        startDate: edu.startDate,
                        endDate: edu.endDate
                    });
                    cont.appendChild(d);
                });
            }

            // 추가 버튼
            const addBtn = document.createElement('button');
            addBtn.className = 'add-school-btn';
            addBtn.textContent = '＋';

            // 버튼 래퍼
            const addBtnWrapper = document.createElement('div');
            addBtnWrapper.style.display = 'flex';
            addBtnWrapper.style.justifyContent = 'center';
            addBtnWrapper.appendChild(addBtn);
            cont.appendChild(addBtnWrapper);

            // 클릭 시 인라인 입력칸
            addBtn.onclick = () => {
                const eduBox = document.createElement('div');
                eduBox.className = 'award-item editable';
                eduBox.innerHTML = `
                    <input type="text" class="school-input" placeholder="학교명">
                    <input type="text" class="major-input" placeholder="학과">
                    <select class="status-input">
                        <option value="">상태 선택</option>
                        <option value="재학">재학</option>
                        <option value="졸업">졸업</option>
                        <option value="휴학">휴학</option>
                        
                        
                        <option value="중퇴">중퇴</option>
                    </select>
                    <div class="date-group2">
                      <!-- 시작 연도 -->
                      <div class="date-item">
                        <input class="year-select" placeholder="YYYY" maxlength="4"/>
                        <select class="month-select">
                          <option value="">월</option>
                          <option value="01">1</option>
                          <option value="02">2</option>
                          <option value="03">3</option>
                          <option value="04">4</option>
                          <option value="05">5</option>
                          <option value="06">6</option>
                          <option value="07">7</option>
                          <option value="08">8</option>
                          <option value="09">9</option>
                          <option value="10">10</option>
                          <option value="11">11</option>
                          <option value="12">12</option>
                        </select>
                        
                        <div class="tilde">~</div>
                      </div>
                    

                    
                      <!-- 종료 연도 -->
                      <div class="date-item2">
                        <input class="year-select" placeholder="YYYY" maxlength="4"/>
                        <select class="month-select">
                          <option value="">월</option>
                          <option value="01">1</option>
                          <option value="02">2</option>
                          <option value="03">3</option>
                          <option value="04">4</option>
                          <option value="05">5</option>
                          <option value="06">6</option>
                          <option value="07">7</option>
                          <option value="08">8</option>
                          <option value="09">9</option>
                          <option value="10">10</option>
                          <option value="11">11</option>
                          <option value="12">12</option>
                        </select>
                      </div>
                    </div>

                `;

                // Enter → 확정
                eduBox.addEventListener("keydown", (e) => {
                    if (e.key === "Enter") {
                        e.preventDefault();

                        const school = eduBox.querySelector(".school-input").value.trim();
                        const major  = eduBox.querySelector(".major-input").value.trim();
                        const status = eduBox.querySelector(".status-input").value.trim();
                        // 시작 연도/월
                        const sy = eduBox.querySelector(".year-input, .year-select")?.value || '';
                        const sm = eduBox.querySelector(".month-input, .month-select")?.value || '';

                        // 종료 연도/월
                        const ey = eduBox.querySelectorAll(".year-input, .year-select")?.[1]?.value || '';
                        const em = eduBox.querySelectorAll(".month-input, .month-select")?.[1]?.value || '';

                        const startYear = [sy, sm].filter(Boolean).join('-');
                        const endYear   = [ey, em].filter(Boolean).join('-');


                        if (!school) {
                            alert("학교명을 입력하세요!");
                            return;
                        }

                        let dateText = "";
                        if (startYear && endYear) {
                            dateText = `${startYear.replace(/-/g, '.')} ~ ${endYear.replace(/-/g, '.')}`;
                        } else if (startYear) {
                            dateText = `${startYear.replace(/-/g, '.')} ~`;
                        } else if (endYear) {
                            dateText = `~ ${endYear.replace(/-/g, '.')}`;
                        }

                        let line2 = "";
                        if (status === "재학") {
                            line2 = `재학 ${dateText} 학과 ${major}`;
                        } else if (status === "졸업") {
                            line2 = `졸업 ${dateText} 학과 ${major}`;
                        } else if (status) {
                            line2 = `${status} ${dateText} 학과 ${major}`;
                        } else {
                            line2 = `${dateText} 학과 ${major}`;
                        }


                        eduBox.className = "award-item";
                        eduBox.innerHTML = `${school}<br><small>${line2}</small>`;
                        makeDraggable(eduBox, {
                            type: 'EDUCATION',
                            schoolName: school,
                            majorName: major,
                            status: status,
                            startDate: startYear,
                            endDate: endYear
                        });
                    }
                });

                cont.insertBefore(eduBox, addBtnWrapper);
            };
        })
        .catch(err => console.error('학력 로드 실패:', err));
}

/***********************
 * 공모전 → 포폴에 같이 표시(선택)
 ***********************/
function renderContestsIntoPortfolio() {
    return fetch('/api/user/resumes/contests')
        .then(res => res.json())
        .then(list => {
            const cont = document.querySelector('.tab-content[data-content="portfolio"]');
            if (!cont || !list) return;
            list.forEach(c => {
                const d = document.createElement('div');
                d.className = 'award-item';
                d.innerHTML = `${c.title}<br><small>${c.date || ''}</small>`;
                makeDraggable(d, { id: c.id, type: 'CONTEST', file: c.filePath || '', title: c.title });
                cont.appendChild(d);
            });
        })
        .catch(err => console.error('공모전 로드 실패:', err));
}

/***********************
 * 왼쪽 섹션 드롭 설정 (학력 자동 채움 포함)
 ***********************/
function setupLeftDrops() {
    setupDropBox(document.querySelector('#section4 .upload-box'));
    setupDropBox(document.querySelector('#section5 .upload-box'));

    const sec2 = document.getElementById('section2');
    if (sec2) {
        sec2.addEventListener('dragover', e => e.preventDefault());
        sec2.addEventListener('drop', e => {
            e.preventDefault();
            const text = e.dataTransfer.getData('application/json');
            if (!text) return;
            try {
                const data = JSON.parse(text);
                if (data.type !== 'EDUCATION') return;

                const schoolInput = sec2.querySelector('input[placeholder="학교명"]');
                const majorInput  = sec2.querySelector('input[placeholder="학과"]');
                const statusInput = sec2.querySelector('.status-input');
                const dateGroup   = sec2.querySelector('.date-group');

                if (schoolInput) schoolInput.value = data.schoolName || '';
                if (majorInput)  majorInput.value  = data.majorName  || '';
                if (statusInput) statusInput.value = data.status     || '';

                const [sy, sm = ''] = (data.startDate || '').split('-');
                const [ey, em = ''] = (data.endDate   || '').split('-');

                const yInputs = dateGroup ? dateGroup.querySelectorAll('.year-input') : null;
                const mInputs = dateGroup ? dateGroup.querySelectorAll('.month-input') : null;

                if (yInputs && yInputs.length >= 2) {
                    if (yInputs[0]) yInputs[0].value = sy || '';
                    if (yInputs[1]) yInputs[1].value = ey || '';
                    if (mInputs && mInputs.length >= 2) {
                        if (mInputs[0]) mInputs[0].value = sm || '';
                        if (mInputs[1]) mInputs[1].value = em || '';
                    }
                } else {
                    const yearSelects = dateGroup ? dateGroup.querySelectorAll('select') : [];
                    if (yearSelects[0]) {
                        populateYearOptions(yearSelects[0], sy);
                        yearSelects[0].value = sy || yearSelects[0].value;
                    }
                    if (yearSelects[1]) {
                        populateYearOptions(yearSelects[1], ey);
                        yearSelects[1].value = ey || yearSelects[1].value;
                    }
                }
            } catch {}
        });
    }
}

/***********************
 * 초기 education-item 드롭 바인딩
 ***********************/
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll('.education-item').forEach(bindDropToEducationItem);
});

/***********************
 * 탭 바인딩
 ***********************/
function bindTabs() {
    document.querySelectorAll('#tab-list .tab').forEach(tab => {
        tab.addEventListener('click', () => {
            activateTab(tab.dataset.tab);
        });
    });
}

/***********************
 * 페이지 초기화
 ***********************/
window.addEventListener('DOMContentLoaded', () => {
    // 브라우저 기본 드래그 방지
    window.addEventListener('dragover', e => e.preventDefault());
    window.addEventListener('drop', e => e.preventDefault());

    bindTabs();
    setupLeftDrops();

    // 프로필 불러오기
    fetch("/api/user/resumes/me")
        .then(res => res.json())
        .then(user => {
            const img = document.getElementById("profileImage");
            if (img) img.src = user.profileImageUrl || "/images/user.png";
            const setText = (id, v) => { const el = document.getElementById(id); if (el) el.textContent = v || ""; };
            setText("userName", user.userName || "이름 없음");
            setText("mainLanguage", user.mainLanguage);
            setText("sex", user.sex);
            setText("birthday", user.birthday);
            setText("phone", user.userPhone);
            setText("email", user.userEmail);
            setText("region", user.region);
        })
        .catch(err => console.error("프로필 불러오기 실패:", err));

    // 데이터 로드
    Promise.all([
        renderProjects(),
        renderJobs(),
        renderEducations(),
        // renderContestsIntoPortfolio()
    ]).then(() => {
        activateTab('school');
    });
});

/***********************
 * 박스 선택 하이라이트
 ***********************/
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll('.resume-section, .section-box').forEach(box => {
        box.addEventListener('click', () => {
            document.querySelectorAll('.resume-section, .section-box').forEach(b => b.classList.remove('selected'));
            box.classList.add('selected');
        });
    });
});

/***********************
 * 학력 섹션: + 추가 및 삭제 바인딩
 ***********************/
document.addEventListener("DOMContentLoaded", () => {
    const addEduBtn = document.getElementById("add-edu");
    const section2 = document.getElementById("section2");

    if (addEduBtn && section2) {
        addEduBtn.addEventListener("click", () => {
            const firstItem = section2.querySelector(".education-item");
            if (firstItem) {
                const clone = firstItem.cloneNode(true);
                clone.querySelectorAll("input, select").forEach(el => el.value = "");
                const delBtn = clone.querySelector(".edu-del");
                if (delBtn) {
                    delBtn.addEventListener("click", () => clone.remove());
                }
                section2.insertBefore(clone, addEduBtn);
                bindDropToEducationItem(clone);
            }
        });
    }

    document.querySelectorAll(".edu-del").forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.target.closest(".education-item").remove();
        });
    });
});

/***********************
 * 템플릿 이동/모달
 ***********************/
document.addEventListener("DOMContentLoaded", () => {
    const arrow = document.querySelector(".arrow-toggle");
    if (arrow) {
        arrow.addEventListener("click", () => {
            window.location.href = "/profile";
        });
    }
});

document.addEventListener("DOMContentLoaded", () => {
    const firstTemplate = document.querySelector(".template-card");
    const modal = document.getElementById("templateModal");
    if (!firstTemplate || !modal) return;
    const closeBtn = modal.querySelector(".close-btn");

    firstTemplate.addEventListener("click", () => {
        modal.style.display = "flex";
    });

    closeBtn?.addEventListener("click", () => {
        modal.style.display = "none";
    });

    window.addEventListener("click", (e) => {
        if (e.target === modal) {
            modal.style.display = "none";
        }
    });
});

document.addEventListener("DOMContentLoaded", () => {
    const applyBtn = document.getElementById("applyTemplateBtn");
    const modal = document.getElementById("templateModal");
    if (!applyBtn || !modal) return;

    applyBtn.addEventListener("click", () => {
        const questions = [
            "내가 가진 큰 강점",
            "내가 지원한 직무와 내가 잘 맞는 이유",
            "어려운 상황을 극복한 경험",
            "입사 후 이루고 싶은 목표"
        ];

        const allSections = document.querySelectorAll(".resume-section");
        let lastNumber = 0;
        if (allSections.length > 0) {
            const lastSection = allSections[allSections.length - 1];
            const match = lastSection.id.match(/section(\d+)/);
            if (match) lastNumber = parseInt(match[1], 10);
        }

        let sectionIndex = lastNumber;

        questions.forEach(q => {
            sectionIndex++;
            const newSection = document.createElement("section");
            newSection.className = "resume-section";
            newSection.id = `section${sectionIndex}`;
            newSection.setAttribute("data-multi-select", "false");

            newSection.innerHTML = `
                <div class="section-header">
                    <span class="section-title-text">${sectionIndex}. ${q}</span>
                    <input type="text" class="section-title-input" value="${q}" style="display: none;">
                    <button class="delete-btn">✕</button>
                </div>
                <textarea placeholder="${q}에 대해 작성해주세요."></textarea>
            `;

            const addAnchor = document.querySelector(".add-section");
            addAnchor?.before(newSection);

            const outlineList = document.querySelector(".outline-list");
            if (outlineList) {
                const tocItem = document.createElement("li");
                tocItem.innerHTML = `<a href="#section${sectionIndex}">${sectionIndex}. ${q}</a>`;
                outlineList.appendChild(tocItem);
            }
        });

        modal.style.display = "none";
        reorderSectionsAndToc();
    });
});

/***********************
 * 파일/사진 업로드 미리보기
 ***********************/
document.addEventListener("change", (e) => {
    if (e.target.classList.contains("file-input")) {
        const preview = e.target.nextElementSibling;
        if (!preview) return;
        preview.innerHTML = "";

        [...e.target.files].forEach(file => {
            const item = document.createElement("div");
            item.className = "uploaded-item";
            item.textContent = file.name;

            if (file.type.startsWith("image/")) {
                const img = document.createElement("img");
                img.src = URL.createObjectURL(file);
                img.style.maxWidth = "100px";
                img.style.display = "block";
                img.style.marginTop = "5px";
                img.style.marginLeft = "340px";
                item.appendChild(img);
            }

            const del = document.createElement("span");
            del.className = "delete-icon";
            del.textContent = "삭제";
            del.onclick = () => item.remove();
            item.appendChild(del);

            preview.appendChild(item);
        });
    }
});

/***********************
 * 내 경력 내역 보기 링크
 ***********************/
document.addEventListener("DOMContentLoaded", () => {
    const viewLink = document.querySelector(".view-link");
    if (viewLink) {
        viewLink.addEventListener("click", () => {
            window.location.href = "/resumehistory";
        });
    }
});

/***********************
 * 경력 섹션: + 추가 및 삭제, 입력잠금
 ***********************/
document.addEventListener("DOMContentLoaded", () => {
    const addCareerBtn = document.getElementById("add-career");

    if (addCareerBtn) {
        addCareerBtn.addEventListener("click", () => {
            const firstCareer = document.querySelector("#section4 .career-item");
            if (firstCareer) {
                const newCareer = firstCareer.cloneNode(true);
                newCareer.querySelectorAll("input, textarea").forEach(el => { el.value = ""; });

                newCareer.querySelectorAll("input:not(.desc-input)").forEach(el => {
                    el.disabled = true;
                    el.style.pointerEvents = "none";
                    el.style.backgroundColor = "#f5f5f5";
                    el.style.cursor = "default";
                });

                const delBtn = newCareer.querySelector(".career-del");
                if (delBtn) {
                    delBtn.addEventListener("click", () => {
                        newCareer.remove();
                    });
                }

                addCareerBtn.parentNode.insertBefore(newCareer, addCareerBtn);
            }
        });
    }

    document.querySelectorAll("#section4 .career-item .career-del").forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.target.closest(".career-item").remove();
        });
    });

    lockCareerInputs();
});

// 입력잠금 함수
function lockCareerInputs() {
    document.querySelectorAll("#section4 .career-item input:not(.desc-input)")
        .forEach(input => {
            input.disabled = true;
            input.style.pointerEvents = "none";
            input.style.backgroundColor = "#f5f5f5";
            input.style.cursor = "default";
        });
}

// 경력 추가 버튼 누른 후에도 잠금
const addCareerBtnOnce = document.getElementById("add-career");
if (addCareerBtnOnce) {
    addCareerBtnOnce.addEventListener("click", () => {
        setTimeout(lockCareerInputs, 50);
    });
}

/***********************
 * 포트폴리오 섹션: + 추가/삭제, 입력잠금
 ***********************/
document.addEventListener("DOMContentLoaded", () => {
    const addPortfolioBtn = document.getElementById("add-portfolio");

    lockPortfolioInputs();

    if (addPortfolioBtn) {
        addPortfolioBtn.addEventListener("click", () => {
            const firstPortfolio = document.querySelector("#section5 .portfolio-item");
            if (firstPortfolio) {
                const newPortfolio = firstPortfolio.cloneNode(true);
                newPortfolio.querySelectorAll("input").forEach(el => el.value = "");

                const delBtn = newPortfolio.querySelector(".portfolio-del");
                if (delBtn) {
                    delBtn.addEventListener("click", () => {
                        newPortfolio.remove();
                    });
                }

                addPortfolioBtn.parentNode.insertBefore(newPortfolio, addPortfolioBtn);
                lockPortfolioInputs();
            }
        });
    }

    document.querySelectorAll("#section5 .portfolio-item .portfolio-del").forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.target.closest(".portfolio-item").remove();
        });
    });
});

function lockPortfolioInputs() {
    document.querySelectorAll("#section5 .portfolio-item input:not(.desc-input), #section5 .portfolio-item select, #section5 .portfolio-item .portfolio-file")
        .forEach(el => {
            el.disabled = true;
            el.style.pointerEvents = "none";
            el.style.backgroundColor = "#f5f5f5";
            el.style.cursor = "default";
        });
}

/***********************
 * 학력 입력 잠금
 ***********************/
function lockEducationInputs() {
    document.querySelectorAll("#section2 .education-item input, #section2 .education-item select, #section2 .education-item textarea")
        .forEach(el => {
            el.disabled = true;
            el.style.pointerEvents = "none";
            el.style.backgroundColor = "#f5f5f5";
            el.style.cursor = "default";
        });
}

// DOMContentLoaded 시 적용
document.addEventListener("DOMContentLoaded", () => {
    lockEducationInputs();

    const addEduBtn = document.getElementById("add-edu");
    if (addEduBtn) {
        addEduBtn.addEventListener("click", () => {
            setTimeout(lockEducationInputs, 50); // 새 학력 항목 추가 후에도 잠금 적용
        });
    }
});
