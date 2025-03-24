document.addEventListener("DOMContentLoaded", function () {
    const menuLinks = document.querySelectorAll(".menu li a");
    const mainContent = document.getElementById("main-content");

    // ✅ 클릭한 메뉴의 내용을 main-content에 동적으로 로드하는 함수
    function loadPage(url) {
        fetch(url)
            .then(response => response.text())
            .then(html => {
                mainContent.innerHTML = html;

                // ✅ 페이지 로드 후 적절한 JS 코드 실행
                if (url.includes("adcontest")) {
                    loadContestScripts(); // 공모전 페이지 검색 & 필터 기능 실행
                } else if (url.includes("adnewcon")) {
                    initContestCreation(); // 공모전 주최 페이지 기능 실행
                }
            })
            .catch(error => console.error("페이지 로딩 실패:", error));
    }

    // ✅ 메뉴 클릭 이벤트 설정
    menuLinks.forEach(link => {
        link.addEventListener("click", function (event) {
            event.preventDefault(); // 기본 링크 이동 방지

            // ✅ 현재 페이지 강조 (Active)
            menuLinks.forEach(link => link.classList.remove("active"));
            this.classList.add("active");

            // ✅ 선택한 페이지 로드
            const pageUrl = this.getAttribute("href");
            loadPage(pageUrl);
        });
    });

    // ✅ 첫 번째 페이지 자동 로드 (기본 화면)
    loadPage("/adcontest"); // "전체 공모전" 페이지를 기본으로 로드
});

// ✅ 공모전 주최 페이지의 기능 실행 함수 (adnewcon)
function initContestCreation() {
    console.log("✅ 공모전 주최 페이지 JS 실행됨!");

    const posterInput = document.getElementById("poster-input");
    const posterPreview = document.getElementById("poster-preview");
    const registerBtn = document.getElementById("register-btn");
    const cancelBtn = document.getElementById("cancel-btn");

    if (!posterInput || !registerBtn || !cancelBtn) {
        console.warn("⚠️ 공모전 주최 페이지의 요소를 찾을 수 없습니다!");
        return;
    }

    // ✅ 포스터 이미지 업로드 및 미리보기 기능
    posterInput.addEventListener("change", function (event) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function (e) {
                posterPreview.src = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    });

    // ✅ 등록 버튼 클릭 시 데이터 콘솔 출력
    registerBtn.addEventListener("click", function () {
        const contestData = {
            title: document.getElementById("contest-title").value,
            host: document.getElementById("contest-host").value,
            field: document.getElementById("contest-field").value,
            target: document.getElementById("contest-target").value,
            region: document.getElementById("contest-region").value,
            startDate: document.getElementById("contest-start").value,
            endDate: document.getElementById("contest-end").value,
            judge: document.getElementById("contest-judge").value,
            prize: document.getElementById("contest-prize").value,
            method: document.getElementById("contest-method").value,
            details: document.getElementById("contest-details").value
        };

        console.log("✅ 공모전 데이터:", contestData);
        alert("공모전이 등록되었습니다!");
    });

    // ✅ 취소 버튼 클릭 시 입력값 초기화
    cancelBtn.addEventListener("click", function () {
        if (confirm("입력한 내용을 모두 초기화하시겠습니까?")) {
            document.querySelector("form").reset();
            posterPreview.src = "/images/placeholder.png";
        }
    });
}

// ✅ 공모전 페이지의 JS를 실행하는 함수 추가 (검색 & 필터 기능)
function loadContestScripts() {
    setTimeout(function () {
        console.log("✅ 공모전 JS 실행됨!");

        var searchIcon = document.getElementById("searchicon");
        var searchBox = document.getElementById("search-box");
        var searchClose = document.getElementById("search-close");

        var filterIcon = document.getElementById("filtericon");
        var filterBox = document.getElementById("filter-box");
        var filterClose = document.getElementById("filter-close");
        var selectedFilters = document.getElementById("selected-filters");
        var filterOptions = document.querySelectorAll(".filter-option");
        var filterTabs = document.querySelectorAll(".filter-tab");
        var filterContents = document.querySelectorAll(".filter-category");

        var clearFiltersBtn = document.getElementById("clear-filters");

        if (!searchIcon || !filterIcon) {
            console.warn("⚠️ 공모전 페이지의 검색/필터 요소를 찾을 수 없습니다!");
            return;
        }

        // ✅ 검색 버튼 클릭 시 검색창 활성화
        searchIcon.addEventListener("click", function () {
            searchBox.classList.toggle("active");
        });

        // ✅ 검색창 닫기 버튼
        searchClose.addEventListener("click", function () {
            searchBox.classList.remove("active");
        });

        // ✅ 필터 버튼 클릭 시 필터 창 표시
        filterIcon.addEventListener("click", function () {
            filterBox.classList.toggle("active");
        });

        // ✅ 필터 닫기 버튼 클릭 시 필터 창 숨기기
        filterClose.addEventListener("click", function () {
            filterBox.classList.remove("active");
        });

        // ✅ 필터 옵션 선택 기능
        filterOptions.forEach(option => {
            option.addEventListener("click", function () {
                let existingFilter = document.querySelector(`.selected-item[data-filter="${option.textContent}"]`);

                if (existingFilter) {
                    option.classList.remove("selected");
                    existingFilter.remove();
                } else {
                    option.classList.add("selected");

                    let selected = document.createElement("div");
                    selected.classList.add("selected-item");
                    selected.textContent = option.textContent;
                    selected.setAttribute("data-filter", option.textContent);

                    selected.addEventListener("click", function () {
                        option.classList.remove("selected");
                        selected.remove();
                    });

                    selectedFilters.appendChild(selected);
                }
            });
        });

        // ✅ 필터 탭 전환 기능
        filterTabs.forEach(tab => {
            tab.addEventListener("click", function () {
                filterTabs.forEach(t => t.classList.remove("active"));
                this.classList.add("active");

                filterContents.forEach(content => content.classList.remove("active"));

                var target = this.getAttribute("data-target");
                document.getElementById(target).classList.add("active");
            });
        });

        // ✅ 필터 전체 삭제 버튼
        clearFiltersBtn.addEventListener("click", function () {
            selectedFilters.innerHTML = "";
            filterOptions.forEach(option => option.classList.remove("selected"));
        });

        console.log("✅ 공모전 필터 & 검색 기능 정상 작동!");
    }, 500); // 0.5초 후 실행 (페이지 로딩을 기다림)
}
