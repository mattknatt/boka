import React, { useState, useEffect, useCallback } from 'react';
import { useToast } from '../Toast';
import ClassFormModal from './ClassFormModal';
import type { AdminClass, ClassType, Instructor, Gym } from './ClassFormModal';

const STATUS_FILTERS = ['ALL', 'SCHEDULED', 'FULL', 'CANCELLED', 'COMPLETED'];

const STATUS_COLORS: Record<string, { bg: string; color: string }> = {
  SCHEDULED:  { bg: '#e8f5e9', color: '#2e7d32' },
  FULL:       { bg: '#fff3e0', color: '#ef6c00' },
  CANCELLED:  { bg: '#ffebee', color: '#c62828' },
  COMPLETED:  { bg: '#f3e5f5', color: '#6a1b9a' },
};

interface PageResponse<T> {
  content: T[];
  totalPages: number;
  number: number;
}

const AdminDashboard: React.FC = () => {
  const toast = useToast();

  const [classes, setClasses] = useState<AdminClass[]>([]);
  const [classTypes, setClassTypes] = useState<ClassType[]>([]);
  const [instructors, setInstructors] = useState<Instructor[]>([]);
  const [gyms, setGyms] = useState<Gym[]>([]);

  const [statusFilter, setStatusFilter] = useState('ALL');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  const [showForm, setShowForm] = useState(false);
  const [editingClass, setEditingClass] = useState<AdminClass | null>(null);
  const [confirmCancelId, setConfirmCancelId] = useState<number | null>(null);

  const fetchClasses = useCallback(async () => {
    setLoading(true);
    try {
      const status = statusFilter === 'ALL' ? '' : `&status=${statusFilter}`;
      const res = await fetch(`/api/admin/classes?page=${page}&size=15${status}`);
      if (!res.ok) throw new Error();
      const data: PageResponse<AdminClass> = await res.json();
      setClasses(data.content);
      setTotalPages(data.totalPages);
    } catch {
      toast.error('Failed to load classes.');
    } finally {
      setLoading(false);
    }
  }, [page, statusFilter, toast]);

  useEffect(() => { fetchClasses(); }, [fetchClasses]);

  useEffect(() => {
    Promise.all([
      fetch('/api/admin/class-types').then(r => r.json()),
      fetch('/api/admin/instructors').then(r => r.json()),
      fetch('/api/gyms?size=100').then(r => r.json()),
    ]).then(([cts, insts, gymsData]) => {
      setClassTypes(cts);
      setInstructors(insts);
      setGyms(gymsData.content ?? gymsData);
    }).catch(() => toast.error('Failed to load reference data.'));
  }, [toast]);

  const handleCancelClass = async (id: number) => {
    setConfirmCancelId(null);
    try {
      const res = await fetch(`/api/admin/classes/${id}`, { method: 'DELETE' });
      if (res.ok) {
        toast.success('Class cancelled.');
        fetchClasses();
      } else {
        const data = await res.json();
        toast.error(data.message || 'Failed to cancel class.');
      }
    } catch {
      toast.error('An error occurred.');
    }
  };

  const openCreate = () => { setEditingClass(null); setShowForm(true); };
  const openEdit = (c: AdminClass) => { setEditingClass(c); setShowForm(true); };

  const handleFilterChange = (f: string) => { setStatusFilter(f); setPage(0); };

  const fmt = (iso: string) =>
    new Date(iso).toLocaleString('sv-SE', { weekday: 'short', day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });

  return (
    <div style={{ textAlign: 'left' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2 style={{ margin: 0, fontSize: '1.5rem' }}>Class Management</h2>
        <button className="btn-primary" style={{ width: 'auto', padding: '8px 20px' }} onClick={openCreate}>
          + Create Class
        </button>
      </div>

      {/* Status filter tabs */}
      <div style={{ display: 'flex', gap: '8px', marginBottom: '20px', flexWrap: 'wrap' }}>
        {STATUS_FILTERS.map(f => (
          <button
            key={f}
            onClick={() => handleFilterChange(f)}
            style={{
              padding: '6px 14px', borderRadius: '20px', border: '1px solid',
              cursor: 'pointer', fontWeight: 500, fontSize: '0.85rem',
              borderColor: statusFilter === f ? '#ff1493' : '#ddd',
              backgroundColor: statusFilter === f ? '#ff1493' : 'white',
              color: statusFilter === f ? 'white' : '#555',
            }}
          >
            {f === 'ALL' ? 'All' : f.charAt(0) + f.slice(1).toLowerCase()}
          </button>
        ))}
      </div>

      {loading ? (
        <p style={{ color: '#888' }}>Loading...</p>
      ) : classes.length === 0 ? (
        <p style={{ color: '#888' }}>No classes found.</p>
      ) : (
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #eee', textAlign: 'left', color: '#555' }}>
                {['Type', 'Instructor', 'Gym', 'Start', 'Cap', 'Booked', 'Status', ''].map(h => (
                  <th key={h} style={{ padding: '8px 12px', fontWeight: 600 }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {classes.map(c => {
                const colors = STATUS_COLORS[c.status] ?? { bg: '#f5f5f5', color: '#333' };
                return (
                  <tr key={c.id} style={{ borderBottom: '1px solid #f0f0f0' }}>
                    <td style={{ padding: '10px 12px', fontWeight: 500 }}>{c.classTypeName}</td>
                    <td style={{ padding: '10px 12px' }}>{c.instructorName}</td>
                    <td style={{ padding: '10px 12px' }}>{c.gymName ?? '—'}</td>
                    <td style={{ padding: '10px 12px', whiteSpace: 'nowrap' }}>{fmt(c.startTime)}</td>
                    <td style={{ padding: '10px 12px' }}>{c.capacity}</td>
                    <td style={{ padding: '10px 12px' }}>{c.currentBookings}</td>
                    <td style={{ padding: '10px 12px' }}>
                      <span style={{
                        padding: '2px 8px', borderRadius: '10px', fontSize: '0.78rem',
                        fontWeight: 600, backgroundColor: colors.bg, color: colors.color,
                      }}>
                        {c.status}
                      </span>
                    </td>
                    <td style={{ padding: '10px 12px', whiteSpace: 'nowrap' }}>
                      {c.status !== 'CANCELLED' && c.status !== 'COMPLETED' && (
                        <>
                          <button
                            onClick={() => openEdit(c)}
                            style={{ marginRight: '8px', padding: '4px 10px', borderRadius: '4px', border: '1px solid #ccc', background: 'white', cursor: 'pointer', fontSize: '0.82rem' }}
                          >
                            Edit
                          </button>
                          {confirmCancelId === c.id ? (
                            <>
                              <button
                                onClick={() => handleCancelClass(c.id)}
                                style={{ marginRight: '4px', padding: '4px 10px', borderRadius: '4px', border: 'none', background: '#ff1493', color: 'white', cursor: 'pointer', fontSize: '0.82rem' }}
                              >
                                Confirm
                              </button>
                              <button
                                onClick={() => setConfirmCancelId(null)}
                                style={{ padding: '4px 10px', borderRadius: '4px', border: '1px solid #ccc', background: 'white', cursor: 'pointer', fontSize: '0.82rem' }}
                              >
                                No
                              </button>
                            </>
                          ) : (
                            <button
                              onClick={() => setConfirmCancelId(c.id)}
                              style={{ padding: '4px 10px', borderRadius: '4px', border: '1px solid #ffb3d0', background: 'white', color: '#d32f2f', cursor: 'pointer', fontSize: '0.82rem' }}
                            >
                              Cancel
                            </button>
                          )}
                        </>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'center', gap: '10px', marginTop: '24px', alignItems: 'center' }}>
          <button
            disabled={page === 0} onClick={() => setPage(p => p - 1)}
            style={{ padding: '6px 16px', borderRadius: '4px', border: '1px solid #ccc', background: 'white', cursor: page === 0 ? 'not-allowed' : 'pointer', opacity: page === 0 ? 0.5 : 1 }}
          >
            Previous
          </button>
          <span style={{ fontSize: '0.9rem', color: '#555' }}>Page {page + 1} of {totalPages}</span>
          <button
            disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}
            style={{ padding: '6px 16px', borderRadius: '4px', border: '1px solid #ccc', background: 'white', cursor: page >= totalPages - 1 ? 'not-allowed' : 'pointer', opacity: page >= totalPages - 1 ? 0.5 : 1 }}
          >
            Next
          </button>
        </div>
      )}

      {showForm && (
        <ClassFormModal
          editingClass={editingClass}
          classTypes={classTypes}
          instructors={instructors}
          gyms={gyms}
          onClose={() => setShowForm(false)}
          onSaved={fetchClasses}
        />
      )}
    </div>
  );
};

export default AdminDashboard;
