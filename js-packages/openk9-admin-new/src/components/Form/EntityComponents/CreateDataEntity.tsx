import React, { useContext } from "react";
import { RecapData } from "./RecapData";
import {
  Box,
  Button,
  FormControl,
  MenuItem,
  SelectChangeEvent,
  Typography,
  Select as SelectMaterial,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { ContainerFluid } from "..";

interface Option {
  value?: string | null | undefined;
  label?: string | null | undefined;
}

interface Props {
  options: Option[];
  defaultSelect?: Option;
  onChange: React.Dispatch<
    React.SetStateAction<{
      value: string | null | undefined;
      label: string | null | undefined;
    }>
  >;
  label: string;
}

export function CreateDataEntity({
  form,
  informationSuggestion,
  page,
  id = "new",
  setPage,
  fieldsControll,
  haveConfirmButton,
  associations,
  pathBack,
  preSubmit,
  associationsMultiSelect,
  isFooterButton = true,
}: {
  page: number;
  id?: string;
  setPage: React.Dispatch<React.SetStateAction<number>>;
  form: any;
  pathBack: string;
  preSubmit?: React.ReactNode;
  associations?: Array<{
    labelName: string;
    multiAssociation: boolean;
    options?: {
      value: string | null | undefined;
      label: string | null | undefined;
    }[];
    defaultSelect?: {
      value: string | null | undefined;
      label: string | null | undefined;
    };

    onchange?: React.Dispatch<
      React.SetStateAction<{
        value: string | null | undefined;
        label: string | null | undefined;
      }>
    >;
    level: number;
  }>;
  associationsMultiSelect?: Array<{
    labelName: string;
    right: {
      value: string;
      label: string;
    }[];
    left: {
      value: string;
      label: string;
    }[];
    items: {
      label: string;
      value: string;
    }[][];
    setItems: React.Dispatch<
      React.SetStateAction<
        {
          label: string;
          value: string;
        }[][]
      >
    >;
    selectValue?: Array<{
      value: string;
      label: string;
    }>;
    onchange?: React.Dispatch<React.SetStateAction<never[]>>;
    level: number;
  }>;
  informationSuggestion: Array<{
    content?: React.ReactElement;
    page?: number;
    validation?: boolean;
  }>;
  haveConfirmButton: boolean;
  fieldsControll?: Array<string>;
  isFooterButton?: boolean;
}) {
  const control = fieldsControll?.every((field) => form.inputProps(field).value);
  const Data: React.ReactNode = informationSuggestion.find((informationS) => informationS.page === page)?.content;
  const associateOneToOne = associations?.filter(
    (information) => information.level === page && information.multiAssociation === false,
  );
  const multiAssociation = associationsMultiSelect?.filter((information) => information.level === page);

  if (informationSuggestion[page].validation) {
    return (
      <RecapData
        Data={informationSuggestion}
        form={form}
        page={page}
        setPage={setPage}
        submit={haveConfirmButton}
        pathBack={pathBack}
        preSubmit={preSubmit}
        associateOneToOne={associations}
        multiAssociation={associationsMultiSelect}
        isCreate={id === "new"}
      />
    );
  }

  return (
    <React.Fragment>
      {Data}
      {associateOneToOne?.map(
        (associate, index) =>
          associate.options &&
          associate.onchange && (
            <div style={{ paddingBottom: "20px" }} key={index}>
              <CustomSelect
                options={associate.options}
                onChange={associate.onchange}
                defaultSelect={associate.defaultSelect}
                label={associate.labelName}
              />
            </div>
          ),
      )}
      {isFooterButton && (
        <FooterButton
          control={control}
          isSubmit={haveConfirmButton}
          page={page}
          setPage={setPage}
          pathComeBack={pathBack}
        />
      )}
    </React.Fragment>
  );
}

function FooterButton({
  page,
  setPage,
  control,
  isSubmit,
  pathComeBack,
}: {
  page: number;
  setPage: React.Dispatch<React.SetStateAction<number>>;
  control?: boolean;
  isSubmit: boolean;
  pathComeBack: string;
}) {
  const navigate = useNavigate();

  return (
    <div
      style={{
        display: "flex",
        flexWrap: "wrap",
        justifyContent: "space-between",
        marginBlock: "32px",
      }}
    >
      <Button
        className="btn btn-secondary"
        type="button"
        variant="outlined"
        color="primary"
        onClick={() => {
          if (page === 0) {
            navigate(pathComeBack, { replace: true });
          } else {
            setPage((p) => p - 1);
          }
        }}
      >
        BACK
      </Button>
      {isSubmit && (
        <Button
          className={`btn ${control && "btn-danger"}`}
          type="button"
          variant={"contained"}
          sx={{ border: !control ? "1px solid" : "unset", borderColor: !control ? "rgba(0, 0, 0, 0.26)" : "unset" }}
          color="primary"
          disabled={!control}
          onClick={() => {
            setPage((p) => p + 1);
          }}
        >
          {"SAVE AND CONTINUE"}
        </Button>
      )}
    </div>
  );
}

const CustomSelect: React.FC<Props> = ({ options, defaultSelect, onChange, label }) => {
  const handleChange = (event: SelectChangeEvent<string>) => {
    const selectedValue = event.target.value as string | null | undefined;
    const selectedLabel = options.find((option) => option.value === selectedValue)?.label ?? "";

    onChange({
      value: selectedValue,
      label: selectedLabel,
    });
  };

  return (
    <FormControl fullWidth>
      <Box sx={{ marginBottom: 1 }}>
        <Typography variant="subtitle1" component="label" htmlFor={"label" + label}>
          {label}
        </Typography>
      </Box>
      <SelectMaterial value={defaultSelect?.value || ""} id={"label" + label} onChange={handleChange} displayEmpty>
        <MenuItem value="">
          <em>Select an option</em>
        </MenuItem>
        {options.map((option) => (
          <MenuItem key={option.value || ""} value={option.value || ""}>
            {option.label || "Unnamed option"}
          </MenuItem>
        ))}
      </SelectMaterial>
    </FormControl>
  );
};
