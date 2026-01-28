/**
 * Import Link Page - Opens InsertLinkModal automatically
 */

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useLinkStore } from '@/src/store/useLinkStore';
import InsertLinkModal from '@/src/components/modals/InsertLinkModal';

export default function ImportLinkPage() {
  const router = useRouter();
  const { isInsertLinkModalOpen, openInsertLinkModal, closeInsertLinkModal } = useLinkStore();

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
      <InsertLinkModal isOpen={isInsertLinkModalOpen} onClose={handleClose} />
    </div>
  );
}
