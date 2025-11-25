## Title and Short Description
An enricher that asynchronously processes documents (using Docling), extracts their Markdown content, and sends the result back to an Openk9 pipeline callback.

## Description

This enricher asynchronously proces documents with a pipeline built on FastAPI.
Given a payload by Openk9, it retrieves binary resources from a file-manager service, converts supported documents through Docling into Markdown format, and posts the processed result back to a callback URL (replyTo).

The transformation is executed in a background thread so the API returns immediately while the processing continues.

Main features:

- Fetches binary files from an external File Manager

- Converts documents using Docling (.docx, .pdf, and other supported formats)

- Sends results to an Openk9 callback endpoint

- Health check endpoint

- Configuration schema endpoint for Openk9 UI

## Quickstart

## API Reference

## Configuration

## License