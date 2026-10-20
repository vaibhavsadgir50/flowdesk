import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import ItemCard from './ItemCard';
import type { WorkItem } from '../types';

const base: WorkItem = {
  id: 1,
  title: 'Printer broken',
  description: 'It shows an error.',
  requester: 'Ann',
  status: 'OPEN',
  priority: 'HIGH',
  category: null,
  aiSummary: null,
  aiDraftReply: null,
  createdAt: '2026-01-01T10:00:00Z',
  updatedAt: '2026-01-01T10:00:00Z',
};

const handlers = { onStatusChange: vi.fn(), onAnalyze: vi.fn(), onDelete: vi.fn() };

describe('ItemCard', () => {
  it('shows the item and triggers AI analysis', async () => {
    render(<ItemCard item={base} busy={false} {...handlers} />);
    expect(screen.getByText('Printer broken')).toBeInTheDocument();
    expect(screen.queryByLabelText('AI assistant output')).not.toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: 'Analyze with AI' }));
    expect(handlers.onAnalyze).toHaveBeenCalledWith(base);
  });

  it('renders AI output when present', () => {
    const analysed = { ...base, category: 'Bug', aiSummary: 'Printer fails', aiDraftReply: 'Hi Ann' };
    render(<ItemCard item={analysed} busy={false} {...handlers} />);
    expect(screen.getByLabelText('AI assistant output')).toHaveTextContent('Printer fails');
    expect(screen.getByText('Bug')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Re-run AI' })).toBeInTheDocument();
  });

  it('disables actions while busy', () => {
    render(<ItemCard item={base} busy={true} {...handlers} />);
    expect(screen.getByRole('button', { name: 'Working…' })).toBeDisabled();
  });
});
