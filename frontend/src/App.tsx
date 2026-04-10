import { useState, useEffect } from 'react'
import './App.css'
import ClassSearch from './ClassSearch'

interface UserInfo {
  name: string;
  email: string;
  picture: string;
}

function App() {
  const [user, setUser] = useState<UserInfo | null>(null);

  useEffect(() => {
    fetch('/api/auth/me')
      .then(res => {
        if (res.ok) return res.json();
        return null;
      })
      .then(data => setUser(data))
      .catch(() => setUser(null));
  }, []);

  const handleLogin = () => {
    // Redirect to backend OAuth2 initiation endpoint
    window.location.href = '/oauth2/authorization/google';
  };

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
              <button className="login-button" onClick={handleLogin}>Login with Google</button>
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
    </div>
  )
}

export default App
