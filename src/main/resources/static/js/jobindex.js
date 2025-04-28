document.addEventListener("DOMContentLoaded", function () {
    // 공고 ID 가져오기
    const jobId = new URLSearchParams(window.location.search).get("id");

    // jobId 값 확인
    console.log('Job ID:', jobId);  // jobId가 올바르게 파싱되고 있는지 확인

    if (!jobId) {
        console.error("공고 ID가 없습니다.");
        return;
    }

    // 서버에서 상세 공고 데이터를 가져옵니다
    fetch(`/api/jobposts/${jobId}`)
        .then(res => {
            console.log('API 호출 상태:', res.status); // 응답 상태 코드 출력

            if (!res.ok) {  // 응답이 정상적이지 않으면
                console.error('API 호출 실패');
                return;
            }

            return res.json();  // 정상적인 응답이 오면 JSON 파싱
        })
        .then(data => {
            console.log('API 응답 데이터:', data);  // 응답 데이터 출력

            // 공고 제목 업데이트
            const jobTitleElement = document.querySelector('.job-header h2');
            if (jobTitleElement && data.title) {
                jobTitleElement.textContent = data.title; // 공고 제목 설정
            }

            // 회사 소개글 업데이트
            const jobDescriptionElement = document.querySelector('.job-desc');
            if (jobDescriptionElement && data.companyIntro) {
                jobDescriptionElement.textContent = data.companyIntro; // 회사 소개글 설정
            }

            // 지원자격 및 근무 조건 동적으로 추가
            const conditionList = document.getElementById("condition-list");
            if (!conditionList) return;  // null 체크
            conditionList.innerHTML = '';  // 기존 내용 초기화

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
                console.log(`조건 항목 추가: ${condition.label} - ${condition.value}`);  // 조건 항목 로그
            });

            // 기업 연락처 정보 동적으로 추가
            const contactInfo = document.querySelector(".contact-info");
            if (contactInfo) {
                contactInfo.innerHTML = ` 
                    <p><img src="/images/email.png"> ${data.email}</p>
                    <p><img src="/images/phone.png"> ${data.phone}</p>
                `;
                console.log('기업 연락처 추가:', data.email, data.phone);  // 기업 연락처 로그
            }

            // 이력서 양식 제목 동적으로 추가
            const resumeListContainer = document.getElementById("resume-list");
            if (resumeListContainer) {
                resumeListContainer.innerHTML = ''; // 기존 내용 초기화
                if (data.resumeTitles && data.resumeTitles.length > 0) {
                    data.resumeTitles.forEach(title => {
                        const resumeItem = document.createElement("div");
                        resumeItem.className = "resume-item";  // 이력서 양식 제목을 위한 div
                        resumeItem.textContent = `📝 ${title}`;  // 이력서 제목을 표시

                        // 제목 클릭 시 모달을 열도록 이벤트 추가
                        resumeItem.addEventListener("click", function () {
                            openResumeModal(title);  // 모달 열기
                        });

                        resumeListContainer.appendChild(resumeItem);  // 이력서 제목을 리스트에 추가
                        console.log(`이력서 양식 추가: ${title}`);  // 이력서 양식 제목 추가 로그
                    });
                } else {
                    resumeListContainer.innerHTML = '<p>등록된 이력서 양식이 없습니다.</p>';
                    console.log('등록된 이력서 양식이 없습니다.');
                }
            }

        })
        .catch(err => {
            console.error('API 호출 오류:', err);  // API 호출이 실패했을 때 오류 메시지 출력
        });

    // 이력서 모달 열기
    function openResumeModal(title) {
        const modal = document.getElementById('resumeModal');
        const modalTitle = document.getElementById('modal-title');
        console.log('모달 열기 시도:', title); // 모달 열기 시도 확인
        if (modal && modalTitle) {
            modalTitle.textContent = title;  // 이력서 양식 제목을 modal-title에 설정
            modal.style.display = 'flex';  // 모달을 화면에 띄움
            console.log(`모달 열기 - 이력서 제목: ${title}`);  // 모달 열기 로그
        } else {
            console.log('모달이 존재하지 않음'); // 모달이 존재하지 않으면 로그 출력
        }
    }

    // 이력서 모달 닫기
    function closeResumeModal() {
        const modal = document.getElementById('resumeModal');
        console.log('모달 닫기 시도'); // 모달 닫기 시도 로그
        if (modal) {
            modal.style.display = 'none';  // 모달을 숨김
            console.log('모달 닫기');  // 모달 닫기 로그
        } else {
            console.log('모달이 존재하지 않음'); // 모달이 존재하지 않으면 로그 출력
        }
    }

    // 이력서 제목을 클릭하면 모달이 열리도록 설정
    const resumeItems = document.querySelectorAll('.resume-item'); // 이력서 제목 div들
    resumeItems.forEach(item => {
        item.addEventListener("click", function () {
            const title = item.textContent;  // 제목을 받아와서
            openResumeModal(title);  // 모달 열기
        });
    });

    // 모달 닫기 버튼 클릭 시
    const closeBtn = document.querySelector(".modal-buttons button:last-child");
    if (closeBtn) {
        closeBtn.addEventListener("click", function () {
            closeResumeModal();  // 모달 닫기
        });
    } else {
        console.log('모달 닫기 버튼이 없습니다.');
    }

    // 예 버튼 클릭 시 이력서 작성 페이지로 이동
    const yesBtn = document.querySelector(".modal-buttons button:first-child");
    if (yesBtn) {
        yesBtn.addEventListener("click", function () {
            console.log('이력서 작성 페이지로 이동');
            window.location.href = "/resume/write"; // 예: 이력서 작성 페이지로 이동
        });
    } else {
        console.log('예 버튼이 없습니다.');
    }

});
