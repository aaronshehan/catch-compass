/*
 * Display helpers.
 *
 * Note this is a genuine improvement on the server-rendered version: the
 * backend formatted timestamps in the *server's* timezone, because it had no
 * way to know the reader's. In the browser we do, so times now render in
 * whatever zone the phone is actually in.
 */

const dateTimeFormat = new Intl.DateTimeFormat(undefined, {
  day: 'numeric',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
});

export function dateTime(isoString) {
  if (!isoString) return 'Not recorded';
  return dateTimeFormat.format(new Date(isoString));
}

export function measurement(value, unit) {
  if (value === null || value === undefined) return 'Not recorded';
  return `${value} ${unit}`;
}

export function orNotRecorded(value) {
  if (value === null || value === undefined || value === '') return 'Not recorded';
  return value;
}

/** Turns SOFT_PLASTIC into "Soft plastic" for display. */
export function humanise(enumValue) {
  if (!enumValue) return null;
  const lower = enumValue.replace(/_/g, ' ').toLowerCase();
  return lower.charAt(0).toUpperCase() + lower.slice(1);
}
