const ALLOWED_PROTOCOLS = ["http:", "https:", "mailto:"];

export function isSafeExternalUrl(url: string | undefined | null): boolean {
  if (!url) return false;
  try {
    return ALLOWED_PROTOCOLS.includes(
      new URL(url, window.location.href).protocol,
    );
  } catch {
    return false;
  }
}

export function safeExternalUrl(
  url: string | undefined | null,
): string | undefined {
  return url && isSafeExternalUrl(url) ? url : undefined;
}
