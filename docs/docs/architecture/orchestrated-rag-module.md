---
id: orchestrated-rag-module
title: Orchestrated Rag Module
slug: /orchestrated-rag-module

---

# Orchestrated Rag Module

The Orchestrated Rag Module answers questions on the documents of a tenant, like the [Agentic Rag Module](agentic-rag-module), with the same API and the same chat storage. An installation uses one of the two modules, never both: the API Gateway sends the RAG requests to the one it is pointed at.

Instead of a fixed pipeline, an **orchestrator** agent decides how to search and delegates each search to a **Discovery** agent. The agents run on [PydanticAI](https://ai.pydantic.dev), on the model of the tenant.

## How a turn is answered

1. **Input guardrail.** The question is compared with the guardrail documents of the tenant; when it comes close to one, the provider of the tenant classifies it, and a blocked question ends the turn.
2. **Intent detection.** The domains of the question, from the domain documents of the tenant or, failing that, from the model, become the domain filter of the searches.
3. **Orchestrator.** It writes a sub-question for what the documents should say and delegates it to the Discovery, with `discover`. It can delegate again: to identify something the question only describes, to rephrase after a search that found nothing, or to repair a claim the Verifier rejected. With `parallel_discovery` it can split a question with independent parts, such as a comparison, into sub-questions that run in parallel, with `discover_many`.
4. **Discovery.** It searches OpenSearch, at most three times, and selects the documents that answer its sub-question. It returns only their ids; the code assembles their content, so the Discovery cannot invent text.
5. **Answer.** The orchestrator answers only from the documents delivered to it, marking each sentence with the number of its source, `[n]`. It is streamed as it is written, through the output guardrail of the tenant.
6. **Verifier** (with `enable_verifier`). It checks every claim of the answer against the delivered documents and the conversation; the orchestrator searches or removes the unsupported claims and answers again. The answer is buffered until it is verified.

A question made of an image alone skips the stages that read the written question: the image goes straight to a single search.

## Chat history

A logged user's chat is saved on OpenSearch in the same documents as the Agentic Rag Module: the chat routes, the retention and a switch between the two modules keep the conversations. An anonymous user's history travels with each request.

## Events

Besides the events of the Agentic Rag Module, the module sends `STATUS` events before the answer starts, with the phase of the turn: `planning`, `discovering`, `retrieving`, `evaluating`, `generating`. The [module README](https://github.com/smclab/openk9/tree/main/ai-packages/orchestrated-rag-module#sse-events) lists every event.

## Configuration

The module reads the RAG configuration of the tenant: some fields are its own, some are shared with the Agentic Rag Module, some are ignored. The [module README](https://github.com/smclab/openk9/tree/main/ai-packages/orchestrated-rag-module#rag-configuration) lists them.
