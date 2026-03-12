'use client';

import { useState, useEffect, useCallback } from 'react';
import { Task, PageResponse, TaskFilters } from '@/types';
import apiClient from '@/lib/axios';
import { AxiosError } from 'axios';

export function useTasks(filters: TaskFilters) {
  const [data, setData] = useState<PageResponse<Task> | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTasks = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const params: Record<string, string | number> = {
        page: filters.page,
        size: filters.size,
        sortBy: filters.sortBy,
        direction: filters.direction,
      };
      if (filters.status) params.status = filters.status;
      if (filters.priority) params.priority = filters.priority;

      const response = await apiClient.get<PageResponse<Task>>('/tasks', { params });
      setData(response.data);
    } catch (err) {
      const axiosError = err as AxiosError<{ message: string }>;
      setError(axiosError.response?.data?.message || 'Failed to load tasks.');
    } finally {
      setIsLoading(false);
    }
  }, [filters.page, filters.size, filters.sortBy, filters.direction, filters.status, filters.priority]);

  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  return { data, isLoading, error, refetch: fetchTasks };
}

