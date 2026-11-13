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
import { useQueryErrorResetBoundary } from "react-query";
import { useTranslation } from "react-i18next";

type SimpleErrorBoundaryProps = {
  children: React.ReactNode;
  resetKey: unknown;
};
type SimpleErrorBoundaryState = { hasError: boolean };

// React already logs the errors a boundary catches, so there is no
// componentDidCatch here: it would only log them a second time.
export class SimpleErrorBoundary extends React.Component<
  SimpleErrorBoundaryProps,
  SimpleErrorBoundaryState
> {
  state: SimpleErrorBoundaryState = { hasError: false };
  static getDerivedStateFromError(): SimpleErrorBoundaryState {
    return { hasError: true };
  }
  componentDidUpdate(
    prevProps: SimpleErrorBoundaryProps,
    prevState: SimpleErrorBoundaryState,
  ) {
    // prevState: only a key that changes after the error resets the boundary
    if (prevState.hasError && prevProps.resetKey !== this.props.resetKey) {
      this.setState({ hasError: false });
    }
  }
  render() {
    if (this.state.hasError) {
      return (
        <RetryButton
          onRetry={() => {
            this.setState({ hasError: false });
          }}
        />
      );
    }
    return this.props.children;
  }
}

function RetryButton({ onRetry }: { onRetry(): void }) {
  const { t } = useTranslation();
  const { reset } = useQueryErrorResetBoundary();
  // type="button": the fallback lives in the host page, possibly inside a form.
  return (
    <button
      type="button"
      onClick={() => {
        // without it the remounted widget throws the cached error again instead of refetching
        reset();
        onRetry();
      }}
    >
      {t("retry")}
    </button>
  );
}
