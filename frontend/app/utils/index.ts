/**
 * Format a date string or Date object into a human-readable string.
 *
 * @param date   ISO string, timestamp, or Date instance
 * @param format Optional pattern — defaults to 'YYYY-MM-DD HH:mm:ss'.
 *               Supported tokens: YYYY, MM, DD, HH, mm, ss
 */
export function formatDate(date: string | Date, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
  const d = date instanceof Date ? date : new Date(date)

  if (isNaN(d.getTime())) {
    return ''
  }

  const pad = (n: number): string => String(n).padStart(2, '0')

  return format
    .replace('YYYY', String(d.getFullYear()))
    .replace('MM', pad(d.getMonth() + 1))
    .replace('DD', pad(d.getDate()))
    .replace('HH', pad(d.getHours()))
    .replace('mm', pad(d.getMinutes()))
    .replace('ss', pad(d.getSeconds()))
}

/**
 * Format a number as a currency string.
 *
 * @param amount  Numeric value (e.g. 1234.5)
 * @returns       Formatted string like "¥1,234.50"
 */
export function formatMoney(amount: number): string {
  return `¥${amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`
}

/**
 * Generate a simple unique ID (not cryptographically secure).
 * Combines timestamp + random hex for reasonable uniqueness.
 */
export function generateId(): string {
  const timestamp = Date.now().toString(36)
  const random = Math.random().toString(36).substring(2, 10)
  return `${timestamp}-${random}`
}

/**
 * Trigger a file download via a temporary anchor element.
 *
 * @param url      The file URL to download
 * @param filename The name the downloaded file should have
 */
export function downloadFile(url: string, filename: string): void {
  if (!import.meta.client) return

  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = filename
  anchor.style.display = 'none'
  document.body.appendChild(anchor)
  anchor.click()
  document.body.removeChild(anchor)
}
