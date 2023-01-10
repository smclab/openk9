import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import ClayButton from "@clayui/button";
import { useCreateOrUpdateRuleQueryMutation, useQueryAnalysesRulesQuery, useRuleQuery } from "../graphql-generated";
import { RulesQuery } from "./Rules";
import { useForm, fromFieldValidators, TextInput, TextArea } from "./Form";
import ClayLayout from "@clayui/layout";
import { useToast } from "./ToastProvider";
import { AddRuleToQueryAnalyses, QueryAnalysesRule, RemoveRuleFromQueryAnalyses } from "./QueryAnalysesRules";
import { ClassNameButton } from "../App";

const RuleQuery = gql`
  query Rule($id: ID!) {
    rule: rule(id: $id) {
      id
      name
      description
      lhs
      rhs
    }
  }
`;

gql`
  mutation CreateOrUpdateRuleQuery($id: ID, $name: String!, $description: String, $lhs: String!, $rhs: String!) {
    rule(id: $id, ruleDTO: { name: $name, description: $description, lhs: $lhs, rhs: $rhs }) {
      entity {
        id
        name
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

export function Rule() {
  const { ruleId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const ruleQuery = useRuleQuery({
    variables: { id: ruleId as string },
    skip: !ruleId || ruleId === "new",
  });
  const [createOrUpdateRuleMutate, createOrUpdateRuleMutation] = useCreateOrUpdateRuleQueryMutation({
    refetchQueries: [RuleQuery, RulesQuery, QueryAnalysesRule, AddRuleToQueryAnalyses, RemoveRuleFromQueryAnalyses],
    onCompleted(data) {
      if (data.rule?.entity) {
        if (ruleId === "new") {
          navigate(`/rules/`, { replace: true });
          showToast({ displayType: "success", title: "Rule created", content: data.rule.entity.name ?? "" });
        } else {
          navigate(`/rules/`, { replace: true });
          showToast({ displayType: "info", title: "Rule updated", content: data.rule.entity.name ?? "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        lhs: "",
        rhs: "",
      }),
      []
    ),
    originalValues: ruleQuery.data?.rule,
    isLoading: ruleQuery.loading || createOrUpdateRuleMutation.loading,
    onSubmit(data) {
      createOrUpdateRuleMutate({ variables: { id: ruleId !== "new" ? ruleId : undefined, ...data } });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateRuleMutation.data?.rule?.fieldValidators),
  });
  return (
    <ClayLayout.ContainerFluid view>
      <ClayForm
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} />
        <TextArea label="Description" {...form.inputProps("description")} />
        <TextInput label="Left Hand Side" {...form.inputProps("lhs")} />
        <TextInput label="Right Hand Side" {...form.inputProps("rhs")} />
        <div className="sheet-footer">
          <ClayButton className={ClassNameButton} type="submit" disabled={!form.canSubmit}>
            {ruleId === "new" ? "Create" : "Update"}
          </ClayButton>
        </div>
      </ClayForm>
    </ClayLayout.ContainerFluid>
  );
}
