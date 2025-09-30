from typing import Dict
from typing import List
from fastapi import FastAPI, Body
from docling.document_converter import DocumentConverter
from docling_core.types.io import DocumentStream
from io import BytesIO

import os
from dotenv import load_dotenv, dotenv_values 
load_dotenv()

# accessing and printing value

import asyncio
import threading
import time
from pydantic import BaseModel
import requests
import base64

class Payload(BaseModel):
    tenantID:str
    resources:Dict[str,List[Dict[str,str]]]

class EnrichItemConfig(BaseModel):
    configs:str

class ReplyTo(BaseModel):
    token:str

class Input(BaseModel):
    payload:Payload
    enrichItemConfig:EnrichItemConfig
    replyTo:ReplyTo

app = FastAPI()

@app.post("/start-task/")
async def start_task(input:Input):#payload= "pl",enrichItemConfig="Configurazioni",replyTo="fake_token"):
    payload=input.payload
    enrichItemConfig=input.enrichItemConfig.configs
    token=input.replyTo.token
    thread = threading.Thread(target=operation,kwargs={"payload":payload,"configs":enrichItemConfig,"token":token})
    thread.start()
    return {"status": "ok", "message": f"Proces started"}

def operation(payload,configs,token):
    s_host=os.getenv("S_HOST")
    fm_host=os.getenv("FM_HOST")

    resourceId=payload.resources["binaries"][0]["resourceId"]
    tenant=payload.tenantID

    response = requests.get(f"{fm_host}/api/file-manager/v1/download/base64/{resourceId}/{tenant}") #body json
    print(response)

    decoded_bytes = base64.b64decode(response.text)
    bites_io = BytesIO(decoded_bytes)#file

    print(f"Starting process")

    source= DocumentStream(name="doc.docx",stream=bites_io)

    converter = DocumentConverter()
    result = converter.convert(source)
    markdown=result.document.export_to_markdown()
    print(f"Process ended")
    res={"markdown": markdown}
    response = requests.post(f"{s_host}/api/datasource/pipeline/callback/{token}", json=res) #body json
    print("Status:", response.status_code)
