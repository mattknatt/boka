import React, { useState } from 'react';

interface GymClass {
  id: number;
  classTypeName: string;
  instructorFirstName: string;
  instructorLastName: string;
  startTime: string;
  endTime: string;
  capacity: number;
  availableSpots: number;
  status: string;
}

const ClassSearch: React.FC = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<GymClass[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) return;

    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`/api/classes/search?query=${encodeURIComponent(query)}`);
      if (!response.ok) {
        throw new Error('Failed to fetch search results');
      }
      const data = await response.json();
      setResults(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="class-search">
      <h2>Search for Gym Classes</h2>
      <form onSubmit={handleSearch}>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="e.g., 'build muscle' or 'yoga'"
          style={{ padding: '8px', width: '300px' }}
        />
        <button type="submit" disabled={loading} style={{ marginLeft: '10px' }}>
          {loading ? 'Searching...' : 'Search'}
        </button>
      </form>

      {error && <p style={{ color: 'red' }}>{error}</p>}

      <div className="results" style={{ marginTop: '20px' }}>
        {results.length > 0 ? (
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {results.map((item) => (
              <li key={item.id} style={{ border: '1px solid #ccc', marginBottom: '10px', padding: '10px', borderRadius: '4px', textAlign: 'left' }}>
                <h3>{item.classTypeName}</h3>
                <p>Instructor: {item.instructorFirstName} {item.instructorLastName}</p>
                <p>Time: {new Date(item.startTime).toLocaleString()}</p>
                <p>Spots: {item.availableSpots} / {item.capacity}</p>
              </li>
            ))}
          </ul>
        ) : (
          !loading && <p>No results found. Try searching for something else!</p>
        )}
      </div>
    </div>
  );
};

export default ClassSearch;
