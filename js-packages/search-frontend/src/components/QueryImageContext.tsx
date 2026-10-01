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
import React from "react";
import {
  forgetQueryImage,
  ImageQueryError,
  ImageQueryErrorCode,
  prepareQueryImageCached,
} from "../../../shared/image-query/imageQuery";
import { newAttachmentId, SearchImage, supportsImageQuery } from "./queryImage";

type AttachmentBase = {
  attachmentId: string;
  file: File;
  previewUrl: string;
  filename: string;
};

export type ImageAttachment =
  | (AttachmentBase & { status: "pending" })
  | (AttachmentBase & { status: "ready"; width: number; height: number })
  | (AttachmentBase & {
      status: "error";
      errorCode: ImageQueryErrorCode | "unknown";
    });

type QueryImageContextValue = {
  enabled: boolean;
  attachment: ImageAttachment | null;
  attach(file: File): void;
  remove(): void;
};

// Outside the provider (components rendered without Main) the control is simply off.
const QueryImageContext = React.createContext<QueryImageContextValue>({
  enabled: false,
  attachment: null,
  attach() {},
  remove() {},
});

function release(attachment: ImageAttachment) {
  forgetQueryImage(attachment.attachmentId);
  URL.revokeObjectURL(attachment.previewUrl);
}

// Kept out of useSelections on purpose: that state goes to the query string and localStorage.
export function QueryImageProvider({
  retrieveType,
  queryImageEnabled,
  children,
}: {
  retrieveType: string | undefined;
  /** The integrator's switch (Configuration.queryImageEnabled); the bucket must also be KNN. */
  queryImageEnabled: boolean;
  children: React.ReactNode;
}) {
  const [attachment, setAttachment] = React.useState<ImageAttachment | null>(
    null,
  );
  // Mirrors the state so replace/remove can release the current image without a stale closure.
  const currentRef = React.useRef<ImageAttachment | null>(null);

  const commit = React.useCallback((next: ImageAttachment | null) => {
    currentRef.current = next;
    setAttachment(next);
  }, []);

  const remove = React.useCallback(() => {
    if (currentRef.current) release(currentRef.current);
    commit(null);
  }, [commit]);

  const attach = React.useCallback(
    (file: File) => {
      if (currentRef.current) release(currentRef.current);
      const base: AttachmentBase = {
        attachmentId: newAttachmentId(),
        file,
        previewUrl: URL.createObjectURL(file),
        filename: file.name,
      };
      commit({ ...base, status: "pending" });
      // Warms the cache the results query reads, so typing never waits on the encoding.
      prepareQueryImageCached(base.attachmentId, file).then(
        ({ width, height }) => {
          if (currentRef.current?.attachmentId !== base.attachmentId) return;
          commit({ ...base, status: "ready", width, height });
        },
        (error: unknown) => {
          if (currentRef.current?.attachmentId !== base.attachmentId) return;
          commit({
            ...base,
            status: "error",
            errorCode:
              error instanceof ImageQueryError ? error.code : "unknown",
          });
        },
      );
    },
    [commit],
  );

  React.useEffect(
    () => () => {
      if (currentRef.current) release(currentRef.current);
    },
    [],
  );

  const enabled = queryImageEnabled && supportsImageQuery(retrieveType);
  const value = React.useMemo<QueryImageContextValue>(
    () => ({
      enabled,
      attachment: enabled ? attachment : null,
      attach,
      remove,
    }),
    [enabled, attachment, attach, remove],
  );

  return (
    <QueryImageContext.Provider value={value}>
      {children}
    </QueryImageContext.Provider>
  );
}

export function useQueryImage() {
  return React.useContext(QueryImageContext);
}

/** The image the results query must send: only once it is ready, and only on a KNN bucket. */
export function useSearchImage(): SearchImage | null {
  const { attachment } = useQueryImage();
  return attachment?.status === "ready"
    ? { attachmentId: attachment.attachmentId, file: attachment.file }
    : null;
}
