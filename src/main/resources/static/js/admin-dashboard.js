// ── Auth Guard ──────────────────────────────────────────────────────
(function () {
    if (localStorage.getItem('isAdmin') !== 'true') {
        window.location.href = '/admin-login.html';
    }
})();

// ── Utilities ────────────────────────────────────────────────────────
function showToast(msg, type = 'success') {
    const t = document.getElementById('toast');
    if (!t) return;
    t.textContent = msg;
    t.className = 'toast show toast-' + type;
    setTimeout(() => t.className = 'toast', 3000);
}

function fmt(n) {
    return Number(n || 0).toLocaleString('en-IN');
}

function fmtDate(str) {
    if (!str) return '—';
    return new Date(str).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

// ── State ─────────────────────────────────────────────────────────────
let allVendors = [], allCustomers = [], allProducts = [], allOrders = [], allDiscounts = [];

// ── Tab System ───────────────────────────────────────────────────────
window.switchTab = function (tab, el) {
    document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.sidebar-nav a').forEach(a => a.classList.remove('active'));
    document.getElementById('pane-' + tab).classList.add('active');
    if (el) el.classList.add('active');
    // Lazy-render
    if (tab === 'vendors')   renderVendors(allVendors);
    if (tab === 'customers') renderCustomers(allCustomers);
    if (tab === 'products')  renderProducts(allProducts);
    if (tab === 'orders')    renderOrders(allOrders);
    if (tab === 'discounts') renderDiscounts(allDiscounts);
};

// ── Search filters ───────────────────────────────────────────────────
function setupSearch(inputId, fn, dataRef) {
    const el = document.getElementById(inputId);
    if (!el) return;
    el.addEventListener('input', () => fn(dataRef().filter(item => {
        const q = el.value.toLowerCase();
        return Object.values(item).some(v => String(v || '').toLowerCase().includes(q));
    })));
}

// ── Renderers ────────────────────────────────────────────────────────
function renderVendors(data) {
    const tb = document.getElementById('tbVendors');
    if (!data.length) { tb.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text-muted);padding:40px;">No vendors found</td></tr>`; return; }
    tb.innerHTML = data.map((v, i) => `
        <tr>
            <td style="color:var(--text-muted)">${i + 1}</td>
            <td style="font-weight:600">${v.name || '—'}</td>
            <td>${v.shopName || '—'}</td>
            <td style="color:var(--gold-400)">${v.email || '—'}</td>
            <td>${v.phone || '—'}</td>
            <td style="color:var(--text-secondary)">${v.address || '—'}</td>
        </tr>`).join('');
}

function renderCustomers(data) {
    const tb = document.getElementById('tbCustomers');
    if (!data.length) { tb.innerHTML = `<tr><td colspan="5" style="text-align:center;color:var(--text-muted);padding:40px;">No customers found</td></tr>`; return; }
    tb.innerHTML = data.map((c, i) => `
        <tr>
            <td style="color:var(--text-muted)">${i + 1}</td>
            <td style="font-weight:600">${c.name || '—'}</td>
            <td>${c.phone || '—'}</td>
            <td style="color:var(--gold-400)">${c.email || '—'}</td>
            <td style="color:var(--text-secondary)">${c.address || '—'}</td>
        </tr>`).join('');
}

function renderProducts(data) {
    const tb = document.getElementById('tbProducts');
    if (!data.length) { tb.innerHTML = `<tr><td colspan="7" style="text-align:center;color:var(--text-muted);padding:40px;">No products found</td></tr>`; return; }
    tb.innerHTML = data.map((p, i) => `
        <tr>
            <td style="color:var(--text-muted)">${i + 1}</td>
            <td style="font-weight:600">${p.name || '—'}</td>
            <td>${p.category || '—'}</td>
            <td style="color:var(--gold-300)">₹${fmt(p.price)}</td>
            <td style="color:var(--text-secondary)">₹${fmt(p.costPrice)}</td>
            <td>${p.stockQuantity != null ? (p.stockQuantity < 5 ? `<span class="badge badge-red">${p.stockQuantity} Low</span>` : `<span class="badge badge-green">${p.stockQuantity}</span>`) : '—'}</td>
            <td style="color:var(--text-muted)">${p.vendor ? (p.vendor.name || p.vendor.shopName || 'Vendor #' + p.vendor.id) : '—'}</td>
        </tr>`).join('');
}

function renderOrders(data) {
    const tb = document.getElementById('tbOrders');
    if (!data.length) { tb.innerHTML = `<tr><td colspan="7" style="text-align:center;color:var(--text-muted);padding:40px;">No orders found</td></tr>`; return; }
    tb.innerHTML = data.map((o, i) => {
        const statusClass = o.status === 'COMPLETED' ? 'badge-green' : o.status === 'CANCELLED' ? 'badge-red' : 'badge-yellow';
        return `
        <tr>
            <td style="color:var(--text-muted)">${o.id}</td>
            <td>${fmtDate(o.orderDate)}</td>
            <td style="color:var(--text-secondary)">${o.vendor ? (o.vendor.shopName || o.vendor.name || '—') : '—'}</td>
            <td>${o.customer ? o.customer.name : '—'}</td>
            <td style="color:var(--gold-300);font-weight:600">₹${fmt(o.totalAmount)}</td>
            <td>${o.paymentMethod || '—'}</td>
            <td><span class="badge ${statusClass}">${o.status || '—'}</span></td>
        </tr>`;
    }).join('');
}

function renderDiscounts(data) {
    const tb = document.getElementById('tbDiscounts');
    if (!data.length) { tb.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text-muted);padding:40px;">No discounts found</td></tr>`; return; }
    tb.innerHTML = data.map((d, i) => `
        <tr>
            <td style="color:var(--text-muted)">${i + 1}</td>
            <td><code style="background:rgba(201,149,44,0.1);padding:3px 10px;border-radius:8px;color:var(--gold-300)">${d.code || '—'}</code></td>
            <td>${d.discountType || '—'}</td>
            <td style="color:var(--gold-400);font-weight:600">${d.discountType === 'PERCENTAGE' ? (d.value || 0) + '%' : '₹' + fmt(d.value)}</td>
            <td>₹${fmt(d.minOrderAmount)}</td>
            <td style="color:var(--text-secondary)">${fmtDate(d.expiryDate)}</td>
        </tr>`).join('');
}

// ── Fetch all data ───────────────────────────────────────────────────
async function loadAll() {
    try {
        const [statsRes, vendorsRes, customersRes, productsRes, ordersRes, discountsRes] = await Promise.all([
            fetch('/api/admin/stats'),
            fetch('/api/admin/vendors'),
            fetch('/api/admin/customers'),
            fetch('/api/admin/products'),
            fetch('/api/admin/orders'),
            fetch('/api/admin/discounts'),
        ]);

        // Stats
        const stats = await statsRes.json();
        document.getElementById('sVendors').textContent   = fmt(stats.totalVendors);
        document.getElementById('sCustomers').textContent = fmt(stats.totalCustomers);
        document.getElementById('sProducts').textContent  = fmt(stats.totalProducts);
        document.getElementById('sOrders').textContent    = fmt(stats.totalOrders);
        document.getElementById('sRevenue').textContent   = '₹' + fmt(Math.round(stats.totalRevenue));

        // Store data
        allVendors   = await vendorsRes.json();
        allCustomers = await customersRes.json();
        allProducts  = await productsRes.json();
        allOrders    = await ordersRes.json();
        allDiscounts = await discountsRes.json();

        // Setup search listeners
        setupSearch('searchVendors',   renderVendors,   () => allVendors);
        setupSearch('searchCustomers', renderCustomers, () => allCustomers);
        setupSearch('searchProducts',  renderProducts,  () => allProducts);
        setupSearch('searchOrders',    renderOrders,    () => allOrders);
        setupSearch('searchDiscounts', renderDiscounts, () => allDiscounts);

    } catch (e) {
        showToast('Failed to load data. Please refresh.', 'error');
        console.error(e);
    }
}

// ── Init ─────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    const name = localStorage.getItem('adminName') || 'Admin';
    const badge = document.getElementById('adminNameBadge');
    if (badge) badge.textContent = ' ' + name;

    // Logout
    document.getElementById('logoutBtn')?.addEventListener('click', e => {
        e.preventDefault();
        localStorage.removeItem('isAdmin');
        localStorage.removeItem('adminEmail');
        localStorage.removeItem('adminName');
        window.location.href = '/admin-login.html';
    });

    loadAll();
});
