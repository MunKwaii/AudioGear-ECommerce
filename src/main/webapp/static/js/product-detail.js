/**
 * Product Detail JavaScript
 * Handles AJAX loading of product info, related products, and add to cart.
 */

document.addEventListener('DOMContentLoaded', () => {
    // Initial fetch
    fetchProductDetails(productId);
    fetchRelatedProducts(productId);

    // Setup quantity controls
    setupQuantityControls();

    // Setup tab switching
    setupTabs();

    // Setup Add to Cart
    document.getElementById('add-to-cart-btn').addEventListener('click', () => {
        const quantity = parseInt(document.getElementById('qty-input').value);
        addToCart(productId, quantity);
    });
});

/**
 * Fetch product details from REST API
 */
async function fetchProductDetails(id) {
    try {
        const response = await fetch(`${contextPath}api/v1/products/${id}`);
        const result = await response.json();

        if (result.success && result.data) {
            renderProduct(result.data);
            hideLoading();
        } else {
            showError(result.message || 'Không thể tải thông tin sản phẩm');
        }
    } catch (error) {
        console.error('Error fetching product:', error);
        showError('Lỗi kết nối máy chủ');
    }
}

/**
 * Fetch related products from REST API
 */
async function fetchRelatedProducts(id) {
    try {
        const response = await fetch(`${contextPath}api/v1/products/${id}/related`);
        const result = await response.json();

        if (result.success && result.data) {
            renderRelatedProducts(result.data);
        }
    } catch (error) {
        console.error('Error fetching related products:', error);
    }
}

/**
 * Render product data to DOM
 */
function renderProduct(product) {
    // Breadcrumb
    document.getElementById('breadcrumb-category').textContent = product.categoryName;
    document.getElementById('breadcrumb-product').textContent = product.name;

    // Title & Info
    document.getElementById('product-name').textContent = product.name;
    document.getElementById('brand-name').textContent = `Thương hiệu: ${product.brandName}`;
    document.getElementById('product-description').innerHTML = product.description || 'Chưa có mô tả cho sản phẩm này.';
    document.getElementById('product-price').textContent = formatCurrency(product.price);

    // Status
    const stockStatus = document.getElementById('stock-status');
    if (product.stockQuantity > 0) {
        stockStatus.className = 'ag-status in-stock';
        stockStatus.innerHTML = `<i class="fas fa-check-circle"></i> Còn hàng (${product.stockQuantity})`;
    } else {
        stockStatus.className = 'ag-status out-of-stock';
        stockStatus.innerHTML = `<i class="fas fa-times-circle"></i> Hết hàng`;
        document.getElementById('add-to-cart-btn').disabled = true;
    }

    // Images
    const mainImage = document.getElementById('main-image');
    mainImage.src = getImageUrl(product.thumbnailUrl);

    const thumbContainer = document.getElementById('thumbnail-list');
    thumbContainer.innerHTML = '';

    // Add primary thumbnail
    addThumbnail(thumbContainer, getImageUrl(product.thumbnailUrl), true);

    // Add other images using Lambda-like forEach
    if (product.images) {
        product.images.forEach(imgUrl => {
            const formattedUrl = getImageUrl(imgUrl);
            if (formattedUrl !== getImageUrl(product.thumbnailUrl)) {
                addThumbnail(thumbContainer, formattedUrl, false);
            }
        });
    }

    // Specifications Table
    renderSpecs(product.specifications);
}

function addThumbnail(container, url, isActive) {
    const div = document.createElement('div');
    div.className = `ag-thumb-item ${isActive ? 'active' : ''}`;
    div.innerHTML = `<img src="${url}" alt="Thumbnail">`;
    div.onclick = () => {
        document.getElementById('main-image').src = url;
        document.querySelectorAll('.ag-thumb-item').forEach(t => t.classList.remove('active'));
        div.classList.add('active');
    };
    container.appendChild(div);
}

function renderSpecs(specsJson) {
    const table = document.getElementById('specs-table');
    table.innerHTML = '';
    if (!specsJson || specsJson === '') {
        table.innerHTML = '<tr><td colspan="2">Chưa có thông số kỹ thuật.</td></tr>';
        return;
    }

    try {
        const specs = JSON.parse(specsJson);
        // Sử dụng Object.entries kết hợp forEach (phong cách stream/lambda)
        Object.entries(specs).forEach(([key, value]) => {
            const row = document.createElement('tr');
            row.innerHTML = `<td>${key}</td><td>${value}</td>`;
            table.appendChild(row);
        });
    } catch (e) {
        console.error('Error parsing specs:', e);
        table.innerHTML = '<tr><td colspan="2">Định dạng thông số kỹ thuật không hợp lệ.</td></tr>';
    }
}

/**
 * Render related products using template string
 */
function renderRelatedProducts(products) {
    const container = document.getElementById('related-products-list');
    if (products.length === 0) {
        container.innerHTML = '<p>Không có sản phẩm liên quan.</p>';
        return;
    }

    // Sử dụng map và join (phong cách filter/reduce/stream)
    container.innerHTML = products.map(p => `
        <a href="${contextPath}product/${p.id}" class="ag-product-card">
            <div class="img-box">
                <img src="${getImageUrl(p.thumbnailUrl)}" alt="${p.name}">
            </div>
            <span class="title">${p.name}</span>
            <span class="price">${formatCurrency(p.price)}</span>
        </a>
    `).join('');
}

/**
 * Add product to cart via Facade Service (Proxy via CartController)
 */
async function addToCart(id, qty) {
    try {
        const response = await fetch(`${contextPath}cart/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `productId=${id}&quantity=${qty}`
        });

        if (response.ok) {
            showToast('Đã thêm sản phẩm vào giỏ hàng!', 'success');
            // Cập nhật số lượng trên header nếu có
            updateCartBadge();
        } else {
            showToast('Có lỗi xảy ra khi thêm vào giỏ hàng.', 'error');
        }
    } catch (error) {
        console.error('Error adding to cart:', error);
        showToast('Lỗi kết nối máy chủ', 'error');
    }
}

// Helpers
function getImageUrl(url) {
    if (!url) return `${contextPath}static/images/placeholder.png`;
    if (url.startsWith('http') || url.startsWith('data:')) return url;

    // url đã được resolve ở Java (có kèm /static/...)
    // Loại bỏ dấu '/' ở đầu vì contextPath thường là '/context/'
    const cleanUrl = url.startsWith('/') ? url.substring(1) : url;
    return `${contextPath}${cleanUrl}`;
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

function setupQuantityControls() {
    const input = document.getElementById('qty-input');
    document.getElementById('qty-minus').onclick = () => { if (input.value > 1) input.value--; };
    document.getElementById('qty-plus').onclick = () => { input.value++; };
}

function setupTabs() {
    document.querySelectorAll('.ag-tab-btn').forEach(btn => {
        btn.onclick = () => {
            document.querySelectorAll('.ag-tab-btn').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.ag-tab-pane').forEach(p => p.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById(`tab-${btn.dataset.tab}`).classList.add('active');
        };
    });
}

function hideLoading() {
    document.getElementById('loading-spinner').classList.add('ag-hidden');
    document.getElementById('product-content').classList.remove('ag-hidden');
}

function showError(msg) {
    const spinner = document.getElementById('loading-spinner');
    spinner.innerHTML = `<p class="error-msg"><i class="fas fa-exclamation-triangle"></i> ${msg}</p>`;
}

function showToast(message, type) {
    // Simple toast implementation
    const toast = document.createElement('div');
    toast.className = `ag-toast ${type}`;
    toast.textContent = message;

    // Add styles dynamically if not in CSS
    Object.assign(toast.style, {
        position: 'fixed',
        bottom: '30px',
        right: '30px',
        padding: '15px 30px',
        backgroundColor: type === 'success' ? '#27ae60' : '#e74c3c',
        color: '#fff',
        borderRadius: '8px',
        boxShadow: '0 5px 15px rgba(0,0,0,0.3)',
        zIndex: '9999',
        animation: 'fadeInUp 0.3s'
    });

    document.body.appendChild(toast);
    setTimeout(() => {
        toast.style.animation = 'fadeOutDown 0.3s';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function updateCartBadge() {
    // Fetch cart count and update header badge
    fetch(`${contextPath}cart/count`)
        .then(res => res.json())
        .then(data => {
            const badge = document.querySelector('.ag-cart-badge');
            if (badge) badge.textContent = data.count;
        });
}
