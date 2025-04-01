package io.openk9.datasource.service;

import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.RAGConfigurationDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class RAGConfigurationServiceTest {


	private static final String ENTITY_NAME_PREFIX = "RAGConfigurationServiceTest - ";
	private static final int CHUNK_WINDOW = 1500;
	private static final String DEFAULT_PROMPT_EMPTY_STRING = "";
	private static final Boolean DEFAULT_REFORMULATE = false;
	private static final int DEFAULT_VALUE_CHUNK_WINDOW = 0;
	private static final String PROMPT_EXAMPLE = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris sit" +
		" amet diam a lorem aliquam pellentesque. Morbi dapibus porttitor quam, id porta elit ultrices vel." +
		" Donec eget ex rutrum, rutrum lectus eget, molestie libero. Nunc at commodo odio. Proin tempus ipsum ac" +
		" lectus mattis, vitae porttitor turpis interdum. Etiam vitae mi sit amet diam efficitur dapibus. Nulla" +
		" egestas, tellus maximus fringilla tincidunt, sapien urna dictum quam, quis consectetur metus urna vel mi." +
		" Proin eleifend, mi pulvinar semper dapibus, massa mi vestibulum est, sit amet finibus odio augue a elit.\n" +
		"\n" +
		"Donec in hendrerit metus, interdum egestas neque. Praesent eget eros sit amet ipsum congue sollicitudin." +
		" Curabitur sit amet tincidunt enim. Phasellus consequat vulputate hendrerit. Morbi in aliquam diam. Morbi" +
		" sem dui, fringilla blandit consectetur ut, imperdiet nec orci. Nulla non quam et velit lacinia maximus.\n" +
		"\n" +
		"Fusce posuere egestas dapibus. Orci varius natoque penatibus et magnis dis parturient montes, nascetur" +
		" ridiculus mus. Nullam sit amet venenatis massa, eget luctus velit. Pellentesque quis blandit sem, ut" +
		" hendrerit purus. Duis nunc purus, accumsan non aliquet non, viverra a odio. Vivamus sed nunc ullamcorper," +
		" volutpat lacus eget, accumsan odio. Integer tincidunt lectus non justo scelerisque hendrerit. Maecenas" +
		" pellentesque gravida magna sed fringilla. Donec fringilla quam eget massa elementum, at pulvinar velit" +
		" facilisis. Donec finibus ipsum sed justo faucibus, sollicitudin vestibulum diam suscipit. Etiam non sem" +
		" vel mi imperdiet ultricies vel sed metus. Quisque luctus massa magna, a mollis eros pretium gravida." +
		" Nam quis libero metus. Cras tellus turpis, imperdiet at consectetur vel, lobortis vitae enim.\n" +
		"\n" +
		"Nullam vitae eros ac eros sagittis fermentum ut facilisis augue. Nunc euismod ultricies tellus. Aliquam" +
		" erat volutpat. Nunc rhoncus ligula arcu, sed mollis mauris scelerisque lacinia. Pellentesque habitant" +
		" morbi tristique senectus et netus et malesuada fames ac turpis egestas. Cras pretium nibh sed dapibus" +
		" pellentesque. Nulla at sem sem. Aenean scelerisque suscipit mauris, eu porta magna luctus a. Vestibulum" +
		" nisl turpis, congue vel libero faucibus, ultricies aliquet elit. Donec at diam nec odio egestas euismod." +
		" Sed eu vulputate orci. Aenean mauris turpis, maximus vel dapibus a, luctus vitae sapien. Aliquam erat volutpat.\n" +
		"\n" +
		"Sed tellus ex, dignissim in eros in, iaculis commodo mauris. Vestibulum ornare leo vel sapien maximus, " +
		"a auctor leo hendrerit. Aenean sapien urna, vestibulum ac ex iaculis, venenatis accumsan elit. Nam luctus" +
		" faucibus nibh et fermentum. Pellentesque ipsum tortor, volutpat eu porta nec, imperdiet in elit. Proin" +
		" pellentesque neque tincidunt enim bibendum bibendum. Vestibulum ante ipsum primis in faucibus orci luctus " +
		"et ultrices posuere cubilia curae; Morbi consequat et leo sed faucibus.";
	private static final String RAG_CONFIGURATION_ONE_NAME = ENTITY_NAME_PREFIX + "RAG Configuration 1 ";
	private static final Logger log = Logger.getLogger(RAGConfigurationServiceTest.class);

	@Inject
	RAGConfigurationService ragConfigurationService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	void should_create_rag_configuration_one() {
		var dto = RAGConfigurationDTO.builder()
			.name(RAG_CONFIGURATION_ONE_NAME)
			.type(RAGType.CHAT)
			.prompt(PROMPT_EXAMPLE)
			.rephrasePrompt(PROMPT_EXAMPLE)
			.promptNoRag(PROMPT_EXAMPLE)
			.ragToolDescription(PROMPT_EXAMPLE)
			.chunkWindow(CHUNK_WINDOW)
			.reformulate(true)
			.build();

		createRAGConfiguration(dto);

		var ragConfigurationOne = getRAGConfigurationOne();

		assertEquals(RAGType.CHAT, ragConfigurationOne.getType());
		assertEquals(PROMPT_EXAMPLE, ragConfigurationOne.getPrompt());
		assertEquals(PROMPT_EXAMPLE, ragConfigurationOne.getRephrasePrompt());
		assertEquals(PROMPT_EXAMPLE, ragConfigurationOne.getPromptNoRag());
		assertEquals(PROMPT_EXAMPLE, ragConfigurationOne.getRagToolDescription());
		assertEquals(CHUNK_WINDOW, ragConfigurationOne.getChunkWindow());
		assertTrue(ragConfigurationOne.getReformulate());

		removeRAGConfigurationOne();
	}

	@Test
	void should_create_rag_configuration_one_with_default_fields() {
		var dto = RAGConfigurationDTO.builder()
			.name(RAG_CONFIGURATION_ONE_NAME)
			.type(RAGType.CHAT)
			.build();

		createRAGConfiguration(dto);

		var ragConfigurationOne = getRAGConfigurationOne();

		assertNotNull(ragConfigurationOne);

		log.info(String.format("Rag configuration created: %s", ragConfigurationOne));

		assertEquals(RAGType.CHAT, ragConfigurationOne.getType());
		assertEquals(DEFAULT_PROMPT_EMPTY_STRING, ragConfigurationOne.getPrompt());
		assertEquals(DEFAULT_PROMPT_EMPTY_STRING, ragConfigurationOne.getRephrasePrompt());
		assertEquals(DEFAULT_PROMPT_EMPTY_STRING, ragConfigurationOne.getPromptNoRag());
		assertEquals(DEFAULT_PROMPT_EMPTY_STRING, ragConfigurationOne.getRagToolDescription());
		assertEquals(DEFAULT_VALUE_CHUNK_WINDOW, ragConfigurationOne.getChunkWindow());
		assertEquals(DEFAULT_REFORMULATE, ragConfigurationOne.getReformulate());

		removeRAGConfigurationOne();
	}

	private RAGConfiguration createRAGConfiguration(RAGConfigurationDTO dto) {
		return sessionFactory.withTransaction(
			session -> ragConfigurationService.create(session, dto)
		)
		.await()
		.indefinitely();
	}

	private RAGConfiguration getRAGConfigurationOne() {
		return sessionFactory.withTransaction(
			session -> ragConfigurationService.findByName(session, RAG_CONFIGURATION_ONE_NAME)
		)
		.await()
		.indefinitely();
	}

	private void removeRAGConfigurationOne() {
		var ragConfiguration = getRAGConfigurationOne();

		sessionFactory.withTransaction(
			session ->
				ragConfigurationService.deleteById(session, ragConfiguration.getId())
		)
		.await()
		.indefinitely();
	}
}
