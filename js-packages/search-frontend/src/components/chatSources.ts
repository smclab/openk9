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
import { ChatSource } from "./client";

/** a cited document as a list renders it: a readable label and an openable url */
export type Citation = { url: string; label: string; source: ChatSource };

/**
 * The document's real title, resolved over the fields the `DOCUMENT` event
 * actually carries: the title mapped by the tenant's RAG configuration first,
 * then the file name of an uploaded document. The raw url is only a last
 * resort; the technical source name is not a title, so it is not in the chain.
 */
export function resolveSourceTitle(source: ChatSource): string | undefined {
  if (source.title !== undefined) return source.title;
  if (source.filename !== undefined) {
    return source.filename + (source.file_extension ?? "");
  }
  return source.url;
}

/**
 * The citations of one answer, as they are shown. A source with no reachable
 * destination is left out rather than rendered as an entry the user cannot
 * open. Shared by the embeddable copilot panel and the standalone app so both
 * present the same list.
 */
export function toCitations(sources: ChatSource[] | undefined): Citation[] {
  return (sources ?? []).flatMap((source) => {
    const url = source.url;
    if (url === undefined) return [];
    return [{ url, label: resolveSourceTitle(source) ?? url, source }];
  });
}
