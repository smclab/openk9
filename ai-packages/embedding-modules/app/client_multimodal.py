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

"""Manual smoke client for the multimodal EmbedContent path.

Serves a local image and text over HTTP (standing in for pre-signed
storage URLs), then drives the running Embedding server on
localhost:5000 with a multimodal request combining inline text and refs
(image, text, and an audio ref that gets skipped). The provider is
selected with MM_PROVIDER; credentials travel in the request, as the
datasource would send them.

Start the server first:  python -m app.server

Common configuration:
    MM_PROVIDER   'vertex' or 'bedrock' (default: vertex)
    MM_IMAGE      path to a local image file to index (required)

Vertex (MM_PROVIDER=vertex):
    VERTEX_CREDENTIALS   path to the ADC / service-account JSON
                    (falls back to GOOGLE_APPLICATION_CREDENTIALS)
    VERTEX_PROJECT       GCP project id (required)
    VERTEX_LOCATION      region serving the model (default: europe-west1)
    VERTEX_MODEL         model id (default: multimodalembedding@001)
    VERTEX_DIMENSION     embedding dimension (default: 1408)

Bedrock (MM_PROVIDER=bedrock):
    BEDROCK_API_KEY    Bedrock bearer token (optional if the standard AWS
                    credential chain is configured in the environment)
    BEDROCK_REGION     AWS region (default: us-east-1)
    BEDROCK_MODEL      model id (default: cohere.embed-v4:0)
    BEDROCK_DIMENSION  output dimension (default: 1024)
"""

import json
import os
import threading
from http.server import BaseHTTPRequestHandler, HTTPServer

import grpc
from google.protobuf.struct_pb2 import Struct

from app.external_services.grpc.embedding import embedding_pb2, embedding_pb2_grpc


def serve_files(routes):
    """Serves the given {path: (bytes, content_type)} over a local HTTP
    server on a random port, so the Embedding server can GET them."""

    class Handler(BaseHTTPRequestHandler):
        def do_GET(self):
            body, content_type = routes.get(
                self.path, (b"", "application/octet-stream")
            )
            self.send_response(200)
            self.send_header("Content-Type", content_type)
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)

        def log_message(self, *args):
            # silence the default per-request logging to stderr
            pass

    httpd = HTTPServer(("localhost", 0), Handler)
    threading.Thread(target=httpd.serve_forever, daemon=True).start()

    return httpd


def vertex_model():
    """Builds the EmbeddingModel for the Vertex multimodal provider."""
    credentials_path = (
        os.getenv("VERTEX_CREDENTIALS") or os.environ["GOOGLE_APPLICATION_CREDENTIALS"]
    )
    with open(credentials_path) as f:
        credentials = json.load(f)

    json_config = Struct()
    json_config.update(
        {
            "chat_vertex_ai_model_garden": {
                "credentials": credentials,
                "project": os.environ["VERTEX_PROJECT"],
                "location": os.getenv("VERTEX_LOCATION", "europe-west1"),
                "dimension": int(os.getenv("VERTEX_DIMENSION", "1408")),
            },
        }
    )

    return embedding_pb2.EmbeddingModel(
        multimodal=True,
        providerModel=embedding_pb2.ProviderModel(
            provider="chat_vertex_ai",
            model=os.getenv("VERTEX_MODEL", "multimodalembedding@001"),
        ),
        jsonConfig=json_config,
    )


def bedrock_model():
    """Builds the EmbeddingModel for the Bedrock multimodal provider."""
    json_config = Struct()
    json_config.update(
        {
            "aws_bedrock": {
                "region_name": os.getenv("BEDROCK_REGION", "us-east-1"),
                "output_dimension": int(os.getenv("BEDROCK_DIMENSION", "1024")),
            },
        }
    )

    return embedding_pb2.EmbeddingModel(
        multimodal=True,
        apiKey=os.getenv("BEDROCK_API_KEY", ""),
        providerModel=embedding_pb2.ProviderModel(
            provider="aws_bedrock",
            model=os.getenv("BEDROCK_MODEL", "cohere.embed-v4:0"),
        ),
        jsonConfig=json_config,
    )


MODELS = {"vertex": vertex_model, "bedrock": bedrock_model}


def run(request):
    with grpc.insecure_channel("localhost:5000") as channel:
        stub = embedding_pb2_grpc.EmbeddingStub(channel)
        for chunk in stub.EmbedContent(request):
            file_id = chunk.fileId if chunk.HasField("fileId") else "-"
            total = chunk.total if chunk.HasField("total") else "?"
            head = (
                list(chunk.f32.values[:3])
                if chunk.WhichOneof("vector") == "f32"
                else "n/a"
            )
            print(
                f"chunk {chunk.number}/{total} fileId={file_id} "
                f"dim={chunk.dimension} head={head} text={chunk.text[:45]!r}"
            )


if __name__ == "__main__":
    provider = os.getenv("MM_PROVIDER", "vertex").lower()
    image_path = os.environ["MM_IMAGE"]

    with open(image_path, "rb") as f:
        image_bytes = f.read()

    embedding_model = MODELS[provider]()

    httpd = serve_files(
        {
            "/image": (image_bytes, "image/jpeg"),
            "/note.txt": ("Ricerca semantica cross-modale.".encode(), "text/plain"),
            "/clip.wav": (b"RIFF-fake-audio", "audio/wav"),  # skipped: no handler
        }
    )
    base = f"http://localhost:{httpd.server_address[1]}"

    chunk_json_config = Struct()
    chunk_json_config.update({"chunk_size": 60, "chunk_overlap": 10})

    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        chunk=embedding_pb2.RequestChunk(type=1, jsonConfig=chunk_json_config),
        embeddingModel=embedding_model,
        vectorDataType=embedding_pb2.VECTOR_DATA_TYPE_FLOAT32,
        text="Ricerca semantica.",
        refs=[
            embedding_pb2.MediaRef(
                url=f"{base}/image", fileId="img-1", contentType="image/jpeg"
            ),
            embedding_pb2.MediaRef(
                url=f"{base}/note.txt", fileId="note-1", contentType="text/plain"
            ),
            embedding_pb2.MediaRef(
                url=f"{base}/clip.wav", fileId="clip-1", contentType="audio/wav"
            ),
        ],
    )

    try:
        run(request)
    finally:
        httpd.shutdown()
