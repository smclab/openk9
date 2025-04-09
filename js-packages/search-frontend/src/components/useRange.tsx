import { createContext, useContext, useState, ReactNode } from "react";
import React from "react";

interface RangeContextType {
  range: [number, number];
  setRange: (range: [number, number]) => void;
  numberOfResults: number;
  setNumberOfResults: (num: number) => void;
}

const RangeContext = createContext<RangeContextType | undefined>(undefined);

interface RangeProviderProps {
  children: ReactNode;
}

export const RangeProvider: React.FC<RangeProviderProps> = ({ children }) => {
  const [range, setRange] = useState<[number, number]>([0, 0]);
  const [numberOfResults, setNumberOfResults] = React.useState<number>(0);

  return (
    <RangeContext.Provider
      value={{ range, setRange, numberOfResults, setNumberOfResults }}
    >
      {children}
    </RangeContext.Provider>
  );
};

export const useRange = () => {
  const context = useContext(RangeContext);
  if (!context) {
    throw new Error("useRange must be used within a RangeProvider");
  }
  return context;
};
