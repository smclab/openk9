import ClayCard from "@clayui/card";
import { CreateGraphic, LabelNumber } from "./Form";

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
    <ClayCard style={{ marginTop: "20px" }}>
      <ClayCard.Body>
        <div
          style={{
            display: "flex",
            fontSize: "24px",
            lineHeight: "32px",
            fontWeight: "600",
            fontFamily: "bold",
          }}
        >
          Engine Overview:
        </div>
        <div style={{ display: "flex", gap: "70px", alignItems: "flex-end" }}>
          <div>
            <LabelNumber label={firstCardLabel} number={firstCardNumber || 0} unity={firstCardUnity} />
            <LabelNumber label={secondCardLabel} number={secondCardNumber || 0} unity={secondCardUnity} />
            <LabelNumber label={thirdCardLabel} number={thirdCardNumber || 0} unity={thirdCardUnity} />
          </div>
          <div>
            <CreateGraphic data={dataGraph} height={210} width={350} labelInformationRigth="View Api Logs" Information="Total Queries" />
          </div>
          <div>
            <CreateGraphic data={secondDataGraph} height={210} width={350} labelInformationRigth="View Api Logs" Information="Total Api" />
          </div>
        </div>
      </ClayCard.Body>
    </ClayCard>
  );
}
