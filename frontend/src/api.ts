import type { ItemStatus, WorkItem, WorkItemInput } from './types';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`/api${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...init,
  });
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      if (body?.error) message = body.error;
    } catch {
      // keep the default message
    }
    throw new Error(message);
  }
  return res.status === 204 ? (undefined as T) : ((await res.json()) as T);
}

export const api = {
  list: (status?: ItemStatus) =>
    request<WorkItem[]>(`/items${status ? `?status=${status}` : ''}`),
  create: (input: WorkItemInput) =>
    request<WorkItem>('/items', { method: 'POST', body: JSON.stringify(input) }),
  update: (id: number, input: WorkItemInput) =>
    request<WorkItem>(`/items/${id}`, { method: 'PUT', body: JSON.stringify(input) }),
  remove: (id: number) => request<void>(`/items/${id}`, { method: 'DELETE' }),
  analyze: (id: number) => request<WorkItem>(`/items/${id}/ai/analyze`, { method: 'POST' }),
};
