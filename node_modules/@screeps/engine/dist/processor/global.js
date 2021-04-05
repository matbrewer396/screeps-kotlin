'use strict';

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var q = require('q'),
    _ = require('lodash'),
    utils = require('../utils'),
    driver = utils.getDriver(),
    C = driver.constants,
    marketProcessor = require('./global-intents/market'),
    powerProcessor = require('./global-intents/power');

module.exports = () => {

    return driver.getInterRoom().then(data => {
        if (!data) {
            return;
        }

        var _data = _slicedToArray(data, 5),
            gameTime = _data[0],
            creeps = _data[1],
            accessibleRooms = _data[2],
            roomObjects = _data[3],
            _data$ = _data[4];

        _data$ = _data$ === undefined ? {} : _data$;
        var orders = _data$.orders,
            users = _data$.users,
            userPowerCreeps = _data$.userPowerCreeps,
            userIntents = _data$.userIntents,
            shardName = _data$.shardName;


        var bulkObjects = driver.bulkObjectsWrite(),
            bulkRooms = driver.bulkRoomsWrite(),
            bulkUsers = driver.bulkUsersWrite(),
            bulkTransactions = driver.bulkTransactionsWrite(),
            bulkUsersMoney = driver.bulkUsersMoney(),
            bulkUsersResources = driver.bulkUsersResources(),
            bulkUsersPowerCreeps = driver.bulkUsersPowerCreeps(),
            bulkMarketOrders = driver.bulkMarketOrders(),
            bulkMarketIntershardOrders = driver.bulkMarketIntershardOrders(),
            activateRooms = {},
            usersById = _.indexBy(users, '_id'),
            roomObjectsByType = _.groupBy(roomObjects, 'type');

        // creeps

        creeps.forEach(creep => {
            if (!accessibleRooms[creep.interRoom.room]) {
                return;
            }
            if (!activateRooms[creep.interRoom.room]) {
                driver.activateRoom(creep.interRoom.room);
            }
            activateRooms[creep.interRoom.room] = true;

            bulkObjects.update(creep, { room: creep.interRoom.room, x: creep.interRoom.x, y: creep.interRoom.y, interRoom: null });
        });

        powerProcessor({ userIntents, usersById, roomObjectsByType, userPowerCreeps, gameTime,
            bulkObjects, bulkUsers, bulkUsersPowerCreeps, shardName });

        marketProcessor({ orders, userIntents, usersById, gameTime, roomObjectsByType, bulkObjects, bulkUsers, bulkTransactions,
            bulkUsersMoney, bulkUsersResources, bulkMarketOrders, bulkMarketIntershardOrders });

        return q.all([bulkObjects.execute(), bulkRooms.execute(), bulkUsers.execute(), bulkMarketOrders.execute(), bulkMarketIntershardOrders.execute(), bulkUsersMoney.execute(), bulkTransactions.execute(), bulkUsersResources.execute(), bulkUsersPowerCreeps.execute(), driver.clearGlobalIntents()]);
    });
};
//# sourceMappingURL=../sourcemaps/processor/global.js.map
