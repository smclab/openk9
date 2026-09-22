import type { Interpolation } from "styled-components";

declare module "react" {
  interface DOMAttributes<T> {
    css?: Interpolation<object>;
  }
}
