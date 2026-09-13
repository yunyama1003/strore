"use strict";
document.querySelectorAll("form[data-confirm]").forEach((form) => {
    form.addEventListener("submit", (event) => {
        if (!window.confirm(form.dataset.confirm)) event.preventDefault();
    });
});

const copyButton = document.getElementById("copy-list");
if (copyButton) {
    copyButton.addEventListener("click", async () => {
        const text = document.getElementById("shopping-text");
        const details = document.getElementById("copy-details");
        const status = document.getElementById("copy-status");
        try {
            if (!navigator.clipboard || !window.isSecureContext) throw new Error("manual");
            await navigator.clipboard.writeText(text.value);
            status.textContent = "コピーしました。買い物用のメモに貼り付けてください。";
        } catch {
            // Home Wi-Fi uses HTTP, where the Clipboard API may be unavailable.
            details.open = true;
            text.focus();
            text.select();
            text.setSelectionRange(0, text.value.length);
            let copied = false;
            try { copied = document.execCommand("copy"); } catch { /* manual selection stays available */ }
            status.textContent = copied
                ? "コピーしました。買い物用のメモに貼り付けてください。"
                : "リストを選択しました。端末の「コピー」操作を使ってください。";
        }
    });
}
