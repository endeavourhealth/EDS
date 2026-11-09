export const DateTimeFormatter = {
  formatHHMMSS(date) {
    if (!date) return ''
    const d = new Date(date)
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
  },

  formatYYYYMMDDHHMM(date) {
    if (!date) return ''
    const d = new Date(date)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  },

  formatYYYYMMDDHHMMSS(date) {
    if (!date) return ''
    const d = new Date(date)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${this.formatHHMMSS(d)}`
  },

  getDateDiffSimpleFromNow(timestamp) {
    if (!timestamp) return ''
    const diff = Date.now() - new Date(timestamp).getTime()
    const mins = Math.floor(diff / 60000)
    if (mins < 1) return '(just now)'
    if (mins < 60) return `(${mins}m ago)`
    const hrs = Math.floor(mins / 60)
    return `(${hrs}h ${mins % 60}m ago)`
  },

  getDateDiffDesc(from, to, precision) {
    if (!from || !to) return ''
    const diffMs = new Date(to).getTime() - new Date(from).getTime()
    const absDiff = Math.abs(diffMs)

    const days = Math.floor(absDiff / (1000 * 60 * 60 * 24))
    const hours = Math.floor((absDiff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    const minutes = Math.floor((absDiff % (1000 * 60)) / (1000 * 60))

    if (precision === 1) {
      if (days > 0) return `${days}d ${hours}h`
      if (hours > 0) return `${hours}h ${minutes}m`
      return `${minutes}m`
    } else if (precision === 2) {
      if (days > 0) return `${days}d ${hours}h ${minutes}m`
      if (hours > 0) return `${hours}h ${minutes}m`
      return `${minutes}m`
    }

    const secs = Math.floor(absDiff / 1000)
    if (secs < 60) return `${secs}s`
    if (secs < 3600) return `${Math.floor(secs / 60)}m`
    return `${Math.floor(secs / 3600)}h ${Math.floor((secs % 3600) / 60)}m`
  },

  getDateDiffDescMs(earlier, later, numToks) {
    const from = new Date(earlier)
    const to = new Date(later)
    return this.getDateDiffDesc(from, to, numToks)
  }
}
