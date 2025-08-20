
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL;
const AUTH_API_URL = `${API_URL}/api/v1/auth`;


const ACCESS_TOKEN_KEY = 'access_token';
const USER_INFO_KEY = 'user_info';


let isAuthenticated = false;
let userInfo = null;
let accessToken = null;
let tokenCheckInterval = null;


function decodeJWT(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        return JSON.parse(jsonPayload);
    } catch (error) {
        console.error('Error decoding JWT:', error);
        return null;
    }
}


function isTokenExpired(token) {
    if (!token) return true;

    const decodedToken = decodeJWT(token);
    if (!decodedToken || !decodedToken.exp) return true;

    const currentTime = Math.floor(Date.now() / 1000);
    const bufferTime = 60;

    return (decodedToken.exp - bufferTime) <= currentTime;
}

// Show token expired notification
function showTokenExpiredNotification() {
    // Create notification element
    const notification = document.createElement('div');
    notification.innerHTML = `
        <div style="
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.7);
            backdrop-filter: blur(10px);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10000;
        ">
            <div style="
                background: white;
                padding: 30px;
                border-radius: 12px;
                box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
                max-width: 400px;
                text-align: center;
                animation: slideIn 0.3s ease-out;
            ">
                <div style="
                    width: 60px;
                    height: 60px;
                    background: #ff3b30;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin: 0 auto 20px;
                ">
                    <span style="color: white; font-size: 24px;">⚠</span>
                </div>
                <h3 style="margin: 0 0 15px; color: #1d1d1f; font-size: 20px;">Phiên đăng nhập đã hết hạn</h3>
                <p style="margin: 0 0 25px; color: #86868b; font-size: 14px; line-height: 1.4;">
                    Vui lòng đăng nhập lại để tiếp tục sử dụng.
                </p>
                <button id="reloginBtn" style="
                    background: #0066cc;
                    color: white;
                    border: none;
                    padding: 12px 24px;
                    border-radius: 8px;
                    font-size: 14px;
                    font-weight: 500;
                    cursor: pointer;
                    transition: background 0.3s;
                    width: 100%;
                ">
                    Đăng nhập lại
                </button>
            </div>
        </div>
        <style>
            @keyframes slideIn {
                from {
                    opacity: 0;
                    transform: scale(0.9) translateY(-20px);
                }
                to {
                    opacity: 1;
                    transform: scale(1) translateY(0);
                }
            }
        </style>
    `;

    document.body.appendChild(notification);

    document.getElementById('reloginBtn').addEventListener('click', () => {
        document.body.removeChild(notification);
        logout();
    });
}

function startTokenMonitoring() {
    if (tokenCheckInterval) {
        clearInterval(tokenCheckInterval);
    }

    tokenCheckInterval = setInterval(() => {
        if (isAuthenticated && accessToken) {
            if (isTokenExpired(accessToken)) {
                console.log('Token expired, logging out...');
                showTokenExpiredNotification();
                stopTokenMonitoring();
            }
        }
    }, 30000);
}

// Stop token monitoring
function stopTokenMonitoring() {
    if (tokenCheckInterval) {
        clearInterval(tokenCheckInterval);
        tokenCheckInterval = null;
    }
}

export function initAuth() {
    try {
        accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
        const storedUserInfo = localStorage.getItem(USER_INFO_KEY);

        if (accessToken) {
            if (isTokenExpired(accessToken)) {
                console.log('Stored token is expired, clearing auth data');
                clearAuthData();
                return Promise.resolve(false);
            }

            const decodedToken = decodeJWT(accessToken);
            if (decodedToken) {
                userInfo = {
                    ...JSON.parse(storedUserInfo || '{}'),
                    roles: decodedToken.realm_access?.roles || []
                };
                isAuthenticated = true;

                axios.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;

                startTokenMonitoring();

                return Promise.resolve(true);
            }
        }

        return Promise.resolve(false);
    } catch (error) {
        console.error('Error initializing auth:', error);
        clearAuthData();
        return Promise.resolve(false);
    }
}

// Login function
export async function login(credentials) {
    try {
        const response = await axios.post(`${AUTH_API_URL}/login`, credentials);

        if (response.data) {
            const { accessToken: newAccessToken, user } = response.data;

            if (isTokenExpired(newAccessToken)) {
                throw new Error('Received expired token from server');
            }

            const decodedToken = decodeJWT(newAccessToken);
            const roles = decodedToken?.realm_access?.roles || [];

            accessToken = newAccessToken;
            userInfo = {
                ...user,
                roles
            };
            isAuthenticated = true;

            localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
            localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo));

            axios.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;

            startTokenMonitoring();

            return {
                success: true,
                user: userInfo,
                accessToken
            };
        }

        throw new Error('Invalid response from server');
    } catch (error) {
        console.error('Login error:', error);
        throw new Error(error.response?.data?.message || 'Đăng nhập thất bại');
    }
}

export async function logout() {
    try {
        stopTokenMonitoring();

        if (accessToken) {
            if (!isTokenExpired(accessToken)) {
                await axios.post(`${AUTH_API_URL}/logout`, {}, {
                    headers: {
                        'Authorization': `Bearer ${accessToken}`,
                        'Content-Type': 'application/json'
                    }
                });
            }
        }
    } catch (error) {
        console.error('Logout API error:', error);
    } finally {
        clearAuthData();

        localStorage.removeItem('vnpt_user');

        window.location.href = '/login?t=' + Date.now();
    }
}

function clearAuthData() {
    stopTokenMonitoring();

    isAuthenticated = false;
    userInfo = null;
    accessToken = null;

    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(USER_INFO_KEY);

    delete axios.defaults.headers.common['Authorization'];

    axios.defaults.headers.common = {};
}

export function getAuthState() {
    return {
        isAuthenticated,
        userInfo,
        accessToken
    };
}

export function hasRole(roleName) {
    if (!isAuthenticated || !userInfo || !userInfo.roles) {
        return false;
    }

    return userInfo.roles.includes(roleName);
}

export function getCurrentUser() {
    return userInfo;
}

export function isUserAuthenticated() {
    return isAuthenticated && accessToken && !isTokenExpired(accessToken);
}

axios.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401) {
            console.log('Received 401, token might be expired');

            if (isAuthenticated && accessToken && isTokenExpired(accessToken)) {
                console.log('Token is expired, showing notification');
                showTokenExpiredNotification();
                return Promise.reject(error);
            }

            clearAuthData();
            window.location.href = '/login';
        }

        return Promise.reject(error);
    }
);

export function validateToken() {
    if (!isAuthenticated || !accessToken) {
        return false;
    }

    if (isTokenExpired(accessToken)) {
        showTokenExpiredNotification();
        return false;
    }

    return true;
}

export default {
    initAuth,
    login,
    logout,
    getAuthState,
    hasRole,
    getCurrentUser,
    isUserAuthenticated,
    validateToken,
    get token() {
        return accessToken;
    },
    get authenticated() {
        return isAuthenticated && accessToken && !isTokenExpired(accessToken);
    }
};