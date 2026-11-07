(function () {
  "use strict";

  let previousFocus = null;
  let filterFrame = 0;

  function firstFocusable(container) {
    return container.querySelector('button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])');
  }

  window.showModal = function (id) {
    const modal = document.getElementById(id);
    if (!modal) return;
    previousFocus = document.activeElement;
    modal.classList.remove("hidden");
    modal.setAttribute("aria-hidden", "false");
    document.body.classList.add("modal-open");
    const focusTarget = firstFocusable(modal);
    if (focusTarget) focusTarget.focus({preventScroll: true});
  };

  window.hideModal = function (id) {
    const modal = document.getElementById(id);
    if (!modal) return;
    modal.classList.add("hidden");
    modal.setAttribute("aria-hidden", "true");
    document.body.classList.remove("modal-open");
    if (previousFocus && typeof previousFocus.focus === "function") previousFocus.focus({preventScroll: true});
  };

  window.closeToast = function () {
    const toast = document.getElementById("toast");
    if (!toast) return;
    toast.classList.add("toast-leaving");
    window.setTimeout(function () { toast.remove(); }, 260);
  };

  window.filterTable = function (query, tableId) {
    window.cancelAnimationFrame(filterFrame);
    filterFrame = window.requestAnimationFrame(function () {
      const needle = String(query || "").trim().toLocaleLowerCase("vi");
      document.querySelectorAll("#" + tableId + " tbody tr").forEach(function (row) {
        row.hidden = needle !== "" && !row.textContent.toLocaleLowerCase("vi").includes(needle);
      });
    });
  };

  function lockSubmittingForms() {
    document.querySelectorAll('form[method="post"], form[method="POST"]').forEach(function (form) {
      form.addEventListener("submit", function (event) {
        const confirmation = form.dataset.confirm;
        if (confirmation && !window.confirm(confirmation)) {
          event.preventDefault();
          return;
        }
        if (!form.checkValidity() || form.dataset.submitting === "true") return;
        form.dataset.submitting = "true";
        form.setAttribute("aria-busy", "true");
        form.querySelectorAll('button[type="submit"], input[type="submit"]').forEach(function (button) {
          button.disabled = true;
          if (button.tagName === "BUTTON") button.textContent = "Đang xử lý...";
          else button.value = "Đang xử lý...";
        });
      });
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    lockSubmittingForms();
    if (document.getElementById("toast")) window.setTimeout(window.closeToast, 4200);

    const clock = document.getElementById("timeDisplay");
    if (clock) {
      const tick = function () {
        const now = new Date();
        clock.textContent = now.toLocaleTimeString("vi-VN", {hour: "2-digit", minute: "2-digit"})
          + " · " + now.toLocaleDateString("vi-VN");
      };
      tick();
      window.setInterval(tick, 30000);
    }

    document.querySelectorAll(".modal").forEach(function (modal) {
      modal.setAttribute("role", "dialog");
      modal.setAttribute("aria-modal", "true");
      modal.setAttribute("aria-hidden", String(modal.classList.contains("hidden")));
      modal.addEventListener("click", function (event) {
        if (event.target === modal) window.hideModal(modal.id);
      });
      modal.addEventListener("keydown", function (event) {
        if (event.key !== "Tab") return;
        const focusable = Array.from(modal.querySelectorAll('button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'));
        if (!focusable.length) return;
        const first = focusable[0];
        const last = focusable[focusable.length - 1];
        if (event.shiftKey && document.activeElement === first) {
          event.preventDefault();
          last.focus();
        } else if (!event.shiftKey && document.activeElement === last) {
          event.preventDefault();
          first.focus();
        }
      });
    });

    document.addEventListener("keydown", function (event) {
      if (event.key !== "Escape") return;
      const modal = document.querySelector(".modal:not(.hidden)");
      if (modal) window.hideModal(modal.id);
    });
  });
})();
