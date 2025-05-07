function showTab(tabId) {
    document.querySelectorAll(".tab").forEach(tab => tab.classList.remove("active"));
    document.querySelectorAll(".tab-content").forEach(tc => tc.classList.remove("active"));

    document.querySelector(`.tab[onclick*="${tabId}"]`).classList.add("active");
    document.getElementById(tabId).classList.add("active");
}
