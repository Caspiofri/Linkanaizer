/**
 * Home Page - Categories Overview
 */

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useLinkStore } from '@/src/store/useLinkStore';
import CategoryCard from '@/src/components/CategoryCard';
import InsertLinkModal from '@/src/components/modals/InsertLinkModal';

export default function HomePage() {
  const router = useRouter();
  const { getCategoriesWithEmojis, isInsertLinkModalOpen, openInsertLinkModal, closeInsertLinkModal } = useLinkStore();

  // Only show user-entered categories, enriched with emojis from links
  const displayCategories = getCategoriesWithEmojis();

  return (
    <div className="min-h-screen bg-[#f5f3f7] pb-20">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 p-4 flex items-center justify-between">
        <h1 className="text-[#7A3E93] font-bold text-2xl">LinkClassify</h1>
        <button
          onClick={() => router.push('/category/new')}
          className="px-4 py-2 bg-[#7A3E93] text-white rounded-[32px] hover:bg-[#6a3580] transition-colors text-sm font-medium"
        >
          New Category
        </button>
      </div>

      {/* Categories Grid */}
      <div className="p-4 space-y-4">
        {displayCategories.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-500 mb-4">No categories yet.</p>
            <button
              onClick={() => openInsertLinkModal()}
              className="px-6 py-3 bg-[#7A3E93] text-white rounded-[32px] hover:bg-[#6a3580] transition-colors"
            >
              Add Your First Link
            </button>
          </div>
        ) : (
          displayCategories.map((category) => (
            <CategoryCard key={category.id} category={category} />
          ))
        )}
      </div>

      {/* Insert Link Modal */}
      <InsertLinkModal
        isOpen={isInsertLinkModalOpen}
        onClose={() => {
          closeInsertLinkModal();
        }}
      />
    </div>
  );
}
