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

// ✅ 5. 추가 버튼 클릭 시 복제
const addBtn = document.querySelector(".edu-btn"); // 버튼 클래스
const list = document.getElementById("education-list");
const firstItem = list.querySelector(".education-item");

addBtn.addEventListener("click", () => {
    const clone = firstItem.cloneNode(true);

    // input/select 초기화
    clone.querySelectorAll("input, select").forEach(el => {
        el.value = "";
    });

    // select 초기화
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

    // X 버튼 연결
    const deleteBtn = clone.querySelector(".del-btn");
    addDeleteFunction(deleteBtn);

    // 상태 이벤트 연결
    setupStatusListener(clone);

    // 간격 추가
    clone.style.marginTop = "10px";

    list.appendChild(clone);
});

// ✅ 6. 초기 항목 세팅
createYearOptions(firstItem.querySelector(".start-year"));
createMonthOptions(firstItem.querySelector(".start-month"));
createYearOptions(firstItem.querySelector(".end-year"));
createMonthOptions(firstItem.querySelector(".end-month"));
setupStatusListener(firstItem);
const deleteBtn = firstItem.querySelector(".del-btn");
addDeleteFunction(deleteBtn);

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

// "네" 클릭 → 제출 처리
confirmBtn.addEventListener("click", () => {
    modal.style.display = "none";
    alert("제출되었습니다!");
    // 실제 폼 제출이 필요하면 여기에 submit 처리 추가
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

// ✅ 학력사항 섹션을 동적으로 렌더링하는 함수
function renderEducationSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";

    const conditionText = [];

// ✅ 학력사항일 경우엔 multiSelect 조건은 제목에 안 넣고, 오직 조건만 넣기
    if (section.title !== '학력사항') {
        if (section.multiSelect) conditionText.push("복수선택 가능");
        if (!section.multiSelect && section.type === "선택형") conditionText.push("단일선택");
    }

// 공통: 조건은 항상 포함
    conditionText.push(...section.conditions);

// 조건이 있을 경우에만 괄호 붙이기
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

    const eduList = document.createElement("div");
    eduList.id = "education-list";

    const formGroup = document.createElement("div");
    formGroup.className = "form-group education-item";
    formGroup.innerHTML = `
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
    `;

    createYearOptions(formGroup.querySelector(".start-year"));
    createMonthOptions(formGroup.querySelector(".start-month"));
    createYearOptions(formGroup.querySelector(".end-year"));
    createMonthOptions(formGroup.querySelector(".end-month"));
    setupStatusListener(formGroup);
    addDeleteFunction(formGroup.querySelector(".del-btn"));

    eduList.appendChild(formGroup);

    const addBtn = document.createElement("button");
    addBtn.className = "edu-btn";
    addBtn.innerHTML = `<span class="plus">＋</span> 추가하기`;
    addBtn.addEventListener("click", () => {
        const clone = formGroup.cloneNode(true);
        clone.querySelectorAll("input, select").forEach(el => el.value = "");
        createYearOptions(clone.querySelector(".start-year"));
        createMonthOptions(clone.querySelector(".start-month"));
        createYearOptions(clone.querySelector(".end-year"));
        createMonthOptions(clone.querySelector(".end-month"));
        setupStatusListener(clone);
        addDeleteFunction(clone.querySelector(".del-btn"));
        clone.style.marginTop = "10px";
        eduList.appendChild(clone);
    });

    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(eduList);
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
    return sectionBox;
}

// ✅ 경력사항 섹션을 동적으로 렌더링하는 함수
function renderCareerSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";

    // multiSelect는 경력사항엔 표시 안 함
    const conditionText = [...section.conditions];
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

    const textarea = document.createElement("textarea");
    textarea.placeholder = "경력 입력";

    const uploadBox = document.createElement("div");
    uploadBox.className = "upload-box";
    uploadBox.textContent = "드래그해서 파일 첨부하기";

    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(textarea);
    sectionBox.appendChild(uploadBox);

    return sectionBox;
}

// ✅ 포트폴리오 섹션을 동적으로 렌더링하는 함수
function renderPortfolioSection(section, number) {
    const sectionBox = document.createElement("section");
    sectionBox.className = "section-box";

    const conditionText = [...section.conditions]; // 복수선택 제외
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

    const textarea = document.createElement("textarea");
    textarea.placeholder = "설명 입력";

    const uploadBox = document.createElement("div");
    uploadBox.className = "upload-box";
    uploadBox.textContent = "드래그해서 파일 첨부하기";

    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(textarea);
    sectionBox.appendChild(uploadBox);

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

    // "사진 선택" 버튼 역할을 하는 label
    const label = document.createElement("label");
    label.setAttribute("for", `photoUpload${number}`);
    label.className = "file-label";
    label.textContent = "사진 선택";

    // 실제 파일 input (숨김 처리됨)
    const input = document.createElement("input");
    input.type = "file";
    input.id = `photoUpload${number}`;
    input.accept = "image/*";  // 이미지 파일만 선택 가능
    input.style.display = "none";

    // 선택된 파일명을 표시하는 영역
    const fileNameSpan = document.createElement("span");
    fileNameSpan.className = "file-name";
    fileNameSpan.textContent = "선택된 파일 없음";

    // 파일 선택 시 파일명 표시
    input.addEventListener("change", () => {
        const file = input.files[0];
        fileNameSpan.textContent = file ? file.name : "선택된 파일 없음";
    });

    // 업로드 UI 조립
    wrapper.appendChild(label);
    wrapper.appendChild(input);
    wrapper.appendChild(fileNameSpan);

    // 전체 섹션 조립
    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(textarea);
    sectionBox.appendChild(wrapper);

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

    // ✅ 파일 업로드 UI
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

    // ✅ 파일 선택 시 파일명 표시
    fileInput.addEventListener("change", () => {
        fileName.textContent = fileInput.files.length > 0 ? fileInput.files[0].name : "선택된 파일 없음";
    });

    label.appendChild(fileInput);
    uploadWrapper.appendChild(label);
    uploadWrapper.appendChild(fileName);

    // 조립
    sectionBox.appendChild(sectionTitle);
    sectionBox.appendChild(textarea);
    sectionBox.appendChild(uploadWrapper);

    return sectionBox;
}





// ✅ 페이지 로드시 수상 탭이 비어있으면 empty-content 보이게 하기
window.addEventListener('DOMContentLoaded', () => {
    const defaultTab = document.querySelector('.tab.active');
    if (!defaultTab) return;

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
            document.getElementById("bio").textContent = user.bio || "";
        })
        .catch(err => console.error("내 정보 불러오기 실패:", err));



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
                    const leftContent = document.querySelector('.left-content');
                    const submitWrapper = document.querySelector('.submit-wrapper');
                    leftContent.insertBefore(rendered, submitWrapper);
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
