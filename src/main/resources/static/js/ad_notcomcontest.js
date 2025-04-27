document.addEventListener("DOMContentLoaded", () => {
    const selectAllCheckbox = document.getElementById("select-all");
    const checkboxes = document.querySelectorAll(".company-checkbox");

    // ✅ 전체 선택 / 해제
    selectAllCheckbox.addEventListener("change", function () {
        checkboxes.forEach(cb => {
            cb.checked = selectAllCheckbox.checked;
        });
    });

    // ✅ 개별 체크 해제 시, 전체 선택 체크 해제
    checkboxes.forEach(cb => {
        cb.addEventListener("change", () => {
            if (!cb.checked) {
                selectAllCheckbox.checked = false;
            } else if (Array.from(checkboxes).every(c => c.checked)) {
                selectAllCheckbox.checked = true;
            }
        });
    });

    // ✅ 삭제 버튼 클릭 시 확인
    const deleteBtn = document.getElementById("delete-selected");
    if (deleteBtn) {
        deleteBtn.addEventListener("click", (e) => {
            const checked = document.querySelectorAll(".company-checkbox:checked");
            if (checked.length === 0) {
                e.preventDefault();
                alert("삭제할 기업을 선택하세요.");
            } else {
                if (!confirm("선택한 기업을 삭제하시겠습니까?")) {
                    e.preventDefault();
                }
            }
        });
    }
});
