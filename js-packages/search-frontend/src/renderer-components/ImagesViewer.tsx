import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCircle } from "@fortawesome/free-solid-svg-icons";
import { faChevronCircleLeft } from "@fortawesome/free-solid-svg-icons";
import { faChevronCircleRight } from "@fortawesome/free-solid-svg-icons";
import { css } from "styled-components/macro";

// TODO refactor styles

type ImagesViewerProps = {
  images: Array<string>;
  showPagination: boolean;
};
export function ImagesViewer({ images, showPagination }: ImagesViewerProps) {
  const [selectedIndex, setSelectedIndex] = React.useState(0);
  return (
    <div
      css={css`
        position: relative;
      `}
    >
      <div
        style={{
          display: "flex",
          alignItems: "baseline",
          justifyContent: "space-between",
          position: "absolute",
          width: "100%",
          padding: "8px",
          boxSizing: "border-box",
        }}
      >
        <a
          href="javascript:void(0)"
          onClick={() => setSelectedIndex(Math.max(0, selectedIndex - 1))}
        >
          <FontAwesomeIcon icon={faChevronCircleLeft} color="black" size="lg" />
        </a>
        {showPagination && (
          <div
            css={css`
              flex-grow: 1;
              display: flex;
              flex-wrap: wrap;
              justify-content: center;
              margin: 0px 4px;
            `}
          >
            {new Array(images.length).fill(0).map((_, index, array) => {
              const isSelected = index === selectedIndex;
              return (
                <React.Fragment key={index}>
                  <a
                    href={
                      index === selectedIndex ? undefined : "javascript:void(0)"
                    }
                    onClick={(event) => {
                      event.preventDefault();
                      setSelectedIndex(index);
                    }}
                    ref={(element) => {
                      if (element && isSelected) {
                        element.scrollIntoView();
                      }
                    }}
                    css={css`
                      margin: 2px;
                    `}
                  >
                    <FontAwesomeIcon
                      icon={faCircle}
                      color={
                        isSelected
                          ? "var(--openk9-embeddable-search--active-color)"
                          : "black"
                      }
                      size="xs"
                    />
                  </a>
                </React.Fragment>
              );
            })}
          </div>
        )}
        <a
          href="javascript:void(0)"
          onClick={() =>
            setSelectedIndex(Math.min(images.length - 1, selectedIndex + 1))
          }
        >
          <FontAwesomeIcon
            icon={faChevronCircleRight}
            color="black"
            size="lg"
          />
        </a>
      </div>
      <div
        style={{
          width: "100%",
          height: "50vh",
          backgroundColor: "white",
          borderRadius: "4px",
          border: "1px solid var(--openk9-embeddable-search--border-color)",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        {images[selectedIndex] && (
          <img
            src={images[selectedIndex]}
            alt="preview"
            style={{ maxWidth: "100%", maxHeight: "100%" }}
          />
        )}
      </div>
    </div>
  );
}
