"use client";

import { useState } from "react";

interface AdditionalPropertyAdderProps {
    properties: Record<string, unknown>;
    setProperties: (props: Record<string, unknown>) => void;
}

const AdditionalPropertyAdder = ({ properties, setProperties }: AdditionalPropertyAdderProps) => {
    const [keyName, setKeyName] = useState("");
    const [typeName, setTypeName] = useState("string");
    const [error, setError] = useState("");

    const onAddProperty = () => {
        if (!keyName) {
            setError("Key is required");
            return;
        }

        if (keyName in properties) {
            setError("Key already exists");
            return;
        }

        const updatedProperties = {
            ...properties,
            [keyName]: { type: typeName },
        };

        setProperties(updatedProperties);

        setKeyName("");
        setError("");
    };

    return (
        <div className="mt-4 p-2 border rounded border-gray-300">
            <h4 className="mb-2 font-semibold">Add Additional Property</h4>
            <input
                type="text"
                placeholder="Property key"
                value={keyName}
                onChange={(e) => setKeyName(e.target.value)}
                className="border px-2 py-1 mr-2 rounded"
            />
            <select
                value={typeName}
                onChange={(e) => setTypeName(e.target.value)}
                className="border px-2 py-1 mr-2 rounded">
                <option value="string">string</option>
                <option value="number">number</option>
                <option value="boolean">boolean</option>
                <option value="object">object</option>
                <option value="array">array</option>
            </select>
            <button onClick={onAddProperty} className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">
                Add
            </button>
            {error && <div className="mt-1 text-red-600">{error}</div>}
        </div>
    );
};

export default AdditionalPropertyAdder;
