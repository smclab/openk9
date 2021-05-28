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

export function firstOrNull<T>(arr: T[] | null | undefined) {
  return arr ? arr[0] || null : null;
}

export function firstOrString(arr: string[] | string) {
  if (typeof arr === "string") return arr;
  else return arr[0];
}

export function arrOrEncapsulate(arr: string[] | string) {
  if (typeof arr === "string") return [arr];
  else return arr;
}

export const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

let opIdCounter = 0;
export function opId() {
  return opIdCounter++;
}

let debounceTimers: {
  [key: string]: {
    lastOpId: number;
    lastTimestamp: number;
  };
} = {};

// Debounce an async function using a string key as channel
// myOpId and the ref are used for async function that may sleep in between
// before performing mutation you check if myOpId === opRef.lastOpId
// otherwise a newer function run has been performed
export async function debounce<
  T extends (myOpId: number, opRef: { lastOpId: number }) => void
>(key: string, func: T, delay: number) {
  const thisOpId = opId();
  const thisTimestamp = new Date().getTime();

  if (!debounceTimers[key]) {
    // No record, immediate call
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

  // Are we the last one?
  if (debounceTimers[key].lastOpId === thisOpId) {
    func(thisOpId, debounceTimers[key]);
  }
}
