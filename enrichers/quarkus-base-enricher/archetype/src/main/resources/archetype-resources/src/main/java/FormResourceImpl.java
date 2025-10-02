package ${package};

import io.openk9.enricher.api.FormResource;
import io.openk9.enricher.api.beans.FieldValue;
import io.openk9.enricher.api.beans.Form;
import io.openk9.enricher.api.beans.FormField;
import io.openk9.enricher.api.beans.FormFieldValidator;
import java.util.ArrayList;
import java.util.List;

public class FormResourceImpl implements FormResource {

    @Override
    public Form form() {
        Form form = new Form();
        form.setFields(getFormFieldList());
        return form;
    }

    // Setting up FormField examples
    private List<FormField> getFormFieldList() {
        List<FormField> formFieldList = new ArrayList<>();

        // 1° FormField object
        FormField formField1 = new FormField();
        formField1.setInfo("");
        formField1.setName("testForm");
        formField1.setLabel("Test Form");
        formField1.setRequired(false);
        formField1.setSize(2.0);
        formField1.setType(FormField.Type.string);
        formField1.setValidator(getValidator());
        formField1.setValues(getFiledValueList());
        formFieldList.add(formField1);

        // 2° FormField object
        FormField formField2 = new FormField();
        formField2.setInfo("");
        formField2.setName("mainObject");
        formField2.setLabel("Main Object");
        formField2.setRequired(true);
        formField2.setSize(0.78);
        formField2.setType(FormField.Type.number);
        formField2.setValidator(getValidator());
        formField2.setValues(getFiledValueList());
        formFieldList.add(formField2);
        return formFieldList;
    }

    private FormFieldValidator getValidator() {
        FormFieldValidator formFieldValidator = new FormFieldValidator();
        formFieldValidator.setMin(0L);
        formFieldValidator.setMax(10L);
        formFieldValidator.setRegex("/[[:test]]");
        return formFieldValidator;
    }

    private List<FieldValue> getFiledValueList() {
        List<FieldValue> formFieldValueList = new ArrayList<>();
        FieldValue formFieldValue = new FieldValue();
        formFieldValue.setIsDefault(true);
        formFieldValue.setValue("Value example");
        formFieldValueList.add(formFieldValue);
        return formFieldValueList;
    }

}
