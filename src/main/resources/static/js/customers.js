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
    loadCustomers(vendorId);

    document.getElementById('addCustomerBtn').addEventListener('click', () => openModal());
    document.getElementById('closeCustomerModal').addEventListener('click', () => closeModal());
    document.getElementById('customerForm').addEventListener('submit', (e) => saveCustomer(e, vendorId));
    document.getElementById('logoutBtn').addEventListener('click', (e) => {
        e.preventDefault(); localStorage.removeItem('vendorId'); localStorage.removeItem('vendorName');
        window.location.href = '/login.html';
    });
});

async function loadCustomers(vendorId) {
    try {
        const [allRes, repeatRes] = await Promise.all([
            fetch(`${API}/api/customers/vendor/${vendorId}`),
            fetch(`${API}/api/customers/vendor/${vendorId}/repeated`)
        ]);
        const customers = await allRes.json();
        const repeats = await repeatRes.json();

        const tbody = document.getElementById('customersBody');
        const empty = document.getElementById('emptyCustomers');
        const repeatSection = document.getElementById('repeatSection');

        // Repeat Customers Section
        if (repeats.length > 0) {
            repeatSection.style.display = 'block';
            document.getElementById('repeatCount').textContent = repeats.length + ' customers';
            document.getElementById('repeatList').innerHTML = repeats.map(c => `
                <div class="glass-card" style="padding:12px 18px;min-width:200px;">
                    <div style="font-weight:600;">${c.name}</div>
                    <div style="font-size:.8rem;color:rgba(255,255,255,0.5);">${c.totalOrders} orders · ₹${Number(c.totalSpent).toLocaleString('en-IN')}</div>
                </div>
            `).join('');
        } else {
            repeatSection.style.display = 'none';
        }

        if (!customers.length) { tbody.innerHTML = ''; empty.style.display = 'block'; return; }
        empty.style.display = 'none';

        tbody.innerHTML = customers.map(c => {
            const loyalty = c.totalOrders >= 5 ? 'badge-yellow' : c.totalOrders >= 2 ? 'badge-green' : 'badge-blue';
            const loyaltyText = c.totalOrders >= 5 ? ' VIP' : c.totalOrders >= 2 ? ' Repeat' : ' New';
            return `
            <tr>
                <td style="font-weight:600;">${c.name}</td>
                <td>${c.phone || '-'}</td>
                <td>${c.email || '-'}</td>
                <td>${c.totalOrders}</td>
                <td>₹${Number(c.totalSpent || 0).toLocaleString('en-IN')}</td>
                <td><span class="badge ${loyalty}">${loyaltyText}</span></td>
                <td>
                    <button class="btn btn-outline btn-sm" onclick="editCustomer(${c.id})"></button>
                    <button class="btn btn-danger btn-sm" onclick="deleteCustomer(${c.id})"></button>
                </td>
            </tr>`;
        }).join('');
    } catch (e) {
        console.error(e);
    }
}

function openModal(customer = null) {
    const modal = document.getElementById('customerModal');
    const title = document.getElementById('customerModalTitle');
    modal.classList.add('show');

    if (customer) {
        title.textContent = 'Edit Customer';
        document.getElementById('editCustomerId').value = customer.id;
        document.getElementById('cName').value = customer.name;
        document.getElementById('cPhone').value = customer.phone || '';
        document.getElementById('cEmail').value = customer.email || '';
        document.getElementById('cAddress').value = customer.address || '';
    } else {
        title.textContent = 'Add Customer';
        document.getElementById('customerForm').reset();
        document.getElementById('editCustomerId').value = '';
    }
}

function closeModal() {
    document.getElementById('customerModal').classList.remove('show');
}

async function editCustomer(customerId) {
    try {
        const res = await fetch(`${API}/api/customers/${customerId}`);
        const customer = await res.json();
        openModal(customer);
    } catch (e) {
        showToast('Failed to load customer', 'error');
    }
}

async function deleteCustomer(customerId) {
    if (!confirm('Delete this customer?')) return;
    try {
        await fetch(`${API}/api/customers/${customerId}`, { method: 'DELETE' });
        showToast('Customer deleted');
        loadCustomers(getVendorId());
    } catch (e) {
        showToast('Failed to delete', 'error');
    }
}

async function saveCustomer(e, vendorId) {
    e.preventDefault();
    const editId = document.getElementById('editCustomerId').value;
    const payload = {
        name: document.getElementById('cName').value,
        phone: document.getElementById('cPhone').value,
        email: document.getElementById('cEmail').value,
        address: document.getElementById('cAddress').value
    };

    try {
        const url = editId ? `${API}/api/customers/${editId}` : `${API}/api/customers/vendor/${vendorId}`;
        const method = editId ? 'PUT' : 'POST';
        await fetch(url, {
            method, headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        showToast(editId ? 'Customer updated!' : 'Customer added!');
        closeModal();
        loadCustomers(vendorId);
    } catch (e) {
        showToast('Failed to save', 'error');
    }
}
