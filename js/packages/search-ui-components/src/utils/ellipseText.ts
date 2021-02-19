export function ellipseText(text: string = "", max: number) {
  const parts = text.split(" ").reduce((parts: string[], part, i) => {
    const length = parts.reduce((l, p) => p.length + l, 0);
    if (length + part.length < max && parts.length === i) {
      return [...parts, part];
    } else {
      return parts;
    }
  }, []);
  const joined = parts.join(" ");

  return `${joined}${joined.length < text.length ? "…" : ""}`;
}
