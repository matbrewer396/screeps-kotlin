'use strict';

var _ = require('lodash'),
    utils = require('../../../utils'),
    driver = utils.getDriver(),
    C = driver.constants;

module.exports = function (object, intent, { bulk }) {

    bulk.update(object, {
        isPublic: !!intent.isPublic
    });
};
//# sourceMappingURL=../../../sourcemaps/processor/intents/ramparts/set-public.js.map
