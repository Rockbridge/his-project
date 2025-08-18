import React from "react";
import { apiRequest } from "../api";

export default function usePatientDetail(patientId) {
  const [detail, setDetail] = React.useState(null);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState(null);

  React.useEffect(() => {
    if (!patientId) return;
    const ctrl = new AbortController();
    setLoading(true);
    setError(null);

    apiRequest(`/patients/${patientId}`, { signal: ctrl.signal })
      .then((data) => setDetail(data || null))
      .catch((e) => {
        if (e.name !== "AbortError") setError(e);
      })
      .finally(() => setLoading(false));

    return () => ctrl.abort();
  }, [patientId]);

  return { detail, loading, error };
}
