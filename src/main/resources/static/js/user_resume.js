// ✅ 1. section-box 강조
document.querySelectorAll('.section-box textarea, .section-box input, .section-box select').forEach(el => {
    el.addEventListener('focus', function () {
        document.querySelectorAll('.section-box').forEach(box => box.classList.remove('active'));
        this.closest('.section-box').classList.add('active');
    });
});

// ✅ 2. 연도/월 select 옵션 생성 함수
function createYearOptions(select, start = 1980) {
    const current = new Date().getFullYear();
    for (let y = current; y >= start; y--) {
        const option = document.createElement("option");
        option.value = y;
        option.textContent = y;
        select.appendChild(option);
    }
}

function createMonthOptions(select) {
    for (let m = 1; m <= 12; m++) {
        const padded = m.toString().padStart(2, "0");
        const option = document.createElement("option");
        option.value = padded;
        option.textContent = padded;
        select.appendChild(option);
    }
}

// ✅ 3. 재학 상태면 종료일 숨기기
function setupStatusListener(eduItem) {
    const status = eduItem.querySelector(".edu-status");
    const endYear = eduItem.querySelector(".end-year");
    const endMonth = eduItem.querySelector(".end-month");
    const tilde = eduItem.querySelector(".tilde");

    status.addEventListener("change", () => {
        if (status.value === "재학") {
            endYear.style.display = "none";
            endMonth.style.display = "none";
            if (tilde) tilde.style.display = "none";
        } else {
            endYear.style.display = "inline-block";
            endMonth.style.display = "inline-block";
            if (tilde) tilde.style.display = "inline-block";
        }
    });
}

// ✅ 4. 삭제 기능
function addDeleteFunction(button) {
    button.addEventListener('click', () => {
        const list = document.getElementById("education-list");
        const items = list.querySelectorAll(".education-item");
        if (items.length > 1) {
            button.closest(".education-item").remove();
        } else {
            alert("최소 1개 이상의 학력사항이 필요합니다.");
        }
    });
}

// ✅ 5. 추가 버튼 클릭 시 복제
const addBtn = document.querySelector(".edu-btn"); // 버튼 클래스
const list = document.getElementById("education-list");
const firstItem = list.querySelector(".education-item");

addBtn.addEventListener("click", () => {
    const clone = firstItem.cloneNode(true);

    // input/select 초기화
    clone.querySelectorAll("input, select").forEach(el => {
        el.value = "";
    });

    // select 초기화
    const startYear = clone.querySelector(".start-year");
    const startMonth = clone.querySelector(".start-month");
    const endYear = clone.querySelector(".end-year");
    const endMonth = clone.querySelector(".end-month");

    startYear.innerHTML = "";
    startMonth.innerHTML = "";
    endYear.innerHTML = "";
    endMonth.innerHTML = "";

    createYearOptions(startYear);
    createMonthOptions(startMonth);
    createYearOptions(endYear);
    createMonthOptions(endMonth);

    // X 버튼 연결
    const deleteBtn = clone.querySelector(".del-btn");
    addDeleteFunction(deleteBtn);

    // 상태 이벤트 연결
    setupStatusListener(clone);

    // 간격 추가
    clone.style.marginTop = "10px";

    list.appendChild(clone);
});

// ✅ 6. 초기 항목 세팅
createYearOptions(firstItem.querySelector(".start-year"));
createMonthOptions(firstItem.querySelector(".start-month"));
createYearOptions(firstItem.querySelector(".end-year"));
createMonthOptions(firstItem.querySelector(".end-month"));
setupStatusListener(firstItem);
const deleteBtn = firstItem.querySelector(".del-btn");
addDeleteFunction(deleteBtn);

// ✅ 글자 수 세기 기능
const selfIntro = document.getElementById("selfIntro");
const charCount = document.getElementById("charCount");

selfIntro.addEventListener("input", () => {
    const len = selfIntro.value.length;
    charCount.textContent = `${len} / 500`;

    if (len < 500) {
        charCount.classList.add("warning");
    } else {
        charCount.classList.remove("warning");
    }
});

// 제출 버튼 누르면 모달 띄우기
const submitBtn = document.querySelector(".sub-btn");
const modal = document.getElementById("submitModal");
const confirmBtn = document.querySelector(".modal-confirm");
const cancelBtn = document.querySelector(".modal-cancel");

submitBtn.addEventListener("click", () => {
    modal.style.display = "flex";
});

// "아니오" 클릭 → 모달 닫기
cancelBtn.addEventListener("click", () => {
    modal.style.display = "none";
});

// "네" 클릭 → 제출 처리 (여기엔 실제 제출 로직 연결 가능)
confirmBtn.addEventListener("click", () => {
    modal.style.display = "none";
    alert("제출되었습니다!");
    // 실제 폼 제출이 필요하면 여기서 submit 호출 등 추가 가능
});
document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', function () {
        const tabName = this.dataset.tab;

        // 활성화 표시
        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        this.classList.add('active');

        // 해당 내용만 표시
        document.querySelectorAll('.tab-content').forEach(content => {
            content.style.display = content.dataset.content === tabName ? 'block' : 'none';
        });
    });
});
// 내 정보 화살표 클릭 시 내 정보로 이동
const arrowToggle = document.querySelector('.arrow-toggle');
arrowToggle.addEventListener('click', () => {
    window.location.href = '/profile';
});