/**
 * Import Link Page - Opens InsertLinkModal automatically
 */

'use client';

import { useEffect, useMemo } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useLinkStore } from '@/src/store/useLinkStore';
import InsertLinkModal from '@/src/components/modals/InsertLinkModal';

export default function ImportLinkPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { isInsertLinkModalOpen, openInsertLinkModal, closeInsertLinkModal } = useLinkStore();

  // Extract a URL candidate from ?url= or from within ?text=
  const sharedUrl = useMemo(() => {
    const urlParam = searchParams.get('url') || '';
    const textParam = searchParams.get('text') || '';

    if (urlParam) {
      return urlParam;
    }

    // Try to extract a URL-like substring from text
    const urlRegex = /(https?:\/\/[^\s]+)/i;
    const match = textParam.match(urlRegex);
    return match ? match[0] : '';
  }, [searchParams]);

  useEffect(() => {
    // Open modal when page loads
    openInsertLinkModal();
  }, [openInsertLinkModal]);

  const handleClose = () => {
    closeInsertLinkModal();
    router.push('/');
  };

  return (
    <div className="min-h-screen bg-[#f5f3f7]">
      <InsertLinkModal
        isOpen={isInsertLinkModalOpen}
        onClose={handleClose}
        initialUrl={sharedUrl}
        autoStartOnOpen={!!sharedUrl}
      />
    </div>
  );
}

