import React, { useState, useEffect } from 'react';
import { useToast } from '../Toast';

export interface ClassType {
  id: number;
  name: string;
  defaultCapacity: number;
  durationMinutes: number;
}

export interface Instructor {
  id: number;
  firstName: string;
  lastName: string;
}

export interface Gym {
  id: number;
  name: string;
  address: string;
}

export interface AdminClass {
  id: number;
  classTypeId: number;
  classTypeName: string;
  instructorId: number;
  instructorName: string;
  gymId: number;
  gymName: string;
  startTime: string;
  endTime: string;
  capacity: number;
  currentBookings: number;
  availableSpots: number;
  status: string;
}

interface Props {
  editingClass: AdminClass | null;
  classTypes: ClassType[];
  instructors: Instructor[];
  gyms: Gym[];
  onClose: () => void;
  onSaved: () => void;
}

function toDatetimeLocal(iso: string): string {
  return iso.replace('T', 'T').slice(0, 16);
}

function addMinutes(datetime: string, minutes: number): string {
  if (!datetime) return '';
  const d = new Date(datetime);
  d.setMinutes(d.getMinutes() + minutes);
  return d.toISOString().slice(0, 16);
}

const ClassFormModal: React.FC<Props> = ({ editingClass, classTypes, instructors, gyms, onClose, onSaved }) => {
  const toast = useToast();
  const isEdit = editingClass !== null;

  const [classTypeId, setClassTypeId] = useState<number | ''>(editingClass?.classTypeId ?? '');
  const [instructorId, setInstructorId] = useState<number | ''>(editingClass?.instructorId ?? '');
  const [gymId, setGymId] = useState<number | ''>(editingClass?.gymId ?? '');
  const [startTime, setStartTime] = useState(editingClass ? toDatetimeLocal(editingClass.startTime) : '');
  const [endTime, setEndTime] = useState(editingClass ? toDatetimeLocal(editingClass.endTime) : '');
  const [capacity, setCapacity] = useState<number | ''>(editingClass?.capacity ?? '');
  const [saving, setSaving] = useState(false);

  // Auto-fill capacity and endTime when class type or start time changes (create only)
  useEffect(() => {
    if (isEdit || classTypeId === '') return;
    const ct = classTypes.find(c => c.id === classTypeId);
    if (!ct) return;
    if (!isEdit) setCapacity(ct.defaultCapacity);
    if (startTime) setEndTime(addMinutes(startTime, ct.durationMinutes));
  }, [classTypeId, startTime, isEdit, classTypes]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (classTypeId === '' || instructorId === '' || gymId === '' || capacity === '') return;

    setSaving(true);
    const body = { classTypeId, instructorId, gymId, startTime, endTime, capacity };
    const url = isEdit ? `/api/admin/classes/${editingClass!.id}` : '/api/admin/classes';
    const method = isEdit ? 'PUT' : 'POST';

    try {
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });

      if (res.ok) {
        toast.success(isEdit ? 'Class updated.' : 'Class created.');
        onSaved();
        onClose();
      } else {
        const data = await res.json();
        toast.error(data.message || 'Failed to save class.');
      }
    } catch {
      toast.error('An error occurred.');
    } finally {
      setSaving(false);
    }
  };

  const inputStyle: React.CSSProperties = {
    width: '100%', padding: '8px 10px', border: '1px solid #ddd',
    borderRadius: '6px', boxSizing: 'border-box', fontSize: '0.95rem',
  };
  const labelStyle: React.CSSProperties = {
    display: 'block', marginBottom: '4px', fontWeight: 500, fontSize: '0.85rem', color: '#555',
  };
  const groupStyle: React.CSSProperties = { marginBottom: '14px' };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()} style={{ maxWidth: '480px' }}>
        <div className="modal-header">
          <h2>{isEdit ? 'Edit Class' : 'Create Class'}</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form onSubmit={handleSubmit}>
          <div style={groupStyle}>
            <label style={labelStyle}>Class Type</label>
            <select value={classTypeId} onChange={e => setClassTypeId(Number(e.target.value))} required style={inputStyle}>
              <option value="">Select class type...</option>
              {classTypes.map(ct => <option key={ct.id} value={ct.id}>{ct.name}</option>)}
            </select>
          </div>

          <div style={groupStyle}>
            <label style={labelStyle}>Instructor</label>
            <select value={instructorId} onChange={e => setInstructorId(Number(e.target.value))} required style={inputStyle}>
              <option value="">Select instructor...</option>
              {instructors.map(i => <option key={i.id} value={i.id}>{i.firstName} {i.lastName}</option>)}
            </select>
          </div>

          <div style={groupStyle}>
            <label style={labelStyle}>Gym</label>
            <select value={gymId} onChange={e => setGymId(Number(e.target.value))} required style={inputStyle}>
              <option value="">Select gym...</option>
              {gyms.map(g => <option key={g.id} value={g.id}>{g.name}</option>)}
            </select>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '14px' }}>
            <div>
              <label style={labelStyle}>Start Time</label>
              <input type="datetime-local" value={startTime} onChange={e => setStartTime(e.target.value)} required style={inputStyle} />
            </div>
            <div>
              <label style={labelStyle}>End Time</label>
              <input type="datetime-local" value={endTime} onChange={e => setEndTime(e.target.value)} required style={inputStyle} />
            </div>
          </div>

          <div style={groupStyle}>
            <label style={labelStyle}>Capacity</label>
            <input
              type="number" min={1} value={capacity}
              onChange={e => setCapacity(e.target.value === '' ? '' : Number(e.target.value))}
              required style={inputStyle}
            />
          </div>

          <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
            <button type="button" className="btn-secondary" onClick={onClose} style={{ flex: 1 }}>
              Cancel
            </button>
            <button
              type="submit" className="btn-primary" disabled={saving}
              style={{ flex: 2, opacity: saving ? 0.7 : 1, cursor: saving ? 'not-allowed' : 'pointer' }}
            >
              {saving ? 'Saving...' : (isEdit ? 'Save Changes' : 'Create Class')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ClassFormModal;
