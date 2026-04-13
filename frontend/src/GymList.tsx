import React, { useState, useEffect, useCallback } from 'react';

interface Gym {
    id: number;
    name: string;
    address: string;
    latitude: number;
    longitude: number;
}

interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
}

const GymList: React.FC = () => {
    const [gyms, setGyms] = useState<Gym[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [error, setError] = useState<string | null>(null);

    const fetchGyms = useCallback(async (pageNum: number) => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`/api/gyms?page=${pageNum}&size=6`);
            if (!response.ok) throw new Error('Failed to fetch gyms');
            const data = await response.json() as PageResponse<Gym>;
            setGyms(data.content);
            setTotalPages(data.totalPages);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An unknown error occurred');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchGyms(page);
    }, [page, fetchGyms]);

    const handleViewOnMap = (lat: number, lon: number) => {
        const url = `https://www.google.com/maps/search/?api=1&query=${lat},${lon}`;
        window.open(url, '_blank', 'noopener,noreferrer');
    };

    if (loading && gyms.length === 0) return <div>Loading gyms...</div>;

    return (
        <div className="gym-list-container">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '20px' }}>Our Gyms in Gothenburg</h2>
            
            {error && <p style={{ color: 'red' }}>{error}</p>}

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '20px', textAlign: 'left' }}>
                {gyms.map(gym => (
                    <div key={gym.id} style={{
                        border: '1px solid #eee',
                        padding: '20px',
                        borderRadius: '10px',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
                        backgroundColor: '#fff',
                        display: 'flex',
                        flexDirection: 'column',
                        justifyContent: 'space-between'
                    }}>
                        <div>
                            <h3 style={{ margin: '0 0 10px 0', color: '#ff1493' }}>{gym.name}</h3>
                            <p style={{ margin: '5px 0', fontSize: '0.9rem', color: '#555' }}>
                                <strong>Address:</strong> {gym.address}
                            </p>
                        </div>
                        <button 
                            onClick={() => handleViewOnMap(gym.latitude, gym.longitude)}
                            style={{
                                marginTop: '20px',
                                width: '100%',
                                padding: '10px',
                                backgroundColor: '#f0f0f0',
                                border: '1px solid #ddd',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontWeight: '600',
                                fontSize: '0.9rem',
                                transition: 'background-color 0.2s'
                            }}
                            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#e0e0e0'}
                            onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#f0f0f0'}
                        >
                            View on Map
                        </button>
                    </div>
                ))}
            </div>

            {totalPages > 1 && (
                <div style={{ marginTop: '30px', display: 'flex', justifyContent: 'center', gap: '10px', alignItems: 'center' }}>
                    <button 
                        disabled={page === 0 || loading}
                        onClick={() => setPage(p => p - 1)}
                        style={{ padding: '8px 16px', borderRadius: '4px', border: '1px solid #ccc', cursor: page === 0 ? 'not-allowed' : 'pointer', opacity: page === 0 ? 0.5 : 1 }}
                    >
                        Previous
                    </button>
                    <span style={{ fontSize: '0.9rem', color: '#666' }}>Page {page + 1} of {totalPages}</span>
                    <button 
                        disabled={page >= totalPages - 1 || loading}
                        onClick={() => setPage(p => p + 1)}
                        style={{ padding: '8px 16px', borderRadius: '4px', border: '1px solid #ccc', cursor: page >= totalPages - 1 ? 'not-allowed' : 'pointer', opacity: page >= totalPages - 1 ? 0.5 : 1 }}
                    >
                        Next
                    </button>
                </div>
            )}
        </div>
    );
};

export default GymList;
