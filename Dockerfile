FROM python:3.12.3-alpine3.20

RUN mkdir -p /embedding-module
RUN mkdir -p /embedding-module/logs
WORKDIR /embedding-module/
COPY ./requirements.txt ./
COPY ./embedding.proto ./
COPY ./derived_text_splitter.py ./
COPY ./server.py ./

RUN pip3 install -r requirements.txt
RUN python3 -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. embedding.proto

EXPOSE 5000

CMD ["python3", "server.py"]