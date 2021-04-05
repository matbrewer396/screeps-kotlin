'use strict';

var _ = require('lodash'),
    utils = require('../../../utils'),
    driver = utils.getDriver(),
    C = driver.constants;

module.exports = function (object, intent, scope) {
    require('../creeps/withdraw')(object, intent, scope);
};
//# sourceMappingURL=../../../sourcemaps/processor/intents/power-creeps/withdraw.js.map
