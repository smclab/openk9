from fastapi import FastAPI
from io import BytesIO

import asyncio
import base64
import random
from pydantic import BaseModel
import requests

class MarkdownR(BaseModel):
    markdown:str

app = FastAPI()

@app.get("/payload/")
def get_random_string():
    file_path = "external/Requisiti_AI_OpenK9.docx"
    with open(file_path, "rb") as f:
        file_bytes = f.read()
        encoded = base64.b64encode(file_bytes).decode("utf-8")  # stringa base64
    doc=encoded


    input ={
        "payload": 
            {"tenantID":"TEST",
            "resources":{
                "binaries":[
                    {
                        "resourceId":f"{doc}"
                    }
                ]
            }},
        "enrichItemConfig": 
            {"configs":"Config passata"},
        "replyTo":
            {"token":"fake-token"}
    }
    response = requests.post("http://127.0.0.1:8002/start-task/", json=input)
    print("Status:", response.status_code)
    print("Response:", response.json())


@app.post("/api/datasource/pipeline/callback/{token}")
def cose(markdown_response:MarkdownR):
    print("Result: \n",markdown_response.markdown[:200])