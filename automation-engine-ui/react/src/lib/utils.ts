import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";
import type { Area } from "@/types/types";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function capitalize(s: string) {
  if (!s) return s;
  return s.charAt(0).toUpperCase() + s.slice(1);
}

const areas: Area[] = ["variable", "trigger", "condition", "action", "result"];

/**
 * Convert a PascalCase/camelCase name with area suffix into an object.
 * Example: "basicVariable" → { variable: "basic" }
 */
export function nameToArea(name?: string): Record<Area, string> | null {
  if (!name) return null;

  for (const area of areas) {
    if (name.toLowerCase().endsWith(area)) {
      const base = name.slice(0, -area.length);
      if (!base) return null;

      const normalizedBase = base.charAt(0).toLowerCase() + base.slice(1);

      return { [area]: normalizedBase } as Record<Area, string>;
    }
  }

  return null; // no match
}

/**
 * Convert an object with one area → back into a name.
 * Example: { variable: "basic" } → "basicVariable"
 */
export function areaToName(obj: Partial<Record<string, unknown>>): string | null {
  for (const area of areas) {
    const value = obj[area];
    if (typeof value === "string" && value.length > 0) {
      const base = value.charAt(0).toLowerCase() + value.slice(1);
      return `${base}${area.charAt(0).toUpperCase()}${area.slice(1)}`;
    }
  }
  return null;
}
