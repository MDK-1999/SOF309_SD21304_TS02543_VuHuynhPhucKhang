(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    const selects = Array.from(document.querySelectorAll('.part-select'));
    if (!selects.length) return;

    const totalEl = document.getElementById('buildTotal');
    const selectedList = document.getElementById('selectedList');
    const advisorBox = document.getElementById('advisorBox');
    const compatibilityBox = document.getElementById('compatibilityBox');
    const compatStatus = document.getElementById('compatStatus');
    const wattStatus = document.getElementById('wattStatus');
    const psuRecommend = document.getElementById('psuRecommend');
    const selectedCount = document.getElementById('selectedCount');
    const addButton = document.getElementById('addBuildButton');
    const resetButton = document.getElementById('resetBuilder');
    const money = new Intl.NumberFormat('vi-VN');
    const storageKey = 'pcshop-builder-selection-v3';
    const slotNames = {1:'CPU',2:'Mainboard',3:'RAM',4:'GPU',5:'SSD',6:'Nguồn',7:'Tản nhiệt',8:'Case',9:'Màn hình',10:'Phụ kiện'};

    function clamp(value, minimum, maximum) {
      return Math.min(Math.max(Number(value) || minimum, minimum), maximum);
    }

    function quantityInput(select) {
      return select.closest('.builder-part').querySelector('[data-part-quantity]');
    }

    function selectedParts() {
      return selects.map(function (select) {
        const option = select.options[select.selectedIndex];
        if (!select.value) return null;
        const stock = Math.max(0, Number(option.dataset.stock || 0));
        const quantity = clamp(quantityInput(select).value, 1, Math.max(1, stock));
        return {
          id: Number(select.value),
          cat: Number(select.dataset.category),
          label: slotNames[select.dataset.category] || 'Linh kiện',
          name: option.dataset.name || option.textContent.trim(),
          brand: option.dataset.brand || '',
          model: option.dataset.model || '',
          specs: option.dataset.specs || '',
          image: option.dataset.image || '',
          price: Number(option.dataset.price || 0),
          stock: stock,
          quantity: quantity,
          subtotal: Number(option.dataset.price || 0) * quantity
        };
      }).filter(Boolean);
    }

    function byCategory(parts, category) {
      return parts.find(function (part) { return part.cat === category; });
    }

    function updatePreview(select) {
      const card = select.closest('.builder-part');
      const preview = card.querySelector('[data-part-preview]');
      const price = card.querySelector('[data-part-price]');
      const actions = card.querySelector('[data-part-actions]');
      const quantity = card.querySelector('[data-part-quantity]');
      const option = select.options[select.selectedIndex];
      preview.textContent = '';

      if (select.value && option.dataset.image) {
        const stock = Math.max(0, Number(option.dataset.stock || 0));
        const currentQuantity = clamp(quantity.value, 1, Math.max(1, stock));
        quantity.max = String(Math.max(1, stock));
        quantity.value = String(currentQuantity);
        quantity.disabled = stock < 1;
        actions.hidden = false;

        const image = document.createElement('img');
        image.src = option.dataset.image;
        image.alt = option.dataset.name || 'Linh kiện đã chọn';
        image.width = 128;
        image.height = 104;
        preview.appendChild(image);
        price.textContent = money.format(Number(option.dataset.price || 0)) + 'đ × '
          + currentQuantity + ' = ' + money.format(Number(option.dataset.price || 0) * currentQuantity) + 'đ';
        card.classList.add('selected');
      } else {
        const label = document.createElement('span');
        label.textContent = slotNames[select.dataset.category] || 'PC';
        preview.appendChild(label);
        quantity.value = '1';
        quantity.max = '1';
        quantity.disabled = false;
        actions.hidden = true;
        price.textContent = 'Chưa chọn';
        card.classList.remove('selected');
      }
    }

    function socketOf(text) {
      const match = String(text || '').toUpperCase().match(/\b(AM4|AM5|LGA\s?\d{4})\b/);
      return match ? match[1].replace(/\s/g, '') : '';
    }

    function ddrOf(text) {
      const match = String(text || '').toUpperCase().match(/\bDDR([45])\b/);
      return match ? 'DDR' + match[1] : '';
    }

    function cpuPower(part) {
      if (!part) return 0;
      const text = (part.name + ' ' + part.specs).toUpperCase();
      const explicit = text.match(/TDP\s*(\d{2,3})\s*W/);
      let watts = 90;
      if (explicit) watts = Number(explicit[1]);
      else if (/9950|7950|14900|13900|285K|ULTRA 9/.test(text)) watts = 190;
      else if (/9900|7900|14700|13700|265K|ULTRA 7|RYZEN 9/.test(text)) watts = 150;
      else if (/X3D|RYZEN 7|CORE I7/.test(text)) watts = 120;
      return watts * part.quantity;
    }

    function gpuPower(part) {
      if (!part) return 0;
      const text = (part.name + ' ' + part.model).toUpperCase();
      let watts = 210;
      if (/5090/.test(text)) watts = 575;
      else if (/4090/.test(text)) watts = 450;
      else if (/5080|7900 XTX/.test(text)) watts = 360;
      else if (/4080|3090|3080/.test(text)) watts = 330;
      else if (/5070 TI|4070 TI|7900 GRE|7800 XT/.test(text)) watts = 285;
      else if (/5070|4070|7700 XT|9070/.test(text)) watts = 235;
      else if (/5060 TI|4060 TI|6700 XT/.test(text)) watts = 180;
      else if (/5060|4060|7600|6600/.test(text)) watts = 135;
      return watts * part.quantity;
    }

    function psuCapacity(part) {
      if (!part) return 0;
      const match = (part.name + ' ' + part.specs).toUpperCase().match(/(\d{3,4})\s*W/);
      return match ? Number(match[1]) : 0;
    }

    function evaluate(parts) {
      const issues = [];
      const cpu = byCategory(parts, 1);
      const board = byCategory(parts, 2);
      const ram = byCategory(parts, 3);
      const gpu = byCategory(parts, 4);
      const psu = byCategory(parts, 6);
      const cpuSocket = socketOf(cpu && (cpu.specs + ' ' + cpu.name));
      const boardSocket = socketOf(board && (board.specs + ' ' + board.name));
      const ramDdr = ddrOf(ram && (ram.specs + ' ' + ram.name));
      const boardDdr = ddrOf(board && (board.specs + ' ' + board.name));

      if (cpu && board && cpuSocket && boardSocket && cpuSocket !== boardSocket) {
        issues.push('CPU dùng ' + cpuSocket + ' nhưng mainboard dùng ' + boardSocket + '.');
      }
      if (ram && board && ramDdr && boardDdr && ramDdr !== boardDdr) {
        issues.push('RAM ' + ramDdr + ' không phù hợp mainboard ' + boardDdr + '.');
      }

      const totalQuantity = parts.reduce(function (sum, part) { return sum + part.quantity; }, 0);
      const load = 85 + cpuPower(cpu) + gpuPower(gpu) + (totalQuantity * 7);
      const recommendedPsu = Math.max(550, Math.ceil((load * 1.35) / 50) * 50);
      const capacity = psuCapacity(psu);
      if (psu && capacity && capacity < recommendedPsu) {
        issues.push('Nguồn ' + capacity + 'W thấp hơn mức khuyến nghị ' + recommendedPsu + 'W.');
      }

      const required = [1, 2, 3, 4, 5, 6];
      const missing = required.filter(function (cat) { return !byCategory(parts, cat); })
        .map(function (cat) { return slotNames[cat]; });
      return {issues: issues, missing: missing, load: load, recommendedPsu: recommendedPsu};
    }

    function updateBuilder() {
      selects.forEach(updatePreview);
      const parts = selectedParts();
      const total = parts.reduce(function (sum, part) { return sum + part.subtotal; }, 0);
      const totalQuantity = parts.reduce(function (sum, part) { return sum + part.quantity; }, 0);
      const result = evaluate(parts);

      totalEl.textContent = money.format(total) + 'đ';
      wattStatus.textContent = parts.length ? result.load + 'W' : '0W';
      psuRecommend.textContent = parts.length ? 'Khuyên dùng nguồn từ ' + result.recommendedPsu + 'W' : 'Chưa đủ dữ liệu';
      selectedCount.textContent = totalQuantity + ' sản phẩm';
      addButton.disabled = parts.length === 0;

      if (result.issues.length) {
        compatStatus.textContent = 'Cần kiểm tra';
        compatStatus.className = 'warning';
        compatibilityBox.className = 'compatibility-box warning';
        compatibilityBox.textContent = result.issues.join(' ');
      } else if (result.missing.length) {
        compatStatus.textContent = 'Thiếu linh kiện';
        compatStatus.className = '';
        compatibilityBox.className = 'compatibility-box';
        compatibilityBox.textContent = 'Còn thiếu: ' + result.missing.join(', ') + '.';
      } else {
        compatStatus.textContent = 'Tương thích';
        compatStatus.className = 'success';
        compatibilityBox.className = 'compatibility-box success';
        compatibilityBox.textContent = 'Các chuẩn socket, RAM và công suất nguồn đang phù hợp với dữ liệu sản phẩm.';
      }

      selectedList.textContent = '';
      if (!parts.length) {
        const empty = document.createElement('div');
        empty.className = 'selected-empty';
        empty.textContent = 'Chưa có linh kiện nào được chọn.';
        selectedList.appendChild(empty);
      } else {
        parts.forEach(function (part) {
          const line = document.createElement('div');
          line.className = 'selected-line';
          const image = document.createElement('img');
          image.src = part.image;
          image.alt = part.name;
          const copy = document.createElement('div');
          const label = document.createElement('span');
          label.textContent = part.label + ' · Số lượng ' + part.quantity;
          const name = document.createElement('strong');
          name.textContent = part.name;
          copy.append(label, name);
          const price = document.createElement('b');
          price.textContent = money.format(part.subtotal) + 'đ';
          line.append(image, copy, price);
          selectedList.appendChild(line);
        });
      }

      advisorBox.textContent = advisorText(total, result, parts);
      saveSelection();
    }

    function advisorText(total, result, parts) {
      if (result.issues.length) return 'Hãy xử lý cảnh báo tương thích trước khi đặt cấu hình.';
      if (result.missing.length) return 'Hoàn tất các linh kiện cốt lõi để hệ thống đánh giá chính xác hơn. Bạn vẫn có thể thêm các món đã chọn vào giỏ.';
      const hasMonitor = Boolean(byCategory(parts, 9));
      if (total < 20000000) return 'Cấu hình tiết kiệm, phù hợp học tập, văn phòng và eSports 1080p. Nên ưu tiên RAM 32GB và SSD 1TB.';
      if (total < 40000000) return 'Cấu hình cân bằng cho gaming 1440p và công việc sáng tạo. ' + (hasMonitor ? 'Ngân sách đã gồm màn hình.' : 'Có thể bổ sung màn hình QHD phù hợp.');
      if (total < 70000000) return 'Cấu hình cao cấp cho 1440p/4K, livestream hoặc render. Hãy dùng case airflow tốt và tản phù hợp CPU.';
      return 'Cấu hình hiệu năng rất cao cho 4K, AI hoặc workstation. Nên ưu tiên RAM 64GB trở lên và nguồn chuẩn Gold công suất dư.';
    }

    function saveSelection() {
      try {
        const value = {};
        selects.forEach(function (select) {
          if (!select.value) return;
          value[select.dataset.category] = {
            id: select.value,
            quantity: Number(quantityInput(select).value || 1)
          };
        });
        sessionStorage.setItem(storageKey, JSON.stringify(value));
      } catch (ignore) {
        // Builder remains usable without browser storage.
      }
    }

    function restoreSelection() {
      try {
        const value = JSON.parse(sessionStorage.getItem(storageKey) || '{}');
        selects.forEach(function (select) {
          const saved = value[select.dataset.category];
          const savedId = saved && typeof saved === 'object' ? saved.id : saved;
          if (savedId && Array.from(select.options).some(function (option) { return option.value === String(savedId); })) {
            select.value = String(savedId);
            quantityInput(select).value = String(saved && saved.quantity ? saved.quantity : 1);
          }
        });
      } catch (ignore) {
        sessionStorage.removeItem(storageKey);
      }
    }

    function changeQuantity(button, direction) {
      const card = button.closest('.builder-part');
      const select = card.querySelector('.part-select');
      if (!select.value) return;
      const input = card.querySelector('[data-part-quantity]');
      const maximum = Math.max(1, Number(input.max || 1));
      input.value = String(clamp(Number(input.value || 1) + direction, 1, maximum));
      updateBuilder();
    }

    selects.forEach(function (select) {
      select.addEventListener('change', function () {
        quantityInput(select).value = '1';
        updateBuilder();
      });
    });

    document.querySelectorAll('[data-part-quantity]').forEach(function (input) {
      input.addEventListener('input', updateBuilder);
      input.addEventListener('change', function () {
        input.value = String(clamp(input.value, 1, Math.max(1, Number(input.max || 1))));
        updateBuilder();
      });
    });

    document.querySelectorAll('[data-qty-action]').forEach(function (button) {
      button.addEventListener('click', function () {
        changeQuantity(button, button.dataset.qtyAction === 'increase' ? 1 : -1);
      });
    });

    document.querySelectorAll('[data-remove-part]').forEach(function (button) {
      button.addEventListener('click', function () {
        const card = button.closest('.builder-part');
        card.querySelector('.part-select').value = '';
        card.querySelector('[data-part-quantity]').value = '1';
        updateBuilder();
      });
    });

    document.querySelectorAll('[data-change-part]').forEach(function (button) {
      button.addEventListener('click', function () {
        const select = button.closest('.builder-part').querySelector('.part-select');
        select.focus();
        if (typeof select.showPicker === 'function') {
          try { select.showPicker(); } catch (ignore) { /* Focus is enough on unsupported browsers. */ }
        }
      });
    });

    resetButton.addEventListener('click', function () {
      selects.forEach(function (select) {
        select.value = '';
        quantityInput(select).value = '1';
      });
      sessionStorage.removeItem(storageKey);
      updateBuilder();
    });

    window.pcShopGetCurrentBuild = function () { return selectedParts(); };
    restoreSelection();
    updateBuilder();
  });
})();
