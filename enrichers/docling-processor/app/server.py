from typing import Dict
from typing import List
from fastapi import FastAPI, Body
from docling.document_converter import DocumentConverter
from docling_core.types.io import DocumentStream
from io import BytesIO
from app.utils.fm_helper import FileManagerHelper

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

FMHelper= FileManagerHelper(os.getenv("FM_HOST"))

class Payload(BaseModel):
    tenantId:str
    resources:Dict[str,List[Dict[str,str]]]

class EnrichItemConfig(BaseModel):
    configs:str

class ReplyTo(BaseModel):
    token:str

class Input(BaseModel):
    payload:dict
    enrichItemConfig:dict
    replyTo:str

app = FastAPI()

@app.post("/start-task/")
async def start_task(input:Input):#payload= "pl",enrichItemConfig="Configurazioni",replyTo="fake_token"):
    payload=input.payload
    enrichItemConfig=input.enrichItemConfig
    token=input.replyTo
    thread = threading.Thread(target=operation,kwargs={"payload":payload,"configs":enrichItemConfig,"token":token})
    thread.start()
    return {"status": "ok", "message": f"Proces started"}

def operation(payload,configs,token):
    s_host=os.getenv("S_HOST")

    resourceIds = [
        b.get("resourceId")
        for b in payload["resources"].get("binaries", [])
        if "resourceId" in b
    ]
    tenant=payload["tenantId"]

    resources= [FMHelper.getBase64(tenant,resourceId) for resourceId in resourceIds]
    bites= [ BytesIO(base64.b64decode(resource)) for resource in resources]

    print(f"Starting process")

    source= DocumentStream(name="doc.docx",stream=bites[0])

    converter = DocumentConverter()
    result = converter.convert(source)
    markdown=result.document.export_to_markdown()
    print(f"Process ended")
    res={"markdown": markdown}
    response = requests.post(f"{s_host}/api/datasource/pipeline/callback/{token}", json=res) #body json
    print("Status:", response.status_code)
