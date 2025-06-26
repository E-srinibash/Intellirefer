import React, { useState } from 'react';
import apiService from '../../api/apiService';
import Button from '../ui/Button';

// The onRegisterSuccess prop is used to tell the parent (AuthPage) to switch views
const RegisterForm = ({ onRegisterSuccess }) => {
    const [formData, setFormData] = useState({ fullName: '', email: '', password: '', yearsOfExperience: 0 });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            // The API call now returns a simple success message, not a token.
            await apiService.post('/auth/register', formData);
            
            // Call the success handler passed from the parent component.
            onRegisterSuccess(); 
        } catch (err) {
            setError(err.response?.data?.message || 'Registration failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            {/* Input fields for fullName, email, password, yearsOfExperience... */}
            <div>
                <label className="block text-sm font-medium text-gray-700">Full Name</label>
                <input type="text" name="fullName" onChange={handleChange} required className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"/>
            </div>
            <div>
                <label className="block text-sm font-medium text-gray-700">Email</label>
                <input type="email" name="email" onChange={handleChange} required className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"/>
            </div>
            <div>
                <label className="block text-sm font-medium text-gray-700">Password</label>
                <input type="password" name="password" onChange={handleChange} required className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"/>
            </div>
            <div>
                <label className="block text-sm font-medium text-gray-700">Years of Experience</label>
                <input type="number" name="yearsOfExperience" onChange={handleChange} required min="0" className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"/>
            </div>
            
            {error && <p className="text-sm text-red-600">{error}</p>}
            <div>
                <Button type="submit" disabled={loading} className="w-full">
                    {loading ? 'Registering...' : 'Register'}
                </Button>
            </div>
        </form>
    );
};

export default RegisterForm;