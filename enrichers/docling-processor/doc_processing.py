from fastapi import FastAPI
from docling.document_converter import DocumentConverter

import asyncio
import threading
import time
import requests
import base64


app = FastAPI()

@app.post("/start-task/")
async def start_task():

    url = "http://127.0.0.1:8001/random-string"
    response = requests.get(url)
    data = response.json()
    encoded_str = data["string"]
    decoded_str = base64.b64decode(encoded_str.encode("utf-8")).decode("utf-8")

    print("Base64:", encoded_str)
    print("Decodificata:", decoded_str)
    thread = threading.Thread(target=operation,kwargs={"document":decoded_str})
    thread.start()
    return {"status": "ok", "message": f"Processo avviato con parametro {decoded_str}"}

def operation(document):
    print(f"Processo {document} iniziato")
    time.sleep(10)
    # converter = DocumentConverter()
    # result = converter.convert_string(string=document)
    # print(result.document.export_to_markdown())
    print(f"Processo {document} terminato")