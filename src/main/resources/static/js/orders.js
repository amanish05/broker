// orders.js
// Handles order placement using the values from the subscription dropdowns
// and quantity/price fields

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('order-form');
    if (!form) return;
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        const name = document.getElementById('api-input').value;
        const payload = {
            tradingsymbol: name,
            exchange: document.getElementById('exchange-select').value,
            transactionType: document.getElementById('order-side').value,
            quantity: parseInt(document.getElementById('order-qty').value, 10),
            price: document.getElementById('order-price').value ? parseFloat(document.getElementById('order-price').value) : null,
            instrumentToken: window.nameTokenMap ? window.nameTokenMap[name] : null
        };
        fetch('/api/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(payload)
        })
        .then(res => res.json())
        .then(data => {
            console.info('Order placed', data);
            document.getElementById('order-result').innerText =
                data.orderId ? `Order placed with ID ${data.orderId}` : 'Order sent';
        })
        .catch(err => {
            console.error('Order failed', err);
            document.getElementById('order-result').innerText = 'Error placing order';
        });
    });
});
