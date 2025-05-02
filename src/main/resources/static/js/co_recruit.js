document.addEventListener('DOMContentLoaded', () => {
    const toggleBtn = document.querySelector('.dropdown-toggle');
    const menu = document.querySelector('.dropdown-menu');

    toggleBtn.addEventListener('click', () => {
        menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
    });

    menu.querySelectorAll('li').forEach(item => {
        item.addEventListener('click', () => {
            toggleBtn.textContent = item.textContent;
            menu.style.display = 'none';

            // 정렬 방식에 따라 정렬 (더미라서 실제 동작 X)
            if (item.dataset.value === 'latest') {
                alert('최신순 정렬 기능 (백엔드 연결 예정)');
            } else {
                alert('오래된순 정렬 기능 (백엔드 연결 예정)');
            }
        });
    });

    document.addEventListener('click', (e) => {
        if (!menu.contains(e.target) && !toggleBtn.contains(e.target)) {
            menu.style.display = 'none';
        }
    });
});
