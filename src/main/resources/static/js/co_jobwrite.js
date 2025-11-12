// ✅ 페이지 로드 시 기본 입력값 미리 채워두기
document.querySelector(".title-input").value = "[신입] 프론트엔드 개발자 채용";
document.querySelector("textarea[name='companyIntro']").value =
    "(주)넥스트코드는 혁신적인 웹 서비스와 모바일 솔루션을 개발하는 IT 스타트업입니다.\n" +
    "사용자 중심의 인터페이스와 안정적인 백엔드 시스템을 통해 더 나은 디지털 경험을 만들어가고 있습니다.";

document.querySelector("input[name='email']").value = "recruit@nextcode.co.kr";
document.querySelector("input[name='phone']").value = "02-3456-7890";
document.querySelector("input[name='companyLink']").value = "https://www.nextcode.co.kr";
document.querySelector("input[name='career']").value = "신입 또는 1년 이하 경력";
document.querySelector("input[name='education']").value = "학사 이상 (전공 무관, 컴퓨터공학 우대)";
document.querySelector("input[name='preference']").value =
    "React 또는 Vue 사용 경험, REST API 연동 프로젝트 경험, Git 협업 경험";

// ✅ 고용형태(정규직) 체크박스 자동 선택
document.querySelectorAll("input[name='employmentType']").forEach(cb => {
    if (cb.value === "정규직") cb.checked = true;
});

document.querySelector("input[name='salary']").value = "연봉 3,200만 원 이상 (경력에 따라 협의)";
document.querySelector("input[name='time']").value = "09:00 ~ 18:00 (주5일제)";
document.querySelector("input[name='surew']").value = "이력서, 포트폴리오, 자기소개서";

// ✅ 모집기간 자동 설정 (오늘 ~ 일주일 후)
const today = new Date();
const weekLater = new Date();
weekLater.setDate(today.getDate() + 7);
document.querySelector("#startDate").value = today.toISOString().slice(0, 10);
document.querySelector("#endDate").value = weekLater.toISOString().slice(0, 10);


document.addEventListener("DOMContentLoaded", function () {
    const resumeButtons = document.querySelectorAll(".resume-tab");
    const modal = document.getElementById("resume-modal");
    const modalTitle = document.getElementById("modal-title");
    const modalBody = document.getElementById("modal-body");
    const modalClose = document.getElementById("modal-close");
    const addButton = document.getElementById("addResumeBtn");
    const resumeOutput = document.getElementById("resume-output");
    const addResumeText = document.querySelector(".add-resume-text");

    // [추가] 지원 방식 라디오 & 회사양식 박스
    const applyTypeRadios = document.querySelectorAll('input[name="applyType"]'); // 라디오
    const companyBox = document.getElementById("company-form-box");               // 회사 양식 박스

    // [추가] 현재 선택된 지원 방식
    function getApplyType() {
        return document.querySelector('input[name="applyType"]:checked')?.value || 'company';
    }

    // [추가] 지원 방식에 따라 UI 토글
    function syncApplyTypeUI() {
        const type = getApplyType();
        if (type === 'company') {
            companyBox?.classList.remove('hidden');
        } else {
            companyBox?.classList.add('hidden');
        }
    }

    // [추가] 라디오 변경 이벤트
    applyTypeRadios.forEach(r => r.addEventListener('change', syncApplyTypeUI));
    syncApplyTypeUI();

    fetch('/api/coresumes')
        .then(res => res.json())
        .then(data => {
            const container = document.getElementById("savedResumeList");
            container.innerHTML = ''; // 기존 내용 비우기

            data.forEach(resume => {
                const div = document.createElement("div");
                div.className = "resume-tab";
                div.setAttribute("draggable", "true");
                div.dataset.id = resume.id;
                div.textContent = resume.title;

                container.appendChild(div);
            });
        })
        .catch(error => {
            console.error("이력서 불러오기 실패:", error);
        });

    // 모달 열기 & 드래그 등록
    document.getElementById("savedResumeList").addEventListener("click", (e) => {
        if (e.target.classList.contains("resume-tab")) {
            const resumeId = e.target.dataset.id;

            fetch(`/api/coresumes/${resumeId}`)
                .then(res => res.json())
                .then(data => {
                    const modal = document.getElementById("resume-modal");
                    const modalTitle = document.getElementById("modal-title");
                    const modalBody = document.getElementById("modal-body");

                    modalTitle.textContent = data.title;

                    let html = `<div class="resume-preview-wrapper">`;

                    data.sections.forEach((section, index) => {
                        html += `
      <section class="resume-section preview-mode">
        <div class="section-header">
          <span>${index + 1}. ${section.title}</span>
        </div>

        ${section.comment ? `<p class="section-note">${section.comment}</p>` : ''}

        ${section.conditions?.length ? `
          <div class="tag-list">
            ${section.conditions.map(cond => `<span class="tag condition">${cond}</span>`).join("")}
          </div>
        ` : ''}

        ${section.tags?.length ? `
          <div class="tag-list">
            ${section.tags.map(tag => `<span class="tag">${tag}</span>`).join("")}
          </div>
        ` : ''}

        ${section.content ? `
          <div class="section-content">
            <textarea readonly class="preview-textarea">${section.content}</textarea>
          </div>
        ` : ''}
      </section>
    `;
                    });

                    html += `</div>`;
                    modalBody.innerHTML = html;
                    modal.style.display = "flex";
                });
        }
    });

    // 드래그 이벤트도 여기에 추가
    document.getElementById("savedResumeList").addEventListener("dragstart", (e) => {
        if (e.target.classList.contains("resume-tab")) {
            const title = e.target.textContent;
            e.dataTransfer.setData("text/plain", title);
        }
    });

    modalClose.addEventListener("click", () => {
        modal.style.display = "none";
    });

    // 이력서 추가 텍스트 클릭 이벤트
    // addResumeText.addEventListener("click", () => {
    //     alert("이력서 추가 기능은 추후 구현 예정입니다.");
    // });

    // 드롭 처리 (회사 양식일 때만 의미 있음)
    addButton.addEventListener("dragover", (e) => {
        e.preventDefault();
        addButton.style.borderColor = "green";
    });

    addButton.addEventListener("dragleave", () => {
        addButton.style.borderColor = "#aaa";
    });

    addButton.addEventListener("drop", (e) => {
        e.preventDefault();
        addButton.style.borderColor = "#aaa";

        const title = e.dataTransfer.getData("text/plain");

        if (title) {
            // 이미 같은 템플릿이 추가되었는지 확인
            const existing = [...resumeOutput.querySelectorAll(".resume-item span")];
            const isDuplicate = existing.some(span => span.textContent === title);

            if (isDuplicate) {
                alert(`'${title}' 템플릿은 이미 추가되었습니다.`);
                return;
            }

            // 새 항목 추가
            const li = document.createElement("li");
            li.className = "resume-item";
            li.innerHTML = `
      <span>${title}</span>
      <button class="delete-btn" title="삭제">🗑️</button>
    `;
            resumeOutput.appendChild(li);
        }
    });

    // 이벤트 위임 방식으로 삭제 처리
    resumeOutput.addEventListener("click", function (e) {
        if (e.target.classList.contains("delete-btn")) {
            const li = e.target.closest("li");
            if (li) li.remove();
        }
    });

    // ✅ 작성 버튼 클릭 시 구인글 저장
    document.querySelector(".submit-btn").addEventListener("click", function (e) {
        e.preventDefault();

        // [추가] 현재 선택된 지원 방식
        const applyType = getApplyType(); // 'company' | 'member'

        // 1. 기본 입력값 수집
        const data = {
            title: document.querySelector(".title-input").value,
            companyIntro: document.querySelector("textarea[name='companyIntro']").value,
            email: document.querySelector("input[name='email']").value,
            phone: document.querySelector("input[name='phone']").value,
            companyLink: document.querySelector("input[name='companyLink']").value,
            career: document.querySelector("input[name='career']").value,
            education: document.querySelector("input[name='education']").value,
            preference: document.querySelector("input[name='preference']").value,
            salary: document.querySelector("input[name='salary']").value,
            time: document.querySelector("input[name='time']").value,
            startDate: document.querySelector("#startDate").value,
            endDate: document.querySelector("#endDate").value,
            surew: document.querySelector("input[name='surew']").value,
            employmentTypes: [],
            // [변경] 서버가 분기할 수 있도록 applyType을 포함
            applyType: applyType,
            resumeIds: [] // 회사 양식일 때만 채움
        };

        // 2. 고용형태 체크박스
        document.querySelectorAll("input[name='employmentType']:checked").forEach(cb => {
            data.employmentTypes.push(cb.value);
        });

        // 3. 회사 양식 선택일 때만 이력서 템플릿 수집
        if (applyType === 'company') {
            const resumeItems = document.querySelectorAll("#resume-output .resume-item span");
            const savedResumeMap = {}; // 텍스트 → ID 매핑
            document.querySelectorAll("#savedResumeList .resume-tab").forEach(tab => {
                savedResumeMap[tab.textContent] = tab.dataset.id;
            });
            resumeItems.forEach(span => {
                const id = savedResumeMap[span.textContent];
                if (id) data.resumeIds.push(Number(id));
            });

            // (선택) 유효성 검사: 회사 양식인데 아무 것도 안 골랐을 때 막기
            // if (data.resumeIds.length === 0) {
            //     alert('회사 양식으로 받기 선택 시, 최소 1개의 이력서 양식을 추가해 주세요.');
            //     return;
            // }
        } else {
            // member 모드: 이 화면에서는 템플릿을 받지 않음(지원 시 회원이 자기 이력서를 제출)
            data.resumeIds = [];
        }

        // 4. 서버 전송 (POST)
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        fetch("/api/cojobs", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(data)
        })
            .then(res => {
                if (!res.ok) throw new Error("서버 오류 발생");
                return res.text();  // 문자열로 받아야 함
            })
            .then(jobId => {
                alert("공고가 성공적으로 등록되었습니다.");
                window.location.href = `/cojobdetail?id=${jobId}`;
            })
            .catch(err => {
                console.error(err);
                alert("저장 중 오류 발생!");
            });
    });
});
