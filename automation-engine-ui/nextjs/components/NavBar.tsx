"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const NavBar = () => {
    const pathname = usePathname();
    const isBuilder = pathname === "/" || pathname === "";
    const isUserDefined = pathname.startsWith("/user-defined");

    return (
        <nav className="bg-white border-b shadow-sm">
            <div className="max-w-7xl mx-auto px-4 sm:px-6">
                <div className="flex items-center gap-4 h-14">
                    <span className="font-semibold text-gray-900">Automation Engine</span>
                    <div className="flex gap-2">
                        <Link
                            href="/"
                            className={`px-3 py-1.5 text-sm rounded-md transition-colors ${isBuilder ? "bg-primary text-primary-foreground" : "text-gray-600 hover:bg-gray-100"
                                }`}
                        >
                            Automation Builder
                        </Link>
                        <Link
                            href="./user-defined"
                            className={`px-3 py-1.5 text-sm rounded-md transition-colors ${isUserDefined ? "bg-primary text-primary-foreground" : "text-gray-600 hover:bg-gray-100"
                                }`}
                        >
                            User-Defined Types
                        </Link>
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default NavBar;
