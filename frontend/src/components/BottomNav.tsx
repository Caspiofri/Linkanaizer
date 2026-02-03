/**
 * Sticky Bottom Navigation Bar
 */

'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Plus, Upload, Link as LinkIcon } from 'lucide-react';

export default function BottomNav() {
  const pathname = usePathname();

  const navItems = [
    {
      href: '/category/new',
      icon: Plus,
      label: 'New Category',
    },
    {
      href: '/import',
      icon: Upload,
      label: 'Import File',
    },
    {
      href: '/import-link',
      icon: LinkIcon,
      label: 'Insert Link',
    },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 shadow-lg z-50">
      <div className="flex justify-around items-center h-20 px-6">
        {navItems.map((item) => {
          const isActive = pathname === item.href;
          const Icon = item.icon;

          return (
            <Link
              key={item.href}
              href={item.href}
              className="flex flex-col items-center justify-center min-h-[48px] min-w-[48px] touch-manipulation"
              aria-label={item.label}
            >
              <div
                className={`w-14 h-14 min-w-[56px] min-h-[56px] rounded-[24px] flex items-center justify-center shadow-sm transition-transform active:scale-[0.98] bg-gradient-to-br from-[#7A3E93] to-[#AC5FCF] ${
                  isActive ? 'ring-2 ring-[#7A3E93]/30' : ''
                }`}
              >
                <Icon className="w-6 h-6 text-white" aria-hidden />
              </div>
              <span className="mt-2 text-[11px] font-medium text-gray-700">
                {item.label}
              </span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
