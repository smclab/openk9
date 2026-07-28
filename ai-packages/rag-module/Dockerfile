ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim

FROM python:${PYTHON_BASE_DOCKER_IMAGE}

WORKDIR /rag-module/

COPY ./requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY ./app ./app

RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. ./app/external_services/grpc/searcher/searcher.proto && \
python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. ./app/external_services/grpc/tenant_manager/tenant_manager.proto

# perl-base is Debian-essential but unused by this service: nothing here invokes perl,
# and its only consumers in the image are package-management helpers (debconf,
# deb-systemd-helper, c_rehash) that never run at runtime. Debian ships no fixed
# perl-base for trixie (CVE-2026-13221, CVE-2026-42496, CVE-2026-57433, CVE-2026-8376
# have no FixedVersion; CVE-2026-42496 is fix_deferred), so removal is the only real
# remediation. Verified: python, pip, apt-get install and update-ca-certificates all
# keep working afterwards. Keep this as the last layer, so no later apt step can
# depend on perl.
RUN dpkg --purge --force-remove-essential --force-depends perl-base

EXPOSE 5000

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]
