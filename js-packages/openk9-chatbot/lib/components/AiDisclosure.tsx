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
import React from "react";

/**
 * Permanent notice that the conversation is held with an AI system, required by
 * Regulation (EU) 2024/1689 (AI Act) art. 50 §1 for systems interacting directly
 * with people. It is deliberately static text and not an `aria-live` region: it
 * never changes, and a live region would have it re-read at every new message.
 * The input references it through `aria-describedby`, so a screen reader announces
 * it when the field takes focus.
 *
 * `text` is always resolved by the caller, so this component has no way to render
 * nothing: the disclosure is not something an integrator can turn off.
 */
export default function AiDisclosure({
  id,
  text,
}: {
  /**
   * carried only by the instance the input points at through `aria-describedby`:
   * when the notice is shown in both slots the other one renders without an id,
   * so the document never holds two nodes with the same one.
   */
  id?: string;
  text: React.ReactNode;
}): React.ReactNode {
  return (
    <div className="openk9-ai-disclosure">
      <p id={id}>{text}</p>
    </div>
  );
}
