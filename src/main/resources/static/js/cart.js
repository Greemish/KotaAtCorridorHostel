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

    const nameCell = document.createElement('td');
    nameCell.textContent = item.name;

    const priceCell = document.createElement('td');
    priceCell.textContent = 'R' + item.price.toFixed(2);

    const qtyCell = document.createElement('td');
    const qtyInput = document.createElement('input');
    qtyInput.type = 'number';
    qtyInput.className = 'form-control form-control-sm';
    qtyInput.style.width = '70px';
    qtyInput.value = item.quantity;
    qtyInput.min = 1;
    qtyInput.dataset.itemId = item.id;
    qtyInput.addEventListener('change', function() {
      updateQuantity(parseInt(this.dataset.itemId, 10), parseInt(this.value, 10));
    });
    qtyCell.appendChild(qtyInput);

    const subtotalCell = document.createElement('td');
    subtotalCell.textContent = 'R' + subtotal.toFixed(2);

    const removeCell = document.createElement('td');
    const removeBtn = document.createElement('button');
    removeBtn.className = 'btn btn-sm btn-outline-danger';
    removeBtn.textContent = 'Remove';
    removeBtn.dataset.itemId = item.id;
    removeBtn.addEventListener('click', function() {
      removeFromCart(parseInt(this.dataset.itemId, 10));
    });
    removeCell.appendChild(removeBtn);

    row.appendChild(nameCell);
    row.appendChild(priceCell);
    row.appendChild(qtyCell);
    row.appendChild(subtotalCell);
    row.appendChild(removeCell);
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
