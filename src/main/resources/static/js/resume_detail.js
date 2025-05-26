document.addEventListener("DOMContentLoaded", () => {
    const deleteBtn = document.querySelector(".delete-btn");
    const jobPostId = document.getElementById("jobPostId")?.value;
    const resumeId = document.getElementById("resumeId")?.value;


    // ✅ CSRF 토큰과 헤더 이름 가져오기
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    // ❎ 불합격 처리 버튼 클릭 시
    const nonpassBtn = document.querySelector(".nonpass-btn");

    if (nonpassBtn && resumeId && jobPostId && csrfToken && csrfHeader) {
        nonpassBtn.addEventListener("click", () => {
            const confirmed = confirm("정말 이 지원자를 불합격 처리하시겠습니까?");
            if (!confirmed) return;

            // 📡 서버로 불합격 처리 요청 전송 (메시지 없이)
            fetch("/api/applications/job/reject", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({
                    resumeId,
                    jobPostId
                    // ❌ message 제거됨
                })
            })
                .then(res => res.ok ? res.json() : res.text().then(msg => { throw new Error(msg); }))
                .then(data => {
                    alert("❎ " + data.message); // 서버 응답 메시지 출력
                })
                .catch(err => {
                    console.error("❌ 에러 발생:", err);
                    alert("⚠️ 불합격 처리에 실패했습니다.\n" + err.message);
                });
        });
    }

    if (deleteBtn && jobPostId && csrfToken && csrfHeader) {
        deleteBtn.addEventListener("click", (e) => {
            e.preventDefault();

            const confirmed = confirm("정말로 지원을 취소하시겠습니까?");
            if (!confirmed) return;

            fetch(`/api/user/resumes/cancel?jobPostId=${jobPostId}`, {
                method: "DELETE",
                credentials: "include",
                headers: {
                    [csrfHeader]: csrfToken
                }
            })
                .then((res) => {
                    if (res.ok) {
                        alert("✅ 지원이 취소되었습니다.");
                        location.href = "/jobapplication";
                    } else {
                        return res.text().then((msg) => {
                            alert("❌ 취소 실패: " + msg);
                        });
                    }
                })
                .catch((err) => {
                    alert("⚠️ 서버 오류로 취소에 실패했습니다.");
                    console.error(err);
                });
        });
    }

    // ✅ 합격 처리 버튼과 모달 로직 (전체 로그 포함 버전)
    const passBtn = document.querySelector(".pass-btn");
    const passModal = document.getElementById("passModal");
    const closeModalBtn = document.querySelector(".close");
    const submitPassBtn = document.getElementById("submitPassBtn");

    if (passBtn && passModal) {
        passBtn.addEventListener("click", () => {
            console.log("[클릭] 합격 버튼 클릭됨");
            passModal.style.display = "block";
        });

        closeModalBtn?.addEventListener("click", () => {
            console.log("[클릭] 모달 닫기 버튼");
            if (passModal) {
                passModal.style.display = "none";
            }
        });

        submitPassBtn?.addEventListener("click", () => {
            const message = document.getElementById("passMessage").value.trim();

            console.log("📦 전송 데이터:", {
                jobPostId,
                resumeId,
                message,
                csrfToken,
                csrfHeader
            });

            if (!message) {
                alert("메시지를 입력해 주세요.");
                return;
            }

            if (!jobPostId || !resumeId || !csrfToken || !csrfHeader) {
                alert("❌ 필수 정보 누락");
                return;
            }

            fetch(`/api/applications/job/pass`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({
                    jobPostId,
                    resumeId,
                    message
                })
            })
                .then(res => {
                    console.log("📡 서버 응답 도착 - status:", res.status);
                    const contentType = res.headers.get("content-type");
                    console.log("📎 Content-Type:", contentType);

                    if (res.ok) {
                        if (contentType && contentType.includes("application/json")) {
                            return res.json().then(data => {
                                console.log("✅ JSON 응답:", data);
                                alert("✅ " + data.message);
                                if (passModal) passModal.style.display = "none";
                            });
                        } else {
                            return res.text().then(msg => {
                                console.log("✅ TEXT 응답:", msg);
                                alert("✅ " + msg);
                                if (passModal) passModal.style.display = "none";
                            });
                        }
                    } else {
                        return res.text().then(msg => {
                            console.error("❌ 실패 응답 내용:", msg);
                            alert("❌ 실패: " + msg);
                        });
                    }
                })
                .catch(err => {
                    console.error("⚠️ 예외 발생 (네트워크 또는 파싱 오류):", err);
                    alert("⚠️ 서버 오류가 발생했습니다.");
                });
        });
    }


});

document.getElementById("treeViewBtn").addEventListener("click", () => {
    const treeContainer = document.getElementById("treeViewContainer");
    treeContainer.style.display = treeContainer.style.display === "none" ? "block" : "none";

    const root = document.getElementById("treeRoot");
    root.innerHTML = ""; // 기존 트리 초기화

    fetchTreeDataAndRender(root);
});

// 실제 데이터 기반으로 트리 구조 생성
function fetchTreeDataAndRender(root) {
    const resumeId = document.getElementById("resumeId").value;

    fetch(`/api/user/resumes/detail/${resumeId}`)
        .then(res => res.json())
        .then(resume => {
            const titleNode = document.createElement("li");
            titleNode.innerHTML = `📁 <strong>${resume.title || '이력서'}</strong>`;
            root.appendChild(titleNode);

            const descNode = document.createElement("div");
            descNode.style.marginLeft = "20px";
            descNode.textContent = resume.description || "항상 열심히 하는 사람입니다!";
            titleNode.appendChild(descNode);

            resume.sections.forEach(section => {
                const sectionNode = document.createElement("li");
                sectionNode.innerHTML = `▿ ${section.title}`;
                const subList = document.createElement("ul");
                if (section.dragItems && section.dragItems.length > 0) {
                    section.dragItems.forEach(item => {
                        console.log("📦 dragItem 확인:", item);
                        const li = document.createElement("li");

                        // 🔹 기본 텍스트
                        li.innerHTML = `<strong>${item.displayText}</strong>`;

                        // 🔹 기간이 있다면 추가
                        if (item.startDate && item.endDate) {
                            li.innerHTML += ` <span style="float:right;">(${item.startDate} ~ ${item.endDate})</span>`;
                        }

                        // 🔹 파일이 있다면 링크 추가
                        if (item.filePath) {
                            li.innerHTML += `<br><a href="/uploads/project/${item.filePath}" target="_blank">📁 파일 보기</a>`;
                        }

                        subList.appendChild(li);
                    });
                }


                if (section.fileNames && section.type === "파일 첨부") {
                    section.fileNames.forEach(file => {
                        const li = document.createElement("li");
                        li.textContent = file;
                        li.innerHTML += `<button style="float:right;">파일 다운</button>`;
                        subList.appendChild(li);
                    });
                }

                if (section.fileNames && section.type === "사진 첨부") {
                    section.fileNames.forEach(file => {
                        const li = document.createElement("li");
                        li.textContent = file;
                        subList.appendChild(li);
                    });
                }

                if (section.content && (!section.dragItems || section.dragItems.length === 0)) {
                    const contentLi = document.createElement("li");
                    contentLi.textContent = section.content;
                    subList.appendChild(contentLi);
                }

                sectionNode.appendChild(subList);
                root.appendChild(sectionNode);
            });
        })
        .catch(err => {
            console.error("트리 뷰 로딩 실패:", err);
        });


}
