import { CreateGraphic, LabelNumber } from "./Form";
import ClayLayout from "@clayui/layout";
import ClayIcon from "@clayui/icon";

type detailGraphProps = {
  dataGraph: {
    name: string;
    query: number;
  }[];
  secondDataGraph: {
    name: string;
    query: number;
  }[];
  firstCardNumber: number;
  secondCardNumber: number;
  thirdCardNumber: number;
  firstCardLabel: string;
  secondCardLabel: string;
  thirdCardLabel: string;
  firstCardUnity?: string;
  secondCardUnity?: string;
  thirdCardUnity?: string;
};

export function DetailGraph({
  dataGraph,
  firstCardLabel,
  secondCardLabel,
  thirdCardLabel,
  secondDataGraph,
  firstCardNumber,
  secondCardNumber,
  thirdCardNumber,
  firstCardUnity = "",
  secondCardUnity = "",
  thirdCardUnity = "",
}: detailGraphProps) {
  return (
    <ClayLayout.ContainerFluid view>
      <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
        <div style={{ color: "#27272A", fontWeight: "600", fontSize: "22px" }}>Engine Overview:</div>
        <div style={{ display: "flex", flexDirection: "row", gap: "30px", width: "100%", alignItems: "stretch" }}>
          <LabelNumber
            label={firstCardLabel}
            number={firstCardNumber || 0}
            unity={firstCardUnity}
            icon={<ClayIcon symbol={"document-text"} />}
          />
          <LabelNumber
            label={secondCardLabel}
            number={secondCardNumber || 0}
            unity={secondCardUnity}
            icon={<ClayIcon symbol={"trash"}></ClayIcon>}
          />
          <LabelNumber label={thirdCardLabel} number={thirdCardNumber || 0} unity={thirdCardUnity} icon={<ClayIcon symbol="ruler" />} />
        </div>
        <div style={{ display: "flex", gap: "30px", width: "100%" }}>
          <CreateGraphic data={dataGraph} height={210} width={450} labelInformationRigth="View Api Logs" Information="Document Count " />
          <CreateGraphic data={secondDataGraph} height={210} width={450} labelInformationRigth="View Api Logs" Information="Total Api" />
        </div>
      </div>
    </ClayLayout.ContainerFluid>
  );
}
