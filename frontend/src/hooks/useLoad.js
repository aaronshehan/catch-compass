import { useEffect, useState } from 'react';

/**
 * Loads data once and reports loading, error and success as three states.
 *
 * <p>The cancelled flag stops state being set after the component unmounts.
 * React's StrictMode mounts every component twice in development specifically
 * to make this class of bug visible, so it is worth doing properly rather than
 * wondering later why a request fires twice.
 */
export function useLoad(loader, deps = []) {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    loader()
      .then((result) => {
        if (!cancelled) setData(result);
      })
      .catch((e) => {
        if (!cancelled) setError(e.message);
      });

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  return { data, error, loading: data === null && error === null };
}
