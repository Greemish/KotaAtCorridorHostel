// cart.js — localStorage cart management

const CART_KEY = 'kota_cart';

function getCart() {
  try {
    return JSON.parse(localStorage.getItem(CART_KEY)) || [];
  } catch {
    return [];
  }
}

function saveCart(cart) {
  localStorage.setItem(CART_KEY, JSON.stringify(cart));
  updateCartCount();
}

function clearCart() {
  localStorage.removeItem(CART_KEY);
  updateCartCount();
}

function addToCart(id, name, price) {
  const cart = getCart();
  const existing = cart.find(i => i.id === id);
  if (existing) {
    existing.quantity += 1;
  } else {
    cart.push({ id, name, price, quantity: 1 });
  }
  saveCart(cart);
  showCartToast(name);
}

function removeFromCart(id) {
  const cart = getCart().filter(i => i.id !== id);
  saveCart(cart);
  renderCart();
}

function updateQuantity(id, qty) {
  const cart = getCart();
  const item = cart.find(i => i.id === id);
  if (item) {
    item.quantity = parseInt(qty);
    if (item.quantity <= 0) return removeFromCart(id);
  }
  saveCart(cart);
  renderCart();
}

function updateCartCount() {
  const cart = getCart();
  const count = cart.reduce((sum, i) => sum + i.quantity, 0);
  document.querySelectorAll('#cartCount').forEach(el => { el.textContent = count; });
}

function renderCart() {
  const cart = getCart();
  const tbody = document.getElementById('cartTableBody');
  const emptyMsg = document.getElementById('emptyCart');
  const cartContent = document.getElementById('cartContent');
  const totalEl = document.getElementById('cartTotal');
  if (!tbody) return;
  if (cart.length === 0) {
    if (emptyMsg) emptyMsg.classList.remove('d-none');
    if (cartContent) cartContent.classList.add('d-none');
    return;
  }
  if (emptyMsg) emptyMsg.classList.add('d-none');
  if (cartContent) cartContent.classList.remove('d-none');
  let total = 0;
  tbody.innerHTML = '';
  cart.forEach(item => {
    const subtotal = item.price * item.quantity;
    total += subtotal;
    const row = document.createElement('tr');
    row.innerHTML = `
      <td>${item.name}</td>
      <td>R${item.price.toFixed(2)}</td>
      <td>
        <input type="number" class="form-control form-control-sm" style="width:70px"
               value="${item.quantity}" min="1"
               onchange="updateQuantity(${item.id}, this.value)">
      </td>
      <td>R${subtotal.toFixed(2)}</td>
      <td><button class="btn btn-sm btn-outline-danger" onclick="removeFromCart(${item.id})">Remove</button></td>
    `;
    tbody.appendChild(row);
  });
  if (totalEl) totalEl.textContent = 'R' + total.toFixed(2);
}

function showCartToast(name) {
  const existing = document.getElementById('cartToast');
  if (existing) existing.remove();
  const toast = document.createElement('div');
  toast.id = 'cartToast';
  toast.className = 'position-fixed bottom-0 start-50 translate-middle-x mb-4 alert alert-success shadow';
  toast.style.zIndex = '9999';
  toast.textContent = name + ' added to cart!';
  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 2000);
}

document.addEventListener('DOMContentLoaded', function() {
  updateCartCount();
  renderCart();
});
