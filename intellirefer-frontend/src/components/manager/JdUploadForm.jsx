import React, { useState } from 'react';
import apiService from '../../api/apiService';
import Button from '../ui/Button';
import Spinner from '../ui/Spinner';

// This component receives a prop to notify the parent on success
const JdUploadForm = ({ onUploadSuccess }) => {
    const [title, setTitle] = useState('');
    const [clientName, setClientName] = useState('');
    const [file, setFile] = useState(null);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!title || !clientName || !file) {
            setError('All fields are required.');
            return;
        }

        setLoading(true);
        setError('');

        const formData = new FormData();
        formData.append('title', title);
        formData.append('clientName', clientName);
        formData.append('file', file);

        try {
            const response = await apiService.post('/manager/jds', formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
            });
            // On success, call the callback function passed from the parent
            onUploadSuccess(response.data); 
        } catch (err) {
            setError(err.response?.data?.message || 'Upload failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div>
                <label htmlFor="title" className="block text-sm font-medium text-gray-700">Job Title</label>
                <input type="text" id="title" value={title} onChange={(e) => setTitle(e.target.value)} required
                    className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"/>
            </div>
            <div>
                <label htmlFor="clientName" className="block text-sm font-medium text-gray-700">Client Name</label>
                <input type="text" id="clientName" value={clientName} onChange={(e) => setClientName(e.target.value)} required
                    className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"/>
            </div>
            <div>
                <label htmlFor="file" className="block text-sm font-medium text-gray-700">Job Description File</label>
                <input type="file" id="file" onChange={handleFileChange} required accept=".pdf,.docx"
                    className="mt-1 block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"/>
            </div>
            {error && <p className="text-sm text-red-600">{error}</p>}
            <div className="flex justify-end pt-4">
                <Button type="submit" disabled={loading}>
                    {loading ? <Spinner /> : 'Upload JD'}
                </Button>
            </div>
        </form>
    );
};

export default JdUploadForm;