// orders.js
// Handles order placement using the values from the subscription dropdowns
// and quantity/price fields

function buildOrderPayload() {
    const name = document.getElementById('order-name').value;
    const token = window.nameTokenMap ? window.nameTokenMap[name] : undefined;
    const qty = document.getElementById('order-qty').value;
    const price = document.getElementById('order-price').value;
    const side = document.getElementById('order-side').value;
    const orderType = document.getElementById('order-type').value;
    const product = document.getElementById('order-product').value;
    
    if (!name || !qty) {
        throw new Error('Instrument name and quantity are required');
    }
    
    if (orderType === 'LIMIT' && !price) {
        throw new Error('Price is required for LIMIT orders');
    }
    
    return {
        tradingsymbol: name,
        instrumentToken: token,
        exchange: document.getElementById('exchange-select').value,
        quantity: parseInt(qty, 10),
        price: price ? parseFloat(price) : null,
        transactionType: side,
        orderType: orderType,
        product: product
    };
}

function validateOrder() {
    try {
        const payload = buildOrderPayload();
        console.info('Order validation', payload);
        
        // Basic validation
        let warnings = [];
        if (payload.quantity > 1000) {
            warnings.push('Large quantity order (>1000 shares)');
        }
        if (payload.transactionType === 'SELL' && payload.product === 'CNC') {
            warnings.push('Selling CNC - ensure you have holdings');
        }
        
        let message = 'Order validation successful';
        if (warnings.length > 0) {
            message += '\n⚠️ Warnings: ' + warnings.join(', ');
        }
        
        document.getElementById('order-result').innerHTML = `
            <div style="background:#d4edda;color:#155724;padding:8px;border-radius:4px;">
                ${message.replace(/\n/g, '<br>')}
            </div>
        `;
    } catch (error) {
        document.getElementById('order-result').innerHTML = `
            <div style="background:#f8d7da;color:#721c24;padding:8px;border-radius:4px;">
                ❌ Validation failed: ${error.message}
            </div>
        `;
    }
}

function submitOrder() {
    try {
        const payload = buildOrderPayload();
        console.info('Submitting order', payload);
        
        document.getElementById('order-result').innerHTML = '<p>Placing order...</p>';

        fetch('/api/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(payload)
        })
        .then(res => res.json())
        .then(data => {
            console.debug('Order response', data);
            document.getElementById('order-result').innerHTML = `
                <div style="background:#d4edda;color:#155724;padding:8px;border-radius:4px;">
                    ✅ Order placed successfully: ${data.orderId}
                </div>
            `;
            // Refresh orders list
            loadOrders();
        })
        .catch(err => {
            console.error('Order failed', err);
            document.getElementById('order-result').innerHTML = `
                <div style="background:#f8d7da;color:#721c24;padding:8px;border-radius:4px;">
                    ❌ Order failed: ${err.message}
                </div>
            `;
        });
    } catch (error) {
        document.getElementById('order-result').innerHTML = `
            <div style="background:#f8d7da;color:#721c24;padding:8px;border-radius:4px;">
                ❌ ${error.message}
            </div>
        `;
    }
}

function loadOrders() {
    fetch('/api/orders', { credentials: 'include' })
        .then(r => r.json())
        .then(data => {
            console.debug('Orders loaded', data);
            if (Array.isArray(data) && data.length > 0) {
                let html = '<table style="width:100%;margin-top:16px;"><tr><th>Order ID</th><th>Symbol</th><th>Type</th><th>Qty</th><th>Price</th><th>Status</th><th>Time</th></tr>';
                data.forEach(order => {
                    html += `<tr>
                        <td>${order.orderId || 'N/A'}</td>
                        <td>${order.tradingsymbol || 'N/A'}</td>
                        <td>${order.transactionType || 'N/A'}</td>
                        <td>${order.quantity || 'N/A'}</td>
                        <td>${order.price || 'Market'}</td>
                        <td>${order.status || 'PENDING'}</td>
                        <td>${order.timestamp ? new Date(order.timestamp).toLocaleString() : 'N/A'}</td>
                    </tr>`;
                });
                html += '</table>';
                document.getElementById('orders-table-container').innerHTML = html;
            } else {
                document.getElementById('orders-table-container').innerHTML = '<p>No orders found.</p>';
            }
        })
        .catch(err => {
            console.error('Failed to load orders', err);
            document.getElementById('orders-table-container').innerHTML = '<p>Failed to load orders.</p>';
        });
}

// Enable/disable price field based on order type
function togglePriceField() {
    const orderType = document.getElementById('order-type').value;
    const priceField = document.getElementById('order-price');
    
    if (orderType === 'MARKET') {
        priceField.disabled = true;
        priceField.placeholder = 'Market price (auto)';
        priceField.value = '';
    } else {
        priceField.disabled = false;
        priceField.placeholder = 'Enter limit price';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    // Order form submission
    document.getElementById('order-form').addEventListener('submit', function(e) {
        e.preventDefault();
        submitOrder();
    });
    
    // Validate order button
    document.getElementById('validate-order').addEventListener('click', validateOrder);
    
    // Refresh orders button
    document.getElementById('refresh-orders').addEventListener('click', loadOrders);
    
    // Order type change handler
    document.getElementById('order-type').addEventListener('change', togglePriceField);
    
    // Initialize
    togglePriceField();
    loadOrders();
});
