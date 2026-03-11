'use client';

import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';
import { LogOut, CheckSquare, User } from 'lucide-react';

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();

  return (
    <nav className="bg-white border-b border-gray-200 shadow-sm sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link href="/tasks" className="flex items-center gap-2 font-bold text-xl text-blue-600 hover:text-blue-700 transition-colors">
            <CheckSquare className="h-6 w-6" />
            <span>TaskFlow</span>
          </Link>

          {/* Nav items */}
          {user && (
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2 text-sm text-gray-600">
                <User className="h-4 w-4" />
                <span className="font-medium">{user.username}</span>
                <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${
                  isAdmin ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'
                }`}>
                  {user.role}
                </span>
              </div>
              <button
                onClick={logout}
                className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-red-600 transition-colors px-3 py-1.5 rounded-lg hover:bg-red-50"
              >
                <LogOut className="h-4 w-4" />
                <span>Logout</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}

