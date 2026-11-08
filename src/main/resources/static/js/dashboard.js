/**
 * University Portal — Simple Dashboard Controller
 */
const SimplePortal = {
    user: {
        id: null,
        role: null,
        fullName: '',
        username: '',
        email: ''
    },
    activeSection: 'overview',
    cachedRecords: {
        teachers: [],
        students: [],
        admins: []
    },

    startDashboard() {
        const token = localStorage.getItem('token');
        const role = localStorage.getItem('role');

        if (!token || !role) {
            window.location.href = '/';
            return;
        }

        this.user = {
            id: localStorage.getItem('userId') ? parseInt(localStorage.getItem('userId')) : null,
            role: role.toUpperCase(),
            fullName: localStorage.getItem('fullName') || 'User',
            username: localStorage.getItem('username') || '',
            email: localStorage.getItem('email') || ''
        };

        const infoEl = document.getElementById('navUserInfo');
        if (infoEl) {
            infoEl.textContent = `${this.user.fullName} (${this.formatRole(this.user.role)})`;
        }

        this.buildNavigation();
        this.switchPanel('overview');
    },

    formatRole(role) {
        if (!role) return '';
        return role.charAt(0) + role.slice(1).toLowerCase();
    },

    buildNavigation() {
        const nav = document.getElementById('navLinks');
        if (!nav) return;

        let links = `
            <button class="nav-btn active" data-section="overview" onclick="SimplePortal.switchPanel('overview')">Overview</button>
            <button class="nav-btn" data-section="teachers" onclick="SimplePortal.switchPanel('teachers')">Teachers</button>
            <button class="nav-btn" data-section="students" onclick="SimplePortal.switchPanel('students')">Students</button>
        `;

        if (this.user.role === 'ADMIN') {
            links += `<button class="nav-btn" data-section="admins" onclick="SimplePortal.switchPanel('admins')">Administrators</button>`;
        }

        links += `<button class="nav-btn" data-section="profile" onclick="SimplePortal.switchPanel('profile')">My Profile</button>`;
        nav.innerHTML = links;
    },

    switchPanel(section) {
        this.activeSection = section;
        document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelector(`.nav-btn[data-section="${section}"]`)?.classList.add('active');

        switch (section) {
            case 'overview': this.refreshOverview(); break;
            case 'teachers': this.loadTeacherList(); break;
            case 'students': this.loadStudentList(); break;
            case 'admins': this.loadAdminList(); break;
            case 'profile': this.loadProfilePanel(); break;
        }
    },

    async refreshOverview() {
        document.getElementById('pageTitle').textContent = `${this.formatRole(this.user.role)} Overview`;
        document.getElementById('pageSubtitle').textContent = `Welcome, ${this.user.fullName}`;

        const bannerSlot = document.getElementById('bannerActionSlot');
        if (bannerSlot) bannerSlot.innerHTML = '';

        try {
            const [teachers, students] = await Promise.all([
                API.get('/api/teachers'),
                API.get('/api/students')
            ]);
            this.cachedRecords.teachers = teachers || [];
            this.cachedRecords.students = students || [];

            let adminCount = 0;
            if (this.user.role === 'ADMIN') {
                const admins = await API.get('/api/admins');
                this.cachedRecords.admins = admins || [];
                adminCount = this.cachedRecords.admins.length;
            }

            const statsGrid = document.getElementById('statsGrid');
            if (this.user.role === 'ADMIN') {
                statsGrid.innerHTML = `
                    <div class="stat-card" onclick="SimplePortal.switchPanel('admins')">
                        <div class="stat-label">Administrators</div>
                        <div class="stat-value">${adminCount}</div>
                    </div>
                    <div class="stat-card" onclick="SimplePortal.switchPanel('teachers')">
                        <div class="stat-label">Teachers</div>
                        <div class="stat-value">${this.cachedRecords.teachers.length}</div>
                    </div>
                    <div class="stat-card" onclick="SimplePortal.switchPanel('students')">
                        <div class="stat-label">Students</div>
                        <div class="stat-value">${this.cachedRecords.students.length}</div>
                    </div>
                `;

                document.getElementById('contentArea').innerHTML = `
                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title">Quick Actions</h3>
                        </div>
                        <div class="card-body" style="display: flex; gap: 10px; flex-wrap: wrap;">
                            <button class="btn btn-primary btn-sm" onclick="SimplePortal.openCreationDialog('teachers')">+ Add Teacher</button>
                            <button class="btn btn-primary btn-sm" onclick="SimplePortal.openCreationDialog('students')">+ Add Student</button>
                            <button class="btn btn-secondary btn-sm" onclick="SimplePortal.openCreationDialog('admins')">+ Add Administrator</button>
                        </div>
                    </div>
                `;
            } else {
                statsGrid.innerHTML = `
                    <div class="stat-card" onclick="SimplePortal.switchPanel('teachers')">
                        <div class="stat-label">Teachers</div>
                        <div class="stat-value">${this.cachedRecords.teachers.length}</div>
                    </div>
                    <div class="stat-card" onclick="SimplePortal.switchPanel('students')">
                        <div class="stat-label">Students</div>
                        <div class="stat-value">${this.cachedRecords.students.length}</div>
                    </div>
                `;

                document.getElementById('contentArea').innerHTML = `
                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title">My Account</h3>
                            <button class="btn btn-secondary btn-sm" onclick="SimplePortal.switchPanel('profile')">Edit Profile</button>
                        </div>
                        <div class="card-body">
                            <p style="margin-bottom: 8px;"><strong>Full Name:</strong> ${this.user.fullName}</p>
                            <p style="margin-bottom: 8px;"><strong>Username:</strong> ${this.user.username}</p>
                            <p style="margin-bottom: 8px;"><strong>Email:</strong> ${this.user.email || '—'}</p>
                            <p><strong>Role:</strong> <span class="badge ${this.user.role === 'TEACHER' ? 'badge-teacher' : 'badge-student'}">${this.user.role}</span></p>
                        </div>
                    </div>
                `;
            }
        } catch (e) {
            showNotification('Failed to load dashboard data', 'error');
        }
    },

    async loadTeacherList() {
        document.getElementById('pageTitle').textContent = 'Teachers';
        document.getElementById('pageSubtitle').textContent = 'Faculty roster';
        document.getElementById('statsGrid').innerHTML = '';

        try {
            const teachers = await API.get('/api/teachers');
            this.cachedRecords.teachers = teachers || [];
            this.renderTable('teachers', this.cachedRecords.teachers);
        } catch (e) {
            showNotification('Failed to load teachers', 'error');
        }
    },

    async loadStudentList() {
        document.getElementById('pageTitle').textContent = 'Students';
        document.getElementById('pageSubtitle').textContent = 'Student directory';
        document.getElementById('statsGrid').innerHTML = '';

        try {
            const students = await API.get('/api/students');
            this.cachedRecords.students = students || [];
            this.renderTable('students', this.cachedRecords.students);
        } catch (e) {
            showNotification('Failed to load students', 'error');
        }
    },

    async loadAdminList() {
        if (this.user.role !== 'ADMIN') {
            this.switchPanel('overview');
            return;
        }

        document.getElementById('pageTitle').textContent = 'Administrators';
        document.getElementById('pageSubtitle').textContent = 'System administrators';
        document.getElementById('statsGrid').innerHTML = '';

        try {
            const admins = await API.get('/api/admins');
            this.cachedRecords.admins = admins || [];
            this.renderTable('admins', this.cachedRecords.admins);
        } catch (e) {
            showNotification('Failed to load administrators', 'error');
        }
    },

    renderTable(userGroup, list) {
        const isAdmin = this.user.role === 'ADMIN';
        const singularLabel = userGroup === 'teachers' ? 'Teacher' : userGroup === 'students' ? 'Student' : 'Administrator';
        const badgeClass = userGroup === 'teachers' ? 'badge-teacher' : userGroup === 'students' ? 'badge-student' : 'badge-admin';

        const bannerSlot = document.getElementById('bannerActionSlot');
        if (bannerSlot) {
            if (isAdmin) {
                bannerSlot.innerHTML = `<button class="btn btn-primary btn-sm" onclick="SimplePortal.openCreationDialog('${userGroup}')">+ Add ${singularLabel}</button>`;
            } else {
                bannerSlot.innerHTML = '';
            }
        }

        let rowsHtml = '';
        list.forEach((item) => {
            const isSelf = (this.user.username === item.username) || (this.user.id && this.user.id === item.id);
            let actionButtons = '<span style="color: #94a3b8;">—</span>';

            if (isAdmin) {
                const canDelete = !(userGroup === 'admins' && isSelf);
                actionButtons = `
                    <div style="display: flex; gap: 6px;">
                        <button class="btn btn-secondary btn-sm" onclick="SimplePortal.openEditDialog('${userGroup}', ${item.id})">Edit</button>
                        ${canDelete ? `<button class="btn btn-danger btn-sm" onclick="SimplePortal.deleteRecord('${userGroup}', ${item.id}, '${item.fullName || item.username}')">Delete</button>` : ''}
                    </div>
                `;
            } else if (isSelf) {
                actionButtons = `<button class="btn btn-secondary btn-sm" onclick="SimplePortal.switchPanel('profile')">Edit Profile</button>`;
            }

            rowsHtml += `
                <tr data-search="${(item.fullName + ' ' + item.username + ' ' + item.email).toLowerCase()}">
                    <td>
                        <strong>${item.fullName || '—'}</strong>
                        ${isSelf ? '<span class="badge badge-admin" style="margin-left: 6px;">You</span>' : ''}
                    </td>
                    <td><code>${item.username}</code></td>
                    <td>${item.email || '—'}</td>
                    <td><span class="badge ${badgeClass}">${item.role || singularLabel.toUpperCase()}</span></td>
                    <td>${actionButtons}</td>
                </tr>
            `;
        });

        document.getElementById('contentArea').innerHTML = `
            <div class="card">
                <div class="card-header">
                    <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
                        <h3 class="card-title">All ${singularLabel}s (${list.length})</h3>
                        <input type="text" id="tableSearchInput" class="search-input" placeholder="Search..." oninput="SimplePortal.filterDirectory(this.value)">
                    </div>
                </div>

                <div class="table-responsive">
                    ${list.length === 0 ? `
                        <p style="color: #64748b; padding: 24px; text-align: center;">No records found.</p>
                    ` : `
                        <table class="table" id="dataTable">
                            <thead>
                                <tr>
                                    <th>Full Name</th>
                                    <th>Username</th>
                                    <th>Email</th>
                                    <th>Role</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>${rowsHtml}</tbody>
                        </table>
                    `}
                </div>
            </div>
        `;
    },

    filterDirectory(query) {
        const searchTerm = (query || '').toLowerCase().trim();
        const rows = document.querySelectorAll('#dataTable tbody tr');
        rows.forEach(row => {
            const text = row.getAttribute('data-search') || '';
            row.style.display = text.includes(searchTerm) ? '' : 'none';
        });
    },

    async loadProfilePanel() {
        document.getElementById('pageTitle').textContent = 'My Profile';
        document.getElementById('pageSubtitle').textContent = 'Edit account details';
        document.getElementById('statsGrid').innerHTML = '';

        const bannerSlot = document.getElementById('bannerActionSlot');
        if (bannerSlot) bannerSlot.innerHTML = '';

        let latestUser = this.user;
        const endpoint = this.user.role === 'TEACHER' ? '/api/teachers' : this.user.role === 'STUDENT' ? '/api/students' : '/api/admins';

        if (this.user.id) {
            try {
                latestUser = await API.get(`${endpoint}/${this.user.id}`);
                this.user.fullName = latestUser.fullName;
                this.user.email = latestUser.email;
                localStorage.setItem('fullName', latestUser.fullName);
                localStorage.setItem('email', latestUser.email);
            } catch (e) {}
        }

        document.getElementById('contentArea').innerHTML = `
            <div class="card" style="max-width: 480px;">
                <div class="card-header">
                    <h3 class="card-title">Edit Profile</h3>
                    <span class="badge ${this.user.role === 'ADMIN' ? 'badge-admin' : this.user.role === 'TEACHER' ? 'badge-teacher' : 'badge-student'}">${this.user.role}</span>
                </div>
                <div class="card-body">
                    <form onsubmit="event.preventDefault(); SimplePortal.saveProfileChanges();">
                        <div class="form-group">
                            <label>Username</label>
                            <input type="text" class="form-control" value="${latestUser.username}" disabled>
                        </div>

                        <div class="form-group">
                            <label>Full Name</label>
                            <input type="text" id="profFullName" class="form-control" value="${latestUser.fullName || ''}" required>
                        </div>

                        <div class="form-group">
                            <label>Email Address</label>
                            <input type="email" id="profEmail" class="form-control" value="${latestUser.email || ''}" required>
                        </div>

                        <div class="form-group">
                            <label>New Password (leave blank to keep current)</label>
                            <input type="password" id="profPassword" class="form-control" placeholder="••••••••">
                        </div>

                        <button type="submit" class="btn btn-primary" id="saveProfBtn" style="margin-top: 8px;">Save Changes</button>
                    </form>
                </div>
            </div>
        `;
    },

    async saveProfileChanges() {
        if (!this.user.id) {
            showNotification('User ID missing', 'error');
            return;
        }

        const fullName = document.getElementById('profFullName').value.trim();
        const email = document.getElementById('profEmail').value.trim();
        const password = document.getElementById('profPassword').value;

        const endpoint = this.user.role === 'TEACHER' ? `/api/teachers/${this.user.id}`
            : this.user.role === 'STUDENT' ? `/api/students/${this.user.id}`
            : `/api/admins/${this.user.id}`;

        const payload = {
            username: this.user.username,
            fullName,
            email,
            role: this.user.role
        };
        if (password) payload.password = password;

        const btn = document.getElementById('saveProfBtn');
        if (btn) btn.disabled = true;

        try {
            const updated = await API.put(endpoint, payload);
            this.user.fullName = updated.fullName;
            this.user.email = updated.email;
            localStorage.setItem('fullName', updated.fullName);
            localStorage.setItem('email', updated.email);

            const infoEl = document.getElementById('navUserInfo');
            if (infoEl) {
                infoEl.textContent = `${updated.fullName} (${this.formatRole(this.user.role)})`;
            }

            showNotification('Profile updated successfully');
            this.loadProfilePanel();
        } catch (e) {
            showNotification(e.message || 'Failed to update profile', 'error');
        } finally {
            if (btn) btn.disabled = false;
        }
    },

    openCreationDialog(type) {
        const typeSingular = type === 'teachers' ? 'Teacher' : type === 'students' ? 'Student' : 'Administrator';
        const role = type === 'teachers' ? 'TEACHER' : type === 'students' ? 'STUDENT' : 'ADMIN';

        const bodyHtml = `
            <div class="form-group">
                <label>Full Name</label>
                <input type="text" id="addFullName" class="form-control" placeholder="e.g. John Doe" required>
            </div>
            <div class="form-group">
                <label>Username</label>
                <input type="text" id="addUsername" class="form-control" placeholder="e.g. jdoe" required>
            </div>
            <div class="form-group">
                <label>Email Address</label>
                <input type="email" id="addEmail" class="form-control" placeholder="e.g. jdoe@university.edu" required>
            </div>
            <div class="form-group">
                <label>Password</label>
                <input type="password" id="addPassword" class="form-control" placeholder="Temporary password" required>
            </div>
        `;

        const footerHtml = `
            <button class="btn btn-secondary btn-sm" onclick="closeModal()">Cancel</button>
            <button class="btn btn-primary btn-sm" onclick="SimplePortal.submitNewRecord('${type}', '${role}')">Create</button>
        `;

        openDialog(`Add ${typeSingular}`, bodyHtml, footerHtml);
    },

    async submitNewRecord(type, role) {
        const fullName = document.getElementById('addFullName').value.trim();
        const username = document.getElementById('addUsername').value.trim();
        const email = document.getElementById('addEmail').value.trim();
        const password = document.getElementById('addPassword').value;

        if (!fullName || !username || !email || !password) {
            showNotification('All fields are required', 'error');
            return;
        }

        try {
            await API.post(`/api/${type}`, { fullName, username, email, password, role });
            closeModal();
            showNotification('Created successfully');
            if (this.activeSection === type) {
                this.switchPanel(type);
            } else {
                this.refreshOverview();
            }
        } catch (e) {
            showNotification(e.message || 'Failed to create record', 'error');
        }
    },

    async openEditDialog(type, id) {
        try {
            const user = await API.get(`/api/${type}/${id}`);
            const typeSingular = type === 'teachers' ? 'Teacher' : type === 'students' ? 'Student' : 'Administrator';

            const bodyHtml = `
                <div class="form-group">
                    <label>Username</label>
                    <input type="text" id="editUsername" class="form-control" value="${user.username}" disabled>
                </div>
                <div class="form-group">
                    <label>Full Name</label>
                    <input type="text" id="editFullName" class="form-control" value="${user.fullName || ''}" required>
                </div>
                <div class="form-group">
                    <label>Email Address</label>
                    <input type="email" id="editEmail" class="form-control" value="${user.email || ''}" required>
                </div>
                <div class="form-group">
                    <label>New Password (optional)</label>
                    <input type="password" id="editPassword" class="form-control" placeholder="Leave blank to keep current">
                </div>
            `;

            const footerHtml = `
                <button class="btn btn-secondary btn-sm" onclick="closeModal()">Cancel</button>
                <button class="btn btn-primary btn-sm" onclick="SimplePortal.saveEditedRecord('${type}', ${id}, '${user.role}')">Save</button>
            `;

            openDialog(`Edit ${typeSingular}`, bodyHtml, footerHtml);
        } catch (e) {
            showNotification('Failed to load user', 'error');
        }
    },

    async saveEditedRecord(type, id, role) {
        const username = document.getElementById('editUsername').value.trim();
        const fullName = document.getElementById('editFullName').value.trim();
        const email = document.getElementById('editEmail').value.trim();
        const password = document.getElementById('editPassword').value;

        if (!fullName || !email) {
            showNotification('Name and email are required', 'error');
            return;
        }

        const payload = { username, fullName, email, role };
        if (password) payload.password = password;

        try {
            await API.put(`/api/${type}/${id}`, payload);
            closeModal();
            showNotification('Updated successfully');

            if (this.user.id === id) {
                this.user.fullName = fullName;
                this.user.email = email;
                localStorage.setItem('fullName', fullName);
                localStorage.setItem('email', email);
                const infoEl = document.getElementById('navUserInfo');
                if (infoEl) {
                    infoEl.textContent = `${fullName} (${this.formatRole(this.user.role)})`;
                }
            }

            this.switchPanel(type);
        } catch (e) {
            showNotification(e.message || 'Failed to update record', 'error');
        }
    },

    async deleteRecord(type, id, name) {
        if (!confirm(`Are you sure you want to delete ${name}?`)) {
            return;
        }

        try {
            await API.delete(`/api/${type}/${id}`);
            showNotification('Deleted successfully');
            this.switchPanel(type);
        } catch (e) {
            showNotification(e.message || 'Failed to delete record', 'error');
        }
    }
};

/* Compatibility Aliases */
const CampusFlow = SimplePortal;
const NexusApp = SimplePortal;
const CampusApp = SimplePortal;
const NorthstarPortal = SimplePortal;
const App = SimplePortal;

window.SimplePortal = SimplePortal;
window.CampusFlow = SimplePortal;
window.NexusApp = SimplePortal;
window.CampusApp = SimplePortal;
window.NorthstarPortal = SimplePortal;
window.App = SimplePortal;

Object.assign(SimplePortal, {
    start: SimplePortal.startDashboard,
    renderMenu: SimplePortal.buildNavigation,
    renderNavigation: SimplePortal.buildNavigation,
    showSection: SimplePortal.switchPanel,
    loadOverview: SimplePortal.refreshOverview,
    loadTeachers: SimplePortal.loadTeacherList,
    loadStudents: SimplePortal.loadStudentList,
    loadAdmins: SimplePortal.loadAdminList,
    renderUsersTable: SimplePortal.renderTable,
    loadProfile: SimplePortal.loadProfilePanel,
    updateProfile: SimplePortal.saveProfileChanges,
    openCreateModal: SimplePortal.openCreationDialog,
    createEntry: SimplePortal.submitNewRecord,
    openEditEntryModal: SimplePortal.openEditDialog,
    updateEntry: SimplePortal.saveEditedRecord,
    removeUser: SimplePortal.deleteRecord,
    filterList: SimplePortal.filterDirectory,
    filterUserList: SimplePortal.filterDirectory,
    showToast: showNotification,
    openModal: openDialog,
    closeModal: dismissDialog,
    logout: signOut
});

document.addEventListener('DOMContentLoaded', () => SimplePortal.startDashboard());
