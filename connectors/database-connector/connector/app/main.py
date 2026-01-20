import logging
import os
import threading
from enum import Enum

import requests
import json
from fastapi import FastAPI, status
from pydantic import BaseModel, model_validator
from typing import List, Optional, Self
from database_api.data_extraction import DataExtraction

app = FastAPI()

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
    ingestion_url = "http://ingestion:8080/v1/ingestion/"

logger = logging.getLogger("uvicorn.access")


class Dialect(str, Enum):
    postgresql = "postgresql"
    mysql = "mysql"
    mariadb = "mariadb"
    oracle = "oracle"
    mssql = "mssql"
    firebird = "firebird"
    ibm_db = "ibm_db"
    snowflake = "snowflake"


class Driver(str, Enum):
    psycopg2 = "psycopg2"
    pymysql = "pymysql"
    mysqlconnector = "mysqlconnector"
    pyodbc = "pyodbc"
    pymssql = "pymssql"
    oracledb = "oracledb"
    fdb = "fdb"
    ibm_db = "ibm_db"
    snowflake = "snowflake"

    @classmethod
    def map(cls):
        return {
            Dialect.postgresql: [cls.psycopg2],
            Dialect.mysql: [cls.mysqlconnector, cls.pymysql],
            Dialect.mariadb: [cls.pymysql],
            Dialect.oracle: [cls.oracledb],
            Dialect.mssql: [cls.pyodbc, cls.pymssql],
            Dialect.firebird: [cls.fdb],
            Dialect.ibm_db: [cls.ibm_db],
            Dialect.snowflake: [cls.snowflake],
        }


class DatabaseRequest(BaseModel):
    # TODO: Change in form.json Dialect and Driver to value selection (?)
    dialect: Dialect
    driver: Driver
    username: str
    password: str
    host: str
    port: str
    db: str
    schema: Optional[str] = None
    table: str
    columns: Optional[List[str]] = None
    where: Optional[str] = None
    timestamp: int
    datasourceId: int
    scheduleId: str
    tenantId: str

    @model_validator(mode='after')
    def verify_square(self) -> Self:
        dialect_drive_map = Driver.map()
        usable_drivers = dialect_drive_map[self.dialect]
        if self.driver not in usable_drivers:
            raise ValueError(f'Acceptable drivers for {self.dialect.value} are {[usable_driver.value for usable_driver in usable_drivers]}')
        return self
# TODO: Implement NoSql extraction


@app.post("/execute")
def get_data(request: DatabaseRequest):
    request = request.dict()

    dialect = request["dialect"]
    driver = request["driver"]
    username = request["username"]
    password = request["password"]
    host = request["host"]
    port = request["port"]
    db = request["db"]
    schema = request["schema"]
    table = request["table"]
    columns = request["columns"]
    where = request["where"]
    timestamp = request["timestamp"]
    datasource_id = request["datasourceId"]
    schedule_id = request["scheduleId"]
    tenant_id = request["tenantId"]

    data_extraction = DataExtraction(dialect, driver, username, password,
                                     host, port, db, schema, table, columns, where,
                                     timestamp, datasource_id, schedule_id, tenant_id, ingestion_url)

    thread = threading.Thread(target=data_extraction.extract_recent)
    thread.start()

    return "Extraction Started"


class HealthCheck(BaseModel):
    """Response model to validate and return when performing a health check."""

    status: str = "UP"


@app.get(
    "/health",
    tags=["healthcheck"],
    summary="Perform a Health Check",
    response_description="Return HTTP Status Code 200 (OK)",
    status_code=status.HTTP_200_OK,
    response_model=HealthCheck,
)
def get_health() -> HealthCheck:
    """
    ## Perform a Health Check
    Endpoint to perform a healthcheck on. This endpoint can primarily be used Docker
    to ensure a robust container orchestration and management is in place. Other
    services which rely on proper functioning of the API service will not deploy if this
    endpoint returns any other HTTP status code except 200 (OK).
    Returns:
        HealthCheck: Returns a JSON response with the health status
    """

    try:
        fastapi_response = requests.get("http://localhost:5000/docs")
        if fastapi_response.status_code == 200:
            return HealthCheck(status="UP")
        else:
            return HealthCheck(status="DOWN")
    except requests.RequestException as e:
        logger.error(str(e) + " during request for health check")
        raise e


@app.get("/sample",
        tags=["sample"],
        summary="Get a sample of result",
        response_description="Return json sample result", )
def get_sample():
    f = open('data/sample.json')

    # returns JSON object as
    # a dictionary
    data = json.load(f)

    f.close()

    return data


@app.get("/form",
        tags=["sitemap-form"],
        summary="Get form structure of Sitemap request",
        response_description="Return json form structure", )
def get_sitemap_form():
    f = open('data/form.json')

    # returns JSON object as
    # a dictionary
    data = json.load(f)

    f.close()

    return data