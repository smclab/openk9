package io.openk9.datasource.index.mappings;

import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class MappingsUtilTest {

	@Test
	void docTypesToMappings() {
		Map<MappingsKey, Object> result = MappingsUtil.docTypesToMappings(List.of(aDocType()));
		Assertions.assertEquals(expectedJson(), JsonObject.mapFrom(result));
	}

	private static DocType aDocType() {
		DocType docType = new DocType();
		docType.setName("aDocType");

		DocTypeField realPart = new DocTypeField();
		realPart.setDocType(docType);
		realPart.setFieldName("complexNumber.realPart");
		realPart.setFieldType(FieldType.INTEGER);

		DocTypeField imaginaryPart = new DocTypeField();
		imaginaryPart.setDocType(docType);
		imaginaryPart.setFieldName("complexNumber.imaginaryPart");
		imaginaryPart.setFieldType(FieldType.INTEGER);

		DocTypeField title = new DocTypeField();
		title.setDocType(docType);
		title.setFieldName("title");
		title.setFieldType(FieldType.TEXT);

		DocTypeField address = new DocTypeField();
		address.setDocType(docType);
		address.setFieldName("address");
		address.setFieldType(FieldType.OBJECT);

		DocTypeField street = new DocTypeField();
		street.setDocType(docType);
		street.setFieldName("address.street");
		street.setFieldType(FieldType.TEXT);
		street.setParentDocTypeField(address);

		DocTypeField streetKeyword = new DocTypeField();
		streetKeyword.setDocType(docType);
		streetKeyword.setFieldName("address.street.keyword");
		streetKeyword.setFieldType(FieldType.KEYWORD);
		streetKeyword.setParentDocTypeField(street);

		street.setSubDocTypeFields(Set.of(streetKeyword));

		DocTypeField number = new DocTypeField();
		number.setDocType(docType);
		number.setFieldName("address.number");
		number.setFieldType(FieldType.INTEGER);
		number.setParentDocTypeField(address);

		address.setSubDocTypeFields(new HashSet<>(List.of(street, number)));

		DocTypeField description = new DocTypeField();
		description.setDocType(docType);
		description.setFieldName("description");
		description.setFieldType(FieldType.I18N);

		DocTypeField descriptionBase = new DocTypeField();
		descriptionBase.setDocType(docType);
		descriptionBase.setFieldName("base");
		descriptionBase.setFieldType(FieldType.TEXT);
		descriptionBase.setParentDocTypeField(description);

		DocTypeField descriptionBaseKeyword = new DocTypeField();
		descriptionBaseKeyword.setDocType(docType);
		descriptionBaseKeyword.setFieldName("keyword");
		descriptionBaseKeyword.setFieldType(FieldType.KEYWORD);
		descriptionBaseKeyword.setParentDocTypeField(descriptionBase);
		descriptionBaseKeyword.setJsonConfig("{\"ignore_above\":256}");
		descriptionBase.setSubDocTypeFields(Set.of(descriptionBaseKeyword));

		DocTypeField descriptionEn = new DocTypeField();
		descriptionEn.setDocType(docType);
		descriptionEn.setFieldName("description.i18n.en_US");
		descriptionEn.setFieldType(FieldType.TEXT);
		descriptionEn.setParentDocTypeField(description);

		DocTypeField descriptionEnKeyword = new DocTypeField();
		descriptionEnKeyword.setDocType(docType);
		descriptionEnKeyword.setFieldName("description.i18n.en_US.keyword");
		descriptionEnKeyword.setFieldType(FieldType.KEYWORD);
		descriptionEnKeyword.setParentDocTypeField(descriptionEn);
		descriptionEnKeyword.setJsonConfig("{\"ignore_above\":256}");

		descriptionEn.setSubDocTypeFields(Set.of(descriptionEnKeyword));

		DocTypeField descriptionDe = new DocTypeField();
		descriptionDe.setDocType(docType);
		descriptionDe.setFieldName("i18n.de_DE");
		descriptionDe.setFieldType(FieldType.TEXT);
		descriptionDe.setParentDocTypeField(description);

		DocTypeField descriptionDeKeyword = new DocTypeField();
		descriptionDeKeyword.setDocType(docType);
		descriptionDeKeyword.setFieldName("keyword");
		descriptionDeKeyword.setFieldType(FieldType.KEYWORD);
		descriptionDeKeyword.setParentDocTypeField(descriptionDe);
		descriptionDeKeyword.setJsonConfig("{\"ignore_above\":256}");

		descriptionDe.setSubDocTypeFields(Set.of(descriptionDeKeyword));

		description.setSubDocTypeFields(new HashSet<>(List.of(descriptionBase, descriptionEn, descriptionDe)));

		docType.setDocTypeFields(new HashSet<>(List.of(realPart, imaginaryPart, title, address, description)));

		return docType;
	}

	private static Object expectedJson() {
		return Json.decodeValue("""
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
			  "type": "text"
			},
			"address": {
			  "properties": {
				"street": {
				  "type": "text",
				  "fields": {
					"keyword": {
					  "type": "keyword"
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
	}

}