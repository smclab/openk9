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

export class SimpleErrorBoundary extends React.Component<{
  children: React.ReactNode;
}> {
  state = { hasError: false };
  componentDidCatch(error: unknown, errorInfo: unknown) {
    this.setState({ hasError: true });
  }
  render() {
    if (this.state.hasError) {
      return (
        <button
          onClick={() => {
            this.setState({ hasError: false });
          }}
        >
          retry
        </button>
      );
    }
    return this.props.children;
  }
}

