import { get } from "lodash";
import { HighlightableTextProps } from "./HighlightableText";
import sanitizeHtml from "sanitize-html";
import { css } from "styled-components/macro";
import { truncatedLineStyle } from "./truncatedLineStyle";
import { HighlightedText } from "./HighlightedText";

export function SanitizeHtml<E>({ result, path }: HighlightableTextProps<E>) {
  const hihglithTextLines = result.highlight[path];
  const text = get(result.source, path);
  const sanitizedHTML = sanitizeHtml(text);
  const createMarkup = () => {
    return {
      __html: sanitizedHTML,
    };
  };
  return (
    <div className="openk9-container-sanitize">
      <div
        className="openk9-container-sanitize-truncate"
        css={css`
          ${hihglithTextLines ? truncatedLineStyle : ""};
        `}
      >
        <div
          className="openk9-container-sanitize-text-style"
          css={css`
            height: auto;
            word-wrap: break-word;
            word-break: break-word;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: inherit;
          `}
        >
          <div dangerouslySetInnerHTML={createMarkup()} />
        </div>
      </div>
    </div>
  );
}
