import enum
import logging
from logging.config import dictConfig
from typing import Dict, Optional

from pydantic import BaseModel


class LogLevel(enum.StrEnum):
    DEBUG = logging.getLevelName(logging.DEBUG)
    INFO = logging.getLevelName(logging.INFO)
    WARNING = logging.getLevelName(logging.WARNING)
    ERROR = logging.getLevelName(logging.ERROR)
    CRITICAL = logging.getLevelName(logging.CRITICAL)


class LogConfig(BaseModel):
    """
    Logging configuration

    USAGE:
        LogConfig.get_logger(
            logger_name: Optional[str] = None,
            log_level: Optional[str] = None)
    """

    LOGGER_NAME: str = "BaseLogger"
    LOG_FORMAT: str = f"%(levelname)s: | %(asctime)s | %(filename)s:%(lineno)d | %(message)s"
    LOG_LEVEL: str = LogLevel.DEBUG

    # Logging config
    version: int = 1
    disable_existing_loggers: bool = False
    formatters: Dict = {
        "default": {
            "()": "uvicorn.logging.DefaultFormatter",
            "fmt": LOG_FORMAT,
            "datefmt": "%Y-%m-%d %H:%M:%S",
        },
    }
    handlers: Dict = {
        "default": {
            "formatter": "default",
            "class": "logging.StreamHandler",
            "stream": "ext://sys.stderr",
        },
    }

    def _as_dict_config(self, logger_name: str, log_level: str) -> dict:
        """Build a dictConfig-compatible dict for the given name and level."""
        return {
            "version": self.version,
            "disable_existing_loggers": self.disable_existing_loggers,
            "formatters": self.formatters,
            "handlers": self.handlers,
            "loggers": {
                logger_name: {
                    "handlers": list(self.handlers.keys()),
                    "level": log_level,
                    "propagate": False,
                },
            },
        }

    @classmethod
    def get_logger(cls, logger_name: Optional[str] = None, log_level: Optional[str] = None,) -> logging.Logger:
        """
        :param logger_name: Logger name, with class name use: 'type(self).__name__'
        :type logger_name:
        :param log_level: Logger log level default 'DEBUG'
        :type log_level:
        :return: Logger instance
        :rtype: logging.Logger
        """
        config = cls()
        name = logger_name or config.LOGGER_NAME
        level = log_level or config.LOG_LEVEL

        dictConfig(config._as_dict_config(name, level))
        return logging.getLogger(name)
