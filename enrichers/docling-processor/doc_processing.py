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

def operation(document):
    print(f"Processo {document} iniziato")
    time.sleep(10)
    # converter = DocumentConverter()
    # result = converter.convert_string(string=document)
    # print(result.document.export_to_markdown())
    print(f"Processo {document} terminato")