import test from 'node:test';
import assert from 'node:assert/strict';
import { normalizeAlertsResponse, normalizeAlertSummary } from './alertResponse.js';

test('normalizeAlertsResponse converts a single alert object into an array', () => {
  const input = { id: 1, title: 'Test', status: 'NEW' };
  assert.deepEqual(normalizeAlertsResponse(input), [{ id: 1, title: 'Test', status: 'NEW' }]);
});

test('normalizeAlertsResponse handles wrapped payloads like value or items', () => {
  const input = { value: [{ id: 2, title: 'Wrapped' }] };
  assert.deepEqual(normalizeAlertsResponse(input), [{ id: 2, title: 'Wrapped' }]);
});

test('normalizeAlertsResponse parses score and reasons from alert messages', () => {
  const input = {
    id: 1,
    title: 'Risque élevé',
    message: 'Score: 88 | Reasons: ancienneté faible; salaire bas',
  };

  assert.deepEqual(normalizeAlertsResponse(input), [{
    id: 1,
    title: 'Risque élevé',
    message: 'Score: 88 | Reasons: ancienneté faible; salaire bas',
    score: 88,
    reasons: ['ancienneté faible', 'salaire bas'],
  }]);
});

test('normalizeAlertSummary maps backend keys to the UI shape', () => {
  const input = { total: 3, newCount: 1, readCount: 1, resolvedCount: 1 };
  assert.deepEqual(normalizeAlertSummary(input), {
    total: 3,
    newCount: 1,
    readCount: 1,
    resolvedCount: 1,
  });
});
