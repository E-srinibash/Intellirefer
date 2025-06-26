import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom'; // Add useLocation
import AuthPage from './pages/AuthPage';
import EmployeeDashboard from './pages/EmployeeDashBoard';
import SelectedCandidatesPage from './pages/SelectedCandidatesPage';
import ManagerDashboard from './pages/ManagerDashboard';
import JdDetailsPage from './pages/JdDetailsPage';
import NotFoundPage from './pages/NotFoundPage';
import ProtectedRoute from './components/layout/ProtectedRoute';
import Navbar from './components/layout/Navbar';
import useAuth from './hooks/useAuth';

// This component contains the logic that needs router hooks
const AppContent = () => {
    const { isAuthenticated, userRole } = useAuth();
    const navigate = useNavigate();
    const location = useLocation(); // <-- Get the current location object

    // This useEffect will now only navigate when necessary
    useEffect(() => {
        if (isAuthenticated) {
            const dashboardPath = userRole === 'MANAGER' ? '/manager/dashboard' : '/employee/dashboard';
            
            // THIS IS THE FIX: Only navigate if we are NOT already on the dashboard or a sub-path of it.
            // This prevents the infinite loop.
            if (location.pathname !== dashboardPath && location.pathname === '/auth') {
                 navigate(dashboardPath, { replace: true });
            }
        }
    }, [isAuthenticated, userRole, navigate, location.pathname]); // Add location.pathname to dependencies

    return (
        <>
            <Navbar />
            <main className="bg-gray-50 min-h-screen">
                <Routes>
                    {/* The root path redirects based on initial auth state */}
                    <Route path="/" element={
                        isAuthenticated ? (
                            <Navigate to={userRole === 'MANAGER' ? '/manager/dashboard' : '/employee/dashboard'} replace />
                        ) : (
                            <Navigate to="/auth" replace />
                        )
                    } />
                    
                    <Route path="/auth" element={<AuthPage />} />

                    {/* Protected Routes */}
                    <Route element={<ProtectedRoute />}>
                        <Route path="/employee/dashboard" element={<EmployeeDashboard />} />
                        <Route path="/manager/dashboard" element={<ManagerDashboard />} />
                        <Route path="/manager/jds/:jdId" element={<JdDetailsPage />} />
                        <Route path="/manager/selected" element={<SelectedCandidatesPage />} />
                    </Route>
                    
                    <Route path="/unauthorized" element={<div>Unauthorized Access</div>} />
                    <Route path="*" element={<NotFoundPage />} />
                </Routes>
            </main>
        </>
    );
}

// The main App component remains the same
function App() {
    return (
        <Router>
            <AppContent />
        </Router>
    );
}

export default App;