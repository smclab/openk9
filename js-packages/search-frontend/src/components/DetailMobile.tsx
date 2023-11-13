import React from "react";
import { css } from "styled-components/macro";
import { GenericResultItem, DetailRendererProps } from "./client";
import { useRenderers } from "./useRenderers";
import "overlayscrollbars/css/OverlayScrollbars.css";
import { DetailMemo } from "./Detail";
import { ModalDetail } from "./ModalDetail";

export type DetailMobileProps<E> = {
  result: GenericResultItem<E> | null;
  setDetailMobile: any;
  onClose(): void; 
};
function DetailMobile<E>(props: DetailMobileProps<E>) {
  const result = props.result as any;
  const setDetailMobile = props.setDetailMobile as any;
  const action=props.onClose ;
  const renderers = useRenderers();
    const modalRef = React.useRef(null);
    const [isOpen,setIsOpen]=React.useState(true)
  
    React.useEffect(() => {
      const modalElement = modalRef.current as any;

      if (true && modalElement) {

        const focusableElements = modalElement?.querySelectorAll(
          'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];
  
        const handleTabKeyPress = (event:any) => {
          if (event.key === "Tab") {
            if (event.shiftKey && document.activeElement === firstElement) {
              event.preventDefault();
              lastElement.focus();
            } else if (
              !event.shiftKey &&
              document.activeElement === lastElement
            ) {
              event.preventDefault();
              firstElement.focus();
            }
          }
        };
  
        const handleEscapeKeyPress = (event:any) => {
          if (event.key === "Escape") {
            setIsOpen(false)
          }
        };
  
        modalElement?.addEventListener("keydown", handleTabKeyPress);
        modalElement?.addEventListener("keydown", handleEscapeKeyPress);
  
        return () => {
          modalElement?.removeEventListener("keydown", handleTabKeyPress);
          modalElement?.removeEventListener("keydown", handleEscapeKeyPress);
        };
      }
    }, [isOpen,setIsOpen,result]);
  
  const componet = (
    <div ref={modalRef} style={{height:"100%"}}>
    <DetailMemo result={result} setDetailMobile={setDetailMobile} isMobile={true} actionOnCLose={action} />
    </div>
  );

  if (!result) {
    document.body.style.overflow = "auto";
    return null;
  }
  document.body.style.overflow = "hidden";

  return <ModalDetail content={componet} />;
}
export const DetailMobileMemo = React.memo(DetailMobile);
