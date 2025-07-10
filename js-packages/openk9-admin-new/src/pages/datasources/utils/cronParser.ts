interface CronComponents {
  minute: string;
  hour: string;
  dayOfMonth: string;
  month: string;
  dayOfWeek: string;
}

/**
 * Converte una stringa cron nei suoi componenti
 * @param cronString - Stringa cron nel formato "* * * * *" (minute hour dayOfMonth month dayOfWeek)
 * @returns Oggetto con i componenti della stringa cron separati
 * @throws Error se la stringa cron non è nel formato corretto
 */
export function parseCronExpression(cronString: string): CronComponents {
  // Rimuove eventuali secondi dalla stringa cron (alcuni formati includono i secondi all'inizio)
  const normalizedCron = cronString.trim().split(' ').slice(-5).join(' ');
  
  const parts = normalizedCron.split(' ');
  
  if (parts.length !== 5) {
    throw new Error('Formato cron non valido. Deve essere nel formato "* * * * *" (minute hour dayOfMonth month dayOfWeek)');
  }

  const [minute, hour, dayOfMonth, month, dayOfWeek] = parts;

  return {
    minute,
    hour,
    dayOfMonth,
    month,
    dayOfWeek
  };
}

/**
 * Verifica se una stringa cron è valida
 * @param cronString - Stringa cron da validare
 * @returns true se la stringa è valida, false altrimenti
 */
export function isValidCronExpression(cronString: string): boolean {
  try {
    parseCronExpression(cronString);
    return true;
  } catch {
    return false;
  }
} 