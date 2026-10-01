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
import { faCircleCheck } from "@fortawesome/free-solid-svg-icons/faCircleCheck";
import { faCircleExclamation } from "@fortawesome/free-solid-svg-icons/faCircleExclamation";
import { faImage } from "@fortawesome/free-solid-svg-icons/faImage";
import { faXmark } from "@fortawesome/free-solid-svg-icons/faXmark";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";
import { useTranslation } from "react-i18next";
import { css } from "styled-components";
import {
  ALLOWED_CONTENT_TYPES,
  MAX_INPUT_BYTES,
} from "../../../shared/image-query/imageQuery";
import { AttachFileSvg } from "../svgElement/AttachFileSvg";
import { ImageAttachment, useQueryImage } from "./QueryImageContext";

const MAX_INPUT_MB = MAX_INPUT_BYTES / (1024 * 1024);

const visuallyHidden = css`
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip-path: inset(50%);
  white-space: nowrap;
`;

const ellipsis = css`
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
`;

type QueryImageButtonProps = {
  buttonRef?: React.Ref<HTMLButtonElement>;
};

export function QueryImageButton({ buttonRef }: QueryImageButtonProps) {
  const { enabled, attach } = useQueryImage();
  const { t } = useTranslation();
  const inputRef = React.useRef<HTMLInputElement | null>(null);

  if (!enabled) return null;

  return (
    <React.Fragment>
      <button
        ref={buttonRef}
        type="button"
        className="openk9--search-image-button"
        title={t("search-by-image") || ""}
        aria-label={t("search-by-image") || ""}
        css={css`
          display: flex;
          flex-shrink: 0;
          align-items: center;
          justify-content: center;
          width: 36px;
          height: 36px;
          margin-left: var(--openk9-embeddable-search--spacing-sm, 8px);
          padding: 0;
          background: inherit;
          border: none;
          cursor: pointer;
        `}
        onClick={() => inputRef.current?.click()}
      >
        <AttachFileSvg />
      </button>
      <input
        ref={inputRef}
        type="file"
        hidden
        tabIndex={-1}
        accept={ALLOWED_CONTENT_TYPES.join(",")}
        onChange={(event) => {
          const file = event.currentTarget.files?.[0];
          // Reset so choosing the same file again still fires a change.
          event.currentTarget.value = "";
          if (file) attach(file);
        }}
      />
    </React.Fragment>
  );
}

function errorMessage(
  attachment: Extract<ImageAttachment, { status: "error" }>,
  t: (key: string, options?: Record<string, unknown>) => string,
) {
  switch (attachment.errorCode) {
    case "unsupported-type":
      return t("unsupported-file-type");
    case "input-too-large":
    case "too-many-pixels":
      return t("image-too-large", { size: MAX_INPUT_MB });
    default:
      return t("image-not-readable");
  }
}

function formatBytes(bytes: number) {
  return bytes < 1024 * 1024
    ? `${Math.max(1, Math.round(bytes / 1024))} KB`
    : `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

type QueryImagePreviewProps = {
  /** Where focus goes back once the image is removed: the button that attached it. */
  returnFocusRef?: { readonly current: HTMLButtonElement | null };
};

/** The attached image as a small card under the search bar; attaching again replaces it. */
export function QueryImagePreview({ returnFocusRef }: QueryImagePreviewProps) {
  const { attachment, remove } = useQueryImage();
  const { t } = useTranslation();
  // Id of the attachment whose thumbnail failed to load (e.g. a format the browser cannot show).
  const [brokenThumbnail, setBrokenThumbnail] = React.useState<string | null>(
    null,
  );

  const announcement =
    attachment?.status === "pending"
      ? t("preparing-image")
      : attachment?.status === "ready"
      ? t("attached-image", { filename: attachment.filename })
      : "";

  const showThumbnail =
    attachment !== null &&
    attachment.status !== "error" &&
    brokenThumbnail !== attachment.attachmentId;

  return (
    <React.Fragment>
      <span role="status" aria-live="polite" css={visuallyHidden}>
        {announcement}
      </span>
      {attachment && (
        <div
          className="openk9--search-image-preview"
          title={
            attachment.status === "ready"
              ? `${attachment.filename} · ${attachment.width} × ${attachment.height} px`
              : attachment.filename
          }
          css={css`
            display: inline-flex;
            align-items: center;
            gap: var(--openk9-embeddable-search--spacing-sm, 8px);
            max-width: 100%;
            box-sizing: border-box;
            margin-top: var(--openk9-embeddable-search--spacing-sm, 8px);
            padding: 4px 4px 4px 6px;
            border: 1px solid
              ${attachment.status === "error"
                ? "var(--openk9-embeddable-search--error-color, #b3261e)"
                : "var(--openk9-embeddable-search--border-color, #ced4da)"};
            border-radius: var(--openk9-embeddable-search--radius-sm, 8px);
            background-color: ${attachment.status === "error"
              ? "var(--openk9-embeddable-search--error-background-color, #fdecea)"
              : "var(--openk9-embeddable-search--primary-background-color, #ffffff)"};
          `}
        >
          {showThumbnail ? (
            <img
              className="openk9--search-image-thumbnail"
              src={attachment.previewUrl}
              alt=""
              width={28}
              height={28}
              onError={() => setBrokenThumbnail(attachment.attachmentId)}
              css={css`
                flex-shrink: 0;
                object-fit: cover;
                border-radius: var(--openk9-embeddable-search--radius-xs, 4px);
                opacity: ${attachment.status === "pending" ? 0.5 : 1};
              `}
            />
          ) : (
            <span
              aria-hidden="true"
              className="openk9--search-image-thumbnail"
              css={css`
                display: flex;
                flex-shrink: 0;
                align-items: center;
                justify-content: center;
                width: 28px;
                height: 28px;
                border-radius: var(--openk9-embeddable-search--radius-xs, 4px);
                color: var(--openk9-embeddable-search--error-color, #b3261e);
              `}
            >
              <FontAwesomeIcon icon={faImage} />
            </span>
          )}
          <div
            css={css`
              display: flex;
              flex-direction: column;
              min-width: 0;
              line-height: 1.25;
            `}
          >
            <span
              className="openk9--search-image-filename"
              css={css`
                ${ellipsis};
                max-width: 180px;
                font-size: var(--openk9-embeddable-search--font-size-sm, 12px);
                font-weight: 600;
              `}
            >
              {attachment.filename}
            </span>
            {attachment.status === "error" ? (
              <span
                role="alert"
                className="openk9--search-image-error"
                css={css`
                  ${ellipsis};
                  font-size: var(
                    --openk9-embeddable-search--font-size-xs,
                    11px
                  );
                  color: var(--openk9-embeddable-search--error-color, #b3261e);
                `}
              >
                {errorMessage(attachment, t)}
              </span>
            ) : (
              <span
                className="openk9--search-image-size"
                css={css`
                  font-size: var(
                    --openk9-embeddable-search--font-size-xs,
                    11px
                  );
                  opacity: 0.7;
                `}
              >
                {attachment.status === "ready"
                  ? formatBytes(attachment.file.size)
                  : t("preparing-image")}
              </span>
            )}
          </div>
          {attachment.status !== "pending" && (
            <FontAwesomeIcon
              aria-hidden="true"
              icon={
                attachment.status === "ready"
                  ? faCircleCheck
                  : faCircleExclamation
              }
              css={css`
                flex-shrink: 0;
                color: ${attachment.status === "ready"
                  ? "var(--openk9-embeddable-search--success-color, #16a34a)"
                  : "var(--openk9-embeddable-search--error-color, #b3261e)"};
              `}
            />
          )}
          <button
            type="button"
            className="openk9--search-image-remove"
            title={t("remove-image") || ""}
            aria-label={t("remove-image") || ""}
            onClick={() => {
              remove();
              returnFocusRef?.current?.focus();
            }}
            css={css`
              display: flex;
              flex-shrink: 0;
              align-items: center;
              justify-content: center;
              width: 24px;
              height: 24px;
              padding: 0;
              background: inherit;
              border: none;
              border-radius: 50%;
              cursor: pointer;
              color: var(--openk9-embeddable-search--secondary-text-color);
              &:hover {
                background-color: var(
                  --openk9-embeddable-search--secondary-background-color,
                  #eeeeee
                );
              }
            `}
          >
            <FontAwesomeIcon icon={faXmark} />
          </button>
        </div>
      )}
    </React.Fragment>
  );
}
