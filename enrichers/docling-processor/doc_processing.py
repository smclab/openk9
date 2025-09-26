from fastapi import FastAPI
from docling.document_converter import DocumentConverter

import asyncio
import threading
import time
import requests
import base64


app = FastAPI()

@app.post("/start-task/")
async def start_task(input:dict= Body(...)):#payload="pl",enrichItemConfig="Configurazioni",replyTo="fake_token"):
    b64_str=input['payload']["doc"]
    enrichItemConfig=input['enrichItemConfig']
    replyTo=input["replyTo"]
    print("Document:", b64_str)
    thread = threading.Thread(target=operation,kwargs={"b64_str":b64_str,"configs":enrichItemConfig,"token":replyTo})
    thread.start()
    return {"status": "ok", "message": f"Processo avviato con parametro {b64_str}"}

def operation(b64_str,configs,token):
    host="http://127.0.0.1:8001"

    decoded_bytes = base64.b64decode(b64_str)

    bites_io = BytesIO(decoded_bytes)
    print(f"Processo {b64_str} iniziato")
    source= DocumentStream(name="doc.pdf",stream=bites_io)

    # converter = DocumentConverter()
    # result = converter.convert(source)
    # markdown=result.document.export_to_markdown()
    # print(markdown)
    # print(f"Processo {b64_str} terminato")

    markdown=decoded_bytes

    headers = {"Content-Type": "text/markdown"}
    response = requests.post(f"{host}/api/datasource/pipeline/callback/{token}", params={"markdown_response": markdown}, headers=headers)

    print("Status:", response.status_code)
    print("Risultato:", response.json())