<script>
    import { Router, Route, Link, navigate } from 'svelte-routing';
    import './app.css';
    import logo from './assets/logo_VNPT.png';
    import Home from './routes/+page.svelte';
    import Admin from './routes/+admin.svelte';
    import Login from './routes/+login.svelte';
    import About from './routes/+about.svelte';
    import Services from './routes/+services.svelte';
    import Projects from './routes/+projects.svelte';
    import Customers from './routes/+customers.svelte';
    import Contact from './routes/+contact.svelte';
    import { onMount, onDestroy } from 'svelte';
    import { searchIntroduces } from './lib/services/page.service.js';
    import { initAuth, logout } from './lib/config/auth-config.js';
    import VantaBackground from "./lib/compoments/VantaBackground.svelte";
    import axios from "axios";

    let mounted = false;
    let mobileMenuOpen = false;
    let searchQuery = '';
    let searchResults = [];
    let isSearching = false;
    let showSearchResults = false;
    let showMobileSearch = false;
    let showUserMenu = false;
    let isAuthenticated = false;
    let isAdmin = false;
    let currentUser = null;
    let showDesktopSearch = false;
    let searchInputRef;

    // Token monitoring
    let authCheckInterval = null;

    onMount(async () => {
        try {
            // Initialize auth system
            const authInitialized = await initAuth();

            if (authInitialized) {
                // Get auth state
                const savedUser = localStorage.getItem('vnpt_user');
                if (savedUser) {
                    const userData = JSON.parse(savedUser);
                    currentUser = userData;
                    isAuthenticated = userData.isAuthenticated;
                    isAdmin = userData.isAdmin;
                }
            }

            // Start periodic auth state check
            startAuthStateMonitoring();

            mounted = true;
            console.log('VNPT-IT đã được khởi tạo');
        } catch (error) {
            console.error('Error during initialization:', error);
            mounted = true;
        }
    });

    onDestroy(() => {
        // Clean up auth monitoring interval
        if (authCheckInterval) {
            clearInterval(authCheckInterval);
        }

        // Remove click outside event listener
        document.removeEventListener('click', handleClickOutside);
    });

    // Start monitoring auth state
    function startAuthStateMonitoring() {
        // Check auth state every minute
        authCheckInterval = setInterval(() => {
            checkAuthState();
        }, 60000); // Every 60 seconds
    }

    // Check current auth state
    function checkAuthState() {
        const savedUser = localStorage.getItem('vnpt_user');
        const savedAccessToken = localStorage.getItem('access_token');

        if (!savedUser || !savedAccessToken) {
            // Auth data is missing, update UI state
            if (isAuthenticated) {
                console.log('Auth data missing, updating UI state');
                currentUser = null;
                isAuthenticated = false;
                isAdmin = false;
            }
        }
    }

    function handleError(error) {
        console.error('Lỗi ứng dụng:', error);
    }

    let isLoading = false;

    function toggleMobileMenu() {
        mobileMenuOpen = !mobileMenuOpen;
    }

    function toggleMobileSearch() {
        showMobileSearch = !showMobileSearch;
        if (!showMobileSearch) {
            searchQuery = '';
            searchResults = [];
            showSearchResults = false;
        }
    }

    function toggleDesktopSearch() {
        showDesktopSearch = !showDesktopSearch;
        console.log('showDesktopSearch:', showDesktopSearch);
        if (showDesktopSearch) {
            setTimeout(() => {
                if (searchInputRef) {
                    searchInputRef.focus();
                }
            }, 100);
        } else {
            searchQuery = '';
            searchResults = [];
            showSearchResults = false;
        }
    }

    function toggleUserMenu() {
        showUserMenu = !showUserMenu;
    }

    function handleUserAction(action) {
        console.log('Thao tác người dùng:', action);
        showUserMenu = false;
        switch(action) {
            case 'login':
                navigate('/login');
                break;
            case 'admin':
                navigate('/admin');
                break;
            case 'home':
                navigate('/');
                break;
            case 'logout':
                handleLogout();
                break;
        }
    }

    function handleLoginSuccess(event) {
        const { username, isAuthenticated: authStatus, isAdmin: adminStatus } = event.detail;
        currentUser = { username, isAuthenticated: authStatus, isAdmin: adminStatus };
        isAuthenticated = authStatus;
        isAdmin = adminStatus;
        localStorage.setItem('vnpt_user', JSON.stringify(currentUser));
        console.log('Đăng nhập thành công:', { username, isAdmin: adminStatus });
        navigate('/');
    }

    function handleLogout() {
        logout().then(() => {
            currentUser = null;
            isAuthenticated = false;
            isAdmin = false;
            localStorage.removeItem('access_token');
            localStorage.removeItem('vnpt_user');
            clearSearch();
            navigate('/', { replace: true });
        }).catch(error => {
            console.error('Logout error:', error);
            // Even if logout API fails, clear local state
            currentUser = null;
            isAuthenticated = false;
            isAdmin = false;
            localStorage.removeItem('access_token');
            localStorage.removeItem('vnpt_user');
            clearSearch();
            navigate('/', { replace: true });
        });
    }

    function handleForgotPassword() {
        console.log('Quên mật khẩu - chức năng sẽ được phát triển');
        alert('Tính năng quên mật khẩu sẽ được phát triển trong phiên bản tiếp theo.');
    }

    async function handleSearch() {
        if (!searchQuery.trim()) {
            searchResults = [];
            showSearchResults = false;
            return;
        }
        isSearching = true;
        try {
            const results = await searchIntroduces(searchQuery);
            searchResults = results;
            showSearchResults = true;
        } catch (error) {
            console.error('Lỗi tìm kiếm:', error);
            searchResults = [];
        } finally {
            isSearching = false;
        }
    }

    let searchTimeout;

    function handleSearchInput() {
        clearTimeout(searchTimeout);
        if (searchQuery.trim() === '') {
            searchResults = [];
            showSearchResults = false;
            return;
        }
        searchTimeout = setTimeout(() => {
            handleSearch();
        }, 300);
    }

    function goTo(path) {
        navigate(path);
    }

    function handleSearchClick() {
        if (!showDesktopSearch) {
            toggleDesktopSearch();
        } else {
            handleSearch();
        }
    }

    function handleKeyPress(event) {
        if (event.key === 'Enter') {
            handleSearch();
        } else if (event.key === 'Escape') {
            toggleDesktopSearch();
        }
    }

    function clearSearch() {
        searchQuery = '';
        searchResults = [];
        showSearchResults = false;
        showDesktopSearch = false;
    }

    function handleClickOutside(event) {
        if (showUserMenu && !event.target.closest('.user-menu-container')) {
            showUserMenu = false;
        }
        if (showDesktopSearch && !event.target.closest('.search-container') && !event.target.closest('.search-icon-btn')) {
            toggleDesktopSearch();
        }
    }

    onMount(() => {
        document.addEventListener('click', handleClickOutside);
    });
</script>

<svelte:head>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined" rel="stylesheet">
</svelte:head>

<VantaBackground />

{#if mounted}
    <Router>
        <!-- Header -->
        <header class="header">
            <div class="header-container">
                <!-- Logo -->
                <div class="logo-section">
                    <div class="logo-icon" on:click={() => { clearSearch(); goTo('/') }}>
                        <img src={logo} alt="Logo" />
                    </div>
                    <div class="logo-text" on:click={() => goTo('/')}>VNPT-IT</div>
                </div>

                <nav class="nav-menu">
                    <div class="nav-item" on:click={() => goTo('/about')}>Giới thiệu</div>
                    <div class="nav-item" on:click={() => goTo('/customers')}>Khách hàng</div>
                    <div class="nav-item" on:click={() => goTo('/contact')}>Liên hệ</div>
                </nav>

                <!-- Search and User Actions -->
                <div class="header-actions">
                    <!-- Desktop Search -->
                    <div class="search-container desktop-search">
                        {#if showDesktopSearch}
                            <div class="search-input-container">
                                <input
                                        type="text"
                                        maxlength="32"
                                        placeholder="Tìm kiếm..."
                                        class="search-input expanded"
                                        bind:value={searchQuery}
                                        bind:this={searchInputRef}
                                        on:input={handleSearchInput}
                                        on:keydown={handleKeyPress}
                                />
                                {#if searchQuery}
                                    <button class="clear-search-btn" on:click={clearSearch}>
                                        <span class="material-symbols-outlined">clear</span>
                                    </button>
                                {/if}
                                <button class="search-btn-expanded" on:click={handleSearch} disabled={isSearching}>
                                    <span class="material-symbols-outlined">search</span>
                                </button>
                            </div>
                        {:else}
                            <button class="search-icon-btn" on:click={toggleDesktopSearch}>
                                <span class="material-symbols-outlined">search</span>
                            </button>
                        {/if}
                    </div>

                    <!-- User Menu -->
                    <div class="user-menu-container">
                        <button class="user-menu-toggle" on:click={toggleUserMenu}>
                            <span class="material-symbols-outlined">account_circle</span>
                        </button>

                        {#if showUserMenu}
                            <div class="user-menu">
                                {#if isAuthenticated}
                                    <!-- User Info -->
                                    <div class="user-info">
                                        <span class="material-symbols-outlined">person</span>
                                        <span class="username">{currentUser?.username || 'User'}</span>
                                    </div>
                                    <div class="user-menu-divider"></div>

                                    {#if isAdmin}
                                        <div class="user-menu-item" on:click={() => handleUserAction('admin')}>
                                            <span class="material-symbols-outlined">admin_panel_settings</span>
                                            <span>Quản trị</span>
                                        </div>
                                        <div class="user-menu-divider"></div>
                                    {/if}

                                    <div class="user-menu-item" on:click={() => handleUserAction('logout')}>
                                        <span class="material-symbols-outlined">logout</span>
                                        <span>Đăng xuất</span>
                                    </div>
                                {:else}
                                    <div class="user-menu-item" on:click={() => handleUserAction('login')}>
                                        <span class="material-symbols-outlined">login</span>
                                        <span>Đăng nhập</span>
                                    </div>
                                {/if}
                            </div>
                        {/if}
                    </div>

                    <!-- Mobile Search Toggle -->
                    <button class="mobile-search-toggle" on:click={toggleMobileSearch}>
                        <span class="material-symbols-outlined">
                            {showMobileSearch ? 'close' : 'search'}
                        </span>
                    </button>

                    <!-- Mobile Menu Toggle -->
                    <button class="mobile-toggle" on:click={toggleMobileMenu}>
                        <div class="hamburger-line" class:active={mobileMenuOpen}></div>
                        <div class="hamburger-line" class:active={mobileMenuOpen}></div>
                        <div class="hamburger-line" class:active={mobileMenuOpen}></div>
                    </button>
                </div>
            </div>

            <!-- Mobile Navigation -->
            {#if mobileMenuOpen}
                <div class="mobile-nav">
                    <nav class="mobile-nav-content">
                        <Link to="/about" class="mobile-nav-item">Giới thiệu</Link>
                        <Link to="/services" class="mobile-nav-item">Dịch vụ/Sản Phẩm</Link>
                        <Link to="/projects" class="mobile-nav-item">Dự Án</Link>
                        <Link to="/customers" class="mobile-nav-item">Khách hàng</Link>
                        <Link to="/contact" class="mobile-nav-item">Liên Hệ</Link>

                        <!-- Mobile User Actions -->
<!--                        <div class="mobile-user-actions">-->
<!--                            {#if isAuthenticated}-->
<!--                                <div class="mobile-user-info">-->
<!--                                    <span class="material-symbols-outlined">person</span>-->
<!--                                    <span>{currentUser?.username || 'User'}</span>-->
<!--                                </div>-->
<!--                                {#if isAdmin}-->
<!--                                    <button class="mobile-nav-item" on:click={() => handleUserAction('admin')}>-->
<!--                                        <span class="material-symbols-outlined">admin_panel_settings</span>-->
<!--                                        Quản trị-->
<!--                                    </button>-->
<!--                                {/if}-->
<!--                                <button class="mobile-nav-item" on:click={() => handleUserAction('logout')}>-->
<!--                                    <span class="material-symbols-outlined">logout</span>-->
<!--                                    Đăng xuất-->
<!--                                </button>-->
<!--                            {:else}-->
<!--                                <button class="mobile-nav-item login-icon" on:click={() => handleUserAction('login')}>-->
<!--                                    <span class="material-symbols-outlined">login</span>-->
<!--                                </button>-->
<!--                            {/if}-->
<!--                        </div>-->
                    </nav>
                </div>
            {/if}

            <!-- Mobile Search Bar -->
            {#if showMobileSearch}
                <div class="mobile-search-container">
                    <div class="mobile-search-content">
                        <input
                                type="text"
                                placeholder="Tìm kiếm..."
                                class="mobile-search-input"
                                bind:value={searchQuery}
                                on:input={handleSearchInput}
                                on:keypress={handleKeyPress}
                        />
                        {#if searchQuery}
                            <button class="mobile-clear-search-btn" on:click={clearSearch}>
                                <span class="material-symbols-outlined">clear</span>
                            </button>
                        {/if}
                        <button class="mobile-search-btn" on:click={handleSearch} disabled={isSearching} class:has-clear={searchQuery}>
                            <span class="material-symbols-outlined">search</span>
                        </button>
                    </div>
                </div>
            {/if}
        </header>

        <div class="app" class:loading={isLoading}>
            {#if isLoading}
                <div class="loading-overlay">
                    <div class="loading-spinner">
                        <div class="spinner"></div>
                        <p>Đang tải...</p>
                    </div>
                </div>
            {/if}

            {#if showSearchResults && searchQuery}
                <div class="search-info">
                    <div class="search-info-content">
                        {#if searchResults.length > 0}
                            <p title="Tìm thấy {searchResults.length} kết quả cho {searchQuery}">
                                Tìm thấy <strong>{searchResults.length}</strong> kết quả cho "<strong>{searchQuery.slice(0, 20)} {#if searchQuery.length > 20}...{/if}</strong>"
                            </p>
                        {:else}
                            <p title="Không tìm thấy kết quả nào cho {searchQuery}">
                                Không tìm thấy kết quả nào cho "<strong>{searchQuery.slice(0, 20)} {#if searchQuery.length > 20}...{/if}</strong>"
                            </p>
                        {/if}
                    </div>
                </div>
            {/if}

            <main class="app-main">
                <Route path="/">
                    <Home {searchResults} {showSearchResults} {searchQuery} />
                </Route>
                <Route path="/login">
                    <Login
                            on:loginSuccess={handleLoginSuccess}
                            on:backToHome={() => navigate('/')}
                            on:forgotPassword={handleForgotPassword}
                    />
                </Route>
                <Route path="/admin">
                    {#if isAdmin}
                        <Admin />
                    {:else}
                        <div class="access-denied">
                            <h2>Access Denied</h2>
                            <p>Bạn không có quyền truy cập trang này.</p>
                            <Link to="/">Quay về trang chủ</Link>
                        </div>
                    {/if}
                </Route>
                <Route path="/about">
                    <About />
                </Route>
                <Route path="/services">
                    <Services />
                </Route>
                <Route path="/projects">
                    <Projects />
                </Route>
                <Route path="/customers">
                    <Customers />
                </Route>
                <Route path="/contact">
                    <Contact />
                </Route>
            </main>

            <footer class="app-footer">
                <div class="footer-bottom">
                    <div class="footer-bottom-content">
                        <div class="footer-left">
                            <div class="logo-section">
                                <div class="logo-icon-footer">
                                    <img src={logo} alt="Logo" />
                                </div>
                                <span class="logo-text-footer">Sản phẩm của VNPT</span>
                            </div>
                        </div>
                        <div class="footer-center">
                            <p>© 2025 VNPT-IT. Bảo lưu mọi quyền.</p>
                        </div>
                        <div class="footer-right">
                            <p>Việt Nam</p>
                        </div>
                    </div>
                </div>
            </footer>
        </div>
    </Router>
{:else}
    <div class="initial-loading">
        <div class="loading-content">
            <div class="logo">VNPT-IT</div>
            <h2>Chào mừng bạn đến với VNPT-IT</h2>
            <div class="loading-dots">
                <span></span>
                <span></span>
                <span></span>
            </div>
        </div>
    </div>
{/if}

<style>
    :global(html) {
        font-family: 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }

    /* Override svelte-routing Link styles to remove bold and underline */
    :global(a) {
        font-weight: 400 !important;
        text-decoration: none !important;
    }

    /* Style for active route */
    :global(a.navLinkActive) {
        color: #1d1d1f !important;
    }

    /* Header */
    .header {
        background: rgba(255, 255, 255, 0.72);
        backdrop-filter: saturate(180%) blur(20px);
        border-bottom: 1px solid rgba(0, 0, 0, 0.1);
        position: sticky;
        top: 0;
        z-index: 50;
        transition: all 0.3s ease;
    }

    .header-container {
        max-width: 90vw;
        margin: 0 auto;
        padding: 0 12px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 44px;
        box-sizing: border-box;
    }

    .logo-section {
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .logo-icon {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;
    }

    .logo-icon img {
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;
    }

    .logo-text {
        font-size: 16px;
        font-weight: 600;
        color: #1d1d1f;
        text-decoration: none;
        transition: color 0.3s;
        cursor: pointer;
    }


    .logo-text:hover {
        color: #0066cc;
    }

    /* Navigation Menu */
    .nav-menu {
        display: flex;
        align-items: center;
        gap: 48px;
    }

    .nav-item {
        color: #1d1d1f;
        text-decoration: none;
        font-size: 12px;
        font-weight: 400;
        transition: color 0.3s;
        letter-spacing: 0.011em;
    }

    .nav-item {
        cursor: pointer;
    }

    .nav-item:hover {
        color: #0066cc;
    }

    .header-actions {
        display: flex;
        align-items: center;
        gap: 15px;
    }

    /* Updated Search Styles */
    .search-container {
        position: relative;
        display: flex;
        align-items: center;
    }

    .search-icon-btn {
        background: none;
        border: none;
        color: #1d1d1f;
        cursor: pointer;
        padding: 6px;
        border-radius: 50%;
        transition: all 0.3s ease;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
        width: 32px;
        height: 32px;
    }

    .search-icon-btn:hover {
        background: rgba(142, 142, 147, 0.12);
        color: #0066cc;
    }

    .search-input-container {
        position: relative;
        display: flex;
        align-items: center;
        animation: expandSearch 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
        will-change: transform, opacity;
    }

    .search-input.expanded {
        background: rgba(142, 142, 147, 0.12);
        color: #1d1d1f;
        border: none;
        border-radius: 20px;
        padding: 8px 40px 8px 16px;
        width: 280px;
        outline: none;
        transition: all 0.3s ease;
        font-size: 13px;
        text-align: left;
        /* Cho phép text cuộn khi dài */
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .search-input.expanded:focus {
        background: rgba(142, 142, 147, 0.18);
        width: 320px;
    }

    .search-btn-expanded {
        position: absolute;
        right: 8px;
        background: none;
        border: none;
        color: #86868b;
        cursor: pointer;
        padding: 4px;
        border-radius: 50%;
        transition: all 0.3s;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        width: 24px;
        height: 24px;
    }

    .search-btn-expanded:hover {
        color: #0066cc;
        background: rgba(142, 142, 147, 0.15);
    }

    .clear-search-btn {
        position: absolute;
        right: 36px;
        background: none;
        border: none;
        color: #86868b;
        cursor: pointer;
        padding: 4px;
        border-radius: 50%;
        transition: all 0.3s;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        width: 24px;
        height: 24px;
    }

    .clear-search-btn:hover {
        color: #0066cc;
        background: rgba(142, 142, 147, 0.15);
    }

    @keyframes expandSearch {
        0% {
            opacity: 0;
            transform: scale(0.8) translateX(20px);
        }
        100% {
            opacity: 1;
            transform: scale(1) translateX(0);
        }
    }

    /* User Menu */
    .user-menu-container {
        position: relative;
    }

    .user-menu-toggle {
        background: none;
        border: none;
        color: #1d1d1f;
        cursor: pointer;
        padding: 6px;
        border-radius: 50%;
        transition: all 0.3s;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
    }

    .user-menu-toggle:hover {
        background: rgba(142, 142, 147, 0.12);
        color: #0066cc;
    }

    .user-menu {
        position: absolute;
        top: 100%;
        right: 0;
        background: rgba(255, 255, 255, 0.95);
        backdrop-filter: saturate(180%) blur(20px);
        border: 1px solid rgba(0, 0, 0, 0.1);
        border-radius: 8px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
        min-width: 200px;
        margin-top: 8px;
        z-index: 100;
        animation: fadeInUp 0.2s ease-out;
    }

    .user-info {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        color: #1d1d1f;
        font-size: 13px;
        font-weight: 500;
        background: rgba(142, 142, 147, 0.05);
    }

    .user-info .material-symbols-outlined {
        font-size: 18px;
        color: #0066cc;
    }

    .username {
        text-transform: capitalize;
    }

    .user-menu-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        color: #1d1d1f;
        cursor: pointer;
        transition: all 0.3s;
        font-size: 13px;
    }

    .user-menu-item:hover {
        background: rgba(142, 142, 147, 0.08);
        color: #0066cc;
    }

    .user-menu-item .material-symbols-outlined {
        font-size: 18px;
    }

    .user-menu-divider {
        height: 1px;
        background: rgba(0, 0, 0, 0.1);
        margin: 4px 0;
    }

    /* Mobile styles */
    .mobile-search-toggle,
    .mobile-toggle {
        display: none;
        background: none;
        border: none;
        color: #1d1d1f;
        cursor: pointer;
        padding: 6px;
        border-radius: 4px;
        transition: all 0.3s;
        align-items: center;
        justify-content: center;
    }

    .mobile-search-toggle:hover,
    .mobile-toggle:hover {
        background: rgba(142, 142, 147, 0.12);
    }

    /* Mobile Login Icon */
    .mobile-nav-item.login-icon {
        border-bottom: none;
        padding: 0;
        margin-left: auto;
        justify-content: flex-end;
    }

    .mobile-user-actions {
        border-top: 1px solid rgba(0, 0, 0, 0.1);
        padding-top: 20px;
        margin-top: 20px;
        display: flex;
        align-items: center;
        justify-content: flex-end;
    }

    .mobile-user-info {
        margin-right: auto;
        border-bottom: none;
        margin-bottom: 0;
        padding: 0;
    }

    .hamburger-line {
        width: 18px;
        height: 1px;
        background: #1d1d1f;
        transition: all 0.3s;
        margin: 2px 0;
    }

    .hamburger-line.active:nth-child(1) {
        transform: rotate(45deg) translate(3px, 3px);
    }

    .hamburger-line.active:nth-child(2) {
        opacity: 0;
    }

    .hamburger-line.active:nth-child(3) {
        transform: rotate(-45deg) translate(3px, -3px);
    }

    /* Mobile Navigation */
    .mobile-nav {
        background: rgba(255, 255, 255, 0.72);
        backdrop-filter: saturate(180%) blur(20px);
        border-top: 1px solid rgba(0, 0, 0, 0.1);
        animation: slideDown 0.3s ease-out;
    }

    .mobile-nav-content {
        max-width: 90vw;
        margin: 0 auto;
        padding: 20px 12px;
        display: flex;
        flex-direction: column;
        gap: 20px;
        box-sizing: border-box;
    }

    .mobile-nav-item {
        color: #1d1d1f;
        text-decoration: none;
        font-size: 17px;
        font-weight: 400;
        transition: color 0.3s;
        padding: 10px 0;
        border-bottom: 1px solid rgba(0, 0, 0, 0.1);
        background: none;
        border: none;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 12px;
    }

    .mobile-nav-item:hover {
        color: #0066cc;
    }

    .mobile-nav-item:last-child {
        border-bottom: none;
    }

    /* Style for active mobile route */
    :global(a.navLinkActive.mobile-nav-item) {
        color: #1d1d1f !important;
    }

    /* Mobile User Actions */
    .mobile-user-actions {
        border-top: 1px solid rgba(0, 0, 0, 0.1);
        padding-top: 20px;
        margin-top: 20px;
    }

    .mobile-user-info {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 10px 0;
        color: #1d1d1f;
        font-weight: 500;
        font-size: 16px;
        border-bottom: 1px solid rgba(0, 0, 0, 0.1);
        margin-bottom: 20px;
    }

    .mobile-user-info .material-symbols-outlined {
        color: #0066cc;
        font-size: 20px;
    }

    .mobile-search-container {
        background: rgba(255, 255, 255, 0.72);
        backdrop-filter: saturate(180%) blur(20px);
        border-top: 1px solid rgba(0, 0, 0, 0.1);
        padding: 15px 12px;
        animation: slideDown 0.3s ease-out;
    }

    .mobile-search-content {
        position: relative;
        display: flex;
        align-items: center;
        max-width: 90vw;
        margin: 0 auto;
        box-sizing: border-box;
    }

    .mobile-search-input {
        background: rgba(142, 142, 147, 0.12);
        color: #1d1d1f;
        border: none;
        border-radius: 6px;
        padding: 8px 32px 8px 12px;
        width: 100%;
        outline: none;
        font-size: 14px;
        /* Cho phép text cuộn trên mobile */
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .mobile-search-btn,
    .mobile-clear-search-btn {
        position: absolute;
        right: 4px;
        background: none;
        border: none;
        color: #86868b;
        cursor: pointer;
        padding: 6px;
        border-radius: 4px;
        transition: all 0.3s;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
    }

    .mobile-search-btn.has-clear {
        right: 32px;
    }

    .mobile-search-btn:hover,
    .mobile-clear-search-btn:hover {
        color: #1d1d1f;
    }

    @keyframes slideDown {
        from {
            opacity: 0;
            transform: translateY(-10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    @keyframes fadeInUp {
        from {
            opacity: 0;
            transform: translateY(-10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    /* Search Info */
    .search-info {
        background: rgba(248, 248, 248, 0.8);
        border-bottom: 1px solid rgba(0, 0, 0, 0.1);
        padding: 15px 0;
    }

    .search-info-content {
        max-width: 90vw;
        margin: 0 auto;
        padding: 0 12px;
        box-sizing: border-box;
    }

    .search-info p {
        margin: 0;
        color: #1d1d1f;
        font-size: 14px;
    }

    /* App Layout */
    .app {
        min-height: 100vh;
        display: flex;
        flex-direction: column;
        position: relative;
    }

    .app-main {
        flex: 1;
    }

    /* Loading Overlay */
    .loading-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 9999;
        backdrop-filter: blur(10px);
    }

    .loading-spinner {
        text-align: center;
        color: white;
    }

    .spinner {
        width: 40px;
        height: 40px;
        border: 2px solid rgba(255, 255, 255, 0.3);
        border-top: 2px solid #0066cc;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin: 0 auto 15px;
    }

    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }

    /* Initial Loading Screen */
    .initial-loading {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: #f5f5f7;
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 10000;
    }

    .loading-content {
        text-align: center;
        animation: fadeInUp 0.8s ease-out;
    }

    .logo {
        font-size: 48px;
        font-weight: 600;
        color: #1d1d1f;
        margin-bottom: 20px;
    }

    .loading-content h2 {
        color: #1d1d1f;
        font-size: 24px;
        font-weight: 400;
        margin-bottom: 30px;
    }

    .loading-dots {
        display: flex;
        justify-content: center;
        gap: 8px;
    }

    .loading-dots span {
        width: 8px;
        height: 8px;
        background: #0066cc;
        border-radius: 50%;
        animation: dot-bounce 1.4s infinite both;
    }

    .loading-dots span:nth-child(2) {
        animation-delay: 0.2s;
    }

    .loading-dots span:nth-child(3) {
        animation-delay: 0.4s;
    }

    @keyframes dot-bounce {
        0%, 80%, 100% {
            transform: scale(0.8);
            opacity: 0.5;
        }
        40% {
            transform: scale(1);
            opacity: 1;
        }
    }

    /* Footer */
    .app-footer {
        background: #f5f5f7;
        color: #1d1d1f;
        margin-top: 60px;
        border-top: 1px solid rgba(0, 0, 0, 0.1);
    }

    .footer-bottom {
        border-top: 1px solid rgba(0, 0, 0, 0.1);
        padding: 20px 0;
        background: rgba(0, 0, 0, 0.02);
    }

    .footer-bottom-content {
        max-width: 90vw;
        margin: 0 auto;
        padding: 0 12px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 20px;
        box-sizing: border-box;
    }

    .footer-left {
        display: flex;
        align-items: center;
    }

    .footer-left .logo-section {
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .logo-icon-footer {
        width: 20px;
        height: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;
    }

    .logo-icon-footer img {
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;
    }

    .logo-text-footer {
        font-size: 12px;
        font-weight: 400;
        color: #86868b;
    }

    .footer-center {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
    }

    .footer-center p {
        margin: 0;
        color: #86868b;
        font-size: 12px;
    }

    .footer-right p {
        margin: 0;
        color: #86868b;
        font-size: 12px;
    }

    /* Responsive Design */
    @media (max-width: 90vw) {
        .header-container,
        .footer-bottom-content,
        .mobile-nav-content,
        .mobile-search-content,
        .search-info-content {
            max-width: 100%;
            padding: 0 8px;
        }
    }

    @media (max-width: 1024px) {
        .nav-menu {
            gap: 36px;
        }

        .nav-item {
            font-size: 11px;
        }

        .search-input.expanded {
            width: 220px;
        }

        .search-input.expanded:focus {
            width: 260px;
        }
    }

    @media (max-width: 768px) {
        .nav-menu {
            display: none;
        }

        .desktop-search {
            display: none;
        }

        .mobile-search-toggle,
        .mobile-toggle {
            display: flex;
        }

        .header-actions {
            gap: 10px;
        }

        .footer-bottom-content {
            flex-direction: column;
            text-align: center;
            gap: 15px;
        }
    }

    @media (max-width: 480px) {
        .header-container {
            padding: 0 8px;
        }

        .logo-text {
            font-size: 14px;
        }

        .mobile-search-container {
            padding: 10px 8px;
        }

        .mobile-search-content {
            max-width: 100%;
        }
    }
</style>