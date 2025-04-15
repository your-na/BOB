document.getElementById('posterInput').addEventListener('change', function (event) {
    const file = event.target.files[0];
    const preview = document.getElementById('posterPreview');
    const text = document.getElementById('posterText');
    const warning = document.getElementById('posterWarning');

    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
            text.style.display = 'none';
            warning.style.display = 'none';
        };
        reader.readAsDataURL(file);
    }
});

document.getElementById('fieldSelect').addEventListener('change', function () {
    const customInput = document.getElementById('customFieldInput');
    if (this.value === 'custom') {
        customInput.style.display = 'block';
    } else {
        customInput.style.display = 'none';
    }
});

window.addEventListener('DOMContentLoaded', () => {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    const formatted = `${yyyy}-${mm}-${dd}`;
    document.getElementById('submissionDate').textContent = formatted;
});

const textarea = document.getElementById('description');
textarea.addEventListener('input', function () {
    this.style.height = 'auto';
    this.style.height = `${this.scrollHeight}px`;
});

// 모달 표시
document.querySelector('.submit-btn').addEventListener('click', function (e) {
    e.preventDefault();
    document.getElementById('confirmModal').style.display = 'flex';
});

document.getElementById('confirmNo').addEventListener('click', function () {
    document.getElementById('confirmModal').style.display = 'none';
});

document.getElementById('confirmYes').addEventListener('click', function () {
    const fieldSelect = document.getElementById('fieldSelect');
    const customInput = document.getElementById('customFieldInput');

    // 직접 입력이 선택되었고 실제 입력이 있다면
    if (fieldSelect.value === 'custom' && customInput.value.trim() !== '') {
        // 🔥 직접 입력된 값을 전송하려면 hidden input에 넣어야 함
        const hiddenCategoryInput = document.createElement("input");
        hiddenCategoryInput.type = "hidden";
        hiddenCategoryInput.name = "customCategory"; // ✅ 서버에서 이 이름으로 받도록 되어 있음
        hiddenCategoryInput.value = customInput.value.trim();

        document.querySelector(".contest-form").appendChild(hiddenCategoryInput);

        // 선택된 category 자체는 무시될 수 있음
    }

    document.getElementById('confirmModal').style.display = 'none';
    document.getElementById('realSubmit').click();
});

