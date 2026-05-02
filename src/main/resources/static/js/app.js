// app.js — General utilities

function getJwt() {
  return localStorage.getItem('jwt');
}

function authHeaders() {
  const token = getJwt();
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = 'Bearer ' + token;
  return headers;
}

async function apiFetch(url, options = {}) {
  options.headers = { ...authHeaders(), ...(options.headers || {}) };
  const res = await fetch(url, options);
  if (res.status === 401) {
    localStorage.removeItem('jwt');
    window.location.href = '/login';
    return null;
  }
  return res;
}

function formatCurrency(amount) {
  return 'R' + parseFloat(amount).toFixed(2);
}

function formatDate(dateStr) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('en-ZA');
}

function showAlert(message, type = 'info', containerId = 'alertBox') {
  const box = document.getElementById(containerId);
  if (!box) return;
  box.textContent = message;
  box.className = 'alert alert-' + type;
}
