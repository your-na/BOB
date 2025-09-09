document.addEventListener("DOMContentLoaded", function () {
    // 공고 ID 가져오기
    const jobId = new URLSearchParams(window.location.search).get("id");
    console.log('Job ID:', jobId);

    if (!jobId) {
        console.error("공고 ID가 없습니다.");
        return;
    }

    // 서버에서 상세 공고 데이터를 가져옵니다
    fetch(`/api/cojobs/${jobId}/with-resumes`)
        .then(res => {
            console.log('API 호출 상태:', res.status);
            if (!res.ok) {
                console.error('API 호출 실패');
                return;
            }
            return res.json();
        })
        .then(data => {
            console.log('API 응답 데이터:', data);

            // ✅ 이력서 패널 및 힌트 영역 처리
            const resumeHint = document.querySelector(".resume-hint");
            const resumePanel = document.getElementById("my-resume-panel");
            const dropZone = document.getElementById("drop-zone");

            if (data.applyType === "company" && data.resumeTitles && data.resumeTitles.length > 0) {
                // 기업이 이력서 양식을 제공했으면 → 회원 이력서 숨김
                if (resumeHint) resumeHint.style.display = "none";
                if (resumePanel) resumePanel.classList.add("hidden");
                if (dropZone) dropZone.classList.add("hidden");
            } else {
                // 기업 이력서 양식이 없으면 → 회원 이력서 사용 가능
                if (resumeHint) resumeHint.style.display = "block";
                if (resumePanel) resumePanel.classList.remove("hidden");
                if (dropZone) dropZone.classList.remove("hidden");
            }





            // 공고 제목 업데이트
            const jobTitleElement = document.querySelector('.job-header h2');
            if (jobTitleElement && data.title) {
                jobTitleElement.textContent = data.title;
            }

            // 회사 소개글 업데이트
            const jobDescriptionElement = document.querySelector('.job-desc');
            if (jobDescriptionElement && data.companyIntro) {
                jobDescriptionElement.textContent = data.companyIntro;
            }

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
                    li.innerHTML = `<strong>${condition.label}:</strong> ${condition.value}`;
                    conditionList.appendChild(li);
                    console.log(`조건 항목 추가: ${condition.label} - ${condition.value}`);
                });
            }

            // 기업 연락처 추가
            const contactInfo = document.querySelector(".contact-info");
            if (contactInfo) {
                contactInfo.innerHTML = `
                    <p><img src="/images/email.png"> ${data.email}</p>
                    <p><img src="/images/phone.png"> ${data.phone}</p>
                `;
                console.log('기업 연락처 추가:', data.email, data.phone);
            }

            // 이력서 양식 목록 추가
            const resumeListContainer = document.getElementById("resume-list");
            if (resumeListContainer) {
                resumeListContainer.innerHTML = '';

                if (data.resumeTitles && data.resumeTitles.length > 0) {
                    data.resumeTitles.forEach(resume => {
                        const resumeItem = document.createElement("div");
                        resumeItem.className = "resume-item";
                        resumeItem.textContent = `📝 ${resume.title}`;
                        resumeItem.addEventListener("click", function () {
                            openResumeModal(resume.title, resume.id);
                        });
                        resumeListContainer.appendChild(resumeItem);
                        console.log(`이력서 양식 추가: ${resume.title} (ID: ${resume.id})`);
                    });
                } else {
                    resumeListContainer.innerHTML = '<p>등록된 이력서 양식이 없습니다.</p>';
                    console.log('등록된 이력서 양식이 없습니다.');
                }
            }
            // ✅ 여기 아래에 제출서류 표시 추가
            const documentBlocks = document.querySelectorAll(".job-info-block");
            documentBlocks.forEach(block => {
                const h3 = block.querySelector("h3");
                if (h3 && h3.textContent.includes("제출서류")) {
                    block.innerHTML = `
            <h3>제출서류</h3>
            <p>${data.surew || '제출 서류 정보 없음'}</p>
            <p class="notice-red">* 필수 제출 안내 확인</p>
        `;
                    console.log('제출서류 출력 완료:', data.surew);
                }
            });
        })
        .catch(err => {
            console.error('API 호출 오류:', err);
        });

    // 모달 열기
    function openResumeModal(title, resumeId) {
        const modal = document.getElementById('resumeModal');
        const modalTitle = document.getElementById('modal-title');
        const confirmBtn = document.getElementById("confirm-btn"); // id로 수정
        const jobPostId = new URLSearchParams(window.location.search).get("id");

        if (modal && modalTitle && confirmBtn) {
            modalTitle.textContent = title;
            modal.style.display = 'flex';
            confirmBtn.dataset.resumeId = resumeId;
            confirmBtn.dataset.jobPostId = jobPostId;
        }
    }


    // 모달 닫기
    function closeResumeModal() {
        const modal = document.getElementById('resumeModal');
        if (modal) {
            modal.style.display = 'none';
            console.log('모달 닫기');
        }
    }

    // 모달 닫기 버튼
    const cancelBtn = document.getElementById("cancel-btn");
    if (cancelBtn) {
        cancelBtn.addEventListener("click", function () {
            const modal = document.getElementById('resumeModal');
            if (modal) {
                modal.style.display = 'none';
                console.log('모달 닫기 - 아니오 버튼');
            }
        });
    }


    // 예 버튼 클릭 시 이력서 작성 페이지 이동
    const confirmBtn = document.getElementById("confirm-btn");
    if (confirmBtn) {
        confirmBtn.addEventListener("click", function () {
            const resumeId = this.dataset.resumeId;
            const jobPostId = this.dataset.jobPostId;
            if (resumeId && jobPostId) {
                window.location.href = `/resume/write?id=${resumeId}&jobPostId=${jobPostId}`;
            } else {
                console.error("이동할 수 없습니다. resumeId 또는 jobPostId가 없습니다.");
            }
        });
    }
});

document.addEventListener('DOMContentLoaded', () => {
    const listEl = document.getElementById('myResumeList');
    let selectedResumeId = null; // 현재 선택된 하나

    // ①회원 이력서 목록 로드
    fetch('/api/myresumes')
        .then(r => r.json())
        .then(items => {
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
        const id = btn.dataset.id;

        if (selectedResumeId === id) {
            // 이미 선택된 것을 다시 클릭 → 해제
            btn.classList.remove('selected');
            selectedResumeId = null;
            return;
        }

        // 기존 선택 제거
        const prev = listEl.querySelector('.resume-tab.selected');
        if (prev) prev.classList.remove('selected');

        // 새 선택 적용
        btn.classList.add('selected');
        selectedResumeId = id;
    }

    // ④ “지원하기” 버튼에서 선택 값 사용
    document.querySelector('.apply-btn')?.addEventListener('click', () => {
        // 필요 시 선택 검증
        if (!selectedResumeId) {
            alert('제출할 이력서를 선택해 주세요.');
            return;
        }

        // 예시: 공고 ID는 서버에서 데이터-속성이나 전역 변수로 내려주세요.
        const jobId = window.JOB_ID || new URLSearchParams(location.search).get('id');

        fetch('/api/apply', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                jobId,
                resumeId: Number(selectedResumeId) // 하나만 보냄
            })
        })
            .then(r => {
                if (!r.ok) throw new Error('지원 실패');
                return r.text();
            })
            .then(msg => {
                alert('지원이 완료되었습니다.');
                // 필요 시 이동
                // location.href = `/apply/complete?jobId=${jobId}`;
            })
            .catch(err => {
                console.error(err);
                alert('지원 중 오류가 발생했습니다.');
            });
    });
});
