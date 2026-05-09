document.getElementById("loginForm").addEventListener("submit", function(e) {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const btn = document.getElementById("loginBtn");

    // Show loading state
    if (btn) {
        btn.disabled = true;
        btn.querySelector('span').textContent = 'Signing in...';
    }

    fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
    })
    .then(response => {
        if (!response.ok) throw new Error("Invalid credentials");
        return response.json();
    })
    .then(data => {
        // Save vendor info to localStorage
        localStorage.setItem('vendorId', data.id);
        localStorage.setItem('vendorName', data.name);
        localStorage.setItem('vendorEmail', data.email);

        if (typeof showAlert === 'function') {
            showAlert('Login successful! Redirecting...', 'success');
        }
        setTimeout(() => { window.location.href = "/dashboard.html"; }, 600);
    })
    .catch(error => {
        if (btn) {
            btn.disabled = false;
            btn.querySelector('span').textContent = 'Login';
        }
        if (typeof showAlert === 'function') {
            showAlert('Invalid email or password. Please try again.', 'error');
        } else {
            alert("Invalid email or password. Please try again.");
        }
        console.error(error);
    });
});