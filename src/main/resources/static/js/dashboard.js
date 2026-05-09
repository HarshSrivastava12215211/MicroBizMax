// ── Utility Functions ──
const API = '';
function getVendorId() {
    const id = localStorage.getItem('vendorId');
    if (!id) { window.location.href = '/login.html'; return null; }
    return id;
}
function showToast(msg, type = 'success') {
    const t = document.getElementById('toast');
    if (!t) return;
    t.textContent = msg;
    t.className = 'toast show toast-' + type;
    setTimeout(() => t.className = 'toast', 3000);
}

// ── Auth Guard ──
document.addEventListener('DOMContentLoaded', () => {
    const vendorId = getVendorId();
    if (!vendorId) return;
    loadDashboard(vendorId);

    // Logout
    const lb = document.getElementById('logoutBtn');
    if (lb) lb.addEventListener('click', (e) => {
        e.preventDefault();
        localStorage.removeItem('vendorId');
        localStorage.removeItem('vendorName');
        window.location.href = '/login.html';
    });
});

async function loadDashboard(vendorId) {
    try {
        // Dashboard stats
        const statsRes = await fetch(`${API}/api/analytics/vendor/${vendorId}/dashboard`);
        const stats = await statsRes.json();

        document.getElementById('statTotalSales').textContent = '₹' + Number(stats.totalSales || 0).toLocaleString('en-IN');
        document.getElementById('statTotalProfit').textContent = '₹' + Number(stats.totalProfit || 0).toLocaleString('en-IN');
        document.getElementById('statProducts').textContent = stats.totalProducts || 0;
        document.getElementById('statOrdersToday').textContent = stats.ordersToday || 0;
        document.getElementById('statCustomers').textContent = stats.totalCustomers || 0;
        document.getElementById('statRepeated').textContent = stats.repeatedCustomers || 0;
        document.getElementById('statLowStock').textContent = (stats.lowStockProducts || 0) + ' items';

        // Personalise welcome
        const name = localStorage.getItem('vendorName');
        if (name) {
            const h1 = document.querySelector('[data-i18n="welcome"]');
            if (h1) h1.textContent = `Welcome, ${name} `;
        }

        // Monthly chart
        const monthlyRes = await fetch(`${API}/api/analytics/vendor/${vendorId}/monthly`);
        const monthly = await monthlyRes.json();

        const ctx = document.getElementById('monthlySalesChart');
        if (ctx) {
            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: monthly.months,
                    datasets: [
                        {
                            label: 'Sales (₹)',
                            data: monthly.sales,
                            backgroundColor: 'rgba(59,130,246,0.2)',
                            borderColor: '#3b82f6',
                            borderWidth: 2,
                            borderRadius: 8
                        },
                        {
                            label: 'Profit (₹)',
                            data: monthly.profit,
                            backgroundColor: 'rgba(16,185,129,0.2)',
                            borderColor: '#10b981',
                            borderWidth: 2,
                            borderRadius: 8
                        }
                    ]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: { labels: { color: '#64748b', font: { family: 'Inter' } } }
                    },
                    scales: {
                        x: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(59,130,246,0.06)' } },
                        y: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(59,130,246,0.06)' } }
                    }
                }
            });
        }
    } catch (e) {
        console.error('Dashboard load error:', e);
    }
}
