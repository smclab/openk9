package ${package};

import io.openk9.enricher.api.FormResource;
import io.openk9.enricher.api.beans.FieldValue;
import io.openk9.enricher.api.beans.Form;
import io.openk9.enricher.api.beans.FormField;
import io.openk9.enricher.api.beans.FormFieldValidator;

import java.util.List;

/**
 * Example form of an enricher reading the binaries of the documents.
 * OpenK9 shows these fields when an enrich item is configured, and sends the values back in the
 * enrichItemConfig of each process request, keyed by field name. {@code size} is the width of the field,
 * from 1 to 12 columns; the value marked as default is the initial one, and for select fields the values
 * are the options.
 */
public class FormResourceImpl implements FormResource {

    @Override
    public Form form() {
        Form form = new Form();
        form.setFields(List.of(
                field("targetField", "Target Field", "Field of the document where the enricher writes its result",
                        FormField.Type.TEXT, 6, true, List.of(value("enrichExample", true))),
                field("contentTypes", "Content Types", "Content types of the binaries to read; none to read them all",
                        FormField.Type.MULTISELECT, 6, false, List.of(
                                value("application/pdf", true), value("text/plain", false),
                                value("text/html", false), value("image/png", false))),
                maxBinarySize(),
                field("readBinaries", "Read Binaries",
                        "Read the binaries through their staging URL; when off, only the text is used",
                        FormField.Type.CHECKBOX, 4, false, List.of(value(true, true)))
        ));
        return form;
    }

    private FormField maxBinarySize() {
        FormFieldValidator validator = new FormFieldValidator();
        validator.setMin(1L);
        validator.setMax(100L);
        FormField maxBinarySize = field("maxBinarySize", "Max Binary Size (MB)",
                "Largest binary to read, in megabytes", FormField.Type.NUMBER, 4, false, List.of(value(10, true)));
        maxBinarySize.setValidator(validator);
        return maxBinarySize;
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
