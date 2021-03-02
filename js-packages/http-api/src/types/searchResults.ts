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

import { DeepKeys, Without } from ".";

export type GenericResultItem<E = {}> = {
  source: {
    type: (keyof E)[];
    contentId: string;
    id: string;
    parsingDate: number; // timestamp
    rawContent: string;
    tenantId: number;
    datasourceId: number;
    entities?: {
      [entity: string]: {
        context: DeepKeys<
          Without<GenericResultItem<E>["source"], "entities">
        >[];
        id: string;
      }[];
    };
  } & E;
  highlight: {
    [field in DeepKeys<
      Without<GenericResultItem<E>["source"], "type" | "entities">
    >]?: string[];
  };
};

export type EmailResultItem = GenericResultItem<{
  email: {
    cc?: string;
    date: number; // timestamp
    htmlBody: string;
    body: string;
    from: string;
    subject: string;
    to: string;
  };
}>;

export type DocumentResultItem = GenericResultItem<{
  document: {
    documentType?: string | null;
    previewUrl: string;
    previewURLs: string[];
    title: string;
    contentType: string;
    content: string;
    URL: string;
  };
  file: {
    path: string;
    lastModifiedDate: number;
  };
  spaces?: {
    spaceName: string;
    spaceId: string;
    URL: string;
  };
}>;

export type ApplicationResultItem = GenericResultItem<{
  application: {
    icon: string;
    description: string;
    title: string;
    URL: string;
  };
}>;

export type ContactResultItem = GenericResultItem<{
  user: {
    birthday: number;
    lastName: string;
    jobTitle: string;
    facebookSn: string;
    screenName: string;
    userId: string;
    twitterSn: string;
    employeeNumber: string;
    skypeSn: string;
    firstName: string;
    emailAddress: string;
    jobClass: string;
    middleName: string;
    male: boolean;
    fullName: string;
    zip?: string;
    country?: string;
    city?: string;
    phoneNumber?: string;
    street?: string;
    portrait_preview?: string;
  };
}>;

export type CalendarResultItem = GenericResultItem<{
  calendar: {
    allDay: boolean;
    calendarBookingId: string;
    description: string;
    location: string;
    startTime: string; // string timestamp int
    endTime: string; // string timestamp int
    title: string; // xml
    titleCurrentValue: string; // real title
  };
}>;
