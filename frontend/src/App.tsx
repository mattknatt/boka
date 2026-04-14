import { useState, useEffect, useCallback } from 'react'
import './App.css'
import ClassSearch from './ClassSearch'
import LoginDropdown from './LoginDropdown'
import RegistrationModal from './RegistrationModal'
import MyBookings from './MyBookings'
import UserSettings from './UserSettings'

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
  const [view, setView] = useState<'search' | 'bookings' | 'settings'>('search');

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
          <button 
            type="button"
            className="brand-logo" 
            onClick={() => setView('search')}
          >
            boka.
          </button>
          <div className="user-nav">
            {user ? (
              <div className="user-info">
                <button 
                  type="button"
                  className="nav-link" 
                  onClick={() => setView('bookings')}
                  style={{ 
                    fontWeight: view === 'bookings' ? 'bold' : 'normal',
                    color: view === 'bookings' ? '#ff1493' : '#333',
                    marginRight: '15px'
                  }}
                >
                  My Bookings
                </button>
                <button 
                  type="button"
                  className="nav-link" 
                  onClick={() => setView('settings')}
                  style={{ 
                    fontWeight: view === 'settings' ? 'bold' : 'normal',
                    color: view === 'settings' ? '#ff1493' : '#333',
                    marginRight: '15px'
                  }}
                >
                  Settings
                </button>
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
          <h1>
            {view === 'search' && 'Find your next workout.'}
            {view === 'bookings' && 'Your reserved classes.'}
            {view === 'settings' && 'Your account settings.'}
          </h1>
          <p>
            {view === 'search' && 'Boka makes it easy to discover and book gym classes at your favorite local studios.'}
            {view === 'bookings' && 'Keep track of your upcoming sessions and manage your fitness schedule in one place.'}
            {view === 'settings' && 'Manage your personal information and account preferences.'}
          </p>
        </main>

        <section className="card">
          {view === 'search' && <ClassSearch isLoggedIn={user !== null} />}
          {view === 'bookings' && <MyBookings onCancelSuccess={() => {}} />}
          {view === 'settings' && <UserSettings onLogout={fetchUser} onBack={() => setView('search')} />}
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
