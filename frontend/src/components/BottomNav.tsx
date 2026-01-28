/**
 * Sticky Bottom Navigation Bar
 */

'use client';

import { useState, useEffect } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { Plus, Upload, Link as LinkIcon } from 'lucide-react';
import { useLinkStore } from '@/src/store/useLinkStore';

export default function BottomNav() {
  const pathname = usePathname();
  const router = useRouter();
  const { openInsertLinkModal } = useLinkStore();
  const [mounted, setMounted] = useState(false);

  // Only render after client-side hydration to avoid hydration mismatch
  useEffect(() => {
    setMounted(true);
  }, []);

  const handleAddClick = (e: React.MouseEvent) => {
    e.preventDefault();
    // Navigate to import-link page which will open the modal
    router.push('/import-link');
  };

  const navItems = [
    {
      href: '/',
      icon: Plus,
      svgIcon: '/assets/images/icon-nav-plus.svg',
      label: 'Add',
      onClick: handleAddClick,
      isSpecial: true, // Special handling for Add button
    },
    {
      href: '/import',
      icon: Upload,
      svgIcon: '/assets/images/icon-nav-upload.svg',
      label: 'Import',
    },
    {
      href: '/links',
      icon: LinkIcon,
      svgIcon: '/assets/images/icon-nav-link.svg',
      label: 'Links',
    },
  ];

  // Don't render until mounted to avoid hydration mismatch
  if (!mounted) {
    return null;
  }

  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 shadow-lg z-50">
      <div className="flex justify-around items-center h-16 px-4">
        {navItems.map((item) => {
          const isActive = pathname === item.href;
          const Icon = item.icon;

          if (item.isSpecial && item.onClick) {
            return (
              <button
                key={item.href}
                onClick={item.onClick}
                className={`flex flex-col items-center justify-center flex-1 h-full transition-colors ${
                  isActive ? 'text-[#7A3E93]' : 'text-gray-500'
                }`}
              >
                <div className="relative w-6 h-6 mb-1 flex items-center justify-center">
                  <Icon className="w-6 h-6" />
                </div>
                <span className="text-xs font-medium">{item.label}</span>
              </button>
            );
          }

          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex flex-col items-center justify-center flex-1 h-full transition-colors ${
                isActive ? 'text-[#7A3E93]' : 'text-gray-500'
              }`}
            >
              <div className="relative w-6 h-6 mb-1 flex items-center justify-center">
                <Icon className="w-6 h-6" />
              </div>
              <span className="text-xs font-medium">{item.label}</span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
