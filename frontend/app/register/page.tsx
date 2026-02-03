/**
 * Register Page
 */

'use client';

import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { ArrowLeft, User, Mail, Key, Eye, EyeOff } from 'lucide-react';

export default function RegisterPage() {
  const router = useRouter();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  return (
    <div className="min-h-screen bg-white flex flex-col">
      {/* Top bar */}
      <div className="px-6 pt-6">
        <button
          type="button"
          onClick={() => router.back()}
          className="text-gray-500 hover:text-gray-700"
          aria-label="Back"
        >
          <ArrowLeft className="w-6 h-6" />
        </button>

        <h1 className="mt-6 text-[#7A3E93] font-bold text-3xl">
          Hello and Welcome !
        </h1>
      </div>

      {/* Form */}
      <div className="flex-1 px-8 pt-10 max-w-md w-full mx-auto space-y-6">
        {/* Username */}
        <div className="relative">
          <User className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Username"
            className="w-full pl-12 pr-4 py-4 border border-gray-200 rounded-[32px] focus:outline-none focus:ring-2 focus:ring-[#7A3E93]"
          />
        </div>

        {/* Email */}
        <div className="relative">
          <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="E-Mail"
            className="w-full pl-12 pr-4 py-4 border border-gray-200 rounded-[32px] focus:outline-none focus:ring-2 focus:ring-[#7A3E93]"
          />
        </div>

        {/* Password */}
        <div className="relative">
          <Key className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type={showPassword ? 'text' : 'password'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Password"
            className="w-full pl-12 pr-12 py-4 border border-gray-200 rounded-[32px] focus:outline-none focus:ring-2 focus:ring-[#7A3E93]"
          />
          <button
            type="button"
            onClick={() => setShowPassword((v) => !v)}
            className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            aria-label={showPassword ? 'Hide password' : 'Show password'}
          >
            {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
          </button>
        </div>

        {/* Confirm Password */}
        <div className="relative">
          <Key className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type={showConfirm ? 'text' : 'password'}
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            placeholder="Validate Password"
            className="w-full pl-12 pr-12 py-4 border border-gray-200 rounded-[32px] focus:outline-none focus:ring-2 focus:ring-[#7A3E93]"
          />
          <button
            type="button"
            onClick={() => setShowConfirm((v) => !v)}
            className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            aria-label={showConfirm ? 'Hide password' : 'Show password'}
          >
            {showConfirm ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
          </button>
        </div>

        {/* Primary Button */}
        <button
          type="button"
          className="w-full py-4 rounded-[32px] font-semibold text-white bg-gradient-to-br from-[#7A3E93] to-[#AC5FCF] hover:opacity-95 transition-opacity"
        >
          Register
        </button>
      </div>

      {/* Bottom Illustration */}
      <div className="relative w-full h-64 px-6 pb-8">
        <Image
          src="/assets/images/ill-register-bottom.svg"
          alt="Register illustration"
          fill
          className="object-contain"
          priority
        />
      </div>
    </div>
  );
}

