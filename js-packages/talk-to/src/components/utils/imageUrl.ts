// Kept out of DocumentPreview.tsx: that file imports ESM-only react-markdown, which Jest 27 (CRA)
// cannot load, making these pure functions untestable there.

const IMAGE_EXT = /\.(png|jpe?g|gif|webp|svg|bmp|avif|ico)$/i;

export function isImageUrl(url?: string): boolean {
	return !!url && IMAGE_EXT.test(url.split(/[?#]/)[0]);
}

export function fileNameFromUrl(url: string, fallback = "document"): string {
	try {
		const parsed = new URL(url, window.location.href);
		const last = parsed.pathname.split("/").filter(Boolean).pop();
		return last ? decodeURIComponent(last) : parsed.hostname || fallback;
	} catch {
		const clean = (url || "").split(/[?#]/)[0];
		const last = clean.split("/").filter(Boolean).pop();
		return last || fallback;
	}
}
