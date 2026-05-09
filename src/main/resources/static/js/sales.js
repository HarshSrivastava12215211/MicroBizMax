const API = '';
function getVendorId() {
    const id = localStorage.getItem('vendorId');
    if (!id) { window.location.href = '/login.html'; return null; }
    return id;
}
function showToast(msg, type = 'success') {
    const t = document.getElementById('toast');
    t.textContent = msg;
    t.className = 'toast show toast-' + type;
    setTimeout(() => t.className = 'toast', 3000);
}

let allProducts = [];
let allCustomers = [];

document.addEventListener('DOMContentLoaded', () => {
    const vendorId = getVendorId();
    if (!vendorId) return;

    loadOrders(vendorId);
    loadProductsAndCustomers(vendorId);

    document.getElementById('newSaleBtn').addEventListener('click', () => openSaleModal());
    document.getElementById('closeSaleModal').addEventListener('click', () => closeSaleModal());
    document.getElementById('addItemRow').addEventListener('click', () => addItemRow());
    document.getElementById('saleForm').addEventListener('submit', (e) => submitSale(e, vendorId));
    document.getElementById('logoutBtn').addEventListener('click', (e) => {
        e.preventDefault(); localStorage.removeItem('vendorId'); localStorage.removeItem('vendorName');
        window.location.href = '/login.html';
    });
});

async function loadOrders(vendorId) {
    try {
        const res = await fetch(`${API}/api/orders/vendor/${vendorId}`);
        const orders = await res.json();
        const tbody = document.getElementById('ordersBody');
        const empty = document.getElementById('emptyOrders');

        if (!orders.length) { tbody.innerHTML = ''; empty.style.display = 'block'; return; }
        empty.style.display = 'none';

        tbody.innerHTML = orders.map(o => `
            <tr>
                <td>#${o.id}</td>
                <td>${new Date(o.orderDate).toLocaleDateString('en-IN')}</td>
                <td>${o.customer ? o.customer.name : 'Walk-in'}</td>
                <td>${o.items ? o.items.length : 0}</td>
                <td style="font-weight:600;">₹${Number(o.totalAmount).toLocaleString('en-IN')}</td>
                <td>${o.discountApplied > 0 ? '<span class="badge badge-yellow">₹' + o.discountApplied + '</span>' : '-'}</td>
                <td style="color:${o.profit >= 0 ? '#34d399' : '#f87171'};font-weight:600;">₹${Number(o.profit).toLocaleString('en-IN')}</td>
                <td><span class="badge badge-blue">${o.paymentMethod}</span></td>
                <td><span class="badge badge-green">${o.status}</span></td>
            </tr>
        `).join('');
    } catch (e) {
        console.error(e);
    }
}

async function loadProductsAndCustomers(vendorId) {
    try {
        const [pRes, cRes] = await Promise.all([
            fetch(`${API}/api/products/vendor/${vendorId}`),
            fetch(`${API}/api/customers/vendor/${vendorId}`)
        ]);
        allProducts = await pRes.json();
        allCustomers = await cRes.json();

        // Populate customer dropdown
        const sel = document.getElementById('sCustomer');
        sel.innerHTML = '<option value="">Walk-in Customer</option>' +
            allCustomers.map(c => `<option value="${c.id}">${c.name} (${c.phone})</option>`).join('');
    } catch (e) {
        console.error(e);
    }
}

function openSaleModal() {
    document.getElementById('saleModal').classList.add('show');
    document.getElementById('saleForm').reset();
    document.getElementById('saleItemsContainer').innerHTML = '';
    document.getElementById('saleTotal').textContent = '₹0';
    addItemRow();
}

function closeSaleModal() {
    document.getElementById('saleModal').classList.remove('show');
}

function addItemRow() {
    const container = document.getElementById('saleItemsContainer');
    const row = document.createElement('div');
    row.className = 'form-row';
    row.style.marginBottom = '12px';
    row.innerHTML = `
        <div class="form-group" style="flex:2;">
            <select class="form-control item-product" onchange="recalcTotal()">
                <option value="">Select Product</option>
                ${allProducts.map(p => `<option value="${p.id}" data-price="${p.price}" data-stock="${p.stockQuantity}">${p.name} (₹${p.price} / Stock: ${p.stockQuantity})</option>`).join('')}
            </select>
        </div>
        <div class="form-group" style="flex:1;">
            <input class="form-control item-qty" type="number" min="1" value="1" placeholder="Qty" onchange="recalcTotal()" oninput="recalcTotal()">
        </div>
        <div style="display:flex;align-items:center;">
            <button type="button" class="btn btn-danger btn-sm" onclick="this.closest('.form-row').remove();recalcTotal();">✕</button>
        </div>
    `;
    container.appendChild(row);
}

function recalcTotal() {
    let total = 0;
    document.querySelectorAll('.item-product').forEach((sel, i) => {
        const opt = sel.options[sel.selectedIndex];
        const price = parseFloat(opt?.dataset?.price || 0);
        const qty = parseInt(document.querySelectorAll('.item-qty')[i]?.value || 0);
        total += price * qty;
    });
    document.getElementById('saleTotal').textContent = '₹' + total.toLocaleString('en-IN');
}

async function submitSale(e, vendorId) {
    e.preventDefault();
    const items = [];
    const productSels = document.querySelectorAll('.item-product');
    const qtySels = document.querySelectorAll('.item-qty');

    for (let i = 0; i < productSels.length; i++) {
        const pid = productSels[i].value;
        const qty = parseInt(qtySels[i].value);
        if (pid && qty > 0) {
            items.push({ productId: parseInt(pid), quantity: qty });
        }
    }

    if (!items.length) { showToast('Add at least one item', 'error'); return; }

    const payload = {
        customerId: document.getElementById('sCustomer').value || null,
        items: items,
        paymentMethod: document.getElementById('sPayment').value,
        discountCode: document.getElementById('sDiscountCode').value || null
    };

    try {
        const res = await fetch(`${API}/api/orders/vendor/${vendorId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (res.ok) {
            showToast('Sale completed! Profit: ₹' + Number(data.profit).toLocaleString('en-IN'));
            closeSaleModal();
            loadOrders(vendorId);
            loadProductsAndCustomers(vendorId);
        } else {
            showToast(data.error || 'Sale failed', 'error');
        }
    } catch (e) {
        showToast('Network error', 'error');
    }
}
