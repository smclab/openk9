package io.openk9.datasource.index.mappings;

import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class MappingsUtilTest {

	@Test
	void docTypesToMappings() {
		Map<MappingsKey, Object> result =
			MappingsUtil.docTypesToMappings(List.of(docType));
		Assertions.assertEquals(expectedJson, JsonObject.mapFrom(result));
	}

	private static final Object expectedJson = Json.decodeValue("""
	{
	  "properties": {
		"complexNumber": {
		  "properties": {
			"realPart": {
			  "type": "integer"
			},
			"imaginaryPart": {
			  "type": "integer"
			}
		  }
		},
		"title": {
		  "type": "text",
		  "fields": {
			"keyword": {
			  "type": "keyword"
			},
			"trigram": {
			  "type": "text",
			  "analyzer": "trigram"
			}
		  }
		},
		"address": {
		  "properties": {
			"street": {
			  "type": "text",
			  "fields": {
				"keyword": {
				  "type": "keyword"
				},
				"search_as_you_type": {
				  "type": "search_as_you_type"
				}
			  }
			},
			"number": {
			  "type": "integer"
			}
		  }
		},
		"description": {
		  "properties": {
			"base": {
			  "type": "text",
			  "fields": {
				"keyword": {
				  "type": "keyword",
				  "ignore_above": 256
				}
			  }
			},
			"i18n": {
			  "properties": {
				"en_US": {
				  "type": "text",
				  "fields": {
					"keyword": {
					  "type": "keyword",
					  "ignore_above": 256
					}
				  }
				},
				"de_DE": {
				  "type": "text",
				  "fields": {
					"keyword": {
					  "type": "keyword",
					  "ignore_above": 256
					}
				  }
				}
			  }
			}
		  }
		}
	  }
	}
	""");

	private static final DocType docType;
	private static final DocTypeField
		realPart,
		imaginaryPart,
		title,
		titleKeyword,
		titleTrigram,
		address,
		street,
		streetKeyword,
		streetSearchAsYouType,
		number,
		description,
		descriptionBase,
		descriptionBaseKeyword,
		descriptionEn,
		descriptionEnKeyword,
		descriptionDe,
		descriptionDeKeyword;

	static {
		docType = new DocType();
		docType.setName("aDocType");

		realPart = new DocTypeField();
		realPart.setDocType(docType);
		realPart.setFieldName("complexNumber.realPart");
		realPart.setFieldType(FieldType.INTEGER);

		imaginaryPart = new DocTypeField();
		imaginaryPart.setDocType(docType);
		imaginaryPart.setFieldName("complexNumber.imaginaryPart");
		imaginaryPart.setFieldType(FieldType.INTEGER);

		title = new DocTypeField();
		title.setDocType(docType);
		title.setFieldName("title");
		title.setFieldType(FieldType.TEXT);

		titleKeyword = new DocTypeField();
		titleKeyword.setDocType(docType);
		titleKeyword.setFieldName("title.keyword");
		titleKeyword.setFieldType(FieldType.KEYWORD);
		titleKeyword.setParentDocTypeField(title);

		titleTrigram = new DocTypeField();
		Analyzer trigram = new Analyzer();
		trigram.setName("trigram");
		trigram.setType("custom");
		titleTrigram.setDocType(docType);
		titleTrigram.setFieldName("title.trigram");
		titleTrigram.setFieldType(FieldType.TEXT);
		titleTrigram.setAnalyzer(trigram);
		titleTrigram.setParentDocTypeField(title);

		title.setSubDocTypeFields(new LinkedHashSet<>(List.of(titleKeyword, titleTrigram)));

		address = new DocTypeField();
		address.setDocType(docType);
		address.setFieldName("address");
		address.setFieldType(FieldType.OBJECT);

		street = new DocTypeField();
		street.setDocType(docType);
		street.setFieldName("address.street");
		street.setFieldType(FieldType.TEXT);
		street.setParentDocTypeField(address);

		streetKeyword = new DocTypeField();
		streetKeyword.setDocType(docType);
		streetKeyword.setFieldName("address.street.keyword");
		streetKeyword.setFieldType(FieldType.KEYWORD);
		streetKeyword.setParentDocTypeField(street);

		streetSearchAsYouType = new DocTypeField();
		streetSearchAsYouType.setDocType(docType);
		streetSearchAsYouType.setFieldName("address.street.search_as_you_type");
		streetSearchAsYouType.setFieldType(FieldType.SEARCH_AS_YOU_TYPE);
		streetSearchAsYouType.setParentDocTypeField(street);

		street.setSubDocTypeFields(
			new LinkedHashSet<>(List.of(streetKeyword, streetSearchAsYouType)));

		number = new DocTypeField();
		number.setDocType(docType);
		number.setFieldName("address.number");
		number.setFieldType(FieldType.INTEGER);
		number.setParentDocTypeField(address);

		address.setSubDocTypeFields(new LinkedHashSet<>(List.of(street, number)));

		description = new DocTypeField();
		description.setDocType(docType);
		description.setFieldName("description");
		description.setFieldType(FieldType.I18N);

		descriptionBase = new DocTypeField();
		descriptionBase.setDocType(docType);
		descriptionBase.setFieldName("description.base");
		descriptionBase.setFieldType(FieldType.TEXT);
		descriptionBase.setParentDocTypeField(description);

		descriptionBaseKeyword = new DocTypeField();
		descriptionBaseKeyword.setDocType(docType);
		descriptionBaseKeyword.setFieldName("description.base.keyword");
		descriptionBaseKeyword.setFieldType(FieldType.KEYWORD);
		descriptionBaseKeyword.setParentDocTypeField(descriptionBase);
		descriptionBaseKeyword.setJsonConfig("{\"ignore_above\":256}");
		descriptionBase.setSubDocTypeFields(Set.of(descriptionBaseKeyword));

		descriptionEn = new DocTypeField();
		descriptionEn.setDocType(docType);
		descriptionEn.setFieldName("description.i18n.en_US");
		descriptionEn.setFieldType(FieldType.TEXT);
		descriptionEn.setParentDocTypeField(description);

		descriptionEnKeyword = new DocTypeField();
		descriptionEnKeyword.setDocType(docType);
		descriptionEnKeyword.setFieldName("description.i18n.en_US.keyword");
		descriptionEnKeyword.setFieldType(FieldType.KEYWORD);
		descriptionEnKeyword.setParentDocTypeField(descriptionEn);
		descriptionEnKeyword.setJsonConfig("{\"ignore_above\":256}");

		descriptionEn.setSubDocTypeFields(Set.of(descriptionEnKeyword));

		descriptionDe = new DocTypeField();
		descriptionDe.setDocType(docType);
		descriptionDe.setFieldName("description.i18n.de_DE");
		descriptionDe.setFieldType(FieldType.TEXT);
		descriptionDe.setParentDocTypeField(description);

		descriptionDeKeyword = new DocTypeField();
		descriptionDeKeyword.setDocType(docType);
		descriptionDeKeyword.setFieldName("description.i18n.de_DE.keyword");
		descriptionDeKeyword.setFieldType(FieldType.KEYWORD);
		descriptionDeKeyword.setParentDocTypeField(descriptionDe);
		descriptionDeKeyword.setJsonConfig("{\"ignore_above\":256}");

		descriptionDe.setSubDocTypeFields(Set.of(descriptionDeKeyword));

		description.setSubDocTypeFields(new LinkedHashSet<>(List.of(
			descriptionBase, descriptionEn, descriptionDe)));

		docType.setDocTypeFields(new LinkedHashSet<>(List.of(
			realPart,
			imaginaryPart,
			title,
			titleKeyword,
			titleTrigram,
			address,
			street,
			streetKeyword,
			streetSearchAsYouType,
			number,
			description,
			descriptionBase,
			descriptionBaseKeyword,
			descriptionEn,
			descriptionEnKeyword,
			descriptionDe,
			descriptionDeKeyword)));
	}

}