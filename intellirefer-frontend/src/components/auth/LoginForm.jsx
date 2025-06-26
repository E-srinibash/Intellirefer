import React, { useState } from 'react';
import useAuth from '../../hooks/useAuth';
import apiService from '../../api/apiService';
import { useNavigate } from 'react-router-dom';
import Button from '../ui/Button';

const LoginForm = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { setAuth } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            const response = await apiService.post('/auth/login', { email, password });
            const { accessToken, role } = response.data;

            // Update global state
            setAuth(accessToken, role);

            // This condition now works correctly for both roles
            const dashboardPath = role === 'MANAGER' ? '/manager/dashboard' : '/employee/dashboard';
            navigate(dashboardPath, { replace: true });

        } catch (err) {
            setError(err.response?.data?.message || 'Login failed. Please check your credentials.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-6">
            <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700">Email</label>
                <input type="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)} required
                    className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"/>
            </div>
            <div>
                <label htmlFor="password" className="block text-sm font-medium text-gray-700">Password</label>
                <input type="password" id="password" value={password} onChange={(e) => setPassword(e.target.value)} required
                    className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"/>
            </div>
            {error && <p className="text-sm text-red-600">{error}</p>}
            <div>
                <Button type="submit" disabled={loading} className="w-full">
                    {loading ? 'Logging in...' : 'Login'}
                </Button>
            </div>
        </form>
    );
};

export default LoginForm;