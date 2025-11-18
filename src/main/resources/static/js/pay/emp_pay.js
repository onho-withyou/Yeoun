/** 공통 셀렉터 */
const $ = id => document.getElementById(id);

/** PDF 다운로드 */
document.addEventListener("DOMContentLoaded", () => {
  const printBtn = $("printBtn");
  if (printBtn) {
    printBtn.onclick = () => window.print();
  }

  /** Drawer 열기 / 닫기 */
  const drawer = $("drawer");
  const openBtn = $("openInquiryBtn");
  const cancelBtn = $("cancelInquiry");

  if (openBtn) openBtn.onclick = () => drawer.classList.add("open");
  if (cancelBtn) cancelBtn.onclick = () => drawer.classList.remove("open");

  /** 월 이동 버튼 */
  const prevBtn = $("prevMonthBtn");
  const nextBtn = $("nextMonthBtn");
  const monthInput = $("month");

  if (prevBtn && nextBtn && monthInput) {

    prevBtn.onclick = () => {
      const m = new Date(monthInput.value + "-01");
      m.setMonth(m.getMonth() - 1);
      monthInput.value = m.toISOString().substring(0, 7);

      moveMonth(m);
    };

    nextBtn.onclick = () => {
      const m = new Date(monthInput.value + "-01");
      m.setMonth(m.getMonth() + 1);
      monthInput.value = m.toISOString().substring(0, 7);

      moveMonth(m);
    };
  }
});

/** 월 이동 시 서버 재호출 */
function moveMonth(dateObj) {
  const y = dateObj.getFullYear();
  const m = (dateObj.getMonth() + 1).toString().padStart(2, "0");

  const yymm = `${y}${m}`;   // yyyyMM

  location.href = `/pay/emp_pay?yymm=${yymm}`;
}
