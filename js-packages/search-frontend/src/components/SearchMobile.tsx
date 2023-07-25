export function SearchMobile() {
    export type SearchMobileProps<E> = {
        isVisibleFilters: boolean;
        setIsVisibleFilters:
          | React.Dispatch<React.SetStateAction<boolean>>
          | undefined;
      };
    const componet = (
        // <React.Fragment>
        //   <div
        //     css={css`
        //       display: flex;
        //       justify-content: space-beetween;
        //       background: #fafafa;
        //     `}
        //   >
        //     <div
        //       className="openk9-filter-list-container-title box-title"
        //       css={css`
        //         padding: 0px 16px;
        //         width: 100%;
        //         background: #fafafa;
        //         padding-top: 20px;
        //         padding-bottom: 13px;
        //         display: flex;
        //       `}
        //     >
        //       <div
        //         className="openk9-filter-list-container-internal-title "
        //         css={css`
        //           display: flex;
        //           gap: 5px;
        //         `}
        //       >
        //         <span>
        //           <FilterSvg />
        //         </span>
        //         <span className="openk9-filters-list-title title">
        //           <h2
        //             css={css`
        //               font-style: normal;
        //               font-weight: 700;
        //               font-size: 18px;
        //               height: 18px;
        //               line-height: 22px;
        //               display: flex;
        //               align-items: center;
        //               color: #3f3f46;
        //               margin: 0;
        //             `}
        //           >
        //             Filtri
        //           </h2>
        //         </span>
        //       </div>
        //     </div>
        //     <button
        //       css={css`
        //         color: var(--openk9-grey-stone-600);
        //         font-size: 10px;
        //         font-family: Nunito Sans;
        //         font-weight: 700;
        //         line-height: 12px;
        //         display: flex;
        //         align-items: center;
        //         gap: 9px;
        //         margin-right: 21px;
        //       `}
        //       onClick={() => {
        //         if (setIsVisibleFilters) setIsVisibleFilters(false);
        //       }}
        //       style={{ backgroundColor: "#FAFAFA", border: "none" }}
        //     >
        //       Chiudi <DeleteLogo heightParam={8} widthParam={8} />
        //     </button>
        //   </div>
        //   <FiltersHorizontalMemo
        //     searchQuery={searchQuery}
        //     onAddFilterToken={onAddFilterToken}
        //     onRemoveFilterToken={onRemoveFilterToken}
        //     onConfigurationChange={onConfigurationChange}
        //     onConfigurationChangeExt={() => {
        //       if (setIsVisibleFilters) setIsVisibleFilters(false);
        //     }}
        //     filtersSelect={configuration.filterTokens}
        //     sort={sort}
        //     dynamicFilters={dynamicFilters}
        //   />
        // </React.Fragment>
      );
      if (!isVisibleFilters) return null;
      document.body.style.overflow = "hidden";
      return <ModalDetail padding="0px" background="white" content={componet} />;
    }
    export const FiltersMobileMemo = React.memo(FiltersMobile);
    
