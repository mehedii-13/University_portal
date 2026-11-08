/**
 * University Portal — Simple API Engine
 */
const API = {
    getToken() {
        return localStorage.getItem('token');
    },

    getHeaders() {
        return {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.getToken()}`
        };
    },

    async request(method, url, body = null) {
        const options = {
            method,
            headers: this.getHeaders()
        };

        if (body) {
            options.body = JSON.stringify(body);
        }

        const response = await fetch(url, options);

        if (response.status === 401 || response.status === 403) {
            localStorage.clear();
            window.location.href = '/';
            return null;
        }

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || 'Request failed');
        }

        return data;
    },

    get(url) { return this.request('GET', url); },
    post(url, body) { return this.request('POST', url, body); },
    put(url, body) { return this.request('PUT', url, body); },
    delete(url) { return this.request('DELETE', url); }
};

function showNotification(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type === 'error' ? 'error' : ''}`;
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 2800);
}

window.showToast = showNotification;
window.showNotification = showNotification;

function openDialog(title, bodyHtml, footerHtml) {
    const titleEl = document.getElementById('modalTitle');
    const bodyEl = document.getElementById('modalBody');
    const footerEl = document.getElementById('modalFooter');
    const overlay = document.getElementById('modalOverlay');

    if (titleEl) titleEl.textContent = title;
    if (bodyEl) bodyEl.innerHTML = bodyHtml;
    if (footerEl) footerEl.innerHTML = footerHtml;
    if (overlay) overlay.classList.add('show');
}

window.openModal = openDialog;
window.openDialog = openDialog;

function dismissDialog() {
    const overlay = document.getElementById('modalOverlay');
    if (overlay) overlay.classList.remove('show');
}

window.closeModal = dismissDialog;
window.dismissDialog = dismissDialog;

document.getElementById('modalOverlay')?.addEventListener('click', function (e) {
    if (e.target === this) dismissDialog();
});

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') dismissDialog();
});

async function signOut() {
    try {
        await fetch('/api/auth/logout', { method: 'POST' });
    } catch (e) {}
    localStorage.clear();
    window.location.href = '/';
}

window.signOut = signOut;
window.logout = signOut;
