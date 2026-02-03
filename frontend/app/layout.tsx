import type { Metadata, Viewport } from "next";
import { Inter, Montserrat } from "next/font/google";
import "./globals.css";
import ServiceWorkerRegistrar from "@/src/components/ServiceWorkerRegistrar";
import AuthProvider from "@/src/components/AuthProvider";
import BottomNavGate from "@/src/components/BottomNavGate";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
});

const montserrat = Montserrat({
  subsets: ["latin"],
  variable: "--font-montserrat",
  display: "swap",
});

export const metadata: Metadata = {
  title: "LinkClassify - AI-Powered Link Classification",
  description: "Classify and organize your web links with AI",
  manifest: "/manifest.webmanifest",
  icons: {
    icon: "/assets/favicon-32x32.png",
    apple: "/assets/icons/icon-192.png",
  },
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 5,
  themeColor: "#7c3aed",
  viewportFit: "cover",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body
        className={`${inter.variable} ${montserrat.variable} font-sans bg-[#F5F3F7] min-h-screen`}
        suppressHydrationWarning
      >
        <ServiceWorkerRegistrar />
        <AuthProvider>{children}</AuthProvider>
        <BottomNavGate />
      </body>
    </html>
  );
}

