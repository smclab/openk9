## Title and Short Description
A microservice that splits text into chunks using multiple strategies, and generates embeddings using OpenAI, Ollama, IBM Watsonx, Google Vertex AI.

## Description

The OpenK9 Embedding Module is the service responsible for transforming text into vector embeddings.

It is an essential component of OpenK9’s ingestion pipeline, where documents are:

1. Chunked according to configured strategies

2. Embedded using pluggable vector models

3. Returned as structured embedding chunks to be indexed into OpenK9

This module provides:

- Multiple chunking strategies, such as:
    - `Recursive`
    - `Sentence-based`
    - `Token-based`
    - `Semantic`
    - `Table-aware`
    - `Neural chunking`

- Provider-agnostic embedding generation, supporting:
    - `OpenAI`
    - `Ollama local models`
    - `IBM Watsonx`
    - `Google Vertex AI`

- gRPC interface used internally by OpenK9 services

- Health checks and reflection for easier integration


## Quickstart

???

## API Reference

The Embedding module provides a gRPC service to generate vector embeddings from textual input.

## Configuration
**ChunkType**:
| ChunkType           | Key |
|---------------------|--------|
| DEFAULT             | 0      |
| TEXT_SPLITTER       | 1      |
| TOKEN_TEXT_SPLITTER | 2      |
| CHARACTER_TEXT_SPLITTER | 3  |
| SEMANTIC_SPLITTER   | 4      |
| SENTENCE_SPLITTER   | 5      |
| RECURSIVE_SPLITTER  | 6      |
| TABLE_CHUNKER       | 7      |
| LATE_CHUNKER        | 8      |
| NEURAL_CHUNKER      | 9      |



## License

Copyright (c) the respective contributors, as shown by the AUTHORS file.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License
along with this program. If not, see http://www.gnu.org/licenses/.