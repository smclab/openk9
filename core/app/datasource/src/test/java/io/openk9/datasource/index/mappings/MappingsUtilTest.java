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
			MappingsUtil.docTypesToMappings(List.of(defaultDocType, webDocType));
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
		"web": {
		  "properties": {
		  	"title": {
		  		"type": "text"
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
	  }
	}
	""");

	private static final DocType defaultDocType, webDocType;
	private static final DocTypeField
		complexNumber,
		realPart,
		imaginaryPart,
		title,
		titleKeyword,
		titleTrigram,
		title2,
		address,
		street,
		streetKeyword,
		streetSearchAsYouType,
		number,
		description,
		descriptionI18n,
		descriptionBase,
		descriptionBaseKeyword,
		descriptionEn,
		descriptionEnKeyword,
		descriptionDe,
		descriptionDeKeyword;

	static {
		defaultDocType = new DocType();
		defaultDocType.setName("default");

		complexNumber = new DocTypeField();
		complexNumber.setDocType(defaultDocType);
		complexNumber.setFieldName("complexNumber");
		complexNumber.setFieldType(FieldType.OBJECT);

		realPart = new DocTypeField();
		realPart.setDocType(defaultDocType);
		realPart.setFieldName("realPart");
		realPart.setFieldType(FieldType.INTEGER);
		realPart.setParentDocTypeField(complexNumber);

		imaginaryPart = new DocTypeField();
		imaginaryPart.setDocType(defaultDocType);
		imaginaryPart.setFieldName("imaginaryPart");
		imaginaryPart.setFieldType(FieldType.INTEGER);
		imaginaryPart.setParentDocTypeField(complexNumber);

		complexNumber.setSubDocTypeFields(new LinkedHashSet<>(
			List.of(realPart, imaginaryPart)));

		title = new DocTypeField();
		title.setDocType(defaultDocType);
		title.setFieldName("title");
		title.setFieldType(FieldType.TEXT);

		titleKeyword = new DocTypeField();
		titleKeyword.setDocType(defaultDocType);
		titleKeyword.setFieldName("keyword");
		titleKeyword.setFieldType(FieldType.KEYWORD);
		titleKeyword.setParentDocTypeField(title);

		titleTrigram = new DocTypeField();
		Analyzer trigram = new Analyzer();
		trigram.setName("trigram");
		trigram.setType("custom");
		titleTrigram.setDocType(defaultDocType);
		titleTrigram.setFieldName("trigram");
		titleTrigram.setFieldType(FieldType.TEXT);
		titleTrigram.setAnalyzer(trigram);
		titleTrigram.setParentDocTypeField(title);

		title.setSubDocTypeFields(new LinkedHashSet<>(List.of(titleKeyword, titleTrigram)));

		address = new DocTypeField();
		address.setDocType(defaultDocType);
		address.setFieldName("address");
		address.setFieldType(FieldType.OBJECT);

		street = new DocTypeField();
		street.setDocType(defaultDocType);
		street.setFieldName("street");
		street.setFieldType(FieldType.TEXT);
		street.setParentDocTypeField(address);

		streetKeyword = new DocTypeField();
		streetKeyword.setDocType(defaultDocType);
		streetKeyword.setFieldName("keyword");
		streetKeyword.setFieldType(FieldType.KEYWORD);
		streetKeyword.setParentDocTypeField(street);

		streetSearchAsYouType = new DocTypeField();
		streetSearchAsYouType.setDocType(defaultDocType);
		streetSearchAsYouType.setFieldName("search_as_you_type");
		streetSearchAsYouType.setFieldType(FieldType.SEARCH_AS_YOU_TYPE);
		streetSearchAsYouType.setParentDocTypeField(street);

		street.setSubDocTypeFields(
			new LinkedHashSet<>(List.of(streetKeyword, streetSearchAsYouType)));

		number = new DocTypeField();
		number.setDocType(defaultDocType);
		number.setFieldName("number");
		number.setFieldType(FieldType.INTEGER);
		number.setParentDocTypeField(address);

		address.setSubDocTypeFields(new LinkedHashSet<>(List.of(street, number)));

		webDocType = new DocType();
		webDocType.setName("web");

		title2 = new DocTypeField();
		title2.setDocType(webDocType);
		title2.setFieldName("title");
		title2.setFieldType(FieldType.TEXT);

		description = new DocTypeField();
		description.setDocType(webDocType);
		description.setFieldName("description");
		description.setFieldType(FieldType.I18N);

		descriptionBase = new DocTypeField();
		descriptionBase.setDocType(webDocType);
		descriptionBase.setFieldName("base");
		descriptionBase.setFieldType(FieldType.TEXT);
		descriptionBase.setParentDocTypeField(description);

		descriptionBaseKeyword = new DocTypeField();
		descriptionBaseKeyword.setDocType(webDocType);
		descriptionBaseKeyword.setFieldName("keyword");
		descriptionBaseKeyword.setFieldType(FieldType.KEYWORD);
		descriptionBaseKeyword.setParentDocTypeField(descriptionBase);
		descriptionBaseKeyword.setJsonConfig("{\"ignore_above\":256}");

		descriptionBase.setSubDocTypeFields(Set.of(descriptionBaseKeyword));

		descriptionI18n = new DocTypeField();
		descriptionI18n.setDocType(webDocType);
		descriptionI18n.setFieldName("i18n");
		descriptionI18n.setFieldType(FieldType.OBJECT);
		descriptionI18n.setParentDocTypeField(description);

		descriptionEn = new DocTypeField();
		descriptionEn.setDocType(webDocType);
		descriptionEn.setFieldName("en_US");
		descriptionEn.setFieldType(FieldType.TEXT);
		descriptionEn.setParentDocTypeField(descriptionI18n);

		descriptionEnKeyword = new DocTypeField();
		descriptionEnKeyword.setDocType(webDocType);
		descriptionEnKeyword.setFieldName("keyword");
		descriptionEnKeyword.setFieldType(FieldType.KEYWORD);
		descriptionEnKeyword.setParentDocTypeField(descriptionEn);
		descriptionEnKeyword.setJsonConfig("{\"ignore_above\":256}");

		descriptionEn.setSubDocTypeFields(Set.of(descriptionEnKeyword));

		descriptionDe = new DocTypeField();
		descriptionDe.setDocType(webDocType);
		descriptionDe.setFieldName("de_DE");
		descriptionDe.setFieldType(FieldType.TEXT);
		descriptionDe.setParentDocTypeField(descriptionI18n);

		descriptionDeKeyword = new DocTypeField();
		descriptionDeKeyword.setDocType(webDocType);
		descriptionDeKeyword.setFieldName("keyword");
		descriptionDeKeyword.setFieldType(FieldType.KEYWORD);
		descriptionDeKeyword.setParentDocTypeField(descriptionDe);
		descriptionDeKeyword.setJsonConfig("{\"ignore_above\":256}");

		descriptionDe.setSubDocTypeFields(Set.of(descriptionDeKeyword));

		descriptionI18n.setSubDocTypeFields(new LinkedHashSet<>(List.of(
			descriptionEn, descriptionDe)));

		description.setSubDocTypeFields(new LinkedHashSet<>(List.of(
			descriptionBase, descriptionI18n)));

		defaultDocType.setDocTypeFields(new LinkedHashSet<>(List.of(
			complexNumber,
			realPart,
			imaginaryPart,
			title,
			titleKeyword,
			titleTrigram,
			address,
			street,
			streetKeyword,
			streetSearchAsYouType,
			number
		)));

		webDocType.setDocTypeFields(new LinkedHashSet<>(List.of(
			title2,
			description,
			descriptionI18n,
			descriptionBase,
			descriptionBaseKeyword,
			descriptionEn,
			descriptionEnKeyword,
			descriptionDe,
			descriptionDeKeyword
		)));
	}

}