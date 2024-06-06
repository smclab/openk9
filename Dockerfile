FROM python:3.12.3-alpine3.20

COPY ./requirements.txt /requirements.txt
RUN pip3 install -r /requirements.txt

COPY ./embedding.proto /embedding.proto
COPY ./derived_text_splitter.py /derived_text_splitter.py
COPY ./server.py /server.py
RUN python3 -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. embedding.proto

EXPOSE 5000

CMD ["python3", "server.py"]