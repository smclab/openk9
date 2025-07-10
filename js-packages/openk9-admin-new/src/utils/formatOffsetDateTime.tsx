  export function formatOffsetDateTime(offsetDateTime: string): string {
    const date = new Date(offsetDateTime);
    const timeZoneOffset = date.getTimezoneOffset();
  
    // Calcoli ora e minuti in formato ±hh:mm
    const hoursOffset = Math.floor(Math.abs(timeZoneOffset) / 60);
    const minutesOffset = Math.abs(timeZoneOffset) % 60;
    const sign = timeZoneOffset > 0 ? '-' : '+';
    const formattedOffset = `${sign}${String(hoursOffset).padStart(2, '0')}:${String(minutesOffset).padStart(2, '0')}`;
  
    //formato YYYY-MM-DDTHH:mm:ss
    const formattedDate = date.toISOString().slice(0, 19); // Esclude i millisecondi e "Z"
    
    return `${formattedDate}${formattedOffset}`;
  }
  
  