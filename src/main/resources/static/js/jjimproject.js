document.addEventListener("DOMContentLoaded", () => {
    const tableBody = document.getElementById("jjimProjectList");

    // ✅ 실제 서버 API 호출
    fetch("/api/jjim-projects")
        .then(response => {
            if (!response.ok) {
                throw new Error("서버 응답 실패");
            }
            return response.json();
        })
        .then(jjimProjects => {
            // ✅ 데이터 받아와서 테이블에 표시
            jjimProjects.forEach(project => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${project.title}</td>
                    <td>${project.createdBy}</td>
                    <td>${project.recruitmentStartDate} ~ ${project.recruitmentEndDate}</td>
                    <td>${project.startDate} ~ ${project.endDate}</td>
                    <td>${project.views}</td>
                    <td>${project.likes}</td>
                    <td>${project.currentParticipants}/${project.recruitmentCount}</td>
                    <td class="status-active">${project.status}</td>
                `;

                // ✅ 📍 여기 추가: 행 클릭 시 상세보기 페이지로 이동
                row.style.cursor = "pointer"; // 마우스 커서 변경
                row.addEventListener("click", () => {
                    window.location.href = `/postproject/${project.id}`;
                });

                // ✅ 테이블에 추가
                tableBody.appendChild(row);
            });

            // 🔃 비어있을 경우 메시지 표시
            if (jjimProjects.length === 0) {
                const emptyRow = document.createElement("tr");
                emptyRow.innerHTML = `<td colspan="8" style="text-align:center;">찜한 프로젝트가 없습니다.</td>`;
                tableBody.appendChild(emptyRow);
            }
        })
        .catch(error => {
            console.error("찜한 프로젝트 불러오기 실패:", error);
            const errorRow = document.createElement("tr");
            errorRow.innerHTML = `<td colspan="8" style="text-align:center; color:red;">프로젝트를 불러오는 중 오류가 발생했습니다.</td>`;
            tableBody.appendChild(errorRow);
        });
});
