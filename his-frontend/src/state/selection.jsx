import { createContext, useContext, useState, useMemo } from "react";

const SelectionContext = createContext();

export default function SelectionProvider({ children }) {
  const [selection, setSelection] = useState(null);
  const value = useMemo(() => ({ selection, setSelection }), [selection]);
  return (
    <SelectionContext.Provider value={value}>{children}</SelectionContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useSelection() {
  return useContext(SelectionContext);
}
