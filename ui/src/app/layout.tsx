import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import App from "@/components/App";

const geistSans = Geist({
    variable: "--font-geist-sans",
    subsets: ["latin"],
});

const geistMono = Geist_Mono({
    variable: "--font-geist-mono",
    subsets: ["latin"],
});

export const metadata: Metadata = {
    title: "Automation Engine Builder",
    description: "A UI for building automation engine JSON/YAML configurations",
};

export default function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="en" data-kantu="1">
            <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
                <App>{children}</App>;
            </body>
        </html>
    );
}
