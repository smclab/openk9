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
import { useCallback, useEffect, useRef } from "react";

export function convertToIntOrFallback(stringToConvert: string | null): number {
  const parsed = parseInt(stringToConvert ?? "");
  return isNaN(parsed) ? 0 : parsed;
}

export function getTabIndexOfNode(targetNode: HTMLElement): number {
  return convertToIntOrFallback(targetNode.getAttribute("tabindex"));
}

function sanitizeTabIndexInput(
  tabIndex: number,
  highestPositiveTabIndex: number
): number {
  if (tabIndex < 0) {
    throw new Error(
      `Unable to sort given input. A negative value is not part of the tab order: ${tabIndex}`
    );
  }
  return tabIndex === 0 ? highestPositiveTabIndex + 1 : tabIndex;
}

export function sortByTabIndex(
  firstNode: HTMLElement,
  secondNode: HTMLElement
): number {
  const tabIndexes = [firstNode, secondNode].map((node) =>
    getTabIndexOfNode(node)
  );
  return tabIndexes
    .map((tabIndexValue) =>
      sanitizeTabIndexInput(tabIndexValue, Math.max(...tabIndexes))
    )
    .reduce((previousValue, currentValue) => previousValue - currentValue);
}

const focusableElementsSelector =
  "a[href], area[href], input:not([disabled]):not([type=hidden]), select:not([disabled]), textarea:not([disabled]), button:not([disabled]), iframe, object, embed, *[tabindex], *[contenteditable]";
const TAB_KEY = 9;

export function useFocusTrap(
  isActive: boolean
): [React.RefObject<HTMLDivElement>] {
  const trapRef = useRef<HTMLDivElement>(null);

  const selectNextFocusableElem = useCallback(
    (
      sortedFocusableElems: HTMLElement[],
      currentIndex?: number,
      shiftKeyPressed: boolean = false,
      skipCount: number = 0
    ): void => {
      if (skipCount > sortedFocusableElems.length) {
        return;
      }

      const backwards = !!shiftKeyPressed;
      const maxIndex = sortedFocusableElems.length - 1;

      if (currentIndex === undefined) {
        currentIndex =
          sortedFocusableElems.indexOf(document.activeElement as HTMLElement) ??
          0;
      }

      let nextIndex = backwards ? currentIndex - 1 : currentIndex + 1;

      if (nextIndex > maxIndex) {
        nextIndex = 0;
      }

      if (nextIndex < 0) {
        nextIndex = maxIndex;
      }

      const newFocusElem = sortedFocusableElems[nextIndex];

      newFocusElem.focus();

      if (document.activeElement !== newFocusElem) {
        selectNextFocusableElem(
          sortedFocusableElems,
          nextIndex,
          shiftKeyPressed,
          skipCount + 1
        );
      }
    },
    []
  );

  const trapper = useCallback(
    (evt: KeyboardEvent) => {
      const trapRefElem = trapRef.current;

      if (trapRefElem !== null) {
        if (evt.which === TAB_KEY || evt.key === "Tab") {
          evt.preventDefault();
          const shiftKeyPressed = !!evt.shiftKey;

          let focusableElems = Array.from(
            trapRefElem.querySelectorAll<HTMLElement>(focusableElementsSelector)
          ).filter(
            (focusableElement) => getTabIndexOfNode(focusableElement) >= 0
          );

          focusableElems = focusableElems.sort(sortByTabIndex);

          selectNextFocusableElem(focusableElems, undefined, shiftKeyPressed);
        }
      }
    },
    [selectNextFocusableElem]
  );

  useEffect(() => {
    if (isActive) {
      window.addEventListener("keydown", trapper);
    }

    return () => {
      if (isActive) {
        window.removeEventListener("keydown", trapper);
      }
    };
  }, [isActive, trapper]);

  return [trapRef];
}

