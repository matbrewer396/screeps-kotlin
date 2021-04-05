'use strict';

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _ = require('lodash'),
    utils = require('../../../utils'),
    driver = utils.getDriver(),
    C = driver.constants,
    movement = require('../movement');

module.exports = function (object, scope) {
    const roomObjects = scope.roomObjects,
          bulk = scope.bulk,
          roomController = scope.roomController,
          gameTime = scope.gameTime,
          eventLog = scope.eventLog;


    if (!object || object.type != 'powerCreep') return;

    movement.execute(object, scope);

    if (utils.isAtEdge(object) && object.user != '2' && object.user != '3') {
        var _utils$roomNameToXY = utils.roomNameToXY(object.room),
            _utils$roomNameToXY2 = _slicedToArray(_utils$roomNameToXY, 2),
            roomX = _utils$roomNameToXY2[0],
            roomY = _utils$roomNameToXY2[1],
            x = object.x,
            y = object.y,
            room = object.room;

        if (object.x == 0) {
            x = 49;
            room = utils.getRoomNameFromXY(roomX - 1, roomY);
        } else if (object.y == 0) {
            y = 49;
            room = utils.getRoomNameFromXY(roomX, roomY - 1);
        } else if (object.x == 49) {
            x = 0;
            room = utils.getRoomNameFromXY(roomX + 1, roomY);
        } else if (object.y == 49) {
            y = 0;
            room = utils.getRoomNameFromXY(roomX, roomY + 1);
        }

        bulk.update(object, { interRoom: { room, x, y } });

        eventLog.push({ event: C.EVENT_EXIT, objectId: object._id, data: { room, x, y } });
    }

    var portal = _.find(roomObjects, i => i.type == 'portal' && i.x == object.x && i.y == object.y);
    if (portal && !portal.destination.shard) {
        bulk.update(object, { interRoom: portal.destination });
    }

    let hits = object.hits;

    if (object._damageToApply) {
        hits -= object._damageToApply;
        delete object._damageToApply;
    }

    if (object._healToApply) {
        hits += object._healToApply;
        delete object._healToApply;
    }

    if (hits > object.hitsMax) {
        hits = object.hitsMax;
    }

    if (hits != object.hits) {
        bulk.update(object, { hits });
    }

    if (!_.isEqual(object.actionLog, object._actionLog)) {
        bulk.update(object, { actionLog: object.actionLog });
    }
};
//# sourceMappingURL=../../../sourcemaps/processor/intents/power-creeps/tick.js.map
