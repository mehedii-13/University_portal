/**
 * University Portal — Simple Auth Handler
 */
(function redirectLoggedInUser() {
    const token = localStorage.getItem('token');
    if (token && (window.location.pathname === '/' || window.location.pathname.endsWith('index.html'))) {
        window.location.href = '/dashboard.html';
    }
})();

function fillDemoSession(username, password) {
    const userField = document.getElementById('username');
    const pwdField = document.getElementById('password');
    if (userField && pwdField) {
        userField.value = username;
        pwdField.value = password;
    }
}

window.fillDemoSession = fillDemoSession;
window.fillCredentials = fillDemoSession;
window.prefillDemoCredentials = fillDemoSession;

document.getElementById('loginForm')?.addEventListener('submit', async function (e) {
    e.preventDefault();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();
    const errorEl = document.getElementById('loginError');
    const btn = document.getElementById('loginBtn');
    const btnText = document.getElementById('loginBtnText');

    if (!username || !password) {
        showError('Please enter both username and password.');
        return;
    }

    btn.disabled = true;
    btnText.textContent = 'Signing in...';
    if (errorEl) errorEl.classList.remove('show');

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem('token', data.token);
            localStorage.setItem('userId', data.id);
            localStorage.setItem('username', data.username);
            localStorage.setItem('fullName', data.fullName);
            localStorage.setItem('email', data.email);
            localStorage.setItem('role', data.role);

            window.location.href = '/dashboard.html';
        } else {
            showError(data.error || 'Invalid username or password.');
        }
    } catch (err) {
        showError('Unable to reach the server. Please try again.');
    } finally {
        btn.disabled = false;
        btnText.textContent = 'Sign In';
    }
});

function showError(message) {
    const errorEl = document.getElementById('loginError');
    if (!errorEl) return;
    errorEl.textContent = message;
    errorEl.classList.add('show');
}

window.showLoginError = showError;
window.showAuthError = showError;
