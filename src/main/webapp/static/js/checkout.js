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
            opt.querySelector('input[type="radio"]').checked = true;
        });
    });

    // Handle address option selection styling
    const addressOptions = document.querySelectorAll('.address-option');
    addressOptions.forEach(opt => {
        opt.addEventListener('click', () => {
            addressOptions.forEach(p => p.classList.remove('selected'));
            opt.classList.add('selected');
            opt.querySelector('input[type="radio"]').checked = true;
        });
    });

    if (typeof isGuest !== 'undefined' && isGuest) {
        // Load Provinces
        fetch('https://provinces.open-api.vn/api/p/')
            .then(res => res.json())
            .then(data => {
                const provinceSelect = document.getElementById('provinceSelect');
                if (provinceSelect) {
                    data.forEach(p => {
                        const option = document.createElement('option');
                        option.value = p.name; // Use name directly to send as 'city'
                        option.dataset.code = p.code;
                        option.textContent = p.name;
                        provinceSelect.appendChild(option);
                    });
                }
            })
            .catch(err => console.error("Error loading provinces:", err));

        const provinceSelect = document.getElementById('provinceSelect');
        if (provinceSelect) {
            provinceSelect.addEventListener('change', function() {
                const pCode = this.options[this.selectedIndex].dataset.code;
                const districtSelect = document.getElementById('districtSelect');
                const wardSelect = document.getElementById('wardSelect');
                
                districtSelect.style.pointerEvents = 'none';
                districtSelect.style.opacity = '0.6';
                districtSelect.innerHTML = '<option value="" disabled selected>Đang tải...</option>';
                
                wardSelect.style.pointerEvents = 'none';
                wardSelect.style.opacity = '0.6';
                wardSelect.innerHTML = '<option value="" disabled selected>Chọn Phường/Xã</option>';
                
                fetch(`https://provinces.open-api.vn/api/p/${pCode}?depth=2`)
                    .then(res => res.json())
                    .then(data => {
                        districtSelect.innerHTML = '<option value="" disabled selected>Chọn Quận/Huyện</option>';
                        data.districts.forEach(d => {
                            const option = document.createElement('option');
                            option.value = d.name;
                            option.dataset.code = d.code;
                            option.textContent = d.name;
                            districtSelect.appendChild(option);
                        });
                        districtSelect.style.pointerEvents = 'auto';
                        districtSelect.style.opacity = '1';
                    });
            });
        }

        const districtSelect = document.getElementById('districtSelect');
        if (districtSelect) {
            districtSelect.addEventListener('change', function() {
                const dCode = this.options[this.selectedIndex].dataset.code;
                const wardSelect = document.getElementById('wardSelect');
                
                wardSelect.style.pointerEvents = 'none';
                wardSelect.style.opacity = '0.6';
                wardSelect.innerHTML = '<option value="" disabled selected>Đang tải...</option>';
                
                fetch(`https://provinces.open-api.vn/api/d/${dCode}?depth=2`)
                    .then(res => res.json())
                    .then(data => {
                        wardSelect.innerHTML = '<option value="" disabled selected>Chọn Phường/Xã</option>';
                        data.wards.forEach(w => {
                            const option = document.createElement('option');
                            option.value = w.name;
                            option.dataset.code = w.code;
                            option.textContent = w.name;
                            wardSelect.appendChild(option);
                        });
                        wardSelect.style.pointerEvents = 'auto';
                        wardSelect.style.opacity = '1';
                    });
            });
        }
    }

    confirmBtn.addEventListener('click', async () => {
        // Simple validation
        if (!validateForm()) {
            showToast('Vui lòng điền đầy đủ thông tin giao hàng.', 'error');
            return;
        }

        // Prepare data
        let data = {};
        if (typeof isGuest !== 'undefined' && isGuest) {
            const ward = document.getElementById('wardSelect').value;
            const district = document.getElementById('districtSelect').value;
            const street = document.getElementById('streetAddress').value;
            
            data = {
                email: document.getElementById('email').value,
                recipientName: document.getElementById('recipientName').value,
                phoneNumber: document.getElementById('phoneNumber').value,
                streetAddress: `${street}, ${ward}, ${district}`,
                city: document.getElementById('provinceSelect').value,
                paymentMethod: document.querySelector('input[name="paymentMethod"]:checked').value,
                voucherCode: document.getElementById('voucherCode').value,
                items: cartItems.map(item => ({
                    productId: item.productId,
                    quantity: item.quantity
                }))
            };
        } else {
            const selectedAddressEl = document.querySelector('input[name="selectedAddress"]:checked');
            const ward = selectedAddressEl.dataset.ward;
            const district = selectedAddressEl.dataset.district;
            const street = selectedAddressEl.dataset.street;
            
            data = {
                email: document.getElementById('userEmailConfig').value,
                recipientName: selectedAddressEl.dataset.name,
                phoneNumber: selectedAddressEl.dataset.phone,
                streetAddress: `${street}, ${ward}, ${district}`,
                city: selectedAddressEl.dataset.province,
                paymentMethod: document.querySelector('input[name="paymentMethod"]:checked').value,
                voucherCode: document.getElementById('voucherCode').value,
                items: cartItems.map(item => ({
                    productId: item.productId,
                    quantity: item.quantity
                }))
            };
        }

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
                const method = result.paymentMethod || data.paymentMethod;
                if (method && method.toUpperCase() === 'SEPAY_QR') {
                    showToast('Đặt hàng thành công! Đang chuyển đến trang thanh toán...', 'success');
                } else {
                    showToast('Đặt hàng thành công!', 'success');
                }
                // Redirect to success page or payment page after a short delay
                setTimeout(() => {
                    console.log("Check result redirect:", result);
                    console.log("Final detected method:", method);
                    
                    if (method && method.toUpperCase() === 'SEPAY_QR') {
                        console.log("-> REDIRECTING TO QR PAGE");
                        window.location.href = `${contextPath}payment?orderCode=${result.orderCode || ''}`;
                    } else {
                        console.log("-> REDIRECTING TO SUCCESS PAGE");
                        window.location.href = `${contextPath}?orderCode=${result.orderCode || ''}&status=success`;
                    }
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
        if (typeof isGuest !== 'undefined' && isGuest) {
            const requiredFields = ['email', 'recipientName', 'phoneNumber', 'streetAddress', 'provinceSelect', 'districtSelect', 'wardSelect'];
            for (const field of requiredFields) {
                const el = document.getElementById(field);
                if (el && !el.value.trim()) {
                    el.focus();
                    return false;
                }
            }
        } else {
            const emailEl = document.getElementById('userEmailConfig');
            if (emailEl && !emailEl.value.trim()) {
                emailEl.focus();
                return false;
            }
            const selectedAddress = document.querySelector('input[name="selectedAddress"]:checked');
            if (!selectedAddress) {
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
