import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import DomainIcon from "@mui/icons-material/Domain";
import { Box, Button, CircularProgress, Container, IconButton, Step, StepLabel, Stepper, Toolbar, Typography } from "@mui/material";
import React from "react";
import { useNavigate } from "react-router-dom";
import { useRestClient } from "../client/queryClient";
import { useToast } from "../ToastProvider";
import { buildCreateTenantRequest } from "./payload";
import { isStep1Valid, Step1Form } from "./Step1Form";
import { isStep2Valid, Step2Security } from "./Step2Security";
import { Step3Confirm } from "./Step3Confirm";
import { initialWizardState, WizardState } from "./types";
import { WizardSuccess } from "./WizardSuccess";

const steps = ["Gateway Configuration", "Route Protection", "Confirmation"];

type CreatedTenant = { id: string; tenantName: string };

export function TenantCreate() {
  const navigate = useNavigate();
  const showToast = useToast();
  const restClient = useRestClient();
  const [activeStep, setActiveStep] = React.useState(0);
  const [state, setState] = React.useState<WizardState>(initialWizardState);
  const [created, setCreated] = React.useState<CreatedTenant | null>(null);
  const [loading, setLoading] = React.useState(false);

  function handleNext() {
    if (activeStep === 0 && !isStep1Valid(state.step1)) return;
    if (activeStep === 1 && !isStep2Valid(state.step2)) return;
    setActiveStep((s) => s + 1);
  }
  function handleBack() {
    setActiveStep((s) => Math.max(0, s - 1));
  }
  async function handleSubmit() {
    if (!state.step2.securityConfiguration) return;
    setLoading(true);
    try {
      const tenant = await restClient.tenantProvisioning.createTenant(buildCreateTenantRequest(state));
      setCreated({ id: String(tenant.id), tenantName: tenant.tenantName ?? state.step1.tenantName });
      showToast({ displayType: "success", title: "Tenant created", content: "" });
    } catch (error: any) {
      showToast({
        displayType: "error",
        title: "Tenant creation failed",
        content: error?.body?.message ?? error?.message ?? "Unknown error",
      });
    } finally {
      setLoading(false);
    }
  }
  function handleCreateAnother() {
    setState(initialWizardState);
    setActiveStep(0);
    setCreated(null);
  }

  const canNext =
    (activeStep === 0 && isStep1Valid(state.step1)) ||
    (activeStep === 1 && isStep2Valid(state.step2));

  return (
    <React.Fragment>
      <Toolbar>
        <IconButton edge="start" color="inherit" aria-label="back" onClick={() => navigate(`/tenants/`)} size="large">
          <ArrowBackIcon />
        </IconButton>
      </Toolbar>
      <Container maxWidth="lg">
        <Box sx={{ px: 4, py: 3 }}>
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              mb: 4,
              borderBottom: "2px solid",
              borderColor: "primary.main",
              pb: 2,
            }}
          >
            <DomainIcon sx={{ fontSize: 40, mr: 2, color: "primary.main" }} />
            <Typography variant="h4" component="h1" color="primary">
              Create Tenant
            </Typography>
          </Box>

          {!created && (
            <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
              {steps.map((label) => (
                <Step key={label}>
                  <StepLabel>{label}</StepLabel>
                </Step>
              ))}
            </Stepper>
          )}

          {created ? (
            <WizardSuccess
              tenantId={created.id}
              tenantName={created.tenantName}
              onViewDetails={() => navigate(`/tenants/${created.id}`)}
              onGoToApiKeys={() => navigate(`/tenants/${created.id}/api-keys`)}
              onCreateAnother={handleCreateAnother}
            />
          ) : (
            <Box>
              {activeStep === 0 && <Step1Form values={state.step1} onChange={(step1) => setState((s) => ({ ...s, step1 }))} />}
              {activeStep === 1 && <Step2Security values={state.step2} onChange={(step2) => setState((s) => ({ ...s, step2 }))} />}
              {activeStep === 2 && <Step3Confirm values={state} />}

              <Box sx={{ display: "flex", justifyContent: "space-between", mt: 4 }}>
                <Button onClick={activeStep === 0 ? () => navigate(`/tenants/`) : handleBack} disabled={loading}>
                  {activeStep === 0 ? "Cancel" : "Previous"}
                </Button>
                {activeStep < steps.length - 1 ? (
                  <Button variant="contained" onClick={handleNext} disabled={!canNext}>
                    Next
                  </Button>
                ) : (
                  <Button
                    variant="contained"
                    onClick={handleSubmit}
                    disabled={loading || !isStep2Valid(state.step2)}
                    startIcon={loading ? <CircularProgress size={16} /> : null}
                  >
                    Create Tenant
                  </Button>
                )}
              </Box>
            </Box>
          )}
        </Box>
      </Container>
    </React.Fragment>
  );
}
