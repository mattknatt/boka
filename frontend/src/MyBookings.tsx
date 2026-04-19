import React, { useState, useEffect } from 'react';
import { useToast } from './Toast';

interface UserBooking {
    bookingId: number;
    gymClassId: number;
    classTypeName: string;
    startTime: string;
    gymName: string;
    status: string;
}

interface MyBookingsProps {
    onCancelSuccess: () => void;
}

const MyBookings: React.FC<MyBookingsProps> = ({ onCancelSuccess }) => {
    const toast = useToast();
    const [bookings, setBookings] = useState<UserBooking[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [cancellingId, setCancellingId] = useState<number | null>(null);
    const [confirmingId, setConfirmingId] = useState<number | null>(null);

    const fetchBookings = async () => {
        setLoading(true);
        try {
            const response = await fetch('/api/bookings/my');
            if (response.ok) {
                const data = await response.json();
                setBookings(data);
            } else {
                setError('Failed to load bookings.');
            }
        } catch {
            setError('An error occurred while loading bookings.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchBookings();
    }, []);

    const handleCancel = async (bookingId: number, gymClassId: number) => {
        setConfirmingId(null);
        setCancellingId(bookingId);
        try {
            const response = await fetch(`/api/bookings/${gymClassId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                toast.success('Booking cancelled.');
                fetchBookings();
                onCancelSuccess();
            } else {
                toast.error('Failed to cancel booking.');
            }
        } catch {
            toast.error('An error occurred while cancelling.');
        } finally {
            setCancellingId(null);
        }
    };

    if (loading) return <p>Loading your bookings...</p>;
    if (error) return <p style={{ color: 'red' }}>{error}</p>;

    return (
        <div className="my-bookings">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '20px' }}>My Bookings</h2>
            {bookings.length === 0 ? (
                <p>You haven't booked any classes yet.</p>
            ) : (
                <ul style={{
                    listStyle: 'none',
                    padding: 0,
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
                    gap: '20px'
                }}>
                    {bookings.map((booking) => (
                        <li key={booking.bookingId} style={{
                            border: '1px solid #eee',
                            padding: '20px',
                            borderRadius: '12px',
                            textAlign: 'left',
                            boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                            backgroundColor: '#fff',
                            color: '#333'
                        }}>
                            <h3 style={{ marginTop: 0, color: '#ff1493' }}>{booking.classTypeName}</h3>
                            <p><strong>Gym:</strong> {booking.gymName}</p>
                            <p><strong>Time:</strong> {new Date(booking.startTime).toLocaleString('sv-SE', {
                                weekday: 'long',
                                day: 'numeric',
                                month: 'short',
                                hour: '2-digit',
                                minute: '2-digit'
                            })}</p>
                            {confirmingId === booking.bookingId ? (
                                <div style={{ display: 'flex', gap: '8px', marginTop: '15px' }}>
                                    <button
                                        onClick={() => handleCancel(booking.bookingId, booking.gymClassId)}
                                        style={{ flex: 1, padding: '10px', borderRadius: '6px', border: 'none', backgroundColor: '#ff1493', color: 'white', fontWeight: '600', cursor: 'pointer' }}
                                    >
                                        Yes, cancel
                                    </button>
                                    <button
                                        onClick={() => setConfirmingId(null)}
                                        style={{ flex: 1, padding: '10px', borderRadius: '6px', border: '1px solid #ccc', backgroundColor: 'white', color: '#333', fontWeight: '600', cursor: 'pointer' }}
                                    >
                                        Keep
                                    </button>
                                </div>
                            ) : (
                                <button
                                    onClick={() => setConfirmingId(booking.bookingId)}
                                    disabled={cancellingId === booking.bookingId}
                                    style={{
                                        marginTop: '15px',
                                        width: '100%',
                                        padding: '10px',
                                        borderRadius: '6px',
                                        border: '2px solid #ff1493',
                                        backgroundColor: 'transparent',
                                        color: '#ff1493',
                                        fontWeight: '600',
                                        cursor: cancellingId === booking.bookingId ? 'not-allowed' : 'pointer',
                                        transition: 'all 0.2s',
                                        opacity: cancellingId === booking.bookingId ? 0.7 : 1
                                    }}
                                >
                                    {cancellingId === booking.bookingId ? 'Cancelling...' : 'Cancel Booking'}
                                </button>
                            )}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default MyBookings;
