import { useAuthStore } from '../store/authStore';

const useAuth = () => {
    const { token, userRole, setAuth, logout } = useAuthStore();
    const isAuthenticated = !!token;

    return { isAuthenticated, userRole, setAuth, logout };
};

export default useAuth;