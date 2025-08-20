<script>
    import { onMount } from 'svelte';
    import { createIntroduceWithImage, updateIntroduceWithImage, deleteIntroduce, getAllIntroduces, getImageById } from '../lib/services/page.service.js';
    import { fade, fly, slide } from 'svelte/transition';
    import { quintOut } from 'svelte/easing';
    import {API_BASE_URL_2} from "../lib/services/page.service.js";

    let isLoading = false;
    let showSuccessMessage = false;
    let successMessage = 'Thao tác thành công!';
    let errorMessage = '';
    let showErrorMessage = false;

    // Unified form data
    let formData = {
        title: '',
        description: '',
        tag: '',
        url: '',
        status: 'ACTIVE'
    };
    let selectedFile = null;
    let previewImage = null;

    // Danh sách các card đã tạo
    let existingCards = [];
    let isCardListLoading = false;
    let activeSection = 'create'; // 'create', 'manage'
    let editingCard = null;
    let confirmDeleteCard = null;
    let searchCardQuery = '';
    let filteredCards = [];

    // Image cache similar to +page.svelte
    let imageCache = new Map();

    // Computed properties
    $: isEditMode = !!editingCard;
    $: submitButtonText = isEditMode ? 'Lưu thay đổi' : 'Thêm thẻ mới';
    $: submitButtonIcon = isEditMode ? 'save' : 'add_circle';
    $: formTitle = isEditMode ? `Chỉnh sửa thẻ: ${editingCard.title}` : 'Thêm thẻ mới';
    $: tabButtonText = isEditMode ? 'Chỉnh sửa thẻ' : 'Thêm thẻ mới';
    $: tabButtonIcon = isEditMode ? 'edit_note' : 'add_circle';

    onMount(async () => {
        await loadExistingCards();
    });

    async function loadExistingCards() {
        isCardListLoading = true;
        try {
            const response = await getAllIntroduces();

            if (Array.isArray(response)) {
                existingCards = response.map(item => ({
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

                filteredCards = [...existingCards];
                // Load images for all cards
                await loadImagesForCards(filteredCards);
            } else {
                console.error('Unexpected API response:', response);
                showError('Không thể tải danh sách thẻ. Vui lòng làm mới trang.');
            }
        } catch (error) {
            console.error('Error loading cards:', error);
            showError('Không thể tải danh sách thẻ. Vui lòng làm mới trang.');
        } finally {
            isCardListLoading = false;
        }
    }

    // Load images for multiple cards
    async function loadImagesForCards(cards) {
        for (let card of cards) {
            if (card.imageId && !card.image && !card.imageLoading) {
                await loadImageForCard(card);
            }
        }
    }

    // Load image for a single card (similar to +page.svelte)
    async function loadImageForCard(card) {
        if (!card.imageId || card.imageLoading) return;

        if (imageCache.has(card.imageId)) {
            card.image = imageCache.get(card.imageId);
            // Force update
            existingCards = existingCards;
            filteredCards = filteredCards;
            return;
        }

        card.imageLoading = true;
        try {
            const imageResponse = await getImageById(card.imageId);
            if (imageResponse && imageResponse.base64 && imageResponse.contentType) {
                const imageUrl = `data:${imageResponse.contentType};base64,${imageResponse.base64}`;
                imageCache.set(card.imageId, imageUrl);
                card.image = imageUrl;
            }
        } catch (error) {
            console.error('Error loading image for card:', card.id, error);
        } finally {
            card.imageLoading = false;
            // Force update
            existingCards = existingCards;
            filteredCards = filteredCards;
        }
    }

    // Load preview image for editing
    async function loadPreviewImage(imageId) {
        if (!imageId) return null;

        if (imageCache.has(imageId)) {
            return imageCache.get(imageId);
        }

        try {
            const imageResponse = await getImageById(imageId);
            if (imageResponse && imageResponse.base64 && imageResponse.contentType) {
                const imageUrl = `data:${imageResponse.contentType};base64,${imageResponse.base64}`;
                imageCache.set(imageId, imageUrl);
                return imageUrl;
            }
        } catch (error) {
            console.error('Error loading preview image:', error);
        }
        return null;
    }

    function formatISOToVietnameseDate(isoString) {
        const date = new Date(isoString);
        const day = date.getUTCDate();
        const month = date.getUTCMonth();
        const year = date.getUTCFullYear();

        return `${day} thg ${month}, ${year}`;
    }

    function filterCards() {
        if (!searchCardQuery.trim()) {
            filteredCards = [...existingCards];
            return;
        }

        const query = searchCardQuery.toLowerCase();
        filteredCards = existingCards.filter(card => {
            return card.title.toLowerCase().includes(query) ||
                (card.description && card.description.toLowerCase().includes(query)) ||
                (card.tag && card.tag.toLowerCase().includes(query));
        });
    }

    // Handle file change for unified form
    function handleFileChange(event) {
        const file = event.target.files[0];
        if (file) {
            selectedFile = file;
            const reader = new FileReader();
            reader.onload = (e) => {
                previewImage = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    }

    // Chuyển đổi chế độ
    function setActiveSection(section) {
        activeSection = section;
        if (section === 'create' && !isEditMode) {
            resetForm();
        }
    }

    // Reset form
    function resetForm() {
        formData = {
            title: '',
            description: '',
            tag: '',
            url: '',
            status: 'ACTIVE'
        };
        selectedFile = null;
        previewImage = null;
    }

    // Populate form with existing card data for editing
    async function populateFormForEdit(card) {
        formData = {
            title: card.title || '',
            description: card.description || '',
            tag: card.tag || '',
            url: card.url || '',
            status: card.status || 'ACTIVE'
        };
        selectedFile = null;

        if (card.imageId) {
            previewImage = await loadPreviewImage(card.imageId);
        } else {
            previewImage = null;
        }
    }

    // Bắt đầu chỉnh sửa card
    async function startEditCard(card) {
        editingCard = card;
        activeSection = 'create';
        await populateFormForEdit(card);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    // Hủy chỉnh sửa
    function cancelEdit() {
        editingCard = null;
        resetForm();
    }

    // Mở hộp thoại xác nhận xóa
    function showDeleteConfirmation(card) {
        confirmDeleteCard = card;
    }

    // Đóng hộp thoại xác nhận xóa
    function cancelDelete() {
        confirmDeleteCard = null;
    }

    // Xử lý xóa card
    async function handleDeleteCard() {
        if (!confirmDeleteCard) return;

        isLoading = true;
        try {
            await deleteIntroduce(confirmDeleteCard.id);
            await loadExistingCards();
            showSuccess('Xóa thẻ thành công!');
            confirmDeleteCard = null;
        } catch (error) {
            console.error('Error deleting card:', error);
            showError('Có lỗi xảy ra khi xóa thẻ. Vui lòng thử lại.');
        } finally {
            isLoading = false;
        }
    }

    // Validate form data
    function validateForm() {
        if (isEditMode) {
            // For edit mode, we allow empty fields (they won't be updated)
            return true;
        } else {
            // For create mode, all required fields must be filled
            if (!formData.title || !formData.description || !formData.tag || !formData.url) {
                showError('Vui lòng điền đầy đủ thông tin bắt buộc (tiêu đề, mô tả, thẻ, URL)');
                return false;
            }
        }
        return true;
    }

    // Xử lý submit form unified
    async function handleFormSubmit() {
        if (!validateForm()) return;

        isLoading = true;
        try {
            if (isEditMode) {
                await handleUpdateCard();
            } else {
                await handleCreateCard();
            }
        } finally {
            isLoading = false;
        }
    }

    // Handle create card
    async function handleCreateCard() {
        await createIntroduceWithImage(formData, selectedFile);
        showSuccess('Thêm thẻ mới thành công!');
        await loadExistingCards();
        resetForm();
    }

    // Handle update card
    async function handleUpdateCard() {
        const updateData = {};

        // Only include non-empty fields for update
        if (formData.title.trim()) updateData.title = formData.title;
        if (formData.description.trim()) updateData.description = formData.description;
        if (formData.tag.trim()) updateData.tag = formData.tag;
        if (formData.url.trim()) updateData.url = formData.url;
        updateData.status = formData.status;

        await updateIntroduceWithImage(editingCard.id, updateData, selectedFile);
        showSuccess('Cập nhật thẻ thành công!');
        await loadExistingCards();
        cancelEdit();
    }

    // Helper functions for showing messages
    function showSuccess(message) {
        successMessage = message;
        showSuccessMessage = true;
        setTimeout(() => {
            showSuccessMessage = false;
        }, 3000);
    }

    function showError(message) {
        errorMessage = message;
        showErrorMessage = true;
        setTimeout(() => {
            showErrorMessage = false;
        }, 3000);
    }

    // Get display values for preview
    function getPreviewTitle() {
        if (isEditMode) {
            return formData.title || editingCard.title || 'Tiêu đề thẻ';
        }
        return formData.title || 'Tiêu đề thẻ';
    }

    function getPreviewDescription() {
        if (isEditMode) {
            return formData.description || editingCard.description || 'Mô tả sản phẩm sẽ hiển thị ở đây';
        }
        return formData.description || 'Mô tả sản phẩm sẽ hiển thị ở đây';
    }

    function getPreviewTag() {
        if (isEditMode) {
            return formData.tag || editingCard.tag || 'Thẻ phân loại';
        }
        return formData.tag || 'Thẻ phân loại';
    }
</script>

<div class="admin-container">
    <div class="admin-header" in:fly={{ y: -20, duration: 600, easing: quintOut }}>
        <h1>Quản lý thẻ sản phẩm</h1>
        <p>Tạo và quản lý các thẻ sản phẩm hiển thị trên trang chủ</p>
    </div>

    <!-- Tabs -->
    <div class="admin-tabs" in:fly={{ y: -10, duration: 400, easing: quintOut }}>
        <button
                class="tab-button {activeSection === 'create' ? 'active' : ''}"
                on:click={() => setActiveSection('create')}
        >
            <span class="material-symbols-outlined">{tabButtonIcon}</span>
            <span>{tabButtonText}</span>
        </button>
        <button
                class="tab-button {activeSection === 'manage' ? 'active' : ''}"
                on:click={() => setActiveSection('manage')}
        >
            <span class="material-symbols-outlined">view_list</span>
            <span>Quản lý danh sách</span>
        </button>
    </div>

    <!-- Success Message -->
    {#if showSuccessMessage}
        <div class="message success" in:fly={{ y: -20, duration: 300 }} out:fade>
            <span class="material-symbols-outlined">check_circle</span>
            <span>{successMessage}</span>
        </div>
    {/if}

    <!-- Error Message -->
    {#if showErrorMessage}
        <div class="message error" in:fly={{ y: -20, duration: 300 }} out:fade>
            <span class="material-symbols-outlined">error</span>
            <span>{errorMessage}</span>
        </div>
    {/if}

    <!-- Create/Edit Section -->
    {#if activeSection === 'create'}
        <div class="admin-content">
            <!-- UNIFIED FORM -->
            <div class="card-form" in:fly={{ y: 20, duration: 600, delay: 200, easing: quintOut }}>
                <h2>{formTitle}</h2>

                <form on:submit|preventDefault={handleFormSubmit}>
                    <div class="form-group">
                        <label for="title">
                            Tiêu đề
                            {#if !isEditMode}<span class="required">*</span>{/if}
                            {#if isEditMode}<span class="edit-hint">mới</span>{/if}
                        </label>
                        <input
                                type="text"
                                id="title"
                                bind:value={formData.title}
                                placeholder={isEditMode ? "Nhập tiêu đề mới (bỏ trống nếu không thay đổi)" : "Nhập tiêu đề thẻ"}
                                required={!isEditMode}
                        />
                    </div>

                    <div class="form-group">
                        <label for="description">
                            Mô tả
                            {#if !isEditMode}<span class="required">*</span>{/if}
                            {#if isEditMode}<span class="edit-hint">mới</span>{/if}
                        </label>
                        <textarea
                                id="description"
                                bind:value={formData.description}
                                placeholder={isEditMode ? "Nhập mô tả mới (bỏ trống nếu không thay đổi)" : "Nhập mô tả thẻ"}
                                rows="4"
                                required={!isEditMode}
                        ></textarea>
                    </div>

                    <div class="form-row">
                        <div class="form-group half">
                            <label for="tag">
                                Thẻ
                                {#if !isEditMode}<span class="required">*</span>{/if}
                                {#if isEditMode}<span class="edit-hint">mới</span>{/if}
                            </label>
                            <input
                                    type="text"
                                    id="tag"
                                    bind:value={formData.tag}
                                    placeholder={isEditMode ? "Nhập thẻ phân loại mới (bỏ trống nếu không thay đổi)" : "Nhập thẻ phân loại"}
                                    required={!isEditMode}
                            />
                        </div>

                        <div class="form-group half">
                            <label for="status">Trạng thái</label>
                            <select id="status" bind:value={formData.status} required>
                                <option value="ACTIVE">Hoạt động</option>
                                <option value="INACTIVE">Không hoạt động</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="url">
                            URL
                            {#if !isEditMode}<span class="required">*</span>{/if}
                            {#if isEditMode}<span class="edit-hint">mới</span>{/if}
                        </label>
                        <input
                                type="url"
                                id="url"
                                bind:value={formData.url}
                                placeholder={isEditMode ? "Nhập URL mới (bỏ trống nếu không thay đổi)" : "Nhập đường dẫn đến sản phẩm"}
                                required={!isEditMode}
                        />
                    </div>

                    <div class="form-group">
                        <label for="image">
                            Hình ảnh
                            {#if !isEditMode}<span class="required">*</span>{/if}
                            {#if isEditMode}<span class="edit-hint">(giữ nguyên nếu không chọn)</span>{/if}
                        </label>
                        <div class="file-input-container">
                            <input
                                    type="file"
                                    id="image"
                                    accept="image/*"
                                    on:change={handleFileChange}
                            />
                            <label for="image" class="file-input-button">
                                <span class="material-symbols-outlined">upload_file</span>
                                {isEditMode ? 'Chọn hình ảnh mới' : 'Chọn hình ảnh'}
                            </label>
                            <span class="file-name">
                                {selectedFile ? selectedFile.name : (isEditMode ? 'Không thay đổi hình ảnh' : 'Chưa chọn file')}
                            </span>
                        </div>
                    </div>

                    {#if previewImage}
                        <div class="image-preview">
                            <img src={previewImage} alt="Preview" />
                            {#if isEditMode && !selectedFile}
                                <div class="current-image-label">Hình ảnh hiện tại</div>
                            {/if}
                        </div>
                    {/if}

                    <div class="form-actions">
                        {#if isEditMode}
                            <button type="button" class="cancel-button" on:click={cancelEdit} disabled={isLoading}>
                                <span class="material-symbols-outlined">close</span>
                                <span>Hủy</span>
                            </button>
                        {/if}
                        <button type="submit" class="submit-button" disabled={isLoading}>
                            {#if isLoading}
                                <span class="spinner"></span>
                                <span>Đang xử lý...</span>
                            {:else}
                                <span class="material-symbols-outlined">{submitButtonIcon}</span>
                                <span>{submitButtonText}</span>
                            {/if}
                        </button>
                    </div>
                </form>
            </div>

            <!-- Preview Card -->
            <div class="card-preview" in:fly={{ y: 20, duration: 600, delay: 400, easing: quintOut }}>
                <h2>Xem trước</h2>

                <div class="preview-card" style="--gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
                    <div class="card-image">
                        {#if previewImage}
                            <img src={previewImage} alt="Preview" class="product-image" />
                        {:else}
                            <div class="placeholder-icon">
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
                            <h3 class="product-title">{getPreviewTitle()}</h3>

                            <div class="product-meta">
                                <span class="product-category">{getPreviewTag()}</span>
                                <span class="product-date">
                                    {formatISOToVietnameseDate(new Date().toISOString())}
                                </span>
                            </div>

                            <p class="product-description">{getPreviewDescription()}</p>
                        </div>

                        <div class="product-bottom">
                            <div class="product-status">
                                <span class="status-indicator {formData.status === 'ACTIVE' ? 'active' : 'inactive'}">
                                    {formData.status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động'}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    {:else}
        <!-- Manage Cards Section -->
        <div class="card-management" in:fly={{ y: 20, duration: 400, easing: quintOut }}>
            <div class="card-list-header">
                <h2>Danh sách thẻ</h2>
                <div class="card-list-search">
                    <input
                            type="text"
                            placeholder="Tìm kiếm thẻ..."
                            bind:value={searchCardQuery}
                            on:input={filterCards}
                    />
                    <span class="material-symbols-outlined search-icon">search</span>
                </div>
            </div>

            {#if isCardListLoading}
                <div class="card-list-loading">
                    <span class="spinner"></span>
                    <p>Đang tải danh sách...</p>
                </div>
            {:else if filteredCards.length === 0}
                <div class="card-list-empty">
                    <span class="material-symbols-outlined">inbox</span>
                    <p>Không có thẻ nào{searchCardQuery ? ' phù hợp với tìm kiếm' : ''}</p>
                </div>
            {:else}
                <div class="card-list">
                    {#each filteredCards as card}
                        <div class="card-list-item" in:slide={{ duration: 300 }}>
                            <div class="card-list-content">
                                <div class="card-list-image">
                                    {#if card.imageLoading}
                                        <div class="loading-spinner">
                                            <div class="spinner small"></div>
                                        </div>
                                    {:else if card.image}
                                        <img src={card.image} alt={card.title} />
                                    {:else}
                                        <div class="placeholder-icon">
                                            <span class="material-symbols-outlined">image</span>
                                        </div>
                                    {/if}
                                </div>
                                <div class="card-list-details">
                                    <h3>{card.title}</h3>
                                    <p class="card-description">{card.description}</p>
                                    <div class="card-meta">
                                        {#if card.tag}
                                            <span class="card-tag">{card.tag}</span>
                                        {/if}
                                        <span class="status-indicator small {card.status === 'ACTIVE' ? 'active' : 'inactive'}">
                                            {card.status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động'}
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div class="card-list-actions">
                                <button class="action-button edit" on:click={() => startEditCard(card)}>
                                    <span class="material-symbols-outlined">edit</span>
                                </button>
                                <button class="action-button delete" on:click={() => showDeleteConfirmation(card)}>
                                    <span class="material-symbols-outlined">delete</span>
                                </button>
                            </div>
                        </div>
                    {/each}
                </div>
            {/if}
        </div>
    {/if}

    <!-- Delete Confirmation Modal -->
    {#if confirmDeleteCard}
        <div class="modal-overlay" in:fade={{ duration: 200 }}>
            <div class="modal-container" in:fly={{ y: 20, duration: 300 }}>
                <div class="modal-header">
                    <h3>Xác nhận xóa thẻ</h3>
                    <button class="modal-close" on:click={cancelDelete}>
                        <span class="material-symbols-outlined">close</span>
                    </button>
                </div>
                <div class="modal-content">
                    <p>Bạn có chắc chắn muốn xóa thẻ <strong>"{confirmDeleteCard.title}"</strong>?</p>
                    <p class="warning">Hành động này không thể hoàn tác.</p>
                </div>
                <div class="modal-actions">
                    <button class="modal-button cancel" on:click={cancelDelete} disabled={isLoading}>
                        Hủy
                    </button>
                    <button class="modal-button delete" on:click={handleDeleteCard} disabled={isLoading}>
                        {#if isLoading}
                            <span class="spinner small"></span>
                            <span>Đang xử lý...</span>
                        {:else}
                            <span>Xóa</span>
                        {/if}
                    </button>
                </div>
            </div>
        </div>
    {/if}
</div>

<style>
    .admin-container {
        max-width: 90%;
        margin: 0 auto;
        padding: 40px 20px;
        color: #ffffff;
    }

    .admin-header {
        margin-bottom: 30px;
        text-align: center;
    }

    .admin-header h1 {
        font-size: 2.5rem;
        margin-bottom: 12px;
        color: #ffffff;
        font-weight: 700;
    }

    .admin-header p {
        font-size: 1.1rem;
        color: #aaaaaa;
    }

    .admin-tabs {
        display: flex;
        justify-content: center;
        gap: 20px;
        margin-bottom: 30px;
    }

    .tab-button {
        background: rgba(26, 26, 26, 0.7);
        border: 1px solid #333333;
        color: #aaaaaa;
        padding: 12px 20px;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.3s;
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 0.95rem;
    }

    .tab-button.active {
        background: #0066cc;
        color: white;
        border-color: #0066cc;
        box-shadow: 0 5px 15px rgba(0, 102, 204, 0.3);
    }

    .tab-button:hover:not(.active) {
        background: rgba(46, 46, 46, 0.8);
        color: white;
    }

    .message {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 15px 20px;
        border-radius: 8px;
        margin-bottom: 20px;
        font-weight: 500;
    }

    .message.success {
        background: rgba(34, 197, 94, 0.1);
        color: #22c55e;
        border: 1px solid rgba(34, 197, 94, 0.3);
    }

    .message.error {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
        border: 1px solid rgba(239, 68, 68, 0.3);
    }

    .admin-content {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 40px;
    }

    .card-form {
        background: rgba(26, 26, 26, 0.7);
        backdrop-filter: blur(10px);
        border-radius: 16px;
        padding: 30px;
        border: 1px solid #333333;
    }

    .card-form h2 {
        font-size: 1.5rem;
        margin-bottom: 24px;
        padding-bottom: 12px;
        border-bottom: 1px solid #333333;
        color: #ffffff;
    }

    .edit-note {
        color: #aaaaaa;
        font-size: 0.9rem;
        margin-bottom: 20px;
    }

    .form-group {
        margin-bottom: 20px;
    }

    .form-row {
        display: flex;
        gap: 20px;
        margin-bottom: 20px;
    }

    .form-group.half {
        flex: 1;
    }

    label {
        display: block;
        margin-bottom: 8px;
        font-size: 0.9rem;
        color: #cccccc;
    }

    .required {
        color: #ef4444;
    }

    .edit-hint {
        color: #888888;
        font-size: 0.8rem;
        font-style: italic;
    }

    input[type="text"],
    input[type="url"],
    textarea,
    select {
        width: 100%;
        padding: 12px 16px;
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid #333333;
        color: #ffffff;
        border-radius: 8px;
        font-size: 0.9rem;
        transition: all 0.3s;
    }

    input[type="text"]:focus,
    input[type="url"]:focus,
    textarea:focus,
    select:focus {
        border-color: #0066cc;
        background: rgba(255, 255, 255, 0.1);
        outline: none;
    }

    textarea {
        resize: vertical;
        min-height: 100px;
    }

    input::placeholder,
    textarea::placeholder {
        color: #666666;
    }

    .file-input-container {
        display: flex;
        align-items: center;
        gap: 10px;
        flex-wrap: wrap;
    }

    input[type="file"] {
        display: none;
    }

    .file-input-button {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        padding: 10px 16px;
        background: #0066cc;
        color: white;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.3s;
    }

    .file-input-button:hover {
        background: #0055aa;
    }

    .file-name {
        color: #aaaaaa;
        font-size: 0.85rem;
        word-break: break-all;
    }

    .image-preview {
        margin-bottom: 20px;
        border-radius: 8px;
        overflow: hidden;
        border: 1px solid #333333;
        max-height: 200px;
        position: relative;
    }

    .image-preview img {
        width: 100%;
        height: auto;
        object-fit: cover;
        display: block;
    }

    .current-image-label {
        position: absolute;
        bottom: 8px;
        right: 8px;
        background: rgba(0, 0, 0, 0.6);
        color: #ffffff;
        padding: 4px 8px;
        border-radius: 4px;
        font-size: 0.8rem;
    }

    .form-actions {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
    }

    .submit-button,
    .cancel-button {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        padding: 12px 24px;
        border: none;
        border-radius: 8px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s;
        font-size: 1rem;
    }

    .submit-button {
        background: white;
        color: black;
    }

    .cancel-button {
        background: rgba(255, 255, 255, 0.1);
        color: #ffffff;
    }

    .submit-button:hover,
    .cancel-button:hover {
        transform: translateY(-2px);
    }

    .submit-button:hover {
        box-shadow: 0 8px 20px rgba(255, 255, 255, 0.2);
    }

    .cancel-button:hover {
        background: rgba(255, 255, 255, 0.2);
    }

    .submit-button:disabled,
    .cancel-button:disabled {
        opacity: 0.7;
        cursor: not-allowed;
        transform: none;
        box-shadow: none;
    }

    .spinner {
        width: 18px;
        height: 18px;
        border: 2px solid rgba(0, 0, 0, 0.3);
        border-top: 2px solid #000000;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }

    .spinner.small {
        width: 14px;
        height: 14px;
        border-width: 1.5px;
    }

    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }

    .card-preview {
        display: flex;
        flex-direction: column;
    }

    .card-preview h2 {
        font-size: 1.5rem;
        margin-bottom: 24px;
        padding-bottom: 12px;
        border-bottom: 1px solid #333333;
        color: #ffffff;
    }

    .preview-card {
        background: #1a1a1a;
        border-radius: 16px;
        overflow: hidden;
        border: 1px solid #333333;
        display: flex;
        flex-direction: column;
        height: fit-content;
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

    .product-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .placeholder-icon {
        color: rgba(255, 255, 255, 0.7);
    }

    .card-content {
        flex: 1;
        padding: 24px;
        display: flex;
        flex-direction: column;
    }

    .product-title {
        font-size: 1.25rem;
        font-weight: 600;
        color: #ffffff;
        margin-bottom: 8px;
        line-height: 1.4;
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
    }

    .product-date {
        color: #666666;
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
        color: #1a1a1a;
    }

    .status-indicator.small {
        padding: 4px 10px;
        font-size: 0.7rem;
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

    .card-management {
        background: rgba(26, 26, 26, 0.7);
        backdrop-filter: blur(10px);
        border-radius: 16px;
        padding: 30px;
        border: 1px solid #333333;
        margin-bottom: 40px;
    }

    .card-list-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 24px;
        padding-bottom: 12px;
        border-bottom: 1px solid #333333;
    }

    .card-list-header h2 {
        font-size: 1.5rem;
        color: #ffffff;
        margin: 0;
    }

    .card-list-search {
        position: relative;
        width: 300px;
    }

    .card-list-search input {
        width: 100%;
        padding: 10px 40px 10px 16px;
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid #333333;
        color: #ffffff;
        border-radius: 20px;
        font-size: 0.9rem;
        transition: all 0.3s;
    }

    .card-list-search .search-icon {
        position: absolute;
        right: 12px;
        top: 50%;
        transform: translateY(-50%);
        color: #666666;
        pointer-events: none;
    }

    .card-list-search input:focus {
        border-color: #0066cc;
        background: rgba(255, 255, 255, 0.1);
        outline: none;
    }

    .card-list-loading,
    .card-list-empty {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 60px 0;
        color: #aaaaaa;
        text-align: center;
    }

    .card-list-loading .spinner {
        border-color: rgba(255, 255, 255, 0.2);
        border-top-color: #ffffff;
        margin-bottom: 20px;
        width: 30px;
        height: 30px;
    }

    .card-list-empty .material-symbols-outlined {
        font-size: 48px;
        margin-bottom: 16px;
        opacity: 0.5;
    }

    .card-list {
        display: flex;
        flex-direction: column;
        gap: 16px;
    }

    .card-list-item {
        background: rgba(46, 46, 46, 0.4);
        border: 1px solid #333333;
        border-radius: 12px;
        overflow: hidden;
        display: flex;
        align-items: stretch;
        transition: all 0.3s;
    }

    .card-list-item:hover {
        transform: translateY(-2px);
        box-shadow: 0 5px 20px rgba(0, 0, 0, 0.3);
        background: rgba(46, 46, 46, 0.7);
    }

    .card-list-content {
        flex: 1;
        display: flex;
        padding: 16px;
        gap: 20px;
    }

    .card-list-image {
        width: 80px;
        height: 80px;
        border-radius: 8px;
        overflow: hidden;
        flex-shrink: 0;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .card-list-image img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .card-list-image .placeholder-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
    }

    .card-list-image .material-symbols-outlined {
        font-size: 32px;
        color: rgba(255, 255, 255, 0.7);
    }

    .card-list-details {
        flex: 1;
        min-width: 0;
    }

    .card-list-details h3 {
        font-size: 1.1rem;
        margin: 0 0 8px 0;
        font-weight: 600;
        color: #ffffff;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .card-description {
        color: #aaaaaa;
        font-size: 0.9rem;
        margin: 0 0 12px 0;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        line-height: 1.5;
    }

    .card-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        flex-wrap: wrap;
    }

    .card-tag {
        color: #888888;
        font-size: 0.8rem;
        background: rgba(255, 255, 255, 0.05);
        padding: 4px 10px;
        border-radius: 4px;
    }

    .card-list-actions {
        display: flex;
        flex-direction: column;
        border-left: 1px solid #333333;
    }

    .action-button {
        display: flex;
        align-items: center;
        justify-content: center;
        background: none;
        border: none;
        padding: 16px;
        cursor: pointer;
        transition: all 0.3s;
    }

    .action-button.edit {
        color: #0066cc;
    }

    .action-button.delete {
        color: #ef4444;
        border-top: 1px solid #333333;
    }

    .action-button:hover {
        background: rgba(255, 255, 255, 0.05);
    }

    .action-button .material-symbols-outlined {
        font-size: 20px;
    }

    .modal-overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.7);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;
        backdrop-filter: blur(5px);
    }

    .modal-container {
        background: #1a1a1a;
        border-radius: 12px;
        width: 90%;
        max-width: 500px;
        border: 1px solid #333333;
        box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
        overflow: hidden;
    }

    .modal-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20px;
        border-bottom: 1px solid #333333;
    }

    .modal-header h3 {
        margin: 0;
        font-size: 1.2rem;
        color: #ffffff;
    }

    .modal-close {
        background: none;
        border: none;
        color: #aaaaaa;
        cursor: pointer;
        padding: 5px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s;
    }

    .modal-close:hover {
        background: rgba(255, 255, 255, 0.1);
        color: #ffffff;
    }

    .modal-content {
        padding: 20px;
    }

    .modal-content p {
        margin: 0 0 16px 0;
        color: #cccccc;
        line-height: 1.6;
    }

    .modal-content p.warning {
        color: #ef4444;
        font-size: 0.9rem;
    }

    .modal-actions {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
        padding: 0 20px 20px;
    }

    .modal-button {
        padding: 10px 20px;
        border-radius: 8px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s;
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .modal-button.cancel {
        background: transparent;
        border: 1px solid #444444;
        color: #cccccc;
    }

    .modal-button.delete {
        background: #ef4444;
        border: 1px solid #ef4444;
        color: white;
    }

    .modal-button.cancel:hover {
        background: rgba(255, 255, 255, 0.05);
    }

    .modal-button.delete:hover {
        background: #e03131;
    }

    .modal-button:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    @media (max-width: 1024px) {
        .admin-content {
            grid-template-columns: 1fr;
            gap: 30px;
        }

        .card-list-header {
            flex-direction: column;
            align-items: flex-start;
            gap: 16px;
        }

        .card-list-search {
            width: 100%;
        }
    }

    @media (max-width: 768px) {
        .admin-container {
            padding: 20px 12px;
        }

        .admin-header h1 {
            font-size: 2rem;
        }

        .admin-tabs {
            flex-direction: column;
            gap: 10px;
        }

        .form-row {
            flex-direction: column;
            gap: 20px;
        }

        .card-list-content {
            flex-direction: column;
            align-items: flex-start;
        }

        .card-list-image {
            width: 100%;
            height: 120px;
        }
    }

    @media (max-width: 480px) {
        .admin-header h1 {
            font-size: 1.8rem;
        }

        .admin-header p {
            font-size: 1rem;
        }

        .card-form,
        .card-preview,
        .card-management {
            padding: 20px;
        }

        .card-list-item {
            flex-direction: column;
        }

        .card-list-actions {
            flex-direction: row;
            border-left: none;
            border-top: 1px solid #333333;
        }

        .action-button {
            flex: 1;
        }

        .action-button.delete {
            border-top: none;
            border-left: 1px solid #333333;
        }
    }
</style>