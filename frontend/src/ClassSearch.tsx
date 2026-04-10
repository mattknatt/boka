import React, {useState, useRef, useEffect} from 'react';

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
    const [hasSearched, setHasSearched] = useState(false);
    const searchAbortControllerRef = useRef<AbortController | null>(null);

    useEffect(() => {
        return () => {
            if (searchAbortControllerRef.current) {
                searchAbortControllerRef.current.abort();
            }
        };
    }, []);

    const handleSearch = async (e: React.FormEvent) => {
        e.preventDefault();

        // Abort any existing controller before creating a new one
        if (searchAbortControllerRef.current) {
            searchAbortControllerRef.current.abort();
        }
        const controller = new AbortController();
        searchAbortControllerRef.current = controller;

        if (!query.trim()) {
            setResults([]);
            setError(null);
            setHasSearched(false);
            return;
        }

        setLoading(true);
        setError(null);
        setResults([]);

        try {
            const response = await fetch(`/api/classes/search?query=${encodeURIComponent(query)}`, {
                signal: controller.signal
            });
            if (!response.ok) {
                throw new Error('Failed to fetch search results');
            }
            const data = await response.json() as GymClass[];
            setResults(data);
            setHasSearched(true);
            setLoading(false);
        } catch (err: any) {
            if (err.name === 'AbortError') {
                return;
            }
            setError(err instanceof Error ? err.message : 'An unknown error occurred');
            setLoading(false);
        }
    };

    return (
        <div className="class-search">
            <h2>Search for Gym Classes</h2>
            <form onSubmit={handleSearch}>
                <label htmlFor="class-search-input">Search gym classes</label>
                <input
                    id="class-search-input"
                    type="text"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder="e.g., 'yoga' or 'spinning'"
                    style={{padding: '8px', width: '300px'}}
                />
                <button type="submit" disabled={loading} style={{marginLeft: '10px'}}>
                    {loading ? 'Searching...' : 'Search'}
                </button>
            </form>

            {error && <p style={{color: 'red'}}>{error}</p>}

            <div className="results" style={{marginTop: '20px'}}>
                {results.length > 0 ? (
                    <ul style={{listStyle: 'none', padding: 0}}>
                        {results.map((item) => (
                            <li key={item.id} style={{
                                border: '1px solid #ccc',
                                marginBottom: '10px',
                                padding: '10px',
                                borderRadius: '4px',
                                textAlign: 'left'
                            }}>
                                <h3>{item.classTypeName}</h3>
                                <p>Instructor: {item.instructorFirstName} {item.instructorLastName}</p>
                                <p>Time: {new Date(item.startTime).toLocaleString()}</p>
                                <p>Spots: {item.availableSpots} / {item.capacity}</p>
                                <p style={{
                                    fontWeight: 'bold',
                                    color: item.status === 'CANCELLED' ? '#d32f2f' :
                                        item.status === 'FULL' ? '#f57c00' : '#2e7d32'
                                }}>
                                    Status: {item.status}
                                </p>
                            </li>
                        ))}
                    </ul>
                ) : (
                    !loading && hasSearched && <p>No results found. Try searching for something else!</p>
                )}
            </div>
        </div>
    );
};

export default ClassSearch;
