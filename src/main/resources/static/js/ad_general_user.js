document.addEventListener("DOMContentLoaded", function () {
    const searchInput = document.getElementById("search-input");

    searchInput.addEventListener("keyup", function () {
        let filter = searchInput.value.toLowerCase();
        let rows = document.querySelectorAll(".user-table tbody tr");

        rows.forEach(row => {
            let userName = row.cells[3].textContent.toLowerCase();
            let userId = row.cells[2].textContent.toLowerCase();
            let email = row.cells[5].textContent.toLowerCase();

            if (userName.includes(filter) || userId.includes(filter) || email.includes(filter)) {
                row.style.display = "";
            } else {
                row.style.display = "none";
            }
        });
    });

    document.addEventListener("DOMContentLoaded", function () {
        const filterBtn = document.querySelector(".filter-btn"); // 필터 버튼
        const filterMenu = document.querySelector(".filter-menu"); // 필터 메뉴
        const userBody = document.querySelector("#user-body");

        // ✅ 필터 버튼 클릭 시 필터 메뉴 토글 (열기/닫기)
        filterBtn.addEventListener("click", function () {
            filterMenu.classList.toggle("show"); // 메뉴 표시 여부 토글
        });

        // ✅ 필터 외부 클릭 시 메뉴 닫기
        document.addEventListener("click", function (event) {
            if (!filterBtn.contains(event.target) && !filterMenu.contains(event.target)) {
                filterMenu.classList.remove("show");
            }
        });

        // ✅ 테이블 데이터를 배열로 변환하는 함수
        function getUserData() {
            return Array.from(userBody.querySelectorAll("tr")).map(row => {
                return {
                    element: row,
                    date: new Date(row.cells[7].innerText) // 가입일 (index 7)
                };
            });
        }

        // ✅ 가입일 기준 정렬 함수
        function sortUsers(order) {
            let users = getUserData();

            users.sort((a, b) => {
                return order === "desc" ? b.date - a.date : a.date - b.date;
            });

            // 정렬된 순서대로 tbody에 추가
            userBody.innerHTML = "";
            users.forEach(user => userBody.appendChild(user.element));
        }

        // ✅ 필터 선택 시 이벤트 처리
        filterMenu.addEventListener("click", function (event) {
            const filterType = event.target.dataset.filter;

            if (filterType === "date-desc") {
                sortUsers("desc"); // 최신순
            } else if (filterType === "date-asc") {
                sortUsers("asc"); // 오래된순
            }

            filterMenu.classList.remove("show"); // 필터 선택 후 자동 닫기
        });
    });

});
