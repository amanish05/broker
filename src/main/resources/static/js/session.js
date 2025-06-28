// session.js
// Handles session validation and login redirect
function validateSession() {
    // Only check with backend, never touch JSESSIONID directly
    fetch('/api/session/kite-access-token', { credentials: 'include' })
        .then(res => {
            if (res.status === 401) {
                window.location.href = '/login';
            }
        });
}
// Only run session check once per page load
if (!window._sessionChecked) {
    window._sessionChecked = true;
    validateSession();
}
