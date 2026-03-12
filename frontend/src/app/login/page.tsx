'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import apiClient from '@/lib/axios';
import { AuthUser } from '@/types';
import { CheckSquare, Eye, EyeOff } from 'lucide-react';
import { AxiosError } from 'axios';
import toast from 'react-hot-toast';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const { login } = useAuth();
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (!username || !password) {
      setError('Please fill in all fields.');
      return;
    }
    setIsLoading(true);
    try {
      const { data } = await apiClient.post<AuthUser>('/auth/login', { username, password });
      login(data);
      toast.success(`Welcome back, ${data.username}!`);
      router.push('/tasks');
    } catch (err) {
      const axiosError = err as AxiosError<{ message: string }>;
      setError(axiosError.response?.data?.message || 'Login failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 bg-blue-600 rounded-2xl shadow-lg mb-4">
            <CheckSquare className="h-8 w-8 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Welcome to TaskFlow</h1>
          <p className="text-gray-500 text-sm mt-1">Sign in to manage your tasks</p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-xl border border-gray-100 p-8">
          <h2 className="text-xl font-semibold text-gray-900 mb-6">Sign In</h2>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-4">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Username</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter your username"
                autoComplete="username"
                className="w-full border border-gray-300 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  autoComplete="current-password"
                  className="w-full border border-gray-300 rounded-xl px-4 py-2.5 text-sm pr-10 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-blue-600 text-white font-medium py-2.5 rounded-xl hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2 mt-2"
            >
              {isLoading ? <><LoadingSpinner size="sm" /> Signing in...</> : 'Sign In'}
            </button>
          </form>
        </div>

        <p className="text-center text-sm text-gray-500 mt-6">
          Don&apos;t have an account?{' '}
          <Link href="/register" className="text-blue-600 font-medium hover:underline">
            Create one
          </Link>
        </p>
      </div>
    </div>
  );
}

