import './App.css'
import ClassSearch from './ClassSearch'

function App() {
  return (
    <div className="app-container">
      <header className="header">
        <div className="brand-logo">boka.</div>
        <button className="login-button">Login</button>
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
  )
}

export default App
