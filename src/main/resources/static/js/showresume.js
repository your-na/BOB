// ✅ 사용자 정보 동적 렌더링
fetch("/api/user/resumes/me")
    .then(res => res.json())
    .then(user => {
        if (!user) return;

        document.getElementById("profileImage").src = user.profileImageUrl || "/images/user.png";
        document.getElementById("userName").textContent = user.userName || "이름 없음";
        document.getElementById("mainLanguage").textContent = user.mainLanguage || "";
        document.getElementById("sex").textContent = user.sex || "";
        document.getElementById("birthday").textContent = user.birthday || "";
        document.getElementById("phone").textContent = user.userPhone || "";
        document.getElementById("email").textContent = user.userEmail || "";
        document.getElementById("region").textContent = user.region || "";

    })
    .catch(err => console.error("내 정보 불러오기 실패:", err));