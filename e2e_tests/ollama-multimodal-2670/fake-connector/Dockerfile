FROM python:3.12-slim
WORKDIR /app
# Il contesto e' la cartella dello scenario (docker-compose.fake-connector.yml);
# il corpus e' montato come volume.
COPY fake-connector/connector.py /app/connector.py
EXPOSE 8080
CMD ["python", "/app/connector.py"]
