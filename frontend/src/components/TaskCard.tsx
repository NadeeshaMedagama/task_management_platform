'use client';

import { Task, TaskStatus, Priority } from '@/types';
import { Calendar, User, Edit, Trash2, CheckCircle2, Clock, Circle } from 'lucide-react';
import { format } from 'date-fns';

interface TaskCardProps {
  task: Task;
  onEdit: (task: Task) => void;
  onDelete: (id: number) => void;
  onStatusChange: (id: number, status: TaskStatus) => void;
  canModify: boolean;
  showOwner?: boolean;
}

const statusConfig: Record<TaskStatus, { label: string; color: string; icon: React.ReactNode }> = {
  TODO: { label: 'To Do', color: 'bg-gray-100 text-gray-700', icon: <Circle className="h-3.5 w-3.5" /> },
  IN_PROGRESS: { label: 'In Progress', color: 'bg-yellow-100 text-yellow-700', icon: <Clock className="h-3.5 w-3.5" /> },
  DONE: { label: 'Done', color: 'bg-green-100 text-green-700', icon: <CheckCircle2 className="h-3.5 w-3.5" /> },
};

const priorityConfig: Record<Priority, { label: string; color: string; dot: string }> = {
  LOW: { label: 'Low', color: 'bg-blue-50 text-blue-600', dot: 'bg-blue-400' },
  MEDIUM: { label: 'Medium', color: 'bg-orange-50 text-orange-600', dot: 'bg-orange-400' },
  HIGH: { label: 'High', color: 'bg-red-50 text-red-600', dot: 'bg-red-500' },
};

export default function TaskCard({ task, onEdit, onDelete, onStatusChange, canModify, showOwner }: TaskCardProps) {
  const status = statusConfig[task.status];
  const priority = priorityConfig[task.priority];
  const isOverdue = task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'DONE';

  const nextStatus: Record<TaskStatus, TaskStatus> = {
    TODO: 'IN_PROGRESS',
    IN_PROGRESS: 'DONE',
    DONE: 'TODO',
  };

  return (
    <div className={`bg-white rounded-xl border shadow-sm hover:shadow-md transition-all duration-200 p-5 flex flex-col gap-3 animate-slide-up ${
      task.status === 'DONE' ? 'opacity-75' : ''
    }`}>
      {/* Header */}
      <div className="flex items-start justify-between gap-2">
        <h3 className={`font-semibold text-gray-900 text-sm leading-snug flex-1 ${task.status === 'DONE' ? 'line-through text-gray-500' : ''}`}>
          {task.title}
        </h3>
        {canModify && (
          <div className="flex items-center gap-1 shrink-0">
            <button
              onClick={() => onEdit(task)}
              className="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
              title="Edit task"
            >
              <Edit className="h-3.5 w-3.5" />
            </button>
            <button
              onClick={() => onDelete(task.id)}
              className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
              title="Delete task"
            >
              <Trash2 className="h-3.5 w-3.5" />
            </button>
          </div>
        )}
      </div>

      {/* Description */}
      {task.description && (
        <p className="text-xs text-gray-500 line-clamp-2 leading-relaxed">{task.description}</p>
      )}

      {/* Badges */}
      <div className="flex flex-wrap items-center gap-2">
        <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${status.color}`}>
          {status.icon}
          {status.label}
        </span>
        <span className={`inline-flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-medium ${priority.color}`}>
          <span className={`h-1.5 w-1.5 rounded-full ${priority.dot}`} />
          {priority.label}
        </span>
      </div>

      {/* Footer */}
      <div className="flex items-center justify-between pt-1 border-t border-gray-50">
        <div className="flex items-center gap-3 text-xs text-gray-400">
          {task.dueDate && (
            <span className={`flex items-center gap-1 ${isOverdue ? 'text-red-500 font-medium' : ''}`}>
              <Calendar className="h-3.5 w-3.5" />
              {format(new Date(task.dueDate), 'MMM d, yyyy')}
              {isOverdue && ' (Overdue)'}
            </span>
          )}
          {showOwner && (
            <span className="flex items-center gap-1">
              <User className="h-3.5 w-3.5" />
              {task.ownerUsername}
            </span>
          )}
        </div>

        {/* Quick status toggle */}
        {canModify && task.status !== 'DONE' && (
          <button
            onClick={() => onStatusChange(task.id, nextStatus[task.status])}
            className="text-xs text-blue-600 hover:text-blue-800 font-medium hover:underline transition-colors"
          >
            → {statusConfig[nextStatus[task.status]].label}
          </button>
        )}
      </div>
    </div>
  );
}

