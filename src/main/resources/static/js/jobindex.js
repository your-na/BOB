let selectedResumeTitle = null;  // 전역 선언 (모든 함수에서 접근 가능)

document.addEventListener("DOMContentLoaded", function () {
    // 공고 ID 가져오기
    const jobId = new URLSearchParams(window.location.search).get("id");
    if (!jobId) {
        console.error("공고 ID가 없습니다.");
        return;
    }

    // 서버에서 상세 공고 데이터를 가져옵니다
    fetch(`/api/cojobs/${jobId}/with-resumes`)
        .then(res => res.ok ? res.json() : Promise.reject('API 호출 실패'))
        .then(data => {
            console.log('API 응답 데이터:', data);

            // ✅ 이력서 블록/패널 제어
            const resumeBlock = document.querySelector("#resume-list")?.parentElement;
            const resumePanel = document.getElementById("my-resume-panel");

            if (data.applyType === "company") {
                if (resumeBlock) resumeBlock.style.display = "block";
                if (resumePanel) resumePanel.classList.add("hidden");
            } else if (data.applyType === "member") {
                if (resumeBlock) resumeBlock.style.display = "none";
                if (resumePanel) resumePanel.classList.remove("hidden");
            }

            // 공고 제목 업데이트
            const jobTitleElement = document.querySelector('.job-header h2');
            if (jobTitleElement && data.title) jobTitleElement.textContent = data.title;

            // 회사 소개글 업데이트
            const jobDescriptionElement = document.querySelector('.job-desc');
            if (jobDescriptionElement && data.companyIntro) jobDescriptionElement.textContent = data.companyIntro;

            // 지원자격 및 근무 조건 동적 추가
            const conditionList = document.getElementById("condition-list");
            if (conditionList) {
                conditionList.innerHTML = '';
                const conditions = [
                    { label: '경력', value: data.career },
                    { label: '학력', value: data.education },
                    { label: '고용형태', value: data.employmentTypes },
                    { label: '급여', value: data.salary },
                    { label: '시간', value: data.time },
                    { label: '우대', value: data.preference }
                ];
                conditions.forEach(condition => {
                    const li = document.createElement("li");
                    li.innerHTML = `<strong>${condition.label}:</strong> ${condition.value || '-'}`;
                    conditionList.appendChild(li);
                });
            }

            // 기업 연락처 추가
            const contactInfo = document.querySelector(".contact-info");
            if (contactInfo) {
                contactInfo.innerHTML = `
                    <p><img src="/images/email.png"> ${data.email}</p>
                    <p><img src="/images/phone.png"> ${data.phone}</p>
                `;
            }

            // 이력서 양식 목록 추가 (company 모드)
            const resumeListContainer = document.getElementById("resume-list");
            if (resumeListContainer && data.applyType === "company") {
                resumeListContainer.innerHTML = '';
                if (data.resumeTitles?.length > 0) {
                    data.resumeTitles.forEach(resume => {
                        const resumeItem = document.createElement("div");
                        resumeItem.className = "resume-item";
                        resumeItem.textContent = `📝 ${resume.title}`;
                        resumeItem.addEventListener("click", () => {
                            openResumeModal(resume.title, resume.id);
                        });
                        resumeListContainer.appendChild(resumeItem);
                    });
                } else {
                    resumeListContainer.innerHTML = '<p>등록된 이력서 양식이 없습니다.</p>';
                }
            }

            // 제출서류 표시
            const documentBlocks = document.querySelectorAll(".job-info-block");
            documentBlocks.forEach(block => {
                const h3 = block.querySelector("h3");
                if (h3 && h3.textContent.includes("제출서류")) {
                    block.innerHTML = `
                        <h3>제출서류</h3>
                        <p>${data.surew || '제출 서류 정보 없음'}</p>
                        <p class="notice-red">* 필수 제출 안내 확인</p>
                    `;
                }
            });
        })
        .catch(err => console.error('API 호출 오류:', err));

    // 모달 열기
    function openResumeModal(title, resumeId) {
        const modal = document.getElementById('resumeModal');
        const modalTitle = document.getElementById('modal-title');
        const confirmBtn = document.getElementById("confirm-btn");
        const jobPostId = new URLSearchParams(window.location.search).get("id");

        if (modal && modalTitle && confirmBtn) {
            modalTitle.textContent = title;
            modal.style.display = 'flex';
            confirmBtn.dataset.resumeTitle = title;
            confirmBtn.dataset.resumeId = resumeId; // ✅ resumeId 추가
            confirmBtn.dataset.jobPostId = jobPostId;
        }
    }



    // 모달 닫기 버튼
    document.getElementById("cancel-btn")?.addEventListener("click", () => {
        document.getElementById('resumeModal').style.display = 'none';
    });

    // 예 버튼 → 이력서 작성 페이지 이동
    document.getElementById("confirm-btn")?.addEventListener("click", function () {
        const resumeId = this.dataset.resumeId;   // ✅ resumeId 사용
        const jobPostId = this.dataset.jobPostId;
        if (resumeId && jobPostId) {
            window.location.href = `/resume/write?id=${resumeId}&jobPostId=${jobPostId}`; // 원래 이동 경로
        } else {
            console.error("이동할 수 없습니다. resumeId 또는 jobPostId가 없습니다.");
        }
    });

});

// ✅ 회원 이력서 목록 불러오기
document.addEventListener('DOMContentLoaded', () => {
    const listEl = document.getElementById('myResumeList');

    // ① 회원 이력서 목록 로드
    fetch('/api/myresumes')
        .then(r => r.json())
        .then(items => {
            console.log('이력서 목록:', items); // 🔴 여기 꼭 확인
            renderResumes(items);
        })
        .catch(err => console.error('이력서 목록 로드 실패:', err));


    // ② 목록 렌더
    function renderResumes(items) {
        listEl.innerHTML = '';
        items.forEach(item => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'resume-tab';
            btn.dataset.id = item.id;
            btn.innerHTML = `
                <div class="resume-title">${item.title}</div>
                <div class="resume-date">${item.date ?? ''}</div>
            `;
            btn.addEventListener('click', () => toggleSelect(btn));
            listEl.appendChild(btn);
        });
    }

    // ③ 단일 선택 토글
    function toggleSelect(btn) {
        const title = btn.querySelector('.resume-title').textContent; // 제목 가져오기
        if (selectedResumeTitle === title) {
            btn.classList.remove('selected');
            selectedResumeTitle = null;
            return;
        }
        const prev = listEl.querySelector('.resume-tab.selected');
        if (prev) prev.classList.remove('selected');
        btn.classList.add('selected');
        selectedResumeTitle = title; // 선택된 제목 저장

        console.log('선택된 이력서 title(selectedResumeTitle) 업데이트:', selectedResumeTitle);
    }
});

// ✅ 지원하기 버튼 클릭 + 확인 모달 이벤트
document.addEventListener("DOMContentLoaded", () => {
    // 지원하기 버튼
    document.querySelector('.apply-btn')?.addEventListener('click', () => {
        if (!selectedResumeTitle) {
            // 이력서 선택 안 했으면 경고 모달
            document.getElementById("alertModal").style.display = "flex";
            return;
        }
        // 선택된 이력서 있으면 확인 모달
        document.getElementById("confirmApplyModal").style.display = "flex";
    });

    // "아니오" 버튼 → 모달 닫기
    document.getElementById("apply-cancel-btn")?.addEventListener('click', () => {
        document.getElementById("confirmApplyModal").style.display = "none";
    });

    // "예" 버튼 → 지원 요청
    document.getElementById("apply-confirm-btn")?.addEventListener('click', () => {
        document.getElementById("confirmApplyModal").style.display = "none";

        const jobId = window.JOB_ID || new URLSearchParams(location.search).get('id');

        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

        // 🔴 여기에 로그 추가
        console.log('csrfMeta:', csrfMeta);
        console.log('csrfHeaderMeta:', csrfHeaderMeta);

        if (!csrfMeta || !csrfHeaderMeta) {
            console.error('CSRF 메타 태그가 없습니다.');
            return;
        }

        const csrfToken = csrfMeta.getAttribute('content');
        const csrfHeader = csrfHeaderMeta.getAttribute('content');

        // 🔴 여기에도 값 출력
        console.log('csrfToken:', csrfToken);
        console.log('csrfHeader:', csrfHeader);

        if (!csrfHeader || !csrfToken) {
            console.error('CSRF 토큰 또는 헤더 이름이 없습니다.');
            return;
        }

        const headers = { 'Content-Type': 'application/json' };
        if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

        // ✅ 여기에 로그 추가
        console.log('보낼 jobId:', jobId);

        console.log('fetch headers:', headers); // 🔴 실제 fetch 보내기 전 확인

        fetch('/api/applications/apply', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({
                jobId: Number(jobId),
                myResumeTitle: selectedResumeTitle // 이제 title 기준
            })
        })
            .then(r => {
                if (!r.ok) throw new Error('지원 실패');
                return r.json();
            })
            .then(data => {
                alert(data.message || '지원이 완료되었습니다.');

                // 서버에서 myResumeId를 반환하면 해당 페이지로 이동
                if (data.myResumeId) {
                    window.location.href = `/myresume/${data.myResumeId}`;
                } else if (data.resumeId) {
                    // 기업 이력서 지원일 경우
                    window.location.href = `/resume/detail?jobPostId=${jobPostId}&resumeId=${data.resumeId}`;
                } else {
                    // fallback: title 기반 상세보기
                    window.location.href = `/resume/detail?title=${encodeURIComponent(selectedResumeTitle)}`;
                }
            })
            .catch(err => {
                console.error(err);
                alert('지원 중 오류가 발생했습니다.');
            });


    });

});

