/**
 * Generates one of the colors randomly
 * @returns {string}
 */
const getRandomColor = () => {
    const letters = [
        '#ff9908',
        '#FFBA00',
        '#DD5F5F',
        '#06A3CB',
        '#8ABDB0',
        '#9EC41C'
    ];
    return (letters[Math.floor(Math.random() * 6)]);
}

export {getRandomColor}