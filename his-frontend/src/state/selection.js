import React from "react";

const SelectionContext = React.createContext(null);

export function SelectionProvider({ children }) {
  const [selectedPatientId, setSelectedPatientId] = React.useState(null);
  const [selectedEncounterId, setSelectedEncounterId] = React.useState(null);

  const value = {
    selectedPatientId,
    setSelectedPatientId,
    selectedEncounterId,
    setSelectedEncounterId,
  };

  return (
    <SelectionContext.Provider value={value}>{children}</SelectionContext.Provider>
  );
}

export function useSelection() {
  const ctx = React.useContext(SelectionContext);
  if (!ctx) {
    throw new Error("useSelection must be used within SelectionProvider");
  }
  return ctx;
}
