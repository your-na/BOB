        document.addEventListener("DOMContentLoaded", () => {
            let sections = document.querySelectorAll(".resume-section");
            const addBtn = document.getElementById("add-section");
            const popup = document.getElementById("section-popup");
            const jobTagContainer = document.querySelector(".job-tags");

            // ➕ 버튼 클릭 시 팝업 위치 설정
            addBtn.addEventListener("click", (e) => {
                e.stopPropagation();
                const rect = addBtn.getBoundingClientRect();
                popup.style.top = `${rect.bottom + window.scrollY + 5}px`;
                popup.style.left = `${rect.left + window.scrollX}px`;
                popup.style.display = popup.style.display === "block" ? "none" : "block";
            });

            document.addEventListener("click", (e) => {
                const target = e.target;

                // 팝업 바깥 클릭 시 닫기
                if (!popup.contains(target) && target !== addBtn) {
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

                    // ✅ 직접입력 input 처리
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
                        // ✅ 다른 태그 클릭 시 직접입력 input 제거
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
                    const tag = target.closest(".tag");
                    if (tag) tag.remove();
                }

                // 섹션 삭제
                if (target.classList.contains("delete-btn")) {
                    const section = target.closest(".resume-section");
                    const sectionId = section.id;
                    section.remove();
                    const tocLink = document.querySelector(`.outline-list a[href="#${sectionId}"]`);
                    tocLink?.closest("li")?.remove();
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



            // 저장 버튼
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
                    const textarea = section.querySelector("textarea");
                    let content = "";

                    if (sectionTitle.includes("학력")) {
                        const school = section.querySelector('input[placeholder="학교명"]')?.value || '';
                        const major = section.querySelector('input[placeholder="학과"]')?.value || '';
                        const status = section.querySelector('select')?.value || '';
                        const years = section.querySelectorAll('.date-group select');
                        const start = years[0]?.value || '';
                        const end = years[1]?.value || '';
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

                    let type = "서술형";
                    if (section.querySelector(".tag-list") || section.querySelector(".tag-input")) {
                        type = "선택형";
                    } else if (textarea?.placeholder?.includes("사진")) {
                        type = "사진 첨부";
                    } else if (textarea?.placeholder?.includes("파일")) {
                        type = "파일 첨부";
                    }

                    // ✅ 드래그 항목 수집
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
                        dragItems // ✅ 추가됨!
                    });
                });

                const jobTags = Array.from(jobTagContainer.querySelectorAll(".tag .tag-label"))

                const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

                const memberId = document.getElementById("memberId")?.value;

                const resumeData = {
                    title,
                    memberId: memberId ? parseInt(memberId) : null,
                    sections: sectionsData
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
                    .then(() => alert("저장 완료!"))
                    .catch(err => {
                        alert("저장 중 오류 발생");
                        console.error(err);
                    });
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
                    input.value = newTitle; // ✅ span이 바뀌었으면 input도 동기화!
                    input.style.display = "none";
                    span.style.display = "inline-block";

                    const section = input.closest(".resume-section");
                    const tocLink = document.querySelector(`.outline-list a[href="#${section.id}"]`);
                    if (tocLink) tocLink.textContent = `${number}. ${newTitle}`;
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

        document.addEventListener("DOMContentLoaded", function () {
            const chatBtn = document.getElementById("openChat");
            const chatBox = document.getElementById("chatBox");
            const closeBtn = document.getElementById("closeChat");

            chatBtn.addEventListener("click", () => {
                chatBox.style.display = chatBox.style.display === "flex" ? "none" : "flex";
            });

            closeBtn.addEventListener("click", () => {
                chatBox.style.display = "none";
            });
        });

        document.addEventListener("DOMContentLoaded", () => {
            const chatInput = document.getElementById("chatInput");
            const chatBody = document.getElementById("chatBody");
            const sendChat = document.getElementById("sendChat");

            // ✅ CSRF 토큰 정의
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

            if (sendChat && chatInput && chatBody) {
                sendChat.onclick = () => {
                    const message = chatInput.value.trim();
                    if (!message) return;

                    const userMsg = document.createElement("p");
                    userMsg.className = "user";
                    userMsg.textContent = message;
                    chatBody.appendChild(userMsg);
                    chatInput.value = "";

                    // Groq 호출
                    fetch("/api/chat", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            [csrfHeader]: csrfToken
                        },
                        body: JSON.stringify({message})
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
                };
            } else {
                console.error("chatInput 또는 sendChat 또는 chatBody가 null입니다.");
            }
        })


        //임의 채팅

        function getAiMockResponse(msg) {
            if (msg.includes("자기소개")) {
                return "저는 책임감 있게 성장하는 개발자입니다.";
            }
            if (msg.includes("경력")) {
                return "2023년부터 프론트엔드 개발자로 근무했습니다.";
            }
            return "죄송해요! 해당 질문은 아직 학습되지 않았어요.";
        }
        // ===================== 공통 드롭박스 세팅 =====================
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
                        // 표시 텍스트 구성
                        let display = data.title || '';
                        if (data.type === 'JOB') {
                            const fmt = s => s ? s.replace(/-/g, '.') : '';
                            if (data.status === '재직') display += ` (재직: ${fmt(data.startDate)} ~)`;
                            else display += ` (퇴직: ${fmt(data.startDate)} ~ ${fmt(data.endDate)})`;
                        }
                        const item = document.createElement('div');
                        item.className = 'uploaded-item';
                        item.textContent = display;

                        // ✅✅✅ 요 아래 코드 추가!! (dataset 채우기)
                        item.dataset.file = data.file || '';
                        item.dataset.startDate = data.startDate || '';
                        item.dataset.endDate = data.endDate || '';
                        item.dataset.displayText = display;

                        // 메타 데이터 보존
                        if (data.id) item.dataset.id = data.id;
                        if (data.type) item.dataset.type = data.type;
                        if (data.file) item.dataset.file = (data.file || '').replace(/^\/?download\//, '');
                        if (data.startDate) item.dataset.startDate = data.startDate;
                        if (data.endDate) item.dataset.endDate = data.endDate;

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

        // ===================== 탭 표시/빈 상태 토글 =====================
        function activateTab(tabName) {
            document.querySelectorAll('#tab-list .tab').forEach(t => t.classList.remove('active'));
            const activeTab = document.querySelector(`#tab-list .tab[data-tab="${tabName}"]`);
            if (activeTab) activeTab.classList.add('active');

            let isEmpty = true;
            document.querySelectorAll('.tab-content').forEach(c => {
                const show = c.dataset.content === tabName;
                c.style.display = show ? 'block' : 'none';
                if (show && c.children.length > 0) isEmpty = false;
            });

            const empty = document.querySelector('.empty-content[data-content="empty"]');
            if (empty) empty.style.display = isEmpty ? 'block' : 'none';
        }

        // ===================== 오른쪽 패널 데이터 로딩 =====================
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
                        d.innerHTML = `${it.jobTitle || '직무 없음'}<br><small>${period}</small>`;
                        makeDraggable(d, {
                            id: it.id, type: 'JOB',
                            title: it.jobTitle || '직무 없음',
                            startDate: it.startDate, endDate: it.endDate, status: it.status
                        });
                        cont.appendChild(d);
                    });
                })
                .catch(err => console.error('구직 이력 로드 실패:', err));
        }

        function renderEducations() {
            return fetch('/api/education-history/list')
                .then(res => res.json())
                .then(list => {
                    const cont = document.querySelector('.tab-content[data-content="school"]');
                    if (!cont) return;
                    cont.innerHTML = '';
                    if (!list || list.length === 0) return;

                    list.forEach(edu => {
                        const fmt = d => d?.replace(/-/g, '.');
                        let line2 = '';
                        if (edu.status === '재학')       line2 = `재학 ${fmt(edu.startDate)} 학과 ${edu.majorName || ''}`;
                        else if (edu.status === '졸업') line2 = `졸업 ${fmt(edu.startDate)} ~ ${fmt(edu.endDate)} 학과 ${edu.majorName || ''}`;
                        else                            line2 = `${edu.status || ''} 학과 ${edu.majorName || ''}`;

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
                })
                .catch(err => console.error('학력 로드 실패:', err));
        }

        // (선택) 공모전을 포트폴리오 탭에 함께 표시하고 싶다면 이 함수도 호출하세요.
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

        // ===================== 왼쪽 섹션 드롭 핸들러(학력 자동 채움 포함) =====================
        function setupLeftDrops() {
            // 경력/포트폴리오 섹션의 드롭존
            setupDropBox(document.querySelector('#section4 .upload-box'));
            setupDropBox(document.querySelector('#section5 .upload-box'));

            // 학력 섹션: EDUCATION 드롭 시 입력칸 자동 채움
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

                        // 입력칸 매핑
                        const inputs = sec2.querySelectorAll('input, select');
                        // 가정: [학교명 input, 학과 input, 상태 select, 시작년도 select, 종료년도 select] 순서
                        const schoolInput = sec2.querySelector('input[placeholder="학교명"]');
                        const majorInput  = sec2.querySelector('input[placeholder="학과"]');
                        const statusSel   = sec2.querySelector('select');
                        const yearSelects = sec2.querySelectorAll('.date-group select');

                        if (schoolInput) schoolInput.value = data.schoolName || '';
                        if (majorInput)  majorInput.value  = data.majorName  || '';
                        if (statusSel)   statusSel.value   = (data.status === '졸업' ? '졸업' : '재학');

                        // 날짜(년도만 존재하는 현재 마크업 기준)
                        const [sYear] = (data.startDate || '').split('-');
                        const [eYear] = (data.endDate || '').split('-');
                        if (yearSelects[0]) yearSelects[0].value = sYear || yearSelects[0].value;
                        if (yearSelects[1]) yearSelects[1].value = eYear || yearSelects[1].value;
                    } catch { /* 무시 */ }
                });
            }
        }

        // ===================== 탭 클릭 바인딩 =====================
        function bindTabs() {
            document.querySelectorAll('#tab-list .tab').forEach(tab => {
                tab.addEventListener('click', () => {
                    activateTab(tab.dataset.tab);
                });
            });
        }

        // ===================== 초기화 =====================
        window.addEventListener('DOMContentLoaded', () => {
            // 브라우저 기본 드래그 동작 방지(페이지 전체)
            window.addEventListener('dragover', e => e.preventDefault());
            window.addEventListener('drop', e => e.preventDefault());

            bindTabs();
            setupLeftDrops();

            // 데이터 로드
            Promise.all([
                renderProjects(),
                renderJobs(),
                renderEducations(),
                // 공모전을 포트폴리오에 함께 보여주려면 주석 해제
                // renderContestsIntoPortfolio()
            ]).then(() => {
                // 기본 탭: 학력
                activateTab('school');
            });
        });

