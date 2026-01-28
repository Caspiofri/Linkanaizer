/**
 * Login/Welcome Page
 */

'use client';

import { useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { User, Key, Eye, EyeOff } from 'lucide-react';

export default function LoginPage() {
  const [showPassword, setShowPassword] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  return (
    <div className="min-h-screen bg-[#f5f3f7] flex flex-col">
      {/* Header */}
      <div className="p-4">
        <h1 className="text-gray-800 font-semibold">Login</h1>
      </div>

      {/* Illustration */}
      <div className="relative w-full h-64 px-4">
        <Image
          src="/assets/images/ill-login.svg"
          alt="Welcome Illustration"
          fill
          className="object-contain"
        />
      </div>

      {/* Title */}
      <div className="px-6 mt-4">
        <h2 className="text-[#7A3E93] font-bold text-3xl">Welcome Back</h2>
      </div>

      {/* Form */}
      <div className="flex-1 px-6 mt-8 space-y-6">
        {/* Username Input */}
        <div>
          <label className="block text-gray-600 text-sm mb-2">Username</label>
          <div className="relative">
            <User className="absolute left-4 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Username"
              className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-[32px] focus:outline-none focus:ring-2 focus:ring-[#7A3E93]"
            />
          </div>
        </div>

        {/* Password Input */}
        <div>
          <label className="block text-gray-600 text-sm mb-2">Password</label>
          <div className="relative">
            <Key className="absolute left-4 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Password"
              className="w-full pl-12 pr-12 py-3 border border-gray-300 rounded-[32px] focus:outline-none focus:ring-2 focus:ring-[#7A3E93]"
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              {showPassword ? (
                <EyeOff className="w-5 h-5" />
              ) : (
                <Eye className="w-5 h-5" />
              )}
            </button>
          </div>
        </div>

        {/* Forgot Password */}
        <div className="text-right">
          <Link href="/forgot-password" className="text-sm text-[#7A3E93] hover:underline">
            Forgot Password?
          </Link>
        </div>

        {/* Login Button */}
        <button className="w-full py-4 bg-gradient-to-b from-purple-400 to-[#7A3E93] text-white rounded-[32px] font-semibold hover:opacity-90 transition-opacity">
          Login
        </button>

        {/* Social Login */}
        <div className="relative my-6">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-gray-300"></div>
          </div>
          <div className="relative flex justify-center text-sm">
            <span className="bg-[#f5f3f7] px-4 text-gray-500">Or continue with</span>
          </div>
        </div>

        <div className="flex justify-center gap-4">
          <button className="w-12 h-12 rounded-full bg-white border border-gray-300 flex items-center justify-center hover:shadow-md transition-shadow">
            <span className="text-xl font-bold text-blue-600">G</span>
          </button>
          <button className="w-12 h-12 rounded-full bg-blue-600 text-white flex items-center justify-center hover:shadow-md transition-shadow">
            <span className="text-xl font-bold">f</span>
          </button>
          <button className="w-12 h-12 rounded-full bg-gradient-to-br from-purple-600 to-pink-600 text-white flex items-center justify-center hover:shadow-md transition-shadow">
            <span className="text-xl">📷</span>
          </button>
        </div>
      </div>
    </div>
  );
}
