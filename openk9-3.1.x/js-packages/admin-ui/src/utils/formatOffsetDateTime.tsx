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
export function formatOffsetDateTime(offsetDateTime: string): string {
  const date = new Date(offsetDateTime);
  const timeZoneOffset = date.getTimezoneOffset();

  const hoursOffset = Math.floor(Math.abs(timeZoneOffset) / 60);
  const minutesOffset = Math.abs(timeZoneOffset) % 60;
  const sign = timeZoneOffset > 0 ? "-" : "+";
  const formattedOffset = `${sign}${String(hoursOffset).padStart(2, "0")}:${String(minutesOffset).padStart(2, "0")}`;

  const formattedDate = date.toISOString().slice(0, 19);

  return `${formattedDate}${formattedOffset}`;
}

