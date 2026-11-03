package ${package};

import io.openk9.connector.api.FormResource;
import io.openk9.connector.api.beans.FieldValue;
import io.openk9.connector.api.beans.Form;
import io.openk9.connector.api.beans.FormField;
import io.openk9.connector.api.beans.FormFieldValidator;

import java.util.List;

/**
 * Example form of a connector reading documents from an HTTP source.
 * OpenK9 shows these fields when a datasource is configured, and sends the values back in the
 * invoke request, keyed by field name. {@code size} is the width of the field, from 1 to 12 columns;
 * the value marked as default is the initial one, and for select fields the values are the options.
 */
public class FormResourceImpl implements FormResource {

    @Override
    public Form form() {
        Form form = new Form();
        form.setFields(List.of(
                field("baseUrl", "Base URL", "Base URL of the source to read the documents from",
                        FormField.Type.URL, 8, true, List.of(value("https://example.org", true))),
                field("apiToken", "API Token", "Access token of the source, if it requires one",
                        FormField.Type.PASSWORD, 4, false, List.of()),
                field("paths", "Paths", "Paths to read, relative to the base URL; empty to read the whole source",
                        FormField.Type.LIST, 12, false, List.of()),
                field("language", "Language", "Language of the documents",
                        FormField.Type.SELECT, 4, true, List.of(
                                value("en", true), value("it", false), value("de", false),
                                value("fr", false), value("es", false))),
                field("documentKinds", "Document Kinds", "Kinds of document to import",
                        FormField.Type.MULTISELECT, 4, false, List.of(
                                value("article", true), value("page", false), value("news", false))),
                pageSize(),
                field("includeAttachments", "Include Attachments", "Import the attachments as binaries too",
                        FormField.Type.CHECKBOX, 2, false, List.of(value(false, true)))
        ));
        return form;
    }

    private FormField pageSize() {
        FormFieldValidator validator = new FormFieldValidator();
        validator.setMin(1L);
        validator.setMax(500L);
        FormField pageSize = field("pageSize", "Page Size", "Documents read for each request to the source",
                FormField.Type.NUMBER, 2, false, List.of(value(50, true)));
        pageSize.setValidator(validator);
        return pageSize;
    }

    private FormField field(
            String name, String label, String info, FormField.Type type, int size, boolean required,
            List<FieldValue> values) {
        FormField formField = new FormField();
        formField.setName(name);
        formField.setLabel(label);
        formField.setInfo(info);
        formField.setType(type);
        formField.setSize((double) size);
        formField.setRequired(required);
        formField.setValues(values);
        return formField;
    }

    private FieldValue value(Object value, boolean isDefault) {
        FieldValue fieldValue = new FieldValue();
        fieldValue.setValue(value);
        fieldValue.setIsDefault(isDefault);
        return fieldValue;
    }
}
