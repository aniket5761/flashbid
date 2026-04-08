export function formatDateTime(value) {
  if (!value) {
    return "Not available";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "Not available";
  }

  return new Intl.DateTimeFormat("en-US", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(date);
}

export function formatCurrency(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0
  }).format(value ?? 0);
}

export function classNames(...values) {
  return values.filter(Boolean).join(" ");
}

export function ensureArray(value) {
  return Array.isArray(value) ? value : [];
}

export function normalizePage(value) {
  if (!value || typeof value !== "object") {
    return {
      content: [],
      number: 0,
      totalPages: 0,
      totalElements: 0
    };
  }

  if (Array.isArray(value)) {
    return {
      content: value,
      number: 0,
      totalPages: value.length ? 1 : 0,
      totalElements: value.length
    };
  }

  const content = ensureArray(value.content);
  const totalElements = Number.isFinite(value.totalElements) ? value.totalElements : content.length;
  const totalPages = Number.isFinite(value.totalPages) ? value.totalPages : (content.length ? 1 : 0);
  const number = Number.isFinite(value.number) ? value.number : 0;

  return {
    ...value,
    content,
    number,
    totalPages,
    totalElements
  };
}
