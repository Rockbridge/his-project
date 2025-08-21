import React from "react";
import { apiRequest } from "../api";

export default function usePatientEncounters(patientId) {
  const [encounters, setEncounters] = React.useState([]);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState(null);

  React.useEffect(() => {
    if (!patientId) return;
    const ctrl = new AbortController();
    setLoading(true);
    setError(null);

    apiRequest(`/encounters/patient/${patientId}?page=0&size=50`, {
      signal: ctrl.signal,
    })
      .then((data) => {
        const list = Array.isArray(data.content) ? data.content : [];
        list.sort(
          (a, b) => new Date(a.encounterDate) - new Date(b.encounterDate)
        );
        setEncounters(list);
      })
      .catch((e) => {
        if (e.name !== "AbortError") setError(e);
      })
      .finally(() => setLoading(false));

    return () => ctrl.abort();
  }, [patientId]);

  return { encounters, loading, error };
}
