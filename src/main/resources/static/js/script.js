document.getElementById("registerForm").addEventListener("submit", function(e) {
    e.preventDefault();

    const data = {
        name: document.getElementById("name").value,
        shopName: document.getElementById("shopName").value,
        email: document.getElementById("email").value,
        password: document.getElementById("password").value,
        phone: document.getElementById("phone").value,
        address: document.getElementById("address").value
    };

    fetch("/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) throw new Error("Registration failed");
        return response.json();
    })
    .then(result => {
        alert("Vendor Registered Successfully! Please login.");
        window.location.href = "/login.html";
    })
    .catch(error => {
        alert("Registration failed. Email might already be taken.");
        console.error("Error:", error);
    });
});