package io.openk9.entity.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Payload implements Serializable {
	private EntityManagerRequest payload;
	private List<EntityRequest> entities;
	private String replyTo;
}
