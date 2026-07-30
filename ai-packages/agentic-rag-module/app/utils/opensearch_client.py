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

from opensearchpy import OpenSearch

HOSTS_SEPARATOR = ","


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


def get_opensearch_client(hosts, **kwargs):
    """
    Build an OpenSearch client for one or more hosts.

    Centralizes the ``OpenSearch(hosts=[...])`` construction so that multi-node
    clusters are supported everywhere: ``opensearch-py`` round-robins over the
    connection pool and skips nodes that are unreachable.

    :param hosts: Comma-separated hosts string, or a list of hosts
    :type hosts: str | list | tuple | None
    :param kwargs: Extra keyword arguments forwarded to the OpenSearch client

    :returns: Configured OpenSearch client
    :rtype: opensearchpy.OpenSearch

    Example:
        .. code-block:: python

            client = get_opensearch_client("node-1:9200,node-2:9200")
    """
    return OpenSearch(hosts=parse_hosts(hosts), **kwargs)
