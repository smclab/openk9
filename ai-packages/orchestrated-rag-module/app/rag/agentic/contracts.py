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
Contratti tipati tra i livelli dell'orchestrazione agentica (design §4).

Sono normali ``pydantic.BaseModel`` (stesso stile di ``app/models/models.py``). In PydanticAI
diventano ``output_type`` degli agent o parametri tipati dei tool.
"""

from typing import Literal, Optional, Union

from pydantic import BaseModel, Field, field_validator

# ── Orchestratore → sub-agent (contratto comune a TUTTI i sub-agent-tool) ──


class SubAgentRequest(BaseModel):
    """Richiesta dell'orchestratore verso un sub-agent (oggi: Discovery Agent)."""

    question: str = Field(
        ..., description="Sotto-domanda autoconsistente per il sub-agent"
    )
    hints: str = Field(
        "",
        description="Indicazioni dell'orchestratore: dominio atteso, vincoli, cosa cercare",
    )


# ── Discovery Agent → Retriever (funzione vera) ──


class RetrieveRequest(BaseModel):
    """Parametri di retrieval scelti dal Discovery Agent.

    ``top_k`` / ``range`` / ``chunk_window`` NON stanno qui: vengono da ``RAGConfiguration``.
    """

    question: str = Field(..., description="Query di retrieval")
    domains: Optional[list[str]] = Field(
        None,
        description=(
            "Filtro dominio. I valori ammessi sono iniettati come XML <domains> nel system "
            "prompt del Discovery e validati a runtime contro get_domains(). Un dominio "
            "inventato viene scartato (= nessun filtro)."
        ),
    )


# ── Discovery Agent — output dell'LLM (sceglie SOLO gli id) ──


class DiscoveryUnanswerable(BaseModel):
    """L'LLM del Discovery dichiara che i documenti trovati non permettono di rispondere."""

    reason: str = Field(
        ..., description="Perche' non e' rispondibile dai documenti trovati"
    )


class DiscoverySelection(BaseModel):
    """L'LLM del Discovery seleziona gli id pertinenti tra i risultati del retriever."""

    document_ids: list[str] = Field(
        ..., description="document_id scelti tra i risultati del retriever"
    )


#: output_type del Discovery Agent (union tipata)
DiscoveryLLMOutput = Union[DiscoveryUnanswerable, DiscoverySelection]


# ── Post-processing del Discovery: id → contenuto (chunk raggruppati per document_id) ──


class DiscoveredDocument(BaseModel):
    """Documento assemblato dal post-processing deterministico del Discovery.

    Niente ``score``: confonde il modello (design §4).
    """

    document_id: str
    content: str = Field(
        ..., description="chunk uniti (riuso di get_context_window_merged)"
    )
    title: Optional[str] = Field(
        None, description="opzionale: dipende dal dynamic_metadata del tenant"
    )
    domain: Optional[str] = None


class DiscoveryResult(BaseModel):
    """Cio' che l'orchestratore riceve dal Discovery Agent: id + chunk dei doc scelti."""

    documents: list[DiscoveredDocument] = Field(default_factory=list)


#: Ciò che arriva effettivamente all'orchestratore dal tool di delega
DiscoveryOutcome = Union[DiscoveryUnanswerable, DiscoveryResult]


# ── Documenti citati, per l'evento SSE DOCUMENT ──


class DocumentRef(BaseModel):
    """Riferimento a un documento citato, per l'evento SSE DOCUMENT."""

    document_id: str
    title: Optional[str] = None
    url: Optional[str] = None
    score: Optional[float] = None
    domain: Optional[str] = None
    #: numero della fonte nei marcatori ``[n]`` della risposta; ``None`` se la risposta non ne ha
    ordinal: Optional[int] = None
    #: citazioni testuali prese dalla fonte, con ``retrieve_citations`` attivo
    quotes: Optional[list[str]] = None


# ── Orchestratore — output finale ──


class OrchestratorOutput(BaseModel):
    """Output finale dell'orchestratore (output_type dell'Agent)."""

    status: Literal["ok", "unanswerable", "error"]
    message: str
    used_document_ids: list[str] = Field(
        default_factory=list,
        description=(
            "id effettivamente citati nel comporre la risposta; risolti a DocumentRef (via i "
            "risultati Discovery accumulati) per l'evento SSE DOCUMENT"
        ),
    )
    citations: dict[str, list[str]] = Field(
        default_factory=dict,
        description=(
            "quotes per marked source number, copied verbatim from the source; "
            "only when the instructions ask for them"
        ),
    )

    @field_validator("citations", mode="before")
    @classmethod
    def lenient_citations(cls, v):
        # a streamed run cannot ask the model to fix its output: malformed quotes are dropped,
        # not allowed to fail the turn (the quotes are checked against the sources anyway)
        if not isinstance(v, dict):
            return {}
        return {
            str(key): [q for q in (raw if isinstance(raw, list) else [raw]) if isinstance(q, str)]
            for key, raw in v.items()
        }


# ── Verifier (opzionale) — output dell'LLM che valuta OrchestratorOutput ──


class VerificationOk(BaseModel):
    """La verifica passa: l'output dell'orchestratore e' adeguato."""

    status: Literal["ok"] = "ok"


class UnsupportedClaim(BaseModel):
    """Affermazione della risposta che i documenti non supportano, con l'azione per ripararla."""

    claim: str = Field(..., description="The answer's claim, quoted as written")
    action: Literal["search", "remove"] = Field(
        ...,
        description=(
            "search: the documents do not mention it, a further search may find it; "
            "remove: the documents contradict it, or it cannot come from documents at all"
        ),
    )


class VerificationFailed(BaseModel):
    """La verifica fallisce: diventa il messaggio di ``ModelRetry`` con le istruzioni di
    riparazione per l'orchestratore (vedi ``orchestrator.repair_instructions``)."""

    reason: str = Field(
        ..., description="Perche' la risposta non e' adeguata → alimenta ModelRetry"
    )
    unsupported_claims: list[UnsupportedClaim] = Field(
        default_factory=list,
        description="Every claim of the answer that the documents do not support",
    )


#: output_type del Verifier (union tipata)
VerifierOutput = Union[VerificationOk, VerificationFailed]
