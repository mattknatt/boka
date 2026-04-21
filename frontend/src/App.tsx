import { useState, useEffect, useCallback } from 'react'
import { Routes, Route, useNavigate, useLocation, Navigate } from 'react-router-dom'
import './App.css'
import ClassSearch from './ClassSearch'
import GymList from './GymList'
import LoginDropdown from './LoginDropdown'
import RegistrationModal from './RegistrationModal'
import MyBookings from './MyBookings'
import UserSettings from './UserSettings'
import AdminDashboard from './admin/AdminDashboard'

interface UserInfo {
  name: string;
  email: string;
  picture?: string;
  type: string;
  role?: string;
}

const HEADINGS: Record<string, { title: string; subtitle: string }> = {
  '/':         { title: 'Find your next workout.',    subtitle: 'The easiest way to find and book your favorite gym classes.' },
  '/search':   { title: 'Find your next workout.',    subtitle: 'Boka makes it easy to discover and book gym classes at your favorite local studios.' },
  '/gyms':     { title: 'Explore our gyms.',          subtitle: 'Discover the best fitness locations in your area and see what they have to offer.' },
  '/bookings': { title: 'Your reserved classes.',     subtitle: 'Keep track of your upcoming sessions and manage your fitness schedule in one place.' },
  '/settings': { title: 'Your account settings.',    subtitle: 'Manage your personal information and account preferences.' },
  '/admin':    { title: 'Admin Dashboard.',           subtitle: 'Manage classes, instructors, and gym schedules.' },
};

function App() {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [userLoading, setUserLoading] = useState(true);
  const [isRegisterModalOpen, setIsRegisterModalOpen] = useState(false);
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const fetchUser = useCallback(() => {
    setUserLoading(true);
    fetch('/api/auth/me')
      .then(res => (res.status === 200 ? res.json() : null))
      .then(data => setUser(data))
      .catch(() => setUser(null))
      .finally(() => setUserLoading(false));
  }, []);

  useEffect(() => {
    fetchUser();
  }, [fetchUser]);

  const handleLogout = () => {
    window.location.href = '/logout';
  };

  const { title, subtitle } = HEADINGS[pathname] ?? HEADINGS['/'];

  // Protect authenticated routes: wait for auth check before deciding
  const protectedElement = (element: React.ReactNode) => {
    if (userLoading) return null;
    return user ? element : <Navigate to="/" replace />;
  };

  return (
    <div className="app-container">
      <div className="content-wrapper">
        <header className="header">
          <button type="button" className="brand-logo" onClick={() => navigate('/')}>
            boka.
          </button>
          <div className="user-nav">
            {user ? (
              <div className="user-info">
                <button
                  type="button"
                  className="nav-link"
                  onClick={() => navigate('/bookings')}
                  style={{
                    fontWeight: pathname === '/bookings' ? 'bold' : 'normal',
                    color: pathname === '/bookings' ? '#ff1493' : '#333',
                    marginRight: '15px'
                  }}
                >
                  My Bookings
                </button>
                <button
                  type="button"
                  className="nav-link"
                  onClick={() => navigate('/settings')}
                  style={{
                    fontWeight: pathname === '/settings' ? 'bold' : 'normal',
                    color: pathname === '/settings' ? '#ff1493' : '#333',
                    marginRight: '15px'
                  }}
                >
                  Settings
                </button>
                {user.role === 'ADMIN' && (
                  <button
                    type="button"
                    className="nav-link"
                    onClick={() => navigate('/admin')}
                    style={{
                      fontWeight: pathname === '/admin' ? 'bold' : 'normal',
                      color: pathname === '/admin' ? '#ff1493' : '#333',
                      marginRight: '15px'
                    }}
                  >
                    Admin
                  </button>
                )}
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
          <h1>{title}</h1>
          <p>{subtitle}</p>
        </main>

        <section className="card">
          <div key={pathname} className="route-transition">
          <Routes>
            <Route path="/" element={
              <div className="landing-choices" style={{ display: 'flex', justifyContent: 'center', gap: '20px', marginTop: '20px' }}>
                <button
                  className="btn-primary"
                  style={{ width: 'auto', padding: '1.5rem 3rem', fontSize: '1.2rem' }}
                  onClick={() => navigate('/search')}
                >
                  Search Classes
                </button>
                <button
                  className="btn-primary"
                  style={{ width: 'auto', padding: '1.5rem 3rem', fontSize: '1.2rem', backgroundColor: '#333' }}
                  onClick={() => navigate('/gyms')}
                >
                  Find Gyms
                </button>
              </div>
            } />
            <Route path="/search" element={<ClassSearch isLoggedIn={user !== null} />} />
            <Route path="/gyms" element={<GymList />} />
            <Route path="/bookings" element={protectedElement(<MyBookings onCancelSuccess={() => {}} />)} />
            <Route path="/settings" element={protectedElement(<UserSettings onLogout={fetchUser} />)} />
            <Route path="/admin" element={
              userLoading ? null : (user?.role === 'ADMIN' ? <AdminDashboard /> : <Navigate to="/" replace />)
            } />
          </Routes>
          </div>
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
