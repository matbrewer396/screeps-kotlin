'use strict';

var _ = require('lodash'),
    utils = require('../../../utils'),
    driver = utils.getDriver(),
    C = driver.constants;

module.exports = function (object, intent, scope) {

    require('../creeps/transfer')(object, intent, scope);
};
//# sourceMappingURL=../../../sourcemaps/processor/intents/power-creeps/transfer.js.map
