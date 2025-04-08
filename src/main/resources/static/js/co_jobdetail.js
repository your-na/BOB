function openResumeModal(title) {
    const modal = document.getElementById('resumeModal');
    const modalTitle = document.getElementById('modal-title');
    modalTitle.textContent = title;
    modal.style.display = 'flex';
}

function closeResumeModal() {
    const modal = document.getElementById('resumeModal');
    modal.style.display = 'none';
}

// ESC 누르면 닫기
document.addEventListener('keydown', function(event) {
    if (event.key === "Escape") {
        closeResumeModal();
    }
});

// 배경 클릭 시 닫기
document.addEventListener('click', function(event) {
    const modal = document.getElementById('resumeModal');
    if (event.target === modal) {
        closeResumeModal();
    }
});
