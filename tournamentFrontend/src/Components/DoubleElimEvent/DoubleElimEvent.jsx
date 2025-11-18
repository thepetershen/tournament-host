import DoubleElimBracket from './DoubleElimBracket';

/**
 * DoubleElimEvent component - wrapper for the double elimination bracket
 *
 * @param {Object} draw - The draw object from the backend containing winners and losers brackets
 */
function DoubleElimEvent({ draw }) {
    return <DoubleElimBracket draw={draw} />;
}

export default DoubleElimEvent;
