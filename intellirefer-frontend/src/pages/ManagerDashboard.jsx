import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import apiService from '../api/apiService';
import Card from '../components/ui/Card';
import Spinner from '../components/ui/Spinner';
import Button from '../components/ui/Button';
import Modal from '../components/ui/Modal';
import JdUploadForm from '../components/manager/JdUploadForm';

const ManagerDashboard = () => {
    const [jds, setJds] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false); // State for the upload modal

    // Fetch the manager's job descriptions on component mount
    useEffect(() => {
        setLoading(true);
        apiService.get('/manager/jds')
            .then(response => {
                setJds(response.data);
            })
            .catch(err => {
                console.error("Failed to fetch job descriptions", err);
                alert("Could not load job descriptions.");
            })
            .finally(() => {
                setLoading(false);
            });
    }, []);

    // Handles the success callback from the JdUploadForm
    const handleUploadSuccess = (newJd) => {
        setJds(prevJds => [newJd, ...prevJds]); // Add new JD to the top of the list
        setIsModalOpen(false); // Close the modal
    };

    // --- NEW FUNCTION TO HANDLE MANUAL JOB CLOSURE ---
    const handleCloseJd = async (jdId) => {
        // Ask for confirmation before proceeding
        if (!window.confirm("Are you sure you want to close this job? This action cannot be undone.")) {
            return;
        }

        const originalJds = [...jds];

        // Optimistic UI Update: Change status immediately for a responsive feel
        setJds(prevJds => 
            prevJds.map(jd => 
                jd.id === jdId ? { ...jd, status: 'CLOSED' } : jd
            )
        );

        try {
            // Call the new backend endpoint
            await apiService.post(`/manager/jds/${jdId}/close`);
        } catch (err) {
            console.error("Failed to close job:", err);
            alert("Failed to close the job. The status has been restored.");
            // On error, revert the UI back to its original state
            setJds(originalJds);
        }
    };

    if (loading) return <div className="flex justify-center items-center h-screen"><Spinner /></div>;

    return (
        <>
            <div className="container mx-auto p-4 md:p-8">
                <div className="flex flex-col md:flex-row justify-between md:items-center mb-6 gap-4">
                    <h1 className="text-3xl font-bold text-gray-800">Manager Dashboard</h1>
                    <Button onClick={() => setIsModalOpen(true)}>
                        Upload New JD
                    </Button>
                </div>

                <div className="space-y-4">
                    <h2 className="text-xl font-semibold text-gray-700">Your Job Descriptions</h2>
                    {jds.length > 0 ? (
                        jds.map(jd => (
                            <Card key={jd.id} className="flex flex-col md:flex-row justify-between md:items-center gap-4 hover:shadow-lg transition-shadow">
                                <div>
                                    <h3 className="font-bold text-lg text-gray-800">{jd.title}</h3>
                                    <p className="text-sm text-gray-600">Client: {jd.clientName}</p>
                                    <span className={`mt-2 inline-block px-3 py-1 text-xs font-bold uppercase rounded-full ${
                                        jd.status === 'OPEN' ? 'bg-green-100 text-green-800' : 'bg-gray-200 text-gray-700'
                                    }`}>
                                        {jd.status}
                                    </span>
                                </div>
                                <div className="flex items-center gap-2 self-start md:self-center">
                                    {/* --- NEW BUTTON: Only shows if the job is OPEN --- */}
                                    {jd.status === 'OPEN' && (
                                        <Button 
                                            onClick={() => handleCloseJd(jd.id)} 
                                            className="!bg-red-500 hover:!bg-red-700 !text-white !py-1 !px-3 text-sm"
                                        >
                                            Close Job
                                        </Button>
                                    )}
                                    <Link to={`/manager/jds/${jd.id}`}>
                                        <Button variant="secondary" className="!py-1 !px-3 text-sm">
                                            View Recommendations
                                        </Button>
                                    </Link>
                                </div>
                            </Card>
                        ))
                    ) : (
                        <Card>
                            <p className="text-center text-gray-600">You have not uploaded any job descriptions yet. Click "Upload New JD" to get started.</p>
                        </Card>
                    )}
                </div>
            </div>

            <Modal 
                isOpen={isModalOpen} 
                onClose={() => setIsModalOpen(false)} 
                title="Upload a New Job Description"
            >
                <JdUploadForm onUploadSuccess={handleUploadSuccess} />
            </Modal>
        </>
    );
};

export default ManagerDashboard;