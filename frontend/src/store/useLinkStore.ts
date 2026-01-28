/**
 * Global state management for links using Zustand
 */

import { create } from 'zustand';
import { Link, Category } from '@/src/types';

interface LinkStore {
  links: Link[];
  categories: Category[];
  isLoading: boolean;
  error: string | null;
  isInsertLinkModalOpen: boolean;
  
  // Actions
  addLink: (link: Link) => void;
  removeLink: (id: string) => void;
  addCategory: (category: Category) => void;
  removeCategory: (id: string) => void;
  toggleCategoryVisibility: (id: string) => void;
  setLinks: (links: Link[]) => void;
  setCategories: (categories: Category[]) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  openInsertLinkModal: () => void;
  closeInsertLinkModal: () => void;
  
  // Computed
  getLinksByCategory: (category: string) => Link[];
  getCategoryCount: (category: string) => number;
  getCategoriesWithEmojis: () => Category[];
}

export const useLinkStore = create<LinkStore>((set, get) => ({
  links: [],
  categories: [],
  isLoading: false,
  error: null,
  isInsertLinkModalOpen: false,

  addLink: (link) =>
    set((state) => ({
      links: [...state.links, link],
      categories: updateCategoryCount(state.categories, link.category, 1),
    })),

  removeLink: (id) =>
    set((state) => {
      const link = state.links.find((l) => l.id === id);
      if (!link) return state;
      
      return {
        links: state.links.filter((l) => l.id !== id),
        categories: updateCategoryCount(state.categories, link.category, -1),
      };
    }),

  addCategory: (category) =>
    set((state) => {
      // Check if category already exists
      const exists = state.categories.find(
        (c) => c.name.toLowerCase() === category.name.toLowerCase()
      );
      if (exists) return state;
      
      return {
        categories: [...state.categories, category],
      };
    }),

  removeCategory: (id) =>
    set((state) => ({
      categories: state.categories.filter((c) => c.id !== id),
    })),

  toggleCategoryVisibility: (id) =>
    set((state) => ({
      categories: state.categories.map((c) =>
        c.id === id ? { ...c, is_visible: !c.is_visible } : c
      ),
    })),

  setLinks: (links) => set({ links }),
  setCategories: (categories) => set({ categories }),
  setLoading: (isLoading) => set({ isLoading }),
  setError: (error) => set({ error }),
  openInsertLinkModal: () => set({ isInsertLinkModalOpen: true }),
  closeInsertLinkModal: () => set({ isInsertLinkModalOpen: false }),

  getLinksByCategory: (category) => {
    const { links } = get();
    return links.filter((link) => link.category.toLowerCase() === category.toLowerCase());
  },

  getCategoryCount: (category) => {
    const { links } = get();
    return links.filter((link) => link.category.toLowerCase() === category.toLowerCase()).length;
  },

  getCategoriesWithEmojis: () => {
    const { categories } = get();
    // Categories already contain the emoji coming from the backend
    return categories;
  },
}));

function updateCategoryCount(
  categories: Category[],
  categoryName: string,
  delta: number
): Category[] {
  const existing = categories.find((c) => c.name.toLowerCase() === categoryName.toLowerCase());
  
  if (existing) {
    return categories.map((c) =>
      c.name.toLowerCase() === categoryName.toLowerCase()
        ? { 
            ...c, 
            count: Math.max(0, c.count + delta),
          }
        : c
    );
  }
  
  if (delta > 0) {
    return [...categories, { 
      id: categoryName.toLowerCase().replace(/\s+/g, '-'), 
      name: categoryName, 
      count: 1 
    }];
  }
  
  return categories;
}
