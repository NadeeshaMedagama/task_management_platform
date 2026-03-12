'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { useTasks } from '@/hooks/useTasks';
import { Task, TaskFilters, TaskRequest, TaskStatus } from '@/types';
import Navbar from '@/components/Navbar';
import TaskCard from '@/components/TaskCard';
import TaskForm from '@/components/TaskForm';
import FilterBar from '@/components/FilterBar';
import Pagination from '@/components/Pagination';
import LoadingSpinner from '@/components/LoadingSpinner';
import apiClient from '@/lib/axios';
import toast from 'react-hot-toast';
import { Plus, ListTodo, AlertCircle } from 'lucide-react';

export default function TasksPage() {
  const { user, isLoading: authLoading, isAdmin } = useAuth();
  const router = useRouter();

  const [filters, setFilters] = useState<TaskFilters>({
    status: undefined,
    priority: undefined,
    page: 0,
    size: 12,
    sortBy: 'createdAt',
    direction: 'desc',
  });

  const [showForm, setShowForm] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);

  const { data, isLoading, error, refetch } = useTasks(filters);

  // Redirect if not logged in
  useEffect(() => {
    if (!authLoading && !user) {
      router.replace('/login');
    }
  }, [user, authLoading, router]);

  if (authLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!user) return null;

  const handleFilterChange = (newFilters: Partial<TaskFilters>) => {
    setFilters((prev) => ({ ...prev, ...newFilters }));
  };

  const handleCreate = async (taskData: TaskRequest) => {
    await apiClient.post('/tasks', taskData);
    toast.success('Task created!');
    setShowForm(false);
    refetch();
  };

  const handleUpdate = async (taskData: TaskRequest) => {
    if (!editingTask) return;
    await apiClient.put(`/tasks/${editingTask.id}`, taskData);
    toast.success('Task updated!');
    setEditingTask(null);
    refetch();
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this task?')) return;
    try {
      await apiClient.delete(`/tasks/${id}`);
      toast.success('Task deleted.');
      refetch();
    } catch {
      toast.error('Failed to delete task.');
    }
  };

  const handleStatusChange = async (id: number, status: TaskStatus) => {
    try {
      await apiClient.patch(`/tasks/${id}/status`, { status });
      toast.success(`Moved to ${status.replace('_', ' ')}`);
      refetch();
    } catch {
      toast.error('Failed to update status.');
    }
  };

  const canModifyTask = (task: Task) =>
    isAdmin || task.ownerId === user.userId;

  const tasks = data?.content ?? [];
  const todoCount = tasks.filter((t) => t.status === 'TODO').length;
  const inProgressCount = tasks.filter((t) => t.status === 'IN_PROGRESS').length;
  const doneCount = tasks.filter((t) => t.status === 'DONE').length;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Page header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
              <ListTodo className="h-6 w-6 text-blue-600" />
              {isAdmin ? 'All Tasks' : 'My Tasks'}
            </h1>
            <p className="text-sm text-gray-500 mt-1">
              {isAdmin
                ? 'Viewing all tasks across all users'
                : 'Manage and track your personal tasks'}
            </p>
          </div>
          <button
            onClick={() => { setEditingTask(null); setShowForm(true); }}
            className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2.5 rounded-xl font-medium text-sm hover:bg-blue-700 transition-colors shadow-sm"
          >
            <Plus className="h-4 w-4" />
            New Task
          </button>
        </div>

        {/* Stats bar */}
        {data && (
          <div className="grid grid-cols-3 gap-4 mb-6">
            {[
              { label: 'To Do', count: todoCount, color: 'bg-gray-100 text-gray-700' },
              { label: 'In Progress', count: inProgressCount, color: 'bg-yellow-100 text-yellow-700' },
              { label: 'Done', count: doneCount, color: 'bg-green-100 text-green-700' },
            ].map(({ label, count, color }) => (
              <div key={label} className={`rounded-xl px-4 py-3 flex items-center justify-between ${color}`}>
                <span className="text-sm font-medium">{label}</span>
                <span className="text-lg font-bold">{count}</span>
              </div>
            ))}
          </div>
        )}

        {/* Filters */}
        <div className="mb-6">
          <FilterBar filters={filters} onChange={handleFilterChange} />
        </div>

        {/* Task Grid */}
        {isLoading ? (
          <div className="flex items-center justify-center py-24">
            <LoadingSpinner size="lg" />
          </div>
        ) : error ? (
          <div className="flex flex-col items-center justify-center py-24 text-center">
            <AlertCircle className="h-12 w-12 text-red-400 mb-3" />
            <p className="text-gray-700 font-medium">{error}</p>
            <button onClick={refetch} className="mt-3 text-sm text-blue-600 hover:underline">
              Try again
            </button>
          </div>
        ) : tasks.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-24 text-center">
            <div className="w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center mb-4">
              <ListTodo className="h-8 w-8 text-blue-400" />
            </div>
            <h3 className="text-gray-700 font-semibold text-lg">No tasks found</h3>
            <p className="text-gray-400 text-sm mt-1">
              {filters.status || filters.priority
                ? 'Try adjusting your filters'
                : 'Create your first task to get started'}
            </p>
            {!filters.status && !filters.priority && (
              <button
                onClick={() => { setEditingTask(null); setShowForm(true); }}
                className="mt-4 flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-xl text-sm font-medium hover:bg-blue-700 transition-colors"
              >
                <Plus className="h-4 w-4" /> Create Task
              </button>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {tasks.map((task) => (
              <TaskCard
                key={task.id}
                task={task}
                onEdit={(t) => { setEditingTask(t); setShowForm(true); }}
                onDelete={handleDelete}
                onStatusChange={handleStatusChange}
                canModify={canModifyTask(task)}
                showOwner={isAdmin}
              />
            ))}
          </div>
        )}

        {/* Pagination */}
        {data && (
          <Pagination
            currentPage={data.number}
            totalPages={data.totalPages}
            totalElements={data.totalElements}
            pageSize={data.size}
            onPageChange={(page) => handleFilterChange({ page })}
          />
        )}
      </main>

      {/* Task Form Modal */}
      {showForm && (
        <TaskForm
          task={editingTask}
          onSubmit={editingTask ? handleUpdate : handleCreate}
          onClose={() => { setShowForm(false); setEditingTask(null); }}
        />
      )}
    </div>
  );
}

