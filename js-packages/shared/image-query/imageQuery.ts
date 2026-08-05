/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

// Always produces a JPEG within the searcher's limits: image/* content type and 2 MiB of decoded bytes.
// No workers and no blob URLs: the embeddable runs inside customer pages with a strict CSP.

/** Max long edge sent: past 1568x1568 (~2.46 MP) production multimodal models downscale anyway. */
export const MAX_EDGE_PX = 1568;

/** Fallback long edge, used only when no quality step fits the cap at 1568 px. */
export const FALLBACK_EDGE_PX = 1024;

/** Searcher cap, measured on decoded bytes rather than base64 characters. */
export const MAX_BYTES = 2 * 1024 * 1024;

export const QUALITY_LADDER = [0.85, 0.75, 0.65, 0.55];

/** Explicit allowlist, not `image/*` (Chrome cannot decode HEIC); `accept` alone misses drag & drop. */
export const ALLOWED_CONTENT_TYPES = [
  "image/jpeg",
  "image/png",
  "image/webp",
  "image/avif",
  "image/gif",
];

/** Input guards: keep an oversized file from killing the browser tab. */
export const MAX_INPUT_BYTES = 25 * 1024 * 1024;
export const MAX_INPUT_MEGAPIXELS = 50;

/** Shape of the `media` field on the KNN token. */
export type QueryImage = {
  /** Pure base64, no `data:` prefix: the backend maps it to `byte[]`. */
  data: string;
  contentType: "image/jpeg";
  width: number;
  height: number;
  bytes: number;
};

export type ImageQueryErrorCode =
  | "unsupported-type"
  | "input-too-large"
  | "too-many-pixels"
  | "decode-failed"
  | "encode-failed";

/** Every failure here is local: nothing has been sent to the searcher. */
export class ImageQueryError extends Error {
  readonly code: ImageQueryErrorCode;

  constructor(code: ImageQueryErrorCode, message: string) {
    super(message);
    this.name = "ImageQueryError";
    this.code = code;
  }
}

export type TargetSize = { width: number; height: number; scaled: boolean };

// TypeScript 4.9 (still used by talk-to) has no "from-image" in `ImageOrientation`; the cast keeps
// this shared module compiling there. Drop it once every app is on TypeScript 5.
const FROM_IMAGE = "from-image" as unknown as ImageBitmapOptions["imageOrientation"];

export function computeTargetSize(
  width: number,
  height: number,
  maxEdge: number,
): TargetSize {
  const longEdge = Math.max(width, height);
  if (longEdge <= maxEdge) {
    return { width, height, scaled: false };
  }
  const ratio = maxEdge / longEdge;
  return {
    width: Math.max(1, Math.round(width * ratio)),
    height: Math.max(1, Math.round(height * ratio)),
    scaled: true,
  };
}

/** Call in the fetch path, not component state: react-query serializes the query key every render. */
export async function prepareQueryImage(file: File): Promise<QueryImage> {
  if (!ALLOWED_CONTENT_TYPES.includes(file.type)) {
    throw new ImageQueryError(
      "unsupported-type",
      `content type non supportato: ${file.type || "sconosciuto"}`,
    );
  }
  if (file.size > MAX_INPUT_BYTES) {
    throw new ImageQueryError("input-too-large", `file di ${file.size} byte`);
  }

  const decoded = await decode(file);

  try {
    // Checked after decode (dimensions unknown before) but before any canvas: 100 MP is ~400 MB of RGBA.
    const megapixels = (decoded.width * decoded.height) / 1_000_000;
    if (megapixels > MAX_INPUT_MEGAPIXELS) {
      throw new ImageQueryError(
        "too-many-pixels",
        `immagine di ${megapixels.toFixed(1)} MP`,
      );
    }

    const target = computeTargetSize(decoded.width, decoded.height, MAX_EDGE_PX);
    let canvas = downscale(decoded, target);
    let blob = await encodeUnderBudget(canvas);

    if (blob === null) {
      canvas = downscale(
        decoded,
        computeTargetSize(target.width, target.height, FALLBACK_EDGE_PX),
      );
      blob = await toJpegBlob(canvas, 0.75);
      if (blob.size > MAX_BYTES) {
        throw new ImageQueryError(
          "encode-failed",
          `non si scende sotto il tetto: ${blob.size} byte`,
        );
      }
    }

    return {
      data: await toBase64(blob),
      contentType: "image/jpeg",
      width: canvas.width,
      height: canvas.height,
      bytes: blob.size,
    };
  } finally {
    // ImageBitmap holds the decoded pixels: close it now instead of waiting for the GC.
    if (typeof ImageBitmap !== "undefined" && decoded.source instanceof ImageBitmap) {
      decoded.source.close();
    }
  }
}

const preparedByAttachment = new Map<string, Promise<QueryImage>>();

export function prepareQueryImageCached(
  attachmentId: string,
  file: File,
): Promise<QueryImage> {
  const cached = preparedByAttachment.get(attachmentId);
  if (cached !== undefined) {
    return cached;
  }
  const pending = prepareQueryImage(file);
  preparedByAttachment.set(attachmentId, pending);
  // Never cache a rejection, otherwise a retry would replay the error without re-running.
  pending.catch(() => preparedByAttachment.delete(attachmentId));
  return pending;
}

export function forgetQueryImage(attachmentId: string): void {
  preparedByAttachment.delete(attachmentId);
}

type Decoded = {
  source: ImageBitmap | HTMLImageElement;
  width: number;
  height: number;
};

async function decode(file: File): Promise<Decoded> {
  try {
    if (await honoursExifOrientation()) {
      const bitmap = await createImageBitmap(file, {
        imageOrientation: FROM_IMAGE,
      });
      return { source: bitmap, width: bitmap.width, height: bitmap.height };
    }

    // Fallback: an <img> applies EXIF orientation in every modern browser
    // (`image-orientation: from-image` is the CSS default), at the cost of a full-resolution decode.
    const image = new Image();
    image.src = await readDataUrl(file);
    await image.decode();
    return {
      source: image,
      width: image.naturalWidth,
      height: image.naturalHeight,
    };
  } catch (cause) {
    throw new ImageQueryError(
      "decode-failed",
      `immagine non decodificabile (${String(cause)})`,
    );
  }
}

// Canvas ignores EXIF orientation where <img> honours it, and the `imageOrientation` default has
// varied across browsers, so pass it explicitly and then verify it was applied.
let orientationSupport: Promise<boolean> | undefined;

function honoursExifOrientation(): Promise<boolean> {
  if (orientationSupport === undefined) {
    orientationSupport = probeExifOrientation().catch(() => false);
  }
  return orientationSupport;
}

async function probeExifOrientation(): Promise<boolean> {
  if (typeof createImageBitmap !== "function") {
    return false;
  }
  const probe = await buildExifRotatedJpeg();
  const bitmap = await createImageBitmap(probe, {
    imageOrientation: FROM_IMAGE,
  });
  // The probe is 2x1 with Orientation = 6, so honouring the tag means it comes back 1x2.
  const honoured = bitmap.width === 1 && bitmap.height === 2;
  bitmap.close();
  return honoured;
}

async function buildExifRotatedJpeg(): Promise<Blob> {
  const canvas = document.createElement("canvas");
  canvas.width = 2;
  canvas.height = 1;
  const context = canvas.getContext("2d");
  if (context === null) {
    throw new Error("canvas 2d non disponibile");
  }
  context.fillStyle = "#000000";
  context.fillRect(0, 0, 1, 1);
  context.fillStyle = "#ffffff";
  context.fillRect(1, 0, 1, 1);

  const plain = new Uint8Array(
    await (await toJpegBlob(canvas, 0.9)).arrayBuffer(),
  );

  const exifHeader = [0x45, 0x78, 0x69, 0x66, 0x00, 0x00]; // "Exif\0\0"
  const tiff = [
    0x49, 0x49, 0x2a, 0x00, 0x08, 0x00, 0x00, 0x00, // little endian, IFD0 at offset 8
    0x01, 0x00, // one entry
    0x12, 0x01, 0x03, 0x00, 0x01, 0x00, 0x00, 0x00, // tag 0x0112 (Orientation), SHORT, count 1
    0x06, 0x00, 0x00, 0x00, // value 6
    0x00, 0x00, 0x00, 0x00, // no further IFD
  ];
  const length = 2 + exifHeader.length + tiff.length;
  const app1 = Uint8Array.from([
    0xff,
    0xe1,
    (length >> 8) & 0xff,
    length & 0xff,
    ...exifHeader,
    ...tiff,
  ]);

  // Canvas JPEGs start with SOI then APP0/JFIF; strict parsers skip an APP1 placed before APP0.
  let at = 2;
  if (plain[2] === 0xff && plain[3] === 0xe0) {
    at = ((plain[4] << 8) | plain[5]) + 4;
  }

  const out = new Uint8Array(plain.length + app1.length);
  out.set(plain.subarray(0, at), 0);
  out.set(app1, at);
  out.set(plain.subarray(at), at + app1.length);
  return new Blob([out], { type: "image/jpeg" });
}

function downscale(decoded: Decoded, target: TargetSize): HTMLCanvasElement {
  let current: CanvasImageSource = decoded.source;
  let width = decoded.width;
  let height = decoded.height;

  // Halve in steps: one drawImage from 4032 to 1568 px aliases thin strokes in technical drawings.
  while (
    Math.floor(width / 2) >= target.width &&
    Math.floor(height / 2) >= target.height
  ) {
    width = Math.floor(width / 2);
    height = Math.floor(height / 2);
    current = drawTo(current, width, height);
  }

  return drawTo(current, target.width, target.height);
}

function drawTo(
  source: CanvasImageSource,
  width: number,
  height: number,
): HTMLCanvasElement {
  const canvas = document.createElement("canvas");
  canvas.width = width;
  canvas.height = height;
  const context = canvas.getContext("2d");
  if (context === null) {
    throw new ImageQueryError("decode-failed", "canvas 2d non disponibile");
  }
  context.imageSmoothingEnabled = true;
  context.imageSmoothingQuality = "high";
  context.drawImage(source, 0, 0, width, height);
  return canvas;
}

async function encodeUnderBudget(
  canvas: HTMLCanvasElement,
): Promise<Blob | null> {
  for (const quality of QUALITY_LADDER) {
    const blob = await toJpegBlob(canvas, quality);
    if (blob.size <= MAX_BYTES) {
      return blob;
    }
  }
  return null;
}

function toJpegBlob(canvas: HTMLCanvasElement, quality: number): Promise<Blob> {
  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) =>
        blob !== null
          ? resolve(blob)
          : reject(
              new ImageQueryError("encode-failed", "toBlob ha restituito null"),
            ),
      "image/jpeg",
      quality,
    );
  });
}

// FileReader instead of `btoa(String.fromCharCode(...bytes))`, which blows the stack on large
// arrays; `media.data` wants pure base64, so keep only what follows the comma.
async function toBase64(blob: Blob): Promise<string> {
  const dataUrl = await readDataUrl(blob);
  return dataUrl.slice(dataUrl.indexOf(",") + 1);
}

function readDataUrl(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = () =>
      reject(reader.error ?? new Error("FileReader senza errore"));
    reader.readAsDataURL(blob);
  });
}

