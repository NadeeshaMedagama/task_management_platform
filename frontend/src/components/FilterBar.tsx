'use client';

import { TaskFilters, TaskStatus, Priority, SortBy, Direction } from '@/types';
import { Filter, SortAsc, SortDesc } from 'lucide-react';

interface FilterBarProps {
  filters: TaskFilters;
  onChange: (filters: Partial<TaskFilters>) => void;
}

export default function FilterBar({ filters, onChange }: FilterBarProps) {
  return (
    <div className="flex flex-wrap items-center gap-3 bg-white border border-gray-200 rounded-xl p-4 shadow-sm">
      <div className="flex items-center gap-1.5 text-sm font-medium text-gray-500">
        <Filter className="h-4 w-4" />
        <span>Filters</span>
      </div>

      {/* Status Filter */}
      <select
        value={filters.status || ''}
        onChange={(e) => onChange({ status: (e.target.value as TaskStatus) || undefined, page: 0 })}
        className="text-sm border border-gray-200 rounded-lg px-3 py-1.5 bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
      >
        <option value="">All Statuses</option>
        <option value="TODO">To Do</option>
        <option value="IN_PROGRESS">In Progress</option>
        <option value="DONE">Done</option>
      </select>

      {/* Priority Filter */}
      <select
        value={filters.priority || ''}
        onChange={(e) => onChange({ priority: (e.target.value as Priority) || undefined, page: 0 })}
        className="text-sm border border-gray-200 rounded-lg px-3 py-1.5 bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
      >
        <option value="">All Priorities</option>
        <option value="LOW">Low</option>
        <option value="MEDIUM">Medium</option>
        <option value="HIGH">High</option>
      </select>

      {/* Sort By */}
      <select
        value={filters.sortBy}
        onChange={(e) => onChange({ sortBy: e.target.value as SortBy, page: 0 })}
        className="text-sm border border-gray-200 rounded-lg px-3 py-1.5 bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
      >
        <option value="createdAt">Sort: Created At</option>
        <option value="dueDate">Sort: Due Date</option>
        <option value="priority">Sort: Priority</option>
      </select>

      {/* Sort Direction */}
      <button
        onClick={() => onChange({ direction: filters.direction === 'asc' ? 'desc' : 'asc', page: 0 })}
        className="flex items-center gap-1.5 text-sm border border-gray-200 rounded-lg px-3 py-1.5 bg-white text-gray-700 hover:bg-gray-50 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        {filters.direction === 'asc'
          ? <><SortAsc className="h-4 w-4" /> Ascending</>
          : <><SortDesc className="h-4 w-4" /> Descending</>
        }
      </button>

      {/* Clear Filters */}
      {(filters.status || filters.priority) && (
        <button
          onClick={() => onChange({ status: undefined, priority: undefined, page: 0 })}
          className="text-sm text-red-500 hover:text-red-700 font-medium hover:underline transition-colors"
        >
          Clear filters
        </button>
      )}
    </div>
  );
}

