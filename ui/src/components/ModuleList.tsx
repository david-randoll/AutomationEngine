"use client";

import React, { useState } from "react";
import ModuleListItem from "./ModuleListItem";

interface ModuleListProps {
  title: string;
  modules: ModuleType[];
  area: AreaPlural;
  path: Path;
  onAdd: () => void;
}

function capitalize(s: string) {
  if (!s) return s;
  return s.charAt(0).toUpperCase() + s.slice(1);
}

const ModuleList = ({ title, modules, area, path, onAdd }: ModuleListProps) => {
  const [editingIdx, setEditingIdx] = useState<number | null>(null);

  return (
    <div className="space-y-3">
      <div className="flex justify-between items-center">
        <div className="font-semibold">{title}</div>
        {/* Add button removed here to avoid duplicates */}
      </div>

      {modules.length === 0 && (
        <div className="text-sm text-gray-500">No {title.toLowerCase()} yet.</div>
      )}

      {modules.map((mod, i) => (
        <ModuleListItem
          key={mod.id || i}
          mod={mod}
          isEditing={editingIdx === i}
          onEdit={() => setEditingIdx(i)}
          onCloseEdit={() => setEditingIdx(null)}
          path={[...path, i]}
        />
      ))}
    </div>
  );
};

export default ModuleList;