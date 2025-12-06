"use client";

import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { FormProvider, useForm } from "react-hook-form";
import { AutomationEngineProvider } from "@/providers/AutomationEngineProvider";
import { Toaster } from "sonner";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

function Providers({ children }: { children: React.ReactNode }) {
  const methods = useForm({
    defaultValues: {},
    mode: "onBlur",
  });

  return (
    <FormProvider {...methods}>
      <AutomationEngineProvider>{children}</AutomationEngineProvider>
    </FormProvider>
  );
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" data-kantu="1">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <Providers>{children}</Providers>
        <Toaster richColors position="top-right" />
        <script src="./app-config.js"></script>
      </body>
    </html>
  );
}