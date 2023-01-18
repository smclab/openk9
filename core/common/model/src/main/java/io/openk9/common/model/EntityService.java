package io.openk9.common.model;

import io.smallrye.mutiny.Uni;

public interface EntityService<ENTITY, DTO> {

	Uni<ENTITY> patch(long id, DTO dto);

	Uni<ENTITY> update(long id, DTO dto);

	Uni<ENTITY> create(DTO dto);

}
