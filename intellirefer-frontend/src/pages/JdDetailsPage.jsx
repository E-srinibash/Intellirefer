import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import apiService from '../api/apiService';
import Spinner from '../components/ui/Spinner';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';

const JdDetailsPage = () => {
    const { jdId } = useParams();
    const [recommendations, setRecommendations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchRecommendations = async () => {
            try {
                setLoading(true);
                setError(null);
                const response = await apiService.get(`/manager/jds/${jdId}/recommendations`);
                // Filter out already rejected candidates on initial load for a cleaner view
                setRecommendations(response.data.filter(rec => rec.status !== 'REJECTED'));
            } catch (err) {
                setError('Failed to fetch recommendations. Please try again later.');
            } finally {
                setLoading(false);
            }
        };
        fetchRecommendations();
    }, [jdId]);

    // === THIS FUNCTION NOW HAS THE UPDATED LOGIC ===
    const handleAction = async (referralId, action) => {
        const originalRecommendations = [...recommendations];

        // Optimistic UI Update based on the action
        if (action === 'select' || action === 'reject') {
            // For 'select' AND 'reject', filter the employee out of the list immediately.
            setRecommendations(prevRecs => prevRecs.filter(r => r.referralId !== referralId));
        } else { // This handles the 'reserve' case
            // For 'reserve', we just update the status in the list.
            setRecommendations(prevRecs => prevRecs.map(r =>
                r.referralId === referralId ? { ...r, status: action.toUpperCase() } : r
            ));
        }

        try {
            // The API call is generic and works for all actions
            await apiService.post(`/manager/referrals/${referralId}/${action}`);
        } catch (err) {
            console.error(`Failed to ${action} employee:`, err);
            alert(`Action failed. The list will be restored.`);
            // On error, revert the UI back to its original state to maintain consistency
            setRecommendations(originalRecommendations);
        }
    };

    if (loading) return <div className="flex justify-center items-center h-screen"><Spinner /></div>;
    if (error) return <div className="p-8 text-center text-red-500">{error}</div>;

    return (
        <div className="container mx-auto p-4 md:p-8">
            <div className="mb-6">
                 <Link to="/manager/dashboard" className="text-blue-600 hover:underline">← Back to Dashboard</Link>
                <h1 className="text-3xl font-bold text-gray-800 mt-2">Candidate Recommendations</h1>
                <p className="text-md text-gray-500">For Job ID: {jdId}</p>
            </div>

            <div className="space-y-4">
                {recommendations.length > 0 ? (
                    recommendations.map((rec) => (
                        <Card key={rec.referralId} className="border-l-4" style={{borderColor: rec.status === 'RESERVED' ? '#F59E0B' : '#E5E7EB'}}>
                            <div className="flex flex-col md:flex-row justify-between items-start gap-4">
                                <div className="flex-grow">
                                    <h2 className="text-xl font-semibold text-gray-900">{rec.employeeFullName}</h2>
                                    {/* ... all other employee details ... */}
                                    <div className="text-sm text-gray-600 flex items-center gap-x-4">
                                        <span>{rec.currentRole || 'Role not specified'}</span>
                                        <span className="font-bold">·</span>
                                        <span>{rec.jobLevel || 'Level not specified'}</span>
                                        <span className="font-bold">·</span>
                                        <span>{rec.yearsOfExperience} years exp.</span>
                                    </div>
                                    <div className="mt-2">
                                        <span className={`px-2 py-0.5 text-xs font-semibold rounded-full ${
                                            rec.availability === 'AVAILABLE' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
                                        }`}>
                                            {rec.availability ? rec.availability.replace('_', ' ') : 'UNKNOWN'}
                                        </span>
                                        {rec.availability === 'ON_PROJECT' && rec.expectedAvailabilityDate && (
                                            <span className="ml-2 text-xs text-gray-500">
                                                (Available on {new Date(rec.expectedAvailabilityDate).toLocaleDateString()})
                                            </span>
                                        )}
                                    </div>
                                    <div className="mt-3 bg-gray-50 p-3 rounded-lg">
                                        <p className="font-mono text-sm">Match Score: <span className="font-bold text-blue-600 text-lg">{rec.matchScore}%</span></p>
                                        <p className="text-sm italic text-gray-700 mt-1">Justification: {rec.justification}</p>
                                    </div>
                                    <div className="mt-4">
                                        <h4 className="text-sm font-semibold text-gray-700 mb-2">Top Matching Skills:</h4>
                                        <div className="flex flex-wrap gap-2">
                                            {rec.matchingSkills && rec.matchingSkills.length > 0 ? (
                                                rec.matchingSkills.map(skill => (
                                                    <span key={skill} className="bg-blue-100 text-blue-800 text-xs font-semibold px-3 py-1 rounded-full">{skill}</span>
                                                ))
                                            ) : (
                                                <span className="text-xs text-gray-500 italic">No specific matching skills were identified.</span>
                                            )}
                                        </div>
                                    </div>
                                </div>

                                <div className="flex-shrink-0 flex flex-col items-end gap-2 w-full md:w-auto">
                                    <span className={`px-3 py-1 text-xs font-bold uppercase rounded-full ${
                                        rec.status === 'RESERVED' ? 'bg-yellow-100 text-yellow-800' : 'bg-gray-100 text-gray-800'
                                    }`}>
                                        {rec.status ? rec.status.replace('_', ' ') : 'PENDING'}
                                    </span>
                                    <div className="flex gap-2 mt-2">
                                        <Button onClick={() => handleAction(rec.referralId, 'select')} className="bg-green-500 hover:bg-green-600 text-white !py-1 !px-3 text-sm">Select</Button>
                                        <Button onClick={() => handleAction(rec.referralId, 'reserve')} className="bg-yellow-500 hover:bg-yellow-600 text-white !py-1 !px-3 text-sm">Reserve</Button>
                                        <Button onClick={() => handleAction(rec.referralId, 'reject')} className="bg-red-500 hover:bg-red-600 text-white !py-1 !px-3 text-sm">Reject</Button>
                                    </div>
                                </div>
                            </div>
                        </Card>
                    ))
                ) : (
                    <Card>
                        <p className="text-center text-gray-600">No pending or reserved recommendations found for this job description.</p>
                    </Card>
                )}
            </div>
        </div>
    );
};

export default JdDetailsPage;