import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./app/globals.css";
import App from "./components/App";
import AutomationBuilderPage from "./components/AutomationBuilderPage";
import UserDefinedPage from "./components/UserDefinedPage";
import UserDefinedRegisterPage from "./components/UserDefinedRegisterPage";
import { RouterProvider, useRoute, Link } from "./lib/router";
import type { BlockType } from "./types/user-defined";

const validBlockTypes = new Set(["actions", "conditions", "triggers", "variables"]);

function isValidBlockType(type: string): type is BlockType {
    return validBlockTypes.has(type);
}

const registerRegex = /^\/user-defined\/(actions|conditions|triggers|variables)\/new$/;

const Router = () => {
    const route = useRoute();
    const path = route.path;

    // Route: /user-defined/:blockType/new
    const registerMatch = registerRegex.exec(path);
    if (registerMatch) {
        const blockType = registerMatch[1];
        if (isValidBlockType(blockType)) {
            return <UserDefinedRegisterPage blockType={blockType} />;
        }
    }

    // Route: /user-defined
    if (path === "/user-defined") {
        return <UserDefinedPage />;
    }

    // Default route: / (Automation Builder)
    return <AutomationBuilderPage />;
};

const NavBar = () => {
    const route = useRoute();
    const isBuilder = route.path === "/" || route.path === "";
    const isUserDefined = route.path.startsWith("/user-defined");

    return (
        <nav className="bg-white border-b shadow-sm">
            <div className="max-w-7xl mx-auto px-4 sm:px-6">
                <div className="flex items-center gap-4 h-14">
                    <span className="font-semibold text-gray-900">Automation Engine</span>
                    <div className="flex gap-2">
                        <Link
                            to="/"
                            className={`px-3 py-1.5 text-sm rounded-md transition-colors ${
                                isBuilder ? "bg-primary text-primary-foreground" : "text-gray-600 hover:bg-gray-100"
                            }`}
                        >
                            Automation Builder
                        </Link>
                        <Link
                            to="/user-defined"
                            className={`px-3 py-1.5 text-sm rounded-md transition-colors ${
                                isUserDefined ? "bg-primary text-primary-foreground" : "text-gray-600 hover:bg-gray-100"
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

const Main = () => {
    return (
        <RouterProvider>
            <App>
                <NavBar />
                <Router />
            </App>
        </RouterProvider>
    );
};

createRoot(document.getElementById("root")!).render(
    <StrictMode>
        <Main />
    </StrictMode>
);


