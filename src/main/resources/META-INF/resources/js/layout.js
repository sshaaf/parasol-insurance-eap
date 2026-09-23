// Shared layout for all pages
function initLayout(activePage) {
    const navItems = [
        { id: 'dashboard', label: 'Dashboard', href: '/dashboard.html' },
        { id: 'policies', label: 'Policies', href: '/policies.html' },
        { id: 'claims', label: 'Claims', href: '/claims.html' },
        { id: 'coverages', label: 'Coverages', href: '/coverages.html' },
        { id: 'annuities', label: 'Annuities', href: '/annuities.html' },
        { id: 'subscriptions', label: 'Subscriptions', href: '/subscriptions.html' },
        { id: 'reports', label: 'Reports', href: '/reports.html' },
        { id: 'inbox', label: 'Inbox', href: '/inbox.html' },
    ];

    const bottomNavItems = [
        { id: 'admin', label: 'Admin', href: '/admin.html' },
        { id: 'settings', label: 'Settings', href: '/settings.html' },
    ];

    const umbrellaIcon = `<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
        <path d="M12 2C6.48 2 2 6.48 2 12h4c0-1.1.9-2 2-2s2 .9 2 2h4c0-1.1.9-2 2-2s2 .9 2 2h4c0-5.52-4.48-10-10-10z"/>
        <path d="M13 12v7c0 1.1-.9 2-2 2s-2-.9-2-2v-1h2v1h0v-7h2z"/>
    </svg>`;

    // Build sidebar
    const sidebar = document.createElement('nav');
    sidebar.className = 'sidebar';
    sidebar.innerHTML = `
        <div class="sidebar-brand">
            ${umbrellaIcon}
            <span>Parasol<span class="tm">\u2122</span></span>
        </div>
        <ul class="sidebar-nav">
            ${navItems.map(item => `
                <li><a href="${item.href}" class="${item.id === activePage ? 'active' : ''}">${item.label}</a></li>
            `).join('')}
        </ul>
        <div class="sidebar-bottom">
            <ul class="sidebar-nav">
                ${bottomNavItems.map(item => `
                    <li><a href="${item.href}" class="${item.id === activePage ? 'active' : ''}">${item.label}</a></li>
                `).join('')}
            </ul>
        </div>
    `;

    // Build header
    const header = document.createElement('header');
    header.className = 'top-header';
    header.innerHTML = `
        <div class="header-actions">
            <button class="header-icon" title="Notifications">\uD83D\uDD14</button>
            <button class="header-icon" title="Settings">\u2699</button>
            <button class="header-icon" title="Help">\u2753</button>
            <div class="user-info">
                <span>Alex Garcia</span>
                <div class="user-avatar">AG</div>
            </div>
        </div>
    `;

    // Insert into page
    const content = document.getElementById('page-content');
    const mainWrapper = document.createElement('div');
    mainWrapper.className = 'main-wrapper';
    mainWrapper.appendChild(header);

    const contentDiv = document.createElement('div');
    contentDiv.className = 'content';
    // Move all children from page-content into contentDiv
    while (content.firstChild) {
        contentDiv.appendChild(content.firstChild);
    }
    mainWrapper.appendChild(contentDiv);

    document.body.innerHTML = '';
    document.body.appendChild(sidebar);
    document.body.appendChild(mainWrapper);
}
