<script>
    import { onMount } from 'svelte';
    import { fade, fly, scale, blur } from 'svelte/transition';
    import { quintOut, elasticOut } from 'svelte/easing';
    import { getAllIntroduces, getImageById } from "../lib/services/page.service.js";

    // Props from parent component
    export let searchResults = [];
    export let showSearchResults = false;
    export let searchQuery = '';

    let products = [];
    let displayProducts = [];
    let filteredProducts = [];
    let imageCache = new Map();
    let mounted = false;
    let heroVisible = false;
    let showStaging = false;

    onMount(async () => {
        try {
            const response = await getAllIntroduces();

            if (Array.isArray(response)) {
                products = response.map(item => ({
                    id: item.id,
                    title: item.title,
                    description: item.description,
                    tag: item.tag,
                    url: item.url,
                    status: item.status,
                    imageId: item.imageId,
                    createAt: item.createAt,
                    image: null,
                    imageLoading: false
                }));

                displayProducts = products;
                filterProducts();
                // Load ảnh cho filteredProducts ngay sau khi filter
                loadImagesForProducts(filteredProducts);
            } else {
                console.error('Unexpected API response:', response);
            }
        } catch (error) {
            console.error('Error fetching products:', error);
        }

        mounted = true;

        setTimeout(() => {
            heroVisible = true;
        }, 100);
    });

    function formatISOToVietnameseDate(isoString) {
        const date = new Date(isoString);
        const day = date.getUTCDate();
        const month = date.getUTCMonth();
        const year = date.getUTCFullYear();

        return `${day} thg ${month}, ${year}`;
    }

    function filterProducts() {
        filteredProducts = products.filter(product =>
            showStaging || product.tag === 'Production'
        );
        // Đảm bảo cập nhật displayProducts sau khi filter
        if (!showSearchResults) {
            displayProducts = filteredProducts;
        }
        // Load lại ảnh mỗi khi filter thay đổi
        loadImagesForProducts(filteredProducts);
    }

    function toggleStaging() {
        showStaging = !showStaging;
        filterProducts();
    }

    async function loadImagesForProducts(productsToLoad) {
        for (let product of productsToLoad) {
            if (product.imageId && !product.image && !product.imageLoading) {
                await loadImageForProduct(product);
            }
        }
    }

    async function loadImageForProduct(product) {
        if (!product.imageId || product.imageLoading) return;

        if (imageCache.has(product.imageId)) {
            product.image = imageCache.get(product.imageId);
            return;
        }

        product.imageLoading = true;
        try {
            const imageResponse = await getImageById(product.imageId);
            if (imageResponse && imageResponse.base64 && imageResponse.contentType) {
                const imageUrl = `data:${imageResponse.contentType};base64,${imageResponse.base64}`;
                imageCache.set(product.imageId, imageUrl);
                product.image = imageUrl;
            }
        } catch (error) {
            console.error('Error loading image for product:', product.id, error);
        } finally {
            product.imageLoading = false;
            // Force update
            filteredProducts = filteredProducts;
            displayProducts = displayProducts;
        }
    }

    $: {
        if (showSearchResults && searchResults.length >= 0) {
            displayProducts = searchResults.map(item => ({
                id: item.id,
                title: item.title,
                description: item.description,
                tag: item.tag,
                url: item.url,
                status: item.status,
                imageId: item.imageId,
                createAt: item.createAt,
                image: null,
                imageLoading: false
            }));
            loadImagesForProducts(displayProducts);
        } else if (!showSearchResults) {
            filterProducts();
        }
    }

    $: {
        if (showSearchResults && searchResults.length >= 0) {
            displayProducts = searchResults.map(item => ({
                id: item.id,
                title: item.title,
                description: item.description,
                tag: item.tag,
                url: item.url,
                status: item.status,
                imageId: item.imageId,
                createAt: item.createAt,
                image: null,
                imageLoading: false
            }));
            loadImagesForProducts(displayProducts);
        } else if (!showSearchResults) {
            displayProducts = products;
            filterProducts();
        }
    }

    function highlightText(text, searchTerm) {
        if (!searchTerm || !text) return text;
        const regex = new RegExp(`(${searchTerm.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
        return text.replace(regex, '<mark>$1</mark>');
    }

    function getRandomGradient() {
        const gradients = [
            'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
            'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
            'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
            'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
            'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
            'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
            'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
            'linear-gradient(135deg, #ff8a80 0%, #ea80fc 100%)',
            'linear-gradient(135deg, #8fd3f4 0%, #84fab0 100%)'
        ];
        return gradients[Math.floor(Math.random() * gradients.length)];
    }

    function navigateToProduct(url) {
        window.open(url, '_blank', 'noopener,noreferrer');
    }
</script>

<div class="page-container">
    <!-- Hero Section -->
    <section class="hero-section">
        {#if heroVisible}
            <div class="hero-content"
                 in:fly="{{ y: 50, duration: 1000, easing: quintOut }}"
                 out:fade="{{ duration: 500 }}">
                <h1 class="hero-title">
                    <span class="title-word" style="--delay: 0s">Khám</span>
                    <span class="title-word" style="--delay: 0.2s">phá</span>
                    <span class="title-word" style="--delay: 0.4s">các</span>
                    <span class="title-word" style="--delay: 0.6s">sản</span>
                    <span class="title-word" style="--delay: 0.8s">phẩm</span>
                    <span class="title-word" style="--delay: 1s">công</span>
                    <span class="title-word" style="--delay: 1.2s">nghệ</span>
                </h1>
                <p class="hero-subtitle"
                   in:fly="{{ y: 30, duration: 800, delay: 600, easing: quintOut }}">
                    Trải nghiệm những giải pháp công nghệ hàng đầu từ VNPT-IT
                </p>
            </div>
        {/if}
    </section>

    <!-- Main Content -->
    <main class="main-content">
        <!-- Section Header -->
        {#if mounted}
            <div class="section-header"
                 in:fly="{{ y: 30, duration: 600, delay: 800, easing: quintOut }}">
                <h2 class="section-title">
                    {#if showSearchResults}
                        Kết quả tìm kiếm
                        {#if searchQuery}
                            cho "{searchQuery}"
                        {/if}
                    {:else}
                        Sản phẩm
                    {/if}
                </h2>

                {#if !showSearchResults}
                    <button class="view-all-button" on:click={toggleStaging}>
                        {showStaging ? 'Ẩn tất cả' : 'Xem tất cả'}
                    </button>
                {/if}
            </div>
        {/if}

        <!-- Products Grid -->
        <div class="products-grid">
            {#each (showSearchResults ? displayProducts : filteredProducts) as product, index}
                <a class="product-card"
                   href={product.url}
                   target="_blank"
                   rel="noopener noreferrer"
                   style="--gradient: {getRandomGradient()}"
                   in:fly="{{ y: 60, duration: 600, delay: 200 + (index * 100), easing: quintOut }}"
                   out:scale="{{ duration: 300, start: 0.8 }}">
                    <div class="card-image">
                        {#if product.imageLoading}
                            <div class="loading-spinner" in:scale="{{ duration: 400, easing: elasticOut }}">
                                <div class="spinner"></div>
                            </div>
                        {:else if product.image}
                            <img src={product.image}
                                 alt={product.title}
                                 class="product-image"
                                 in:fade="{{ duration: 500 }}" />
                        {:else}
                            <div class="placeholder-icon" in:scale="{{ duration: 400, easing: elasticOut }}">
                                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                                    <circle cx="8.5" cy="8.5" r="1.5"/>
                                    <polyline points="21,15 16,10 5,21"/>
                                </svg>
                            </div>
                        {/if}
                    </div>

                    <div class="card-content">
                        <div class="product-top">
                            <h3 class="product-title">
                                {#if showSearchResults && searchQuery}
                                    {@html highlightText(product.title, searchQuery)}
                                {:else}
                                    {product.title}
                                {/if}
                            </h3>

                            <div class="product-meta">
                                <span class="product-category">
                                    {#if showSearchResults && searchQuery}
                                        {@html product.tag}
                                    {:else}
                                        {product.tag}
                                    {/if}
                                </span>
                                <span class="product-date">
                                    {formatISOToVietnameseDate(new Date(product.createAt).toISOString())}
                                </span>
                            </div>

                            <p class="product-description">
                                {#if showSearchResults && searchQuery}
                                    {@html product.description}
                                {:else}
                                    {product.description}
                                {/if}
                            </p>
                        </div>

                        <div class="product-bottom">
                            <div class="product-status">
                                <span class="status-indicator {product.status === 'ACTIVE' ? 'active' : 'inactive'}">
                                    {product.status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động'}
                                </span>
                            </div>

<!--                            <button class="access-button"-->
<!--                                    on:click|stopPropagation={() => navigateToProduct(product.url)}>-->
<!--                                Truy cập ngay-->
<!--                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">-->
<!--                                    <path d="M7 17L17 7M17 7H7M17 7V17"/>-->
<!--                                </svg>-->
<!--                            </button>-->
                        </div>
                    </div>
                </a>
            {/each}
        </div>

        <!-- Empty State -->
        {#if (showSearchResults ? displayProducts : filteredProducts).length === 0 && mounted}
            <div class="empty-state"
                 in:fly="{{ y: 30, duration: 600, delay: 400, easing: quintOut }}">
                <div class="empty-icon" in:scale="{{ duration: 600, delay: 600, easing: elasticOut }}">
                    <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
                        <circle cx="11" cy="11" r="8"/>
                        <path d="m21 21-4.35-4.35"/>
                    </svg>
                </div>
                <h3 class="empty-title">
                    {#if showSearchResults}
                        Không tìm thấy kết quả
                    {:else}
                        Chưa có sản phẩm
                    {/if}
                </h3>
                <p class="empty-description">
                    {#if showSearchResults}
                        Không có sản phẩm nào phù hợp với từ khóa "{searchQuery}". Hãy thử tìm kiếm với từ khóa khác.
                    {:else}
                        Hiện tại chưa có sản phẩm nào được hiển thị.
                    {/if}
                </p>
            </div>
        {/if}
    </main>
</div>

<style>
    :global(*) {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
    }

    :global(body) {
        font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        background-color: #000000;
        background-size: cover;
        background-repeat: no-repeat;
        background-position: center;
        background-attachment: fixed;
        color: #ffffff;
        line-height: 1.6;
        overflow-x: hidden;
    }

    :global(mark) {
        background: #fbbf24;
        color: #000;
        padding: 2px 4px;
        border-radius: 3px;
        animation: highlight 0.5s ease-in-out;
    }

    @keyframes highlight {
        0% { background: transparent; }
        50% { background: #fbbf24; transform: scale(1.1); }
        100% { background: #fbbf24; transform: scale(1); }
    }

    .page-container {
        min-height: 100vh;
        max-width: 90%;
        margin: 0 auto;
        padding: 0 12px;
        position: relative;
    }

    /* Animated Background */
    .animated-background {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: -1;
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
        top: 20%;
        left: 10%;
        animation-delay: 0s;
    }

    .shape-2 {
        width: 80px;
        height: 80px;
        top: 60%;
        right: 15%;
        animation-delay: -5s;
    }

    .shape-3 {
        width: 200px;
        height: 200px;
        bottom: 20%;
        left: 20%;
        animation-delay: -10s;
    }

    .shape-4 {
        width: 60px;
        height: 60px;
        top: 40%;
        right: 30%;
        animation-delay: -15s;
    }

    .shape-5 {
        width: 150px;
        height: 150px;
        top: 70%;
        left: 60%;
        animation-delay: -7s;
    }

    @keyframes float {
        0%, 100% { transform: translateY(0px) rotate(0deg); }
        25% { transform: translateY(-20px) rotate(90deg); }
        50% { transform: translateY(-40px) rotate(180deg); }
        75% { transform: translateY(-20px) rotate(270deg); }
    }

    /* Hero Section */
    .hero-section {
        padding: 80px 0 60px;
        text-align: center;
        position: relative;
    }

    .hero-content {
        max-width: 600px;
        margin: 0 auto;
    }

    .hero-title {
        font-size: 3.5rem;
        font-weight: 700;
        margin-bottom: 16px;
        line-height: 1.2;
        display: flex;
        flex-wrap: wrap;
        justify-content: center;
        gap: 0.5rem;
    }

    .title-word {
        display: inline-block;
        background: linear-gradient(135deg, #ffffff 0%, #aaaaaa 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        animation: titleAnimation 1s ease-out forwards;
        animation-delay: var(--delay);
        opacity: 0;
        transform: translateY(20px);
    }

    @keyframes titleAnimation {
        0% {
            opacity: 0;
            transform: translateY(20px) scale(0.9);
        }
        100% {
            opacity: 1;
            transform: translateY(0) scale(1);
        }
    }

    .hero-subtitle {
        font-size: 1.25rem;
        color: #888888;
        margin-bottom: 32px;
        opacity: 0;
        animation: subtitleFade 0.8s ease-out 1.4s forwards;
    }

    @keyframes subtitleFade {
        0% {
            opacity: 0;
            transform: translateY(10px);
        }
        100% {
            opacity: 1;
            transform: translateY(0);
        }
    }

    /* Main Content */
    .main-content {
        padding-bottom: 80px;
    }

    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 32px;
    }

    a {
        text-decoration: none;
    }

    .section-title {
        font-size: 2rem;
        font-weight: 600;
        color: #ffffff;
        position: relative;
        overflow: hidden;
    }

    .section-title::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        width: 0;
        height: 2px;
        background: white;
        animation: underlineExpand 1s ease-out 0.5s forwards;
    }

    @keyframes underlineExpand {
        0% { width: 0; }
        100% { width: 100%; }
    }

    /* View All Button */
    .view-all-button {
        background: rgba(255, 255, 255, 0.1);
        color: white;
        border: 1px solid rgba(255, 255, 255, 0.2);
        border-radius: 8px;
        padding: 8px 16px;
        font-size: 0.875rem;
        cursor: pointer;
        transition: all 0.3s ease;
    }

    .view-all-button:hover {
        background: rgba(255, 255, 255, 0.2);
        border-color: rgba(255, 255, 255, 0.3);
    }

    /* Products Grid */
    .products-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
        gap: 16px;
    }

    .product-card {
        background: #1a1a1a;
        border-radius: 16px;
        overflow: hidden;
        transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        border: 1px solid #333333;
        display: flex;
        flex-direction: column;
        position: relative;
        cursor: pointer;
    }

    .product-card::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(45deg, rgba(255, 255, 255, 0.1), transparent);
        opacity: 0;
        transition: opacity 0.3s ease;
        z-index: 1;
        pointer-events: none;
    }

    .product-card:hover {
        transform: translateY(-8px) scale(1.02);
        box-shadow: 0 25px 50px rgba(0, 0, 0, 0.4);
        border-color: #555555;
    }

    .product-card:hover::before {
        opacity: 1;
    }

    .card-image {
        height: 250px;
        background: var(--gradient);
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
        overflow: hidden;
    }

    .card-image::after {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
        transition: left 0.5s ease;
    }

    .product-card:hover .card-image::after {
        left: 100%;
    }

    .product-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.4s ease;
    }

    .product-card:hover .product-image {
        transform: scale(1.05);
    }

    .card-content {
        flex: 1;
        padding: 24px;
        display: flex;
        flex-direction: column;
        position: relative;
        z-index: 2;
    }

    .placeholder-icon {
        color: rgba(255, 255, 255, 0.7);
        animation: pulse 2s infinite;
    }

    @keyframes pulse {
        0%, 100% { opacity: 0.7; }
        50% { opacity: 1; }
    }

    .loading-spinner {
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .spinner {
        width: 32px;
        height: 32px;
        border: 3px solid rgba(255, 255, 255, 0.3);
        border-top: 3px solid #ffffff;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }

    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }

    .product-title {
        font-size: 1.25rem;
        font-weight: 600;
        color: #ffffff;
        margin-bottom: 8px;
        line-height: 1.4;
        transition: color 0.3s ease;
    }

    .product-card:hover .product-title {
        color: #f0f0f0;
    }

    .product-meta {
        display: flex;
        gap: 12px;
        margin-bottom: 16px;
        font-size: 0.875rem;
    }

    .product-category {
        color: #888888;
        font-weight: 500;
        transition: color 0.3s ease;
    }

    .product-card:hover .product-category {
        color: #aaaaaa;
    }

    .product-date {
        color: #666666;
        transition: color 0.3s ease;
    }

    .product-card:hover .product-date {
        color: #888888;
    }

    .product-description {
        color: #aaaaaa;
        font-size: 0.9rem;
        margin-bottom: 20px;
        line-height: 1.6;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
        transition: color 0.3s ease;
    }

    .product-card:hover .product-description {
        color: #cccccc;
    }

    .product-status {
        margin-bottom: 20px;
    }

    .status-indicator {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 6px 12px;
        border-radius: 20px;
        font-size: 0.75rem;
        font-weight: 500;
        letter-spacing: 0.5px;
        transition: all 0.3s ease;
        position: relative;
        overflow: hidden;
    }

    .status-indicator::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
        transition: left 0.5s ease;
    }

    .product-card:hover .status-indicator::before {
        left: 100%;
    }

    .status-indicator.active {
        background: rgba(34, 197, 94, 0.1);
        color: #22c55e;
        border: 1px solid rgba(34, 197, 94, 0.3);
    }

    .status-indicator.inactive {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
        border: 1px solid rgba(239, 68, 68, 0.3);
    }

    .access-button {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        padding: 12px 20px;
        border: none;
        border-radius: 8px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        text-decoration: none;
        background: white;
        color: black;
        position: relative;
        overflow: hidden;
        pointer-events: auto;
    }

    .access-button::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
        transition: left 0.5s ease;
    }

    .access-button:hover::before {
        left: 100%;
    }

    .access-button:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
    }

    .access-button:active {
        transform: translateY(0);
    }

    .access-button svg {
        transition: transform 0.3s ease;
    }

    .access-button:hover svg {
        transform: translateX(2px);
    }

    /* Empty State */
    .empty-state {
        text-align: center;
        padding: 80px 20px;
        max-width: 500px;
        margin: 0 auto;
    }

    .empty-icon {
        margin-bottom: 24px;
        color: #444444;
        animation: float 3s ease-in-out infinite;
    }

    .empty-title {
        font-size: 1.5rem;
        font-weight: 600;
        color: #ffffff;
        margin-bottom: 12px;
    }

    .empty-description {
        color: #888888;
        font-size: 1rem;
        line-height: 1.6;
    }

    /* Responsive Design */
    @media (max-width: 768px) {
        .page-container {
            padding: 0 8px;
        }

        .hero-title {
            font-size: 2.5rem;
        }

        .hero-subtitle {
            font-size: 1.1rem;
        }

        .products-grid {
            grid-template-columns: 1fr;
            gap: 12px;
        }

        .section-header {
            flex-direction: column;
            align-items: flex-start;
            gap: 16px;
        }

        .section-title {
            font-size: 1.5rem;
        }
    }

    @media (max-width: 480px) {
        .hero-section {
            padding: 60px 0 40px;
        }

        .hero-title {
            font-size: 2rem;
        }

        .title-word {
            font-size: 1.8rem;
        }

        .card-content {
            padding: 20px;
        }

        .card-image {
            height: 200px;
        }

        .product-title {
            font-size: 1.1rem;
        }

        .product-card:hover {
            transform: translateY(-4px) scale(1.01);
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