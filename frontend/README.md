# LinkClassify Frontend

Next.js frontend application for LinkClassify - AI-powered link classification.

## Features

- 🎨 **Modern UI** - Clean, responsive design with Tailwind CSS
- 🔗 **Link Classification** - AI-powered link categorization
- 📱 **PWA Ready** - Progressive Web App support
- 🔍 **Search** - Real-time search within categories
- 📂 **Category Management** - Organize links by category
- 🎯 **TypeScript** - Full type safety

## Tech Stack

- **Next.js 16** - React framework
- **TypeScript** - Type safety
- **Tailwind CSS** - Styling
- **Zustand** - State management
- **Lucide React** - Icons
- **Zod** - Schema validation (if needed)

## Getting Started

### Installation

```bash
npm install
```

### Environment Variables

Create a `.env.local` file:

```env
NEXT_PUBLIC_API_URL=http://localhost:8000
```

### Development

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Build

```bash
npm run build
npm start
```

## Project Structure

```
frontend/
├── app/                    # Next.js app router pages
│   ├── page.tsx           # Home page (categories)
│   ├── login/             # Login page
│   ├── category/          # Category view pages
│   └── ...
├── src/
│   ├── components/        # React components
│   │   ├── modals/        # Modal components
│   │   └── ...
│   ├── hooks/             # Custom React hooks
│   ├── store/             # Zustand state management
│   └── types/              # TypeScript types
└── public/
    └── assets/
        └── images/         # Figma SVGs and assets
```

## Components

### Core Components

- `BottomNav` - Sticky bottom navigation bar
- `LinkItem` - Individual link display card
- `CategoryCard` - Category display card
- `InsertLinkModal` - Modal for adding new links
- `ImportFileModal` - Modal for importing files

## API Integration

The frontend communicates with the FastAPI backend at `http://localhost:8000/api/v1`.

### API Endpoints Used

- `POST /api/v1/links/classify` - Classify a link URL

See `src/hooks/useApi.ts` for the API integration.

## State Management

Global state is managed using Zustand in `src/store/useLinkStore.ts`.

### Store Methods

- `addLink(link)` - Add a new link
- `removeLink(id)` - Remove a link
- `getLinksByCategory(category)` - Get links by category
- `getCategoryCount(category)` - Get count for a category

## Styling

The app uses Tailwind CSS with custom theme:

- **Background**: Light Lavender (`#f5f3f7`)
- **Primary Color**: Deep Purple (`#7A3E93`)
- **Cards**: White with `rounded-[32px]` and `shadow-sm`
- **Typography**: Inter or Montserrat (sans-serif)

## Assets

Place Figma SVGs in `public/assets/images/`:

- `ill-welcome.svg` - Welcome/login illustration
- `ill-import-link.svg` - Import link illustration
- `ill-import-file.svg` - Import file illustration
- `icon-nav-plus.svg` - Plus icon for navigation
- `icon-nav-upload.svg` - Upload icon for navigation
- `icon-nav-link.svg` - Link icon for navigation

## PWA Support

The app includes a `manifest.json` for PWA functionality. Users can "Add to Home Screen" on mobile devices.
