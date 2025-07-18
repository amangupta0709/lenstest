export default function parsedDate(date) {
    return new Date(date).toLocaleString('en-US', {
          month: 'short',
          day: '2-digit',
          year: 'numeric',
          hour: 'numeric',
          minute: '2-digit',
          hour12: true
        }).replace(/,/g, '')
}