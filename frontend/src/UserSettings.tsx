import React, { useState, useEffect } from 'react';

interface UserResponse {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    phoneNumber: string | null;
    role: string;
    isActive: boolean;
    createdAt: string;
}

interface UserSettingsProps {
    onLogout: () => void;
    onBack: () => void;
}

const UserSettings: React.FC<UserSettingsProps> = ({ onLogout, onBack }) => {
    const [user, setUser] = useState<UserResponse | null>(null);
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [message, setMessage] = useState<string | null>(null);

    useEffect(() => {
        fetchUser();
    }, []);

    const fetchUser = async () => {
        try {
            const response = await fetch('/api/users/me');
            if (response.ok) {
                const data = await response.json();
                setUser(data);
                setFirstName(data.firstName);
                setLastName(data.lastName);
                setPhoneNumber(data.phoneNumber || '');
            } else {
                setErrors({ general: 'Failed to load user details.' });
            }
        } catch (err) {
            setErrors({ general: 'An error occurred while fetching user details.' });
        } finally {
            setLoading(false);
        }
    };

    const handleUpdate = async (e: React.FormEvent) => {
        e.preventDefault();
        setSaving(true);
        setErrors({});
        setMessage(null);

        const updateRequest = {
            firstName,
            lastName,
            phoneNumber: phoneNumber || null,
            password: password || null
        };

        try {
            const response = await fetch('/api/users/me', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updateRequest)
            });

            const data = await response.json();

            if (response.ok) {
                setMessage('Profile updated successfully!');
                setPassword('');
            } else {
                if (typeof data === 'object' && data !== null) {
                    setErrors(data as Record<string, string>);
                } else {
                    setErrors({ general: data.message || 'Failed to update profile.' });
                }
            }
        } catch (err) {
            setErrors({ general: 'An error occurred while updating profile.' });
        } finally {
            setSaving(false);
        }
    };

    const handleDeleteAccount = async () => {
        if (!window.confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
            return;
        }

        try {
            const response = await fetch('/api/users/me', {
                method: 'DELETE'
            });

            if (response.ok) {
                alert('Account deleted successfully.');
                onLogout();
            } else {
                setErrors({ general: 'Failed to delete account.' });
            }
        } catch (err) {
            setErrors({ general: 'An error occurred while deleting account.' });
        }
    };

    const getErrorStyle = (field: string) => ({
        color: 'red',
        fontSize: '0.85rem',
        marginTop: '4px',
        display: errors[field] ? 'block' : 'none'
    });

    if (loading) return <div>Loading settings...</div>;

    return (
        <div className="user-settings" style={{ maxWidth: '500px', margin: '0 auto', textAlign: 'left' }}>
            <button onClick={onBack} style={{ marginBottom: '20px', cursor: 'pointer', background: 'none', border: 'none', color: '#ff1493', fontWeight: 'bold' }}>
                &larr; Back to classes
            </button>
            
            <h2 style={{ marginBottom: '20px' }}>User Settings</h2>
            
            {errors.general && <p style={{ color: 'red' }}>{errors.general}</p>}
            {message && <p style={{ color: 'green' }}>{message}</p>}

            <form onSubmit={handleUpdate} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                <div>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Email</label>
                    <input type="text" value={user?.email || ''} disabled style={{ width: '100%', padding: '10px', background: '#f0f0f0', border: errors.message ? '1px solid red' : '1px solid #ccc', borderRadius: '4px' }} />
                    <span style={getErrorStyle('message')}>{errors.message}</span>
                </div>
                <div>
                    <label style={{ display: 'block', marginBottom: '5px' }}>First Name</label>
                    <input 
                        type="text" 
                        value={firstName} 
                        onChange={(e) => setFirstName(e.target.value)} 
                        required 
                        style={{ width: '100%', padding: '10px', border: errors.firstName ? '1px solid red' : '1px solid #ccc', borderRadius: '4px' }} 
                    />
                    <span style={getErrorStyle('firstName')}>{errors.firstName}</span>
                </div>
                <div>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Last Name</label>
                    <input 
                        type="text" 
                        value={lastName} 
                        onChange={(e) => setLastName(e.target.value)} 
                        required 
                        style={{ width: '100%', padding: '10px', border: errors.lastName ? '1px solid red' : '1px solid #ccc', borderRadius: '4px' }} 
                    />
                    <span style={getErrorStyle('lastName')}>{errors.lastName}</span>
                </div>
                <div>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Phone Number</label>
                    <input 
                        type="text" 
                        value={phoneNumber} 
                        onChange={(e) => setPhoneNumber(e.target.value)} 
                        style={{ width: '100%', padding: '10px', border: errors.phoneNumber ? '1px solid red' : '1px solid #ccc', borderRadius: '4px' }} 
                    />
                    <span style={getErrorStyle('phoneNumber')}>{errors.phoneNumber}</span>
                </div>
                <div>
                    <label style={{ display: 'block', marginBottom: '5px' }}>New Password (min 8 chars, leave blank to keep current)</label>
                    <input 
                        type="password" 
                        value={password} 
                        onChange={(e) => setPassword(e.target.value)} 
                        minLength={8}
                        style={{ width: '100%', padding: '10px', border: errors.password ? '1px solid red' : '1px solid #ccc', borderRadius: '4px' }} 
                    />
                    <span style={getErrorStyle('password')}>{errors.password}</span>
                </div>
                <button 
                    type="submit" 
                    disabled={saving}
                    style={{ 
                        padding: '12px', 
                        backgroundColor: '#ff1493', 
                        color: 'white', 
                        border: 'none', 
                        borderRadius: '4px', 
                        fontWeight: 'bold', 
                        cursor: saving ? 'not-allowed' : 'pointer',
                        marginTop: '10px'
                    }}
                >
                    {saving ? 'Saving...' : 'Save Changes'}
                </button>
            </form>

            <div style={{ marginTop: '50px', borderTop: '1px solid #eee', paddingTop: '20px' }}>
                <h3>Danger Zone</h3>
                <p style={{ fontSize: '0.9rem', color: '#666' }}>Once you delete your account, there is no going back. Please be certain.</p>
                <button 
                    onClick={handleDeleteAccount}
                    style={{ 
                        padding: '10px 20px', 
                        backgroundColor: '#fff', 
                        color: '#d32f2f', 
                        border: '1px solid #d32f2f', 
                        borderRadius: '4px', 
                        fontWeight: 'bold', 
                        cursor: 'pointer',
                        marginTop: '10px'
                    }}
                >
                    Delete Account
                </button>
            </div>
        </div>
    );
};

export default UserSettings;
