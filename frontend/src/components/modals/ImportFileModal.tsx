/**
 * Import File Modal Component
 */

'use client';

import { useState, useRef } from 'react';
import { X, Upload as UploadIcon, FileText } from 'lucide-react';
import Image from 'next/image';

interface ImportFileModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function ImportFileModal({ isOpen, onClose }: ImportFileModalProps) {
  const [dragActive, setDragActive] = useState(false);
  const [uploadedFiles, setUploadedFiles] = useState<Array<{
    id: string;
    name: string;
    size: string;
    progress: number;
    timeLeft: string;
  }>>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFiles(e.dataTransfer.files);
    }
  };

  const handleFileInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      handleFiles(e.target.files);
    }
  };

  const handleFiles = (files: FileList) => {
    Array.from(files).forEach((file) => {
      const fileSize = (file.size / 1024 / 1024).toFixed(1);
      const newFile = {
        id: Date.now().toString() + Math.random(),
        name: file.name,
        size: `${fileSize}MB`,
        progress: 0,
        timeLeft: 'Calculating...',
      };

      setUploadedFiles((prev) => [...prev, newFile]);

      // Simulate upload progress
      simulateUpload(newFile.id);
    });
  };

  const simulateUpload = (fileId: string) => {
    let progress = 0;
    const interval = setInterval(() => {
      progress += Math.random() * 15;
      if (progress >= 100) {
        progress = 100;
        clearInterval(interval);
      }

      setUploadedFiles((prev) =>
        prev.map((file) =>
          file.id === fileId
            ? {
                ...file,
                progress,
                timeLeft: progress >= 100 ? 'Complete' : `${Math.ceil((100 - progress) / 10)} seconds left`,
              }
            : file
        )
      );
    }, 500);
  };

  const removeFile = (fileId: string) => {
    setUploadedFiles((prev) => prev.filter((file) => file.id !== fileId));
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-[32px] shadow-lg w-full max-w-md max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b">
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700"
          >
            <X className="w-6 h-6" />
          </button>
          <h2 className="text-[#7A3E93] font-bold text-xl">Import File</h2>
          <div className="w-6"></div>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* File Upload Area */}
          {uploadedFiles.length === 0 ? (
            <div
              onDragEnter={handleDrag}
              onDragLeave={handleDrag}
              onDragOver={handleDrag}
              onDrop={handleDrop}
              className={`border-2 border-dashed rounded-[32px] p-8 text-center transition-colors ${
                dragActive ? 'border-[#7A3E93] bg-purple-50' : 'border-gray-300'
              }`}
            >
              <UploadIcon className="w-12 h-12 text-[#7A3E93] mx-auto mb-4" />
              <p className="text-gray-700 mb-2">
                Drag & Drop or{' '}
                <button
                  onClick={() => fileInputRef.current?.click()}
                  className="text-[#7A3E93] font-semibold hover:underline"
                >
                  Choose file
                </button>{' '}
                to upload
              </p>
              <p className="text-sm text-gray-500">
                fig, zip, pdf, png, jpeg, txt
              </p>
              <input
                ref={fileInputRef}
                type="file"
                onChange={handleFileInput}
                accept=".fig,.zip,.pdf,.png,.jpeg,.jpg,.txt"
                multiple
                className="hidden"
              />
            </div>
          ) : (
            <div className="space-y-3">
              {uploadedFiles.map((file) => (
                <div
                  key={file.id}
                  className="bg-white rounded-[32px] shadow-sm p-4 flex items-center gap-3"
                >
                  <FileText className="w-8 h-8 text-[#7A3E93] flex-shrink-0" />
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-gray-800 truncate">{file.name}</p>
                    <p className="text-sm text-gray-500">
                      {file.size} • {file.timeLeft}
                    </p>
                    <div className="mt-2 w-full bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-[#7A3E93] h-2 rounded-full transition-all"
                        style={{ width: `${file.progress}%` }}
                      />
                    </div>
                  </div>
                  <button
                    onClick={() => removeFile(file.id)}
                    className="text-gray-400 hover:text-gray-600"
                  >
                    <X className="w-5 h-5" />
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* Separator */}
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-300"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="bg-white px-2 text-gray-500">OR</span>
            </div>
          </div>

          {/* Import from URL Section */}
          <div>
            <label className="block text-gray-700 font-medium mb-2">
              Import from URL
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="Add file URL"
                className="flex-1 px-4 py-2 border border-gray-300 rounded-[32px] focus:outline-none focus:ring-2 focus:ring-[#7A3E93]"
              />
              <button className="px-4 py-2 bg-gray-100 text-gray-700 rounded-[32px] hover:bg-gray-200 transition-colors">
                Upload
              </button>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex gap-4">
            <button
              onClick={onClose}
              className="flex-1 px-6 py-3 bg-gray-100 text-gray-700 rounded-[32px] hover:bg-gray-200 transition-colors font-medium"
            >
              Cancel
            </button>
            <button
              className={`flex-1 px-6 py-3 rounded-[32px] font-medium transition-colors ${
                uploadedFiles.length > 0 && uploadedFiles.some((f) => f.progress < 100)
                  ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  : 'bg-[#7A3E93] text-white hover:bg-[#6a3580]'
              }`}
              disabled={uploadedFiles.length > 0 && uploadedFiles.some((f) => f.progress < 100)}
            >
              Import
            </button>
          </div>

          {/* Illustration */}
          <div className="relative w-full h-48 mt-6">
            <Image
              src="/assets/images/ill-import-file.svg"
              alt="Import File Illustration"
              fill
              className="object-contain"
            />
          </div>
        </div>
      </div>
    </div>
  );
}
