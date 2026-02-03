/**
 * Import Page - Opens Import File Modal
 */

'use client';

import { useState, useEffect } from 'react';
import ImportFileModal from '../../src/components/modals/ImportFileModal';

export default function ImportPage() {
  const [isModalOpen, setIsModalOpen] = useState(true);

  return (
    <div className="min-h-screen bg-[#f5f3f7]">
      <ImportFileModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} />
    </div>
  );
}
