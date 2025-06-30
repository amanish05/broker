// ticker.js
// Handles WebSocket for live ticker feed and ticker subscribe/disconnect actions
let ws;

function initializeTicker() {
    // Called after configuration is loaded
    console.log('Ticker module initialized with config');
}

function connectWebSocket() {
    const wsUrl = getWebSocketUrl('ticker');
    ws = new WebSocket(wsUrl);
    console.log('Connecting to WebSocket:', wsUrl);
    ws.onopen = () => {
        console.info('WebSocket opened');
        document.getElementById('ticker-table-container').innerHTML = '<p>Connected. Waiting for data...</p>';
    };
    ws.onmessage = (event) => {
        let message = JSON.parse(event.data);
        console.debug('Received message', message);
        
        if (message.type === 'connection') {
            console.info('WebSocket connection confirmed:', message.status);
            return;
        }
        
        if (message.type === 'ticker' && message.data) {
            let tick = message.data;
            let html = '<table style="width:100%;margin-top:16px;"><tr><th>Token</th><th>Last Price</th><th>Volume</th><th>Change</th></tr>';
            html += `<tr><td>${tick.instrumentToken}</td><td>${tick.lastPrice}</td><td>${tick.volumeTraded}</td><td>${tick.netChange}</td></tr>`;
            html += '</table>';
            document.getElementById('ticker-table-container').innerHTML = html;
        }
    };
    ws.onclose = () => {
        console.warn('WebSocket closed');
        document.getElementById('ticker-table-container').innerHTML = '<p>WebSocket disconnected.</p>';
    };
}
// Connect WebSocket and load subscription data on home page load
document.addEventListener('DOMContentLoaded', function() {
    // Connect WebSocket
    let ws = new WebSocket('ws://localhost:8080/ws/ticker');
    ws.onopen = () => {
        console.info('WebSocket opened');
        document.getElementById('ticker-table-container').innerHTML = '<p>Connected. Waiting for data...</p>';
    };
    ws.onmessage = (event) => {
        let message = JSON.parse(event.data);
        console.debug('Received message', message);
        
        if (message.type === 'connection') {
            console.info('WebSocket connection confirmed:', message.status);
            return;
        }
        
        if (message.type === 'ticker' && message.data) {
            let tick = message.data;
            let html = '<table style="width:100%;margin-top:16px;"><tr><th>Token</th><th>Last Price</th><th>Volume</th><th>Change</th></tr>';
            html += `<tr><td>${tick.instrumentToken}</td><td>${tick.lastPrice}</td><td>${tick.volumeTraded}</td><td>${tick.netChange}</td></tr>`;
            html += '</table>';
            document.getElementById('ticker-table-container').innerHTML = html;
        }
    };
    ws.onclose = () => {
        console.warn('WebSocket closed');
        document.getElementById('ticker-table-container').innerHTML = '<p>WebSocket disconnected.</p>';
    };

    // Load current ticker subscriptions
    fetch('/api/ticker/subscriptions', { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            console.debug('Current subscriptions', data);
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
