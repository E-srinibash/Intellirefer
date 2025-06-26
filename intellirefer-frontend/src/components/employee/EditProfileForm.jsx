import React, { useState, useEffect } from 'react';
import Button from '../ui/Button';
import Spinner from '../ui/Spinner';

/**
 * A form component for editing an employee's profile details.
 * @param {object} props - The component's props.
 * @param {object} props.currentProfile - The employee's current profile data to pre-fill the form.
 * @param {function} props.onUpdateSuccess - A callback function to handle the API call on form submission.
 * @param {function} props.onClose - A callback to close the modal.
 */
const EditProfileForm = ({ currentProfile, onUpdateSuccess, onClose }) => {
    const [formData, setFormData] = useState({
        fullName: '',
        yearsOfExperience: 0,
        availability: 'AVAILABLE',
        jobLevel: '',
        currentRole: '',
        expectedAvailabilityDate: '',
        skills: '' // We use a comma-separated string for easy editing in a textarea
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    // Pre-fill the form when the component receives the current profile data
    useEffect(() => {
        if (currentProfile) {
            setFormData({
                fullName: currentProfile.fullName || '',
                yearsOfExperience: currentProfile.yearsOfExperience || 0,
                availability: currentProfile.availability || 'AVAILABLE',
                jobLevel: currentProfile.jobLevel || '',
                currentRole: currentProfile.currentRole || '',
                // Format date for the HTML date input field, which expects 'YYYY-MM-DD'
                expectedAvailabilityDate: currentProfile.expectedAvailabilityDate ? currentProfile.expectedAvailabilityDate.split('T')[0] : '',
                skills: currentProfile.skills ? currentProfile.skills.join(', ') : ''
            });
        }
    }, [currentProfile]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        // Convert the comma-separated skills string back into a clean array
        const skillsArray = formData.skills
            .split(',')
            .map(skill => skill.trim())
            .filter(skill => skill); // Remove any empty strings resulting from extra commas

        const payload = {
            fullName: formData.fullName,
            yearsOfExperience: parseInt(formData.yearsOfExperience, 10),
            availability: formData.availability,
            jobLevel: formData.jobLevel,
            currentRole: formData.currentRole,
            // Only send the date if the status is ON_PROJECT, otherwise send null
            expectedAvailabilityDate: formData.availability === 'ON_PROJECT' ? formData.expectedAvailabilityDate : null,
            skills: skillsArray
        };

        // The onUpdateSuccess prop is an async function that handles the API call
        onUpdateSuccess(payload)
            .catch((err) => {
                setError(err.response?.data?.message || 'Failed to update profile. Please check your inputs.');
            })
            .finally(() => {
                setLoading(false);
            });
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div>
                <label htmlFor="fullName" className="block text-sm font-medium text-gray-700">Full Name</label>
                <input type="text" name="fullName" id="fullName" value={formData.fullName} onChange={handleChange} required className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"/>
            </div>
            <div>
                <label htmlFor="currentRole" className="block text-sm font-medium text-gray-700">Current Role</label>
                <input type="text" name="currentRole" id="currentRole" placeholder="e.g., Senior Java Developer" value={formData.currentRole} onChange={handleChange} required className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"/>
            </div>
             <div>
                <label htmlFor="jobLevel" className="block text-sm font-medium text-gray-700">Job Level</label>
                <input type="text" name="jobLevel" id="jobLevel" placeholder="e.g., Mid-Level, Senior, Lead" value={formData.jobLevel} onChange={handleChange} required className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"/>
            </div>
            <div>
                <label htmlFor="yearsOfExperience" className="block text-sm font-medium text-gray-700">Years of Experience</label>
                <input type="number" name="yearsOfExperience" id="yearsOfExperience" value={formData.yearsOfExperience} onChange={handleChange} required min="0" className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"/>
            </div>
            <div>
                <label htmlFor="availability" className="block text-sm font-medium text-gray-700">Availability Status</label>
                <select name="availability" id="availability" value={formData.availability} onChange={handleChange} required className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm">
                    <option value="AVAILABLE">Available</option>
                    <option value="ON_PROJECT">On a Project</option>
                    <option value="RESERVED">Reserved</option>
                </select>
            </div>

            {/* Conditionally render the date input only when status is ON_PROJECT */}
            {formData.availability === 'ON_PROJECT' && (
                <div>
                    <label htmlFor="expectedAvailabilityDate" className="block text-sm font-medium text-gray-700">Expected Availability Date</label>
                    <input type="date" name="expectedAvailabilityDate" id="expectedAvailabilityDate" value={formData.expectedAvailabilityDate} onChange={handleChange} required className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"/>
                </div>
            )}

            <div>
                <label htmlFor="skills" className="block text-sm font-medium text-gray-700">Skills (comma-separated)</label>
                <textarea name="skills" id="skills" value={formData.skills} onChange={handleChange} rows="3"
                    className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"
                    placeholder="e.g., Java, Spring Boot, React, SQL"
                ></textarea>
            </div>

            {error && <p className="text-sm text-red-600 py-2">{error}</p>}

            <div className="flex justify-end gap-3 pt-4">
                 <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
                <Button type="submit" disabled={loading}>
                    {loading ? <Spinner /> : 'Save Changes'}
                </Button>
            </div>
        </form>
    );
};

export default EditProfileForm;