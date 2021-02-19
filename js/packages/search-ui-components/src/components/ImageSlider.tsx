import React, { useState } from "react";
import ClayIcon from "@clayui/icon";
import ClayModal, { useModal } from "@clayui/modal";
import { createUseStyles } from "react-jss";
import { ThemeType } from "../theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  imgPreview: {
    display: "block",
    width: "100%",
  },
  buttons: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    "& button": {
      margin: theme.spacingUnit,
    },
  },
}));

function Modal({
  visible,
  setVisible,
  urls,
  currentPreviewI,
  setCurrentPreviewI,
}: {
  visible: boolean;
  setVisible(v: boolean): void;
  urls: string[];
  currentPreviewI: number;
  setCurrentPreviewI(f: (n: number) => number): void;
}) {
  const { observer } = useModal({
    onClose: () => setVisible(false),
  });

  return visible ? (
    <ClayModal observer={observer} size="lg">
      <ClayModal.Header>
        <ClayIcon symbol="document-text" /> Documento
      </ClayModal.Header>
      <ClayModal.Body>
        <Inner
          currentPreviewI={currentPreviewI}
          setCurrentPreviewI={setCurrentPreviewI}
          urls={urls}
        />
      </ClayModal.Body>
    </ClayModal>
  ) : null;
}

function Inner({
  urls,
  currentPreviewI,
  setCurrentPreviewI,
  onOpenModal,
}: {
  urls: string[];
  currentPreviewI: number;
  setCurrentPreviewI(f: (n: number) => number): void;
  onOpenModal?: () => void;
}) {
  const classes = useStyles();
  return (
    <>
      <img
        src={urls[currentPreviewI]}
        className={classes.imgPreview}
        onClick={onOpenModal}
      />
      <div className={classes.buttons}>
        <button
          disabled={currentPreviewI === 0}
          className="btn btn-secondary btn-sm"
          onClick={() => setCurrentPreviewI((i) => Math.max(0, i - 1))}
        >
          <ClayIcon symbol="angle-left" />
        </button>
        <button
          disabled={currentPreviewI === urls.length - 1}
          className="btn btn-secondary btn-sm"
          onClick={() =>
            setCurrentPreviewI((i) => Math.min(urls.length - 1, i + 1))
          }
        >
          <ClayIcon symbol="angle-right" />
        </button>
      </div>
    </>
  );
}

export function ImageSlider({
  urls,
  ...rest
}: { urls: string[] } & React.HTMLAttributes<HTMLDivElement>) {
  const [currentPreviewI, setCurrentPreviewI] = useState(0);
  const [visible, setVisible] = useState(false);

  return (
    <div {...rest}>
      <Modal
        visible={visible}
        setVisible={setVisible}
        currentPreviewI={currentPreviewI}
        setCurrentPreviewI={setCurrentPreviewI}
        urls={urls}
      />
      <Inner
        onOpenModal={() => setVisible(true)}
        currentPreviewI={currentPreviewI}
        setCurrentPreviewI={setCurrentPreviewI}
        urls={urls}
      />
    </div>
  );
}
