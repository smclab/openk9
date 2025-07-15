import styled from "@emotion/styled";

export const ParagraphTime = styled.p<{ $color?: string }>`
  color: ${(props) => props.$color};
  margin: 0px;
  align-self: end;
  font-size: 10px;
  font-weight: 500;
  line-height: 11.72px;
`;
export const ParagraphMessage = styled.p<{ $color?: string }>`
    color:${(props) => props.$color};
    margin: 0px;
    fontSize: "12px",
    fontWeight: "400",
    lineHeight: "18.25px",
    textAlign: "left",
    `;