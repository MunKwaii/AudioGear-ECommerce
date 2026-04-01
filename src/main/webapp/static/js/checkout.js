/**
 * Checkout JavaScript
 * Handles delivery info collection and API call to process the order.
 */

document.addEventListener('DOMContentLoaded', () => {
    const checkoutForm = document.getElementById('checkout-form');
    const confirmBtn = document.getElementById('btn-confirm-checkout');
    const loader = document.getElementById('loader');

    // Handle payment option selection styling
    const paymentOptions = document.querySelectorAll('.payment-option');
    paymentOptions.forEach(opt => {
        opt.addEventListener('click', () => {
            paymentOptions.forEach(p => p.classList.remove('selected'));
            opt.classList.add('selected');
            opt.querySelector('input').checked = true;
        });
    });

    confirmBtn.addEventListener('click', async () => {
        // Simple validation
        if (!validateForm()) {
            showToast('Vui lòng điền đầy đủ thông tin giao hàng.', 'error');
            return;
        }

        // Prepare data
        const data = {
            email: document.getElementById('email').value,
            recipientName: document.getElementById('recipientName').value,
            phoneNumber: document.getElementById('phoneNumber').value,
            streetAddress: document.getElementById('streetAddress').value,
            city: document.getElementById('city').value,
            paymentMethod: document.querySelector('input[name="paymentMethod"]:checked').value,
            voucherCode: document.getElementById('voucherCode').value,
            // Items are already handled by the backend from the user's cart, 
            // but the API CheckoutRequest might expect them. 
            // Re-mapping items from the injected cartItems if necessary.
            items: cartItems.map(item => ({
                productId: item.productId,
                quantity: item.quantity
            }))
        };

        // Show loading
        loader.style.display = 'flex';
        confirmBtn.disabled = true;

        try {
            const response = await fetch(`${contextPath}api/v1/checkout`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (response.ok) {
                showToast('Đặt hàng thành công!', 'success');
                // Redirect to success page or home after a short delay
                setTimeout(() => {
                    window.location.href = `${contextPath}?orderCode=${result.orderCode || ''}&status=success`;
                }, 2000);
            } else {
                showToast(result.message || 'Thanh toán không thành công.', 'error');
                loader.style.display = 'none';
                confirmBtn.disabled = false;
            }
        } catch (error) {
            console.error('Checkout error:', error);
            showToast('Lỗi kết nối máy chủ.', 'error');
            loader.style.display = 'none';
            confirmBtn.disabled = false;
        }
    });

    function validateForm() {
        const requiredFields = ['email', 'recipientName', 'phoneNumber', 'streetAddress', 'city'];
        for (const field of requiredFields) {
            const el = document.getElementById(field);
            if (!el.value.trim()) {
                el.focus();
                return false;
            }
        }
        return true;
    }

    function showToast(message, type) {
        const toast = document.createElement('div');
        toast.className = `ag-toast ${type}`;
        toast.textContent = message;

        Object.assign(toast.style, {
            position: 'fixed',
            bottom: '30px',
            right: '30px',
            padding: '15px 30px',
            backgroundColor: type === 'success' ? '#27ae60' : '#e74c3c',
            color: '#fff',
            borderRadius: '8px',
            boxShadow: '0 5px 15px rgba(0,0,0,0.3)',
            zIndex: '10001',
            animation: 'fadeInUp 0.3s'
        });

        document.body.appendChild(toast);
        setTimeout(() => {
            toast.style.animation = 'fadeOutDown 0.3s';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }
});
