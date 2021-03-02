export * from "./ellipseText";
export * from "./dynamicPluginLoader";

export function firstOrNull<T>(arr: T[] | null | undefined) {
  return arr ? arr[0] || null : null;
}

export function firstOrString(arr: string[] | string) {
  if (typeof arr === "string") return arr;
  else return arr[0];
}

export function arrOrEncapsulate(arr: string[] | string) {
  if (typeof arr === "string") return [arr];
  else return arr;
}
