from fastapi import FastAPI
import asyncio
import base64
import random

app = FastAPI()

STRINGS = [
    "Ciao mondo",
    "FastAPI è fantastico!",
    "Stringa casuale",
    "OpenAI rocks",
    "Python ❤️"
]

@app.get("/random-string")
def get_random_string():
    chosen = random.choice(STRINGS)
    print(f"Chosen: {chosen}")
    encoded_str = base64.b64encode(chosen.encode("utf-8")).decode("utf-8")
    return {"string": encoded_str}