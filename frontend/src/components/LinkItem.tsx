/**
 * Link Item Component for Category View
 */

'use client';

import { Link } from '@/src/types';
import { ExternalLink } from 'lucide-react';
import Image from 'next/image';

interface LinkItemProps {
  link: Link;
}

export default function LinkItem({ link }: LinkItemProps) {
  return (
    <div className="flex gap-4 p-4 bg-white rounded-[32px] shadow-sm">
      {/* Text Content */}
      <div className="flex-1 min-w-0">
        <h3 className="text-[#7A3E93] font-semibold text-lg mb-2 line-clamp-3">
          {link.title || 'Untitled'}
        </h3>
        <p className="text-gray-600 text-sm mb-2 line-clamp-2">
          {link.summary}
        </p>
        <a
          href={link.url}
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-1 text-gray-500 text-xs hover:text-[#7A3E93] transition-colors"
        >
          <ExternalLink className="w-3 h-3" />
          <span className="truncate">link to video/post</span>
        </a>
      </div>

      {/* Visual Placeholder Card */}
      <div className="flex-shrink-0 w-24 h-24 bg-gray-100 rounded-[32px] shadow-sm flex items-center justify-center">
        <div className="flex gap-1">
          <div className="w-2 h-2 bg-gray-400 rounded-full"></div>
          <div className="w-2 h-2 bg-gray-400 rounded-full"></div>
          <div className="w-2 h-2 bg-gray-400 rounded-full"></div>
        </div>
      </div>
    </div>
  );
}
