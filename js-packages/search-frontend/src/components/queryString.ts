export function loadQueryString<Value>(): Value | null {
  const params = new URLSearchParams(window.location.search);  
  const q = params.get("q");
  if (q) return JSON.parse(q);
  return null;
}

export function saveQueryString<Value>(value: Value) {
  const params = new URLSearchParams(window.location.search);  
  params.set("q", JSON.stringify(value));  
  const url = `${window.location.pathname}?${params.toString()}`;
  window.history.replaceState(null, "", url);
}
