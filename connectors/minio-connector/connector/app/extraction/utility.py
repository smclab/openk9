import traceback
import os
import base64
from dataclasses import dataclass
from io import BytesIO
from pathlib import Path
from typing import Union, Callable

import pandas as pd
import requests
import logging
from datetime import datetime
from logging.config import dictConfig

from pandas import DataFrame

from .log_config import LogConfig

dictConfig(LogConfig().dict())

logger = logging.getLogger("status-logger")


def get_as_base64(response):
    data = base64.b64encode(response).decode('utf-8')
    return data


def log_error_location(exception: Exception) -> None:
    tb = traceback.extract_tb(exception.__traceback__)
    for frame in tb:
        logger.error(f"File: {os.path.relpath(frame.filename)}, line {frame.lineno}, in {frame.name}")


def has_valid_header(df: DataFrame) -> bool:
    # Checks if columns are all strings
    return all([
        isinstance(el, str)
        for el in df.columns
    ])


class IngestionHandler:
    def __init__(self, ingestion_url, datasource_id, schedule_id, tenant_id, do_raise_error: bool = True):
        self.ingestion_url = ingestion_url
        self.datasource_id = datasource_id
        self.schedule_id = schedule_id
        self.tenant_id = tenant_id
        self.do_raise_error = do_raise_error

        self.status_logger = logging.getLogger("status-logger")

    def get_end_timestamp(self) -> float:
        return datetime.utcnow().timestamp() * 1000

    def post_message(self, payload):
        try:
            r = requests.post(self.ingestion_url, json=payload, timeout=20)
            if r.status_code == 200:
                return
            else:
                r.raise_for_status()
        except Exception as e:
            logger.error(str(e) + " during request at url: " + str(self.ingestion_url))
            if self.do_raise_error:
                log_error_location(e)
                raise e

    def post_halt(self, exception: Exception, end_timestamp: float | None):
        end_timestamp = end_timestamp if end_timestamp else self.get_end_timestamp()

        payload = {
            "datasourceId": self.datasource_id,
            "scheduleId": self.schedule_id,
            "tenantId": self.tenant_id,
            "contentId": -1,
            "parsingDate": int(end_timestamp),
            "rawContent": str(exception),
            "datasourcePayload": {

            },
            "resources": {
                "binaries": []
            },
            "type": "HALT"
        }
        self.status_logger.error(exception)
        self.post_message(payload)

    def post_last(self, end_timestamp: float | None):
        end_timestamp = end_timestamp if end_timestamp else self.get_end_timestamp()

        payload = {
            "datasourceId": self.datasource_id,
            "parsingDate": int(end_timestamp),
            "contentId": None,
            "rawContent": None,
            "datasourcePayload": {},
            "resources": {
                "binaries": []
            },
            "scheduleId": self.schedule_id,
            "tenantId": self.tenant_id,
            "last": True
        }
        self.post_message(payload)


@dataclass
class FileExtensionMethod:
    file_type_name: str
    extensions: set[str]
    method: Callable[[bytes], DataFrame]

    def contains(self, extension: str) -> bool:
        return extension in self.extensions


class FileExtractor:
    def __init__(self, do_try_extract_header: bool):
        self.do_try_extract_header = do_try_extract_header

        self._file_extension_methods: list[FileExtensionMethod] = [
            FileExtensionMethod(
                file_type_name = "excel",
                # odf engine: .odf, .ods, .odt
                # xlrd engine: .xls
                # pyxlsb engine: .xlsb
                # openpyxl engine: everything else
                extensions={".xls", ".xlsx", ".xlsm", ".xlsb", ".odf", ".ods", ".odt"},
                method=self.__extract_excel
            ),
            FileExtensionMethod(
                file_type_name="csv",
                # .csv: L'estensione standard universale.
                # .txt: Spesso utilizzata quando il file usa delimitatori diversi dalla virgola (come tab o pipe) o per semplice compatibilità testuale.
                # .tsv: Specifico per i file Tab-Separated Values (valori separati da tabulazioni).
                # .dat: Un'estensione generica per file di dati, spesso formattati come CSV all'interno.
                # .log: File di registro che salvano i dati in formato tabellare.
                # .bak: Copie di backup di database che mantengono la struttura CSV.
                # .prn: Vecchio formato di esportazione per la stampa, talvolta strutturato a colonne fisse.
                extensions={".csv", ".txt", ".tsv", ".dat", ".log", ".bak", ".prn"},
                method=self.__extract_csv
            ),
        ]

    def extract_file(self, object_name: str, data: bytes) -> Union[DataFrame, None]:
        file_extension = Path(object_name).suffix
        if not file_extension:
            logger.debug(f"Skipped object {object_name}. This is not a File.")
            return None

        for file_extension_method in self._file_extension_methods:
            try:
                if file_extension_method.contains(extension=file_extension):
                    logger.info(f"Trying to extract file: {object_name} as {file_extension_method.file_type_name}")
                    return file_extension_method.method(data)
            except Exception as e:
                logger.warning(f"[FileExtractor] Error extracting file data {object_name} as {file_extension_method.file_type_name}.", exc_info=e)
                continue

        logger.warning(f"Skipped file {object_name}. Extension {file_extension} not supported.")
        return None

    def __extract_excel(self, data: bytes) -> DataFrame:
        try:
            data = BytesIO(data)
            df = pd.read_excel(data, header=0 if self.do_try_extract_header else None)
            if self.do_try_extract_header and not has_valid_header(df):
                df = pd.read_excel(data, header=None)
            return df
        except Exception:
            raise

    def __extract_csv(self, data: bytes) -> DataFrame:
        try:
            data = BytesIO(data)
            # CSV will always have header
            df = pd.read_csv(data)
            return df
        except Exception:
            raise
