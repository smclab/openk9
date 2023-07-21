import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import { useToast } from "./ToastProvider";
import { useBucketQuery, useCreateOrUpdateBucketMutation } from "../graphql-generated";
import { LanguagesQuery } from "./Languages";

// const LanguageQuery = gql`
//   query Language($id: ID!) {
//     language(id: $id) {
//       id
//       name
//       value
//     }
//   }
// `;

// gql`
//   mutation CreateOrUpdateLanguage($id: ID, $name: String!, $value: String) {
//     language(id: $id, languageDTO: { name: $name, value: $value }) {
//       entity {
//         id
//         name
//         value
//       }
//       fieldValidators {
//         field
//         message
//       }
//     }
//   }
// `;

export function Language() {
  const { languageId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  // const languageQuery = useLanguageQuery({
  //   variables: { id: languageId as string },
  //   skip: !languageId || languageId === "new",
  // });
  // const [createOrUpdateBucketMutate, createOrUpdateBucketMutation] = useCreateOrUpdateBucketMutation({
  //   refetchQueries: [LanguageQuery, LanguagesQuery],
  //   onCompleted(data) {
  //     if (data.bucket?.entity) {
  //       if (bucketId === "new") {
  //         navigate(`/buckets/`, { replace: true });
  //         showToast({ displayType: "success", title: "Bucket created", content: data.bucket.entity.name ?? "" });
  //       } else {
  //         showToast({ displayType: "info", title: "Bucket updated", content: data.bucket.entity.name ?? "" });
  //       }
  //     }
  //   },
  // });
  // const form = useForm({
  //   initialValues: React.useMemo(
  //     () => ({
  //       name: "",
  //       description: "",
  //       enable: false,
  //       handleDynamicFilters: false,
  //     }),
  //     []
  //   ),
  //   originalValues: bucketQuery.data?.bucket,
  //   isLoading: bucketQuery.loading || createOrUpdateBucketMutation.loading,
  //   onSubmit(data) {
  //     createOrUpdateBucketMutate({ variables: { id: bucketId !== "new" ? bucketId : undefined, ...data } });
  //   },
  //   getValidationMessages: fromFieldValidators(createOrUpdateBucketMutation.data?.bucket?.fieldValidators),
  // });
  return (
    <div>test</div>
    // <ContainerFluid>
    //   {bucketId !== "new" && <MainTitle title="Attribute" />}
    //   <form
    //     className="sheet"
    //     onSubmit={(event) => {
    //       event.preventDefault();
    //       form.submit();
    //     }}
    //   >
    //     <TextInput label="Name" {...form.inputProps("name")} />
    //     <TextArea label="Description" {...form.inputProps("description")} />
    //     {bucketId !== "new" && (
    //       <React.Fragment>
    //         <SearchSelect
    //           label="Query Analyzer"
    //           value={bucketQuery.data?.bucket?.queryAnalysis?.id}
    //           useValueQuery={useQueryAnalysisValueQuery}
    //           useOptionsQuery={useQueryAnalysisOptionsQuery}
    //           useChangeMutation={useBindQueryAnalysisToBucketMutation}
    //           mapValueToMutationVariables={(queryAnalysis) => ({ bucketId, queryAnalysis })}
    //           useRemoveMutation={useUnbindQueryAnalysisFromBucketMutation}
    //           mapValueToRemoveMutationVariables={() => ({ bucketId })}
    //           invalidate={() => bucketQuery.refetch()}
    //           description={"Query Analysis configuration for current bucket"}
    //         />

    //         <SearchSelect
    //           label="Search Config"
    //           value={bucketQuery.data?.bucket?.searchConfig?.id}
    //           useValueQuery={useSearchConfigValueQuery}
    //           useOptionsQuery={useSearchConfigOptionsQuery}
    //           useChangeMutation={useBindSearchConfigToBucketMutation}
    //           mapValueToMutationVariables={(searchConfigId) => ({ bucketId, searchConfigId })}
    //           useRemoveMutation={useUnbindSearchConfigFromBucketMutation}
    //           mapValueToRemoveMutationVariables={() => ({ bucketId })}
    //           invalidate={() => bucketQuery.refetch()}
    //           description={"Search Configuration for current bucket"}
    //         />
    //       </React.Fragment>
    //     )}
    //     {bucketId !== "new" && (
    //       <BooleanInput
    //         label="Dynamic Filters"
    //         description=" Allow to handle filter in dynamic way. Filters will change base to current query."
    //         {...form.inputProps("handleDynamicFilters")}
    //       />
    //     )}
    //     <div className="sheet-footer">
    //       <CustomButtom nameButton={bucketId === "new" ? "Create" : "Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
    //     </div>
    //   </form>
    // </ContainerFluid>
  );
}
