export type ItemStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export const STATUSES: ItemStatus[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
export const PRIORITIES: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

export interface WorkItem {
  id: number;
  title: string;
  description: string;
  requester: string | null;
  status: ItemStatus;
  priority: Priority;
  category: string | null;
  aiSummary: string | null;
  aiDraftReply: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface WorkItemInput {
  title: string;
  description: string;
  requester?: string;
  priority?: Priority;
  status?: ItemStatus;
}
