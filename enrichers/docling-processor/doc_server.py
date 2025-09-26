from fastapi import FastAPI
from io import BytesIO

import asyncio
import base64
import random
import requests

app = FastAPI()

STRINGS = [
    "Ciao mondo",
    "FastAPI è fantastico!",
    "Stringa casuale",
    "OpenAI rocks",
    "Python"
]

@app.get("/payload/")
def get_random_string():
    chosen = random.choice(STRINGS)
    encode_bytes= base64.b64encode(chosen.encode("utf-8"))
    decoded_bytes = encode_bytes.decode("utf-8")

    input ={"payload": 
                {"doc":decoded_bytes},
            "enrichItemConfig": 
                {"configs":"Config passata"},
            "replyTo":"fake-token"
            }
    response = requests.post("http://127.0.0.1:8000/start-task/", json=input)
    print("Status:", response.status_code)
    print("Risposta:", response.json())


@app.post("/api/datasource/pipeline/callback/{token}")
def cose(token:str,markdown_response):
    print(token)
    print(markdown_response)