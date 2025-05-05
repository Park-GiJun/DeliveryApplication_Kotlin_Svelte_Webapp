/**
 * 날짜 형식화 함수
 */
export function formatDate(date) {
  if (!date) return '';
  
  if (typeof date === 'string') {
    date = new Date(date);
  }
  
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  const seconds = date.getSeconds().toString().padStart(2, '0');
  
  return `${hours}:${minutes}:${seconds}`;
}

/**
 * API 기본 URL 반환
 */
export function getApiBaseUrl() {
  return 'http://localhost:52001';
}

/**
 * WebSocket 기본 URL 반환
 */
export function getWsBaseUrl() {
  return 'ws://localhost:52001';
}
