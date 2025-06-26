import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';

const ProtectedRoute = ({ allowedRoles }) => {
    const { isAuthenticated, userRole } = useAuth();

    if (!isAuthenticated) {
        // Redirect to login page if not authenticated
        return <Navigate to="/auth" replace />;
    }

    if (allowedRoles && !allowedRoles.includes(userRole)) {
        // Redirect if the user role is not permitted
        return <Navigate to="/unauthorized" replace />;
    }

    return <Outlet />; // Render the child component if authenticated and authorized
};

export default ProtectedRoute;