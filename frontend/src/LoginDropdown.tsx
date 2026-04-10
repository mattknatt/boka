import React, { useState, useRef, useEffect } from 'react';

interface LoginDropdownProps {
  onLoginSuccess: () => void;
}

const LoginDropdown: React.FC<LoginDropdownProps> = ({ onLoginSuccess }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleFormLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    const params = new URLSearchParams();
    params.append('username', formData.email);
    params.append('password', formData.password);

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
      });

      if (response.ok) {
        onLoginSuccess();
        setIsOpen(false);
      } else {
        setError('Invalid email or password');
      }
    } catch {
      setError('Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = '/oauth2/authorization/google';
  };

  return (
    <div className="login-dropdown-container" ref={dropdownRef}>
      <button className="login-button" onClick={() => setIsOpen(!isOpen)}>
        Login
      </button>

      {isOpen && (
        <div className="login-dropdown-menu">
          <h3>Login to Boka</h3>
          <form onSubmit={handleFormLogin}>
            <div className="form-group">
              <input
                type="email"
                placeholder="Email"
                required
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              />
            </div>
            <div className="form-group">
              <input
                type="password"
                placeholder="Password"
                required
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              />
            </div>
            {error && <p className="error-message">{error}</p>}
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Logging in...' : 'Sign In'}
            </button>
          </form>

          <div className="divider">
            <span>OR</span>
          </div>

          <button className="btn-google" onClick={handleGoogleLogin}>
            Continue with Google
          </button>
        </div>
      )}
    </div>
  );
};

export default LoginDropdown;
