import { MutationHookOptions, MutationTuple } from "@apollo/client";





export type MutationHook<
  Mutation,
  MutationVariables extends Record<string, any>
> = (
  baseOptions: MutationHookOptions<Mutation, MutationVariables>
) => MutationTuple<Mutation, MutationVariables>;
