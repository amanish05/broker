// portfolio.js
// Fetches and renders portfolio holdings
document.addEventListener('DOMContentLoaded', function() {
    if (typeof validateSession === 'function') validateSession(); // Defensive: ensure session is valid
    fetch('/api/portfolio/holdings')
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                const holdings = data.data;
                let html = '<table style="width:100%;margin-top:16px;"><tr><th>Symbol</th><th>Exchange</th><th>Quantity</th><th>Avg Price</th><th>Last Price</th><th>P&amp;L</th></tr>';
                holdings.forEach(h => {
                    html += `<tr><td>${h.tradingsymbol}</td><td>${h.exchange}</td><td>${h.quantity}</td><td>${h.average_price}</td><td>${h.last_price}</td><td>${h.pnl}</td></tr>`;
                });
                html += '</table>';
                document.getElementById('holdings-table-container').innerHTML = html;
            } else {
                document.getElementById('holdings-table-container').innerHTML = '<p>Failed to load holdings.</p>';
            }
        })
        .catch(() => {
            document.getElementById('holdings-table-container').innerHTML = '<p>Error loading holdings.</p>';
        });
});
