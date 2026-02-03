/**
 * Category View Page with Search
 */

'use client';

import { useState, useMemo } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { ArrowLeft, Search } from 'lucide-react';
import { useLinkStore } from '../../../src/store/useLinkStore';
import LinkItem from '../../../src/components/LinkItem';

export default function CategoryPage() {
  const params = useParams();
  const router = useRouter();
  const categoryName = (params.categoryName as string) || '';
  const [searchQuery, setSearchQuery] = useState('');

  const { getLinksByCategory, getCategoriesWithEmojis } = useLinkStore();
  const allLinks = getLinksByCategory(categoryName);
  const categoriesWithEmojis = getCategoriesWithEmojis();

  // Filter links by search query
  const filteredLinks = useMemo(() => {
    if (!searchQuery.trim()) return allLinks;

    const query = searchQuery.toLowerCase();
    return allLinks.filter(
      (link) =>
        link.title.toLowerCase().includes(query) ||
        link.summary.toLowerCase().includes(query) ||
        link.url.toLowerCase().includes(query) ||
        link.keywords.some((keyword) => keyword.toLowerCase().includes(query))
    );
  }, [allLinks, searchQuery]);

  // Get category emoji from category list or from first link
  const categoryEmoji = useMemo(() => {
    const category = categoriesWithEmojis.find(
      (c) => c.name.toLowerCase() === categoryName.toLowerCase()
    );
    if (category?.emoji) return category.emoji;
    // Fallback: get emoji from first link in this category
    if (allLinks.length > 0 && allLinks[0].category_emoji) {
      return allLinks[0].category_emoji;
    }
    return null;
  }, [categoriesWithEmojis, categoryName, allLinks]);

  const displayName = categoryName.charAt(0).toUpperCase() + categoryName.slice(1);

  return (
    <div className="min-h-screen bg-[#f5f3f7] pb-20">
      {/* Header */}
      <div className="sticky top-0 bg-white z-10 border-b border-gray-200">
        <div className="flex items-center gap-4 p-4">
          <button
            onClick={() => router.back()}
            className="text-gray-500 hover:text-gray-700"
          >
            <ArrowLeft className="w-6 h-6" />
          </button>
          <h1 className="text-[#7A3E93] font-bold text-2xl flex-1 flex items-center gap-2">
            {categoryEmoji && <span className="text-2xl">{categoryEmoji}</span>}
            <span>{displayName}</span>
          </h1>
        </div>

        {/* Search Bar */}
        <div className="px-4 pb-4">
          <div className="relative">
            <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="search"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by title or summary..."
              className="w-full pl-12 pr-4 py-3 min-h-[48px] bg-white border border-gray-300 rounded-[32px] focus:outline-none focus:ring-2 focus:ring-[#7A3E93] touch-manipulation text-base"
              aria-label="Search links by title or summary"
            />
          </div>
        </div>
      </div>

      {/* Links List */}
      <div className="p-4 space-y-4">
        {filteredLinks.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-500">
              {searchQuery ? 'No links found matching your search.' : 'No links in this category yet.'}
            </p>
          </div>
        ) : (
          filteredLinks.map((link) => <LinkItem key={link.id} link={link} />)
        )}
      </div>
    </div>
  );
}
