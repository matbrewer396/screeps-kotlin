'use strict';

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _ = require('lodash'),
    utils = require('../../../utils'),
    driver = utils.getDriver(),
    C = driver.constants,
    movement = require('../movement');

module.exports = function (spawn, creep, scope) {
    const roomObjects = scope.roomObjects,
          roomTerrain = scope.roomTerrain,
          bulk = scope.bulk;


    var newX, newY, isOccupied, hostileOccupied;
    var checkObstacleFn = i => i.x == newX && i.y == newY && (_.contains(C.OBSTACLE_OBJECT_TYPES, i.type) || // just unwalkable
    i.type == 'constructionSite' && _.contains(C.OBSTACLE_OBJECT_TYPES, i.structureType) // unwalkable site
    );

    var directions = [1, 2, 3, 4, 5, 6, 7, 8];
    if (spawn.spawning && spawn.spawning.directions) {
        directions = spawn.spawning.directions;
    }
    const otherDirections = _.difference([1, 2, 3, 4, 5, 6, 7, 8], directions);
    // find the first direction where the creep can spawn
    for (var direction of directions) {
        var _utils$getOffsetsByDi = utils.getOffsetsByDirection(direction),
            _utils$getOffsetsByDi2 = _slicedToArray(_utils$getOffsetsByDi, 2),
            dx = _utils$getOffsetsByDi2[0],
            dy = _utils$getOffsetsByDi2[1];

        newX = spawn.x + dx;
        newY = spawn.y + dy;
        isOccupied = _.any(roomObjects, checkObstacleFn) || movement.isTileBusy(newX, newY) || utils.checkTerrain(roomTerrain, newX, newY, C.TERRAIN_MASK_WALL) && !_.any(roomObjects, { type: 'road', x: newX, y: newY });

        if (!isOccupied) {
            break;
        }

        // remember the first direction where we found a hostile creep
        if (!hostileOccupied) {
            hostileOccupied = _.find(roomObjects, i => i.x == newX && i.y == newY && i.type == 'creep' && i.user != spawn.user);
        }
    }

    // if we found a place to spawn, do so
    if (!isOccupied) {
        bulk.update(creep, {
            x: newX,
            y: newY,
            spawning: false
        });
        return true;
    }

    // spawn is surrounded, spawnstomp the first hostile we found above, unless...
    if (hostileOccupied) {
        // bail if there's an opening we could spawn to but chose not to
        for (var direction of otherDirections) {
            var _utils$getOffsetsByDi3 = utils.getOffsetsByDirection(direction),
                _utils$getOffsetsByDi4 = _slicedToArray(_utils$getOffsetsByDi3, 2),
                dx = _utils$getOffsetsByDi4[0],
                dy = _utils$getOffsetsByDi4[1];

            newX = spawn.x + dx;
            newY = spawn.y + dy;
            isOccupied = _.any(roomObjects, checkObstacleFn) || utils.checkTerrain(roomTerrain, newX, newY, C.TERRAIN_MASK_WALL) || movement.isTileBusy(newX, newY);

            if (!isOccupied) {
                return false;
            }
        }

        require('../creeps/_die')(hostileOccupied, undefined, true, scope);
        bulk.update(creep, {
            x: hostileOccupied.x,
            y: hostileOccupied.y,
            spawning: false
        });
        return true;
    }

    return false;
};
//# sourceMappingURL=../../../sourcemaps/processor/intents/spawns/_born-creep.js.map
