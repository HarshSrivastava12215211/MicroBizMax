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
    loadProducts(vendorId);

    document.getElementById('addProductBtn').addEventListener('click', () => openModal());
    document.getElementById('closeProductModal').addEventListener('click', () => closeModal());
    document.getElementById('productForm').addEventListener('submit', (e) => saveProduct(e, vendorId));
    document.getElementById('logoutBtn').addEventListener('click', (e) => {
        e.preventDefault(); localStorage.removeItem('vendorId'); localStorage.removeItem('vendorName');
        window.location.href = '/login.html';
    });
});

async function loadProducts(vendorId) {
    try {
        const res = await fetch(`${API}/api/products/vendor/${vendorId}`);
        const products = await res.json();
        const tbody = document.getElementById('productsBody');
        const empty = document.getElementById('emptyProducts');

        if (!products.length) {
            tbody.innerHTML = '';
            empty.style.display = 'block';
            return;
        }
        empty.style.display = 'none';

        tbody.innerHTML = products.map(p => `
            <tr>
                <td style="font-weight:600;">${p.name}</td>
                <td>${p.category || '-'}</td>
                <td>₹${Number(p.price).toLocaleString('en-IN')}</td>
                <td>₹${Number(p.costPrice || 0).toLocaleString('en-IN')}</td>
                <td>
                    <span class="${p.stockQuantity <= 5 ? 'badge badge-red' : 'badge badge-green'}">${p.stockQuantity} ${p.unit || ''}</span>
                </td>
                <td>${p.unit || '-'}</td>
                <td>${p.active ? '<span class="badge badge-green">Active</span>' : '<span class="badge badge-red">Inactive</span>'}</td>
                <td>
                    <button class="btn btn-outline btn-sm" onclick="editProduct(${p.id})"></button>
                    <button class="btn btn-danger btn-sm" onclick="deleteProduct(${p.id})"></button>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        console.error(e);
        showToast('Failed to load products', 'error');
    }
}

function openModal(product = null) {
    const modal = document.getElementById('productModal');
    const title = document.getElementById('productModalTitle');
    modal.classList.add('show');

    if (product) {
        title.textContent = 'Edit Product';
        document.getElementById('editProductId').value = product.id;
        document.getElementById('pName').value = product.name;
        document.getElementById('pCategory').value = product.category || '';
        document.getElementById('pPrice').value = product.price;
        document.getElementById('pCostPrice').value = product.costPrice || '';
        document.getElementById('pStock').value = product.stockQuantity;
        document.getElementById('pUnit').value = product.unit || 'piece';
        document.getElementById('pDesc').value = product.description || '';
    } else {
        title.textContent = 'Add Product';
        document.getElementById('productForm').reset();
        document.getElementById('editProductId').value = '';
    }
}

function closeModal() {
    document.getElementById('productModal').classList.remove('show');
}

async function editProduct(productId) {
    try {
        const res = await fetch(`${API}/api/products/${productId}`);
        const product = await res.json();
        openModal(product);
    } catch (e) {
        showToast('Failed to load product', 'error');
    }
}

async function deleteProduct(productId) {
    if (!confirm('Delete this product?')) return;
    try {
        await fetch(`${API}/api/products/${productId}`, { method: 'DELETE' });
        showToast('Product deleted');
        loadProducts(getVendorId());
    } catch (e) {
        showToast('Failed to delete', 'error');
    }
}

async function saveProduct(e, vendorId) {
    e.preventDefault();
    const editId = document.getElementById('editProductId').value;
    const payload = {
        name: document.getElementById('pName').value,
        category: document.getElementById('pCategory').value,
        price: parseFloat(document.getElementById('pPrice').value),
        costPrice: parseFloat(document.getElementById('pCostPrice').value) || 0,
        stockQuantity: parseInt(document.getElementById('pStock').value),
        unit: document.getElementById('pUnit').value,
        description: document.getElementById('pDesc').value,
        active: true
    };

    try {
        const url = editId ? `${API}/api/products/${editId}` : `${API}/api/products/vendor/${vendorId}`;
        const method = editId ? 'PUT' : 'POST';
        await fetch(url, {
            method, headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        showToast(editId ? 'Product updated!' : 'Product added!');
        closeModal();
        loadProducts(vendorId);
    } catch (e) {
        showToast('Failed to save product', 'error');
    }
}
