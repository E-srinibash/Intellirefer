import React from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import Button from '../ui/Button';

const Navbar = () => {
    // useAuth hook provides all the necessary authentication state and actions.
    const { isAuthenticated, userRole, logout } = useAuth();
    const navigate = useNavigate();

    // Handles the logout action.
    const handleLogout = () => {
        logout(); // Clears the token and user role from the global store.
        navigate('/auth'); // Redirects the user to the login page.
    };

    // Determines the correct dashboard link based on the user's role.
    const getDashboardLink = () => {
        if (userRole === 'MANAGER') return '/manager/dashboard';
        if (userRole === 'EMPLOYEE') return '/employee/dashboard';
        return '/'; // Fallback link
    };

    // Style for an active NavLink to give visual feedback to the user.
    const activeLinkStyle = {
        color: '#2563EB', // A blue color for the active link
        textDecoration: 'underline',
    };

    return (
        <nav className="bg-white shadow-md sticky top-0 z-30">
            <div className="container mx-auto px-4 sm:px-6 py-3 flex justify-between items-center">
                
                {/* Application Logo/Brand Name */}
                <Link to={isAuthenticated ? getDashboardLink() : '/'} className="text-2xl font-bold text-gray-800 hover:text-blue-600 transition-colors">
                    IntelliRefer
                </Link>

                {/* Navigation Links and Actions */}
                <div>
                    {isAuthenticated ? (
                        // Links to show when the user IS authenticated
                        <div className="flex items-center gap-4 sm:gap-6">
                            <NavLink 
                                to={getDashboardLink()} 
                                className="text-gray-600 hover:text-blue-600 font-medium"
                                style={({ isActive }) => isActive ? activeLinkStyle : undefined}
                            >
                                Dashboard
                            </NavLink>
                            
                            {/* --- CONDITIONAL LINK FOR MANAGERS --- */}
                            {userRole === 'MANAGER' && (
                                <NavLink 
                                    to="/manager/selected" 
                                    className="text-gray-600 hover:text-blue-600 font-medium"
                                    style={({ isActive }) => isActive ? activeLinkStyle : undefined}
                                >
                                    Selected Candidates
                                </NavLink>
                            )}
                            
                            <Button onClick={handleLogout} variant="secondary" className="!py-1.5 !px-4">
                                Logout
                            </Button>
                        </div>
                    ) : (
                        // Link to show when the user IS NOT authenticated
                        <Link to="/auth">
                            <Button>Login / Register</Button>
                        </Link>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;