<script>
    import { createEventDispatcher, onMount } from 'svelte';
    import { fade, fly, scale } from 'svelte/transition';
    import { quintOut, elasticOut } from 'svelte/easing';
    import auth from '../lib/config/auth-config.js';
    import VantaBackground from "../lib/compoments/VantaBackground.svelte";
    // Import logo (giả sử logo được import từ assets)
    import logo from '../assets/logo_VNPT.png';

    const dispatch = createEventDispatcher();

    let username = '';
    let password = '';
    let isLoading = false;
    let showPassword = false;
    let loginError = '';
    let mounted = false;
    let formVisible = false;

    // Form validation
    let usernameError = '';
    let passwordError = '';
    let isFormValid = false;

    onMount(async () => {
        mounted = true;

        // Initialize auth and check if already authenticated
        try {
            const isAlreadyAuthenticated = await auth.initAuth();
            if (isAlreadyAuthenticated) {
                // User is already logged in, dispatch success
                const userInfo = auth.getCurrentUser();
                dispatch('loginSuccess', {
                    username: userInfo?.username || userInfo?.email || 'user',
                    isAuthenticated: true,
                    isAdmin: auth.hasRole('ADMIN') || auth.hasRole('admin'),
                    user: userInfo
                });
                return;
            }
        } catch (error) {
            console.error('Error initializing auth:', error);
        }

        // Trigger form animation after a short delay
        setTimeout(() => {
            formVisible = true;
        }, 300);
    });

    // Reactive validation
    $: {
        // Username validation
        if (username.length === 0) {
            usernameError = '';
        } else if (username.length < 3) {
            usernameError = 'Tên đăng nhập phải có ít nhất 3 ký tự';
        } else {
            usernameError = '';
        }

        // Password validation
        if (password.length === 0) {
            passwordError = '';
        } else if (password.length < 6) {
            passwordError = 'Mật khẩu phải có ít nhất 6 ký tự';
        } else {
            passwordError = '';
        }

        // Form validation
        isFormValid = username.length >= 3 && password.length >= 6 && !usernameError && !passwordError;
    }

    function togglePasswordVisibility() {
        showPassword = !showPassword;
    }

    async function handleLogin() {
        if (!isFormValid || isLoading) return;

        isLoading = true;
        loginError = '';

        try {
            // Prepare credentials object
            const credentials = {
                username: username.trim(),
                password: password
            };

            // Call the actual login API
            const result = await auth.login(credentials);

            if (result.success) {
                // Login successful
                dispatch('loginSuccess', {
                    username: result.user?.username || result.user?.email || username,
                    isAuthenticated: true,
                    isAdmin: auth.hasRole('ADMIN') || auth.hasRole('admin'),
                    user: result.user,
                    accessToken: result.accessToken
                });
            } else {
                // This shouldn't happen if login() throws on failure
                loginError = 'Đăng nhập thất bại. Vui lòng thử lại.';
            }
        } catch (error) {
            console.error('Login error:', error);

            // Handle different types of errors
            if (error.message) {
                loginError = error.message;
            } else if (error.response?.status === 401) {
                loginError = 'Tên đăng nhập hoặc mật khẩu không chính xác';
            } else if (error.response?.status === 403) {
                loginError = 'Tài khoản bị khóa hoặc không có quyền truy cập';
            } else if (error.response?.status === 500) {
                loginError = 'Lỗi máy chủ. Vui lòng thử lại sau.';
            } else if (error.code === 'NETWORK_ERROR' || !navigator.onLine) {
                loginError = 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.';
            } else {
                loginError = 'Đã xảy ra lỗi trong quá trình đăng nhập. Vui lòng thử lại.';
            }
        } finally {
            isLoading = false;
        }
    }

    function handleKeyPress(event) {
        if (event.key === 'Enter' && isFormValid) {
            handleLogin();
        }
    }

    function handleForgotPassword() {
        // Handle forgot password logic
        console.log('Forgot password clicked');
        dispatch('forgotPassword');
    }

    function handleBackToHome() {
        dispatch('backToHome');
    }

    // Clear error when user starts typing
    $: if (username || password) {
        if (loginError && !isLoading) {
            loginError = '';
        }
    }
</script>

<VantaBackground />

<div class="login-container">
    <!-- Animated Background -->
    <div class="animated-background">
        <div class="floating-shapes">
            <div class="shape shape-1"></div>
            <div class="shape shape-2"></div>
            <div class="shape shape-3"></div>
            <div class="shape shape-4"></div>
            <div class="shape shape-5"></div>
        </div>
    </div>

    <!-- Back to Home Button -->
    <button class="back-button" on:click={handleBackToHome}
            in:fly="{{ x: -50, duration: 600, delay: 200, easing: quintOut }}">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        Trang chủ
    </button>

    <!-- Login Form -->
    <div class="login-wrapper">
        {#if mounted}
            <!-- Logo Section -->
            <div class="logo-section"
                 in:fly="{{ y: -30, duration: 800, delay: 100, easing: quintOut }}">
                <div class="logo-container">
                    <img src={logo} alt="VNPT Logo" class="logo-image" />
                </div>
                <h1 class="brand-title">VNPT-IT</h1>
                <p class="welcome-text">Chào mừng bạn trở lại</p>
            </div>

            {#if formVisible}
                <!-- Login Form -->
                <form class="login-form"
                      on:submit|preventDefault={handleLogin}
                      in:fly="{{ y: 50, duration: 800, delay: 400, easing: quintOut }}">

                    <!-- Form Title -->
                    <h2 class="form-title">Đăng nhập</h2>

                    <!-- Error Message -->
                    {#if loginError}
                        <div class="error-message"
                             in:fly="{{ y: -20, duration: 400, easing: quintOut }}"
                             out:fade="{{ duration: 300 }}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="12" cy="12" r="10"/>
                                <line x1="15" y1="9" x2="9" y2="15"/>
                                <line x1="9" y1="9" x2="15" y2="15"/>
                            </svg>
                            {loginError}
                        </div>
                    {/if}

                    <!-- Username Field -->
                    <div class="form-field">
                        <label for="username" class="field-label">Tên đăng nhập</label>
                        <div class="input-wrapper" class:error={usernameError} class:focused={username}>
                            <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                <circle cx="12" cy="7" r="4"/>
                            </svg>
                            <input
                                    id="username"
                                    type="text"
                                    bind:value={username}
                                    on:keypress={handleKeyPress}
                                    placeholder="Nhập tên đăng nhập"
                                    class="form-input"
                                    disabled={isLoading}
                                    autocomplete="username"
                            />
                        </div>
                        {#if usernameError}
                            <span class="field-error" in:fly="{{ y: -10, duration: 300, easing: quintOut }}">
                                {usernameError}
                            </span>
                        {/if}
                    </div>

                    <!-- Password Field -->
                    <div class="form-field">
                        <label for="password" class="field-label">Mật khẩu</label>
                        <div class="input-wrapper" class:error={passwordError} class:focused={password}>
                            <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                                <circle cx="12" cy="16" r="1"/>
                                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                            </svg>
                            <input
                                    id="password"
                                    type={showPassword ? 'text' : 'password'}
                                    bind:value={password}
                                    on:keypress={handleKeyPress}
                                    placeholder="Nhập mật khẩu"
                                    class="form-input"
                                    disabled={isLoading}
                                    autocomplete="current-password"
                            />
                            <button
                                    type="button"
                                    class="password-toggle"
                                    on:click={togglePasswordVisibility}
                                    disabled={isLoading}
                            >
                                {#if showPassword}
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                                        <line x1="1" y1="1" x2="23" y2="23"/>
                                    </svg>
                                {:else}
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                                        <circle cx="12" cy="12" r="3"/>
                                    </svg>
                                {/if}
                            </button>
                        </div>
                        {#if passwordError}
                            <span class="field-error" in:fly="{{ y: -10, duration: 300, easing: quintOut }}">
                                {passwordError}
                            </span>
                        {/if}
                    </div>

                    <!-- Login Button -->
                    <button
                            type="submit"
                            class="login-button"
                            class:loading={isLoading}
                            disabled={!isFormValid || isLoading}
                    >
                        {#if isLoading}
                            <div class="loading-spinner" in:scale="{{ duration: 400, easing: elasticOut }}">
                                <div class="spinner"></div>
                            </div>
                            Đang đăng nhập...
                        {:else}
                            Đăng nhập
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4M10 17l5-5-5-5M21 12H9"/>
                            </svg>
                        {/if}
                    </button>

                    <!-- Forgot Password Link -->
<!--                    <button-->
<!--                            type="button"-->
<!--                            class="forgot-password"-->
<!--                            on:click={handleForgotPassword}-->
<!--                            disabled={isLoading}-->
<!--                    >-->
<!--                        Quên mật khẩu?-->
<!--                    </button>-->
                </form>
            {/if}
        {/if}
    </div>
</div>

<style>

    .login-container {
        min-height: 100vh;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        position: relative;
        padding: 20px;
        background-size: cover;
        background-repeat: no-repeat;
        background-position: center;
        background-attachment: fixed;
    }

    .login-container::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.6);
        z-index: 1;
    }

    /* Animated Background */
    .animated-background {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 0;
        overflow: hidden;
    }

    .floating-shapes {
        position: absolute;
        width: 100%;
        height: 100%;
    }

    .shape {
        position: absolute;
        border-radius: 50%;
        background: linear-gradient(45deg, rgba(255, 255, 255, 0.03), rgba(255, 255, 255, 0.08));
        animation: float 20s infinite ease-in-out;
    }

    .shape-1 {
        width: 120px;
        height: 120px;
        top: 15%;
        left: 15%;
        animation-delay: 0s;
    }

    .shape-2 {
        width: 80px;
        height: 80px;
        top: 70%;
        right: 20%;
        animation-delay: -5s;
    }

    .shape-3 {
        width: 200px;
        height: 200px;
        bottom: 10%;
        left: -50px;
        animation-delay: -10s;
    }

    .shape-4 {
        width: 60px;
        height: 60px;
        top: 30%;
        right: 10%;
        animation-delay: -15s;
    }

    .shape-5 {
        width: 150px;
        height: 150px;
        top: 60%;
        left: 70%;
        animation-delay: -7s;
    }

    @keyframes float {
        0%, 100% { transform: translateY(0px) rotate(0deg); }
        25% { transform: translateY(-20px) rotate(90deg); }
        50% { transform: translateY(-40px) rotate(180deg); }
        75% { transform: translateY(-20px) rotate(270deg); }
    }

    /* Back Button */
    .back-button {
        position: absolute;
        top: 30px;
        left: 30px;
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px 20px;
        background: rgba(255, 255, 255, 0.1);
        border: 1px solid rgba(255, 255, 255, 0.2);
        border-radius: 8px;
        color: #ffffff;
        text-decoration: none;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;
        backdrop-filter: blur(10px);
        z-index: 10;
    }

    .back-button:hover {
        background: rgba(255, 255, 255, 0.15);
        border-color: rgba(255, 255, 255, 0.3);
        transform: translateX(-4px);
    }

    /* Login Wrapper */
    .login-wrapper {
        position: relative;
        z-index: 5;
        width: 100%;
        max-width: 400px;
        margin: 0 auto;
    }

    /* Logo Section */
    .logo-section {
        text-align: center;
        margin-bottom: 40px;
    }

    .logo-container {
        width: 80px;
        height: 80px;
        margin: 0 auto 20px;
        background: rgba(255, 255, 255, 0.1);
        border-radius: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.2);
        transition: all 0.3s ease;
    }

    .logo-container:hover {
        transform: scale(1.05);
        background: rgba(255, 255, 255, 0.15);
    }

    .logo-image {
        width: 50px;
        height: 50px;
        object-fit: contain;
    }

    .brand-title {
        font-size: 2rem;
        font-weight: 700;
        margin-bottom: 8px;
        background: linear-gradient(135deg, #ffffff 0%, #aaaaaa 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
    }

    .welcome-text {
        color: #aaaaaa;
        font-size: 1rem;
        margin: 0;
    }

    /* Login Form */
    .login-form {
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 16px;
        padding: 32px;
        backdrop-filter: blur(20px);
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
    }

    .form-title {
        font-size: 1.5rem;
        font-weight: 600;
        text-align: center;
        margin-bottom: 24px;
        color: #ffffff;
    }

    /* Error Message */
    .error-message {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px 16px;
        background: rgba(239, 68, 68, 0.1);
        border: 1px solid rgba(239, 68, 68, 0.3);
        border-radius: 8px;
        color: #ef4444;
        font-size: 14px;
        margin-bottom: 20px;
    }

    /* Form Fields */
    .form-field {
        margin-bottom: 20px;
    }

    .field-label {
        display: block;
        margin-bottom: 8px;
        color: #cccccc;
        font-size: 14px;
        font-weight: 500;
    }

    .input-wrapper {
        position: relative;
        display: flex;
        align-items: center;
        background: rgba(255, 255, 255, 0.08);
        border: 1px solid rgba(255, 255, 255, 0.15);
        border-radius: 8px;
        transition: all 0.3s ease;
    }

    .input-wrapper:focus-within,
    .input-wrapper.focused {
        border-color: rgba(255, 255, 255, 0.3);
        background: rgba(255, 255, 255, 0.12);
        box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.1);
    }

    .input-wrapper.error {
        border-color: rgba(239, 68, 68, 0.5);
        background: rgba(239, 68, 68, 0.05);
    }

    .input-icon {
        position: absolute;
        left: 12px;
        color: #888888;
        transition: color 0.3s ease;
        z-index: 2;
    }

    .input-wrapper:focus-within .input-icon,
    .input-wrapper.focused .input-icon {
        color: #cccccc;
    }

    .form-input {
        width: 100%;
        padding: 14px 16px 14px 44px;
        border: none;
        background: transparent;
        color: #ffffff;
        font-size: 16px;
        outline: none;
        transition: all 0.3s ease;
    }

    .form-input::placeholder {
        color: #666666;
    }

    .form-input:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .password-toggle {
        position: absolute;
        right: 12px;
        background: none;
        border: none;
        color: #888888;
        cursor: pointer;
        padding: 4px;
        border-radius: 4px;
        transition: all 0.3s ease;
        z-index: 2;
    }

    .password-toggle:hover {
        color: #cccccc;
        background: rgba(255, 255, 255, 0.1);
    }

    .password-toggle:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }

    /* Field Error */
    .field-error {
        display: block;
        margin-top: 6px;
        color: #ef4444;
        font-size: 12px;
    }

    /* Login Button */
    .login-button {
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        padding: 16px 24px;
        border: none;
        border-radius: 8px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #ffffff;
        font-size: 16px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        position: relative;
        overflow: hidden;
        margin-bottom: 16px;
    }

    .login-button::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
        transition: left 0.5s ease;
    }

    .login-button:hover::before {
        left: 100%;
    }

    .login-button:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 25px rgba(102, 126, 234, 0.3);
    }

    .login-button:active {
        transform: translateY(0);
    }

    .login-button:disabled {
        opacity: 0.6;
        cursor: not-allowed;
        transform: none;
        box-shadow: none;
    }

    .login-button.loading {
        cursor: not-allowed;
    }

    /* Loading Spinner */
    .loading-spinner {
        display: inline-flex;
        align-items: center;
        margin-right: 8px;
    }

    .spinner {
        width: 16px;
        height: 16px;
        border: 2px solid rgba(255, 255, 255, 0.3);
        border-top: 2px solid #ffffff;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }

    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }

    /* Forgot Password */
    .forgot-password {
        width: 100%;
        background: none;
        border: none;
        color: #888888;
        font-size: 14px;
        cursor: pointer;
        padding: 8px 0;
        text-align: center;
        transition: color 0.3s ease;
    }

    .forgot-password:hover {
        color: #cccccc;
        text-decoration: underline;
    }

    .forgot-password:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }

    /* Responsive Design */
    @media (max-width: 768px) {
        .login-container {
            padding: 15px;
        }

        .back-button {
            top: 20px;
            left: 20px;
            padding: 10px 16px;
            font-size: 13px;
        }

        .login-wrapper {
            max-width: 350px;
        }

        .logo-section {
            margin-bottom: 30px;
        }

        .logo-container {
            width: 70px;
            height: 70px;
            margin-bottom: 16px;
        }

        .logo-image {
            width: 40px;
            height: 40px;
        }

        .brand-title {
            font-size: 1.75rem;
        }

        .login-form {
            padding: 24px;
        }
    }

    @media (max-width: 480px) {
        .login-container {
            padding: 10px;
        }

        .login-wrapper {
            max-width: 100%;
        }

        .back-button {
            position: relative;
            top: auto;
            left: auto;
            margin-bottom: 20px;
            align-self: flex-start;
        }

        .login-form {
            padding: 20px;
        }

        .form-input {
            font-size: 16px; /* Prevent zoom on iOS */
        }
    }

    @media (prefers-reduced-motion: reduce) {
        * {
            animation-duration: 0.01ms !important;
            animation-iteration-count: 1 !important;
            transition-duration: 0.01ms !important;
        }
    }
</style>