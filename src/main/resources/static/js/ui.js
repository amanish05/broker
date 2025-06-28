// ui.js
// Handles UI event listeners (logout, API form, disconnect)
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('api-action-form').addEventListener('submit', function(e) {
        e.preventDefault();
        const name = document.getElementById('api-input').value;
        const token = window.nameTokenMap ? window.nameTokenMap[name] : undefined;
        if (!token) { return; }
        fetch('/api/ticker/subscribe', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'tokens=' + encodeURIComponent(token)
        })
        .then(res => res.text())
        .then(msg => {
            document.getElementById('api-action-result').innerText = msg;
        });
    });
    document.getElementById('disconnect-btn').addEventListener('click', function() {
        fetch('/api/ticker/disconnect', { method: 'POST' })
            .then(res => res.text())
            .then(msg => {
                document.getElementById('api-action-result').innerText = msg;
            });
    });
});
