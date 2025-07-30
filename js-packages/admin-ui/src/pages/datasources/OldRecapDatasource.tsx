import {
  Autocomplete,
  Box,
  Chip,
  Grid,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { ConnectionData } from "./types";

function RecapDatasource({ formValues, jsonConfig }: { formValues: ConnectionData; jsonConfig: string }) {
  const order = [
    "name",
    "description",
    "datasourceId",
    "jsonConfig",
    "linkedEnrichItems",
    "schedulingMonth",
    "enrichPipeline",
  ];

  const excludeKeys = ["dataIndexes", "__typename"];

  const renderFields: any = (values: any, prefix = "", isNested = false) => {
    const filteredEntries = Object.entries(values)
      .filter(([key, value]) => {
        return (
          !excludeKeys.some((excludedKey) => key.includes(excludedKey)) &&
          value !== null &&
          value !== undefined &&
          ((typeof value === "string" &&
            value.trim() !== "" &&
            !(key.toLowerCase().includes("id") && value === "new")) ||
            (typeof value === "object" && value !== null && Object.keys(value).length > 0))
        );
      })
      .sort(([keyA], [keyB]) => {
        const indexA = order.indexOf(keyA);
        const indexB = order.indexOf(keyB);

        if (indexA === -1 && indexB === -1) {
          return keyA.localeCompare(keyB);
        }

        if (indexA === -1) return 1;
        if (indexB === -1) return -1;

        return indexA - indexB;
      });

    if (filteredEntries.length === 0) {
      return null;
    }

    return filteredEntries.map(([key, value], index) => {
      const displayKey = key;

      if (key === "jsonConfig" && typeof value === "string") {
        try {
          const parsedValue = JSON.parse(value);
          if (Array.isArray(parsedValue)) {
            return parsedValue.map((item, idx) => {
              const label = item.label || item.name || `Field ${idx}`;
              const valuesField = item.values;
              if (item.type === "list" && Array.isArray(valuesField)) {
                return (
                  <Grid item xs={12} key={`${prefix}${label}-${idx}`}>
                    <Typography
                      variant="h6"
                      style={{
                        marginTop: index > 0 ? "10px" : "0px",
                        marginBottom: "5px",
                        fontWeight: "bold",
                      }}
                    >
                      {label as any}
                    </Typography>
                    <Autocomplete
                      freeSolo
                      multiple
                      disabled
                      options={[]}
                      sx={{ minWidth: "230px", marginBottom: "15px" }}
                      value={valuesField.map((val: any) => val.value) || []}
                      renderTags={(value: string[], getTagProps) =>
                        value?.map((option: string, idx: number) => (
                          <Chip label={option} {...getTagProps({ index: idx })} onDelete={undefined} />
                        ))
                      }
                      renderInput={(params) => (
                        <TextField {...params} variant="outlined" name={label} placeholder="Values" />
                      )}
                    />
                  </Grid>
                );
              } else if (item.type === "select") {
                const selectedValue = valuesField.find((val: any) => val.isDefault);
                return (
                  <Grid item xs={12} md={6} key={`${prefix}${label}-${idx}`}>
                    <Typography variant="body2" style={{ fontWeight: "bold", marginBottom: "3px" }}>
                      {label as any}
                    </Typography>
                    <TextField
                      value={selectedValue?.value || "N/A"}
                      fullWidth
                      disabled
                      variant="outlined"
                      size="small"
                      InputLabelProps={{
                        style: {
                          fontSize: "0.9rem",
                        },
                      }}
                      style={{ marginBottom: "15px" }}
                    />
                  </Grid>
                );
              } else if (
                typeof valuesField === "object" &&
                valuesField !== null &&
                Array.isArray(valuesField) &&
                valuesField.length > 0
              ) {
                const finalValue = valuesField[0].value ?? "N/A";
                return (
                  <Grid item xs={12} md={6} key={`${prefix}${label}-${idx}`}>
                    <Typography variant="body2" style={{ fontWeight: "bold", marginBottom: "3px" }}>
                      {label as any}
                    </Typography>
                    <TextField
                      value={String(finalValue)}
                      fullWidth
                      disabled
                      variant="outlined"
                      size="small"
                      InputLabelProps={{
                        style: {
                          fontSize: "0.9rem",
                        },
                      }}
                      style={{ marginBottom: "15px" }}
                    />
                  </Grid>
                );
              }
            });
          }
        } catch (error) {
          console.error("Invalid JSON in jsonConfig:", error);
        }
      }

      if (key === "schedulingMonth" && Array.isArray(value)) {
        return (
          <Grid item xs={12} key={displayKey}>
            <Typography
              variant="h6"
              style={{
                marginTop: index > 0 ? "10px" : "0px",
                marginBottom: "5px",
                fontWeight: "bold",
              }}
            >
              Scheduling Month
            </Typography>
            <Autocomplete
              freeSolo
              multiple
              disabled
              options={[]}
              sx={{ minWidth: "230px", marginBottom: "15px" }}
              value={value || []}
              renderTags={(value: string[], getTagProps) =>
                value?.map((option: string, idx: number) => (
                  <Chip label={option} {...getTagProps({ index: idx })} onDelete={undefined} />
                ))
              }
              renderInput={(params) => (
                <TextField {...params} variant="outlined" name="schedulingMonth" placeholder="Months" />
              )}
            />
          </Grid>
        );
      }

      if (key === "linkedEnrichItems" && Array.isArray(value) && value.length > 0) {
        return (
          <Grid item xs={12} key={displayKey}>
            <Typography
              variant="h6"
              style={{
                marginTop: index > 0 ? "10px" : "0px",
                marginBottom: "5px",
                fontWeight: "bold",
                textDecoration: "underline",
              }}
            >
              Linked Enrich Items
            </Typography>
            <TableContainer component={Paper} style={{ marginBottom: "15px" }}>
              <Table aria-label="linked enrich items table">
                <TableHead>
                  <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>Name</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {value.map((item: any, idx: number) => (
                    <TableRow key={idx}>
                      <TableCell>{item.id || "N/A"}</TableCell>
                      <TableCell>{item.name || "N/A"}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Grid>
        );
      }

      if (typeof value === "object" && value !== null && Object.keys(value).length > 0) {
        const renderedNestedFields = renderFields(value, displayKey, true);
        if (renderedNestedFields) {
          return (
            <Grid item xs={12} key={displayKey}>
              {/* <Typography
                variant="h5"
                style={{
                  marginTop: index > 0 ? "10px" : "0px",
                  marginBottom: "5px",
                  fontWeight: "bold",
                  color: "#3f51b5", // Enfatizza il nome della categoria con un colore distinto
                }}
              >
                {displayKey as any}
              </Typography> */}
              <Box sx={{ paddingLeft: isNested ? "15px" : "0px" }}>{renderedNestedFields}</Box>
            </Grid>
          );
        }
        return null;
      }

      return (
        <Grid item xs={12} md={6} key={displayKey}>
          <Typography variant="body2" style={{ fontWeight: "bold", marginBottom: "3px" }}>
            {displayKey as any}
          </Typography>
          <TextField
            value={String(value)}
            fullWidth
            disabled
            variant="outlined"
            size="small"
            InputLabelProps={{
              style: {
                fontSize: "0.9rem",
              },
            }}
            style={{ marginBottom: "15px" }}
          />
        </Grid>
      );
    });
  };
  const parsedJson = JSON.parse(jsonConfig || "{}");
  const keys = Object.keys(parsedJson);

  return (
    <Box display={"flex"} flexDirection={"column"}>
      <Grid container spacing={2} sx={{ padding: "20px", width: "60%" }}>
        <Grid item xs={12}>
          <Typography variant="h4" sx={{ fontWeight: "bold", marginBottom: "20px" }}>
            Recap of Connection Data
          </Typography>
        </Grid>
        {renderFields(formValues)}
      </Grid>
      <Box sx={{ padding: "0 0 20px 20px", width: "60%" }}>
        {keys.map((key) => {
          return (
            <>
              {parsedJson[key] && parsedJson[key].length > 0 && (
                <>
                  <Typography variant="body2" fontWeight={"bold"} marginBottom={"3px"}>{`${key}:`}</Typography>
                  <TextField size="small" sx={{ marginBottom: "32px" }} disabled label={`${parsedJson[key]}`} />
                </>
              )}
            </>
          );
        })}
      </Box>
    </Box>
  );
}

export default RecapDatasource;
