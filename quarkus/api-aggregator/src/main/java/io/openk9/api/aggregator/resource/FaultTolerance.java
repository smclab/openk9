package io.openk9.api.aggregator.resource;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.opentracing.Traced;

@CircuitBreaker
@Traced
public class FaultTolerance {
}
