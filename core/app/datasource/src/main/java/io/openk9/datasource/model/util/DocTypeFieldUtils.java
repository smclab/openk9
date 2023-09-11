package io.openk9.datasource.model.util;

import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;

public class DocTypeFieldUtils {

	public static String fieldPath(DocTypeField docTypeField) {
		DocType docType = docTypeField.getDocType();
		return fieldPath(docType != null ? docType.getName() : null, docTypeField);
	}

	public static String fieldPath(String docTypeName, DocTypeField docTypeField) {

		String docTypePath =
			docTypeName != null && !docTypeName.equals("default") ? docTypeName + "." : "";

		return docTypeField.getParentDocTypeField() != null
			? fieldPath(docTypeField.getParentDocTypeField()) + "." + docTypeField.getFieldName()
			: docTypePath + docTypeField.getFieldName();
	}
}
