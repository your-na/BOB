function goBack() {
    window.location.href = "/chatting"; // 또는 location.href = "/chatlist"; 처럼 경로 지정도 가능
}

document.addEventListener("DOMContentLoaded", function () {
    const input = document.getElementById("chat-input");
    const chatBox = document.querySelector('.chat-box');
    chatBox.scrollTop = chatBox.scrollHeight;

    document.querySelector(".send-button").addEventListener("click", sendMessage);
    input.addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
            sendMessage();
        }
    });

    function sendMessage() {
        const text = input.value.trim();
        if (text !== "") {
            const message = document.createElement("div");
            message.classList.add("message", "user");
            message.textContent = text;
            chatBox.appendChild(message);
            input.value = "";
            chatBox.scrollTop = chatBox.scrollHeight;
        }
    }

});
