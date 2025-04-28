document.addEventListener("DOMContentLoaded", function () {
    console.log("📦 DOMContentLoaded - 시작");

    // ✅ 1. section-box 강조
    document.querySelectorAll('.section-box textarea, .section-box input, .section-box select').forEach(el => {
        el.addEventListener('focus', function () {
            document.querySelectorAll('.section-box').forEach(box => box.classList.remove('active'));
            this.closest('.section-box').classList.add('active');
        });
    });

    // ✅ 2. 연도/월 select 옵션 생성 함수
    function createYearOptions(select, start = 1980) {
        const current = new Date().getFullYear();
        for (let y = current; y >= start; y--) {
            const option = document.createElement("option");
            option.value = y;
            option.textContent = y;
            select.appendChild(option);
        }
    }

    function createMonthOptions(select) {
        for (let m = 1; m <= 12; m++) {
            const padded = m.toString().padStart(2, "0");
            const option = document.createElement("option");
            option.value = padded;
            option.textContent = padded;
            select.appendChild(option);
        }
    }

    function setupStatusListener(eduItem) {
        const status = eduItem.querySelector(".edu-status");
        const endYear = eduItem.querySelector(".end-year");
        const endMonth = eduItem.querySelector(".end-month");
        const tilde = eduItem.querySelector(".tilde");

        if (!status || !endYear || !endMonth) return;

        status.addEventListener("change", () => {
            if (status.value === "재학") {
                endYear.style.display = "none";
                endMonth.style.display = "none";
                if (tilde) tilde.style.display = "none";
            } else {
                endYear.style.display = "inline-block";
                endMonth.style.display = "inline-block";
                if (tilde) tilde.style.display = "inline-block";
            }
        });
    }

    function addDeleteFunction(button) {
        button.addEventListener('click', () => {
            const list = document.getElementById("education-list");
            const items = list.querySelectorAll(".education-item");
            if (items.length > 1) {
                button.closest(".education-item").remove();
            } else {
                alert("최소 1개 이상의 학력사항이 필요합니다.");
            }
        });
    }

    const addBtn = document.querySelector(".edu-btn");
    const list = document.getElementById("education-list");
    const firstItem = list ? list.querySelector(".education-item") : null;

    if (addBtn && firstItem) {
        addBtn.addEventListener("click", () => {
            const clone = firstItem.cloneNode(true);
            clone.querySelectorAll("input, select").forEach(el => el.value = "");

            const startYear = clone.querySelector(".start-year");
            const startMonth = clone.querySelector(".start-month");
            const endYear = clone.querySelector(".end-year");
            const endMonth = clone.querySelector(".end-month");

            startYear.innerHTML = "";
            startMonth.innerHTML = "";
            endYear.innerHTML = "";
            endMonth.innerHTML = "";

            createYearOptions(startYear);
            createMonthOptions(startMonth);
            createYearOptions(endYear);
            createMonthOptions(endMonth);

            const deleteBtn = clone.querySelector(".del-btn");
            addDeleteFunction(deleteBtn);
            setupStatusListener(clone);

            clone.style.marginTop = "10px";
            list.appendChild(clone);
        });

        // 초기 세팅
        createYearOptions(firstItem.querySelector(".start-year"));
        createMonthOptions(firstItem.querySelector(".start-month"));
        createYearOptions(firstItem.querySelector(".end-year"));
        createMonthOptions(firstItem.querySelector(".end-month"));
        setupStatusListener(firstItem);
        const deleteBtn = firstItem.querySelector(".del-btn");
        addDeleteFunction(deleteBtn);
    }

    // ✅ 글자 수 세기
    const selfIntro = document.getElementById("selfIntro");
    const charCount = document.getElementById("charCount");

    if (selfIntro && charCount) {
        selfIntro.addEventListener("input", () => {
            const len = selfIntro.value.length;
            charCount.textContent = `${len} / 500`;

            if (len < 500) {
                charCount.classList.add("warning");
            } else {
                charCount.classList.remove("warning");
            }
        });
    }

    // 모달
    const submitBtn = document.querySelector(".sub-btn");
    const modal = document.getElementById("submitModal");
    const confirmBtn = document.querySelector(".modal-confirm");
    const cancelBtn = document.querySelector(".modal-cancel");

    submitBtn?.addEventListener("click", () => modal.style.display = "flex");
    cancelBtn?.addEventListener("click", () => modal.style.display = "none");
    confirmBtn?.addEventListener("click", () => {
        modal.style.display = "none";
        alert("제출되었습니다!");
    });

    // 탭
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', function () {
            const tabName = this.dataset.tab;
            document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
            this.classList.add('active');
            document.querySelectorAll('.tab-content').forEach(content => {
                content.style.display = content.dataset.content === tabName ? 'block' : 'none';
            });
        });
    });

    document.querySelector('.arrow-toggle')?.addEventListener('click', () => {
        window.location.href = '/profile';
    });

    // ✅ Fetch 이력서
    function getResumeIdFromURL() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get("id");
    }

    const resumeId = getResumeIdFromURL();
    console.log("🆔 resumeId:", resumeId);

    if (resumeId) {
        fetch(`/api/coresumes/${resumeId}`)
            .then(res => res.json())
            .then(data => {
                console.log("📦 응답 받은 데이터:", data);
                if (data.sections) {
                    renderResumeSections(data.sections);
                } else {
                    console.warn("❌ data.sections 없음");
                }
            })
            .catch(err => console.error("❌ 이력서 양식 불러오기 실패", err));
    } else {
        console.error("❌ URL에 이력서 ID 없음");
    }

    function renderResumeSections(sections) {
        const container = document.querySelector(".left-content");
        console.log("📦 렌더링 시작 - sections 개수:", sections.length);
        if (!container) {
            console.error("❌ .left-content 요소를 찾을 수 없음");
            return;
        }

        sections.forEach((s, i) => {
            const section = document.createElement("section");
            section.className = "section-box";

            let html = `
                <div class="section-title">
                    <div class="number">${i + 2}.</div>
                    <div class="title-content">
                        <h3>${s.title}${s.multiSelect ? "(복수선택 가능)" : ""}</h3>
                        <p class="section-desc">${s.comment || ''}</p>
                    </div>
                </div>
            `;

            if (s.title.includes("학력사항")) {
                html += `
                    <div class="form-group education-item">
                        <input type="text" placeholder="학교명">
                        <input type="text" placeholder="학과명">
                        <select class="edu-status">
                            <option disabled selected>상태</option>
                            <option value="재학">재학</option>
                            <option value="졸업">졸업</option>
                        </select>
                        <select class="start-year"></select>
                        <select class="start-month"></select>
                        <span class="tilde">~</span>
                        <select class="end-year"></select>
                        <select class="end-month"></select>
                        <button type="button" class="del-btn">✖</button>
                    </div>
                `;
            } else if (s.type === "선택형" && s.conditions?.length > 0) {
                html += s.conditions.map(cond => `
                    <label>
                        <input type="${s.multiSelect ? "checkbox" : "radio"}" name="section-${i}" value="${cond}">
                        ${cond}
                    </label>
                `).join('');
            } else if (s.type === "서술형") {
                html += `<textarea placeholder="입력해주세요."></textarea>`;
                if (s.conditions?.length > 0) {
                    html += `
                        <div class="resume-preview-conditions">
                            <strong>조건:</strong> ${s.conditions.map(c => `<span class="condition">${c}</span>`).join(' ')}
                        </div>`;
                }
            }

            section.innerHTML = html;
            container.appendChild(section);

            if (s.title.includes("학력사항")) {
                const item = section.querySelector(".education-item");
                if (item) {
                    createYearOptions(item.querySelector(".start-year"));
                    createMonthOptions(item.querySelector(".start-month"));
                    createYearOptions(item.querySelector(".end-year"));
                    createMonthOptions(item.querySelector(".end-month"));
                    setupStatusListener(item);
                    addDeleteFunction(item.querySelector(".del-btn"));
                }
            }
        });
    }
});
