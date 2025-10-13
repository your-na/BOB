let sections = [];
/***********************
 * 공통 유틸
 ***********************/
// 안내 문구 삽입 함수
function appendEditHint(container) {
    if (!container) return;
    if (!container.querySelector(".edit-hint")) {
        const hint = document.createElement("p");
        hint.className = "edit-hint";
        hint.textContent = "※ 드래그 전, 항목을 더블클릭하면 수정할 수 있습니다.";
        hint.style.fontSize = "11px";
        hint.style.color = "#777";
        hint.style.margin = "6px 0";
        hint.style.textAlign = "left";
        container.prepend(hint); // 항상 맨 위에 표시
    }
}


function normalizeDate(date) {
    return date && date.length === 7 ? date + "-01" : date;
}
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

// ✅ 파일을 서버에 업로드하고 저장된 파일명을 반환하는 함수
async function uploadFileToServer(file) {
    const formData = new FormData();
    formData.append("file", file);

    // ✅ CSRF 토큰 설정 여기 넣기!
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const response = await fetch("/api/user/resumes/upload", {
        method: "POST",
        headers: {
            [csrfHeader]: csrfToken   // ✅ 여기 주의!
        },
        body: formData,
        credentials: "include"
    });

    if (!response.ok) throw new Error("파일 업로드 실패");

    const fileName = await response.text(); // 서버에서 저장된 파일명 반환
    return fileName;
}


// ✅ 드롭 가능한 upload-box에 drag 이벤트 연결하는 함수
function setupDropBox(box) {
    box.addEventListener('dragover', e => {
        e.preventDefault();
        box.style.border = '2px dashed #4CAF50';
    });

    box.addEventListener('dragleave', () => {
        box.style.border = '1px dashed #ccc';
    });

    box.addEventListener('drop', e => {
        e.preventDefault();
        box.style.border = '1px solid #ccc';

        if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
            [...e.dataTransfer.files].forEach(file => {
                const item = document.createElement('div');
                item.className = 'uploaded-item';
                item.textContent = file.name;

                // ✅ 삭제 버튼은 preview 모드가 아닐 때만 보이게
                if (!box.classList.contains('preview-mode')) {
                    const deleteBtn = document.createElement('span');
                    deleteBtn.className = 'delete-icon';
                    deleteBtn.innerHTML = '삭제️';
                    deleteBtn.addEventListener('click', () => item.remove());
                    item.appendChild(deleteBtn);
                }


                box.appendChild(item);
            });
        } else if (e.dataTransfer.types.includes("application/json")) {
            const json = JSON.parse(e.dataTransfer.getData("application/json"));

            // ✅ 학력사항이면 왼쪽 입력 필드에 자동 입력
            if (json.type === "EDUCATION") {
                const eduList = document.getElementById("education-list");
                const firstItem = eduList?.querySelector(".education-item");
                if (!firstItem) return;

                const [startY, startM] = (json.startDate || "").split("-");
                const [endY, endM] = (json.endDate || "").split("-");

                firstItem.querySelector("input[placeholder='학교명']").value = json.schoolName || "";
                firstItem.querySelector("input[placeholder='학과명']").value = json.majorName || "";
                firstItem.querySelector(".edu-status").value = json.status || "";
                firstItem.querySelector(".start-year").value = startY || "";
                firstItem.querySelector(".start-month").value = startM || "";
                firstItem.querySelector(".end-year").value = endY || "";
                firstItem.querySelector(".end-month").value = endM || "";

                setupStatusListener(firstItem);  // 재학이면 종료일 숨기기 적용
                return; // ✅ uploaded-item 추가 금지
            }

            const item = document.createElement('div');
            item.className = 'uploaded-item';

            // ✅ 기본 텍스트는 title
            let displayText = json.title;

            // ✅ 구직 이력일 경우에만 상태 및 날짜 추가
            if (json.type === "JOB") {
                const format = (d) => d ? d.replace(/-/g, ".") : "";
                if (json.status === "재직") {
                    displayText += ` (${json.status}: ${format(json.startDate)} ~)`;
                } else if (json.status === "퇴직") {
                    displayText += ` (${json.status}: ${format(json.startDate)} ~ ${format(json.endDate)})`;
                }
            }

            item.textContent = displayText;

            // ✅ 🔥 바로 여기에 추가!
            item.dataset.type = json.type || "PROJECT";


            // ✅ 드래그 항목 속성 주입
            item.dataset.id = json.id;
            item.dataset.type = json.type;
            item.dataset.file = json.file;
            item.dataset.startDate = json.startDate || "";
            item.dataset.endDate = json.endDate || "";
            item.dataset.status = json.status || "";

            // ✅ 삭제 버튼 추가
            const deleteBtn = document.createElement('span');
            deleteBtn.className = 'delete-icon';
            deleteBtn.innerHTML = '삭제️';
            deleteBtn.addEventListener('click', () => item.remove());
            item.appendChild(deleteBtn);

            box.appendChild(item);
        }
        else {
            const title = e.dataTransfer.getData('text/plain');
            const item = document.createElement('div');
            item.className = 'uploaded-item';
            item.textContent = title;

            // ✅ 삭제 버튼 추가
            const deleteBtn = document.createElement('span');
            deleteBtn.className = 'delete-icon';
            deleteBtn.innerHTML = '삭제️';
            deleteBtn.addEventListener('click', () => item.remove());
            item.appendChild(deleteBtn);

            box.appendChild(item);
        }
    });
}



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

// ✅ 3. 재학 상태면 종료일 숨기기
function setupStatusListener(eduItem) {
    const status = eduItem.querySelector(".edu-status");
    const endYear = eduItem.querySelector(".end-year");
    const endMonth = eduItem.querySelector(".end-month");
    const tilde = eduItem.querySelector(".tilde");

    // ✅ 상태 변경될 때마다 실행
    const toggleEndDateVisibility = () => {
        if (status.value === "재학") {
            endYear.style.display = "none";
            endMonth.style.display = "none";
            if (tilde) tilde.style.display = "none";
        } else {
            endYear.style.display = "inline-block";
            endMonth.style.display = "inline-block";
            if (tilde) tilde.style.display = "inline-block";
        }
    };

    status.addEventListener("change", toggleEndDateVisibility);
    toggleEndDateVisibility();  // ✅ 초기 상태 반영
}


// ✅ 4. 삭제 기능
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

/***********************
 * 탭 바인딩
 ***********************/
/***********************
 * 탭 전환 및 빈 화면 안내
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
        const itemType = div.dataset.type || "PROJECT"; // 기본값은 "PROJECT"
        const file = div.dataset.file || "";

        e.dataTransfer.setData('application/json', JSON.stringify({
            ...payload,
            type: itemType,
            file: file
        }));
    });

}

/***********************
 * 프로젝트 렌더링
 ***********************/
function renderProjects() {
    return fetch('/api/user/resumes/projects')
        .then(res => res.json())
        .then(projects => {
            const cont = document.querySelector('.tab-content[data-content="project"]');
            if (!cont) return;
            cont.innerHTML = '';
            if (!projects || projects.length === 0) return;

            projects.forEach(p => {
                const d = document.createElement('div');
                d.className = 'award-item';
                d.innerHTML = `${p.title}<br><small>${p.submittedDate || ''}</small>`;
                // ✅ dataset 보강
                Object.assign(d.dataset, {
                    type: 'PROJECT',
                    id: p.id,
                    title: p.title || "",
                    desc: p.desc || "",
                    file: (p.filePath || '').replace(/^\/?download\//, ''),
                    startDate: p.startDate || "",
                    endDate: p.endDate || ""
                });
                makeDraggable(d, d.dataset);
                cont.appendChild(d);
            });

            // 안내 문구 추가
            appendEditHint(cont);
        })
        .catch(err => console.error('프로젝트 로드 실패:', err));
}

/***********************
 * 경력 카드 렌더링
 ***********************/
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
                // ✅ dataset 보강
                Object.assign(d.dataset, {
                    type: 'JOB',
                    id: it.id,
                    workplace: it.workplace || "",
                    jobTitle: it.jobTitle || "",
                    status: it.status || "",
                    startDate: it.startDate || "",
                    endDate: it.endDate || ""
                });
                makeDraggable(d, d.dataset);
                cont.appendChild(d);
            });

            // 안내 문구 추가
            appendEditHint(cont);
        })
        .catch(err => console.error('구직 이력 로드 실패:', err));
}

/***********************
 * 학력 카드 렌더링
 ***********************/
function renderEducations() {
    return fetch('/api/education-history/list')
        .then(res => res.json())
        .then(list => {
            const cont = document.querySelector('.tab-content[data-content="school"]');
            if (!cont) return;
            cont.innerHTML = '';

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
                    // ✅ dataset 보강
                    Object.assign(d.dataset, {
                        type: 'EDUCATION',
                        id: edu.id,
                        schoolName: edu.schoolName || "",
                        majorName: edu.majorName || "",
                        status: edu.status || "",
                        startDate: edu.startDate || "",
                        endDate: edu.endDate || ""
                    });
                    makeDraggable(d, d.dataset);
                    cont.appendChild(d);
                });

                // 안내 문구 추가
                appendEditHint(cont);
            }

            // ➕ 버튼 그대로 유지
            const addBtn = document.createElement('button');
            addBtn.className = 'add-school-btn';
            addBtn.textContent = '＋';
            const addBtnWrapper = document.createElement('div');
            addBtnWrapper.style.display = 'flex';
            addBtnWrapper.style.justifyContent = 'center';
            addBtnWrapper.appendChild(addBtn);
            cont.appendChild(addBtnWrapper);

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
                        <input type="month" class="start-date">
                        ~
                        <input type="month" class="end-date">
                    </div>
                `;

                eduBox.addEventListener("keydown", async (e) => {
                    if (e.key === "Enter") {
                        e.preventDefault();
                        const school = eduBox.querySelector(".school-input").value.trim();
                        const major = eduBox.querySelector(".major-input").value.trim();
                        const status = eduBox.querySelector(".status-input").value;
                        const start = eduBox.querySelector(".start-date").value;
                        const end = eduBox.querySelector(".end-date").value;

                        if (!school) {
                            alert("학교명을 입력하세요!");
                            return;
                        }

                        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
                        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

                        try {
                            // ✅ DB 저장 요청
                            const res = await fetch("/api/education-history/save", {
                                method: "POST",
                                headers: {
                                    "Content-Type": "application/json",
                                    [csrfHeader]: csrfToken
                                },
                                body: JSON.stringify({
                                    schoolName: school,
                                    majorName: major,
                                    status: status,
                                    startDate: start,
                                    endDate: end
                                })
                            });

                            if (!res.ok) throw new Error("저장 실패");
                            const savedId = await res.json();

                            // ✅ UI 업데이트
                            let line2 = "";
                            if (status === "재학") line2 = `재학 ${start} ~ 학과 ${major}`;
                            else if (status === "졸업") line2 = `졸업 ${start} ~ ${end} 학과 ${major}`;
                            else line2 = `${status} ${start} ~ ${end} 학과 ${major}`;

                            eduBox.className = "award-item";
                            eduBox.innerHTML = `${school}<br><small>${line2}</small>`;
                            Object.assign(eduBox.dataset, {
                                type: "EDUCATION",
                                id: savedId,
                                schoolName: school,
                                majorName: major,
                                status,
                                startDate: start,
                                endDate: end
                            });
                            makeDraggable(eduBox, eduBox.dataset);
                        } catch (err) {
                            alert("DB 저장 중 오류 발생");
                            console.error(err);
                        }
                    }
                });


                eduBox.addEventListener("keydown", (e) => {
                    if (e.key === "Enter") {
                        e.preventDefault();
                        const school = eduBox.querySelector(".school-input").value.trim();
                        const major = eduBox.querySelector(".major-input").value.trim();
                        const status = eduBox.querySelector(".status-input").value;
                        const start = eduBox.querySelector(".start-date").value;
                        const end = eduBox.querySelector(".end-date").value;

                        if (!school) {
                            alert("학교명을 입력하세요!");
                            return;
                        }

                        let line2 = "";
                        if (status === "재학") line2 = `재학 ${start} ~ 학과 ${major}`;
                        else if (status === "졸업") line2 = `졸업 ${start} ~ ${end} 학과 ${major}`;
                        else line2 = `${status} ${start} ~ ${end} 학과 ${major}`;

                        eduBox.className = "award-item";
                        eduBox.innerHTML = `${school}<br><small>${line2}</small>`;
                        Object.assign(eduBox.dataset, {
                            type: "EDUCATION",
                            schoolName: school,
                            majorName: major,
                            status: status,
                            startDate: start,
                            endDate: end
                        });
                        makeDraggable(eduBox, eduBox.dataset);
                    }
                });

                cont.insertBefore(eduBox, addBtnWrapper);
            };
        })
        .catch(err => console.error('학력 로드 실패:', err));
}

// ✅ 페이지 로드 완료 후 실행
document.addEventListener("DOMContentLoaded", () => {
    renderEducations();
    renderProjects();
    renderJobs();
    renderContestsIntoPortfolio();
});

function renderContestsIntoPortfolio() {
    return fetch('/api/contest-history/resume-view', {
        credentials: "include" // 로그인 세션 포함
    })
        .then(res => {
            if (!res.ok) throw new Error("공모전 데이터 로드 실패");
            return res.json();
        })
        .then(list => {
            const cont = document.querySelector('.tab-content[data-content="contest"]');
            if (!cont) return;
            cont.innerHTML = ""; // 기존 내용 초기화

            if (!list || list.length === 0) {
                const empty = document.createElement('p');
                empty.textContent = "참여한 공모전이 없습니다.";
                cont.appendChild(empty);
                return;
            }

            list.forEach(c => {
                const start = c.startDate ? c.startDate.replace(/-/g, ".") : "";
                const end = c.endDate ? c.endDate.replace(/-/g, ".") : "";
                const period = start && end ? `${start} ~ ${end}` : start || "";

                // ✅ HTML 구성
                const d = document.createElement("div");
                d.className = "award-item";
                d.innerHTML = `
                    <strong>${c.title || "공모전명 없음"}</strong><br>
                    <small>${c.organizer || "주최기관 미상"}</small><br>
                    <span style="color:#0077cc; font-weight:bold;">🏆 ${c.grade || "참여"}</span><br>
                    <small>${period}</small>
                `;

                // ✅ dataset 설정 (드래그용)
                Object.assign(d.dataset, {
                    type: "CONTEST",
                    id: c.id,
                    title: c.title || "",
                    grade: c.grade || "",
                    startDate: c.startDate || "",
                    endDate: c.endDate || "",
                    organizer: c.organizer || "",
                    status: c.status || ""
                });

                makeDraggable(d, d.dataset);
                cont.appendChild(d);
            });
        })
        .catch(err => console.error("공모전 로드 실패:", err));
}


// ✅ 개월 수 계산 함수 (맨 위에 추가)
function calcMonths(startDate, endDate) {
    if (!startDate || !endDate) return "";
    const [sy, sm] = startDate.split("-").map(Number);
    const [ey, em] = endDate.split("-").map(Number);
    return (ey - sy) * 12 + (em - sm) + 1; // 종료월 포함
}
/***********************
 * 왼쪽 섹션 드롭 설정 (학력 자동 채움 포함)
 ***********************/
function setupLeftDrops() {
    setupDropBox(document.querySelector('#section4 .upload-box'));
    setupDropBox(document.querySelector('#section5 .upload-box'));


}

/***********************
 * 초기 education-item 드롭 바인딩
 ***********************/
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll('.education-item').forEach(item => {
        if (typeof bindDropToEducationItem === "function") {
            bindDropToEducationItem(item);
        }
    });
});


/***********************
 * 탭 바인딩
 ***********************/
function bindTabs() {
    document.querySelectorAll('#tab-list .tab').forEach(tab => {
        // 도움말 툴팁 추가
        tab.setAttribute("title", "더블클릭 시 경력 내역 페이지로 이동합니다.");

        // 기본: 클릭하면 탭 활성화
        tab.addEventListener('click', () => {
            activateTab(tab.dataset.tab);
        });

        // 추가: 더블클릭하면 /resumehistory 이동
        tab.addEventListener('dblclick', () => {
            window.location.href = "/resumehistory";
        });
    });
}

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

// ✅ 페이지 로드 시 자동 탭 활성화
document.addEventListener("DOMContentLoaded", async() => {
    bindTabs();

    // ✅ 먼저 데이터 렌더링
    await renderEducations();
    await renderProjects();
    await renderJobs();
    await renderContestsIntoPortfolio();

    // 기본 탭 자동 활성화 (예: school)
    const defaultTab = document.querySelector(".tab.active") || document.querySelector(".tab[data-tab='school']");
    if (defaultTab) {
        activateTab(defaultTab.dataset.tab);
    }
});



// ✅ 학력사항 드롭 처리
document.addEventListener("DOMContentLoaded", () => {
    const educationList = document.getElementById("education-list");

    if (!educationList) return;

    educationList.addEventListener("dragover", e => {
        e.preventDefault();  // drop 허용
    });

    educationList.addEventListener("drop", e => {
        e.preventDefault();

        dropZone.addEventListener("drop", function (e) {
            e.preventDefault(); // ✅ 반드시 있어야 함
            console.log("📦 drop 이벤트 실행됨"); // 이거 먼저 확인!
        });


        const data = e.dataTransfer.getData("application/json");
        if (!data) return;

        let json;
        try {
            json = JSON.parse(data);
        } catch {
            return;
        }

        // 🎯 학력 항목이 아닐 경우 무시
        if (json.type !== "EDUCATION") return;

        // ✅ 첫 번째 항목 선택
        const firstItem = educationList.querySelector(".education-item");
        if (!firstItem) return;

        // ✅ select 옵션이 비어 있으면 생성
        const startYear = firstItem.querySelector(".start-year");
        const startMonth = firstItem.querySelector(".start-month");
        const endYear = firstItem.querySelector(".end-year");
        const endMonth = firstItem.querySelector(".end-month");

        if (startYear.options.length === 0) createYearOptions(startYear);
        if (startMonth.options.length === 0) createMonthOptions(startMonth);
        if (endYear.options.length === 0) createYearOptions(endYear);
        if (endMonth.options.length === 0) createMonthOptions(endMonth);

        // ✅ 값 주입
        firstItem.querySelector("input[placeholder='학교명']").value = json.schoolName || "";
        firstItem.querySelector("input[placeholder='학과명']").value = json.majorName || "";
        firstItem.querySelector(".edu-status").value = json.status || "";

        const [startY, startM] = (json.startDate || "").split("-");
        const [endY, endM] = (json.endDate || "").split("-");

        firstItem.querySelector(".start-year").value = startY || "";
        firstItem.querySelector(".start-month").value = startM || "";
        firstItem.querySelector(".end-year").value = endY || "";
        firstItem.querySelector(".end-month").value = endM || "";

        // ✅ 상태 선택에 따라 종료일 숨김 처리 다시 연결
        setupStatusListener(firstItem);
    });
});



// ✅ 글자 수 세기 기능
const selfIntro = document.getElementById("selfIntro");
const charCount = document.getElementById("charCount");

selfIntro.addEventListener("input", () => {
    const len = selfIntro.value.length;
    charCount.textContent = `${len} / 500`;

    if (len < 500) {
        charCount.classList.add("warning");
    } else {
        charCount.classList.remove("warning");
    }
});

// ✅ 제출 버튼 누르면 모달 띄우기
const submitBtn = document.querySelector(".sub-btn");
const modal = document.getElementById("submitModal");
const confirmBtn = document.querySelector(".modal-confirm");
const cancelBtn = document.querySelector(".modal-cancel");

submitBtn.addEventListener("click", () => {
    modal.style.display = "flex";
});

// "아니오" 클릭 → 모달 닫기
cancelBtn.addEventListener("click", () => {
    modal.style.display = "none";
});

// ✅ "네" 클릭 → 제출 처리
confirmBtn.addEventListener("click", () => {
    modal.style.display = "none";

    // ✅ 쿼리스트링에서 coResumeId, jobPostId 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    const coResumeId = urlParams.get("id");
    const jobPostId = urlParams.get("jobPostId");

    // 🚨 유효성 검사 추가
    if (!coResumeId || isNaN(Number(coResumeId))) {
        alert("이력서 양식 ID(coResumeId)가 존재하지 않거나 잘못되었습니다.");
        return;
    }

    if (!jobPostId || isNaN(Number(jobPostId))) {
        alert("공고 ID(jobPostId)가 존재하지 않거나 잘못되었습니다.");
        return;
    }



    // ✅ 사용자 입력값 수집 + 파일 업로드 처리 포함
    const sectionBoxes = document.querySelectorAll(".section-box[data-co-section-id]");
    const sections = [];
    const uploadPromises = [];

    sectionBoxes.forEach(box => {
        const coSectionId = box.dataset.coSectionId;
        console.log("section id:", coSectionId); // 👈 이거 추가

        const selectedTags = [...box.querySelectorAll("input[type=checkbox]:checked, input[type=radio]:checked")]
            .map(input => input.parentElement.textContent.trim());

        // ✅ 드래그드롭된 항목이 있다면 selectedTags에 덮어쓰기
        const uploadBox = box.querySelector(".upload-box");
        if (uploadBox) {
            const draggedItems = [...uploadBox.querySelectorAll(".uploaded-item")].map(el => el.textContent.trim());
            if (draggedItems.length > 0) selectedTags.splice(0, selectedTags.length, ...draggedItems);
        }


        const textarea = box.querySelector("textarea");
        let content = textarea ? textarea.value.trim() : "";

        const fileInput = box.querySelector("input[type=file]");
        let uploadedFileName = null;

        let uploadPromise = Promise.resolve();

        if (fileInput && fileInput.files.length > 0) {
            const file = fileInput.files[0];
            uploadPromise = uploadFileToServer(file).then(fileName => {
                uploadedFileName = fileName;
            });
        }

        const section = {
            coSectionId: Number(coSectionId),
            content,
            selectedTags,
            uploadedFileName: null // 나중에 주입
        };

        // ✅ 이 부분에 추가
        const textareas = box.querySelectorAll("textarea");
        if (textareas.length > 0) {
            content = [...textareas]
                .map(t => t.value.trim())
                .filter(v => v.length > 0)
                .join("###"); // 여러 줄 구분자
        }

        // ✅ 드래그된 항목 수집
        const draggedDivs = box.querySelectorAll(".uploaded-item");
        if (draggedDivs.length > 0) {
            section.dragItems = [...draggedDivs].map(div => {
                const rawId = div.dataset.id;
                const referenceId = rawId && !isNaN(Number(rawId)) ? Number(rawId) : null;

                return {
                    coSectionId: Number(coSectionId),
                    itemType: div.dataset.type || "PROJECT",
                    referenceId: referenceId,
                    displayText: div.cloneNode(true).childNodes[0]?.textContent.trim(),
                    filePath: div.dataset.file || null
                };
            });
        }



        // ✅ [FIX] 학력사항 (새 구조: .edu-box / .edu-item) 수집
        const eduBox = box.querySelector(".edu-box");
        if (eduBox) {
            const educations = [];
            const items = eduBox.querySelectorAll(".edu-item");
            items.forEach(item => {
                const yearInputs = item.querySelectorAll("input[placeholder='YYYY']");
                const monthInputs = item.querySelectorAll("input[placeholder='MM']");

                educations.push({
                    schoolName: item.querySelector(".school-input")?.value || "",
                    majorName: item.querySelector(".major-input")?.value || "",
                    status: item.querySelector(".status-input")?.value || "",
                    startYear: yearInputs[0]?.value || "",
                    startMonth: monthInputs[0]?.value || "",
                    endYear: yearInputs[1]?.value || "",
                    endMonth: monthInputs[1]?.value || ""
                });
            });

            section.educations = educations;
        }


        // ✅ 경력사항 수집 (JobHistoryDTO와 매핑)
        const careerBox = box.querySelector(".career-box");
        if (careerBox) {
            const careers = [];
            const items = careerBox.querySelectorAll(".career-item");

            items.forEach(item => {
                const yearInputs = item.querySelectorAll("input[placeholder='YYYY']");
                const monthInputs = item.querySelectorAll("input[placeholder='MM']");

                const startYear = yearInputs[0]?.value || "";
                const startMonth = monthInputs[0]?.value || "";
                const endYear   = yearInputs[1]?.value || "";
                const endMonth  = monthInputs[1]?.value || "";

                const startDate = (startYear && startMonth)
                    ? `${startYear}-${startMonth.padStart(2, "0")}-01`
                    : null;
                const endDate = (endYear && endMonth)
                    ? `${endYear}-${endMonth.padStart(2, "0")}-01`
                    : null;

                careers.push({
                    workplace: item.querySelector(".company-input")?.value || "",
                    jobTitle:  item.querySelector(".job-input")?.value || "",
                    status:    item.querySelector(".status-input")?.value || "",
                    startDate,
                    endDate
                });
            });

            if (careers.length > 0) {
                section.careers = careers; // ← 서버로 보낼 섹션에 경력 붙이기
            }
        }

        uploadPromises.push(
            uploadPromise.then(() => {
                section.uploadedFileName = uploadedFileName;
            })
        );
        // ✅ 포트폴리오 섹션 수집 (PortfolioItemDTO 매핑)
        const portfolioBox = box.querySelector(".portfolio-box");
        if (portfolioBox) {
            const portfolios = [];
            const items = portfolioBox.querySelectorAll(".portfolio-item");

            items.forEach(item => {
                const yearInputs = item.querySelectorAll("input[placeholder='YYYY']");
                const monthInputs = item.querySelectorAll("input[placeholder='MM']");

                const startYear = yearInputs[0]?.value || "";
                const startMonth = monthInputs[0]?.value || "";
                const endYear = yearInputs[1]?.value || "";
                const endMonth = monthInputs[1]?.value || "";

                const startDate = (startYear && startMonth)
                    ? `${startYear}-${startMonth.padStart(2, "0")}-01`
                    : null;
                const endDate = (endYear && endMonth)
                    ? `${endYear}-${endMonth.padStart(2, "0")}-01`
                    : null;

                portfolios.push({
                    type: item.dataset.type || "PROJECT", // PROJECT 또는 CONTEST
                    title: item.querySelector(".portfolio-title")?.value || "",
                    description: item.querySelector(".desc-input")?.value || "",
                    status: item.querySelector(".status-input")?.value || "",
                    filePath: item.querySelector(".portfolio-file-path")?.value ||
                        item.querySelector("input[name='filePath']")?.value || null,
                    startDate,
                    endDate
                });
            });

            if (portfolios.length > 0) {
                section.portfolios = portfolios; // ✅ PortfolioItemDTO 리스트로 백엔드에 전송
            }
        }
         // ✅ 드래그 아이템 로그 확인 (이 부분 추가!)
        console.log("📦 현재 섹션 ID:", section.coSectionId);
        console.log("🎯 section.dragItems:", section.dragItems);

        sections.push(section);
    });



// ✅ 모든 업로드 끝나고 서버에 제출
    Promise.all(uploadPromises)
        .then(() => {
            console.log("📤 최종 제출할 sections:", sections);

            const requestData = {
                coResumeId: Number(coResumeId),
                jobPostId: jobPostId ? Number(jobPostId) : null,
                sections
            };

            // ✅ CSRF 토큰 설정 여기!
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

            return fetch("/api/user/resumes/submit", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken  // ✅ 추가!
                },
                body: JSON.stringify(requestData)
            });
        })
        .then(res => {
            if (res.status === 400) {
                return res.text().then(msg => {
                    alert(msg); // 👉 서버에서 보낸 안내 메시지 보여주기
                    throw new Error(msg); // 👉 이후 then 체인 중단
                });
            }
            if (!res.ok) throw new Error("제출 실패");
            return res.text();
        })
        .then(() => {
            alert("제출이 완료되었습니다!");
            window.location.href = `/jobindex?id=${jobPostId}`;  // ✅ 공고 상세보기 페이지로 이동
        })
        .catch(err => {
            console.error("제출 오류:", err);
            if (!err.message.includes("이미 이 공고에")) {
                alert("제출에 실패했습니다.");
            }
        });


});


// ✅ 내 정보 화살표 클릭 시 프로필 페이지 이동
const arrowToggle = document.querySelector('.arrow-toggle');
arrowToggle.addEventListener('click', () => {
    window.location.href = '/profile';
});

// ✅ "내 경력내역 보기" 클릭 시 이동 모달 띄우기
const viewLink = document.querySelector('.view-link');
const redirectModal = document.getElementById("redirectModal");
const redirectCancel = redirectModal.querySelector('.modal-cancel');

viewLink?.addEventListener('click', () => {
    redirectModal.style.display = "flex";
});

redirectCancel.addEventListener('click', () => {
    redirectModal.style.display = "none";
});

// ✅ 학력사항 섹션을 동적으로 렌더링하는 함수 (조건 기반 + 추가 버튼)
function renderEducationSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";
    sectionBox.dataset.sectionId = section.id;
    sectionBox.dataset.coSectionId = section.id;
    sectionBox.dataset.title = section.title;

    const sectionTitle = document.createElement("div");
    sectionTitle.className = "section-title";
    sectionTitle.innerHTML = `
    <div class="number">${number}.</div>
    <div class="title-content">
      <h3>${section.title}</h3>
      <p class="section-desc">${section.comment || "기업에서 쓴 설명입력 칸 입니다."}</p>
    </div>
  `;

    // ✅ 조건 불러오기
    const allowed = section.conditions || [];

    // ✅ 드롭 대상 박스 (.edu-box)
    const eduBox = document.createElement("div");
    eduBox.className = "edu-box";

    // ✅ 학력 아이템 생성 함수
    const createEduItem = () => {
        const eduItem = document.createElement("div");
        eduItem.className = "edu-item";
        eduItem.innerHTML = `<button type="button" class="edu-del">✕</button>`;

        // 첫 번째 줄 (학교명 / 학과)
        const row1 = document.createElement("div");
        row1.className = "edu-row";

        if (allowed.includes("학교명")) {
            const schoolInput = document.createElement("input");
            schoolInput.type = "text";
            schoolInput.className = "school-input";
            schoolInput.placeholder = "학교명";
            row1.appendChild(schoolInput);
        }

        if (allowed.includes("학과")) {
            const majorInput = document.createElement("input");
            majorInput.type = "text";
            majorInput.className = "major-input";
            majorInput.placeholder = "학과";
            row1.appendChild(majorInput);
        }

        // 두 번째 줄 (기간 / 상태)
        const row2 = document.createElement("div");
        row2.className = "edu-row";

        if (allowed.includes("기간")) {
            const dateGroup = document.createElement("div");
            dateGroup.className = "date-group";
            dateGroup.innerHTML = `
        <input type="text" class="year-input" placeholder="YYYY">
        -
        <input type="text" class="month-input" placeholder="MM">
        ~
        <input type="text" class="year-input" placeholder="YYYY">
        -
        <input type="text" class="month-input" placeholder="MM">
      `;
            row2.appendChild(dateGroup);
        }

        if (allowed.includes("상태")) {
            const statusInput = document.createElement("input");
            statusInput.type = "text";
            statusInput.className = "status-input";
            statusInput.placeholder = "상태";
            row2.appendChild(statusInput);
        }

        eduItem.appendChild(row1);
        eduItem.appendChild(row2);

        // 삭제 버튼 동작
        eduItem.querySelector(".edu-del").addEventListener("click", () => {
            eduItem.remove();
        });

        return eduItem;
    };

    // ✅ 기본 항목 1개 추가
    const firstItem = createEduItem();
    eduBox.appendChild(firstItem);

    // ✅ 추가 버튼
    const addBtn = document.createElement("button");
    addBtn.className = "edu-btn";
    addBtn.innerHTML = `<span class="plus">＋</span>`;

    addBtn.addEventListener("click", () => {
        const newItem = createEduItem();
        eduBox.appendChild(newItem);
        console.log("✨ [EDU] 새 학력 항목 추가됨");

        // ✅ drop 이벤트 재등록
        setTimeout(() => {
            if (typeof attachEduDropEvent === "function") {
                attachEduDropEvent(newItem);
                console.log("✨ [EDU] 새 항목 drop 이벤트 등록 완료");
            }
        }, 100);
    });

    // ✅ 조립
    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(eduBox);
    sectionBox.appendChild(addBtn);

    return sectionBox;
}



// ✅ 희망직무 섹션을 동적으로 렌더링하는 함수
function renderJobSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";
    sectionBox.dataset.sectionId = section.id;     // ✅ 이걸 추가!
    sectionBox.dataset.coSectionId = section.id;   // ✅ 기존 것도 유지 (기업 섹션용)


    const conditionText = [];
    if (section.multiSelect) conditionText.push("복수선택 가능");
    if (!section.multiSelect && section.type === "선택형") conditionText.push("단일선택");
    conditionText.push(...section.conditions);

    const title = `${section.title}(${conditionText.join(", ")})`;

    const sectionTitle = document.createElement("div");
    sectionTitle.className = "section-title";
    sectionTitle.innerHTML = `
        <div class="number">${number}.</div>
        <div class="title-content">
            <h3>${title}</h3>
            <p class="section-desc">${section.comment || "기업이 작성한 설명 칸 입니다."}</p>
        </div>
    `;

    const tagList = document.createElement("div");
    tagList.className = "tag-list";

    section.tags?.forEach((tag, idx) => {
        const label = document.createElement("label");
        label.innerHTML = `<input type="checkbox" ${idx === 0 ? "checked" : ""}> ${tag}`;
        tagList.appendChild(label);
    });

    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(tagList);

    sectionBox.dataset.coSectionId = section.id;
    sectionBox.dataset.title = section.title;
    sectionBox.dataset.type = section.type;

    return sectionBox;
}

// ✅ 경력사항 섹션을 동적으로 렌더링하는 함수 (드롭 이벤트 대응)
function renderCareerSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";
    sectionBox.dataset.sectionId = section.id;
    sectionBox.dataset.coSectionId = section.id;
    sectionBox.dataset.title = section.title;

    const sectionTitle = document.createElement("div");
    sectionTitle.className = "section-title";
    sectionTitle.innerHTML = `
        <div class="number">${number}.</div>
        <div class="title-content">
            <h3>${section.title}</h3>
            <p class="section-desc">${section.comment || "기업이 작성한 설명 칸 입니다."}</p>
        </div>
    `;

    const careerBox = document.createElement("div");
    careerBox.className = "career-box";

    const createCareerItem = () => {
        const item = document.createElement("div");
        item.className = "career-item";

        item.innerHTML = `<button type="button" class="career-del">✕</button>`;

        // 1줄: 회사명, 직무
        const row1 = document.createElement("div");
        row1.className = "career-row";
        if (section.conditions.includes("회사명")) {
            row1.innerHTML += `<input type="text" class="company-input" placeholder="회사명">`;
        }
        if (section.conditions.includes("직무")) {
            row1.innerHTML += `<input type="text" class="job-input" placeholder="직무">`;
        }

        // 2줄: 기간, 상태
        const row2 = document.createElement("div");
        row2.className = "career-row";
        if (section.conditions.includes("근무 개월수") || section.conditions.includes("기간")) {
            row2.innerHTML += `
                <div class="date-group">
                    <input type="text" class="year-input" placeholder="YYYY">
                    -
                    <input type="text" class="month-input" placeholder="MM">
                    ~
                    <input type="text" class="year-input" placeholder="YYYY">
                    -
                    <input type="text" class="month-input" placeholder="MM">
                </div>
            `;
        }
        if (section.conditions.includes("상태")) {
            row2.innerHTML += `<input type="text" class="status-input" placeholder="재직/퇴사">`;
        }

        item.appendChild(row1);
        item.appendChild(row2);

        // ✅ 각 경력 항목마다 설명 textarea 추가
        const descRow = document.createElement("div");
        descRow.className = "career-row";
        const descTextarea = document.createElement("textarea");
        descTextarea.className = "career-desc";
        descTextarea.placeholder = "경력사항 관련 설명 입력";
        descRow.appendChild(descTextarea);
        item.appendChild(descRow);

        return item;
    };

    const careerItem = createCareerItem();
    careerBox.appendChild(careerItem);

    const addBtn = document.createElement("button");
    addBtn.className = "career-btn";
    addBtn.innerHTML = `<span class="plus">＋</span>`;
    addBtn.addEventListener("click", () => {
        const clone = createCareerItem();
        clone.querySelectorAll("input").forEach(el => el.value = "");
        careerBox.appendChild(clone);
    });


    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(careerBox);
    sectionBox.appendChild(addBtn);

    return sectionBox;
}



// ✅ 포트폴리오 섹션을 동적으로 렌더링하는 함수 (날짜·상태·파일경로 포함 + 드롭 대응)
function renderPortfolioSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";
    sectionBox.dataset.sectionId = section.id;     // ✅ 이걸 추가!
    sectionBox.dataset.coSectionId = section.id;   // ✅ 기존 것도 유지 (기업 섹션용)


    // 🔹 섹션 제목
    const sectionTitle = document.createElement("div");
    sectionTitle.className = "section-title";
    sectionTitle.innerHTML = `
        <div class="number">${number}.</div>
        <div class="title-content">
            <h3>${section.title}</h3>
            <p class="section-desc">${section.comment || "기업이 작성한 설명 칸 입니다.."}</p>
        </div>
    `;

    // ✅ 드롭 이벤트 대상
    const portfolioBox = document.createElement("div");
    portfolioBox.className = "portfolio-box";

    // 🔹 기본 포트폴리오 입력칸 (프로젝트명, 기간, 상태, 설명, 파일)
    const portfolioItem = document.createElement("div");
    portfolioItem.className = "portfolio-item";
    portfolioItem.innerHTML = `
        <button type="button" class="portfolio-del">✕</button>

        <!-- 프로젝트명 -->
        <div class="portfolio-row">
            <input type="text" class="portfolio-title" placeholder="프로젝트 또는 공모전명" style="flex:1;">
        </div>

        <!-- 파일 경로 -->
        <div class="portfolio-row">
            <input type="text" class="portfolio-file-path" placeholder="드래그된 파일 경로" readonly>
            <input type="hidden" name="filePath">
        </div>

        <!-- 기간(년/월) + 상태 -->
        <div class="portfolio-row">
            <div class="date-group">
                <input type="text" class="year-input" placeholder="YYYY">
                -
                <input type="text" class="month-input" placeholder="MM">
                ~
                <input type="text" class="year-input" placeholder="YYYY">
                -
                <input type="text" class="month-input" placeholder="MM">
            </div>

            <input type="text" class="status-input" placeholder="상태">
        </div>

        <!-- 설명 -->
        <div class="portfolio-row">
            <input type="text" class="desc-input" placeholder="설명" style="width:100%;">
        </div>
    `;

    // 🔹 추가 버튼
    const addBtn = document.createElement("button");
    addBtn.className = "portfolio-btn";
    addBtn.innerHTML = `<span class="plus">＋</span>`;
    addBtn.addEventListener("click", () => {
        const clone = portfolioItem.cloneNode(true);
        clone.querySelectorAll("input, textarea").forEach(el => el.value = "");
        portfolioBox.appendChild(clone);

        // ✅ 새로 추가된 항목에도 드롭 이벤트 연결
        attachPortfolioDropEvent(clone);
    });

    // ✅ 구성 정리
    portfolioBox.appendChild(portfolioItem);
    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(portfolioBox);

    // ✅ 여기에 아래 코드 추가!!
    const hiddenUploadBox = document.createElement("div");
    hiddenUploadBox.className = "upload-box";
    hiddenUploadBox.style.display = "none";
    sectionBox.appendChild(hiddenUploadBox);

    sectionBox.appendChild(addBtn);

    return sectionBox;
}




// ✅ 자기소개 섹션 렌더링 함수
function renderSelfIntroSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";
    sectionBox.dataset.sectionId = section.id;     // ✅ 이걸 추가!
    sectionBox.dataset.coSectionId = section.id;   // ✅ 기존 것도 유지 (기업 섹션용)


    // 복수선택 여부 제외하고 조건만 괄호에 넣음
    const conditionText = [...section.conditions];
    const title = conditionText.length > 0
        ? `${section.title}(${conditionText.join(", ")})`
        : section.title;

    // 제목과 설명
    const sectionTitle = document.createElement("div");
    sectionTitle.className = "section-title";
    sectionTitle.innerHTML = `
        <div class="number">${number}.</div>
        <div class="title-content">
            <h3>${title}</h3>
            <p class="section-desc">${section.comment || "기업이 작성한 설명 칸 입니다.."}</p>
        </div>
    `;

    // 자기소개 textarea + 글자 수
    const textarea = document.createElement("textarea");
    textarea.id = "selfIntro";
    textarea.placeholder = "입력해주세요.";

    const charCount = document.createElement("div");
    charCount.id = "charCount";
    charCount.className = "char-count";
    charCount.textContent = "0 / 500";

    // 글자 수 실시간 반영
    textarea.addEventListener("input", () => {
        const len = textarea.value.length;
        charCount.textContent = `${len} / 500`;
        if (len < 500) {
            charCount.classList.add("warning");
        } else {
            charCount.classList.remove("warning");
        }
    });

    // 조립
    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(textarea);
    sectionBox.appendChild(charCount);

    sectionBox.dataset.coSectionId = section.id;
    sectionBox.dataset.title = section.title;
    sectionBox.dataset.type = section.type;

    return sectionBox;
}

// ✅ 선택형 섹션 렌더링 함수
function renderSelectSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";
    sectionBox.dataset.sectionId = section.id;     // ✅ 이걸 추가!
    sectionBox.dataset.coSectionId = section.id;   // ✅ 기존 것도 유지 (기업 섹션용)


    const conditionText = [];
    if (!section.multiSelect && section.type === "선택형") conditionText.push("단일선택");
    if (section.multiSelect) conditionText.push("복수선택 가능");
    conditionText.push(...section.conditions);

    const title = `${section.title}(${conditionText.join(", ")})`;

    const sectionTitle = document.createElement("div");
    sectionTitle.className = "section-title";
    sectionTitle.innerHTML = `
    <div class="number">${number}.</div>
    <div class="title-content">
      <h3>${title}</h3>
      <p class="section-desc">${section.comment || "기업이 작성한 설명 칸 입니다."}</p>
    </div>
  `;

    const tagList = document.createElement("div");
    tagList.className = "tag-list";

    section.tags?.forEach(tag => {
        const label = document.createElement("label");
        label.innerHTML = `<input type="${section.multiSelect ? "checkbox" : "radio"}" name="select-${number}"> ${tag}`;
        tagList.appendChild(label);
    });

    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(tagList);

    sectionBox.dataset.coSectionId = section.id;
    sectionBox.dataset.title = section.title;
    sectionBox.dataset.type = section.type;

    return sectionBox;
}
// ✅ 서술형 섹션 렌더링 함수
function renderDescriptiveSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";
    sectionBox.dataset.sectionId = section.id;     // ✅ 이걸 추가!
    sectionBox.dataset.coSectionId = section.id;   // ✅ 기존 것도 유지 (기업 섹션용)


    const conditionText = [...section.conditions];  // 복수선택 안 넣음
    const title = conditionText.length > 0
        ? `${section.title}(${conditionText.join(", ")})`
        : section.title;

    const sectionTitle = document.createElement("div");
    sectionTitle.className = "section-title";
    sectionTitle.innerHTML = `
    <div class="number">${number}.</div>
    <div class="title-content">
      <h3>${title}</h3>
      <p class="section-desc">${section.comment || "기업이 작성한 설명 칸 입니다."}</p>
    </div>
  `;

    const conditionBox = document.createElement("div");
    conditionBox.className = "tag-list";
    section.conditions?.forEach(cond => {
        const span = document.createElement("span");
        span.className = "tag condition selected-tag";
        span.textContent = cond;
        conditionBox.appendChild(span);
    });

    const textarea = document.createElement("textarea");
    textarea.placeholder = "자유롭게 입력해주세요.";
    textarea.value = section.content || "";

    sectionBox.appendChild(sectionTitle);
    if (section.conditions?.length) sectionBox.appendChild(conditionBox);
    sectionBox.appendChild(textarea);

    sectionBox.dataset.coSectionId = section.id;
    sectionBox.dataset.title = section.title;
    sectionBox.dataset.type = section.type;

    return sectionBox;
}
// ✅ 사진 첨부 섹션을 렌더링하는 함수
function renderPhotoSection(section, number) {
    // section-box 생성
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";
    sectionBox.dataset.sectionId = section.id;     // ✅ 이걸 추가!
    sectionBox.dataset.coSectionId = section.id;   // ✅ 기존 것도 유지 (기업 섹션용)


    // 조건 텍스트(복수선택은 제외하고 조건만 사용)
    const conditionText = [...section.conditions];
    const title = conditionText.length > 0
        ? `${section.title}(${conditionText.join(", ")})`
        : section.title;

    // 섹션 제목 + 설명 영역 생성
    const sectionTitle = document.createElement("div");
    sectionTitle.className = "section-title";
    sectionTitle.innerHTML = `
    <div class="number">${number}.</div>
    <div class="title-content">
      <h3>${title}</h3>
      <p class="section-desc">${section.comment || "기업이 작성한 설명 칸 입니다."}</p>
    </div>
  `;

    // ✅ 사진 설명 입력용 textarea
    const textarea = document.createElement("textarea");
    textarea.placeholder = "사진 관련 설명 입력";
    textarea.value = section.content || "";

    // ✅ 파일 업로드 UI 생성
    const wrapper = document.createElement("div");
    wrapper.className = "file-upload-wrapper";


// ✅ 파일 선택 라벨 및 input
    const label = document.createElement("label");
    label.setAttribute("for", `photoUpload${number}`);
    label.className = "file-label";
    label.textContent = "사진 선택";

    const input = document.createElement("input");
    input.type = "file";
    input.id = `photoUpload${number}`;
    input.accept = "image/*";
    input.style.display = "none";

// ✅ 파일명 표시 영역
    const fileNameSpan = document.createElement("span");
    fileNameSpan.className = "file-name";
    fileNameSpan.textContent = "선택된 파일 없음";

// ✅ 파일 선택 시 파일명 표시
    input.addEventListener("change", () => {
        const file = input.files[0];
        fileNameSpan.textContent = file ? file.name : "선택된 파일 없음";
    });

// ✅ 조립
    wrapper.appendChild(label);
    wrapper.appendChild(input);
    wrapper.appendChild(fileNameSpan);

    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(textarea);
    sectionBox.appendChild(wrapper);

    sectionBox.dataset.coSectionId = section.id;
    sectionBox.dataset.title = section.title;
    sectionBox.dataset.type = section.type;

    return sectionBox;
}
// ✅ 파일첨부 섹션을 동적으로 렌더링하는 함수
function renderFileSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";
    sectionBox.dataset.sectionId = section.id;     // ✅ 이걸 추가!
    sectionBox.dataset.coSectionId = section.id;   // ✅ 기존 것도 유지 (기업 섹션용)


    // 조건 텍스트 (복수선택은 포함 안 함)
    const conditionText = [...section.conditions];
    const title = conditionText.length > 0
        ? `${section.title}(${conditionText.join(", ")})`
        : section.title;

    // 제목 + 설명
    const sectionTitle = document.createElement("div");
    sectionTitle.className = "section-title";
    sectionTitle.innerHTML = `
      <div class="number">${number}.</div>
      <div class="title-content">
        <h3>${title}</h3>
        <p class="section-desc">${section.comment || "기업이 작성한 설명 칸 입니다."}</p>
      </div>
    `;

    // 설명 입력 textarea
    const textarea = document.createElement("textarea");
    textarea.placeholder = "파일 관련 설명 입력";

    const uploadWrapper = document.createElement("div");
    uploadWrapper.className = "file-upload-wrapper";

    const label = document.createElement("label");
    label.className = "file-label";
    label.textContent = "파일 선택";

    const fileInput = document.createElement("input");
    fileInput.type = "file";
    fileInput.className = "file-input";
    fileInput.style.display = "none";

    const fileName = document.createElement("span");
    fileName.className = "file-name";
    fileName.textContent = "선택된 파일 없음";

    fileInput.addEventListener("change", () => {
        fileName.textContent = fileInput.files.length > 0 ? fileInput.files[0].name : "선택된 파일 없음";
    });

    label.appendChild(fileInput);
    uploadWrapper.appendChild(label);
    uploadWrapper.appendChild(fileName);

    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(textarea);
    sectionBox.appendChild(uploadWrapper);

    sectionBox.dataset.coSectionId = section.id;
    sectionBox.dataset.title = section.title;
    sectionBox.dataset.type = section.type;


    return sectionBox;
}


// ✅ 페이지 로드시 수상 탭이 비어있으면 empty-content 보이게 하기
window.addEventListener('DOMContentLoaded', () => {
    // 📌 이거 꼭 필요합니다!
    window.addEventListener('dragover', e => e.preventDefault());
    window.addEventListener('drop', e => e.preventDefault());

    const defaultTab = document.querySelector('.tab.active');
    if (!defaultTab) return;

    // ✅ 학력 탭 자동 클릭되게 하기
    const schoolTab = document.querySelector('.tab[data-tab="school"]');
    if (schoolTab) schoolTab.click();


    const tabName = defaultTab.dataset.tab;
    const targetContent = document.querySelector(`.tab-content[data-content="${tabName}"]`);
    const isEmpty = !targetContent || targetContent.children.length === 0;
    const emptyBox = document.querySelector('.empty-content');
    if (emptyBox) {
        emptyBox.style.display = isEmpty ? 'block' : 'none';
    }

    document.querySelector('.empty-content').style.display = isEmpty ? 'block' : 'none';

    // ✅ 사용자 정보 동적 렌더링
    fetch("/api/user/resumes/me")
        .then(res => res.json())
        .then(user => {
            if (!user) return;

            document.getElementById("profileImage").src = user.profileImageUrl || "/images/profile.png";
            document.getElementById("userName").textContent = user.userName || "이름 없음";
            document.getElementById("mainLanguage").textContent = user.mainLanguage || "";
            document.getElementById("sex").textContent = user.sex || "";
            document.getElementById("birthday").textContent = user.birthday || "";
            document.getElementById("phone").textContent = user.userPhone || "";
            document.getElementById("email").textContent = user.userEmail || "";
            document.getElementById("region").textContent = user.region || "";
            document.getElementById("chinese").innerText = user.nameHanja || "-";
            document.getElementById("english").innerText = user.nameEng || "-";

        })
        .catch(err => console.error("내 정보 불러오기 실패:", err));


    // ✅ 드래그 가능한 항목 설정
    document.querySelectorAll('.award-item').forEach(item => {
        item.setAttribute('draggable', true);
        item.addEventListener('dragstart', e => {
            const title = item.innerText.split('\n')[0];e.dataTransfer.setData('application/json', JSON.stringify({
                id: item.dataset.id,
                type: item.dataset.type,
                file: item.dataset.file || "",
                title: title
            }));

        });
    });



    // ✅  기업 이력서 양식 동적 불러오기
    const urlParams = new URLSearchParams(window.location.search);  // 주소에서 쿼리스트링 추출
    const resumeId = urlParams.get("id");
    const jobPostId = urlParams.get("jobPostId");

    // 기업에서 설정한 이력서 양식 정보를 API로 요청
    fetch(`/api/user/resumes/init?id=${resumeId}`)
        .then(res => res.json())
        .then(data => {
            console.log("기업 이력서 양식:", data);

            // 페이지 상단 제목 변경
            document.querySelector('.resume-title h2').textContent = `${data.title} 이력서 작성`;

            // ✅ 모든 섹션을 순서대로 렌더링
            data.sections.forEach((section, index) => {

                console.log(`[${index}] section.id =`, section.id, section);

                if (!section.id) {
                    console.warn("⚠️ section.id가 없습니다!", section);
                }

                let rendered;

                // ✅ '일반회원 정보'는 스킵 (이미 내 정보에서 표현됨)
                if (section.title === '일반회원 정보') return;

                // 고정 항목들 먼저 처리
                if (section.title === '학력사항') {
                    rendered = renderEducationSection(section, index + 1);
                } else if (section.title === '희망직무') {
                    rendered = renderJobSection(section, index + 1);
                } else if (section.title === '경력사항') {
                    rendered = renderCareerSection(section, index + 1);
                } else if (section.title === '포트폴리오') {
                    rendered = renderPortfolioSection(section, index + 1);
                } else if (section.title === '자기소개') {
                    // 자기소개는 따로! 여기서 처리했으면 return
                    rendered = renderSelfIntroSection(section, index + 1);
                } else {
                    // 나머지 사용자 추가 섹션 처리
                    if (section.type === '선택형') {
                        rendered = renderSelectSection(section, index + 1);
                    } else if (section.type === '서술형') {
                        rendered = renderDescriptiveSection(section, index + 1);
                    } else if (section.type === '사진 첨부') {
                        rendered = renderPhotoSection(section, index + 1);  // 📌 요 줄 추가!
                    } else if (section.type === '파일 첨부') {
                        rendered = renderFileSection(section, index + 1);
                    }

                }

                if (rendered) {
                    if (!section.id) {
                        console.warn(`❌ section.id 누락 – dataset 설정 안됨`, section);
                    } else {
                        rendered.dataset.coSectionId = section.id;
                    }

                    const leftContent = document.querySelector('.left-content');
                    const submitWrapper = document.querySelector('.submit-wrapper');
                    leftContent.insertBefore(rendered, submitWrapper);
                }
            });

            // ✅ 섹션 렌더링이 모두 끝난 뒤 drop 이벤트 등록
            console.log("✅ 모든 섹션 렌더링 완료, 드롭 이벤트 등록 시작");

            document.querySelectorAll(".section-box").forEach(section => {
                const title = section.querySelector("h3")?.textContent || "";

                // 🔹 학력 (EDUCATION)
                if (title.includes("학력")) {
                    console.log("🎯 [EDU] 학력 섹션에 drop 이벤트 등록 준비");

                    // ✅ 개별 학력 아이템에 drop 이벤트 등록 함수
                    const attachEduDropEvent = (item) => {
                        item.addEventListener("dragover", e => {
                            e.preventDefault();
                            e.stopPropagation();
                        });

                        item.addEventListener("drop", e => {
                            e.preventDefault();
                            e.stopPropagation();
                            console.log("🔥 [EDU] 개별 학력 item drop 감지됨!");

                            const data = e.dataTransfer.getData("application/json");
                            if (!data) {
                                console.warn("⚠️ [EDU] 드래그 데이터 없음");
                                return;
                            }

                            let json;
                            try {
                                json = JSON.parse(data);
                            } catch {
                                console.warn("⚠️ [EDU] JSON 파싱 실패");
                                return;
                            }

                            if (json.type !== "EDUCATION") {
                                console.log("🚫 [EDU] type이 EDUCATION 아님:", json.type);
                                return;
                            }

                            console.log("📦 [EDU] 받은 데이터:", json);

                            // ✅ 현재 item 내부의 input만 찾기
                            const schoolInput = item.querySelector("input[placeholder='학교명']") || item.querySelector(".school-input");
                            const majorInput = item.querySelector("input[placeholder='학과']") || item.querySelector(".major-input");
                            const statusInput = item.querySelector("input[placeholder='상태']") || item.querySelector(".status-input");
                            const yearInputs = item.querySelectorAll("input[placeholder='YYYY']");
                            const monthInputs = item.querySelectorAll("input[placeholder='MM']");

                            const [startY, startM] = (json.startDate || "").split("-");
                            const [endY, endM] = (json.endDate || "").split("-");

                            // ✅ 값 입력
                            if (schoolInput) schoolInput.value = json.schoolName || "";
                            if (majorInput) majorInput.value = json.majorName || "";
                            if (statusInput) statusInput.value = json.status || "졸업";
                            if (yearInputs.length >= 2) {
                                yearInputs[0].value = startY || "";
                                yearInputs[1].value = endY || "";
                            }
                            if (monthInputs.length >= 2) {
                                monthInputs[0].value = startM || "";
                                monthInputs[1].value = endM || "";
                            }

                            console.log("✅ [EDU] 학력 정보 자동입력 완료!");
                        });
                    };

                    // ✅ 이미 존재하는 학력 항목들에 이벤트 등록
                    section.querySelectorAll(".edu-item, .edu-box, .resume-section").forEach(item => {
                        attachEduDropEvent(item);
                    });

                    // ✅ 추가 버튼 클릭 시 새로 생긴 항목에도 자동 등록
                    const addBtn = section.querySelector(".edu-btn");
                    if (addBtn) {
                        addBtn.addEventListener("click", () => {
                            setTimeout(() => {
                                const newItem = section.querySelector(".edu-item:last-child, .edu-box:last-child");
                                if (newItem) {
                                    attachEduDropEvent(newItem);
                                    console.log("✨ [EDU] 새로 추가된 학력 item에도 drop 이벤트 연결 완료");
                                }
                            }, 100);
                        });
                    }
                }



                // 🔹 경력 (JOB)
                else if (title.includes("경력")) {
                    console.log("🎯 [JOB] 경력 섹션에 drop 이벤트 등록 준비");

                    // ✅ 개별 경력 item에 드롭 이벤트 등록하는 함수
                    const attachCareerDropEvent = (item) => {
                        item.addEventListener("dragover", e => e.preventDefault());
                        item.addEventListener("drop", e => {
                            e.preventDefault();
                            console.log("🔥 [JOB] 개별 career-item drop 감지됨!");

                            const data = e.dataTransfer.getData("application/json");
                            if (!data) {
                                console.warn("⚠️ [JOB] 드래그 데이터 없음");
                                return;
                            }

                            let json;
                            try {
                                json = JSON.parse(data);
                            } catch (err) {
                                console.error("❌ [JOB] JSON 파싱 실패:", err);
                                return;
                            }

                            if (json.type !== "JOB") {
                                console.log("🚫 [JOB] type이 JOB 아님:", json.type);
                                return;
                            }

                            console.log("📦 [JOB] 받은 데이터:", json);

                            // ✅ 현재 아이템 내의 입력칸만 찾기
                            const companyInput = item.querySelector("input[placeholder='회사명']") || item.querySelector(".company-input");
                            const jobInput = item.querySelector("input[placeholder='직무']") || item.querySelector(".job-input");
                            const statusInput = item.querySelector("input[placeholder='재직/퇴사']") || item.querySelector(".status-input");

                            const yearInputs = item.querySelectorAll("input[placeholder='YYYY']");
                            const monthInputs = item.querySelectorAll("input[placeholder='MM']");

                            // ✅ 날짜 파싱
                            const [startY, startM] = (json.startDate || json.jobStart || "").split("-");
                            const [endY, endM] = (json.endDate || json.jobEnd || "").split("-");

                            console.log("📆 추출된 날짜:", { startY, startM, endY, endM });

                            // ✅ 값 입력
                            if (companyInput) companyInput.value = json.companyName || json.workplace || "";
                            if (jobInput) jobInput.value = json.jobTitle || json.title || "";
                            if (statusInput) statusInput.value = json.status || "퇴사";

                            if (yearInputs.length >= 2) {
                                yearInputs[0].value = startY || "";
                                yearInputs[1].value = endY || "";
                            }
                            if (monthInputs.length >= 2) {
                                monthInputs[0].value = startM || "";
                                monthInputs[1].value = endM || "";
                            }

                            console.log("✅ [JOB] 경력 정보 자동입력 완료!");
                        });
                    };

                    // ✅ 이미 렌더된 모든 career-item에 이벤트 등록
                    section.querySelectorAll(".career-item").forEach(item => {
                        attachCareerDropEvent(item);
                    });

                    // ✅ 새로 추가되는 항목에도 자동 등록
                    const addBtn = section.querySelector(".career-btn");
                    if (addBtn) {
                        addBtn.addEventListener("click", () => {
                            setTimeout(() => {
                                const newItem = section.querySelector(".career-item:last-child");
                                if (newItem) {
                                    attachCareerDropEvent(newItem);
                                    console.log("✨ [JOB] 새로 추가된 career-item에도 drop 이벤트 연결 완료");
                                }
                            }, 100);
                        });
                    }
                }


                // 🔹 포트폴리오 (PROJECT)
                else if (title.includes("포트폴리오") || title.includes("프로젝트")) {
                    console.log("🎯 [PROJECT] 포트폴리오 섹션에 drop 이벤트 등록 준비");

                    // ✅ 드롭 이벤트 등록 함수 (개별 item용)
                    const attachPortfolioDropEvent = (item) => {
                        item.addEventListener("dragover", e => e.preventDefault());
                        item.addEventListener("drop", e => {
                            e.preventDefault();
                            console.log("🔥 [PROJECT] 개별 포트폴리오 item drop 감지됨!");

                            const data = e.dataTransfer.getData("application/json");
                            if (!data) return;

                            let json;
                            try {
                                json = JSON.parse(data);
                            } catch (err) {
                                console.error("❌ [PROJECT] JSON 파싱 실패:", err);
                                return;
                            }
                            // ✅ 디버깅용 로그 (추가)
                            console.log("🎯 [PROJECT] 드롭된 데이터 전체:", json);


                            if (json.type !== "PROJECT" && json.type !== "CONTEST") return;

                            console.log("📦 [PROJECT] 받은 데이터:", json);

                            // ✅ 현재 item 내부의 입력칸 찾기
                            const titleInput =
                                item.querySelector("input[placeholder*='프로젝트']") ||
                                item.querySelector("input[placeholder*='공모전']") ||
                                item.querySelector(".portfolio-title");

                            const descInput =
                                item.querySelector("textarea[placeholder*='설명']") ||
                                item.querySelector("input[placeholder*='설명']") ||
                                item.querySelector(".desc-input");

                            const statusInput =
                                item.querySelector("input[placeholder*='상태']") ||
                                item.querySelector(".status-input");

                            const filePathInput =
                                item.querySelector("input[placeholder*='파일']") ||
                                item.querySelector(".portfolio-file-path");

                            const hiddenFileInput =
                                item.querySelector("input[name='filePath']");

                            const yearInputs = item.querySelectorAll("input[placeholder='YYYY']");
                            const monthInputs = item.querySelectorAll("input[placeholder='MM']");

                            const [startY, startM] = (json.startDate || "").split("-");
                            const [endY, endM] = (json.endDate || "").split("-");

                            if (titleInput) titleInput.value = json.projectName || json.title || "";
                            if (descInput) descInput.value = json.description || "";
                            if (statusInput) statusInput.value = json.status || "완료";
                            if (json.type === "CONTEST") {
                                const gradeText = json.grade ? `🏆 ${json.grade}` : "참여";
                                if (filePathInput) filePathInput.value = gradeText;
                                if (hiddenFileInput) hiddenFileInput.value = gradeText;
                            } else {
                                const filePathValue = json.filePath || json.fileUrl || json.file || "";
                                if (filePathInput) filePathInput.value = filePathValue;
                                if (hiddenFileInput) hiddenFileInput.value = filePathValue;
                            }

                            if (yearInputs.length >= 2) {
                                yearInputs[0].value = startY || "";
                                yearInputs[1].value = endY || "";
                            }
                            if (monthInputs.length >= 2) {
                                monthInputs[0].value = startM || "";
                                monthInputs[1].value = endM || "";
                            }

                            console.log("✅ [PROJECT] 포트폴리오 정보 자동입력 완료!");

                            // ✅ [PROJECT] 포트폴리오 정보 자동입력 완료!  아래에 추가했던 코드 부분 교체
                            let sectionBox = e.target.closest(".section-box");
                            if (!sectionBox) {
                                // 🔁 이벤트 버블링 때문에 e.currentTarget(드롭 이벤트 등록된 요소)에서 찾기
                                sectionBox = e.currentTarget.closest(".section-box");
                            }
                            const sectionId = sectionBox?.getAttribute("data-section-id");
                            console.log("📦 저장용 섹션 ID:", sectionId);

                            if (sectionId) {
                                const section = sections.find(sec => sec.id == sectionId);
                                if (section) {
                                    if (!section.dragItems) section.dragItems = [];
                                    section.dragItems.push({
                                        itemType: "PROJECT",
                                        referenceId: data.referenceId,
                                        displayText: data.displayText,
                                        filePath: data.filePath,
                                        startDate: data.startDate,
                                        endDate: data.endDate
                                    });
                                    console.log("💾 [PROJECT] dragItems에 저장 완료:", section.dragItems);
                                } else {
                                    console.warn("⚠️ 해당 ID의 섹션을 찾을 수 없습니다:", sectionId);
                                }
                            } else {
                                console.warn("⚠️ 섹션 ID를 찾을 수 없습니다. DOM 구조 확인 필요.");
                            }

                        });
                    };

                    // ✅ 기존에 렌더링된 모든 포트폴리오 아이템에 이벤트 연결
                    section.querySelectorAll(".portfolio-item").forEach(item => {
                        attachPortfolioDropEvent(item);
                    });

                    // ✅ 새로 추가되는 항목에도 자동 연결
                    const addBtn = section.querySelector(".portfolio-btn");
                    if (addBtn) {
                        addBtn.addEventListener("click", () => {
                            setTimeout(() => {
                                const newItem = section.querySelector(".portfolio-item:last-child");
                                if (newItem) {
                                    attachPortfolioDropEvent(newItem);
                                    console.log("✨ [PROJECT] 새로 추가된 포트폴리오 item에도 drop 이벤트 연결 완료");
                                }
                            }, 100);
                        });
                    }
                }

                
            });


            // ✅ 공고 정보 동적 렌더링
            if (jobPostId) {
                fetch(`/api/job-post/info?jobPostId=${jobPostId}`)
                    .then(res => res.json())
                    .then(info => {
                        document.querySelector('.resume-title p').textContent = `안녕하세요 ${info.companyName}회사 이력서 작성 폼입니다.`;
                        document.querySelector('.resume-title small').textContent = `${info.startDate} ~ ${info.endDate}`;
                    })
                    .catch(err => console.error("공고 정보 불러오기 실패:", err));
            }



        })
        .catch(err => console.error('양식 불러오기 실패:', err));

});

document.querySelector(".modal-cancel").onclick = () => {
    document.getElementById("submitModal").style.display = "none";
};

document.querySelector(".modal-confirm").onclick = () => {
    alert("이력서가 제출되었습니다!");
    document.getElementById("submitModal").style.display = "none";
};

// ✅ 미리보기 버튼 클릭 시 → 서버에 자기소개 저장 요청
function togglePreview() {
    const urlParams = new URLSearchParams(window.location.search);
    const coResumeId = urlParams.get("id");

    const sectionBoxes = document.querySelectorAll(".section-box[data-co-section-id]");
    const sections = [];
    const uploadPromises = [];

    sectionBoxes.forEach(box => {
        const coSectionId = Number(box.dataset.coSectionId);
        const title = box.dataset.title || "제목 없음";     // ✅ title 속성
        const type = box.dataset.type || "서술형";          // ✅ type 속성

        // ✅ 섹션 내 모든 textarea 내용 합치기
        const textareas = box.querySelectorAll("textarea");
        let content = "";

        if (textareas.length > 0) {
            content = [...textareas]
                .map(t => t.value.trim())
                .filter(v => v.length > 0)
                .join("\n"); // 줄바꿈 기준으로 연결
        }


        const selectedTags = [...box.querySelectorAll("input[type=checkbox]:checked, input[type=radio]:checked")]
            .map(input => input.parentElement.textContent.trim());

        const section = {
            coSectionId,
            title,        // ✅ 추가
            type,         // ✅ 추가
            content,
            selectedTags
        };

        // ✅ 학력사항 처리 (최신 구조 대응)
        const eduBox = box.querySelector(".edu-box");
        if (eduBox) {
            const educations = [];
            const items = eduBox.querySelectorAll(".edu-item");
            items.forEach(item => {
                const yearInputs = item.querySelectorAll("input[placeholder='YYYY']");
                const monthInputs = item.querySelectorAll("input[placeholder='MM']");

                educations.push({
                    schoolName: item.querySelector(".school-input")?.value || "",
                    majorName: item.querySelector(".major-input")?.value || "",
                    status: item.querySelector(".status-input")?.value || "",
                    startYear: yearInputs[0]?.value || "",
                    startMonth: monthInputs[0]?.value || "",
                    endYear: yearInputs[1]?.value || "",
                    endMonth: monthInputs[1]?.value || ""
                });
            });

            section.educations = educations;
        }

        // ✅ 경력사항 처리 (JobHistoryDTO와 완벽 매칭)
        if (box.querySelector("h3")?.textContent.includes("경력")) {  // 🔥 경력 섹션에서만 실행
            const careerBox = box.querySelector(".career-box");
            if (careerBox) {
                const careers = [];
                const items = careerBox.querySelectorAll(".career-item");

                items.forEach(item => {
                    const yearInputs = item.querySelectorAll("input[placeholder='YYYY']");
                    const monthInputs = item.querySelectorAll("input[placeholder='MM']");

                    const startYear = yearInputs[0]?.value || "";
                    const startMonth = monthInputs[0]?.value || "";
                    const endYear = yearInputs[1]?.value || "";
                    const endMonth = monthInputs[1]?.value || "";

                    const startDate = (startYear && startMonth)
                        ? `${startYear}-${startMonth.padStart(2, "0")}-01`
                        : null;
                    const endDate = (endYear && endMonth)
                        ? `${endYear}-${endMonth.padStart(2, "0")}-01`
                        : null;

                    careers.push({
                        workplace: item.querySelector(".company-input")?.value || "",
                        jobTitle: item.querySelector(".job-input")?.value || "",
                        status: item.querySelector(".status-input")?.value || "",
                        startDate: startDate,
                        endDate: endDate
                    });
                });

                section.careers = careers;
            }
        }

        // ✅ 포트폴리오 섹션 처리 (PROJECT / CONTEST 구분)
        const portfolioBox = box.querySelector(".portfolio-box");
        if (portfolioBox) {
            const portfolios = [];
            const items = portfolioBox.querySelectorAll(".portfolio-item");

            items.forEach(item => {
                const yearInputs = item.querySelectorAll("input[placeholder='YYYY']");
                const monthInputs = item.querySelectorAll("input[placeholder='MM']");

                const startYear = yearInputs[0]?.value || "";
                const startMonth = monthInputs[0]?.value || "";
                const endYear = yearInputs[1]?.value || "";
                const endMonth = monthInputs[1]?.value || "";

                const startDate = (startYear && startMonth)
                    ? `${startYear}-${startMonth.padStart(2, "0")}-01`
                    : null;
                const endDate = (endYear && endMonth)
                    ? `${endYear}-${endMonth.padStart(2, "0")}-01`
                    : null;

                portfolios.push({
                    type: item.dataset.type || "PROJECT",  // dataset.type 설정해두면 여기 자동 반영됨
                    title: item.querySelector(".portfolio-title")?.value || "",
                    description: item.querySelector(".desc-input")?.value || "",
                    status: item.querySelector(".status-input")?.value || "",
                    filePath: item.querySelector(".portfolio-file-path")?.value ||
                        item.querySelector("input[name='filePath']")?.value || null,
                    startDate,
                    endDate
                });
            });

            if (portfolios.length > 0) {
                section.portfolios = portfolios;
            }
        }



        // ✅ 드래그 항목 처리 → 꼭 여기에 넣어야 합니다!
        const draggedDivs = box.querySelectorAll(".uploaded-item");
        if (draggedDivs.length > 0) {
            section.dragItems = [...draggedDivs].map(div => {
                const rawId = div.dataset.id;
                const referenceId = rawId && !isNaN(Number(rawId)) ? Number(rawId) : null;


                return {
                    coSectionId,
                    itemType: div.dataset.type || "PROJECT",
                    referenceId: referenceId,
                    displayText: div.cloneNode(true).childNodes[0]?.textContent.trim(),
                    filePath: div.dataset.file || null,
                    startDate: div.dataset.startDate || null,
                    endDate: div.dataset.endDate || null
                };
            });
        }

        const fileInput = box.querySelector("input[type=file]");
        if (fileInput && fileInput.files.length > 0) {
            const file = fileInput.files[0];

            uploadPromises.push(
                uploadFileToServer(file).then(fileName => {
                    section.fileNames = [fileName];  // ✅ 서버 저장된 UUID 파일명
                })
            );
        }

        // ✅ 여기 로그 추가
        console.log("🧩 PREVIEW: section.portfolios =", section.portfolios);
        if (portfolioBox) section.title = "포트폴리오";
        sections.push(section);
    });
    // ✅ 모든 업로드 완료 후 preview 요청
    Promise.all(uploadPromises).then(() => {
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        return fetch("/api/user/resumes/preview", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ coResumeId: Number(coResumeId), sections }),
            credentials: "include"
        });
    }).then(() => {
        // ✅ 미리보기 iframe 표시
        const container = document.getElementById("resume-preview-container");
        container.style.display = "block";
        document.getElementById("resumePreviewFrame").src = "/showresume";
    });
}
document.addEventListener("DOMContentLoaded", () => {
    disableAllInputs();

    // 동적으로 새 항목 추가 시에도 자동으로 비활성화되도록 MutationObserver 사용
    const observer = new MutationObserver(() => disableAllInputs());
    observer.observe(document.body, { childList: true, subtree: true });
});

function disableAllInputs() {
    document.querySelectorAll('.edu-row input, .career-row input, .portfolio-row input').forEach(el => {
        el.disabled = true;
        el.style.backgroundColor = '#eee';
    });
}
