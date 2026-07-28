// I locale di moment (`moment/locale/it`, `/de`, `/es`, `/fr`, …) sono file
// JS senza dichiarazioni di tipo e vengono importati solo per side-effect
// (registrano il locale nel singleton di moment). In un monorepo con
// dipendenze hoistate il TS server dell'IDE talvolta non ne risolve il path e
// segnala TS2307. Questa dichiarazione ambient li rende sempre validi per
// TypeScript senza incidere sul runtime (il bundler li carica normalmente).
declare module "moment/locale/*";
