import React from 'react';
import { Navigate } from 'react-router-dom'; // Import Navigate
import useAuth from '../hooks/useAuth'; // Import useAuth
import LoginForm from '../components/auth/LoginForm';
import RegisterForm from '../components/auth/RegisterForm';
import Card from '../components/ui/Card';
import { useState } from 'react';

const AuthPage = () => {
    const [isLoginView, setIsLoginView] = useState(true);
    const { isAuthenticated, userRole } = useAuth(); // Get auth state

    // THIS IS THE GUARD:
    // If the user is already authenticated, redirect them to their dashboard.
    if (isAuthenticated) {
        const dashboardPath = userRole === 'MANAGER' ? '/manager/dashboard' : '/employee/dashboard';
        return <Navigate to={dashboardPath} replace />;
    }

    const handleRegisterSuccess = () => {
        alert('Registration successful! Please proceed to log in.');
        setIsLoginView(true);
    };

    // This component will now only render if the user is NOT authenticated.
    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center">
            <Card className="w-full max-w-md">
                <h2 className="text-2xl font-bold text-center mb-6">
                    {isLoginView ? 'Welcome Back!' : 'Create Your Account'}
                </h2>
                {isLoginView ? (
                    <LoginForm />
                ) : (
                    <RegisterForm onRegisterSuccess={handleRegisterSuccess} />
                )}
                <div className="mt-6 text-center">
                    <button onClick={() => setIsLoginView(!isLoginView)} className="text-sm text-blue-500 hover:underline">
                        {isLoginView ? "Don't have an account? Register" : "Already have an account? Login"}
                    </button>
                </div>
            </Card>
        </div>
    );
};

export default AuthPage;