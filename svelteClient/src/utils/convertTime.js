/**
 * Returns the time passed
 * @param date
 * @returns {string} time passed since now
 * @constructor
 */
export const convertTime = (date) => {
    let calc = new Date(Date.now()) - Date.parse(date + ' UTC');
    const daysOld = Math.floor(calc / 86400000);
    const hoursOld = Math.floor(calc / 3600000);
    const minutesOld = Math.floor(calc / 60000);
    let time = "";
    if (daysOld === 0) {
        if (hoursOld === 0) {
            if (minutesOld === 0) {
                time = "0m";
            } else {
                time = minutesOld + "m";
            }
        } else {
            time = hoursOld + "h";
        }
    } else {
        time = daysOld + "d";
    }
    return time;

};

/**
 * Calculates left time for survey
 * @param date
 * @returns {string}
 */
export const timeDiff = (date) => {
    let calc = Date.parse(date + ' UTC')+86400000 - new Date(Date.now());
    const daysOld = Math.floor(calc / 86400000);
    const hoursOld = Math.floor(calc / 3600000);
    const minutesOld = Math.floor(calc / 60000);
    let time = "";
    if (daysOld === 0) {
        if (hoursOld === 0) {
            if (minutesOld === 0) {
                time = "Noch 0m";
            } else {
                time = "Noch " + minutesOld + "m";
            }
        } else {
            time = "Noch " + hoursOld + "h";
        }
    } else {
        time = "Abgelaufen";
    }
    return time;

};