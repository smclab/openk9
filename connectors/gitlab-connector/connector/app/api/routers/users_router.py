import logging
import threading
import json


from fastapi import APIRouter
import requests
from starlette import status

from ..models.models import (
    UsersGitlabExtractionRequest, 
    HealthCheck, 
    HealthStatus
)


logger = logger = logging.getLogger("gitlab_logger")

users_router = APIRouter(
        prefix="/users", 
        tags=["users"],
        on_startup=[lambda: logger.info("Endpoint /users started")],
        on_shutdown=[lambda: logger.warning("Endpoint /users shutdown")],
    )


@users_router.post(
        path="/execute",
        summary="Starts Gitlab Users extraction",
    )
def execute(payload: UsersGitlabExtractionRequest):
    extractor = payload.create_extractor()
    
    thread = threading.Thread(target=extractor.extract)
    thread.start()
    
    return "extraction started"


@users_router.get(
        path="/form",
        tags=["form"],
        summary="Get form structure of Gitlab Users request",
        response_description="Return json form structure", 
    )
def form():
    f = open('data/users/form.json')

    # returns JSON object as
    # a dictionary
    data = json.load(f)

    f.close()

    return data


@users_router.get(
        path="/sample",
        tags=["sample"],
        summary="Get sample structure of Gitlab Users request",
        response_description="Return json form structure", 
    )
def sample():
    f = open('data/users/sample.json')

    # returns JSON object as
    # a dictionary
    data = json.load(f)

    f.close()

    return data


@users_router.get(
        path="/health",
        tags=["healthcheck"],
        summary="Perform a Health Check",
        response_description="Return HTTP Status Code 200 (OK)",
        status_code=status.HTTP_200_OK,
        response_model=HealthCheck,
    )
def health():
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
            return HealthCheck(status=HealthStatus.UP)
        else:
            return HealthCheck(status=HealthStatus.DOWN)
    except requests.RequestException as e:
        logger.error(str(e) + " during request for health check")
        return HealthCheck(status=HealthStatus.UNKNOWN)