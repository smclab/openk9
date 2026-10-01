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
import { prepareQueryImageCached } from "../../../shared/image-query/imageQuery";
import { QueryMedia, SearchToken } from "./client";

/** The image the results query sends: only the id goes in the query key, the file is encoded on fetch. */
export type SearchImage = { attachmentId: string; file: File };

/** The searcher accepts `media` on KNN tokens only; anything else answers 400. */
export function supportsImageQuery(retrieveType: string | undefined) {
  return retrieveType === "KNN";
}

let attachmentCounter = 0;

/** Unique within the page; crypto.randomUUID needs a secure context the widget cannot count on. */
export function newAttachmentId() {
  attachmentCounter += 1;
  return `query-image-${Date.now()}-${attachmentCounter}`;
}

/** Puts the image on the first KNN search token, or adds an image-only KNN token when the text gave none. */
export function withQueryImage(
  searchQuery: SearchToken[],
  media: QueryMedia,
): SearchToken[] {
  const index = searchQuery.findIndex(
    (token) => token.tokenType === "KNN" && token.search,
  );
  if (index === -1) {
    return [
      ...searchQuery,
      { tokenType: "KNN", values: [], filter: false, media },
    ];
  }
  return searchQuery.map((token, position) =>
    position === index && token.tokenType === "KNN"
      ? { ...token, media }
      : token,
  );
}

export async function resolveQueryImage(
  searchQuery: SearchToken[],
  image: SearchImage | null,
): Promise<SearchToken[]> {
  if (!image) return searchQuery;
  const { data, contentType } = await prepareQueryImageCached(
    image.attachmentId,
    image.file,
  );
  return withQueryImage(searchQuery, { data, contentType });
}
