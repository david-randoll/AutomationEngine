"use client";
import NavBar from "@/components/NavBar";
import AutomationBuilderPage from "@/components/AutomationBuilderPage";

export default function Home() {
  return (
    <div className="min-h-screen bg-background">
      <NavBar />
      <AutomationBuilderPage />
    </div>
  );
}
