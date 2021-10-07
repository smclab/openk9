import React from "react";

export function EmbedElement(props: { element: Element }) {
  return (
    <div
      ref={(element) => {
        if (element) {
          for (const child of element.children) {
            element?.removeChild(child);
          }
          element.appendChild(props.element);
        }
      }}
    ></div>
  );
}
