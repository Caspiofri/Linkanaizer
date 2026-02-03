/**
 * Category Card Component
 */

'use client';

import { Category } from '../types';
import Link from 'next/link';

interface CategoryCardProps {
  category: Category;
}

export default function CategoryCard({ category }: CategoryCardProps) {
  return (
    <Link
      href={`/category/${category.name.toLowerCase()}`}
      className="block p-4 bg-white rounded-[32px] shadow-sm hover:shadow-md transition-shadow"
    >
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {category.emoji ? (
            <span className="text-xl">{category.emoji}</span>
          ) : null}
          <h3 className="text-[#7A3E93] font-semibold text-lg">{category.name}</h3>
        </div>
        <span className="text-gray-500 text-sm">
          {category.count} Links
        </span>
      </div>
    </Link>
  );
}
