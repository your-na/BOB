document.addEventListener("DOMContentLoaded", function () {
    const tabs = document.querySelectorAll(".tab");
    const listContainer = document.querySelector(".application-list");
    const userId = document.querySelector('meta[name="user-id"]')?.content || 1;
    let currentTab = "online";

    function renderList(type, data) {
        currentTab = type;
        listContainer.innerHTML = "";

        if (!data || data.length === 0) {
            listContainer.innerHTML = "<p style='padding: 20px; color: #888;'>해당 내역이 없습니다.</p>";
            return;
        }

        const filtered = data.filter(item => {
            const status = item.status?.toUpperCase();
            switch (type) {
                case "online": return status === "SUBMITTED";
                case "other": return status === "ACCEPTED";
                case "other2": return status === "REJECTED";
                case "hidden": return status === "HIDDEN";
                default: return true;
            }
        });

        if (filtered.length === 0) {
            listContainer.innerHTML = "<p style='padding: 20px; color: #888;'>해당 내역이 없습니다.</p>";
            return;
        }

        filtered.forEach((item, index) => {
            const card = document.createElement("div");
            card.className = "application-card";
            card.setAttribute("data-id", item.id || index + 1);

            card.innerHTML = `
            <div class="left">
                <div class="apply-date">${item.appliedDate}</div>
                <div class="job-title">${item.jobTitle}</div>
                <div class="job-desc">${item.companyIntro}</div>
            </div>
            <div class="right">
                <button class="open-menu-btn" onclick="toggleMenu(this)">⋯</button>
                <ul class="dropdown-menu">
                   <li onclick="viewDetail(${item.jobPostId})">지원내역</li>
                   <li onclick="cancelApply(${item.jobPostId})">지원취소</li>
                   <li onclick="hideItem(${item.id || index + 1})">숨기기</li>
                   <li onclick="previewResume(${item.resumeId || item.id})">이력서 보기</li>
                </ul>
            </div>
        `;
            listContainer.appendChild(card);
        });
    }

    function fetchApplications() {
        fetch(`/api/applications/me`)
            .then(res => res.json())
            .then(data => {
                console.log("✅ 받아온 지원 데이터:", data);
                window.__applicationData = data;
                renderList("online", data);
            })
            .catch(err => {
                console.error("지원 내역 불러오기 실패:", err);
                listContainer.innerHTML = "<p style='color: red;'>데이터를 불러올 수 없습니다.</p>";
            });
    }

    tabs.forEach(tab => {
        tab.addEventListener("click", () => {
            tabs.forEach(t => t.classList.remove("active"));
            tab.classList.add("active");

            const type = tab.getAttribute("data-type");
            if (type === "online") {
                renderList("online", window.__applicationData || []);
            } else {
                listContainer.innerHTML = "<p style='padding: 20px; color: #888;'>해당 탭은 아직 미구현 상태입니다.</p>";
            }
        });
    });

    document.addEventListener("click", function (e) {
        if (!e.target.closest(".application-card")) {
            document.querySelectorAll(".dropdown-menu").forEach(menu => {
                menu.style.display = "none";
            });
        }
    });

    window.hideItem = function (id) {
        const data = window.__applicationData;
        const item = data.find(i => i.id === id);
        if (item) {
            item.companyIntro += " (숨김)";
            renderList(currentTab, data);
        }
    };

    window.viewDetail = function (jobPostId) {
        if (!jobPostId) return;
        location.href = `/resume/detail?jobPostId=${jobPostId}`;
    };

    window.cancelApply = function (jobPostId) {
        if (!confirm("정말 지원을 취소하시겠습니까?")) return;

        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        fetch(`/api/user/resumes/cancel?jobPostId=${jobPostId}`, {
            method: "DELETE",
            credentials: "include",
            headers: {
                [csrfHeader]: csrfToken
            }
        })
            .then(res => {
                if (res.ok) {
                    alert("✅ 지원이 취소되었습니다.");
                    fetchApplications();
                } else {
                    return res.text().then(msg => {
                        alert("❌ 취소 실패: " + msg);
                    });
                }
            })
            .catch(err => {
                alert("⚠️ 서버 오류가 발생했습니다.");
                console.error(err);
            });
    };

    // ✅ "이력서 보기" 기능
    window.previewResume = function (resumeId) {
        const url = resumeId ? `/showresume?id=${resumeId}` : `/showresume`;
        window.open(url, "_blank");
    };

    fetchApplications();
});

function toggleMenu(button) {
    const menu = button.nextElementSibling;
    document.querySelectorAll(".dropdown-menu").forEach(m => {
        if (m !== menu) m.style.display = "none";
    });
    menu.style.display = menu.style.display === "block" ? "none" : "block";
}
