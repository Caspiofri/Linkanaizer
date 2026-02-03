'use client';

import { usePathname } from 'next/navigation';
import BottomNav from './BottomNav';

export default function BottomNavGate() {
  const pathname = usePathname();

  if (pathname.startsWith('/login') || pathname.startsWith('/register')) {
    return null;
  }

  return <BottomNav />;
}

