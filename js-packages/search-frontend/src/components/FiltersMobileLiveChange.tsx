import React from "react";
import { css } from "styled-components/macro";
import {
  GenericResultItem,
  DetailRendererProps,
  SearchToken,
  SortField,
} from "./client";
import { useRenderers } from "./useRenderers";
import "overlayscrollbars/css/OverlayScrollbars.css";
import { ModalDetail } from "./ModalDetail";
import {
  Configuration,
  ConfigurationUpdateFunction,
} from "../embeddable/entry";
import { FilterSvg } from "../svgElement/FiltersSvg";
import { DeleteLogo } from "./DeleteLogo";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { FiltersMemo } from "./Filters";
import { useInfiniteResults } from "./ResultList";
import { TrashSvg } from "../svgElement/TrashSvg";
import { AddFiltersSvg } from "../svgElement/AddFiltersSvg";

export type FiltersMobileProps<E> = {
  searchQuery: SearchToken[];
  onAddFilterToken: (searchToken: SearchToken) => void;
  onRemoveFilterToken: (searchToken: SearchToken) => void;
  filtersSelect: SearchToken[];
  sort: SortField[];
  dynamicFilters: boolean;
  onConfigurationChange: ConfigurationUpdateFunction;
  configuration: Configuration;
  isVisibleFilters: boolean;
  setIsVisibleFilters:
    | React.Dispatch<React.SetStateAction<boolean>>
    | undefined;
};
function FiltersMobileLiveChange<E>({
  dynamicFilters,
  searchQuery,
  sort,
  onAddFilterToken,
  onRemoveFilterToken,
  onConfigurationChange,
  configuration,
  isVisibleFilters,
  setIsVisibleFilters,
}: FiltersMobileProps<E>) {
  const results = useInfiniteResults<any>(searchQuery, sort);

  const componet = (
    <React.Fragment>
      <div
        css={css`
          display: flex;
          justify-content: space-beetween;
          background: #fafafa;
          justify-content: baseline;
        `}
      >
        <div
          className="openk9-filter-list-container-title box-title"
          css={css`
            padding: 0px 16px;
            width: 100%;
            background: #fafafa;
            padding-top: 20px;
            padding-bottom: 13px;
            display: flex;
          `}
        >
          <div
            className="openk9-filter-list-container-internal-title "
            css={css`
              display: flex;
              gap: 5px;
            `}
          >
            <span>
              <FilterSvg />
            </span>
            <span className="openk9-filters-list-title title">
              <h2
                css={css`
                  font-style: normal;
                  font-weight: 700;
                  font-size: 18px;
                  height: 18px;
                  line-height: 22px;
                  display: flex;
                  align-items: center;
                  color: #3f3f46;
                  margin: 0;
                `}
              >
                Filtri
              </h2>
            </span>
          </div>
        </div>
        <button
          css={css`
            color: var(--openk9-grey-stone-600);
            font-size: 15px;
            font-family: Nunito Sans;
            font-weight: 700;
            line-height: 12px;
            display: flex;
            align-items: center;
            gap: 9px;
            margin-right: 21px;
          `}
          onClick={() => {
            if (setIsVisibleFilters) setIsVisibleFilters(false);
          }}
          style={{ backgroundColor: "#FAFAFA", border: "none" }}
        >
          Chiudi <DeleteLogo heightParam={8} widthParam={8} />
        </button>
      </div>
      <OverlayScrollbarsComponent
        className="openk9-filter-overlay-scrollbars"
        style={{
          overflowY: "auto",
          position: "relative",
          height: "68%",
          borderRadius: "8px",
        }}
      >
        <FiltersMemo
          searchQuery={searchQuery}
          onAddFilterToken={onAddFilterToken}
          onRemoveFilterToken={onRemoveFilterToken}
          onConfigurationChange={onConfigurationChange}
          filtersSelect={configuration.filterTokens}
          sort={sort}
          dynamicFilters={dynamicFilters}
        />
      </OverlayScrollbarsComponent>
      <div
        css={css`
          margin-top: 10px;
          border: 0.5px solid #d4d4d8;
        `}
      ></div>
      <div
        className="openk9-filter-horizontal-container-submit"
        css={css`
          display: flex;
          justify-content: flex-end;
          @media (max-width: 480px) {
            padding-inline: 20px;
            flex-direction: column;
          }
        `}
      >
        <button
          className="openk9-filter-horizontal-submit"
          aria-label="rimuovi filtri"
          css={css`
            font-size: smaller;
            height: 52px;
            padding: 8px 12px;
            white-space: nowrap;
            border: 1px solid #d6012e;
            background-color: #d6012e;
            border-radius: 5px;
            color: white;
            font-weight: 600;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 3px;
            @media (max-width: 480px) {
              background: white;
              border: 1px solid #d6012e;
              width: 100%;
              height: auto;
              margin-top: 20px;
              color: black;
              border-radius: 50px;
              display: flex;
              justify-content: center;
              color: var(--red-tones-500, #c0272b);
              text-align: center;
              font-size: 16px;
              font-style: normal;
              font-weight: 700;
              line-height: normal;
              align-items: center;
            }
          `}
          onClick={() => {
            onConfigurationChange({ filterTokens: [] });
            if (setIsVisibleFilters) setIsVisibleFilters(false);
          }}
        >
          <div>Rimuovi filtri </div>
          <div>
            <TrashSvg size="18px" />
          </div>
        </button>
        <button
          className="openk9-filter-horizontal-submit"
          aria-label="applica filtri"
          css={css`
            font-size: smaller;
            height: 52px;
            padding: 8px 12px;
            white-space: nowrap;
            border: 1px solid #d6012e;
            background-color: #d6012e;
            border-radius: 5px;
            color: white;
            font-weight: 600;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 3px;
            @media (max-width: 480px) {
              background: #d6012e;
              border: 1px solid #d6012e;
              width: 100%;
              height: auto;
              margin-top: 20px;
              color: white;
              border-radius: 50px;
              display: flex;
              justify-content: center;
              text-align: center;
              font-size: 16px;
              font-style: normal;
              font-weight: 700;
              line-height: normal;
            }
          `}
          onClick={() => {
            if (setIsVisibleFilters) setIsVisibleFilters(false);
          }}
        >
          <div>Mostra i {results.data?.pages[0].total} risultati</div>
          <div>
            <AddFiltersSvg size="21px" />
          </div>
        </button>
      </div>
    </React.Fragment>
  );
  if (!isVisibleFilters) return null;
  document.body.style.overflow = "hidden";
  return <ModalDetail padding="0px" background="white" content={componet} />;
}
export const FiltersMobileLiveChangeMemo = React.memo(FiltersMobileLiveChange);
