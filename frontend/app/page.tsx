/**
 * Home Page - Categories Overview
 */

'use client';

import Image from 'next/image';
import { useLinkStore } from '../src/store/useLinkStore';
import CategoryCard from '../src/components/CategoryCard';
import InsertLinkModal from '../src/components/modals/InsertLinkModal';

export default function HomePage() {
  const {
    getCategoriesWithEmojis,
    isInsertLinkModalOpen,
    openInsertLinkModal,
    closeInsertLinkModal,
  } = useLinkStore();

  const displayCategories = getCategoriesWithEmojis().filter((c) => c.is_visible);

  return (
    <div className="min-h-screen bg-[#F5F3F7] pb-28">
      {/* Top welcome section */}
      <div className="px-6 pt-6">
        <div className="flex items-start justify-between gap-8">
          {/* Left Column - Text */}
          <div className="flex-1 min-w-0">
            <p className="text-gray-700 text-2xl leading-tight">Hello,</p>
            <p className="text-gray-700 text-2xl leading-tight mt-1">
              <span className="font-bold text-[#7A3E93]">Shir A.</span> 👋
            </p>
            <p className="mt-3 text-gray-500 text-sm">Your Top Categories:</p>
          </div>

          {/* Right Column - Illustration */}
          <div className="relative w-44 h-44 sm:w-56 sm:h-56 md:w-64 md:h-64 shrink-0">
            <Image
              src="/assets/images/ill-welcome.svg"
              alt="Welcome illustration"
              fill
              priority
              className="object-contain"
            />
          </div>
        </div>
      </div>

      {/* Categories Grid */}
      <div className="px-6 mt-6 space-y-4">
        {displayCategories.length === 0 ? (
          <div className="text-center py-10">
            <p className="text-gray-500 mb-4">No categories yet.</p>
            <button
              onClick={() => openInsertLinkModal()}
              className="px-6 py-3 bg-gradient-to-br from-[#7A3E93] to-[#AC5FCF] text-white rounded-[24px] hover:opacity-95 transition-opacity shadow-sm"
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
