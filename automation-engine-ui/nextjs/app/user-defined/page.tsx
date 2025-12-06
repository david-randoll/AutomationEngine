"use client";
import NavBar from "@/components/NavBar";
import UserDefinedPage from "@/components/UserDefinedPage";

export default function UserDefinedIndex() {
    return (
        <div className="min-h-screen bg-background">
            <NavBar />
            <UserDefinedPage initialTab="actions" />
        </div>
    );
}
