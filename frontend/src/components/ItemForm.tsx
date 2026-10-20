import { FormEvent, useState } from 'react';
import { PRIORITIES, type Priority, type WorkItemInput } from '../types';

interface Props {
  onSubmit: (input: WorkItemInput) => Promise<void>;
}

export default function ItemForm({ onSubmit }: Props) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [requester, setRequester] = useState('');
  const [priority, setPriority] = useState<Priority>('MEDIUM');
  const [saving, setSaving] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    try {
      await onSubmit({ title, description, requester: requester || undefined, priority });
      setTitle('');
      setDescription('');
      setRequester('');
      setPriority('MEDIUM');
    } finally {
      setSaving(false);
    }
  }

  return (
    <form className="card form" onSubmit={handleSubmit}>
      <h2>New work item</h2>
      <label>
        Title
        <input value={title} onChange={(e) => setTitle(e.target.value)} maxLength={200} required />
      </label>
      <label>
        Description
        <textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          rows={4}
          required
        />
      </label>
      <div className="row">
        <label>
          Requester
          <input value={requester} onChange={(e) => setRequester(e.target.value)} maxLength={120} />
        </label>
        <label>
          Priority
          <select value={priority} onChange={(e) => setPriority(e.target.value as Priority)}>
            {PRIORITIES.map((p) => (
              <option key={p}>{p}</option>
            ))}
          </select>
        </label>
      </div>
      <button type="submit" disabled={saving}>
        {saving ? 'Saving…' : 'Create item'}
      </button>
    </form>
  );
}
