import ClayDropDown from "@clayui/drop-down";
import fuzzy from "fuzzy";
import React from "react";

export interface IProps extends React.ComponentProps<typeof ClayDropDown.Item> {
  innerRef?: React.Ref<HTMLAnchorElement>;

  /**
   * Match is the string that will be compared with value.
   */
  match?: string;

  /**
   * Value is the string that will be compared to the characters of
   * the match property.
   */
  value: string;

  icon?: JSX.Element;
}

const optionsFuzzy = { post: "</strong>", pre: "<strong>" };

export const AutocompleteItemIcon = React.forwardRef<HTMLLIElement, IProps>(
  ({ innerRef, icon, match = "", value, ...otherProps }: IProps, ref) => {
    const fuzzyMatch = fuzzy.match(match, value, optionsFuzzy);

    return (
      <ClayDropDown.Item {...otherProps} innerRef={innerRef} ref={ref}>
        <span className="inline-item inline-item-before">{icon}</span>

        {match && fuzzyMatch ? (
          <div
            dangerouslySetInnerHTML={{
              __html: fuzzyMatch.rendered,
            }}
          />
        ) : (
          value
        )}
      </ClayDropDown.Item>
    );
  },
);
