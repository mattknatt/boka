import React, { useState } from 'react';

interface RegistrationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const RegistrationModal: React.FC<RegistrationModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: ''
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setErrors({});

    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      const data = await response.json();

      if (response.ok) {
        onSuccess();
        onClose();
      } else {
        if (typeof data === 'object' && data !== null) {
          setErrors(data as Record<string, string>);
        } else {
          setErrors({ general: data.message || 'Registration failed' });
        }
      }
    } catch {
      setErrors({ general: 'An error occurred. Please try again.' });
    } finally {
      setLoading(false);
    }
  };

  const getErrorStyle = (field: string) => ({
    color: 'red',
    fontSize: '0.85rem',
    marginTop: '4px',
    display: errors[field] ? 'block' : 'none'
  });

  return (
    <div className="modal-overlay">
      <div className="modal-content" style={{ textAlign: 'left' }}>
        <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>Create Account</h2>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '5px' }}>First Name</label>
            <input
              type="text"
              placeholder="First Name"
              required
              value={formData.firstName}
              onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
              style={{ width: '100%', padding: '10px', border: errors.firstName ? '1px solid red' : '1px solid #ccc', borderRadius: '4px' }}
            />
            <span style={getErrorStyle('firstName')}>{errors.firstName}</span>
          </div>
          
          <div>
            <label style={{ display: 'block', marginBottom: '5px' }}>Last Name</label>
            <input
              type="text"
              placeholder="Last Name"
              required
              value={formData.lastName}
              onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              style={{ width: '100%', padding: '10px', border: errors.lastName ? '1px solid red' : '1px solid #ccc', borderRadius: '4px' }}
            />
            <span style={getErrorStyle('lastName')}>{errors.lastName}</span>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '5px' }}>Email</label>
            <input
              type="email"
              placeholder="Email"
              required
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              style={{ width: '100%', padding: '10px', border: (errors.email || errors.message) ? '1px solid red' : '1px solid #ccc', borderRadius: '4px' }}
            />
            <span style={getErrorStyle('email')}>{errors.email}</span>
            <span style={getErrorStyle('message')}>{errors.message}</span>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '5px' }}>Password (min 6 chars)</label>
            <input
              type="password"
              placeholder="Password"
              required
              minLength={6}
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              style={{ width: '100%', padding: '10px', border: errors.password ? '1px solid red' : '1px solid #ccc', borderRadius: '4px' }}
            />
            <span style={getErrorStyle('password')}>{errors.password}</span>
          </div>
          
          {errors.general && <p style={{ color: 'red', margin: 0 }}>{errors.general}</p>}
          
          <div className="modal-actions" style={{ marginTop: '10px' }}>
            <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={loading} style={{ backgroundColor: '#ff1493', color: 'white', border: 'none', padding: '10px 20px', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}>
              {loading ? 'Creating...' : 'Register'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default RegistrationModal;
