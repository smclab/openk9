---
id: logging
title: Logging
---

This section describes tools for monitoring Openk9 solution.

## Metrics

Every Openk9 service exposes metrics. You can install [Prometheus](https://prometheus.io/) in your environment and collect metrics.

Then you can view metrics using available dashoboards on [Grafana](https://grafana.com/)

## Alerting

If you collect metrics using Prometheus, you can use Grafana to configure and enable [Alert Manager](https://grafana.com/docs/grafana/latest/alerting/fundamentals/alertmanager/) to monitor
your system and receive reports when system has failures.

## Logging

To collect and explore logs you can use [Grafana Loki](https://grafana.com/oss/loki/) solution.