import React, {useState, useRef, useEffect, useCallback} from 'react';

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

    const performSearch = useCallback(async (searchQuery: string) => {
        // Abort any existing controller before creating a new one
        if (searchAbortControllerRef.current) {
            searchAbortControllerRef.current.abort();
        }
        const controller = new AbortController();
        searchAbortControllerRef.current = controller;

        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`/api/classes/search?query=${encodeURIComponent(searchQuery)}`, {
                signal: controller.signal
            });
            if (!response.ok) {
                throw new Error('Failed to fetch search results');
            }
            const data = await response.json() as GymClass[];
            setResults(data);
            setHasSearched(true);
            setLoading(false);
        } catch (err: unknown) {
            if (err instanceof Error && err.name === 'AbortError') {
                return;
            }
            setError(err instanceof Error ? err.message : 'An unknown error occurred');
            setLoading(false);
        }
    }, []);

    // Cleanup on unmount
    useEffect(() => {
        return () => {
            if (searchAbortControllerRef.current) {
                searchAbortControllerRef.current.abort();
            }
        };
    }, []);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        if (query.trim()) {
            performSearch(query);
        }
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = e.target.value;
        setQuery(newValue);
        // Hide classes if input is cleared
        if (newValue === '') {
            setResults([]);
            setHasSearched(false);
        }
    };

    return (
        <div className="class-search">
            <h2>Find Gym Classes</h2>
            <form onSubmit={handleSearch}>
                <label htmlFor="class-search-input" style={{ display: 'block', marginBottom: '8px' }}>
                    Search by class name
                </label>
                <input
                    id="class-search-input"
                    type="text"
                    value={query}
                    onChange={handleInputChange}
                    placeholder="e.g., 'Yoga' or 'Spinning'"
                    style={{padding: '10px', width: '300px', borderRadius: '4px', border: '1px solid #ccc'}}
                />
                <button 
                    type="submit" 
                    disabled={loading} 
                    style={{
                        marginLeft: '10px', 
                        padding: '10px 20px', 
                        cursor: loading ? 'not-allowed' : 'pointer',
                        backgroundColor: '#ff1493',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        fontWeight: '600',
                        transition: 'opacity 0.2s',
                        opacity: loading ? 0.7 : 1
                    }}
                >
                    {loading ? 'Searching...' : 'Search'}
                </button>
            </form>

            {error && <p style={{color: 'red', marginTop: '10px'}}>{error}</p>}

            {hasSearched && (
                <div className="results" style={{marginTop: '30px'}}>
                    {results.length > 0 ? (
                        <>
                            <h3 style={{ marginBottom: '20px' }}>Search Results</h3>
                            <ul style={{listStyle: 'none', padding: 0, display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px'}}>
                                {results.map((item) => (
                                    <li key={item.id} style={{
                                        border: '1px solid #eee',
                                        padding: '20px',
                                        borderRadius: '8px',
                                        textAlign: 'left',
                                        boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
                                        backgroundColor: '#fff',
                                        color: '#333'
                                    }}>
                                        <h3 style={{marginTop: 0, color: '#ff1493'}}>{item.classTypeName}</h3>
                                        <p><strong>Instructor:</strong> {item.instructorFirstName} {item.instructorLastName}</p>
                                        <p><strong>Time:</strong> {new Date(item.startTime).toLocaleString('sv-SE', {
                                            weekday: 'long',
                                            day: 'numeric',
                                            month: 'short',
                                            hour: '2-digit',
                                            minute: '2-digit'
                                        })}</p>
                                        <p><strong>Spots:</strong> {item.availableSpots} / {item.capacity}</p>
                                        <div style={{
                                            display: 'inline-block',
                                            padding: '4px 12px',
                                            borderRadius: '12px',
                                            fontSize: '0.85rem',
                                            fontWeight: 'bold',
                                            backgroundColor: item.status === 'CANCELLED' ? '#ffebee' :
                                                item.status === 'FULL' ? '#fff3e0' : '#e8f5e9',
                                            color: item.status === 'CANCELLED' ? '#d32f2f' :
                                                item.status === 'FULL' ? '#ef6c00' : '#2e7d32'
                                        }}>
                                            {item.status}
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        </>
                    ) : (
                        !loading && <p>No classes found. Try a different search term!</p>
                    )}
                </div>
            )}
        </div>
    );
};

export default ClassSearch;
