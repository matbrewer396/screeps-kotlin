'use strict';

const _ = require('lodash'),
      utils = require('../../../utils'),
      driver = utils.getDriver(),
      C = driver.constants;

module.exports = function (object, scope) {
    const roomObjects = scope.roomObjects,
          bulk = scope.bulk,
          gameTime = scope.gameTime;


    if (!object.decayTime || gameTime >= object.decayTime - 1) {
        if (object.store) {
            _.forEach(object.store, (amount, resourceType) => {
                require('../_create-energy')(object.x, object.y, object.room, amount, resourceType, scope);
            });
        }

        bulk.remove(object._id);
        delete roomObjects[object._id];
    }
};
//# sourceMappingURL=../../../sourcemaps/processor/intents/ruins/tick.js.map
