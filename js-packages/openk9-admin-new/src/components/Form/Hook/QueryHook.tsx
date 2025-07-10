import { QueryHookOptions, QueryResult } from "@apollo/client";





export type QueryHook<Query, QueryVariables extends Record<string, any>> = (
  baseOptions: QueryHookOptions<Query, QueryVariables>
) => QueryResult<Query, QueryVariables>;
