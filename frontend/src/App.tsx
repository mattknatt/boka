import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import ClassSearch from './ClassSearch'

function App() {
  return (
    <>
      <div>
        <a href="https://vite.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1>Boka Gym Classes</h1>
      <div className="card">
        <ClassSearch />
      </div>
      <p className="read-the-docs">
        Search for your favorite gym classes and start training!
      </p>
    </>
  )
}

export default App
