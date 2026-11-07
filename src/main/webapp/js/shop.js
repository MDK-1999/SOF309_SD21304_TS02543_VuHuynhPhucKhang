(function () {
  'use strict';

  function csrfToken() {
    const meta = document.querySelector('meta[name="csrf-token"]');
    return meta ? meta.content : '';
  }

  function lockSubmittingForms() {
    document.querySelectorAll('form[method="post"], form[method="POST"]').forEach(function (form) {
      form.addEventListener('submit', function () {
        if (!form.checkValidity() || form.dataset.submitting === 'true') return;
        form.dataset.submitting = 'true';
        form.setAttribute('aria-busy', 'true');
        form.querySelectorAll('button[type="submit"], input[type="submit"]').forEach(function (button) {
          button.disabled = true;
          if (!button.dataset.originalLabel) button.dataset.originalLabel = button.textContent || button.value;
          if (button.tagName === 'BUTTON') button.textContent = 'Đang xử lý...';
          else button.value = 'Đang xử lý...';
        });
      });
    });
  }

  function initializePaymentOptions() {
    const paymentOptions = Array.from(document.querySelectorAll('input[name="paymentMethod"]'));
    const qrBox = document.getElementById('qrBox');
    if (!paymentOptions.length || !qrBox) return;
    const updatePayment = function () {
      paymentOptions.forEach(function (option) {
        const wrapper = option.closest('.payment-option');
        if (wrapper) wrapper.classList.toggle('active', option.checked);
      });
      const selected = paymentOptions.find(function (option) { return option.checked; });
      const open = Boolean(selected && selected.value === 'qr');
      qrBox.classList.toggle('open', open);
      qrBox.setAttribute('aria-hidden', String(!open));
    };
    paymentOptions.forEach(function (option) { option.addEventListener('change', updatePayment); });
    updatePayment();
  }

  document.addEventListener('DOMContentLoaded', function () {
    lockSubmittingForms();
    initializePaymentOptions();

    const panel = document.getElementById('buildChat');
    if (!panel) return;

    const launcher = document.getElementById('chatLauncher');
    const closeButton = document.getElementById('chatClose');
    const prompt = document.getElementById('chatPrompt');
    const body = document.getElementById('chatBody');
    const status = document.getElementById('aiStatus');
    const buttons = Array.from(panel.querySelectorAll('[data-preset]'));
    const localAiForm = document.getElementById('localAiForm');
    const localAiInput = document.getElementById('localAiInput');
    const localAiSend = document.getElementById('localAiSend');
    const contextPath = panel.dataset.contextPath || '';
    const storageKey = 'pcshop-build-picker-history-v1';
    let busy = false;
    let previousFocus = null;

    function setOpen(open) {
      panel.classList.toggle('open', open);
      panel.setAttribute('aria-hidden', String(!open));
      if (launcher) launcher.setAttribute('aria-expanded', String(open));
      if (prompt) prompt.classList.toggle('hidden', open);
      if (open) {
        previousFocus = document.activeElement;
        body.scrollTop = body.scrollHeight;
        if (closeButton) closeButton.focus({preventScroll: true});
      } else if (previousFocus && typeof previousFocus.focus === 'function') {
        previousFocus.focus({preventScroll: true});
      }
    }

    function setStatus(text, mode) {
      if (!status) return;
      status.textContent = text;
      status.className = 'ai-status ' + (mode || '');
    }

    function appendMessage(text, type, persist) {
      const message = document.createElement('div');
      message.className = type === 'user' ? 'user-msg' : 'bot-msg';
      message.textContent = String(text || '');
      body.appendChild(message);
      body.scrollTop = body.scrollHeight;
      if (persist !== false) saveMessage(text, type);
      return message;
    }

    function appendThinking() {
      const message = document.createElement('div');
      message.className = 'bot-msg thinking-msg';
      message.setAttribute('role', 'status');
      message.setAttribute('aria-label', 'Đang ghép cấu hình từ catalog');
      message.innerHTML = '<span></span><span></span><span></span>';
      body.appendChild(message);
      body.scrollTop = body.scrollHeight;
      return message;
    }

    function saveMessage(text, type) {
      try {
        const history = JSON.parse(sessionStorage.getItem(storageKey) || '[]');
        history.push({text: String(text || '').slice(0, 7000), type: type});
        sessionStorage.setItem(storageKey, JSON.stringify(history.slice(-8)));
      } catch (ignore) {
        // The configurator remains usable when sessionStorage is unavailable.
      }
    }

    function restoreHistory() {
      try {
        const history = JSON.parse(sessionStorage.getItem(storageKey) || '[]');
        history.forEach(function (item) {
          if (item && (item.type === 'user' || item.type === 'bot')) appendMessage(item.text, item.type, false);
        });
      } catch (ignore) {
        sessionStorage.removeItem(storageKey);
      }
    }

    function setBusy(value) {
      busy = value;
      panel.setAttribute('aria-busy', String(value));
      buttons.forEach(function (button) { button.disabled = value; });
      if (localAiInput) localAiInput.disabled = value;
      if (localAiSend) localAiSend.disabled = value;
    }

    async function requestPreset(button) {
      if (busy) return;
      const preset = button.dataset.preset || '';
      const question = button.dataset.question || button.textContent.trim();
      if (!preset) return;

      setOpen(true);
      setBusy(true);
      appendMessage(question, 'user');
      const thinking = appendThinking();
      setStatus('Đang ghép linh kiện từ catalog...', 'working');

      const controller = new AbortController();
      const timeout = window.setTimeout(function () { controller.abort(); }, 30_000);
      try {
        const response = await fetch(contextPath + '/ai-chat', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
            'X-CSRF-Token': csrfToken()
          },
          body: new URLSearchParams({preset: preset, message: question}),
          signal: controller.signal
        });
        if (!response.ok) {
          if (response.status === 429) throw new Error('RATE_LIMIT');
          throw new Error('HTTP_' + response.status);
        }
        const data = await response.json();
        if (data.mode !== 'preset' || !data.answer) throw new Error('INVALID_RESPONSE');
        thinking.remove();
        appendMessage(data.answer, 'bot');
        setStatus('Đã random 1 trong 7 phương án', 'preset');
      } catch (error) {
        thinking.remove();
        const rateLimited = error && error.message === 'RATE_LIMIT';
        appendMessage(rateLimited
          ? 'Bạn thao tác hơi nhanh. Vui lòng chờ một phút rồi chọn lại cấu hình.'
          : 'Không tải được catalog từ máy chủ. Hãy kiểm tra Tomcat và thử lại.', 'bot');
        setStatus(rateLimited ? 'Tạm giới hạn yêu cầu' : 'Chưa kết nối được catalog', 'local');
      } finally {
        window.clearTimeout(timeout);
        setBusy(false);
      }
    }

    async function requestLocalAi(question) {
      if (busy || !question) return;
      setOpen(true);
      setBusy(true);
      appendMessage(question, 'user');
      const thinking = appendThinking();
      setStatus('AI local đang phân tích catalog...', 'working');

      const controller = new AbortController();
      const timeout = window.setTimeout(function () { controller.abort(); }, 100_000);
      try {
        const response = await fetch(contextPath + '/ai-chat', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
            'X-CSRF-Token': csrfToken()
          },
          body: new URLSearchParams({message: question}),
          signal: controller.signal
        });
        if (!response.ok) {
          if (response.status === 429) throw new Error('RATE_LIMIT');
          throw new Error('HTTP_' + response.status);
        }
        const data = await response.json();
        if (!data.answer) throw new Error('INVALID_RESPONSE');
        thinking.remove();
        appendMessage(data.answer, 'bot');
        if (data.mode === 'local-ai') setStatus('AI local đang hoạt động', 'local-ai');
        else setStatus('Đang dùng tư vấn nội bộ', 'local');
      } catch (error) {
        thinking.remove();
        const rateLimited = error && error.message === 'RATE_LIMIT';
        appendMessage(rateLimited
          ? 'Bạn gửi hơi nhanh. Vui lòng chờ một phút rồi thử lại.'
          : 'Chưa kết nối được LM Studio. Hãy tải model, bật Local Server ở cổng 1234 rồi thử lại.', 'bot');
        setStatus(rateLimited ? 'Tạm giới hạn yêu cầu' : 'LM Studio chưa sẵn sàng', 'local');
      } finally {
        window.clearTimeout(timeout);
        setBusy(false);
        if (localAiInput) localAiInput.focus({preventScroll: true});
      }
    }
    if (launcher) launcher.addEventListener('click', function () { setOpen(!panel.classList.contains('open')); });
    if (closeButton) closeButton.addEventListener('click', function () { setOpen(false); });
    buttons.forEach(function (button) {
      button.addEventListener('click', function () { requestPreset(button); });
    });
    if (localAiForm) {
      localAiForm.addEventListener('submit', function (event) {
        event.preventDefault();
        const question = localAiInput ? localAiInput.value.trim() : '';
        if (!question) {
          if (localAiInput) localAiInput.focus();
          return;
        }
        localAiInput.value = '';
        requestLocalAi(question);
      });
    }
    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape' && panel.classList.contains('open')) setOpen(false);
    });

    restoreHistory();
  });
})();
