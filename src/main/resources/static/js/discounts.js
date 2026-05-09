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

document.addEventListener('DOMContentLoaded', () => {
    const vendorId = getVendorId();
    if (!vendorId) return;
    loadDiscounts(vendorId);

    document.getElementById('addDiscountBtn').addEventListener('click', () => openModal());
    document.getElementById('closeDiscountModal').addEventListener('click', () => closeModal());
    document.getElementById('discountForm').addEventListener('submit', (e) => saveDiscount(e, vendorId));
    document.getElementById('logoutBtn').addEventListener('click', (e) => {
        e.preventDefault(); localStorage.removeItem('vendorId'); localStorage.removeItem('vendorName');
        window.location.href = '/login.html';
    });
});

async function loadDiscounts(vendorId) {
    try {
        const res = await fetch(`${API}/api/discounts/vendor/${vendorId}`);
        const discounts = await res.json();
        const tbody = document.getElementById('discountsBody');
        const empty = document.getElementById('emptyDiscounts');

        if (!discounts.length) { tbody.innerHTML = ''; empty.style.display = 'block'; return; }
        empty.style.display = 'none';

        tbody.innerHTML = discounts.map(d => `
            <tr>
                <td style="font-weight:700;letter-spacing:1px;color:#38bdf8;">${d.code}</td>
                <td>${d.description || '-'}</td>
                <td><span class="badge badge-blue">${d.type}</span></td>
                <td>${d.type === 'PERCENTAGE' ? d.value + '%' : '₹' + d.value}</td>
                <td>₹${Number(d.minOrderAmount || 0).toLocaleString('en-IN')}</td>
                <td>${d.active ? '<span class="badge badge-green">Active</span>' : '<span class="badge badge-red">Inactive</span>'}</td>
                <td>
                    <button class="btn btn-outline btn-sm" onclick="editDiscount(${d.id})"></button>
                    <button class="btn btn-danger btn-sm" onclick="deleteDiscount(${d.id})"></button>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        console.error(e);
    }
}

function openModal(discount = null) {
    const modal = document.getElementById('discountModal');
    const title = document.getElementById('discountModalTitle');
    modal.classList.add('show');

    if (discount) {
        title.textContent = 'Edit Discount';
        document.getElementById('editDiscountId').value = discount.id;
        document.getElementById('dCode').value = discount.code;
        document.getElementById('dType').value = discount.type;
        document.getElementById('dValue').value = discount.value;
        document.getElementById('dMinOrder').value = discount.minOrderAmount || '';
        document.getElementById('dDesc').value = discount.description || '';
    } else {
        title.textContent = 'Create Discount';
        document.getElementById('discountForm').reset();
        document.getElementById('editDiscountId').value = '';
    }
}

function closeModal() {
    document.getElementById('discountModal').classList.remove('show');
}

async function editDiscount(discountId) {
    try {
        const res = await fetch(`${API}/api/discounts/vendor/${getVendorId()}`);
        const discounts = await res.json();
        const d = discounts.find(x => x.id === discountId);
        if (d) openModal(d);
    } catch (e) {
        showToast('Failed to load', 'error');
    }
}

async function deleteDiscount(discountId) {
    if (!confirm('Delete this discount?')) return;
    try {
        await fetch(`${API}/api/discounts/${discountId}`, { method: 'DELETE' });
        showToast('Discount deleted');
        loadDiscounts(getVendorId());
    } catch (e) {
        showToast('Failed to delete', 'error');
    }
}

async function saveDiscount(e, vendorId) {
    e.preventDefault();
    const editId = document.getElementById('editDiscountId').value;
    const payload = {
        code: document.getElementById('dCode').value.toUpperCase(),
        type: document.getElementById('dType').value,
        value: parseFloat(document.getElementById('dValue').value),
        minOrderAmount: parseFloat(document.getElementById('dMinOrder').value) || 0,
        description: document.getElementById('dDesc').value,
        active: true
    };

    try {
        const url = editId ? `${API}/api/discounts/${editId}` : `${API}/api/discounts/vendor/${vendorId}`;
        const method = editId ? 'PUT' : 'POST';
        await fetch(url, {
            method, headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        showToast(editId ? 'Discount updated!' : 'Discount created!');
        closeModal();
        loadDiscounts(vendorId);
    } catch (e) {
        showToast('Failed to save', 'error');
    }
}
