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

export function ellipseText(text: string = "", max: number) {
  const parts = text.split(" ").reduce((parts: string[], part, i) => {
    const length = parts.reduce((l, p) => p.length + l, 0);
    if (length + part.length < max && parts.length === i) {
      return [...parts, part];
    } else {
      return parts;
    }
  }, []);
  const joined = parts.join(" ");

  return `${joined}${joined.length < text.length ? "…" : ""}`;
}
