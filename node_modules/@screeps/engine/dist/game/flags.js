'use strict';

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var utils = require('./../utils'),
    rooms = require('./rooms'),
    driver = utils.getRuntimeDriver(),
    C = driver.constants,
    _ = require('lodash');

var runtimeData, intents, register, globals;

exports.make = function (_runtimeData, _intents, _register, _globals) {

    runtimeData = _runtimeData;
    intents = _intents;
    register = _register;
    globals = _globals;

    if (globals.Flag) {
        return;
    }

    var Flag = register.wrapFn(function (name, color, secondaryColor, roomName, x, y) {
        globals.RoomObject.call(this, Number(x), Number(y), roomName);

        this.name = name;
        this.color = Number(color);
        this.secondaryColor = Number(secondaryColor || color);
    });

    Flag.prototype = Object.create(globals.RoomObject.prototype);
    Flag.prototype.constructor = Flag;

    Object.defineProperty(Flag.prototype, 'memory', {
        get: function () {
            if (_.isUndefined(globals.Memory.flags) || globals.Memory.flags === 'undefined') {
                globals.Memory.flags = {};
            }
            if (!_.isObject(globals.Memory.flags)) {
                return undefined;
            }
            return globals.Memory.flags[this.name] = globals.Memory.flags[this.name] || {};
        },

        set: function (value) {
            if (_.isUndefined(globals.Memory.flags) || globals.Memory.flags === 'undefined') {
                globals.Memory.flags = {};
            }
            if (!_.isObject(globals.Memory.flags)) {
                throw new Error('Could not set flag memory');
            }
            globals.Memory.flags[this.name] = value;
        }
    });

    Flag.prototype.toString = register.wrapFn(function () {
        return `[flag ${this.name}]`;
    });

    Flag.prototype.remove = register.wrapFn(function () {

        intents.pushByName('room', 'removeFlag', { roomName: this.pos.roomName, name: this.name });
        return C.OK;
    });

    Flag.prototype.setPosition = register.wrapFn(function (firstArg, secondArg) {
        var _utils$fetchXYArgumen = utils.fetchXYArguments(firstArg, secondArg, globals),
            _utils$fetchXYArgumen2 = _slicedToArray(_utils$fetchXYArgumen, 3),
            x = _utils$fetchXYArgumen2[0],
            y = _utils$fetchXYArgumen2[1],
            roomName = _utils$fetchXYArgumen2[2];

        roomName = roomName || this.pos.roomName;
        if (_.isUndefined(x) || _.isUndefined(y)) {
            return C.ERR_INVALID_TARGET;
        }

        intents.pushByName('room', 'removeFlag', { roomName: this.pos.roomName, name: this.name });
        intents.pushByName('room', 'createFlag', { roomName, x, y, name: this.name, color: this.color, secondaryColor: this.secondaryColor });
        return C.OK;
    });

    Flag.prototype.setColor = register.wrapFn(function (color, secondaryColor) {

        if (!_.contains(C.COLORS_ALL, color)) {
            return C.ERR_INVALID_ARGS;
        }

        secondaryColor = secondaryColor || color;

        if (!_.contains(C.COLORS_ALL, secondaryColor)) {
            return C.ERR_INVALID_ARGS;
        }

        intents.pushByName('room', 'removeFlag', { roomName: this.pos.roomName, name: this.name });
        intents.pushByName('room', 'createFlag', { roomName: this.pos.roomName, x: this.pos.x, y: this.pos.y, name: this.name, color, secondaryColor });
        return C.OK;
    });

    Object.defineProperty(globals, 'Flag', {
        enumerable: true,
        value: Flag
    });
};
//# sourceMappingURL=../sourcemaps/game/flags.js.map
