import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Proxy API requests to backend (optional - frontend uses absolute URLs from NEXT_PUBLIC_API_URL)
  // This allows using relative URLs like /api/v1/... which will be proxied to the backend
  async rewrites() {
    // Get backend URL from environment variable (set in Vercel/Render)
    // Fallback to localhost for local development
    const backendUrl = process.env.NEXT_PUBLIC_API_URL || 'http://127.0.0.1:8000';
    
    // Only proxy /api/v1/* to the backend. Do NOT rewrite /api/auth/* — that is
    // handled by Next.js (NextAuth) in app/api/auth/[...nextauth]/route.ts.
    if (backendUrl) {
      return [
        {
          source: '/api/v1/:path*',
          destination: `${backendUrl}/api/v1/:path*`,
        },
      ];
    }
    
    return [];
  },
};

export default nextConfig;
