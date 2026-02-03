/**
 * Category Autocomplete component
 * - Filters existing categories as user types
 * - Allows creating a new category via backend if no match exists
 */

'use client';

import { useState, useMemo } from 'react';
import { Plus, Search } from 'lucide-react';
import { Category } from '../types';
import { useApi } from '../hooks/useApi';
import { useLinkStore } from '../store/useLinkStore';

interface CategoryAutocompleteProps {
  selectedCategory: Category | null;
  onCategorySelected: (category: Category | null) => void;
}

export default function CategoryAutocomplete({
  selectedCategory,
  onCategorySelected,
}: CategoryAutocompleteProps) {
  const { categories, addCategory } = useLinkStore();
  const { createCategory } = useApi();

  const [query, setQuery] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const normalizedQuery = query.trim();
  const filtered = useMemo(
    () =>
      categories.filter((cat) =>
        cat.name.toLowerCase().includes(normalizedQuery.toLowerCase())
      ),
    [categories, normalizedQuery]
  );

  const exactMatch = useMemo(
    () =>
      categories.find(
        (cat) => cat.name.toLowerCase() === normalizedQuery.toLowerCase()
      ) || null,
    [categories, normalizedQuery]
  );

  const showCreateOption =
    normalizedQuery.length > 0 && !exactMatch && !isCreating;

  const handleSelect = (category: Category) => {
    setQuery(category.name);
    setError(null);
    onCategorySelected(category);
  };

  const handleClear = () => {
    setQuery('');
    setError(null);
    onCategorySelected(null);
  };

  const handleCreate = async () => {
    if (!normalizedQuery) return;
    setIsCreating(true);
    setError(null);
    try {
      const created = await createCategory(normalizedQuery);
      const newCategory: Category = {
        id: String(created.id),
        name: created.name,
        emoji: created.emoji,
        is_visible: created.is_visible,
        count: created.link_count ?? 0,
      };
      addCategory(newCategory);
      setQuery(newCategory.name);
      onCategorySelected(newCategory);
    } catch (e: any) {
      setError(e?.detail || 'Failed to create category');
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <div className="space-y-2">
      <label className="block text-gray-700 font-medium">Category</label>
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input
          type="text"
          value={query}
          onChange={(e) => {
            setQuery(e.target.value);
            if (selectedCategory && e.target.value !== selectedCategory.name) {
              onCategorySelected(null);
            }
          }}
          placeholder="Search or create category..."
          className="w-full pl-9 pr-10 py-2 min-h-[44px] border border-gray-300 rounded-[24px] focus:outline-none focus:ring-2 focus:ring-[#7A3E93] text-sm touch-manipulation"
          aria-label="Search or create category"
        />
        {query && (
          <button
            type="button"
            onClick={handleClear}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 text-xs"
          >
            ✕
          </button>
        )}
      </div>

      {error && (
        <div className="text-xs text-red-600 bg-red-50 rounded-[16px] px-3 py-1">
          {error}
        </div>
      )}

      <div className="space-y-1 max-h-40 overflow-y-auto">
        {filtered.map((cat) => (
          <button
            key={cat.id}
            type="button"
            onClick={() => handleSelect(cat)}
            className={`w-full flex items-center justify-between px-3 py-1.5 rounded-[16px] text-sm ${
              selectedCategory && selectedCategory.id === cat.id
                ? 'bg-[#7A3E93] text-white'
                : 'bg-gray-100 text-gray-800 hover:bg-gray-200'
            }`}
          >
            <span className="flex items-center gap-2">
              {cat.emoji && <span>{cat.emoji}</span>}
              <span>{cat.name}</span>
            </span>
            <span className="text-[10px] text-gray-500">
              {cat.count ?? 0} links
            </span>
          </button>
        ))}

        {showCreateOption && (
          <button
            type="button"
            onClick={handleCreate}
            className="w-full flex items-center gap-2 px-3 py-1.5 rounded-[16px] text-sm bg-white border border-dashed border-[#7A3E93] text-[#7A3E93] hover:bg-[#f7eefb]"
          >
            <Plus className="w-4 h-4" />
            <span>Create "{normalizedQuery}"</span>
          </button>
        )}

        {!filtered.length && !showCreateOption && !error && normalizedQuery && (
          <div className="text-xs text-gray-500 px-1">No results</div>
        )}
      </div>
    </div>
  );
}

