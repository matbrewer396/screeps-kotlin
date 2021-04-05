'use strict';

var _ = require('lodash'),
    utils = require('../../../utils'),
    driver = utils.getDriver(),
    C = driver.constants;

module.exports = function (object, intent, {}) {

    if (!_.isString(intent.message)) {
        return;
    }

    object.actionLog.say = {
        message: intent.message.substring(0, 10),
        isPublic: intent.isPublic
    };
};
//# sourceMappingURL=../../../sourcemaps/processor/intents/power-creeps/say.js.map
