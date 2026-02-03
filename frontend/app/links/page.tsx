/**
 * All Links Page
 */

'use client';

import { useLinkStore } from '../../src/store/useLinkStore';
import LinkItem from '../../src/components/LinkItem';

export default function LinksPage() {
  const { links } = useLinkStore();

  return (
    <div className="min-h-screen bg-[#f5f3f7] pb-20">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 p-4">
        <h1 className="text-[#7A3E93] font-bold text-2xl">All Links</h1>
      </div>

      {/* Links List */}
      <div className="p-4 space-y-4">
        {links.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-500">No links yet. Add your first link!</p>
          </div>
        ) : (
          links.map((link) => <LinkItem key={link.id} link={link} />)
        )}
      </div>
    </div>
  );
}
