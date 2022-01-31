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
