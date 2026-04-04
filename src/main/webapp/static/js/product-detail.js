/**
 * Product Detail JavaScript
 * Handles AJAX loading of product info, related products, add to cart, and reviews.
 */

let currentSortBy = 'newest';
let selectedRating = 0;

document.addEventListener('DOMContentLoaded', () => {
    // Initial fetch
    fetchProductDetails(productId);
    fetchRelatedProducts(productId);
    fetchReviews(productId, currentSortBy);

    // Setup quantity controls
    setupQuantityControls();

    // Setup tab switching
    setupTabs();

    // Setup review features
    setupSortControls();
    setupReviewModal();

    // Setup Add to Cart
    document.getElementById('add-to-cart-btn').addEventListener('click', () => {
        const quantity = parseInt(document.getElementById('qty-input').value);
        addToCart(productId, quantity, false);
    });

    // Setup Buy Now
    document.getElementById('buy-now-btn').addEventListener('click', () => {
        const quantity = parseInt(document.getElementById('qty-input').value);
        addToCart(productId, quantity, true);
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
    
    // Set max quantity for input
    const qtyInput = document.getElementById('qty-input');
    if (qtyInput) {
        qtyInput.setAttribute('max', product.stockQuantity);
    }

    // Status & Buttons
    const stockStatus = document.getElementById('stock-status');
    const outOfStockMsg = document.getElementById('out-of-stock-message');
    const addToCartBtn = document.getElementById('add-to-cart-btn');
    const buyNowBtn = document.getElementById('buy-now-btn');
    // qtyInput is already declared above at line 83

    if (product.stockQuantity > 0) {
        stockStatus.className = 'ag-status in-stock';
        stockStatus.innerHTML = `<i class="fas fa-check-circle"></i> Còn hàng (${product.stockQuantity})`;
        outOfStockMsg.classList.add('ag-hidden');
        addToCartBtn.disabled = false;
        buyNowBtn.disabled = false;
        if (qtyInput) qtyInput.disabled = false;
    } else {
        stockStatus.className = 'ag-status out-of-stock';
        stockStatus.innerHTML = `<i class="fas fa-times-circle"></i> Hết hàng`;
        outOfStockMsg.classList.remove('ag-hidden');
        addToCartBtn.disabled = true;
        buyNowBtn.disabled = true;
        if (qtyInput) qtyInput.disabled = true;
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
        const specs = typeof specsJson === 'string' ? JSON.parse(specsJson) : specsJson;
        const isObject = specs && typeof specs === 'object' && !Array.isArray(specs);
        if (!isObject) {
            table.innerHTML = '<tr><td colspan="2">Định dạng thông số kỹ thuật không hợp lệ.</td></tr>';
            return;
        }

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
 * @param {boolean} redirect - If true, redirect to cart page after success
 */
async function addToCart(id, qty, redirect) {
    try {
        const response = await fetch(`${contextPath}cart/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `productId=${id}&quantity=${qty}`
        });

        if (response.ok) {
            if (redirect) {
                window.location.href = `${contextPath}cart`;
            } else {
                showToast('Đã thêm sản phẩm vào giỏ hàng!', 'success');
                // Cập nhật số lượng trên header nếu có
                updateCartBadge();
            }
        } else {
            let errorMsg = 'Có lỗi xảy ra khi thêm vào giỏ hàng.';
            try {
                const result = await response.json();
                if (result.message) errorMsg = result.message;
            } catch (e) {
                // Not JSON, use default
            }
            showToast(errorMsg, 'error');
        }
    } catch (error) {
        console.error('Error adding to cart:', error);
        showToast('Lỗi kết nối máy chủ', 'error');
    }
}

// ========== REVIEW FUNCTIONS ==========

/**
 * Fetch reviews from REST API
 */
async function fetchReviews(id, sortBy) {
    try {
        const headers = {};
        const token = getCookie('accessToken');
        if (token) headers['Authorization'] = 'Bearer ' + token;

        const url = `${contextPath}api/v1/products/${id}/reviews?sortBy=${sortBy || 'newest'}`;
        const response = await fetch(url, { headers });
        const result = await response.json();

        if (result.success && result.data) {
            renderReviews(result.data);
        } else {
            document.getElementById('review-list').innerHTML =
                '<p class="ag-reviews-empty">Không có đánh giá nào.</p>';
        }
    } catch (error) {
        console.error('Error fetching reviews:', error);
        document.getElementById('review-list').innerHTML =
            '<p class="ag-reviews-empty">Lỗi khi tải đánh giá.</p>';
    }
}

/**
 * Render all review data: summary, histogram, review list
 */
function renderReviews(data) {
    const { summary, ratingDistribution, reviews } = data;

    // Update summary
    document.getElementById('review-avg-rating').textContent = summary.averageRating;
    document.getElementById('review-avg-stars').innerHTML = renderStars(summary.averageRating);
    document.getElementById('review-total').textContent = `${summary.reviewCount} đánh giá`;

    // Update tab button count
    const reviewTabBtn = document.querySelector('.ag-tab-btn[data-tab="reviews"]');
    if (reviewTabBtn) {
        reviewTabBtn.textContent = `Đánh giá (${summary.reviewCount})`;
    }

    // Update product rating in info section
    const ratingSpan = document.querySelector('.ag-rating span');
    if (ratingSpan) {
        ratingSpan.textContent = `(${summary.averageRating}/5 - ${summary.reviewCount} đánh giá)`;
    }
    const ratingDiv = document.querySelector('.ag-rating');
    if (ratingDiv && summary.reviewCount > 0) {
        ratingDiv.innerHTML = renderStars(summary.averageRating) +
            ` <span>(${summary.averageRating}/5 - ${summary.reviewCount} đánh giá)</span>`;
    }

    // Update histogram
    const dist = ratingDistribution || {};
    const totalReviews = summary.reviewCount || 1;
    for (let star = 1; star <= 5; star++) {
        const count = dist[star] || 0;
        const pct = Math.round((count / totalReviews) * 100);
        const fill = document.querySelector(`.ag-histogram-fill[data-star="${star}"]`);
        const countEl = document.querySelector(`.ag-histogram-count[data-star="${star}"]`);
        if (fill) fill.style.width = pct + '%';
        if (countEl) countEl.textContent = count;
    }

    // Show/hide write review button
    const writeBtn = document.getElementById('btn-write-review');
    if (writeBtn) {
        const token = getCookie('accessToken');
        if (token) {
            writeBtn.classList.remove('ag-hidden');
        } else {
            writeBtn.classList.add('ag-hidden');
        }
    }

    // Render review list
    const listContainer = document.getElementById('review-list');
    if (!reviews || reviews.length === 0) {
        listContainer.innerHTML = '<p class="ag-reviews-empty">Chưa có đánh giá nào. Hãy là người đầu tiên đánh giá!</p>';
        return;
    }

    listContainer.innerHTML = reviews.map(r => renderReviewCard(r)).join('');
}

/**
 * Render a single review card
 */
function renderReviewCard(review) {
    const timeAgo = formatTimeAgo(review.createdAt);
    const token = getCookie('accessToken');
    
    // Parse token to get current user ID (for delete button)
    let currentUserId = null;
    if (token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            currentUserId = payload.userId || payload.id;
        } catch (e) {}
    }
    const isOwner = currentUserId && review.userId === currentUserId;

    return `
        <div class="ag-review-card" data-review-id="${review.id}">
            <div class="ag-review-header">
                <div class="ag-review-avatar">${getInitials(review.userName)}</div>
                <div class="ag-review-info">
                    <span class="ag-review-name">${escapeHtml(review.userName)}</span>
                    <span class="ag-review-time">${timeAgo}</span>
                </div>
                ${isOwner ? `
                    <button class="ag-review-delete-btn" title="Xóa đánh giá" onclick="handleDeleteReview(${review.id})">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                ` : ''}
            </div>
            <div class="ag-review-stars">${renderStars(review.rating)}</div>
            <p class="ag-review-comment">${escapeHtml(review.comment || '')}</p>
            <div class="ag-review-actions">
                <button class="ag-like-btn ${review.isLiked ? 'liked' : ''}"
                        onclick="handleLike(${review.id})"
                        ${!token ? 'title="Đăng nhập để thích"' : ''}>
                    <i class="${review.isLiked ? 'fas' : 'far'} fa-heart"></i>
                    <span>${review.totalLikes}</span>
                </button>
            </div>
        </div>
    `;
}

/**
 * Render star rating HTML from numeric rating
 */
function renderStars(rating) {
    const full = Math.floor(rating);
    const half = rating - full >= 0.5 ? 1 : 0;
    const empty = 5 - full - half;
    let html = '';
    for (let i = 0; i < full; i++) html += '<i class="fas fa-star"></i>';
    if (half) html += '<i class="fas fa-star-half-alt"></i>';
    for (let i = 0; i < empty; i++) html += '<i class="far fa-star"></i>';
    return html;
}

/**
 * Handle like/unlike toggle
 */
async function handleLike(reviewId) {
    const token = getCookie('accessToken');
    if (!token) {
        showToast('Vui lòng đăng nhập để thích đánh giá', 'error');
        return;
    }

    try {
        const response = await fetch(`${contextPath}api/v1/reviews/${reviewId}/like`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();

        if (response.ok) {
            // Update the like button in-place without re-fetching
            const card = document.querySelector(`.ag-review-card[data-review-id="${reviewId}"]`);
            if (card) {
                const btn = card.querySelector('.ag-like-btn');
                const icon = btn.querySelector('i');
                const countSpan = btn.querySelector('span');

                if (result.liked) {
                    btn.classList.add('liked');
                    icon.className = 'fas fa-heart';
                } else {
                    btn.classList.remove('liked');
                    icon.className = 'far fa-heart';
                }
                countSpan.textContent = result.totalLikes;

                // Pulse animation
                btn.classList.add('ag-like-pulse');
                setTimeout(() => btn.classList.remove('ag-like-pulse'), 400);
            }
        } else {
            showToast(result.message || 'Không thể thích đánh giá', 'error');
        }
    } catch (error) {
        console.error('Error toggling like:', error);
        showToast('Lỗi kết nối máy chủ', 'error');
    }
}

/**
 * Submit a new review
 */
async function handleSubmitReview() {
    const token = getCookie('accessToken');
    if (!token) {
        showToast('Vui lòng đăng nhập để đánh giá', 'error');
        return;
    }

    if (selectedRating === 0) {
        showToast('Vui lòng chọn số sao', 'error');
        return;
    }

    const comment = document.getElementById('review-comment').value.trim();
    const btn = document.getElementById('btn-submit-review');
    btn.disabled = true;
    btn.textContent = 'Đang gửi...';

    try {
        const response = await fetch(`${contextPath}api/v1/reviews`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                productId: parseInt(productId),
                rating: selectedRating,
                comment: comment
            })
        });

        const result = await response.json();

        if (response.ok || response.status === 201) {
            showToast('Đánh giá đã được gửi!', 'success');
            closeReviewModal();
            // Re-fetch reviews
            fetchReviews(productId, currentSortBy);
        } else {
            showToast(result.message || 'Không thể gửi đánh giá', 'error');
        }
    } catch (error) {
        console.error('Error submitting review:', error);
        showToast('Lỗi kết nối máy chủ', 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Gửi đánh giá';
    }
}

/**
 * Handle delete review
 */
async function handleDeleteReview(reviewId) {
    if (!confirm('Bạn có chắc chắn muốn xóa đánh giá này?')) return;

    const token = getCookie('accessToken');
    try {
        const response = await fetch(`${contextPath}api/v1/reviews/${reviewId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });

        const result = await response.json();

        if (response.ok) {
            showToast('Đã xóa đánh giá', 'success');
            fetchReviews(productId, currentSortBy);
        } else {
            showToast(result.message || 'Không thể xóa đánh giá', 'error');
        }
    } catch (error) {
        console.error('Error deleting review:', error);
        showToast('Lỗi kết nối máy chủ', 'error');
    }
}

/**
 * Setup sort controls event listeners
 */
function setupSortControls() {
    document.querySelectorAll('.ag-sort-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.ag-sort-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentSortBy = btn.dataset.sort;
            fetchReviews(productId, currentSortBy);
        });
    });
}

/**
 * Setup review modal: star selector, submit, open/close
 */
function setupReviewModal() {
    // Write review button
    document.getElementById('btn-write-review').addEventListener('click', openReviewModal);

    // Close modal
    document.getElementById('btn-close-modal').addEventListener('click', closeReviewModal);

    // Click overlay to close
    document.getElementById('review-modal').addEventListener('click', (e) => {
        if (e.target.id === 'review-modal') closeReviewModal();
    });

    // Submit review
    document.getElementById('btn-submit-review').addEventListener('click', handleSubmitReview);

    // Star selector
    const stars = document.querySelectorAll('#star-selector i');
    const starLabels = ['', 'Rất tệ', 'Tệ', 'Bình thường', 'Tốt', 'Rất tốt'];

    stars.forEach(star => {
        star.addEventListener('click', () => {
            selectedRating = parseInt(star.dataset.value);
            updateStarDisplay(selectedRating);
            document.getElementById('star-label').textContent = starLabels[selectedRating];
        });

        star.addEventListener('mouseenter', () => {
            const val = parseInt(star.dataset.value);
            highlightStars(val);
        });
    });

    document.getElementById('star-selector').addEventListener('mouseleave', () => {
        updateStarDisplay(selectedRating);
    });

    // Character counter
    document.getElementById('review-comment').addEventListener('input', (e) => {
        document.getElementById('char-count').textContent = e.target.value.length;
    });
}

function openReviewModal() {
    selectedRating = 0;
    updateStarDisplay(0);
    document.getElementById('review-comment').value = '';
    document.getElementById('char-count').textContent = '0';
    document.getElementById('star-label').textContent = 'Chọn số sao';
    document.getElementById('review-modal').classList.remove('ag-hidden');
}

function closeReviewModal() {
    document.getElementById('review-modal').classList.add('ag-hidden');
}

function updateStarDisplay(rating) {
    document.querySelectorAll('#star-selector i').forEach((star, index) => {
        if (index < rating) {
            star.className = 'fas fa-star';
        } else {
            star.className = 'far fa-star';
        }
    });
}

function highlightStars(upTo) {
    document.querySelectorAll('#star-selector i').forEach((star, index) => {
        star.className = index < upTo ? 'fas fa-star' : 'far fa-star';
    });
}

// ========== HELPERS ==========

function getInitials(name) {
    if (!name) return '?';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}

function formatTimeAgo(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now - date;
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);

    if (diffSec < 60) return 'Vừa xong';
    if (diffMin < 60) return `${diffMin} phút trước`;
    if (diffHour < 24) return `${diffHour} giờ trước`;
    if (diffDay < 30) return `${diffDay} ngày trước`;

    return date.toLocaleDateString('vi-VN');
}

/**
 * Get cookie value by name
 */
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
function getImageUrl(url) {
    if (!url) return `${contextPath}static/images/placeholder.png`;
    if (url.startsWith('http') || url.startsWith('data:') || url.startsWith('blob:')) return url;

    let path = url;
    if (!path.startsWith('/')) path = '/' + path;

    // Nếu path bắt đầu bằng /images/products/ mà thiếu /static thì thêm vào
    if (path.startsWith('/images/products/') && !path.startsWith('/static/')) {
        path = '/static' + path;
    }

    const cleanUrl = path.startsWith('/') ? path.substring(1) : path;
    return `${contextPath}${cleanUrl}`;
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

function setupQuantityControls() {
    const input = document.getElementById('qty-input');
    const stockStatus = document.getElementById('stock-status');
    
    // Extract stock count from product data if available
    // We can store it in a global or data attribute. Let's use a hidden data attribute or just parse the text.
    // Better: let's set the max attribute when rendering product.
    
    document.getElementById('qty-minus').onclick = () => { 
        if (parseInt(input.value) > 1) input.value = parseInt(input.value) - 1; 
    };
    
    document.getElementById('qty-plus').onclick = () => { 
        const max = parseInt(input.getAttribute('max')) || Infinity;
        if (parseInt(input.value) < max) {
            input.value = parseInt(input.value) + 1; 
        } else {
            showToast(`Chỉ còn ${max} sản phẩm trong kho.`, 'error');
        }
    };

    input.onchange = () => {
        const max = parseInt(input.getAttribute('max')) || Infinity;
        let val = parseInt(input.value);
        if (isNaN(val) || val < 1) input.value = 1;
        else if (val > max) {
            input.value = max;
            showToast(`Chỉ còn ${max} sản phẩm trong kho.`, 'error');
        }
    };
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
    if (typeof window.updateHeaderCartBadge === 'function') {
        window.updateHeaderCartBadge();
    } else {
        // Fallback for cases where header might not be loaded yet or used differently
        fetch(`${contextPath}cart/count`)
            .then(res => res.json())
            .then(data => {
                const badge = document.querySelector('.ag-cart-badge');
                if (badge) {
                    badge.textContent = data.count;
                    badge.style.display = data.count > 0 ? 'block' : 'none';
                }
            });
    }
}
