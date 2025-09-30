
// ✅ CSRF 관련 함수
function getCsrfToken() {
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    return csrfMeta ? csrfMeta.getAttribute("content") : "";
}

function getCsrfHeader() {
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    return csrfHeaderMeta ? csrfHeaderMeta.getAttribute("content") : "X-CSRF-TOKEN";
}

// ✅ 공모전 참여 내역 초기 로딩
function loadContests() {
    const tbody = document.querySelector("#contest-history .history-table tbody");
    const templateRow = tbody.querySelector(".new-entry-row");

    tbody.innerHTML = "";
    if (templateRow) {
        tbody.appendChild(templateRow);
        templateRow.style.display = "none";
    }

    fetch("/api/contest-history", {
        headers: { [getCsrfHeader()]: getCsrfToken() }
    })
        .then(res => res.json())
        .then(data => {
            data.forEach((contest, index) => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${index + 1}</td>
                    <td>${contest.status}</td>
                    <td>${contest.startDate || ""}</td>
                    <td>${contest.endDate || ""}</td>
                    <td>
                        <span>${contest.title || ""}</span><br/>
                        <small>${contest.organizer || ""}</small>
                    </td>
                    <td>${contest.grade || ""}</td>
                    <td><button class="delete-btn" data-id="${contest.id}">삭제</button></td>
                `;
                tbody.insertBefore(row, templateRow);
            });
        })
        .catch(err => console.error("❌ 공모전 불러오기 실패:", err));
}


// ✅ 공모전 OCR 모달 단계 제어
function goContestStep(n) {
    const steps = document.querySelectorAll("#contest-award-modal .award-step");
    steps.forEach(s => {
        s.style.display = (s.dataset.step === String(n)) ? "block" : "none";
    });
}

// ✅ OCR 업로드 처리
document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById('contest-award-modal');
    if (!modal) return; // HTML에 요소 없으면 실행 안 함

    const steps = [...modal.querySelectorAll('.award-step')];
    const openBtn = document.querySelector('#contest-add-btn');
    const closeBtns = modal.querySelectorAll('.award-modal-close, .award-cancel, .award-done');
    const nextBtn = modal.querySelector('.award-next');
    const backBtn = modal.querySelector('.award-back');
    const confirmBtn = modal.querySelector('.award-confirm');

    const fileInput = document.getElementById('contest-award-file');
    const imgEl = document.getElementById('contest-award-image');
    const canvas = document.getElementById('contest-award-canvas');
    const rawTextEl = document.getElementById('contest-award-raw-text');
    const titleInput = document.getElementById('contest-title');
    const gradeInput = document.getElementById('contest-grade');
    const orgInput = document.getElementById('contest-organizer');
    const progressBar = modal.querySelector('.award-progress-bar'); // ✅ 수정됨
    const finalMsg = document.getElementById('contest-award-final-message');

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    let fileBlob = null;
    let ocrText = '';
    let parsed = { title: '', grade: '', organizer: '' };

    // ===== 유틸 =====
    const goStep = (n) => steps.forEach(s => s.style.display = (s.dataset.step === String(n)) ? 'block' : 'none');
    const open = () => { modal.style.display = 'flex'; goStep(1); reset(); };
    const close = () => { modal.style.display = 'none'; };
    const reset = () => {
        fileInput.value = '';
        nextBtn.disabled = true;
        if (imgEl) imgEl.style.display = 'none';
        if (canvas) canvas.style.display = 'none';
        rawTextEl.value = '';
        titleInput.value = '';
        gradeInput.value = '';
        orgInput.value = '';
        if (progressBar) progressBar.style.width = '0%';
        finalMsg.textContent = '처리가 완료되었습니다.';
        ocrText = '';
        parsed = { title: '', grade: '', organizer: '' };
    };

    // 모달 열고 닫기
    openBtn?.addEventListener('click', open);
    closeBtns.forEach(b => b.addEventListener('click', close));
    backBtn?.addEventListener('click', () => goStep(1));

    // 파일 선택되면 활성화
    fileInput.addEventListener('change', () => {
        nextBtn.disabled = !fileInput.files?.[0];
        fileBlob = fileInput.files?.[0] ?? null;
    });

    // OCR 실행 버튼
    nextBtn.addEventListener('click', async () => {
        if (!fileBlob) return;
        goStep(2);

        try {
            const isPDF = /\.pdf$/i.test(fileBlob.name);
            let dataURL;
            if (isPDF) {
                dataURL = await renderPdfFirstPageToDataURL(fileBlob);
            } else {
                dataURL = await readFileAsDataURL(fileBlob);
            }

            await showPreview(dataURL);

            let pseudo = 0;
            const tick = setInterval(() => {
                pseudo = Math.min(90, pseudo + 3);
                if (progressBar) progressBar.style.width = pseudo + '%';
            }, 120);

            ocrText = await runOCR(dataURL);
            ocrText = normalizeKoreanForAward(ocrText);

            clearInterval(tick);
            if (progressBar) progressBar.style.width = '100%';

            rawTextEl.value = ocrText.trim();

            parsed = parseAward(ocrText);
            gradeInput.value = parsed.grade || '';
            orgInput.value = parsed.organizer || '';

            goStep(3);
        } catch (err) {
            console.error(err);
            finalMsg.textContent = '인식 중 오류가 발생했습니다. 파일 형식을 확인해 주세요.';
            goStep(4);
        }
    });

    // ✅ OCR 확정 버튼 클릭 시 → 새 행 추가만 담당
    confirmBtn.addEventListener("click", () => {
        const title = titleInput.value.trim();
        const grade = (parsed.grade || "").trim();
        const organizer = (parsed.organizer || "").trim();

        if (!title && !grade && !organizer) {
            alert("인식된 정보가 없습니다. 다시 시도해 주세요.");
            return;
        }

        const tbody = document.querySelector("#contest-history .history-table tbody");
        const templateRow = tbody.querySelector(".new-entry-row");

        // ✅ 새 출력용 행 생성
        const row = document.createElement("tr");
        row.innerHTML = `
        <td>-</td>
        <td>
          <select class="status-select">
            <option value="참여중">참여중</option>
            <option value="참여완료" selected>참여완료</option>
          </select>
        </td>
        <td><input type="date" class="start-date"></td>
        <td><input type="date" class="end-date"></td>
        <td>
            <span>${title}</span><br/>
            <small>${organizer}</small>
        </td>
        <td>${grade}</td>
        <td><button class="delete-btn">삭제</button></td>
    `;

        // ✅ 입력용 템플릿 앞에 삽입
        tbody.insertBefore(row, templateRow);

        finalMsg.textContent = "공모전 내역에 추가되었습니다. 필요한 날짜를 입력해 주세요.";
        goStep(4);
    });


    // ===== 파일/미리보기 =====
    function readFileAsDataURL(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.onerror = reject;
            reader.readAsDataURL(file);
        });
    }

    async function renderPdfFirstPageToDataURL(file) {
        const arrayBuf = await file.arrayBuffer();
        const pdf = await pdfjsLib.getDocument({ data: arrayBuf }).promise;
        const page = await pdf.getPage(1);
        const viewport = page.getViewport({ scale: 2.4 });
        const cvs = document.createElement('canvas');
        const ctx = cvs.getContext('2d');
        cvs.width = viewport.width;
        cvs.height = viewport.height;
        await page.render({ canvasContext: ctx, viewport }).promise;
        return cvs.toDataURL('image/png');
    }

    async function showPreview(dataURL) {
        imgEl.src = dataURL;
        imgEl.style.display = 'block';
        canvas.style.display = 'none';
    }

    // ===== OCR 전처리 & 실행 =====
    async function preprocessForOcr(dataURL, targetMax = 3000) {
        return new Promise((resolve) => {
            const img = new Image();
            img.onload = () => {
                const W = img.naturalWidth, H = img.naturalHeight;
                const scale = Math.max(1, targetMax / Math.max(W, H));
                const w = Math.round(W * scale), h = Math.round(H * scale);

                const c = document.createElement('canvas');
                c.width = w; c.height = h;
                const ctx = c.getContext('2d');
                ctx.drawImage(img, 0, 0, w, h);

                const id = ctx.getImageData(0, 0, w, h);
                const d = id.data;
                for (let i = 0; i < d.length; i += 4) {
                    const g = 0.299 * d[i] + 0.587 * d[i + 1] + 0.114 * d[i + 2];
                    d[i] = d[i + 1] = d[i + 2] = g > 160 ? 255 : 0;
                }
                ctx.putImageData(id, 0, 0);
                resolve(c.toDataURL('image/png'));
            };
            img.src = dataURL;
        });
    }

    async function runOCR(dataURL) {
        const prepped = await preprocessForOcr(dataURL, 3000);
        const worker = await Tesseract.createWorker('kor', 1);
        const { data: { text } } = await worker.recognize(prepped, 'kor+eng', {
            tessedit_pageseg_mode: '6',
            user_defined_dpi: '300'
        });
        await worker.terminate();
        return text || '';
    }

    // ===== 텍스트 보정 & 파싱 =====
    function normalizeKoreanForAward(t) {
        if (!t) return '';
        let s = t.replace(/\r/g, '').replace(/[ \t]+/g, ' ').replace(/\u00A0/g, ' ');
        s = s.replace(/(?<=\p{Script=Hangul})\s+(?=\p{Script=Hangul})/gu, '');
        const grades = ['대상', '최우수상', '금상', '우수상', '은상', '장려상', '동상', '특별상', '입상', '본상'];
        for (const g of grades) {
            const spaced = new RegExp(g.split('').join('\\s*'), 'g');
            s = s.replace(spaced, g);
        }
        return s;
    }

    function parseAward(textRaw) {
        const text = (textRaw || '').replace(/\s+\n/g, '\n').trim();

        // 1. 수상 등급 찾기
        const gradeDict = ['대상', '최우수상', '금상', '우수상', '은상', '장려상', '동상', '특별상', '입상', '본상'];
        let grade = '';
        for (const g of gradeDict) {
            if (text.includes(g)) { grade = g; break; }
        }

        // 2. 주최기관 추출
        let organizer = '';
        const lines = text.split('\n');
        const orgKeys = /(대학교|교육청|협회|재단|공단|주식회사|\(주\)|기업)/;

        for (let i = lines.length - 1; i >= 0; i--) {
            if (orgKeys.test(lines[i])) {
                organizer = lines[i].trim();

                // 직함 / 불필요 단어 제거
                organizer = organizer.replace(/대표이사.*$/, '')
                    .replace(/회장.*$/, '')
                    .replace(/사장.*$/, '')
                    .replace(/원장.*$/, '')
                    .replace(/교수.*$/, '')
                    .replace(/\s+$/, ''); // 끝 공백 제거
                break;
            }
        }

        return { grade, organizer };
    }

});

// ✅ 공모전 자동 저장
// ✅ 공모전 행 추가 후 자동 저장 로직
document.addEventListener("change", async (event) => {
    const row = event.target.closest("#contest-history tbody tr");
    if (!row || row.classList.contains("new-entry-row")) return;

    let id = row.querySelector(".delete-btn")?.dataset.id;

    const data = {
        status: row.querySelector(".status-select")?.value || "참여완료",
        startDate: row.querySelector(".start-date")?.value,
        endDate: row.querySelector(".end-date")?.value,
        title: row.querySelector(".contest-name")?.value.trim(),
        organizer: row.querySelector(".contest-org")?.value.trim(),
        grade: row.querySelector(".contest-grade")?.value.trim()
    };

    try {
        if (!id) {
            // 아직 저장 안 된 신규 Row → POST
            const res = await fetch("/api/contest-history", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [getCsrfHeader()]: getCsrfToken()
                },
                body: JSON.stringify(data)
            });
            if (!res.ok) throw new Error("저장 실패");
            const saved = await res.json();
            row.querySelector(".delete-btn").dataset.id = saved.id;
            console.log("✅ 공모전 최초 저장:", saved);
        } else {
            // 이미 저장된 Row → PUT
            const res = await fetch(`/api/contest-history/${id}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    [getCsrfHeader()]: getCsrfToken()
                },
                body: JSON.stringify(data)
            });
            if (!res.ok) throw new Error("수정 실패");
            console.log("✅ 공모전 수정됨:", await res.json());
        }
    } catch (err) {
        console.error("❌ 공모전 저장/수정 실패:", err);
    }
});


document.addEventListener("DOMContentLoaded", function () {
    const tabs = document.querySelectorAll(".tab-item");
    const sections = document.querySelectorAll(".history-section");

    // ✅ 탭 클릭 시 해당 섹션 표시
    tabs.forEach(tab => {
        tab.addEventListener("click", function () {
            tabs.forEach(t => t.classList.remove("active"));
            sections.forEach(s => s.style.display = "none");

            this.classList.add("active");
            const targetId = this.getAttribute("data-target");
            const targetSection = document.getElementById(targetId);
            if (targetSection) {
                targetSection.style.display = "block";
            }
        });
    });

    // ✅ 검색 기능
    document.addEventListener("keyup", function (event) {
        if (event.target.classList.contains("searchbar")) {
            const searchKeyword = event.target.value.toLowerCase();
            const section = event.target.closest(".history-section");
            const rows = section.querySelectorAll(".history-table tbody tr:not(.new-entry-row)");

            rows.forEach(row => {
                const titleCell = row.querySelector("td:nth-child(5)");
                if (!titleCell) return;
                const title = titleCell.textContent.toLowerCase();
                row.style.display = title.includes(searchKeyword) ? "table-row" : "none";
            });
        }
    });


    // ✅ 삭제 버튼 처리 (섹션별 메시지 + URL 다르게 적용)
    document.addEventListener("click", function (event) {
        if (event.target.classList.contains("delete-btn")) {
            const section = event.target.closest(".history-section");
            const row = event.target.closest("tr");
            const id = event.target.getAttribute("data-id");

            if (!id) {
                alert("❌ 삭제할 항목의 ID가 없습니다.");
                return;
            }

            let deleteUrl = "";
            let message = "정말 삭제하시겠습니까?";
            let isHandled = true;

            switch (section?.id) {
                case "project-history":
                    deleteUrl = `/project-history/${id}`;
                    message = "⚠정말 삭제하시겠습니까?\n삭제하면 마이프로젝트에서도 사라지며, 복구할 수 없습니다.";
                    break;
                case "simple-history":
                    deleteUrl = `/api/basic-info/${id}`;
                    break;
                case "school-history":
                    deleteUrl = `/api/education-history/delete/${id}`;
                    break;
                case "contest-history":
                    deleteUrl = `/api/contest-history/${id}`;
                    break;
                default:
                    isHandled = false;
            }

            if (!isHandled || !deleteUrl) return;

            if (!confirm(message)) return;

            fetch(deleteUrl, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                    [getCsrfHeader()]: getCsrfToken()
                }
            })
                .then(res => {
                    if (res.ok) {
                        alert("✅ 삭제되었습니다!");
                        row.remove();
                    } else {
                        alert("❌ 삭제 실패");
                    }
                })
                .catch(err => {
                    console.error("❌ 삭제 중 오류 발생:", err);
                    alert("❌ 서버 오류가 발생했습니다.");
                });
        }
    });



    // ✅ 셀 더블클릭 시 수정 (자격증 + 구직 상태 드롭다운 포함)
    document.addEventListener("dblclick", function (event) {
        const target = event.target;

        if (!target.matches("td") || target.querySelector("input, select")) return;

        const row = target.closest("tr");
        const section = target.closest(".history-section");
        const sectionId = section?.id;
        const colIndex = target.cellIndex;
        const originalText = target.textContent.trim();

        const editableMap = {
            "career-history": {
                editableCols: [1, 2, 3],
                dateCols: [3],
                selectCols: []
            },
            "job-history": {
                editableCols: [1, 2, 3, 4, 5],
                dateCols: [2, 3],
                selectCols: [1] // ✅ 상태만 select로
            }
        };

        if (!editableMap[sectionId] || !editableMap[sectionId].editableCols.includes(colIndex)) return;

        // ✅ 종료일(3번째 셀)인데 상태가 '재직'이면 수정 금지
        if (sectionId === "job-history" && colIndex === 3) {
            const statusCell = row.querySelector("td:nth-child(2)");
            const statusText = statusCell?.textContent.trim();
            if (statusText === "재직") return;
        }


        const isDate = editableMap[sectionId].dateCols.includes(colIndex);
        const isSelect = editableMap[sectionId].selectCols.includes(colIndex);

        let input;

        if (isSelect) {
            input = document.createElement("select");
            ["재직", "퇴직"].forEach(opt => {
                const option = document.createElement("option");
                option.value = opt;
                option.textContent = opt;
                if (opt === originalText) option.selected = true;
                input.appendChild(option);
            });
        } else {
            input = document.createElement("input");
            input.type = isDate ? "date" : "text";
            input.value = isDate && !isNaN(Date.parse(originalText))
                ? new Date(originalText).toISOString().split("T")[0]
                : originalText;
        }

        input.className = "editable-input";
        input.onblur = () => {
            target.textContent = input.value.trim() || originalText;
        };
        input.onkeydown = (e) => {
            if (e.key === "Enter") input.blur();
        };

        target.innerHTML = "";
        target.appendChild(input);
        input.focus();
    });

    // ✅ 기본 탭 표시
    const defaultSection = document.querySelector(".tab-item.active")?.getAttribute("data-target");
    if (defaultSection) {
        document.querySelectorAll(".history-section").forEach(sec => sec.style.display = "none");
        const section = document.getElementById(defaultSection);
        if (section) section.style.display = "block";
    }
    loadJobHistories();
    loadEducations();
    loadContests();
});

// ✅ 구직 내역 초기 로딩 (GET 요청)
function loadJobHistories() {
    const tbody = document.querySelector("#job-history .history-table tbody");

    // ✅ 기존의 .new-entry-row 백업
    const templateRow = tbody.querySelector(".new-entry-row");

    // ✅ tbody 안을 비우되 템플릿은 살려둠
    tbody.innerHTML = "";

    // ✅ 템플릿 다시 추가
    if (templateRow) {
        tbody.appendChild(templateRow);
        templateRow.style.display = "none"; // 템플릿은 안보이게 유지
    }



    fetch("/api/job-history", {
        headers: { [getCsrfHeader()]: getCsrfToken() }
    })
        .then(response => response.json())
        .then(data => {
            data.forEach((item, index) => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${index + 1}</td>
                    <td>${item.status}</td>
                    <td>${item.startDate || ""}</td>
                    <td>${item.endDate || ""}</td>
                    <td>${item.workplace || ""}</td>
                    <td>${item.jobTitle || ""}</td>
                    <td><button class="delete-btn" data-id="${item.id}">삭제</button></td>
                `;
                tbody.appendChild(row);
            });
        });
}

// ✅ 여기서부터 loadEducations() 시작!
function loadEducations() {
    const tbody = document.querySelector("#school-history .history-table tbody");
    const templateRow = tbody.querySelector(".new-entry-row");

    tbody.innerHTML = "";
    if (templateRow) {
        tbody.appendChild(templateRow);
        templateRow.style.display = "none";
    }

    fetch("/api/education-history/list", {
        headers: { [getCsrfHeader()]: getCsrfToken() }
    })
        .then(res => res.json())
        .then(data => {
            data.forEach((edu, index) => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${index + 1}</td>
                    <td>${edu.schoolName}</td>
                    <td>${edu.majorName}</td> 
                    <td>${edu.status}</td>
                    <td>${edu.startDate}</td>
                    <td>${edu.endDate}</td>
                    <td><button class="delete-btn" data-id="${edu.id}">삭제</button></td>
                `;
                tbody.insertBefore(row, templateRow);
            });
        })
        .catch(err => console.error("❌ 학력 불러오기 실패:", err));
}

// ✅ 새 경력 추가 시 서버로 POST 요청
document.addEventListener("click", function (event) {
    if (
        event.target.classList.contains("add-project-btn") &&
        event.target.closest("#job-history")
    ) {
        const section = event.target.closest(".history-section");
        const newRow = section.querySelector(".new-entry-row").cloneNode(true);
        newRow.style.display = "table-row";
        newRow.classList.remove("new-entry-row");

        updateEndDateState(newRow); // 새로 만들자마자 상태 검사해서 종료일 비활성화


        const inputs = newRow.querySelectorAll("input, select");

        // ✅ 입력 후 자동 저장
        inputs.forEach(input => {
            input.addEventListener("change", function () {
                if (input.classList.contains("status-select")) {
                    updateEndDateState(newRow); // 상태 변경될 때마다 실행
                }
                const status = newRow.querySelector(".status-select").value;
                const startDate = newRow.querySelector(".start-date").value;
                const workplace = newRow.querySelector(".workplace").value;
                const jobTitle = newRow.querySelector(".job-title").value;

                let endDate = null;
                if (status !== "재직") {
                    endDate = newRow.querySelector(".end-date").value;
                }


                // 유효성 검사 (근무지, 직무는 있어야 저장)
                if (!workplace || !jobTitle) return;

                const data = {
                    status,
                    startDate,
                    endDate,
                    workplace,
                    jobTitle
                };

                fetch("/api/job-history", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        [getCsrfHeader()]: getCsrfToken()
                    },
                    body: JSON.stringify(data)
                })
                    .then(response => response.json())
                    .then(saved => {
                        // 저장 성공 시 ID를 부여하고 삭제버튼에 data-id 설정
                        const delBtn = newRow.querySelector(".delete-btn");
                        delBtn.setAttribute("data-id", saved.id);
                        alert("✅ 저장되었습니다!");

                        loadJobHistories(); // ✅ 새로고침하여 저장한 데이터 포함해 전체 다시 불러오기
                    })
                    .catch(err => console.error("❌ 저장 실패:", err));
            });
        });

        section.querySelector("tbody").appendChild(newRow);
    }
});

// ✅ 구직 내역 수정 시 자동 저장 (기존 row 더블클릭 수정 후 blur 시)
document.addEventListener("blur", function (event) {
    if (event.target.classList.contains("editable-input")) {
        const input = event.target;
        const td = input.closest("td");
        const row = td.closest("tr");
        const id = row.querySelector(".delete-btn")?.getAttribute("data-id");

        if (!id) return;

        // 현재 row에서 값 읽기
        const cells = row.querySelectorAll("td");
        const status = cells[1].textContent.trim();
        const startDate = cells[2].textContent.trim();
        const workplace = cells[4].textContent.trim();
        const jobTitle = cells[5].textContent.trim();
        let endDate = null;
        if (status !== "재직") {
            endDate = cells[3].textContent.trim();
        }


        const data = {
            status,
            startDate,
            endDate,
            workplace,
            jobTitle
        };

        fetch(`/api/job-history/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                [getCsrfHeader()]: getCsrfToken()
            },
            body: JSON.stringify(data)
        })
            .then(res => res.ok && console.log("자동 저장 완료"))
            .catch(err => console.error("자동 저장 실패", err));
    }
}, true); // useCapture: true

// ✅ 상태에 따라 종료일 비활성화 함수
function updateEndDateState(row) {
    const statusSelect = row.querySelector(".status-select");
    const endDateInput = row.querySelector(".end-date");

    if (!statusSelect || !endDateInput) return;

    if (statusSelect.value === "재직") {
        endDateInput.value = "";
        endDateInput.disabled = true;
        endDateInput.placeholder = "재직 중";
    } else {
        endDateInput.disabled = false;
        endDateInput.placeholder = "YYYY-MM-DD";
    }
}

// ✅ 기본 사항 추가
document.addEventListener("click", function (e) {
    if (e.target.closest("#simple-history")?.classList.contains("history-section") &&
        e.target.classList.contains("add-project-btn")) {

        const section = e.target.closest(".history-section");
        const newRow = section.querySelector(".new-entry-row").cloneNode(true);
        newRow.style.display = "table-row";
        newRow.classList.remove("new-entry-row");

        const inputs = newRow.querySelectorAll("input");

        let isSaved = false; // 중복 저장 방지

        inputs.forEach(input => {
            input.addEventListener("change", () => {
                if (isSaved) return;

                const data = {
                    name: newRow.querySelector(".input-name").value.trim(),
                    birthDate: newRow.querySelector(".input-birth").value.trim(),
                    region: newRow.querySelector(".input-region").value.trim(),
                    email: newRow.querySelector(".input-email").value.trim(),
                    phone: newRow.querySelector(".input-phone").value.trim()
                };

                // ✅ 모든 항목이 입력되었을 때만 저장
                const allFilled = data.name && data.birthDate && data.region && data.email && data.phone;
                if (!allFilled) return;

                fetch("/api/basic-info", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        [getCsrfHeader()]: getCsrfToken()
                    },
                    body: JSON.stringify(data)
                })
                    .then(res => res.json())
                    .then(saved => {
                        isSaved = true;
                        newRow.querySelector(".delete-btn").setAttribute("data-id", saved.id);
                        alert("✅ 기본 정보 저장됨");
                    })
                    .catch(err => {
                        console.error("❌ 저장 실패:", err);
                        alert("❌ 저장 실패");
                    });
            });
        });

        section.querySelector("tbody").appendChild(newRow);
    }
});

// ✅ 기본 정보 삭제 (simple-history 섹션)
document.addEventListener("click", function (event) {
    if (event.target.classList.contains("delete-btn") &&
        event.target.closest("#simple-history")) {

        const row = event.target.closest("tr");
        const id = event.target.getAttribute("data-id");

        if (!id) {
            row.remove(); // 새로 추가된 미저장 행이면 그냥 제거
            return;
        }

        if (!confirm("정말 삭제하시겠습니까?")) return;

        fetch(`/api/basic-info/${id}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
                [getCsrfHeader()]: getCsrfToken()
            }
        })
            .then(res => {
                if (res.ok) {
                    alert("✅ 삭제되었습니다!");
                    row.remove();
                } else {
                    alert("❌ 삭제 실패");
                }
            })
            .catch(err => {
                console.error("❌ 오류 발생:", err);
                alert("❌ 서버 오류가 발생했습니다.");
            });
    }
});



// ✅ 학력 추가
document.addEventListener("click", function (e) {
    if (e.target.closest("#school-history")?.classList.contains("history-section") &&
        e.target.classList.contains("add-project-btn")) {

        const section = e.target.closest(".history-section");
        const newRow = section.querySelector(".new-entry-row").cloneNode(true);
        newRow.style.display = "table-row";
        newRow.classList.remove("new-entry-row");

        const inputs = newRow.querySelectorAll("input, select");

        // ✅ 한번 저장된 row 중복 저장 방지
        let isSaved = false;

        inputs.forEach(input => {
            input.addEventListener("change", () => {
                if (isSaved) return; // 중복 저장 방지

                const data = {
                    schoolName: newRow.querySelector(".input-school").value.trim(),
                    majorName: newRow.querySelector(".input-major").value.trim(),
                    status: newRow.querySelector(".input-status").value,
                    startDate: newRow.querySelector(".input-start").value,
                    endDate: newRow.querySelector(".input-end").value
                };

                // ✅ 모든 항목이 채워져 있을 때만 저장
                const allFilled = data.schoolName && data.majorName && data.status && data.startDate && data.endDate;
                if (!allFilled) return;

                fetch("/api/education-history/save", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        [getCsrfHeader()]: getCsrfToken()
                    },
                    body: JSON.stringify(data)
                })
                    .then(res => res.json())
                    .then(saved => {
                        isSaved = true;
                        newRow.querySelector(".delete-btn").setAttribute("data-id", saved.id);
                        alert("✅ 학력 저장됨");

                        loadEducations(); // ✅ 이 줄 추가!

                    })
                    .catch(err => {
                        console.error("❌ 저장 실패:", err);
                        alert("❌ 저장 실패");
                    });
            });
        });

        section.querySelector("tbody").appendChild(newRow);
    }


});

// ✅ 개월 수 계산 함수
function calculateMonths(startDate, endDate) {
    if (!startDate) return "";
    const start = new Date(startDate);
    const end = endDate ? new Date(endDate) : new Date(); // 종료일 없으면 오늘 기준
    let months = (end.getFullYear() - start.getFullYear()) * 12;
    months += end.getMonth() - start.getMonth();
    if (end.getDate() < start.getDate()) months -= 1; // 일자 차이 보정
    return months >= 0 ? months : 0;
}

// ✅ 구직 내역 초기 로딩 (GET 요청)
function loadJobHistories() {
    const tbody = document.querySelector("#job-history .history-table tbody");
    const templateRow = tbody.querySelector(".new-entry-row");

    tbody.innerHTML = "";
    if (templateRow) {
        tbody.appendChild(templateRow);
        templateRow.style.display = "none";
    }

    fetch("/api/job-history", {
        headers: { [getCsrfHeader()]: getCsrfToken() }
    })
        .then(response => response.json())
        .then(data => {
            data.forEach((item, index) => {
                const months = calculateMonths(item.startDate, item.endDate);

                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${index + 1}</td>
                    <td>${item.status}</td>
                    <td>${months}개월</td> <!-- ✅ 계산된 개월 수 -->
                    <td>${item.startDate || ""}</td>
                    <td>${item.endDate || (item.status === "재직" ? "현재" : "")}</td>
                    <td>${item.workplace || ""}</td>
                    <td>${item.jobTitle || ""}</td>
                    <td><button class="delete-btn" data-id="${item.id}">삭제</button></td>
                `;
                tbody.appendChild(row);
            });
        });
}

// ✅ 공모전 추가 버튼 클릭 시 모달 열기
document.addEventListener("click", function (e) {
    const btn = e.target.closest("#contest-add-btn"); // id 기반으로 탐색
    if (!btn) return;

    console.log("✅ contest-add-btn 클릭됨");

    const modal = document.getElementById("contest-award-modal");
    if (!modal) {
        console.error("❌ contest-award-modal 요소 없음!");
        return;
    }

    modal.style.display = "flex"; // 모달 열기
    goContestStep(1); // Step 1부터 시작
});

// ✅ 공모전 정정 요청 모달 처리
(function(){
    const corrModal = document.getElementById("award-correction-modal");
    if (!corrModal) return;

    const corrCloseBtns = corrModal.querySelectorAll(".award-correction-close, .award-correction-cancel");
    const corrSendBtn = corrModal.querySelector(".award-correction-send");
    const corrCurrentGrade = corrModal.querySelector("#corr-current-grade");
    const corrCurrentOrg = corrModal.querySelector("#corr-current-org");
    const corrRaw = corrModal.querySelector("#corr-raw-text");
    const corrMsg = corrModal.querySelector("#corr-message");

    // ✅ 정정요청 열기 버튼 (.award-correct) 눌렀을 때
    document.addEventListener("click", function(e){
        const btn = e.target.closest(".award-correct");
        if (!btn) return;

        corrCurrentGrade.value = document.querySelector("#contest-grade")?.value || "";
        corrCurrentOrg.value = document.querySelector("#contest-organizer")?.value || "";
        corrRaw.value = document.querySelector("#contest-award-raw-text")?.value || "";
        corrMsg.value = "";

        corrModal.style.display = "flex";
    });

    // 닫기
    corrCloseBtns.forEach(b => b.addEventListener("click", () => corrModal.style.display = "none"));

    // 전송
    corrSendBtn.addEventListener("click", async () => {
        const message = corrMsg.value.trim();
        if (!message) {
            alert("정정 요청 내용을 입력해주세요.");
            return;
        }
        try {
            const fd = new FormData();
            const fileInput = document.querySelector("#contest-award-file");
            if (fileInput?.files[0]) {
                fd.append("file", fileInput.files[0], fileInput.files[0].name);
            }
            fd.append("ocrText", document.querySelector("#contest-award-raw-text")?.value || "");
            fd.append("parsedGrade", document.querySelector("#contest-grade")?.value || "");
            fd.append("parsedOrganizer", document.querySelector("#contest-organizer")?.value || "");
            fd.append("message", message);

            const res = await fetch("/api/contest-history/corrections", {
                method: "POST",
                headers: { [getCsrfHeader()]: getCsrfToken() },
                body: fd
            });
            if (!res.ok) throw new Error("정정 요청 실패");

            alert("정정 요청이 접수되었습니다.");
            corrModal.style.display = "none";
        } catch(err) {
            console.error("❌ 정정 요청 에러:", err);
            alert("정정 요청 중 오류가 발생했습니다.");
        }
    });
})();

    // OCR 완료 후 값 반영 시
    document.querySelector(".contest-org").value = recognizedOrg;
    document.querySelector(".contest-org").readOnly = true;

    document.querySelector(".contest-grade").value = recognizedGrade;
    document.querySelector(".contest-grade").readOnly = true;



