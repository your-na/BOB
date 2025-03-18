document.addEventListener("DOMContentLoaded", function () {
    var filter = document.getElementById("task-filter");

    filter.addEventListener("change", function () {
        var category = filter.value;
        var tasks = document.querySelectorAll(".task-item");

        tasks.forEach(task => {
            if (category === "전체" || task.getAttribute("data-category") === category) {
                task.style.display = "flex";
            } else {
                task.style.display = "none";
            }
        });
    });
});
