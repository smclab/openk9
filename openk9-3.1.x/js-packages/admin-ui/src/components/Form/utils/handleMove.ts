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
function not(a: { value: string; label: string }[], b: string[]) {
  return a.filter(({ value }) => !b.includes(value));
}

function intersection(a: { value: string; label: string }[], b: string[]) {
  return a.filter(({ value }) => b.includes(value));
}

export const handleMove = (
  direction: 'right' | 'left',
  association: {
    items: { label: string; value: string }[][];
    setItems: React.Dispatch<React.SetStateAction<{ label: string; value: string }[][]>>;
  },
  leftSelected: string[],
  rightSelected: string[],
  setLeftSelected: React.Dispatch<React.SetStateAction<string[]>>,
  setRightSelected: React.Dispatch<React.SetStateAction<string[]>>,
) => {
  const newLeft = direction === 'right' 
    ? not(association.items[0], leftSelected) 
    : [...association.items[0], ...intersection(association.items[1], rightSelected)];

  const newRight = direction === 'right' 
    ? [...association.items[1], ...intersection(association.items[0], leftSelected)] 
    : not(association.items[1], rightSelected);

  association.setItems([newLeft, newRight]);
  direction === 'right' ? setLeftSelected([]) : setRightSelected([]);
}; 
