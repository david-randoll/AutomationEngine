import type { UIMode } from "@/types/types";
import { FaCode, FaWpforms, FaProjectDiagram } from "react-icons/fa";

interface ModeSelectorProps {
    mode: UIMode;
    onModeChange: (mode: UIMode) => void;
}

const modes: { value: UIMode; label: string; icon: React.ReactNode; description: string }[] = [
    {
        value: "interactive",
        label: "Interactive",
        icon: <FaWpforms className="w-4 h-4" />,
        description: "Form-based editing",
    },
    {
        value: "code",
        label: "Code",
        icon: <FaCode className="w-4 h-4" />,
        description: "JSON/YAML editor",
    },
    {
        value: "workflow",
        label: "Workflow",
        icon: <FaProjectDiagram className="w-4 h-4" />,
        description: "Visual canvas",
    },
];

const ModeSelector = ({ mode, onModeChange }: ModeSelectorProps) => {
    return (
        <div className="inline-flex rounded-lg border border-gray-200 bg-white p-1 shadow-sm">
            {modes.map((m) => (
                <button
                    key={m.value}
                    onClick={() => onModeChange(m.value)}
                    className={`
                        flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-md transition-all duration-200
                        ${
                            mode === m.value
                                ? "bg-blue-600 text-white shadow-sm"
                                : "text-gray-600 hover:text-gray-900 hover:bg-gray-100"
                        }
                    `}
                    title={m.description}
                >
                    {m.icon}
                    <span className="hidden sm:inline">{m.label}</span>
                </button>
            ))}
        </div>
    );
};

export default ModeSelector;
