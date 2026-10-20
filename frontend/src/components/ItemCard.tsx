import { STATUSES, type ItemStatus, type WorkItem } from '../types';

interface Props {
  item: WorkItem;
  busy: boolean;
  onStatusChange: (item: WorkItem, status: ItemStatus) => void;
  onAnalyze: (item: WorkItem) => void;
  onDelete: (item: WorkItem) => void;
}

export default function ItemCard({ item, busy, onStatusChange, onAnalyze, onDelete }: Props) {
  return (
    <article className="card item">
      <header>
        <h3>{item.title}</h3>
        <span className={`badge priority-${item.priority.toLowerCase()}`}>{item.priority}</span>
      </header>
      <p className="muted">
        {item.requester ? `From ${item.requester} · ` : ''}
        {new Date(item.createdAt).toLocaleString()}
      </p>
      <p>{item.description}</p>

      {item.aiSummary && (
        <section className="ai" aria-label="AI assistant output">
          <div className="ai-head">
            <strong>AI assistant</strong>
            {item.category && <span className="badge category">{item.category}</span>}
          </div>
          <p>
            <em>Summary:</em> {item.aiSummary}
          </p>
          {item.aiDraftReply && (
            <>
              <em>Draft reply:</em>
              <pre>{item.aiDraftReply}</pre>
            </>
          )}
        </section>
      )}

      <footer>
        <select
          aria-label="Status"
          value={item.status}
          disabled={busy}
          onChange={(e) => onStatusChange(item, e.target.value as ItemStatus)}
        >
          {STATUSES.map((s) => (
            <option key={s} value={s}>
              {s.replace('_', ' ')}
            </option>
          ))}
        </select>
        <button type="button" disabled={busy} onClick={() => onAnalyze(item)}>
          {busy ? 'Working…' : item.aiSummary ? 'Re-run AI' : 'Analyze with AI'}
        </button>
        <button type="button" className="danger" disabled={busy} onClick={() => onDelete(item)}>
          Delete
        </button>
      </footer>
    </article>
  );
}
