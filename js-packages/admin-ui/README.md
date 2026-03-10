# OpenK9 Admin UI

The **OpenK9 Admin UI** is the management frontend for the OpenK9 platform. Built with React and TypeScript, it allows administrators to configure, monitor, and manage the core features of OpenK9, including datasources, analyzers, enrich items, and search pipelines.

## Key Features
- **Datasources Management:** Create and configure connections to external data systems.
- **Analyzers & Tokenizers:** Define custom text analysis pipelines for Elasticsearch/OpenSearch.
- **Enrich Items & Pipelines:** Configure dynamic data enrichment flows using external services or Groovy scripting.
- **RAG Configuration:** Setup and manage Retrieval-Augmented Generation flows using LLMs and Embedding Models.
- **Search Configs:** Manage and test specific search layouts and configurations.
- **Index Management:** Triggers and rules for Data Indices and Reindexing operations.

## Tech Stack
- **Framework:** React 19 (Modernized with Vite)
- **Language:** TypeScript 5
- **UI Components:** Material UI (MUI) v6 with Styled Components
- **Data Fetching:** 
  - `@apollo/client` for GraphQL APIs
  - `@tanstack/react-query` & OpenApi client for REST APIs
- **Routing:** React Router v6

## Getting Started

### Prerequisites
- Node.js
- Yarn package manager

### Installation

Clone the repository and install the dependencies:

```bash
yarn install
```

### Running Locally

To start the development server:

```bash
yarn start
```
This runs the app in the development mode using Vite. Open [http://localhost:3000/admin](http://localhost:3000/admin) to view it in the browser.

## Available Scripts

In the project directory, you can run the following scripts defined in `package.json`:

- `yarn start`: Runs the app in development mode using Vite.
- `yarn build`: Compiles TypeScript and builds the app for production to the `dist` folder using Vite. It produces a highly optimized bundle with Gzip/Brotli compression.
- `yarn test`: Launches the test runner using Vitest.
- `yarn compile-graphql`: Watches for changes in `.graphql` queries and regenerates the TypeScript types and Apollo hooks in `src/graphql-generated.ts`.
- `yarn compile-openapi`: Fetches the latest OpenAPI specification from the backend and generates the TypeScript REST client in `src/openapi-generated/`.
