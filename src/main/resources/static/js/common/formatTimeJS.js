
const now = new Date();
const year = now.getFullYear(); // YYYY
const month = String(now.getMonth() + 1).padStart(2, "0")   // MM
const day = String(now.getDate()).padStart(2, "0")  // DD
let hours = now.getHours(); // 0~23
const minutes = String(now.getMinutes()).padStart(2, "0");
const ampm = hours >= 12 ? "오후" : "오전";
hours = hours % 12 || 12; // 0 → 12


// 2025-11-22 오후 5:11
function formatTimeFull() {
    return `${year}-${month}-${day} ${ampm} ${hours}:${minutes}`;
}

// 오후 5:11
function formatTimeOnly() {
    return `${ampm} ${hours}:${minutes}`;
}
