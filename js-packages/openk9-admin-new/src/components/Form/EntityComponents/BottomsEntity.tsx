import { Button } from "@mui/material";

export function BottomsEntity({
  controll,
  actionSave,
  actionBack,
  submit,
}: {
  controll: boolean;
  actionSave(): void;
  actionBack(): void;
  submit: boolean;
}) {
  return (
    <div
      style={{
        display: "flex",
        flexWrap: "wrap",
        justifyContent: "space-between",
      }}
    >
      <Button
        className="btn btn-secondary"
        variant="contained"
        type="button"
        onClick={() => {
          actionBack();
        }}
      >
        BACK
      </Button>
      {submit && (
        <Button
          className="btn btn-danger"
          variant="contained"
          type="button"
          disabled={!controll}
          onClick={() => {
            actionSave();
          }}
        >
          {"SAVE AND CONTINUE"}
        </Button>
      )}
    </div>
  );
}