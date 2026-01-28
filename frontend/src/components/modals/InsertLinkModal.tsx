/**
 * Insert Link Modal Component
 */

'use client';

import { useState, useEffect } from 'react';
import { X, Loader2, ExternalLink } from 'lucide-react';
import Image from 'next/image';
import { useApi } from '@/src/hooks/useApi';
import { useLinkStore } from '@/src/store/useLinkStore';
import { useRouter } from 'next/navigation';
import { Link } from '@/src/types';

interface InsertLinkModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function InsertLinkModal({ isOpen, onClose }: InsertLinkModalProps) {
  const [url, setUrl] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [classification, setClassification] = useState<any>(null);
  
  const { classifyLink } = useApi();
  const { addLink, categories } = useLinkStore();
  const router = useRouter();

  // Reset state when modal opens
  useEffect(() => {
    if (isOpen) {
      setUrl('');
      setError(null);
      setClassification(null);
      setIsLoading(false);
    }
  }, [isOpen]);

  // Reset state when modal closes
  const handleClose = () => {
    setUrl('');
    setError(null);
    setClassification(null);
    setIsLoading(false);
    onClose();
  };

  const handleImport = async () => {
    if (!url.trim()) return;

    setIsLoading(true);
    setError(null);
    setClassification(null);

    try {
      // Build allowed categories list from current visible categories
      const visibleCategoryNames = categories
        .filter((c) => c.is_visible)
        .map((c) => c.name);

      const categoryNames =
        visibleCategoryNames.length > 0 ? visibleCategoryNames : undefined;

      console.log('Classifying URL:', url.trim());
      console.log('Categories to send:', categoryNames);
      const result = await classifyLink(url.trim(), categoryNames);

      // Detailed logging of LLM classification result
      console.log('LLM classification raw result:', result);
      console.log('LLM classification - category:', result.category);
      console.log('LLM classification - title:', result.title);
      console.log('LLM classification - summary:', result.summary);
      console.log('LLM classification - category_emoji:', result.category_emoji);
      console.log('LLM classification - keywords:', result.keywords);
      
      // Create a link object with generated ID
      const newLink: Link = {
        id: Date.now().toString(),
        url: url.trim(),
        ...result,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      addLink(newLink);
      setClassification(result);
      
      // Redirect to category view after a short delay
      setTimeout(() => {
        handleClose();
        router.push(`/category/${result.category.toLowerCase()}`);
      }, 1500);
    } catch (err: any) {
      setError(err.detail || 'Failed to classify link');
    } finally {
      setIsLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div 
      className="fixed inset-0 bg-black/50 flex items-center justify-center z-[100] p-4" 
      onClick={(e) => {
        // Close modal when clicking outside
        if (e.target === e.currentTarget && !isLoading) {
          handleClose();
        }
      }}
    >
      <div 
        className="bg-white rounded-[32px] shadow-lg w-full max-w-md max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b">
          <button
            onClick={handleClose}
            className="text-gray-500 hover:text-gray-700"
          >
            <X className="w-6 h-6" />
          </button>
          <h2 className="text-[#7A3E93] font-bold text-xl">Insert Link</h2>
          <div className="w-6"></div>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Import from URL Section */}
          <div>
            <label className="block text-gray-700 font-medium mb-2">
              Import from URL
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                onKeyDown={(e) => {
                  // Allow Enter key to trigger import
                  if (e.key === 'Enter' && !isLoading && url.trim() && !classification) {
                    handleImport();
                  }
                }}
                placeholder="Add file URL"
                className="flex-1 px-4 py-2 border border-gray-300 rounded-[32px] focus:outline-none focus:ring-2 focus:ring-[#7A3E93]"
                disabled={isLoading}
              />
              <button
                className="px-4 py-2 bg-gray-100 text-gray-700 rounded-[32px] hover:bg-gray-200 transition-colors"
                disabled={isLoading}
              >
                Upload
              </button>
            </div>
          </div>

          {/* Classification Results */}
          {classification && (
            <div className="space-y-3 p-4 bg-gray-50 rounded-[32px]">
              <div>
                <span className="text-sm text-gray-600">Category - </span>
                <span className="text-[#7A3E93] font-semibold flex items-center gap-2">
                  {classification.category_emoji && (
                    <span className="text-lg">{classification.category_emoji}</span>
                  )}
                  <span>{classification.category}</span>
                </span>
              </div>
              <div>
                <p className="text-sm text-gray-600 mb-1">title</p>
                <p className="text-gray-800">{classification.title}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600 mb-1">description</p>
                <p className="text-gray-800">{classification.summary}</p>
              </div>
              <div className="flex items-center gap-2">
                <ExternalLink className="w-4 h-4 text-gray-500" />
                <a
                  href={url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-sm text-gray-600 hover:text-[#7A3E93]"
                >
                  link to video/post
                </a>
              </div>
            </div>
          )}

          {/* Error Message */}
          {error && (
            <div className="p-4 bg-red-50 text-red-700 rounded-[32px] text-sm">
              {error}
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex gap-4">
            <button
              onClick={handleClose}
              className="flex-1 px-6 py-3 bg-gray-100 text-gray-700 rounded-[32px] hover:bg-gray-200 transition-colors font-medium"
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              onClick={handleImport}
              disabled={isLoading || !url.trim() || !!classification}
              className={`flex-1 px-6 py-3 rounded-[32px] font-medium transition-colors ${
                isLoading || !url.trim() || classification
                  ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  : 'bg-[#7A3E93] text-white hover:bg-[#6a3580]'
              }`}
            >
              {isLoading ? (
                <span className="flex items-center justify-center gap-2">
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Thinking...
                </span>
              ) : (
                'Import'
              )}
            </button>
          </div>

          {/* Illustration */}
          <div className="relative w-full h-48 mt-6">
            <Image
              src="/assets/images/ill-import-lin.svg"
              alt="Import Link Illustration"
              fill
              className="object-contain"
              onError={(e) => {
                // Fallback if image not found
                console.error('Image not found');
              }}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
