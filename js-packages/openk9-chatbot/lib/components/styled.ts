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
import styled from "@emotion/styled";

export const ParagraphTime = styled.p<{ $color?: string }>`
  color: ${(props) => props.$color};
  margin: 0px;
  align-self: end;
  font-size: 10px;
  font-weight: 500;
  line-height: 11.72px;
`;
export const ParagraphMessage = styled.p<{ $color?: string }>`
    color:${(props) => props.$color};
    margin: 0px;
    fontSize: "12px",
    fontWeight: "400",
    lineHeight: "18.25px",
    textAlign: "left",
    `;
