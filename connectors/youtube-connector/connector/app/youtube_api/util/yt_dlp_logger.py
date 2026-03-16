import logging


# TODO: Check if can have better logs
class YtDlpLogger:
    """Wrapper mapping yt-dlp to Python logging"""
    def __init__(self, url_ref: str, logger_name: str = "youtube_logger", verbose: bool = False):
        self.logger = logging.getLogger(logger_name)
        self.url_ref = url_ref
        self.verbose = verbose

    def debug(self, msg):
        if msg.startswith('[debug] ') and not self.verbose:
            pass  # Ignores internal debug messages
        else:
            self.info(msg)

    def info(self, msg):
        self.logger.info(f"[{self.url_ref}] {msg}")

    def warning(self, msg):
        self.logger.warning(f"[{self.url_ref}] {msg}")

    def error(self, msg):
        self.logger.error(f"[{self.url_ref}] {msg}")
