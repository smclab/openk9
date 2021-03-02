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
