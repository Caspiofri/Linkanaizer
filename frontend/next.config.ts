import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Proxy API requests to backend (optional - frontend uses absolute URLs from NEXT_PUBLIC_API_URL)
  // This allows using relative URLs like /api/v1/... which will be proxied to the backend
  async rewrites() {
    // Get backend URL from environment variable (set in Vercel)
    // Fallback to localhost for local development
    const backendUrl = process.env.NEXT_PUBLIC_API_URL || 'http://127.0.0.1:8000';
    
    // Only add rewrites if backend URL is configured
    if (backendUrl) {
      return [
        {
          source: '/api/:path*',
          destination: `${backendUrl}/api/:path*`,
        },
      ];
    }
    
    return [];
  },
};

export default nextConfig;
