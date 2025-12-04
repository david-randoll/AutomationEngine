import type { BlockType } from "@/types/user-defined";
import { useRoute } from "@/lib/router";
import UserDefinedRegisterPage from "./UserDefinedRegisterPage";
import UserDefinedPage from "./UserDefinedPage";
import AutomationBuilderPage from "./AutomationBuilderPage";

const validBlockTypes = new Set(["actions", "conditions", "triggers", "variables"]);

function isValidBlockType(type: string): type is BlockType {
    return validBlockTypes.has(type);
}

const registerRegex = /^\/user-defined\/(actions|conditions|triggers|variables)\/new$/;
const tabRegex = /^\/user-defined\/(actions|conditions|triggers|variables)$/;

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

    // Route: /user-defined/:blockType (tab route)
    const tabMatch = tabRegex.exec(path);
    if (tabMatch) {
        const blockType = tabMatch[1];
        if (isValidBlockType(blockType)) {
            return <UserDefinedPage initialTab={blockType} />;
        }
    }

    // Route: /user-defined (redirect to default tab)
    if (path === "/user-defined") {
        return <UserDefinedPage initialTab="actions" />;
    }

    // Default route: / (Automation Builder)
    return <AutomationBuilderPage />;
};

export default Router;