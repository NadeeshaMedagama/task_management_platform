export type Role = 'USER' | 'ADMIN';
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';
export type SortBy = 'createdAt' | 'dueDate' | 'priority';
export type Direction = 'asc' | 'desc';

export interface User {
  id: number;
  username: string;
  email: string;
  role: Role;
  createdAt: string;
}

export interface AuthUser {
  token: string;
  tokenType: string;
  userId: number;
  username: string;
  email: string;
  role: Role;
}

export interface Task {
  id: number;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: Priority;
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
  ownerId: number;
  ownerUsername: string;
}

export interface TaskRequest {
  title: string;
  description?: string;
  status: TaskStatus;
  priority: Priority;
  dueDate?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: string[];
}

export interface TaskFilters {
  status?: TaskStatus | '';
  priority?: Priority | '';
  page: number;
  size: number;
  sortBy: SortBy;
  direction: Direction;
}

