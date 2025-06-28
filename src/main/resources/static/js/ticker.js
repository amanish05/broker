// ticker.js
// Handles WebSocket for live ticker feed and ticker subscribe/disconnect actions
let ws;
function connectWebSocket() {
    ws = new WebSocket('ws://localhost:8080/ws/ticker');
    ws.onopen = () => {
        document.getElementById('ticker-table-container').innerHTML = '<p>Connected. Waiting for data...</p>';
    };
    ws.onmessage = (event) => {
        let data = JSON.parse(event.data);
        let html = '<table style="width:100%;margin-top:16px;"><tr><th>Token</th><th>Price</th><th>Timestamp</th></tr>';
        data.forEach(tick => {
            html += `<tr><td>${tick.token}</td><td>${tick.price}</td><td>${tick.timestamp}</td></tr>`;
        });
        html += '</table>';
        document.getElementById('ticker-table-container').innerHTML = html;
    };
    ws.onclose = () => {
        document.getElementById('ticker-table-container').innerHTML = '<p>WebSocket disconnected.</p>';
    };
}
// Connect WebSocket and load subscription data on home page load
document.addEventListener('DOMContentLoaded', function() {
    // Connect WebSocket
    let ws = new WebSocket('ws://localhost:8080/ws/ticker');
    ws.onopen = () => {
        document.getElementById('ticker-table-container').innerHTML = '<p>Connected. Waiting for data...</p>';
    };
    ws.onmessage = (event) => {
        let data = JSON.parse(event.data);
        let html = '<table style="width:100%;margin-top:16px;"><tr><th>Token</th><th>Price</th><th>Timestamp</th></tr>';
        data.forEach(tick => {
            html += `<tr><td>${tick.token}</td><td>${tick.price}</td><td>${tick.timestamp}</td></tr>`;
        });
        html += '</table>';
        document.getElementById('ticker-table-container').innerHTML = html;
    };
    ws.onclose = () => {
        document.getElementById('ticker-table-container').innerHTML = '<p>WebSocket disconnected.</p>';
    };

    // Load current ticker subscriptions
    fetch('/api/ticker/subscriptions', { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            if (Array.isArray(data) && data.length > 0) {
                let html = '<ul>';
                data.forEach(token => {
                    html += `<li>Subscribed: ${token}</li>`;
                });
                html += '</ul>';
                document.getElementById('api-action-result').innerHTML = html;
            } else {
                document.getElementById('api-action-result').innerHTML = '<p>No active ticker subscriptions.</p>';
            }
        });
});
// Uncomment to auto-connect WebSocket on page load:
// document.addEventListener('DOMContentLoaded', connectWebSocket);
