/**
 * Login Page
 */

'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useState } from 'react';
import { User, Key, Eye, EyeOff } from 'lucide-react';
import { signIn } from 'next-auth/react';

export default function LoginPage() {
  const [showPassword, setShowPassword] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  return (
    <div className="min-h-screen bg-white flex flex-col">
      {/* Top Illustration */}
      <div className="relative w-full h-72 mt-6 px-6">
        <Image
          src="/assets/images/ill-login.svg"
          alt="Login illustration"
          fill
          priority
          className="object-contain"
        />
      </div>

      {/* Content */}
      <div className="flex-1 px-8 pb-10 max-w-md w-full mx-auto">
        <h1 className="mt-2 text-[#7A3E93] font-bold text-3xl">Welcome Back</h1>

        <div className="mt-10 space-y-6">
          {/* Username */}
          <div>
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
          </div>

          {/* Password */}
          <div>
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
                {showPassword ? (
                  <EyeOff className="w-5 h-5" />
                ) : (
                  <Eye className="w-5 h-5" />
                )}
              </button>
            </div>
            <div className="mt-2 text-right">
              <Link
                href="/forgot-password"
                className="text-sm text-gray-500 hover:text-[#7A3E93]"
              >
                Forgot Password?
              </Link>
            </div>
          </div>

          {/* Primary Button */}
          <button
            type="button"
            className="w-full py-4 rounded-[32px] font-semibold text-white bg-gradient-to-br from-[#7A3E93] to-[#AC5FCF] hover:opacity-95 transition-opacity"
          >
            Login
          </button>

          {/* Divider */}
          <div className="relative my-6">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-200" />
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="bg-white px-4 text-gray-500">Or continue with</span>
            </div>
          </div>

          {/* Social Login */}
          <div className="flex justify-center gap-4">
            <button
              type="button"
              onClick={() => signIn('google')}
              className="w-12 h-12 rounded-full bg-white border border-gray-200 flex items-center justify-center hover:shadow-sm transition-shadow"
              aria-label="Continue with Google"
            >
              <Image src="/assets/images/icon-google.svg" alt="Google" width={22} height={22} />
            </button>
            <button
              type="button"
              className="w-12 h-12 rounded-full bg-white border border-gray-200 flex items-center justify-center hover:shadow-sm transition-shadow"
              aria-label="Continue with Facebook"
            >
              <Image src="/assets/images/icon-facebook.svg" alt="Facebook" width={22} height={22} />
            </button>
            <button
              type="button"
              className="w-12 h-12 rounded-full bg-white border border-gray-200 flex items-center justify-center hover:shadow-sm transition-shadow"
              aria-label="Continue with Instagram"
            >
              <Image src="/assets/images/icon-instagram.svg" alt="Instagram" width={22} height={22} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
