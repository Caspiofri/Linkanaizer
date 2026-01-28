/**
 * Centralized API hook for backend communication
 */

import { LinkClassification, LinkClassificationRequest, Category } from '@/src/types';

// Prefer explicit 127.0.0.1 to avoid localhost DNS quirks on Windows
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://127.0.0.1:8000';
const API_V1 = `${API_BASE_URL}/api/v1`;

export interface ApiError {
  detail: string;
  status?: number;
}

export async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_V1}${endpoint}`;
  console.log('API request:', {
    url,
    method: options.method || 'GET',
  });

  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  if (!response.ok) {
    const error: ApiError = await response.json().catch(() => ({
      detail: `HTTP ${response.status}: ${response.statusText}`,
      status: response.status,
    }));
    throw error;
  }

  return response.json();
}

export function useApi() {
  const classifyLink = async (
    url: string,
    categories?: string[]
  ): Promise<LinkClassification> => {
    // Build request body - only include categories if they exist
    const requestBody: LinkClassificationRequest = { url };
    if (categories && categories.length > 0) {
      requestBody.categories = categories;
    }
    // If categories is undefined or empty, don't include it in the request
    
    return apiRequest<LinkClassification>('/links/classify', {
      method: 'POST',
      body: JSON.stringify(requestBody),
    });
  };

  const fetchCategories = async (): Promise<Category[]> => {
    console.log('API: fetching categories...');
    return apiRequest<Category[]>('/categories', {
      method: 'GET',
    });
  };

  const createCategory = async (name: string): Promise<Category> => {
    console.log('API: creating category with name:', name);
    return apiRequest<Category>('/categories', {
      method: 'POST',
      body: JSON.stringify({ name }),
    });
  };

  const toggleCategoryVisibility = async (id: string): Promise<Category> => {
    console.log('API: toggling category visibility for id:', id);
    return apiRequest<Category>(`/categories/${id}/toggle-visibility`, {
      method: 'PATCH',
    });
  };

  return {
    classifyLink,
    fetchCategories,
    createCategory,
    toggleCategoryVisibility,
  };
}
