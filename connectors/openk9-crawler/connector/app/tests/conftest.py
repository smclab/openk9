import os
import sys

# make the connector app importable and resolve the relative "data/..." paths
# used by the form endpoints from the app directory
APP_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, APP_DIR)
os.chdir(APP_DIR)
