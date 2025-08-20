import axios from 'axios';
import auth from '../config/auth-config.js';

export const API_URL = import.meta.env.VITE_API_URL;
export const API_BASE_URL_1 = `${API_URL}/api/v1/introduce`;
export const API_BASE_URL_2 = `${API_URL}/api/v1/image`;
export const API_BASE_URL_3 = `${API_URL}/api/v1/admin/introduce`;

export async function getAllIntroduces() {
    const response = await axios.get(`${API_BASE_URL_1}/all`);
    return response.data;
}

export async function getImageById(id) {
    const response = await axios.get(`${API_BASE_URL_2}/get-image-by-id/${id}`);
    return response.data;
}

export async function searchIntroduces(searchQuery) {
    try {
        const allIntroduces = await getAllIntroduces();
        return allIntroduces.filter(item =>
            item.title && item.title.toLowerCase().includes(searchQuery.toLowerCase())
        );
    } catch (error) {
        console.error('Error searching introduces:', error);
        return [];
    }
}

function getAuthHeaders() {
    // Validate token before making request
    if (!auth.validateToken()) {
        throw new Error('Token expired or invalid');
    }

    const { accessToken } = auth.getAuthState();
    return accessToken ? { Authorization: `Bearer ${accessToken}` } : {};
}

export async function createIntroduceWithImage(data, imageFile) {
    try {
        // Validate token before making request
        if (!auth.validateToken()) {
            throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
        }

        const formData = new FormData();
        formData.append('title', data.title);
        formData.append('description', data.description);
        formData.append('tag', data.tag || '');
        formData.append('url', data.url);
        formData.append('status', data.status);

        if (imageFile) {
            formData.append('file', imageFile);
        }

        console.log('Form data being sent:');
        for (let [key, value] of formData.entries()) {
            console.log(`${key}: ${value instanceof File ? value.name : value}`);
        }

        const headers = getAuthHeaders();

        const response = await axios.post(`${API_BASE_URL_3}/create`, formData, {
            headers,
        });

        return response.data;
    } catch (error) {
        console.error('Error creating card with image:', error.response?.data || error.message);

        // If error is related to authentication, don't throw - let the interceptor handle it
        if (error.response?.status === 401) {
            throw error;
        }

        throw error;
    }
}

export async function updateIntroduceWithImage(id, data, imageFile) {
    try {
        // Validate token before making request
        if (!auth.validateToken()) {
            throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
        }

        const formData = new FormData();
        formData.append('id', id);
        if (data.title) formData.append('title', data.title);
        if (data.description) formData.append('description', data.description);
        if (data.tag) formData.append('tag', data.tag);
        if (data.url) formData.append('url', data.url);
        formData.append('status', data.status);
        if (imageFile) {
            formData.append('file', imageFile);
        }

        for (let [key, value] of formData.entries()) {
            console.log(`${key}: ${value instanceof File ? value.name : value}`);
        }

        const headers = getAuthHeaders();

        const response = await axios.put(`${API_BASE_URL_3}/update`, formData, {
            headers,
            transformRequest: [function (data) {
                return data;
            }]
        });

        return response.data;
    } catch (error) {
        console.error('Error updating card with image:', error.response?.data || error.message);

        // If error is related to authentication, don't throw - let the interceptor handle it
        if (error.response?.status === 401) {
            throw error;
        }

        throw error;
    }
}

export async function updateIntroduceStatus(id, status) {
    try {
        // Validate token before making request
        if (!auth.validateToken()) {
            throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
        }

        const headers = getAuthHeaders();
        const response = await axios.put(
            `${API_BASE_URL_3}/update-status/${encodeURIComponent(id)}?status=${status}`,
            null,
            { headers }
        );
        return response.data;
    } catch (error) {
        console.error('Error updating status:', error.response?.data || error.message);

        // If error is related to authentication, don't throw - let the interceptor handle it
        if (error.response?.status === 401) {
            throw error;
        }

        throw error;
    }
}

export async function deleteIntroduce(id) {
    try {
        // Validate token before making request
        if (!auth.validateToken()) {
            throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
        }

        const headers = getAuthHeaders();
        const response = await axios.delete(`${API_BASE_URL_3}/delete/${encodeURIComponent(id)}`, { headers });
        return response.data;
    } catch (error) {
        console.error('Error deleting card:', error.response?.data || error.message);

        // If error is related to authentication, don't throw - let the interceptor handle it
        if (error.response?.status === 401) {
            throw error;
        }

        throw error;
    }
}