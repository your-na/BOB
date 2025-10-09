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



// ✅ 학력사항 드롭 처리
document.addEventListener("DOMContentLoaded", () => {
    const educationList = document.getElementById("education-list");

    if (!educationList) return;

    educationList.addEventListener("dragover", e => {
        e.preventDefault();  // drop 허용
    });

    educationList.addEventListener("drop", e => {
        e.preventDefault();

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
        const content = textarea ? textarea.value.trim() : "";

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



        // ✅ 학력사항인 경우, education 정보 수집
        if (box.querySelector("#education-list")) {
            const educationItems = box.querySelectorAll(".education-item");
            const educations = [];

            educationItems.forEach(item => {
                educations.push({
                    schoolName: item.querySelector("input[placeholder='학교명']").value,
                    majorName: item.querySelector("input[placeholder='학과명']").value,
                    status: item.querySelector(".edu-status").value,
                    startYear: item.querySelector(".start-year").value,
                    startMonth: item.querySelector(".start-month").value,
                    endYear: item.querySelector(".end-year").value,
                    endMonth: item.querySelector(".end-month").value
                });
            });

            section.educations = educations;  // ✅ 핵심: section에 추가
        }

        sections.push(section);

        uploadPromises.push(
            uploadPromise.then(() => {
                section.uploadedFileName = uploadedFileName;
            })
        );
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


// ✅ 탭 클릭 시 콘텐츠 보여주고, 데이터 없을 경우 안내 메시지 처리
document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', function () {
        const tabName = this.dataset.tab;

        // 활성화 표시
        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        this.classList.add('active');

        // 해당 내용만 표시
        document.querySelectorAll('.tab-content').forEach(content => {
            content.style.display = content.dataset.content === tabName ? 'block' : 'none';
        });

        // 데이터 없을 때 empty-content 표시
        const targetContent = document.querySelector(`.tab-content[data-content="${tabName}"]`);
        const isEmpty = !targetContent || targetContent.children.length === 0;

        document.querySelector('.empty-content').style.display = isEmpty ? 'block' : 'none';
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

// ✅ 학력사항 섹션을 동적으로 렌더링하는 함수 (추가 버튼 + 드래그 호환)
function renderEducationSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";
    sectionBox.dataset.coSectionId = section.id;

    // ✅ 제목 저장 (미리보기용)
    sectionBox.dataset.title = section.title;

    // 🔹 섹션 제목
    const sectionTitle = document.createElement("div");
    sectionTitle.className = "section-title";
    sectionTitle.innerHTML = `
        <div class="number">${number}.</div>
        <div class="title-content">
            <h3>${section.title}</h3>
            <p class="section-desc">${section.comment || "구직자 설명입력 칸 입니다."}</p>
        </div>
    `;

    // ✅ 드롭 이벤트 대상 박스 (.edu-box)
    const eduBox = document.createElement("div");
    eduBox.className = "edu-box";

    // 🔹 기본 학력 입력칸
    const eduItem = document.createElement("div");
    eduItem.className = "edu-item"; // ✅ class 이름 통일 (drop 이벤트용)
    eduItem.innerHTML = `
        <button type="button" class="edu-del">✕</button>

        <!-- 1줄: 학교명 + 학과 -->
        <div class="edu-row">
            <input type="text" class="school-input" placeholder="학교명">
            <input type="text" class="major-input" placeholder="학과">
        </div>

        <!-- 2줄: 기간 + 상태 -->
        <div class="edu-row">
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
    `;

    // ✅ 추가 버튼
    const addBtn = document.createElement("button");
    addBtn.className = "edu-btn";
    addBtn.innerHTML = `<span class="plus">＋</span>`;

    // ✅ 클릭 시 새로운 학력 항목 추가
    addBtn.addEventListener("click", () => {
        const clone = eduItem.cloneNode(true);
        clone.querySelectorAll("input").forEach(el => (el.value = ""));
        eduBox.appendChild(clone);
        console.log("✨ [EDU] 새 학력 항목 추가됨");

        // ✅ 새 항목에도 드롭 이벤트 자동 연결
        setTimeout(() => {
            if (typeof attachEduDropEvent === "function") {
                attachEduDropEvent(clone);
                console.log("✨ [EDU] 새 항목에도 drop 이벤트 등록 완료");
            }
        }, 100);
    });

    // ✅ 전체 구성
    eduBox.appendChild(eduItem);
    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(eduBox);
    sectionBox.appendChild(addBtn);

    return sectionBox;
}



// ✅ 희망직무 섹션을 동적으로 렌더링하는 함수
function renderJobSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";

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
            <p class="section-desc">${section.comment || "구직자 설명입력 칸 입니다."}</p>
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
    sectionBox.dataset.coSectionId = section.id;

    sectionBox.dataset.title = section.title;

    // 🔹 섹션 제목
    const sectionTitle = document.createElement("div");
    sectionTitle.className = "section-title";
    sectionTitle.innerHTML = `
        <div class="number">${number}.</div>
        <div class="title-content">
            <h3>${section.title}</h3>
            <p class="section-desc">${section.comment || "구직자 설명입력 칸 입니다."}</p>
        </div>
    `;

    // ✅ 드롭 이벤트 대상 (.career-box)
    const careerBox = document.createElement("div");
    careerBox.className = "career-box";

    // 🔹 기본 경력 입력칸
    const careerItem = document.createElement("div");
    careerItem.className = "career-item";
    careerItem.innerHTML = `
        <button type="button" class="career-del">✕</button>

        <!-- 1줄: 회사명 + 직무 -->
        <div class="career-row">
            <input type="text" class="company-input" placeholder="회사명">
            <input type="text" class="job-input" placeholder="직무">
        </div>

        <!-- 2줄: 기간 + 상태 -->
        <div class="career-row">
            <div class="date-group">
                <input type="text" class="year-input" placeholder="YYYY">
                -
                <input type="text" class="month-input" placeholder="MM">
                ~
                <input type="text" class="year-input" placeholder="YYYY">
                -
                <input type="text" class="month-input" placeholder="MM">
            </div>
            <input type="text" class="status-input" placeholder="재직/퇴사">
        </div>
    `;

    // ✅ 추가 버튼
    const addBtn = document.createElement("button");
    addBtn.className = "career-btn";
    addBtn.innerHTML = `<span class="plus">＋</span>`;
    addBtn.addEventListener("click", () => {
        const clone = careerItem.cloneNode(true);
        clone.querySelectorAll("input").forEach(el => el.value = "");
        careerBox.appendChild(clone);
    });

    // ✅ 구성 정리
    careerBox.appendChild(careerItem);
    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(careerBox);
    sectionBox.appendChild(addBtn);

    return sectionBox;
}


// ✅ 포트폴리오 섹션을 동적으로 렌더링하는 함수 (날짜·상태·파일경로 포함 + 드롭 대응)
function renderPortfolioSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";
    sectionBox.dataset.coSectionId = section.id;

    // 🔹 섹션 제목
    const sectionTitle = document.createElement("div");
    sectionTitle.className = "section-title";
    sectionTitle.innerHTML = `
        <div class="number">${number}.</div>
        <div class="title-content">
            <h3>${section.title}</h3>
            <p class="section-desc">${section.comment || "구직자 설명입력 칸 입니다."}</p>
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
    sectionBox.appendChild(addBtn);

    return sectionBox;
}




// ✅ 자기소개 섹션 렌더링 함수
function renderSelfIntroSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";

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
            <p class="section-desc">${section.comment || "구직자 설명입력 칸 입니다."}</p>
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
      <p class="section-desc">${section.comment || "구직자 설명입력 칸 입니다."}</p>
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
      <p class="section-desc">${section.comment || "구직자 설명입력 칸 입니다."}</p>
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
      <p class="section-desc">${section.comment || "구직자 설명입력 칸 입니다."}</p>
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
        <p class="section-desc">${section.comment || "구직자 설명입력 칸 입니다."}</p>
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

    document.querySelector('.empty-content').style.display = isEmpty ? 'block' : 'none';

    // ✅ 사용자 정보 동적 렌더링
    fetch("/api/user/resumes/me")
        .then(res => res.json())
        .then(user => {
            if (!user) return;

            document.getElementById("profileImage").src = user.profileImageUrl || "/images/user.png";
            document.getElementById("userName").textContent = user.userName || "이름 없음";
            document.getElementById("mainLanguage").textContent = user.mainLanguage || "";
            document.getElementById("sex").textContent = user.sex || "";
            document.getElementById("birthday").textContent = user.birthday || "";
            document.getElementById("phone").textContent = user.userPhone || "";
            document.getElementById("email").textContent = user.userEmail || "";
            document.getElementById("region").textContent = user.region || "";

        })
        .catch(err => console.error("내 정보 불러오기 실패:", err));

    // ✅ 사용자 완료 프로젝트 목록 불러오기 (오른쪽 경력 및 포트폴리오 탭에 표시)
    fetch("/api/user/resumes/projects")

        .then(res => res.json())
        .then(projects => {
            const container = document.querySelector(".tab-content[data-content='portfolio']"); // ✅ portfolio 탭으로 수정
            container.innerHTML = ""; // ✅ 기존 하드코딩된 우따따 프로젝트 삭제
            if (!projects || projects.length === 0) return;

            // 수정된 코드 - 기존 "우따따 만들기 프로젝트"와 동일한 마크업
            projects.forEach(project => {
                const div = document.createElement("div");
                div.className = "award-item";
                div.innerHTML = `
        ${project.title}<br>
        <small>${project.submittedDate}</small>
    `;

                // ✅ 여기 추가: 드래그된 항목에 필요한 데이터 속성 심기
                div.dataset.id = project.id;
                div.dataset.type = "PROJECT"; // 고정값이지만 명시
                div.dataset.file = project.filePath || "";

                // ✅ 드래그 시 JSON 형태로 전체 정보 담아서 전송
                div.setAttribute('draggable', true);
                div.addEventListener('dragstart', e => {
                    const dragData = JSON.stringify({
                        id: project.id,
                        type: "PROJECT",
                        file: project.filePath?.replace(/^\/?download\//, ""),
                        title: project.title,
                        startDate: project.startDate || "",
                        endDate: project.endDate || ""
                    });

                    console.log("📦 dragData filePath:", project.filePath); // ✅ 추가
                    console.log("📦 전체 dragData:", dragData);              // ✅ 추가
                    e.dataTransfer.setData("application/json", dragData);
                });



                container.appendChild(div);
            });
        })
        .catch(err => console.error("프로젝트 불러오기 실패:", err));


    // ✅ 구직 내역 불러오기 (오른쪽 award 탭에 출력)
    fetch("/api/job-history")
        .then(res => res.json())
        .then(histories => {
            console.log("🔥 받아온 구직 이력:", histories);  // 이거 추가!
            const container = document.querySelector(".tab-content[data-content='job']");
            container.innerHTML = "";



            if (!histories || histories.length === 0) return;

            histories.forEach(item => {
                const div = document.createElement("div");
                div.className = "award-item";

                const start = item.startDate?.replace(/-/g, ".") || "";
                const end = item.endDate?.replace(/-/g, ".") || "";

                let periodText = "";
                if (item.status === "재직") {
                    periodText = `재직: ${start} ~`;
                } else {
                    periodText = `퇴직: ${start} ~ ${end}`;
                }

                div.innerHTML = `
    ${item.workplace || item.companyName || item.company || "회사명 없음"}<br>
    <small>${item.jobTitle ? item.jobTitle + " · " : ""}${periodText}</small>
`;



                // 🔧 드래그 속성 추가
                div.setAttribute("draggable", true);

                // 🔧 드래그 시작 시 데이터 설정
                div.addEventListener("dragstart", e => {
                    const dragData = JSON.stringify({
                        id: item.id,
                        type: "JOB", // 드래그 타입 구분
                        title: item.jobTitle || "직무 없음",
                        companyName: item.workplace || item.companyName || item.company || "", // ✅ 추가
                        startDate: item.startDate,
                        endDate: item.endDate,
                        status: item.status
                    });

                    e.dataTransfer.setData("application/json", dragData);
                });

                container.appendChild(div);
            });
        })
        .catch(err => console.error("구직 내역 불러오기 실패:", err));

    // ✅ 학력사항 목록 불러오기
    fetch("/api/education-history/list")
        .then(res => res.json())
        .then(educations => {
            const container = document.querySelector(`.tab-content[data-content='school']`);
            container.innerHTML = "";

            educations.forEach(edu => {
                const div = document.createElement("div");
                div.className = "award-item";

                // 날짜 포맷 (yyyy-mm-dd → yyyy.mm.dd)
                const format = (date) => date?.replace(/-/g, ".");

                // 상태에 따라 출력 문장 분기
                let line2 = "";
                if (edu.status === "재학") {
                    line2 = `재학 ${format(edu.startDate)} 학과 ${edu.majorName || ""}`;
                } else if (edu.status === "졸업") {
                    line2 = `졸업 ${format(edu.startDate)} ~ ${format(edu.endDate)} 학과 ${edu.majorName || ""}`;
                } else {
                    line2 = `${edu.status || ""} 학과 ${edu.majorName || ""}`;
                }

                // HTML 구성
                div.innerHTML = `${edu.schoolName}<br><small>${line2}</small>`;

                // ✅ 드래그 가능하게 설정
                div.setAttribute("draggable", true);

                // ✅ dragstart 이벤트로 학력 데이터를 JSON으로 설정
                div.addEventListener("dragstart", e => {
                    const dragData = JSON.stringify({
                        type: "EDUCATION",   // 학력 데이터임을 구분
                        schoolName: edu.schoolName,
                        majorName: edu.majorName,
                        status: edu.status,
                        startDate: edu.startDate,
                        endDate: edu.endDate
                    });
                    console.log("🎒 드래그 데이터:", dragData);
                    e.dataTransfer.setData("application/json", dragData);
                });

                container.appendChild(div);
            });
        })
        .catch(err => console.error("학력 불러오기 실패:", err));




    // ✅ 📌 여기 공모전 fetch 넣기 – 프로젝트 fetch 밖으로!
    fetch("/api/user/resumes/contests")
        .then(res => res.json())
        .then(contests => {
            const container = document.querySelector(".tab-content[data-content='22']");
            container.innerHTML = "";
            contests.forEach(contest => {
                const div = document.createElement("div");
                div.className = "award-item";
                div.textContent = `${contest.title}\n${contest.date}`;
                div.dataset.id = contest.id;
                div.dataset.type = "CONTEST";
                div.dataset.file = contest.filePath || "";

                div.setAttribute("draggable", true);
                div.addEventListener("dragstart", e => {
                    const data = JSON.stringify({
                        id: contest.id,
                        type: "CONTEST",
                        file: contest.filePath,
                        title: contest.title
                    });
                    e.dataTransfer.setData("application/json", data);
                });

                container.appendChild(div);
            });
        })
        .catch(err => console.error("공모전 불러오기 실패:", err));

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
                            if (filePathInput) filePathInput.value = json.file || json.filePath || json.fileUrl || "";
                            if (hiddenFileInput) hiddenFileInput.value = json.file || json.filePath || json.fileUrl || "";

                            if (yearInputs.length >= 2) {
                                yearInputs[0].value = startY || "";
                                yearInputs[1].value = endY || "";
                            }
                            if (monthInputs.length >= 2) {
                                monthInputs[0].value = startM || "";
                                monthInputs[1].value = endM || "";
                            }

                            console.log("✅ [PROJECT] 포트폴리오 정보 자동입력 완료!");
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

        const textarea = box.querySelector("textarea");
        const content = textarea ? textarea.value.trim() : "";

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

