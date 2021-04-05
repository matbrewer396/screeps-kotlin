'use strict';

module.exports = function (object, objectIntents, scope) {

    if (objectIntents.processPower) require('./process-power')(object, objectIntents.processPower, scope);
};
//# sourceMappingURL=../../../sourcemaps/processor/intents/power-spawns/intents.js.map
