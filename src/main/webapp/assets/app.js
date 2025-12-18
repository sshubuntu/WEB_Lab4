const { createApp, ref, computed, onMounted, watch } = Vue;

const allowedX = [-3, -2, -1, 0, 1, 2, 3, 4, 5];
const allowedR = [-3, -2, -1, 0, 1, 2, 3, 4, 5];

function getApiBase() {
  const path = window.location.pathname;
  if (path.includes('/WEB_Lab4')) {
    return '/WEB_Lab4/api';
  }
  return '/api';
}

const API_BASE = getApiBase();

function nearestX(value) {
  let best = allowedX[0];
  let diff = Math.abs(value - best);
  for (const v of allowedX) {
    const d = Math.abs(value - v);
    if (d < diff) {
      diff = d;
      best = v;
    }
  }
  return best;
}

createApp({
  setup() {
    const canvas = ref(null);
    const user = ref(null);
    const results = ref([]);
    const selectedX = ref(null);
    const yInput = ref('');
    const selectedR = ref(null);
    const filteredResults = computed(() => {
      const r = selectedR.value;
      if (!Number.isFinite(r)) {
        return results.value;
      }
      return results.value.filter((item) => Number(item.r) === Number(r));
    });
    const errors = ref([]);
    const loading = ref(true);

    const fetchMe = async () => {
      const res = await fetch(`${API_BASE}/auth/me`, { credentials: 'include' });
      if (res.status === 401) {
        window.location.href = 'index.html';
        return;
      }
      const payload = await res.json();
      user.value = payload;
    };

    const fetchResults = async () => {
      const res = await fetch(`${API_BASE}/points/results`, { credentials: 'include' });
      if (res.status === 401) {
        window.location.href = 'index.html';
        return;
      }
      const text = await res.text();
      results.value = text ? JSON.parse(text) : [];
    };

    const redraw = () => {
      const el = canvas.value;
      if (!el) return;
      const ctx = el.getContext('2d');
      const pixelRatio = window.devicePixelRatio || 1;
      const width = el.clientWidth || el.offsetWidth || 720;
      const height = el.clientHeight || el.offsetHeight || 420;
      el.width = width * pixelRatio;
      el.height = height * pixelRatio;
      ctx.resetTransform?.();
      ctx.scale(pixelRatio, pixelRatio);

      ctx.fillStyle = '#0d1425';
      ctx.fillRect(0, 0, width, height);
      ctx.strokeStyle = 'rgba(148, 163, 184, 0.8)';
      ctx.lineWidth = 1;

      const cx = width / 2;
      const cy = height / 2;
      const displayRange = Math.max(Math.abs(selectedR.value || 3), 3);
      const scale = Math.min(width, height) / (displayRange * 2 + 2);

      ctx.beginPath();
      ctx.moveTo(20, cy);
      ctx.lineTo(width - 20, cy);
      ctx.moveTo(cx, 20);
      ctx.lineTo(cx, height - 20);
      ctx.stroke();

      ctx.fillStyle = '#9ca3af';
      ctx.font = '12px "Inter", system-ui, sans-serif';
      for (let i = -displayRange; i <= displayRange; i++) {
        const px = cx + i * scale;
        ctx.beginPath();
        ctx.moveTo(px, cy - 4);
        ctx.lineTo(px, cy + 4);
        ctx.stroke();
        if (i !== 0) ctx.fillText(i.toString(), px - 4, cy + 16);

        const py = cy - i * scale;
        ctx.beginPath();
        ctx.moveTo(cx - 4, py);
        ctx.lineTo(cx + 4, py);
        ctx.stroke();
        if (i !== 0) ctx.fillText(i.toString(), cx + 8, py + 4);
      }

      const r = selectedR.value;
      if (Number.isFinite(r) && r !== 0) {
        const R = Math.abs(r);
        const areaColor = 'rgba(59,130,246,0.28)';
        ctx.fillStyle = areaColor;

        if (r > 0) {
          ctx.beginPath();
          ctx.rect(cx - R * scale, cy, R * scale, R/2 * scale);
          ctx.fill();

          ctx.beginPath();
          ctx.moveTo(cx, cy);
          ctx.lineTo(cx + (R / 2) * scale, cy);
          ctx.lineTo(cx, cy + (R / 2) * scale);
          ctx.closePath();
          ctx.fill();

          ctx.beginPath();
          ctx.moveTo(cx, cy);
          ctx.arc(cx, cy, (R / 2) * scale, -Math.PI / 2, 0, false);
          ctx.closePath();
          ctx.fill();
        } else {
          ctx.beginPath();
          ctx.rect(cx, cy - R/2 * scale, R * scale, R/2 * scale);
          ctx.fill();

          ctx.beginPath();
          ctx.moveTo(cx, cy);
          ctx.lineTo(cx - (R / 2) * scale, cy);
          ctx.lineTo(cx, cy - (R / 2) * scale);
          ctx.closePath();
          ctx.fill();

          ctx.beginPath();
          ctx.moveTo(cx, cy);
          ctx.arc(cx, cy, (R / 2) * scale, Math.PI / 2, Math.PI, false);
          ctx.closePath();
          ctx.fill();
        }
      }

      const itemsToDraw = filteredResults.value;
      for (const item of itemsToDraw) {
        const px = cx + item.x * scale;
        const py = cy - item.y * scale;
        ctx.fillStyle = item.hit ? '#22c55e' : '#ef4444';
        ctx.beginPath();
        ctx.arc(px, py, 4, 0, Math.PI * 2);
        ctx.fill();
      }
    };

    const validate = () => {
      const issues = [];
      const x = selectedX.value;
      if (!Number.isFinite(x)) issues.push('Выберите X');

      const yRaw = yInput.value.replace(',', '.');
      const y = Number(yRaw);
      if (yInput.value.trim() === '') issues.push('Введите Y');
      else if (!Number.isFinite(y)) issues.push('Y должен быть числом');
      else if (y < -3 || y > 3) issues.push('Y должен быть в диапазоне [-3; 3]');

      const r = selectedR.value;
      if (!Number.isFinite(r)) issues.push('Выберите R');
      else if (!allowedR.includes(Number(r))) issues.push('R должен быть из набора {-3..5}');
      else if (r === 0) issues.push('Радиус не может быть нулевым');

      return { issues, y, r, x };
    };

    const submit = async () => {
      errors.value = [];
      const { issues, y, r, x } = validate();
      if (issues.length) {
        errors.value = issues;
        return;
      }
      loading.value = true;
      try {
        const res = await fetch(`${API_BASE}/points`, {
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ x, y, r })
        });
        const text = await res.text();
        const payload = text ? JSON.parse(text) : {};
        if (!res.ok) {
          errors.value = [payload.error || 'Ошибка проверки точки'];
          return;
        }
        results.value = [payload, ...results.value];
        redraw();
      } catch (err) {
        errors.value = ['Ошибка соединения: ' + err];
      } finally {
        loading.value = false;
      }
    };

    const handleCanvasClick = (event) => {
      errors.value = [];
      const r = selectedR.value;
      if (!Number.isFinite(r) || r === 0) {
        errors.value = ['Сначала выберите ненулевой R'];
        return;
      }

      const el = canvas.value;
      const rect = el.getBoundingClientRect();
      const width = el.clientWidth;
      const height = el.clientHeight;
      const displayRange = Math.max(Math.abs(r), 3);
      const scale = Math.min(width, height) / (displayRange * 2 + 2);
      const cx = width / 2;
      const cy = height / 2;

      const px = event.clientX - rect.left;
      const py = event.clientY - rect.top;
      const xCanvas = (px - cx) / scale;
      const yCanvas = (cy - py) / scale;
      const snappedX = nearestX(xCanvas);
      const roundedY = Number(yCanvas.toFixed(4));

      selectedX.value = snappedX;
      yInput.value = roundedY.toString();
      submit();
    };

    const logout = async () => {
      await fetch(`${API_BASE}/auth/logout`, { method: 'POST', credentials: 'include' });
      window.location.href = 'index.html';
    };

    const reloadResults = async () => {
      loading.value = true;
      try {
        await fetchResults();
      } finally {
        loading.value = false;
        redraw();
      }
    };

    const deleteResult = async (id) => {
      if (!id) return;
      loading.value = true;
      try {
        const res = await fetch(`${API_BASE}/points/${id}`, {
          method: 'DELETE',
          credentials: 'include'
        });
        if (res.status === 401) {
          window.location.href = 'index.html';
          return;
        }
        if (res.ok) {
          results.value = results.value.filter(item => item.id !== id);
          redraw();
        } else {
          errors.value = ['Не удалось удалить запись'];
        }
      } catch (err) {
        errors.value = ['Ошибка соединения: ' + err];
      } finally {
        loading.value = false;
      }
    };

    const deleteAllResults = async () => {
      loading.value = true;
      try {
        const res = await fetch(`${API_BASE}/points/all`, {
          method: 'DELETE',
          credentials: 'include'
        });
        if (res.status === 401) {
          window.location.href = 'index.html';
          return;
        }
        if (res.ok) {
          results.value = [];
          redraw();
        } else {
          errors.value = ['Не удалось удалить записи'];
        }
      } catch (err) {
        errors.value = ['Ошибка соединения: ' + err];
      } finally {
        loading.value = false;
      }
    };

    onMounted(async () => {
      await fetchMe();
      await fetchResults();
      loading.value = false;
      redraw();
    });

    watch(() => selectedR.value, redraw);
    watch(results, redraw, { deep: true });

    return {
      canvas,
      user,
      results,
      filteredResults,
      selectedX,
      yInput,
      selectedR,
      allowedX,
      allowedR,
      errors,
      loading,
      submit,
      logout,
      handleCanvasClick,
      reloadResults,
      deleteResult,
      deleteAllResults
    };
  }
}).mount('#app');


