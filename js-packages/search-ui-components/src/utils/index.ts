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

export * from "./ellipseText";
export * from "./dynamicPluginLoader";

/**
 * Access the first item in an array safely, even if the array is null.
 */
export function firstOrNull<T>(arr: T[] | null | undefined) {
  return arr ? arr[0] || null : null;
}

/**
 * Returns the first element of an array, or a string if it's not an array.
 */
export function firstOrString(arr: string[] | string) {
  if (typeof arr === "string") return arr;
  else return arr[0];
}

/**
 * If the given parameter is a string, it encapsulates it into a 1-item array.
 */
export function arrOrEncapsulate(arr: string[] | string) {
  if (typeof arr === "string") return [arr];
  else return arr;
}

/**
 * Capitalize the first letter of a string.
 */
export function capitalize(s: string) {
  return s.charAt(0).toUpperCase() + s.substring(1);
}

/**
 * A modulo operation that doesn't return negative numbers, to perform wrapping.
 * @example
 * 2 % 4 = 2
 * circularMod(2, 4) = 2
 * -2 % 4 = -2
 * circularMod(-2, 4) = 2
 */
export function circularMod(n: number, mod: number) {
  return ((n % mod) + mod) % mod;
}

/**
 * Does absolutely nothing, almost like the developer!
 * Use this to silence silly warnings in ESLint.
 */
export const noop = () => {};

/**
 * Waits for ms milliseconds. You can await me since I'm a Promise!
 */
export const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

let opIdCounter = 0;
/**
 * Returns a progressive monotone numeric id.
 * Actually, it may be not monotone when it will wrap after a large number.
 * But actually JS uses floating point numbers... loss of precision?
 * Caveat emptor!
 */
export function opId() {
  return opIdCounter++;
}

let debounceTimers: {
  [key: string]: {
    lastOpId: number;
    lastTimestamp: number;
  };
} = {};

/**
 * Debounce an async function using a string key as channel.
 * myOpId and the ref are used for async function that may sleep in between.
 * Before performing mutation check if myOpId === opRef.lastOpId, otherwise a newer function run has been performed!
 *
 * @example
 * // We check the opId, since if an older longProcess() call takes less than a newer one,
 * // we may loose the results of the latest and set the older result instead!
 * debounce(
 *   "myProcess",
 *   async function (myOpId, opRef) {
 *     const value = await longProcess();
 *     if (opRef.lastOpId === myOpId)
 *       setState(value);
 *   },
 *   500,
 * );
 */

export async function debounce<
  T extends (myOpId: number, opRef: { lastOpId: number }) => void
>(key: string, func: T, delay: number) {
  const thisOpId = opId();
  const thisTimestamp = new Date().getTime();

  if (!debounceTimers[key]) {
    // No record in the channels map, this is the first time, so perform immediate call
    debounceTimers[key] = {
      lastTimestamp: thisTimestamp,
      lastOpId: thisOpId,
    };
    func(thisOpId, debounceTimers[key]);
    return;
  }

  const timeDiff = thisTimestamp - (debounceTimers[key].lastTimestamp + delay);

  // If we are the last one we set our id as last, otherwise we exit
  if (debounceTimers[key].lastOpId > thisOpId) return;
  debounceTimers[key].lastOpId = thisOpId;
  debounceTimers[key].lastTimestamp = thisTimestamp;

  // Last call not so long ago, sleep for the remaining time
  if (timeDiff < 0) {
    await sleep(-timeDiff);
  }

  // Are we the last one now? If so we call our fn
  if (debounceTimers[key].lastOpId === thisOpId) {
    func(thisOpId, debounceTimers[key]);
  }
}

/**
 * Utility to merge react refs into a single one.
 */
export function mergeRefs<T = any>(
  refs: Array<React.MutableRefObject<T> | React.LegacyRef<T>>,
): React.RefCallback<T> {
  return (value) => {
    refs.forEach((ref) => {
      if (typeof ref === "function") {
        ref(value);
      } else if (ref != null) {
        (ref as React.MutableRefObject<T | null>).current = value;
      }
    });
  };
}
