import React from "react";

interface CustomFormGroupProps extends React.HTMLAttributes<HTMLDivElement> {
  className?: string;
}

export const CustomFormGroup: React.FC<CustomFormGroupProps> = (props) => {
  const { children, className, ...rest } = props;

  const combinedClassName = `${className || ""}`;

  return (
    <div {...rest} className={combinedClassName}>
      {children}
    </div>
  );
};
