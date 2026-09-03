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


from unittest.mock import patch

import pytest

from app.utils.opensearch_client import get_opensearch_client, parse_hosts

OPENSEARCH_ENVIRONMENT_VARIABLES = (
    "OPENSEARCH_VERIFY_CERTS",
    "OPENSEARCH_CA_CERTS",
    "OPENSEARCH_USERNAME",
    "OPENSEARCH_PASSWORD",
)


@pytest.fixture(autouse=True)
def clean_opensearch_environment(monkeypatch):
    """The factory reads these at every call and app.server loads the developer's
    .env at import time, so clear them to keep the assertions machine independent."""
    for variable in OPENSEARCH_ENVIRONMENT_VARIABLES:
        monkeypatch.delenv(variable, raising=False)


def test_parse_hosts_single_host():
    assert parse_hosts("opensearch-cluster-master-headless:9200") == [
        "opensearch-cluster-master-headless:9200"
    ]


def test_parse_hosts_multiple_hosts():
    assert parse_hosts("node-1:9200,node-2:9200,node-3:9200") == [
        "node-1:9200",
        "node-2:9200",
        "node-3:9200",
    ]


def test_parse_hosts_strips_whitespace():
    assert parse_hosts(" node-1:9200 ,\tnode-2:9200 ") == [
        "node-1:9200",
        "node-2:9200",
    ]


def test_parse_hosts_drops_empty_entries():
    assert parse_hosts("node-1:9200,,node-2:9200,") == [
        "node-1:9200",
        "node-2:9200",
    ]


def test_parse_hosts_empty_or_missing_value():
    assert parse_hosts("") == []
    assert parse_hosts(None) == []
    assert parse_hosts("  ") == []
    assert parse_hosts(",") == []


def test_parse_hosts_is_idempotent_on_lists():
    hosts = ["node-1:9200", "node-2:9200"]

    assert parse_hosts(hosts) == hosts
    assert parse_hosts(parse_hosts(hosts)) == hosts


def test_parse_hosts_keeps_url_form():
    """URLs contain no comma, so scheme and port must survive parsing."""
    assert parse_hosts("http://node-1:9200,https://node-2:9200") == [
        "http://node-1:9200",
        "https://node-2:9200",
    ]


@patch("app.utils.opensearch_client.OpenSearch")
def test_get_opensearch_client_single_host(mock_opensearch):
    get_opensearch_client("node-1:9200")

    mock_opensearch.assert_called_once_with(hosts=["node-1:9200"])


@patch("app.utils.opensearch_client.OpenSearch")
def test_get_opensearch_client_multi_host(mock_opensearch):
    get_opensearch_client("node-1:9200, node-2:9200")

    mock_opensearch.assert_called_once_with(hosts=["node-1:9200", "node-2:9200"])


@patch("app.utils.opensearch_client.OpenSearch")
def test_get_opensearch_client_forwards_extra_kwargs(mock_opensearch):
    get_opensearch_client("node-1:9200", http_compress=True)

    mock_opensearch.assert_called_once_with(
        hosts=["node-1:9200"], http_compress=True
    )


@patch("app.utils.opensearch_client.OpenSearch")
def test_get_opensearch_client_https_verifies_certificates(mock_opensearch):
    get_opensearch_client("https://node-1:9200")

    mock_opensearch.assert_called_once_with(
        hosts=["https://node-1:9200"], verify_certs=True
    )


@patch("app.utils.opensearch_client.OpenSearch")
def test_get_opensearch_client_https_without_certificate_verification(
    mock_opensearch, monkeypatch
):
    monkeypatch.setenv("OPENSEARCH_VERIFY_CERTS", "false")

    get_opensearch_client("https://node-1:9200")

    mock_opensearch.assert_called_once_with(
        hosts=["https://node-1:9200"], verify_certs=False, ssl_show_warn=False
    )


@patch("app.utils.opensearch_client.OpenSearch")
def test_get_opensearch_client_https_with_ca_bundle(mock_opensearch, monkeypatch):
    monkeypatch.setenv("OPENSEARCH_CA_CERTS", "/etc/opensearch-certs/ca.pem")

    get_opensearch_client("https://node-1:9200")

    mock_opensearch.assert_called_once_with(
        hosts=["https://node-1:9200"],
        verify_certs=True,
        ca_certs="/etc/opensearch-certs/ca.pem",
    )


@patch("app.utils.opensearch_client.OpenSearch")
def test_get_opensearch_client_http_ignores_tls_settings(mock_opensearch, monkeypatch):
    """Without an https host the TLS options are left out of the call entirely."""
    monkeypatch.setenv("OPENSEARCH_VERIFY_CERTS", "false")
    monkeypatch.setenv("OPENSEARCH_CA_CERTS", "/etc/opensearch-certs/ca.pem")

    get_opensearch_client("node-1:9200")

    mock_opensearch.assert_called_once_with(hosts=["node-1:9200"])


@patch("app.utils.opensearch_client.OpenSearch")
def test_get_opensearch_client_basic_auth(mock_opensearch, monkeypatch):
    monkeypatch.setenv("OPENSEARCH_USERNAME", "opensearch")
    monkeypatch.setenv("OPENSEARCH_PASSWORD", "s3cret")

    get_opensearch_client("node-1:9200")

    mock_opensearch.assert_called_once_with(
        hosts=["node-1:9200"], http_auth=("opensearch", "s3cret")
    )


@patch("app.utils.opensearch_client.OpenSearch")
def test_get_opensearch_client_without_password_skips_auth(
    mock_opensearch, monkeypatch
):
    monkeypatch.setenv("OPENSEARCH_USERNAME", "opensearch")

    get_opensearch_client("node-1:9200")

    mock_opensearch.assert_called_once_with(hosts=["node-1:9200"])


@patch("app.utils.opensearch_client.OpenSearch")
def test_get_opensearch_client_mixed_schemes_apply_tls_once(mock_opensearch):
    """A single https node is enough to configure TLS, and the plain node keeps
    its scheme so opensearch-py does not upgrade it."""
    get_opensearch_client("node-1:9200,https://node-2:9200")

    mock_opensearch.assert_called_once_with(
        hosts=["node-1:9200", "https://node-2:9200"], verify_certs=True
    )


@patch("app.utils.opensearch_client.OpenSearch")
def test_get_opensearch_client_multi_host_shares_credentials(
    mock_opensearch, monkeypatch
):
    monkeypatch.setenv("OPENSEARCH_USERNAME", "opensearch")
    monkeypatch.setenv("OPENSEARCH_PASSWORD", "s3cret")

    get_opensearch_client("https://node-1:9200,https://node-2:9200")

    mock_opensearch.assert_called_once_with(
        hosts=["https://node-1:9200", "https://node-2:9200"],
        verify_certs=True,
        http_auth=("opensearch", "s3cret"),
    )


@patch("app.utils.opensearch_client.OpenSearch")
def test_get_opensearch_client_explicit_kwargs_win_over_environment(
    mock_opensearch, monkeypatch
):
    monkeypatch.setenv("OPENSEARCH_USERNAME", "opensearch")
    monkeypatch.setenv("OPENSEARCH_PASSWORD", "s3cret")

    get_opensearch_client("node-1:9200", http_auth=("admin", "admin"))

    mock_opensearch.assert_called_once_with(
        hosts=["node-1:9200"], http_auth=("admin", "admin")
    )
