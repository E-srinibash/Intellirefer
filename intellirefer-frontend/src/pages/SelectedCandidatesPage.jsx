import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import apiService from '../api/apiService';
import Spinner from '../components/ui/Spinner';
import Card from '../components/ui/Card';

const SelectedCandidatesPage = () => {
    const [employees, setEmployees] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        setLoading(true);
        apiService.get('/manager/selected-employees')
            .then(response => {
                setEmployees(response.data);
            })
            .catch(err => {
                console.error("Failed to fetch selected employees:", err);
                setError('Could not load data. Please try again later.');
            })
            .finally(() => {
                setLoading(false);
            });
    }, []);

    if (loading) return <div className="flex justify-center items-center h-screen"><Spinner /></div>;
    if (error) return <div className="p-8 text-center text-red-500">{error}</div>;

    return (
        <div className="container mx-auto p-4 md:p-8">
            <div className="mb-6">
                <Link to="/manager/dashboard" className="text-blue-600 hover:underline">← Back to Dashboard</Link>
                <h1 className="text-3xl font-bold text-gray-800 mt-2">Selected & Reserved Candidates</h1>
                <p className="text-md text-gray-500">This list shows all employees currently assigned to or on hold for a project.</p>
            </div>

            <Card>
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Employee Name</th>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Assigned Project / Client</th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {employees.length > 0 ? employees.map((emp) => (
                                <tr key={emp.employeeUserId}>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="text-sm font-medium text-gray-900">{emp.employeeFullName}</div>
                                        <div className="text-sm text-gray-500">{emp.employeeEmail}</div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                            emp.availability === 'ON_PROJECT' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
                                        }`}>
                                            {emp.availability.replace('_', ' ')}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-800">
                                        {emp.jobTitle ? (
                                            <>
                                                <span className="font-semibold">{emp.jobTitle}</span>
                                                <span className="text-gray-500"> for {emp.clientName}</span>
                                            </>
                                        ) : (
                                            <span className="italic text-gray-400">Project info not available</span>
                                        )}
                                    </td>
                                </tr>
                            )) : (
                                <tr>
                                    <td colSpan="3" className="px-6 py-4 text-center text-gray-500">No employees are currently selected or reserved.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </Card>
        </div>
    );
};

export default SelectedCandidatesPage;