
let me = null;
let allProducts = [];
let cart = [];
let category = "All";

const $ = id => document.getElementById(id);

async function api(url, options = {}) {
    const headers = {
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...(options.headers || {})
    };

    const response = await fetch(url, {
        credentials: "same-origin",
        ...options,
        headers
    });

    let data = null;

    try {
        data = await response.json();
    } catch {
        data = null;
    }

    if (!response.ok) {
        throw new Error(
            data?.message || data?.error || `Request failed (${response.status})`
        );
    }

    return data;
}

function esc(value = "") {
    return String(value).replace(/[&<>"']/g, char => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[char]);
}

function showAuth(mode) {
    $("authForm").dataset.mode = mode;

    $("authTitle").textContent =
        mode === "login" ? "Login to RithickMart" : "Create Account";

    $("nameField").classList.toggle("hidden", mode !== "register");
    $("name").required = mode === "register";

    $("authMsg").textContent = "";
    $("auth").classList.remove("hidden");

    $("auth").scrollIntoView({ behavior: "smooth" });
}

function hideAuth() {
    $("auth").classList.add("hidden");
}

async function loadProducts() {
    try {
        allProducts = await api("/api/products");
        renderProducts();
        renderAdminProducts();
    } catch (error) {
        $("products").innerHTML =
            `<p>Unable to load shoes: ${esc(error.message)}</p>`;
    }
}

function renderProducts() {
    const search = $("search").value.trim().toLowerCase();

    const filtered = allProducts.filter(product => {
        const productCategory = (product.category || "").toLowerCase();

        const matchesCategory =
            category === "All" ||
            productCategory === category.toLowerCase();

        const matchesSearch =
            (product.name || "").toLowerCase().includes(search) ||
            productCategory.includes(search) ||
            (product.description || "").toLowerCase().includes(search);

        return matchesCategory && matchesSearch;
    });

    if (filtered.length === 0) {
        $("products").innerHTML = "<p>No shoes found.</p>";
        return;
    }

    $("products").innerHTML = filtered.map(product => `
        <article class="product-card">
            <img
                src="${esc(product.image || "https://placehold.co/600x400?text=Shoe")}"
                alt="${esc(product.name)}"
                onerror="this.onerror=null;this.src='https://placehold.co/600x400?text=Shoe';"
            >

            <div class="product-info">
                <h3>${esc(product.name)}</h3>

                <span class="category-label">
                    ${esc(product.category || "Shoes")}
                </span>

                <p>${esc(product.description || "")}</p>

                <p class="price">
                    ₹${Number(product.price).toLocaleString("en-IN")}
                </p>

                <p>Available stock: ${Number(product.stock)}</p>

                <button
                    onclick="addToCart(${Number(product.id)})"
                    ${Number(product.stock) < 1 ? "disabled" : ""}>
                    ${Number(product.stock) < 1 ? "Out of Stock" : "Add to Cart"}
                </button>
            </div>
        </article>
    `).join("");
}

function setCategory(value, button) {
    category = value;

    document.querySelectorAll(".categories button").forEach(btn => {
        btn.classList.toggle("active", btn === button);
    });

    renderProducts();
}

function addToCart(productId) {
    const product = allProducts.find(
        p => Number(p.id) === Number(productId)
    );

    if (!product || Number(product.stock) <= 0) {
        alert("This shoe is out of stock.");
        return;
    }

    const existing = cart.find(
        item => Number(item.productId) === Number(productId)
    );

    if (existing) {
        if (existing.quantity >= Number(product.stock)) {
            alert("You have reached the available stock limit.");
            return;
        }

        existing.quantity++;
    } else {
        cart.push({
            productId: Number(productId),
            quantity: 1
        });
    }

    renderCart();
}

function renderCart() {
    const count = cart.reduce(
        (sum, item) => sum + item.quantity, 0
    );

    $("cartCount").textContent = count;

    let total = 0;

    $("cartItems").innerHTML = cart.map(item => {
        const product = allProducts.find(
            p => Number(p.id) === Number(item.productId)
        );

        if (!product) return "";

        const subtotal = Number(product.price) * item.quantity;
        total += subtotal;

        return `
            <div class="admin-product">
                <div>
                    <strong>${esc(product.name)}</strong>
                    <p>
                        ₹${Number(product.price).toLocaleString("en-IN")}
                        × ${item.quantity}
                    </p>
                    <strong>
                        Subtotal: ₹${subtotal.toLocaleString("en-IN")}
                    </strong>
                </div>

                <div>
                    <button
                        onclick="changeQuantity(${Number(product.id)}, -1)">
                        −
                    </button>

                    <button
                        onclick="changeQuantity(${Number(product.id)}, 1)">
                        +
                    </button>

                    <button
                        onclick="removeFromCart(${Number(product.id)})">
                        Remove
                    </button>
                </div>
            </div>
        `;
    }).join("") || "<p>Your cart is empty.</p>";

    $("cartTotal").textContent = total.toLocaleString("en-IN", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function changeQuantity(productId, change) {
    const item = cart.find(
        i => Number(i.productId) === Number(productId)
    );

    const product = allProducts.find(
        p => Number(p.id) === Number(productId)
    );

    if (!item || !product) return;

    const nextQuantity = item.quantity + change;

    if (nextQuantity < 1) {
        removeFromCart(productId);
        return;
    }

    if (nextQuantity > Number(product.stock)) {
        alert("Only " + product.stock + " items are available.");
        return;
    }

    item.quantity = nextQuantity;
    renderCart();
}

function removeFromCart(productId) {
    cart = cart.filter(
        item => Number(item.productId) !== Number(productId)
    );

    renderCart();
}

function showCart() {
    $("cartSection").classList.remove("hidden");
    renderCart();

    $("cartSection").scrollIntoView({ behavior: "smooth" });
}

function hideCart() {
    $("cartSection").classList.add("hidden");
}

async function checkout() {
    if (cart.length === 0) {
        alert("Your cart is empty.");
        return;
    }

    if (!me) {
        alert("Please login before placing your order.");
        showAuth("login");
        return;
    }

    if (!confirm("Confirm your Cash on Delivery order?")) {
        return;
    }

    try {
        const order = await api("/api/orders/checkout", {
            method: "POST",
            body: JSON.stringify({
                items: cart.map(item => ({
                    productId: item.productId,
                    quantity: item.quantity
                }))
            })
        });

        cart = [];
        renderCart();

        await loadProducts();

        alert(
            "Order placed successfully!\n" +
            "Order ID: " + order.id +
            "\nTotal: ₹" +
            Number(order.totalAmount).toLocaleString("en-IN")
        );

        hideCart();
    } catch (error) {
        alert("Checkout failed: " + error.message);
        await loadProducts();
    }
}

$("authForm").addEventListener("submit", async event => {
    event.preventDefault();

    const mode = event.currentTarget.dataset.mode;
    const message = $("authMsg");

    const payload = {
        email: $("email").value.trim(),
        password: $("password").value
    };

    if (mode === "register") {
        payload.name = $("name").value.trim();
        payload.role = "BUYER";
    }

    try {
        const result = await api("/api/auth/" + mode, {
            method: "POST",
            body: JSON.stringify(payload)
        });

        if (mode === "register") {
            event.currentTarget.reset();
            showAuth("login");
            $("authMsg").textContent =
                "Account created. Please login.";
            $("authMsg").style.color = "green";
            return;
        }

        me = result;
        event.currentTarget.reset();

        $("auth").classList.add("hidden");
        updateUI();

        await loadProducts();
    } catch (error) {
        message.style.color = "#b42318";
        message.textContent = error.message;
    }
});

function updateUI() {
    $("account").innerHTML = me
        ? `
            <span>${esc(me.name)} (${esc(me.role)})</span>
            <button onclick="logout()">Logout</button>
          `
        : `
            <button onclick="showAuth('login')">Login</button>
            <button onclick="showAuth('register')">Register</button>
          `;

    $("admin").classList.toggle(
        "hidden",
        me?.role !== "ADMIN"
    );

    renderAdminProducts();
}

async function logout() {
    try {
        await api("/api/auth/logout", {
            method: "POST"
        });

        me = null;
        updateUI();

        alert("Logged out successfully.");
    } catch (error) {
        alert("Logout failed: " + error.message);
    }
}

$("productForm").addEventListener("submit", async event => {
    event.preventDefault();

    if (me?.role !== "ADMIN") {
        alert("Admin access is required.");
        return;
    }

    const payload = {
        name: $("pname").value.trim(),
        category: $("pcategory").value.trim(),
        price: Number($("pprice").value),
        stock: Number($("pstock").value),
        image: $("pimage").value.trim(),
        description: $("pdesc").value.trim()
    };

    if (
        !payload.name ||
        !payload.category ||
        !Number.isFinite(payload.price) ||
        payload.price <= 0 ||
        !Number.isInteger(payload.stock) ||
        payload.stock < 0
    ) {
        alert("Enter valid shoe details.");
        return;
    }

    try {
        await api("/api/products", {
            method: "POST",
            body: JSON.stringify(payload)
        });

        event.currentTarget.reset();
        $("pstock").value = 10;

        await loadProducts();

        alert("Shoe added successfully.");
    } catch (error) {
        alert("Could not add shoe: " + error.message);
    }
});

function renderAdminProducts() {
    const container = $("adminProducts");

    if (!container) return;

    if (me?.role !== "ADMIN") {
        container.innerHTML = "";
        return;
    }

    container.innerHTML = allProducts.map(product => `
        <div class="admin-product">
            <span>
                ${esc(product.name)} —
                ₹${Number(product.price).toLocaleString("en-IN")}
                (Stock: ${Number(product.stock)})
            </span>

            <button onclick="deleteProduct(${Number(product.id)})">
                Delete
            </button>
        </div>
    `).join("") || "<p>No shoes available.</p>";
}

async function deleteProduct(id) {
    if (me?.role !== "ADMIN") {
        alert("Admin access is required.");
        return;
    }

    if (!confirm("Are you sure you want to delete this shoe?")) {
        return;
    }

    try {
        await api("/api/products/" + Number(id), {
            method: "DELETE"
        });

        cart = cart.filter(item => item.productId !== Number(id));

        await loadProducts();
        renderCart();

        alert("Shoe deleted successfully.");
    } catch (error) {
        alert("Could not delete shoe: " + error.message);
    }
}

$("search").addEventListener("input", renderProducts);

document.querySelectorAll(".categories button").forEach(button => {
    button.addEventListener("click", () => {
        setCategory(button.dataset.category, button);
    });
});

(async function initializeApp() {
    try {
        me = await api("/api/auth/me");
    } catch {
        me = null;
    }

    updateUI();
    renderCart();
    await loadProducts();
})();
