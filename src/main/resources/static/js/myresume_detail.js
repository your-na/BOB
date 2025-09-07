document.addEventListener("DOMContentLoaded", () => {
    const treeBtn = document.getElementById("treeViewBtn");
    const treeContainer = document.getElementById("treeViewContainer");
    const root = document.getElementById("treeRoot");

    if (treeBtn) {
        treeBtn.addEventListener("click", () => {
            treeContainer.style.display = treeContainer.style.display === "none" ? "block" : "none";
            root.innerHTML = ""; // 초기화
            fetchTreeDataAndRender(root);
        });
    }
});

// 트리뷰 그리기 함수
function fetchTreeDataAndRender(root) {
    const resumeId = document.getElementById("resumeId")?.value;
    if (!resumeId) return;

    fetch(`/api/user/resumes/detail/${resumeId}`)
        .then(res => res.json())
        .then(resume => {
            // 최상단 타이틀
            const titleNode = document.createElement("li");
            titleNode.innerHTML = `📁 <strong>${resume.title || '이력서'}</strong>`;
            root.appendChild(titleNode);

            // 섹션들 렌더링
            resume.sections.forEach(section => {
                const sectionNode = document.createElement("li");
                sectionNode.innerHTML = `▿ ${section.title}`;
                const subList = document.createElement("ul");

                // 드래그 항목 있으면 출력
                if (section.dragItems?.length) {
                    section.dragItems.forEach(item => {
                        const li = document.createElement("li");
                        li.innerHTML = `<strong>${item.displayText}</strong>`;
                        if (item.startDate && item.endDate) {
                            li.innerHTML += ` <span style="float:right;">(${item.startDate} ~ ${item.endDate})</span>`;
                        }
                        if (item.filePath) {
                            li.innerHTML += `<br><a href="/uploads/projectFiles/${item.filePath}" target="_blank">📁 파일 보기</a>`;
                        }
                        subList.appendChild(li);
                    });
                }

                // 일반 텍스트 content
                if (!section.dragItems?.length && section.content) {
                    const contentLi = document.createElement("li");
                    contentLi.textContent = section.content;
                    subList.appendChild(contentLi);
                }

                // 파일 첨부
                if (section.fileNames?.length && section.type === "파일 첨부") {
                    section.fileNames.forEach(file => {
                        const li = document.createElement("li");
                        li.innerHTML = `
                            📄 <a href="/uploads/resumeFiles/${file}" download>${file}</a>
                        `;
                        subList.appendChild(li);
                    });
                }

                // 사진 첨부
                if (section.fileNames?.length && section.type === "사진 첨부") {
                    section.fileNames.forEach(file => {
                        const li = document.createElement("li");
                        li.innerHTML = `<img src="/uploads/resumeFiles/${file}" alt="이미지" width="100">`;
                        subList.appendChild(li);
                    });
                }

                sectionNode.appendChild(subList);
                root.appendChild(sectionNode);
            });
        })
        .catch(err => {
            console.error("트리뷰 로딩 실패:", err);
        });
}
