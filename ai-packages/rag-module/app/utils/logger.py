import logging
import os

from dotenv import load_dotenv

load_dotenv()

LOGGING_LEVEL = os.getenv("LOGGING_LEVEL", "INFO")

logging.basicConfig(
    level=LOGGING_LEVEL, format="%(asctime)s - %(levelname)s - %(message)s"
)

logger = logging.getLogger(__name__)
