var q = require('q');
var _ = require('lodash');

module.exports = function(fn) {

	return function(request, response, next) {
		q.when({})

			.then(function() {
				return fn(request, response);
			})
			.then(function(data) {
				response.json(_.extend({ok: 1}, data));
				next();
			})
			.catch(function(error) {
				response.json({error: "" + error});
				if(error.stack) {
					console.error(error.stack);
				}
				next();
			});
    }
};