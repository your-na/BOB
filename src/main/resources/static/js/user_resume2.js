document.querySelectorAll('.section-box textarea, .section-box input, .section-box select').forEach(el => {
    el.addEventListener('focus', function () {
        document.querySelectorAll('.section-box').forEach(box => box.classList.remove('active'));
        this.closest('.section-box').classList.add('active');
    });
});
const modal = document.getElementById('modal');
const openBtn = document.getElementById('openModal');
const closeBtn = document.getElementById('closeModal');

openBtn.addEventListener('click', () => {
    modal.classList.add('active');
});

closeBtn.addEventListener('click', () => {
    modal.classList.remove('active');
});
