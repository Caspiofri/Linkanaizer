/**
 * New Category Page - Add or Search Categories
 */

'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeft, X, Plus, Search, Check } from 'lucide-react';
import { useLinkStore } from '@/src/store/useLinkStore';
import { Category } from '@/src/types';
import { useApi } from '@/src/hooks/useApi';

export default function NewCategoryPage() {
  const router = useRouter();
  const { getCategoriesWithEmojis, addCategory, setCategories, toggleCategoryVisibility } = useLinkStore();
  const { fetchCategories, createCategory, toggleCategoryVisibility: toggleCategoryVisibilityApi } = useApi();
  const [searchQuery, setSearchQuery] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const categories = getCategoriesWithEmojis();

  // Load categories from backend on mount (run once)
  useEffect(() => {
    const loadCategories = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const backendCategories = await fetchCategories();
        // Map backend categories (which don't have counts) into frontend Category shape
        const mapped: Category[] = backendCategories.map((c) => ({
          id: String(c.id),
          name: c.name,
          emoji: c.emoji,
          is_visible: c.is_visible,
          count: c.link_count ?? 0,
        }));
        setCategories(mapped);
      } catch (e: any) {
        // Keep UI usable even if loading categories fails
        setError(e?.detail || 'Failed to load categories from backend');
      } finally {
        setIsLoading(false);
      }
    };

    void loadCategories();
    // We intentionally omit fetchCategories/setCategories from deps to avoid re-fetch loop
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  
  const canonicalQuery = searchQuery.trim();

  // Filter categories based on search (case-insensitive)
  const filteredCategories = categories.filter((cat) =>
    cat.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const hasExactMatch =
    canonicalQuery.length > 0 &&
    categories.some(
      (cat) => cat.name.toLowerCase() === canonicalQuery.toLowerCase()
    );

  const handleCreateFromSearch = async () => {
    if (!canonicalQuery) return;

    // Check if category already exists in local state (case-insensitive)
    const exists = categories.find(
      (c) => c.name.toLowerCase() === canonicalQuery.toLowerCase()
    );

    if (exists) {
      alert('Category already exists!');
      return;
    }

    try {
      setIsLoading(true);
      setError(null);
      const created = await createCategory(canonicalQuery);
      const newCategory: Category = {
        id: String(created.id),
        name: created.name,
        emoji: created.emoji,
        is_visible: created.is_visible,
        count: 0,
      };
      addCategory(newCategory);
      setSearchQuery('');
    } catch (e: any) {
      console.error('UI: failed to create category', e);
      setError(e?.detail || 'Failed to create category');
    } finally {
      setIsLoading(false);
    }
  };

  const handleToggleVisibility = async (category: Category) => {
    // Optimistic update in local state
    toggleCategoryVisibility(category.id);
    try {
      await toggleCategoryVisibilityApi(category.id);
    } catch (e: any) {
      console.error('UI: failed to toggle visibility', e);
      // Revert optimistic toggle on error
      toggleCategoryVisibility(category.id);
      setError(e?.detail || 'Failed to toggle category visibility');
    }
  };

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
          <h1 className="text-[#7A3E93] font-bold text-2xl flex-1">New Category</h1>
        </div>
      </div>

      <div className="p-4 space-y-6">
        {error && (
          <div className="p-3 rounded-[24px] bg-red-50 text-red-700 text-sm">
            {error}
          </div>
        )}

        {!error && isLoading && categories.length === 0 && (
          <div className="p-3 rounded-[24px] bg-white text-gray-500 text-sm text-center border border-gray-200">
            Loading categories...
          </div>
        )}

        {/* Search Bar */}
        <div className="relative">
          <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="text"
            suppressHydrationWarning
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search categories..."
            className="w-full pl-12 pr-10 py-3 bg-white border border-gray-300 rounded-[32px] focus:outline-none focus:ring-2 focus:ring-[#7A3E93]"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              <X className="w-5 h-5" />
            </button>
          )}
        </div>

        {/* Category List */}
        {filteredCategories.length > 0 && (
          <div className="space-y-0">
            {filteredCategories.map((category) => (
              <div
                key={category.id}
                className="flex items-center justify-between p-4 border-b border-gray-200 last:border-b-0"
              >
                <div className="flex items-center gap-3">
                  {category.emoji && <span className="text-xl">{category.emoji}</span>}
                  <span className="text-gray-800 font-medium">{category.name}</span>
                  <span className="text-gray-500 text-sm">{category.count}</span>
                </div>
                <button
                  onClick={() => handleToggleVisibility(category)}
                  className={`w-7 h-7 rounded-full flex items-center justify-center transition-colors border ${
                    category.is_visible
                      ? 'bg-[#7A3E93] border-[#7A3E93] text-white'
                      : 'bg-white border-gray-300 text-gray-400'
                  }`}
                  title={category.is_visible ? 'Hide category' : 'Show category'}
                >
                  <Check className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        )}

        {/* Create New Category from search */}
        {canonicalQuery && !hasExactMatch && (
          <button
            onClick={handleCreateFromSearch}
            disabled={isLoading}
            className="w-full mt-2 flex items-center justify-between px-4 py-3 bg-white border border-dashed border-[#7A3E93] rounded-[24px] text-sm text-[#7A3E93] hover:bg-[#f7eefb] transition-colors"
          >
            <span className="flex items-center gap-2">
              <Plus className="w-4 h-4" />
              <span>Create "{canonicalQuery}"</span>
            </span>
          </button>
        )}
      </div>
    </div>
  );
}
