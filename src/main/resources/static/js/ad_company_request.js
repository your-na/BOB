document.addEventListener("DOMContentLoaded", function () {
    const companyBody = document.getElementById("company-body");
    const searchInput = document.getElementById("search-input");
    const deleteButton = document.getElementById("delete-selected");
    const selectAllCheckbox = document.getElementById("select-all");

    // 샘플 데이터 (백엔드 연동 시 서버에서 가져와야 함)
    const companyData = [
        { id: 1, username: "kongjaji", company: "(주)콩자", name: "임콩자", email: "zhdwk@gmail.com", phone: "02)1234-5618", date: "2025.01.06", status: "승인 대기" },
        { id: 2, username: "kongjaji", company: "(주)콩자", name: "임콩자", email: "zhdwk@gmail.com", phone: "02)1234-5618", date: "2025.01.06", status: "승인 대기" },
        { id: 3, username: "kongjaji", company: "(주)콩자", name: "임콩자", email: "zhdwk@gmail.com", phone: "02)1234-5618", date: "2025.01.06", status: "승인 대기" }
    ];

    // ✅ 테이블 데이터 로드
    function loadCompanyData() {
        companyBody.innerHTML = "";
        companyData.forEach((company, index) => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${index + 1}</td>
                <td><input type="checkbox" class="row-checkbox"></td>
                <td>${company.username}</td>
                <td>${company.company}</td>
                <td>${company.name}</td>
                <td>${company.email}</td>
                <td>${company.phone}</td>
                <td>${company.date}</td>
                <td><button class="status-btn">${company.status}</button></td>
            `;
            companyBody.appendChild(row);
        });
    }

    loadCompanyData(); // 데이터 로드

    // ✅ 검색 기능
    searchInput.addEventListener("keyup", function () {
        const searchKeyword = searchInput.value.toLowerCase();
        const rows = document.querySelectorAll(".company-table tbody tr");

        rows.forEach(row => {
            const username = row.children[2].textContent.toLowerCase();
            const company = row.children[3].textContent.toLowerCase();
            const name = row.children[4].textContent.toLowerCase();

            if (username.includes(searchKeyword) || company.includes(searchKeyword) || name.includes(searchKeyword)) {
                row.style.display = "";
            } else {
                row.style.display = "none";
            }
        });
    });

    // ✅ 체크박스 전체 선택 기능
    selectAllCheckbox.addEventListener("change", function () {
        const checkboxes = document.querySelectorAll(".row-checkbox");
        checkboxes.forEach(checkbox => checkbox.checked = selectAllCheckbox.checked);
    });

    // ✅ 선택한 항목 삭제 기능
    deleteButton.addEventListener("click", function () {
        const selectedRows = document.querySelectorAll(".row-checkbox:checked");

        if (selectedRows.length === 0) {
            alert("삭제할 항목을 선택해주세요.");
            return;
        }

        selectedRows.forEach(row => {
            row.closest("tr").remove();
        });

        alert("선택된 항목이 삭제되었습니다.");
    });
});
