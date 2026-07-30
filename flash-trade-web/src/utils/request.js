import axios from 'axios';
const instance = axios.create({
    baseURL: '/QuickTrade/web',
    timeout: 5000
});
// request 拦截
instance.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers = config.headers || {};
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
// response 拦截（解包 ApiResponse）
instance.interceptors.response.use(response => {
    const responseData = response.data;
    if (responseData && typeof responseData === 'object' && 'success' in responseData) {
        if (responseData.success) {
            return responseData.data;
        }
        return Promise.reject(responseData.error || responseData);
    }
    return responseData;
}, error => {
    const res = error.response?.data;
    if (error.response?.status === 401) {
        localStorage.removeItem('token');
        window.location.href = '/login';
    }
    return Promise.reject(res ?? error);
});
const request = instance;
export default request;
//# sourceMappingURL=request.js.map