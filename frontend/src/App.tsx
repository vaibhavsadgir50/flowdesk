import { useCallback, useEffect, useState } from 'react';
import { api } from './api';
import ItemCard from './components/ItemCard';
import ItemForm from './components/ItemForm';
import { STATUSES, type ItemStatus, type WorkItem, type WorkItemInput } from './types';

export default function App() {
  const [items, setItems] = useState<WorkItem[]>([]);
  const [filter, setFilter] = useState<ItemStatus | ''>('');
  const [busyId, setBusyId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    try {
      setItems(await api.list(filter || undefined));
      setError(null);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    void load();
  }, [load]);

  async function run(id: number | null, action: () => Promise<unknown>) {
    setBusyId(id);
    try {
      await action();
      await load();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusyId(null);
    }
  }

  const create = (input: WorkItemInput) => run(null, () => api.create(input));

  const changeStatus = (item: WorkItem, status: ItemStatus) =>
    run(item.id, () =>
      api.update(item.id, {
        title: item.title,
        description: item.description,
        requester: item.requester ?? undefined,
        priority: item.priority,
        status,
      }),
    );

  const analyze = (item: WorkItem) => run(item.id, () => api.analyze(item.id));
  const remove = (item: WorkItem) => run(item.id, () => api.remove(item.id));

  return (
    <div className="app">
      <header className="top">
        <h1>FlowDesk</h1>
        <p className="muted">AI-assisted workflow manager</p>
      </header>

      {error && (
        <div className="error" role="alert">
          {error}
        </div>
      )}

      <main className="layout">
        <ItemForm onSubmit={create} />

        <section>
          <div className="toolbar">
            <h2>Work items</h2>
            <select
              aria-label="Filter by status"
              value={filter}
              onChange={(e) => setFilter(e.target.value as ItemStatus | '')}
            >
              <option value="">All statuses</option>
              {STATUSES.map((s) => (
                <option key={s} value={s}>
                  {s.replace('_', ' ')}
                </option>
              ))}
            </select>
          </div>

          {loading ? (
            <p className="muted">Loading…</p>
          ) : items.length === 0 ? (
            <p className="muted">No work items yet.</p>
          ) : (
            items.map((item) => (
              <ItemCard
                key={item.id}
                item={item}
                busy={busyId === item.id}
                onStatusChange={changeStatus}
                onAnalyze={analyze}
                onDelete={remove}
              />
            ))
          )}
        </section>
      </main>
    </div>
  );
}
