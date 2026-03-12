import { AuthUser } from '@/types';

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

export const saveAuth = (authUser: AuthUser): void => {
  localStorage.setItem(TOKEN_KEY, authUser.token);
  localStorage.setItem(USER_KEY, JSON.stringify(authUser));
};

export const getToken = (): string | null => {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(TOKEN_KEY);
};

export const getAuthUser = (): AuthUser | null => {
  if (typeof window === 'undefined') return null;
  const user = localStorage.getItem(USER_KEY);
  return user ? JSON.parse(user) : null;
};

export const clearAuth = (): void => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
};

export const isAuthenticated = (): boolean => {
  return !!getToken();
};

