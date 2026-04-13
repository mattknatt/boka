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

interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
    first: boolean;
    last: boolean;
}

interface ClassSearchProps {
    isLoggedIn: boolean;
}

const ClassSearch: React.FC<ClassSearchProps> = ({ isLoggedIn }) => {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState<GymClass[]>([]);
    const [loading, setLoading] = useState(false);
    const [bookingLoading, setBookingLoading] = useState<Record<number, boolean>>({});
    const [error, setError] = useState<string | null>(null);
    const [hasSearched, setHasSearched] = useState(false);
    
    // Pagination state
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    
    const searchAbortControllerRef = useRef<AbortController | null>(null);

    const performSearch = useCallback(async (searchQuery: string, pageNum: number) => {
        if (searchAbortControllerRef.current) {
            searchAbortControllerRef.current.abort();
        }
        const controller = new AbortController();
        searchAbortControllerRef.current = controller;

        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`/api/classes/search?query=${encodeURIComponent(searchQuery)}&page=${pageNum}&size=6`, {
                signal: controller.signal
            });
            if (!response.ok) {
                throw new Error('Failed to fetch search results');
            }
            const data = await response.json() as PageResponse<GymClass>;
            setResults(data.content);
            setTotalPages(data.totalPages);
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

    // Search when page changes
    useEffect(() => {
        if (hasSearched) {
            performSearch(query, page);
        }
    }, [page, performSearch]);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        setPage(0); // Reset to first page on new search
        performSearch(query, 0);
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = e.target.value;
        setQuery(newValue);
        if (newValue === '') {
            setResults([]);
            setHasSearched(false);
            setTotalPages(0);
            setPage(0);
        }
    };

    const handleBookClass = async (classId: number) => {
        if (!isLoggedIn) return;

        setBookingLoading(prev => ({ ...prev, [classId]: true }));
        setError(null);

        try {
            const response = await fetch('/api/bookings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ gymClassId: classId })
            });

            if (response.ok) {
                alert('Booking successful!');
                performSearch(query, page);
            } else {
                const data = await response.json();
                setError(data.message || 'Booking failed.');
            }
        } catch {
            setError('An error occurred while booking.');
        } finally {
            setBookingLoading(prev => ({ ...prev, [classId]: false }));
        }
    };

    return (
        <div className="class-search">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '20px' }}>Find Gym Classes</h2>
            <form onSubmit={handleSearch} style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '10px' }}>
                <div style={{ textAlign: 'left' }}>
                    <input
                        id="class-search-input"
                        type="text"
                        value={query}
                        onChange={handleInputChange}
                        placeholder="e.g., 'Yoga' or 'Spinning'"
                        style={{padding: '10px', width: '250px', borderRadius: '4px', border: '1px solid #ccc'}}
                    />
                </div>
                <button 
                    type="submit" 
                    disabled={loading} 
                    style={{
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
                <div className="results" style={{marginTop: '25px'}}>
                    {results.length > 0 ? (
                        <>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                                <h3 style={{ margin: 0, fontSize: '1.2rem' }}>Upcoming Classes</h3>
                                <div className="pagination-info" style={{ fontSize: '0.9rem', color: '#666' }}>
                                    Page {page + 1} of {totalPages}
                                </div>
                            </div>

                            <ul style={{
                                listStyle: 'none', 
                                padding: 0, 
                                display: 'grid', 
                                gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', 
                                gap: '15px'
                            }}>
                                {results.map((item) => {
                                    const isFull = item.availableSpots <= 0 || item.status === 'FULL';
                                    const isLoading = bookingLoading[item.id];
                                    
                                    return (
                                        <li key={item.id} style={{
                                            border: '1px solid #eee',
                                            padding: '15px',
                                            borderRadius: '8px',
                                            textAlign: 'left',
                                            boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
                                            backgroundColor: '#fff',
                                            color: '#333',
                                            display: 'flex',
                                            flexDirection: 'column',
                                            justifyContent: 'space-between',
                                            fontSize: '0.9rem'
                                        }}>
                                            <div>
                                                <h4 style={{marginTop: 0, marginBottom: '8px', color: '#ff1493', fontSize: '1.1rem'}}>{item.classTypeName}</h4>
                                                <p style={{ margin: '4px 0' }}><strong>Instructor:</strong> {item.instructorFirstName}</p>
                                                <p style={{ margin: '4px 0' }}><strong>Time:</strong> {new Date(item.startTime).toLocaleString('sv-SE', {
                                                    weekday: 'short',
                                                    day: 'numeric',
                                                    month: 'short',
                                                    hour: '2-digit',
                                                    minute: '2-digit'
                                                })}</p>
                                                <p style={{ margin: '4px 0' }}><strong>Spots:</strong> {item.availableSpots} / {item.capacity}</p>
                                                
                                                <div style={{
                                                    display: 'inline-block',
                                                    padding: '2px 8px',
                                                    borderRadius: '10px',
                                                    fontSize: '0.75rem',
                                                    fontWeight: 'bold',
                                                    marginTop: '8px',
                                                    marginBottom: '12px',
                                                    backgroundColor: item.status === 'CANCELLED' ? '#ffebee' :
                                                        item.status === 'FULL' ? '#fff3e0' : '#e8f5e9',
                                                    color: item.status === 'CANCELLED' ? '#d32f2f' :
                                                        item.status === 'FULL' ? '#ef6c00' : '#2e7d32',
                                                }}>
                                                    {item.status}
                                                </div>
                                            </div>
                                            
                                            <button
                                                onClick={() => handleBookClass(item.id)}
                                                disabled={!isLoggedIn || isFull || isLoading}
                                                style={{
                                                    width: '100%',
                                                    padding: '8px',
                                                    borderRadius: '4px',
                                                    border: 'none',
                                                    fontWeight: '600',
                                                    fontSize: '0.85rem',
                                                    cursor: (!isLoggedIn || isFull || isLoading) ? 'not-allowed' : 'pointer',
                                                    backgroundColor: !isLoggedIn ? '#f0f0f0' : (isFull ? '#eee' : '#ff1493'),
                                                    color: !isLoggedIn ? '#999' : (isFull ? '#999' : 'white'),
                                                    transition: 'opacity 0.2s',
                                                    opacity: isLoading ? 0.7 : 1
                                                }}
                                            >
                                                {!isLoggedIn ? 'Log in to book' : (isFull ? 'Full' : (isLoading ? '...' : 'Book'))}
                                            </button>
                                        </li>
                                    );
                                })}
                            </ul>

                            {totalPages > 1 && (
                                <div className="pagination-controls" style={{ marginTop: '30px', display: 'flex', justifyContent: 'center', gap: '10px' }}>
                                    <button 
                                        disabled={page === 0 || loading}
                                        onClick={() => setPage(prev => prev - 1)}
                                        style={{
                                            padding: '8px 16px',
                                            borderRadius: '4px',
                                            border: '1px solid #ccc',
                                            backgroundColor: '#fff',
                                            cursor: (page === 0 || loading) ? 'not-allowed' : 'pointer',
                                            opacity: (page === 0 || loading) ? 0.5 : 1
                                        }}
                                    >
                                        Previous
                                    </button>
                                    <button 
                                        disabled={page >= totalPages - 1 || loading}
                                        onClick={() => setPage(prev => prev + 1)}
                                        style={{
                                            padding: '8px 16px',
                                            borderRadius: '4px',
                                            border: '1px solid #ccc',
                                            backgroundColor: '#fff',
                                            cursor: (page >= totalPages - 1 || loading) ? 'not-allowed' : 'pointer',
                                            opacity: (page >= totalPages - 1 || loading) ? 0.5 : 1
                                        }}
                                    >
                                        Next
                                    </button>
                                </div>
                            )}
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
