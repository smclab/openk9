## Title and Short Description
A microservice that splits text into chunks using multiple strategies, and generates embeddings using OpenAI, Ollama, IBM Watsonx, Google Vertex AI.

## Description

The OpenK9 Embedding Module is the service responsible for transforming text into high-quality vector embeddings.

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

???

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

???