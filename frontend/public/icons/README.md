# PWA Icons Setup Guide

## Problem
The PWA manifest requires `icon-192.png` and `icon-512.png` files, but they're currently missing, causing validation errors.

## Quick Solution (Choose One)

### Option 1: Use the HTML Converter (Easiest)
1. Open `convert-svg-to-png.html` in your browser (double-click it)
2. Click "Load SVG"
3. Click "Generate 192x192" → Right-click canvas → "Save image as..." → Save as `icon-192.png`
4. Click "Generate 512x512" → Right-click canvas → "Save image as..." → Save as `icon-512.png`
5. Make sure both PNG files are saved in this directory (`frontend/public/icons/`)

### Option 2: Use Online Converter
1. Go to https://cloudconvert.com/svg-to-png
2. Upload `../logo.svg` (the logo file in the parent directory)
3. Set output size to **192x192** → Convert → Download as `icon-192.png`
4. Set output size to **512x512** → Convert → Download as `icon-512.png`
5. Place both files in this directory

### Option 3: Use Figma/Design Tool
1. Open `../logo.svg` in Figma
2. Export as PNG:
   - **192x192** → Save as `icon-192.png`
   - **512x512** → Save as `icon-512.png`
3. Place both files in this directory

### Option 4: Use Command Line (if you have ImageMagick)
```powershell
cd frontend\public\icons
magick ..\logo.svg -resize 192x192 icon-192.png
magick ..\logo.svg -resize 512x512 icon-512.png
```

## After Creating the Icons
1. Restart your Next.js dev server (`npm run dev`)
2. Open Chrome DevTools → Application → Manifest
3. Verify that both icons load without errors
4. The PWA validation errors should disappear!

## Notes
- Icons must be **square** (192x192 and 512x512)
- Icons should have a **solid background** (the logo SVG has a purple gradient)
- The manifest is already configured correctly - you just need the actual PNG files
