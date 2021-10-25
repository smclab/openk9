package io.openk9.datasource.fix;

import io.openk9.datasource.emitter.datasource.DeleteEmitter;
import io.openk9.datasource.emitter.datasource.InsertEmitter;
import io.openk9.datasource.emitter.datasource.UpdateEmitter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class JPAEntityListenerInjectionWorkaround {
  @Inject
  DeleteEmitter deleteEmitter;
  @Inject
  InsertEmitter insertEmitter;
  @Inject
  UpdateEmitter updateEmitter;
}