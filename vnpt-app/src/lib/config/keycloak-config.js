import Keycloak from 'keycloak-js';

export const keycloak = new Keycloak({
    url: import.meta.env.VITE_KEYCLOAK_URL,
    realm: import.meta.env.VITE_KEYCLOAK_REALM,
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
});

// Hàm khởi tạo Keycloak
export const initKeycloak = async () => {
    try {
        const authenticated = await keycloak.init({
            onLoad: 'check-sso',
            silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
            pkceMethod: 'S256'
        });
        return authenticated;
    } catch (error) {
        console.error('Keycloak initialization failed:', error);
        return false;
    }
};

// Hàm đăng nhập
export const login = () => {
    keycloak.login({
        redirectUri: window.location.origin
    });
};

// Hàm đăng xuất
export const logout = () => {
    keycloak.logout({
        redirectUri: window.location.origin
    });
};

// Hàm kiểm tra quyền
export const hasRole = (role) => {
    return keycloak.tokenParsed?.realm_access?.roles?.includes(role);
};

export default keycloak;