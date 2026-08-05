const parseAlertMessage = (message) => {
  if (typeof message !== 'string' || !message.trim()) {
    return { score: null, reasons: [] };
  }

  const trimmedMessage = message.trim();
  const scoreMatch = trimmedMessage.match(/score\s*[:=]\s*(\d+)/i);
  const reasonsMatch = trimmedMessage.match(/reasons\s*[:=]\s*(.+)$/i);

  if (!scoreMatch && !reasonsMatch) {
    return { score: null, reasons: [] };
  }

  const score = scoreMatch ? Number(scoreMatch[1]) : null;
  const reasonsText = reasonsMatch ? reasonsMatch[1].trim() : '';
  const reasons = reasonsText
    ? reasonsText
        .split(/;|,|\|/)
        .map((reason) => reason.trim())
        .filter(Boolean)
    : [];

  return { score, reasons };
};

const normalizeAlertItem = (item) => {
  if (!item || typeof item !== 'object') {
    return item;
  }

  const parsed = parseAlertMessage(item.message);
  const normalizedReasons = Array.isArray(item.reasons)
    ? item.reasons.filter(Boolean).map((reason) => String(reason).trim())
    : parsed.reasons;

  return {
    ...item,
    ...(parsed.score != null ? { score: parsed.score } : {}),
    ...(normalizedReasons.length > 0 ? { reasons: normalizedReasons } : {}),
  };
};

export const normalizeAlertsResponse = (payload) => {
  if (Array.isArray(payload)) return payload.map(normalizeAlertItem);
  if (!payload || typeof payload !== 'object') return [];

  if (Array.isArray(payload.value)) return payload.value.map(normalizeAlertItem);
  if (Array.isArray(payload.items)) return payload.items.map(normalizeAlertItem);
  if (Array.isArray(payload.alerts)) return payload.alerts.map(normalizeAlertItem);

  return payload.id || payload.title || payload.message || payload.status || payload.severity
    ? [normalizeAlertItem(payload)]
    : [];
};

export const normalizeAlertSummary = (payload) => {
  if (!payload || typeof payload !== 'object') {
    return { total: 0, newCount: 0, readCount: 0, resolvedCount: 0 };
  }

  return {
    total: Number(payload.total ?? payload.count ?? 0),
    newCount: Number(payload.newCount ?? payload.new ?? 0),
    readCount: Number(payload.readCount ?? payload.read ?? 0),
    resolvedCount: Number(payload.resolvedCount ?? payload.resolved ?? 0),
  };
};
