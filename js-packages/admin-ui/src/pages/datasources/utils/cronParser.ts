/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
 * @throws Error se la stringa cron non Ã¨ nel formato corretto
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
 * Verifica se una stringa cron Ã¨ valida
 * @param cronString - Stringa cron da validare
 * @returns true se la stringa Ã¨ valida, false altrimenti
 */
export function isValidCronExpression(cronString: string): boolean {
  try {
    parseCronExpression(cronString);
    return true;
  } catch {
    return false;
  }
} 
