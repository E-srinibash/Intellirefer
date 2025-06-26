import React, { useEffect, useState } from 'react';
import apiService from '../api/apiService';
import Card from '../components/ui/Card';
import Spinner from '../components/ui/Spinner';
import Button from '../components/ui/Button';
import Modal from '../components/ui/Modal';
import EditProfileForm from '../components/employee/EditProfileForm';

const EmployeeDashboard = () => {
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [resumeFile, setResumeFile] = useState(null);
    const [uploading, setUploading] = useState(false);

    const [isEditModalOpen, setIsEditModalOpen] = useState(false);

    const fetchProfile = () => {
        // We don't set loading to true here on refetch to avoid a jarring UI refresh
        apiService.get('/employee/me')
            .then(response => {
                setProfile(response.data);
            })
            .catch(err => {
                console.error("Failed to fetch profile", err);
                setError('Could not load your profile. Please try again later.');
            })
            .finally(() => {
                setLoading(false);
            });
    };

    // Fetch profile on initial component mount
    useEffect(() => {
        fetchProfile();
    }, []);

    const handleFileChange = (e) => {
        setResumeFile(e.target.files[0]);
    };

    const handleResumeUpload = async (e) => {
        e.preventDefault();
        if (!resumeFile) return;
        setUploading(true);
        const formData = new FormData();
        formData.append('file', resumeFile);

        try {
            const response = await apiService.post('/employee/me/resume', formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
            });
            alert(response.data.message);
            // After successful upload, re-fetch profile to get auto-populated skills.
            // We add a delay to give the backend time to process the async skill extraction.
            setTimeout(fetchProfile, 5000);
        } catch (err) {
            alert("Resume upload failed.");
        } finally {
            setUploading(false);
            // Clear the file input after upload attempt
            e.target.reset();
            setResumeFile(null);
        }
    };

    const handleUpdateProfile = async (payload) => {
        // This function is passed to the EditProfileForm.
        // It returns the promise so the form can handle its own loading/error state.
        return apiService.put('/employee/me', payload)
            .then(response => {
                setProfile(response.data); // Update the dashboard's profile state
                setIsEditModalOpen(false); // Close the modal on success
                alert("Profile updated successfully!");
            });
    };

    if (loading && !profile) return <div className="flex justify-center items-center h-screen"><Spinner /></div>;
    if (error) return <div className="p-8 text-center text-red-500">{error}</div>;

    return (
        <>
            <div className="container mx-auto p-4 md:p-8">
                <h1 className="text-3xl font-bold mb-6">Employee Dashboard</h1>
                {profile ? (
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                        <Card>
                            <div className="flex justify-between items-start mb-4">
                                <h2 className="text-xl font-semibold">Your Profile</h2>
                                <Button onClick={() => setIsEditModalOpen(true)} variant="secondary" className="!py-1 !px-3 text-sm">Edit</Button>
                            </div>
                            <div className="space-y-3">
                                <p><strong>Name:</strong> {profile.fullName}</p>
                                <p><strong>Email:</strong> {profile.email}</p>
                                <p><strong>Current Role:</strong> {profile.currentRole || <span className="text-gray-500 italic">Not specified</span>}</p>
                                <p><strong>Job Level:</strong> {profile.jobLevel || <span className="text-gray-500 italic">Not specified</span>}</p>
                                <p><strong>Experience:</strong> {profile.yearsOfExperience} years</p>
                                <p><strong>Status:</strong>
                                    <span className={`ml-2 px-2 py-0.5 text-xs font-semibold rounded-full ${
                                        profile.availability === 'AVAILABLE' ? 'bg-green-100 text-green-800' :
                                        profile.availability === 'ON_PROJECT' ? 'bg-red-100 text-red-800' :
                                        'bg-yellow-100 text-yellow-800'
                                    }`}>
                                        {profile.availability.replace('_', ' ')}
                                    </span>
                                </p>
                                {profile.availability === 'ON_PROJECT' && profile.expectedAvailabilityDate && (
                                    <p><strong>Available On:</strong> {new Date(profile.expectedAvailabilityDate + 'T00:00:00').toLocaleDateString()}</p>
                                )}
                                <div className="pt-2">
                                    <strong className="block mb-2">Skills:</strong>
                                    <div className="flex flex-wrap gap-2">
                                        {profile.skills && profile.skills.length > 0 ? (
                                            profile.skills.map(skill => <span key={skill} className="bg-gray-200 text-gray-800 text-sm font-medium px-3 py-1 rounded-full">{skill}</span>)
                                        ) : (
                                            <span className="text-sm text-gray-500 italic">No skills listed. Upload your resume to auto-populate.</span>
                                        )}
                                    </div>
                                </div>
                            </div>
                        </Card>
                        <Card>
                            <h2 className="text-xl font-semibold mb-4">Upload or Update Your Resume</h2>
                            <p className="text-sm text-gray-600 mb-4">Uploading a new resume will automatically update your skills list based on its content.</p>
                            <form onSubmit={handleResumeUpload}>
                                <input type="file" onChange={handleFileChange} accept=".pdf,.docx" className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"/>
                                <Button type="submit" disabled={uploading || !resumeFile} className="mt-4">
                                    {uploading ? <Spinner/> : 'Upload Resume'}
                                </Button>
                            </form>
                        </Card>
                    </div>
                ) : <p>Could not load profile.</p>}
            </div>

            <Modal
                isOpen={isEditModalOpen}
                onClose={() => setIsEditModalOpen(false)}
                title="Edit Your Profile"
            >
                <EditProfileForm
                    currentProfile={profile}
                    onUpdateSuccess={handleUpdateProfile}
                    onClose={() => setIsEditModalOpen(false)}
                />
            </Modal>
        </>
    );
};

export default EmployeeDashboard;