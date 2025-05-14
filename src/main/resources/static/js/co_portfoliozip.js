document.addEventListener("DOMContentLoaded", function () {
    const checkbox = document.getElementById("bobPickOnly");
    const allRows = document.querySelectorAll(".portfolio-table tbody tr");

    checkbox.addEventListener("change", function () {
        allRows.forEach(row => {
            const isBobPick = row.querySelector(".badge")?.textContent === "BOB PICK";
            if (checkbox.checked) {
                row.style.display = isBobPick ? "" : "none";
            } else {
                row.style.display = "";
            }
        });
    });
});