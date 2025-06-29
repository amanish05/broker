// orders.js
// Handles placing orders via REST API

document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('order-form').addEventListener('submit', function(e) {
        e.preventDefault();
        const name = document.getElementById('order-name').value;
        const token = window.nameTokenMap ? window.nameTokenMap[name] : undefined;
        const qty = document.getElementById('order-qty').value;
        const price = document.getElementById('order-price').value;
        const side = document.getElementById('order-side').value;
        if (!name || !qty) { return; }
        const payload = {
            tradingsymbol: name,
            instrumentToken: token,
            exchange: document.getElementById('exchange-select').value,
            quantity: parseInt(qty, 10),
            price: price ? parseFloat(price) : null,
            transactionType: side
        };
        console.info('Submitting order', payload);
        fetch('/api/orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        })
        .then(r => r.json())
        .then(data => {
            console.debug('Order response', data);
            document.getElementById('order-result').innerText = 'Order placed: ' + data.orderId;
        })
        .catch(err => {
            console.error('Order failed', err);
            document.getElementById('order-result').innerText = 'Order failed';
        });
    });
});
