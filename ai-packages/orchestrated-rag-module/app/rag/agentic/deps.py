#
# Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

"""
Dipendenze iniettate (``deps_type`` di PydanticAI) per orchestratore e Discovery Agent.

Portano config per-richiesta (RAGConfiguration, LLM config, host servizi, tenant/user/chat) e i
**modelli** da usare. ``retrieve_fn`` e' iniettabile: default = ``retrieval.retrieve_documents``
(OpenSearch reale), ma nei test si passa uno stub → il loop agentico e' validabile **offline**.
"""

from dataclasses import dataclass, field
from typing import Any, Callable, Optional

from app.rag.agentic import retrieval


@dataclass
class AgenticDeps:
    """Contesto per-richiesta condiviso da orchestratore e sub-agent (dependency injection)."""

    rag_configuration: dict
    llm_configuration: dict
    opensearch_host: str
    grpc_host_datasource: str
    grpc_host_embedding: str
    tenant_id: str
    # modelli PydanticAI (build_model): orchestratore e discovery possono differire
    orchestrator_model: Any = None
    discovery_model: Any = None
    user_id: Optional[str] = None
    chat_id: Optional[str] = None
    #: domanda utente corrente (impostata da run_orchestrator; usata dal Verifier per il giudizio)
    question: Optional[str] = None
    #: storia della conversazione prima del turno (impostata da run_orchestrator): il Verifier la
    #: legge per capire una domanda di follow-up, che da sola non ha senso
    message_history: list = field(default_factory=list)
    #: domini ammessi (get_domains) iniettati nel prompt del Discovery e validati a runtime
    allowed_domains: list[str] = field(default_factory=list)
    #: domini rilevati dall'intent detection per QUESTA query (equivalente di ``state.domain``):
    #: diventano il filtro ``keywordKey="domain"`` del retrieval. Distinti da ``allowed_domains``,
    #: che resta la whitelist completa per prompt e validazione: sovrascrivere quella con i domini
    #: rilevati impedirebbe all'LLM di scegliere qualunque dominio fuori dalla detection.
    detected_domains: list[str] = field(default_factory=list)
    #: ``datasourceIds`` della request: diventano il filtro ``DATASOURCE`` del retrieval
    datasource_ids: list[int] = field(default_factory=list)
    #: parametri di ricerca della richiesta passati a ogni ricerca: ``jwt`` ed ``extra`` (con
    #: l'header ``OPENK9_ACL``) decidono l'ACL applicata dal searcher, senza i quali un utente
    #: loggato vede solo i documenti pubblici; ``sort``, ``sort_after_key``, ``language``
    request_search_args: dict = field(default_factory=dict)
    #: token ``filter=True`` di ``searchQuery`` (faccette, date): si aggiungono al testo cercato
    filter_tokens: list = field(default_factory=list)
    #: immagine della richiesta (#2500): va nel token di ogni ricerca, insieme al testo
    media: Any = None
    #: funzione di retrieval principale; iniettabile per i test (default: OpenSearch reale)
    retrieve_fn: Callable[..., list[dict]] = retrieval.retrieve_documents
    #: accumulatore run-scoped dei candidati scoperti (id -> record) per risolvere used_document_ids
    #: → DocumentRef nell'evento SSE DOCUMENT. Popolato dal Discovery ad ogni delega.
    discovered: dict = field(default_factory=dict)
    #: id dei documenti consegnati all'orchestratore (selezionati dal Discovery), in ordine di
    #: consegna: sono il contesto su cui il Verifier giudica la risposta. ``discovered`` contiene
    #: anche i candidati scartati, che l'orchestratore non ha mai visto.
    delivered_ids: list[str] = field(default_factory=list)
    #: numero di fonte di ogni documento consegnato all'orchestratore (id -> n), assegnato alla
    #: consegna e stabile nel turno: e' il ``[n]`` dei marcatori della risposta
    source_ordinals: dict = field(default_factory=dict)
    #: sotto-domande delegate al Discovery in questo turno (tetto: ``MAX_DISCOVERIES_PER_TURN``)
    discoveries_run: int = 0
    #: token stimati dei documenti gia' consegnati all'orchestratore in questo turno
    delivered_tokens: float = 0.0
    #: sink delle fasi STATUS, iniettato dall'entrypoint. I tool (``discover``, ``retriever``)
    #: girano dentro ``agent.run``, parecchi frame sotto il generator che produce gli eventi SSE:
    #: non possono cederli direttamente, quindi li consegnano qui. ``None`` (default) = nessun
    #: sink → :meth:`emit_status` no-op, per i test offline.
    status_sink: Optional[Callable[[str], None]] = None
    #: fasi già pubblicate in questa run (vedi :meth:`emit_status`)
    emitted_phases: set = field(default_factory=set)

    def emit_status(self, phase: str) -> None:
        """Consegna una fase al sink, **al massimo una volta per run**.

        Un turno invoca ``discover``/``retriever`` piu' volte: senza la deduplica il frontend
        riceverebbe eventi identici e l'indicatore, che mostra una sola label, sfarfallerebbe tra
        le stesse due fasi. Una fase sola per run rende la progressione monotona (planning →
        discovering → retrieving → evaluating → generating).
        """
        if self.status_sink is None or phase in self.emitted_phases:
            return
        self.emitted_phases.add(phase)
        self.status_sink(phase)

    def retrieve_top_k_args(self) -> dict:
        """Parametri di retrieval derivati da RAGConfiguration (NON scelti dall'LLM)."""
        cfg = self.rag_configuration
        return {
            # retrieve_type viene dal Bucket via GetLLMConfigurations (KNN/HYBRID/TEXT), non
            # dalla RAGConfiguration: leggerlo da cfg forzava sempre il fallback HYBRID.
            "retrieve_type": self.llm_configuration.get("retrieve_type") or "HYBRID",
            # context_window vive nella config LLM, non nella RAGConfiguration: leggerlo
            # da cfg dava 0 -> max_tokens=0 -> tutti i chunk scartati (0 candidati).
            "context_window": self.llm_configuration.get("context_window", 0),
            # la chiave del dict e' ``range_values`` (get_rag_configuration), non ``range``:
            # leggerla come ``range`` scartava il Range Start/End della RAGConfiguration.
            # ``or []`` e non un default di get(): la chiave c'e' sempre, ma il protobuf
            # restituisce lista vuota quando il campo non e' valorizzato.
            "range_values": cfg.get("range_values") or [],
            "rerank": cfg.get("rerank", False),
            "reranker_api_url": cfg.get("reranker_api_url", ""),
            "chunk_window": cfg.get("chunk_window", 0),
            "metadata": cfg.get("metadata"),
            "score_threshold": cfg.get("score_threshold"),
        }
