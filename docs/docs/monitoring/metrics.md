---
id: metrics
title: Metrics
---

This section describes tools for monitoring Openk9 solution.

## Prometheus

Every Openk9 service exposes metrics. You can install [Prometheus](https://prometheus.io/) in your environment and collect metrics.

Then you can view metrics using available dashoboards on [Grafana](https://grafana.com/)

## Grafana

If you collect metrics using Prometheus, you can use Grafana to configure and enable [Alert Manager](https://grafana.com/docs/grafana/latest/alerting/fundamentals/alertmanager/) to monitor
your system and receive reports when system has failures.