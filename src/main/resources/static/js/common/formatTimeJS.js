function formatTimeFull() {
    const now = new Date();
    let hours = now.getHours(); // 0~23
    const minutes = String(now.getMinutes()).padStart(2, "0");
    const ampm = hours >= 12 ? "오후" : "오전";
    hours = hours % 12 || 12; // 0 → 12

    return `${ampm} ${hours}:${minutes}`;
}
