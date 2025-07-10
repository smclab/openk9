import TrashIcon from "@mui/icons-material/Delete";
import DocumentIcon from "@mui/icons-material/Description";
import RulerIcon from "@mui/icons-material/Straighten";
import { Box, Container } from "@mui/material";
import { CreateGraphic, LabelNumber } from "@components/Form";

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
    <Container>
      <Box display="flex" flexDirection="column" gap="20px">
        <Box display="flex" flexDirection="row" gap="30px" width="100%" alignItems="stretch">
          <LabelNumber
            label={firstCardLabel}
            number={firstCardNumber || 0}
            unity={firstCardUnity}
            icon={<DocumentIcon style={{ opacity: 0.2 }} />}
          />
          <LabelNumber
            label={secondCardLabel}
            number={secondCardNumber || 0}
            unity={secondCardUnity}
            icon={<TrashIcon style={{ opacity: 0.2 }} />}
          />
          <LabelNumber
            label={thirdCardLabel}
            number={thirdCardNumber || 0}
            unity={thirdCardUnity}
            icon={<RulerIcon style={{ opacity: 0.2 }} />}
          />
        </Box>
        <Box display="flex" gap="30px" width="100%">
          <CreateGraphic
            data={dataGraph}
            height={210}
            width={450}
            labelInformationRigth="View Api Logs"
            Information="Document Count "
          />
          <CreateGraphic
            data={secondDataGraph}
            height={210}
            width={450}
            labelInformationRigth="View Api Logs"
            Information="Total Api"
          />
        </Box>
      </Box>
    </Container>
  );
}
