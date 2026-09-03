#
# Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

import os

from opensearchpy import OpenSearch

HOSTS_SEPARATOR = ","
TLS_SCHEME = "https://"


def parse_hosts(hosts):
    """
    Normalize an OpenSearch hosts configuration into a list of hosts.

    Accepts either the comma-separated string coming from the OPENSEARCH_HOST
    environment variable (single host or multi-node cluster) or an already
    parsed list, so that call sites can pass through whatever they received
    without knowing which form it is in.

    :param hosts: Comma-separated hosts string, or a list of hosts
    :type hosts: str | list | tuple | None

    :returns: List of non-empty, stripped hosts
    :rtype: list

    Example:
        .. code-block:: python

            parse_hosts("node-1:9200, node-2:9200")
            # ["node-1:9200", "node-2:9200"]
    """
    if hosts is None:
        return []

    if isinstance(hosts, (list, tuple)):
        candidates = hosts
    else:
        candidates = str(hosts).split(HOSTS_SEPARATOR)

    return [host.strip() for host in candidates if host and str(host).strip()]


def _connection_options(hosts):
    """
    Derive the TLS and authentication options from the environment.

    The environment is read at every call rather than at import time: clients
    are already built per request, and this keeps the factory testable.

    The scheme is left to ``opensearch-py``, which resolves it per host: an
    entry without ``://`` is plain HTTP, while an ``https://`` entry enables TLS
    for that host alone. Passing ``use_ssl`` globally would force TLS on the
    plain nodes of a mixed list, so only the options the library cannot infer
    are returned, and only when at least one host speaks TLS.

    :param hosts: Parsed list of hosts
    :type hosts: list

    :returns: Keyword arguments for the OpenSearch client
    :rtype: dict

    Example:
        .. code-block:: python

            _connection_options(["https://node-1:9200"])
            # {"verify_certs": True}
    """
    options = {}

    if any(host.lower().startswith(TLS_SCHEME) for host in hosts):
        verify_certs = os.getenv("OPENSEARCH_VERIFY_CERTS", "true").lower() != "false"
        options["verify_certs"] = verify_certs

        ca_certs = os.getenv("OPENSEARCH_CA_CERTS")
        if ca_certs:
            options["ca_certs"] = ca_certs

        if not verify_certs:
            options["ssl_show_warn"] = False

    username = os.getenv("OPENSEARCH_USERNAME")
    password = os.getenv("OPENSEARCH_PASSWORD")

    if username and password:
        options["http_auth"] = (username, password)

    return options


def get_opensearch_client(hosts, **kwargs):
    """
    Build an OpenSearch client for one or more hosts.

    Centralizes the ``OpenSearch(hosts=[...])`` construction so that multi-node
    clusters are supported everywhere: ``opensearch-py`` round-robins over the
    connection pool and skips nodes that are unreachable.

    TLS and basic authentication are configured from the environment, through
    ``OPENSEARCH_VERIFY_CERTS``, ``OPENSEARCH_CA_CERTS``, ``OPENSEARCH_USERNAME``
    and ``OPENSEARCH_PASSWORD``. Explicit keyword arguments take precedence.

    :param hosts: Comma-separated hosts string, or a list of hosts
    :type hosts: str | list | tuple | None
    :param kwargs: Extra keyword arguments forwarded to the OpenSearch client

    :returns: Configured OpenSearch client
    :rtype: opensearchpy.OpenSearch

    Example:
        .. code-block:: python

            client = get_opensearch_client("node-1:9200,node-2:9200")
    """
    parsed_hosts = parse_hosts(hosts)
    options = _connection_options(parsed_hosts)
    options.update(kwargs)

    return OpenSearch(hosts=parsed_hosts, **options)
