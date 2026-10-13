import os
import re

import pytest

DOCKERFILE = os.path.join(
    os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))),
    "Dockerfile",
)


def runtime_stage():
    with open(DOCKERFILE, encoding="utf-8") as f:
        content = f.read()
    return re.split(r"^FROM .*$", content, flags=re.MULTILINE)[-1]


@pytest.mark.parametrize("env_var, builder_path", [
    ("PLAYWRIGHT_BROWSERS_PATH", "/ms-playwright"),
    ("TLDEXTRACT_CACHE", "/tld_cache"),
])
def test_runtime_env_points_to_the_copied_files(env_var, builder_path):
    stage = runtime_stage()
    env = re.search(rf"^ENV {env_var}=(\S+)$", stage, flags=re.MULTILINE).group(1)
    copied_to = re.search(
        rf"^COPY --from=builder {re.escape(builder_path)} (\S+)$", stage, flags=re.MULTILINE
    ).group(1)
    assert env == copied_to


def test_tini_is_pid_1():
    stage = runtime_stage()
    assert re.search(r"^ENTRYPOINT \[\"/usr/bin/tini\", \"--\"\]$", stage, flags=re.MULTILINE)
    assert re.search(r"^CMD \[\"/etc/docker-entrypoint.sh\"\]$", stage, flags=re.MULTILINE)
    assert re.search(r"apt-get install\b[^&]*\btini\b", stage)


def test_runtime_runs_as_non_root():
    stage = runtime_stage()
    assert re.findall(r"^USER (\S+)$", stage, flags=re.MULTILINE)[-1] == "1001"
