package io.openk9.datasource.processor.indexwriter;

import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexerEventsTest {

	@Test
	void shouldMapToDocTypeFields() {
		List<DocTypeField> docTypeFields = IndexerEvents.toDocTypeFields(mappings.getMap());
		Optional<DocTypeField> optField = docTypeFields
			.stream()
			.filter(f -> f.getFieldName().equals("acl"))
			.flatMap(f -> f.getSubDocTypeFields().stream())
			.filter(f -> f.getFieldName().equals("public"))
			.findFirst();
		assertTrue(optField.isPresent());
		DocTypeField field = optField.get();
		assertSame(field.getFieldType(), FieldType.BOOLEAN);
		assertEquals("public", field.getFieldName());
		assertEquals(DocTypeField.fieldPath(field), "acl.public");


		Map<String, List<DocTypeField>> docTypeAndFieldsGroup =
			IndexerEvents.toDocTypeAndFieldsGroup(
				Tuple2.of(docTypeFields, List.of("web", "resources", "document"))
			);

		assertTrue(
			docTypeAndFieldsGroup
				.get("default")
				.stream()
				.filter(f -> f.getFieldName().equals("acl"))
				.flatMap(f -> f.getSubDocTypeFields().stream())
				.anyMatch(f -> f.getFieldName().equals("public"))
		);

		assertTrue(
			docTypeAndFieldsGroup
				.get("web")
				.stream()
				.anyMatch(f -> f.getFieldName().equals("title"))
		);

		Set<DocType> docTypes =
			IndexerEvents.mergeDocTypes(docTypeAndFieldsGroup, List.of(docType));

		docTypes.forEach(docType1 -> docType1
			.getDocTypeFields()
			.forEach(docTypeField ->
				_printDocTypeField(docTypeField, "")
			)
		);

	}

	private static final JsonObject mappings = (JsonObject) Json.decodeValue("""
	{
	  "properties" : {
		"acl" : {
		  "properties" : {
			"public" : {
			  "type" : "boolean"
			}
		  }
		},
		"contentId" : {
		  "type" : "text",
		  "fields" : {
			"keyword" : {
			  "type" : "keyword",
			  "ignore_above" : 256
			}
		  }
		},
		"datasourceId" : {
		  "type" : "long"
		},
		"document" : {
		  "properties" : {
			"content" : {
			  "type" : "text",
			  "fields" : {
				"keyword" : {
				  "type" : "keyword",
				  "ignore_above" : 256
				}
			  }
			},
			"contentType" : {
			  "type" : "text",
			  "fields" : {
				"keyword" : {
				  "type" : "keyword",
				  "ignore_above" : 256
				}
			  }
			},
			"relativeUrl" : {
			  "type" : "text",
			  "fields" : {
				"keyword" : {
				  "type" : "keyword",
				  "ignore_above" : 256
				}
			  }
			},
			"summary" : {
			  "type" : "text",
			  "fields" : {
				"keyword" : {
				  "type" : "keyword",
				  "ignore_above" : 256
				}
			  }
			},
			"title" : {
			  "type" : "text",
			  "fields" : {
				"keyword" : {
				  "type" : "keyword",
				  "ignore_above" : 256
				}
			  }
			},
			"url" : {
			  "type" : "text",
			  "fields" : {
				"keyword" : {
				  "type" : "keyword",
				  "ignore_above" : 256
				}
			  }
			}
		  }
		},
		"documentTypes" : {
		  "type" : "text",
		  "fields" : {
			"keyword" : {
			  "type" : "keyword",
			  "ignore_above" : 256
			}
		  }
		},
		"file" : {
		  "properties" : {
			"path" : {
			  "type" : "text",
			  "fields" : {
				"keyword" : {
				  "type" : "keyword",
				  "ignore_above" : 256
				}
			  }
			}
		  }
		},
		"indexName" : {
		  "type" : "text",
		  "fields" : {
			"keyword" : {
			  "type" : "keyword",
			  "ignore_above" : 256
			}
		  }
		},
		"ingestionId" : {
		  "type" : "text",
		  "fields" : {
			"keyword" : {
			  "type" : "keyword",
			  "ignore_above" : 256
			}
		  }
		},
		"last" : {
		  "type" : "boolean"
		},
		"parsingDate" : {
		  "type" : "long"
		},
		"rawContent" : {
		  "type" : "text",
		  "fields" : {
			"keyword" : {
			  "type" : "keyword",
			  "ignore_above" : 256
			}
		  }
		},
		"resources" : {
		  "properties" : {
			"binaries" : {
			  "properties" : {
				"id" : {
				  "type" : "text",
				  "fields" : {
					"keyword" : {
					  "type" : "keyword",
					  "ignore_above" : 256
					}
				  }
				},
				"name" : {
				  "type" : "text",
				  "fields" : {
					"keyword" : {
					  "type" : "keyword",
					  "ignore_above" : 256
					}
				  }
				},
				"resourceId" : {
				  "type" : "text",
				  "fields" : {
					"keyword" : {
					  "type" : "keyword",
					  "ignore_above" : 256
					}
				  }
				}
			  }
			}
		  }
		},
		"scheduleId" : {
		  "type" : "text",
		  "fields" : {
			"keyword" : {
			  "type" : "keyword",
			  "ignore_above" : 256
			}
		  }
		},
		"tenantId" : {
		  "type" : "text",
		  "fields" : {
			"keyword" : {
			  "type" : "keyword",
			  "ignore_above" : 256
			}
		  }
		},
		"topic" : {
		  "properties" : {
			"topics" : {
			  "type" : "text",
			  "fields" : {
				"keyword" : {
				  "type" : "keyword",
				  "ignore_above" : 256
				}
			  }
			}
		  }
		},
		"web" : {
		  "properties" : {
			"content" : {
			  "properties" : {
				"base" : {
				  "type" : "text",
				  "fields" : {
					"keyword" : {
					  "type" : "keyword",
					  "ignore_above" : 256
					}
				  }
				},
				"i18n" : {
				  "properties" : {
					"de_DE" : {
					  "type" : "text",
					  "fields" : {
						"keyword" : {
						  "type" : "keyword",
						  "ignore_above" : 256
						}
					  }
					},
					"en_US" : {
					  "type" : "text",
					  "fields" : {
						"keyword" : {
						  "type" : "keyword",
						  "ignore_above" : 256
						}
					  }
					}
				  }
				}
			  }
			},
			"favicon" : {
			  "type" : "text",
			  "fields" : {
				"keyword" : {
				  "type" : "keyword",
				  "ignore_above" : 256
				}
			  }
			},
			"title" : {
			  "properties" : {
				"base" : {
				  "type" : "text",
				  "fields" : {
					"keyword" : {
					  "type" : "keyword",
					  "ignore_above" : 256
					}
				  }
				},
				"i18n" : {
				  "properties" : {
					"de_DE" : {
					  "type" : "text",
					  "fields" : {
						"keyword" : {
						  "type" : "keyword",
						  "ignore_above" : 256
						}
					  }
					},
					"en_US" : {
					  "type" : "text",
					  "fields" : {
						"keyword" : {
						  "type" : "keyword",
						  "ignore_above" : 256
						}
					  }
					}
				  }
				}
			  }
			},
			"url" : {
			  "type" : "text",
			  "fields" : {
				"keyword" : {
				  "type" : "keyword",
				  "ignore_above" : 256
				}
			  }
			}
		  }
		}
	  }
	}
	""");


	private static final DocType docType;
	private static final DocTypeField title, titleKeyword, titleTrigram;

	private static void _printDocTypeField(DocTypeField docTypeField, String depth) {
		System.out.println(
			depth
				+ " fieldName: " + docTypeField.getFieldName()
				+ " name: " + docTypeField.getName()
				+ " description: " + docTypeField.getDescription()
				+ " path: " + DocTypeField.fieldPath(docTypeField)
		);
		for (DocTypeField child : docTypeField.getSubDocTypeFields()) {
			_printDocTypeField(child, depth + "-");
		}
	}

	static {
		docType = new DocType();
		docType.setName("web");
		docType.setId(1L);

		title = new DocTypeField();
		title.setId(2L);
		title.setDocType(docType);
		title.setFieldName("title");
		title.setDescription("persisted");
		title.setFieldType(FieldType.TEXT);

		titleKeyword = new DocTypeField();
		title.setId(3L);
		titleKeyword.setDocType(docType);
		titleKeyword.setDescription("persisted");
		titleKeyword.setFieldName("keyword");
		titleKeyword.setFieldType(FieldType.KEYWORD);
		titleKeyword.setParentDocTypeField(title);

		titleTrigram = new DocTypeField();
		title.setId(4L);
		Analyzer trigram = new Analyzer();
		trigram.setId(5L);
		trigram.setName("trigram");
		trigram.setType("custom");
		titleTrigram.setDocType(docType);
		titleTrigram.setFieldName("trigram");
		titleTrigram.setFieldType(FieldType.TEXT);
		titleTrigram.setAnalyzer(trigram);
		titleTrigram.setParentDocTypeField(title);

		title.setSubDocTypeFields(new LinkedHashSet<>(List.of(titleKeyword, titleTrigram)));

		docType.setDocTypeFields(new LinkedHashSet<>(List.of(
			title,
			titleKeyword,
			titleTrigram)));
	}

}
