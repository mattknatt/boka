import { useState, useEffect, useCallback } from 'react'
import './App.css'
import ClassSearch from './ClassSearch'
import LoginDropdown from './LoginDropdown'
import RegistrationModal from './RegistrationModal'

interface UserInfo {
  name: string;
  email: string;
  picture?: string;
  type: string;
  role?: string;
}

function App() {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [isRegisterModalOpen, setIsRegisterModalOpen] = useState(false);

  const fetchUser = useCallback(() => {
    fetch('/api/auth/me')
      .then(res => {
        if (res.status === 200) return res.json();
        return null;
      })
      .then(data => setUser(data))
      .catch(() => setUser(null));
  }, []);

  useEffect(() => {
    fetchUser();
  }, [fetchUser]);

  const handleLogout = () => {
    window.location.href = '/logout';
  };

  return (
    <div className="app-container">
      <div className="content-wrapper">
        <header className="header">
          <div className="brand-logo">boka.</div>
          <div className="user-nav">
            {user ? (
              <div className="user-info">
                <span className="user-name">Hey, {user.name}</span>
                <button className="login-button logout" onClick={handleLogout}>Logout</button>
              </div>
            ) : (
              <>
                <button className="create-account-button" onClick={() => setIsRegisterModalOpen(true)}>
                  Create Account
                </button>
                <LoginDropdown onLoginSuccess={fetchUser} />
              </>
            )}
          </div>
        </header>

        <main className="hero">
          <h1>Find your next workout.</h1>
          <p>
            Boka makes it easy to discover and book gym classes at your favorite local studios. 
            Start your fitness journey today.
          </p>
        </main>

        <section className="card">
          <ClassSearch />
        </section>
      </div>

      <RegistrationModal 
        isOpen={isRegisterModalOpen} 
        onClose={() => setIsRegisterModalOpen(false)}
        onSuccess={fetchUser}
      />
    </div>
  )
}

export default App
