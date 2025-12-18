const { createApp, ref } = Vue;

function getApiBase() {
  const path = window.location.pathname;
  if (path.includes('/WEB_Lab4')) {
    return '/WEB_Lab4/api';
  }
  return '/api';
}

const API_BASE = getApiBase();

createApp({
  setup() {
    const isRegisterMode = ref(false);
    const username = ref('');
    const password = ref('');
    const regUsername = ref('');
    const regPassword = ref('');
    const regPasswordConfirm = ref('');
    const errors = ref([]);
    const successMessage = ref('');
    const loading = ref(false);

    const validateLogin = () => {
      const problems = [];
      if (!username.value.trim()) problems.push('Введите имя пользователя');
      if (!password.value.trim()) problems.push('Введите пароль');
      return problems;
    };

    const validateRegister = () => {
      const problems = [];
      if (!regUsername.value.trim()) problems.push('Введите имя пользователя');
      if (!regPassword.value.trim()) problems.push('Введите пароль');
      if (regPassword.value.length < 4) problems.push('Пароль должен содержать не менее 4 символов');
      if (!regPasswordConfirm.value.trim()) problems.push('Подтвердите пароль');
      if (regPassword.value !== regPasswordConfirm.value) problems.push('Пароли не совпадают');
      return problems;
    };

    const submitLogin = async () => {
      errors.value = [];
      successMessage.value = '';
      const validation = validateLogin();
      if (validation.length) {
        errors.value = validation;
        return;
      }
      loading.value = true;
      try {
        const res = await fetch(`${API_BASE}/auth/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify({
            username: username.value.trim(),
            password: password.value
          })
        });

        const text = await res.text();
        const payload = text ? JSON.parse(text) : {};
        if (!res.ok) {
          errors.value = [payload.error || 'Не удалось войти'];
          return;
        }
        successMessage.value = 'Успешный вход, перенаправляем...';
        setTimeout(() => window.location.href = 'app.html', 400);
      } catch (err) {
        errors.value = ['Ошибка соединения: ' + err];
      } finally {
        loading.value = false;
      }
    };

    const submitRegister = async () => {
      errors.value = [];
      successMessage.value = '';
      const validation = validateRegister();
      if (validation.length) {
        errors.value = validation;
        return;
      }
      loading.value = true;
      try {
        const res = await fetch(`${API_BASE}/auth/register`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify({
            username: regUsername.value.trim(),
            password: regPassword.value
          })
        });

        const text = await res.text();
        const payload = text ? JSON.parse(text) : {};
        if (!res.ok) {
          errors.value = [payload.error || 'Не удалось зарегистрироваться'];
          return;
        }
        successMessage.value = 'Регистрация успешна, перенаправляем...';
        setTimeout(() => window.location.href = 'app.html', 400);
      } catch (err) {
        errors.value = ['Ошибка соединения: ' + err];
      } finally {
        loading.value = false;
      }
    };

    return {
      isRegisterMode,
      username,
      password,
      regUsername,
      regPassword,
      regPasswordConfirm,
      errors,
      successMessage,
      loading,
      submitLogin,
      submitRegister
    };
  }
}).mount('#app');


