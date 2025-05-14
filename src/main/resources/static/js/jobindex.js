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
