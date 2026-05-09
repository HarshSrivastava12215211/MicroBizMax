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
    loadProfile(vendorId);
    loadStats(vendorId);

    document.getElementById('profileForm').addEventListener('submit', (e) => updateProfile(e, vendorId));
    document.getElementById('passwordForm').addEventListener('submit', (e) => changePassword(e, vendorId));
    document.getElementById('logoutBtn').addEventListener('click', (e) => {
        e.preventDefault(); localStorage.removeItem('vendorId'); localStorage.removeItem('vendorName');
        window.location.href = '/login.html';
    });
});

async function loadProfile(vendorId) {
    try {
        const res = await fetch(`${API}/api/vendor/${vendorId}`);
        const vendor = await res.json();

        document.getElementById('profileName').textContent = vendor.name;
        document.getElementById('profileShopName').textContent = vendor.shopName;
        document.getElementById('profileEmail').textContent = vendor.email;
        document.getElementById('profileAvatar').textContent = vendor.name ? vendor.name.charAt(0).toUpperCase() : 'V';

        document.getElementById('prName').value = vendor.name || '';
        document.getElementById('prShopName').value = vendor.shopName || '';
        document.getElementById('prPhone').value = vendor.phone || '';
        document.getElementById('prAddress').value = vendor.address || '';
    } catch (e) {
        console.error(e);
    }
}

async function loadStats(vendorId) {
    try {
        const res = await fetch(`${API}/api/analytics/vendor/${vendorId}/dashboard`);
        const stats = await res.json();

        document.getElementById('prTotalSales').textContent = '₹' + Number(stats.totalSales || 0).toLocaleString('en-IN');
        document.getElementById('prTotalProfit').textContent = '₹' + Number(stats.totalProfit || 0).toLocaleString('en-IN');
        document.getElementById('prProducts').textContent = stats.totalProducts || 0;
        document.getElementById('prCustomers').textContent = stats.totalCustomers || 0;
    } catch (e) {
        console.error(e);
    }
}

async function updateProfile(e, vendorId) {
    e.preventDefault();
    const payload = {
        name: document.getElementById('prName').value,
        shopName: document.getElementById('prShopName').value,
        phone: document.getElementById('prPhone').value,
        address: document.getElementById('prAddress').value
    };

    try {
        const res = await fetch(`${API}/api/vendor/${vendorId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            const vendor = await res.json();
            localStorage.setItem('vendorName', vendor.name);
            showToast('Profile updated!');
            loadProfile(vendorId);
        } else {
            showToast('Update failed', 'error');
        }
    } catch (e) {
        showToast('Network error', 'error');
    }
}

async function changePassword(e, vendorId) {
    e.preventDefault();
    const newPass = document.getElementById('newPassword').value;
    const confirmPass = document.getElementById('confirmPassword').value;

    if (newPass !== confirmPass) {
        showToast('Passwords do not match', 'error');
        return;
    }

    const payload = {
        oldPassword: document.getElementById('oldPassword').value,
        newPassword: newPass
    };

    try {
        const res = await fetch(`${API}/api/vendor/${vendorId}/password`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (res.ok) {
            showToast('Password changed!');
            document.getElementById('passwordForm').reset();
        } else {
            showToast(data.error || 'Failed to change password', 'error');
        }
    } catch (e) {
        showToast('Network error', 'error');
    }
}
