import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useAuthStore = create(
    persist(
        (set) => ({
            token: null,
            userRole: null, // This will store "MANAGER" or "EMPLOYEE"
            setAuth: (token, role) => {
                set({ token, userRole: role });
            },
            logout: () => set({ token: null, userRole: null }),
        }),
        {
            name: 'intellirefer-auth-storage',
        }
    )
);