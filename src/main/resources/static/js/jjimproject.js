document.addEventListener("DOMContentLoaded", () => {
    const tableBody = document.getElementById("jjimProjectList");

    // ✅ 현재는 백엔드 연동 전이므로 더미 데이터로 표시
    const jjimProjects = [
        {
            name: "AI 이미지 분석 프로젝트",
            creator: "김민지",
            recruitPeriod: "D-2",
            progress: "2025-10-05 ~ 2025-10-20",
            views: 12,
            likes: 3,
            members: "2/3",
            status: "진행중"
        },
        {
            name: "헬스케어 앱 프론트 개발",
            creator: "서유진",
            recruitPeriod: "D-5",
            progress: "2025-10-10 ~ 2025-11-01",
            views: 8,
            likes: 5,
            members: "1/2",
            status: "진행중"
        },
        {
            name: "ChatGPT API 활용 서비스",
            creator: "박민서",
            recruitPeriod: "D-7",
            progress: "2025-10-08 ~ 2025-11-05",
            views: 5,
            likes: 7,
            members: "3/3",
            status: "진행중"
        }
    ];

    // ✅ 테이블 생성
    jjimProjects.forEach(project => {
        const row = document.createElement("tr");
        row.innerHTML = `
      <td>${project.name}</td>
      <td>${project.creator}</td>
      <td>${project.recruitPeriod}</td>
      <td>${project.progress}</td>
      <td>${project.views}</td>
      <td>${project.likes}</td>
      <td>${project.members}</td>
      <td class="status-active">${project.status}</td>
    `;
        tableBody.appendChild(row);
    });
});
