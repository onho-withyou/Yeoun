document.addEventListener('DOMContentLoaded', () => {

  // ============================
  // 항목 코드 자동 대문자 변환
  // ============================
  document.addEventListener('input', function (e) {
    if (e.target.matches('#createItemModal input[name="itemCode"]')) {
      e.target.value = e.target.value.replace(/\s+/g, '').toUpperCase();
    }
  });

  // ============================
  // Bootstrap Form Validation
  // ============================
  document.addEventListener('submit', function (e) {
    const form = e.target.closest('form.needs-validation');
    if (!form) return;

    if (!form.checkValidity()) {
      e.preventDefault();
      e.stopPropagation();
      form.classList.add('was-validated');
    }
  }, true);

  // ============================
  // 플래시로 전달된 모달 자동 오픈
  // ============================
  const openCreate = window.openItemCreate;
  const openEditId = window.openItemEdit;

  if (openCreate) {
    const m = document.getElementById('createItemModal');
    if (m) new bootstrap.Modal(m).show();
  }

  if (openEditId) {
    const el = document.getElementById('editItemModal-' + openEditId);
    if (el) new bootstrap.Modal(el).show();
  }

});
