---
id: gen-ai-features
title: Openk9 Chat with data features
---

# Openk9 Generative Search Documentation

This document describes the features and functionalities of the Openk9 product related to the use of generative artificial intelligence.

## Architecture and Features

The diagram illustrates how the Openk9 product has evolved by introducing new search functionalities based on:

- Generative artificial intelligence using Large Language Models (LLMs)  
- Semantic/vector search using vector databases (e.g., Opensearch) and data embeddings  
- Use of [Retrieval Augmented Generation (RAG)](https://en.wikipedia.org/wiki/Retrieval-augmented_generation)

By leveraging these tools, Openk9 enables generative search experiences of the *chat with your data* type.

The user asks questions, and the engine/assistant responds conversationally using relevant information retrieved from the knowledge base of documents indexed in Openk9.

The solution is agnostic with respect to:

- **LLMs used**: It can connect to proprietary or cloud-based services, or on-premises LLMs.  
- **Embedding models used**: It can connect to proprietary or cloud-based services, or on-premises models.

The new generative search functionalities inherit from the classic search engine features such as:

- Multilingual search  
- Single Sign-On (SSO) support  
- Search profiling using Keycloak as the identity manager  

**If the user is logged in and data profiling is enabled, conversational assistants will respond using only the information indexed in the knowledge base they have actual visibility into.**

The RAG component is implemented by:

- Using Opensearch as the search engine/vector database to retrieve information via textual match, vector search, or hybrid search  
- Using the [Langchain](https://python.langchain.com/docs/introduction/) library to develop services  
- Supporting result reranking mechanisms  
- Optionally storing user conversations  

## Implemented Experiences

Using the tools mentioned above, Openk9 implements different experiences with various purposes.

All the described experiences are accessible through **HEADLESS REST APIs**.  
The document includes the OpenAPI file with the Swagger documentation for the provided Openk9 services.

The following experiences are shown via dedicated interfaces implemented in the Openk9 suite and are available as frontend libraries, allowing rapid and efficient development of externally embeddable interfaces.

### Generative Widget on Search Engine

It’s possible to integrate a widget into a classic or semantic search experience that shows a short answer related to the search/question asked by the user, using the documents found by the engine as context.

This is a Q&A experience and does not implement a conversational approach.

It quickly provides the user with useful and relevant information.

An example follows.

### Conversational Virtual Assistant

Openk9 provides different interfaces that allow users to interact conversationally and with *memory* with the data indexed in the knowledge base.

The user can ask questions, and Openk9 responds:

- Conversationally  
- Keeping the history of the conversation and using query reformulation techniques  
- Displaying and citing the sources used to respond  
- Storing (for logged-in users) past chats and allowing users to resume conversations later  

#### Desktop Interface

The chatbot interface allows the user to interact conversationally and chat with the knowledge base data.

Additionally, when logged in, users can interact with the chat history to view, edit, or resume conversations.

The user can start a new conversation at any time via the appropriate CTA.

Example shown: conversation using data from the Italian Revenue Agency website.

#### Chatbot Interface

This chatbot interface enables conversational interaction with the knowledge base data.

It follows standard chatbot features and supports multilingual interaction.

Users can start a new conversation at any time via the CTA.

The welcome message can be customized, and new user messages can be added.

Example shown: conversation using data from the Italian Revenue Agency website.

## RAG Experience Configuration

All experiences can be configured from the admin panel.

A view of the RAG configuration area from the admin panel is shown.

In this area, prompting configurations for RAG and question reformulation can be managed.

Prompting allows you to constrain the conversational assistant to respond according to specific rules, helping avoid hallucinations, for example.
