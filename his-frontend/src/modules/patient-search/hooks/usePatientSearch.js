import React from "react";
import { apiRequest, PAGE_SIZE } from "../api";


function useDebounced(value, delay = 350) {
  const [debounced, setDebounced] = React.useState(value);
  React.useEffect(() => {
    const t = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(t);
  }, [value, delay]);
  return debounced;
}

export default function usePatientSearch({ query, page /* sort */ }) {
  const debouncedQuery = useDebounced(query);
  const [rows, setRows] = React.useState([]);
  const [total, setTotal] = React.useState(0);
  const [pageCount, setPageCount] = React.useState(0);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState(null);

  React.useEffect(() => {
    const ctrl = new AbortController();
    setLoading(true);
    setError(null);

    const q = (debouncedQuery ?? "").trim();

    apiRequest(
      `/patients/search?searchTerm=${encodeURIComponent(q)}&page=${page}&size=${PAGE_SIZE}`,
      { signal: ctrl.signal }
    )
      .then((data) => {
        setRows(data.content || []);
        setTotal(
          typeof data.totalElements === "number" ? data.totalElements : 0
        );
        setPageCount(typeof data.totalPages === "number" ? data.totalPages : 0);
      })
      .catch((e) => {
        if (e.name !== "AbortError") setError(e);
      })
      .finally(() => setLoading(false));

    return () => ctrl.abort();
  }, [debouncedQuery, page /* sort (Stub) */]);

  return { rows, total, pageCount, loading, error };
}
