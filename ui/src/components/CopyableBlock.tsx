"use client";

import { cn } from "@/lib/utils";
import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { FiCheck, FiCopy } from "react-icons/fi";

const CopyableBlock = ({ label, content }: { label: string; content: string }) => {
    const [copied, setCopied] = useState(false);

    const handleCopy = async () => {
        try {
            await navigator.clipboard.writeText(content);
            setCopied(true);
            setTimeout(() => setCopied(false), 1500);
        } catch (err) {
            console.error("Failed to copy:", err);
        }
    };

    return (
        <div className="relative group mb-4">
            <div className="text-xs text-gray-500 mb-2">{label}</div>
            <pre className="text-xs bg-gray-100 rounded p-2 max-h-80 overflow-auto pr-10">{content}</pre>

            {/* Hover copy button */}
            <button
                onClick={handleCopy}
                className={cn(
                    "absolute top-6 right-3 opacity-0 group-hover:opacity-100 transition-opacity",
                    "bg-white shadow-md rounded-full p-1.5 hover:bg-gray-50"
                )}>
                <AnimatePresence mode="wait" initial={false}>
                    {copied ? (
                        <motion.div
                            key="check"
                            initial={{ scale: 0, opacity: 0 }}
                            animate={{ scale: 1, opacity: 1 }}
                            exit={{ scale: 0, opacity: 0 }}
                            transition={{ duration: 0.2 }}>
                            <FiCheck className="h-4 w-4 text-green-500" />
                        </motion.div>
                    ) : (
                        <motion.div
                            key="copy"
                            initial={{ scale: 0, opacity: 0 }}
                            animate={{ scale: 1, opacity: 1 }}
                            exit={{ scale: 0, opacity: 0 }}
                            transition={{ duration: 0.2 }}>
                            <FiCopy className="h-4 w-4 text-gray-600" />
                        </motion.div>
                    )}
                </AnimatePresence>
            </button>
        </div>
    );
};

export default CopyableBlock;
