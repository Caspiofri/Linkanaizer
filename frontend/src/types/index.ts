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

export interface Category {
  id: string;
  name: string;
  emoji?: string;
  is_visible: boolean;
  count: number;
}
