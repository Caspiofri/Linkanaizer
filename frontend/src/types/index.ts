/**
 * TypeScript types matching Pydantic schemas from backend
 */

export interface LinkClassification {
  category: string;
  category_emoji: string;
  title: string;
  summary: string;
  keywords: string[];
}

export interface LinkClassificationRequest {
  url: string;
  categories?: string[];
}

export interface Link extends LinkClassification {
  id: string;
  url: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Category as returned from backend API (matches CategoryRead schema)
 */
export interface CategoryApiResponse {
  id: number;
  name: string;
  emoji?: string;
  is_visible: boolean;
  link_count: number;
}

/**
 * Category as used in frontend store/components
 */
export interface Category {
  id: string;
  name: string;
  emoji?: string;
  is_visible: boolean;
  count: number;
}
