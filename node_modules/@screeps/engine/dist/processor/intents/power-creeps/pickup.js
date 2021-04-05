'use strict';

var _ = require('lodash'),
    utils = require('../../../utils'),
    driver = utils.getDriver(),
    C = driver.constants;

module.exports = function (object, intent, scope) {

    require('../creeps/pickup')(object, intent, scope);
};
//# sourceMappingURL=../../../sourcemaps/processor/intents/power-creeps/pickup.js.map
