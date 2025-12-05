"use client";

import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { FiCheck, FiCopy } from "react-icons/fi";
import MonacoEditor from "@monaco-editor/react";

interface CopyableBlockProps {
    label: string;
    content: string;
    language?: "json" | "yaml";
}

const CopyableBlock = ({ label, content, language }: CopyableBlockProps) => {
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
        <div className="flex flex-col flex-1 h-full">
            <div className="flex items-center justify-between text-xs text-gray-500 mb-2">
                <span>{label}</span>
                <button onClick={handleCopy} className="bg-white hover:shadow-md rounded-full p-1.5 hover:bg-gray-50">
                    <AnimatePresence mode="wait" initial={false}>
                        {copied ? (
                            <motion.div
                                key="check"
                                initial={{ scale: 0.5, opacity: 0 }}
                                animate={{ scale: 1.2, opacity: 1 }}
                                exit={{ scale: 0.5, opacity: 0 }}
                                transition={{ type: "spring", stiffness: 500, damping: 25 }}>
                                <FiCheck className="h-4 w-4 text-green-500" />
                            </motion.div>
                        ) : (
                            <motion.div
                                key="copy"
                                initial={{ scale: 0.5, opacity: 0 }}
                                animate={{ scale: 1, opacity: 1 }}
                                exit={{ scale: 0.5, opacity: 0 }}
                                transition={{ type: "spring", stiffness: 500, damping: 25 }}>
                                <FiCopy className="h-4 w-4 text-gray-600" />
                            </motion.div>
                        )}
                    </AnimatePresence>
                </button>
            </div>

            <div className="flex-1">
                <MonacoEditor
                    height="100%"
                    language={language}
                    value={content}
                    options={{
                        readOnly: true,
                        lineNumbers: "off",
                        minimap: { enabled: false },
                        folding: true,
                        scrollBeyondLastLine: false,
                        renderLineHighlight: "none",
                        contextmenu: false,
                        fontSize: 12,
                        wordWrap: "on",
                    }}
                />
            </div>
        </div>
    );
};

export default CopyableBlock;