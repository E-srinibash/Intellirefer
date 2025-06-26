import axios from 'axios';
import { useAuthStore } from '../store/authStore';

// Centralized Axios instance
const apiService = axios.create({
    // Set this to your Spring Boot backend's URL
    baseURL: 'http://localhost:8080/api', 
});

// Request Interceptor: This runs before every request is sent.
// It automatically attaches the JWT token to the Authorization header.
apiService.interceptors.request.use(
    (config) => {
        const token = useAuthStore.getState().token;
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default apiService;