// ✅ 일반회원 정보 섹션 숨기기
document.addEventListener("DOMContentLoaded", () => {
    const sections = document.querySelectorAll(".info-section");

    sections.forEach((section) => {
        const titleEl = section.querySelector("h3");
        if (!titleEl) return;

        const rawTitle = titleEl.textContent;
        const normalized = rawTitle.trim().replace(/\s+/g, "").toLowerCase();

        if (normalized === "일반회원정보") {
            section.style.display = "none";
        }
    });
});

// ✅ 섹션 순서 정렬
document.addEventListener("DOMContentLoaded", () => {
    const container = document.querySelector(".resume-detail-container");
    const order = ["일반회원정보", "학력사항", "경력사항", "포트폴리오", "자기소개"];

    order.forEach((title) => {
        const section = [...document.querySelectorAll(".info-section")]
            .find(sec => sec.querySelector("h3")?.textContent.trim() === title);
        if (section) container.appendChild(section);
    });
});

// ✅ 다운로드 버튼 기능 (PDF 저장)
document.addEventListener("DOMContentLoaded", () => {
    const downloadBtn = document.getElementById("downloadBtn");
    if (downloadBtn) {
        downloadBtn.addEventListener("click", () => {
            const { jsPDF } = window.jspdf;
            const resume = document.querySelector(".resume-detail-container");

            html2canvas(resume, { scale: 2 }).then(canvas => {
                const imgData = canvas.toDataURL("image/png");
                const pdf = new jsPDF("p", "mm", "a4");

                const pageWidth = pdf.internal.pageSize.getWidth();
                const pageHeight = pdf.internal.pageSize.getHeight();

                const imgWidth = pageWidth;
                const imgHeight = canvas.height * pageWidth / canvas.width;

                let heightLeft = imgHeight;
                let position = 0;

                pdf.addImage(imgData, "PNG", 0, position, imgWidth, imgHeight);
                heightLeft -= pageHeight;

                while (heightLeft > 0) {
                    position = heightLeft - imgHeight;
                    pdf.addPage();
                    pdf.addImage(imgData, "PNG", 0, position, imgWidth, imgHeight);
                    heightLeft -= pageHeight;
                }

                pdf.save("이력서.pdf");
            });
        });
    }
});

// ✅ 합격/불합격 처리 기능
document.addEventListener("DOMContentLoaded", () => {
    const jobPostId = new URLSearchParams(location.search).get("jobPostId");
    const pathParts = window.location.pathname.split("/");
    const resumeId = pathParts[pathParts.length - 1];
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    // ❎ 불합격 처리
    const nonpassBtn = document.querySelector(".nonpass-btn");
    if (nonpassBtn) {
        nonpassBtn.addEventListener("click", () => {
            if (!confirm("정말 이 지원자를 불합격 처리하시겠습니까?")) return;

            fetch("/api/applications/job/reject-myresume", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({
                    jobPostId,
                    myResumeId: resumeId,
                    message: "" // ✅ 빈 문자열
                })
            })
                .then(res => res.ok ? res.json() : res.text().then(msg => { throw new Error(msg); }))
                .then(data => alert("❎ " + data.message))
                .catch(err => alert("⚠️ 불합격 처리 실패: " + err.message));
        });
    }

    // ✅ 합격 처리
    const passBtn = document.querySelector(".pass-btn");
    const passModal = document.getElementById("passModal");
    const submitPassBtn = document.getElementById("submitPassBtn");

    if (passBtn && passModal) {
        passBtn.addEventListener("click", () => {
            passModal.style.display = "block";
        });

        submitPassBtn?.addEventListener("click", () => {
            const message = document.getElementById("passMessage").value.trim();
            if (!message) {
                alert("메시지를 입력해 주세요.");
                return;
            }

            fetch("/api/applications/job/pass-myresume", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({ jobPostId, myResumeId: resumeId, message })
            })
                .then(res => res.ok ? res.json() : res.text().then(msg => { throw new Error(msg); }))
                .then(data => {
                    alert("✅ " + data.message);
                    passModal.style.display = "none";
                })
                .catch(err => alert("⚠️ 합격 처리 실패: " + err.message));
        });
    }
});
