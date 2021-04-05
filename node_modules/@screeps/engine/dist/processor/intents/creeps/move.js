'use strict';

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _ = require('lodash'),
    utils = require('../../../utils'),
    driver = utils.getDriver(),
    C = driver.constants,
    movement = require('../movement');

module.exports = function (object, intent, { roomObjects }) {

    if (object.spawning) {
        return;
    }

    object._oldFatigue = object.fatigue;

    var d = null;
    if (intent.direction) {
        d = utils.getOffsetsByDirection(intent.direction);
    }
    if (intent.id) {
        const creep = roomObjects[intent.id];
        if (creep && creep.type == 'creep' && utils.dist(object, creep) == 1) {
            d = [creep.x - object.x, creep.y - object.y];
        }
    }

    if (!d) {
        return;
    }

    var _d = d,
        _d2 = _slicedToArray(_d, 2),
        dx = _d2[0],
        dy = _d2[1];

    if (object.x + dx < 0 || object.x + dx > 49 || object.y + dy < 0 || object.y + dy > 49) {
        return;
    }

    var targetObjects = _.filter(roomObjects, { x: object.x + dx, y: object.y + dy });

    if (!_.any(targetObjects, target => _.contains(C.OBSTACLE_OBJECT_TYPES, target.type) && target.type != 'creep' && target.type != 'powerCreep' || target.type == 'rampart' && !target.isPublic && object.user != target.user || object.type == 'powerCreep' && target.type == 'portal' && target.destination.shard)) {

        movement.add(object, dx, dy);
    }
};
//# sourceMappingURL=../../../sourcemaps/processor/intents/creeps/move.js.map
