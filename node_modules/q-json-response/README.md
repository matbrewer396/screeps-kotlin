# q-json-response

Usage:

```javascript
var router = require('express').Router();
var jsonResponse = require('q-json-response');
var q = require('q');

router.get('/user', jsonResponse(function(request, response) {
	return Users.getUser(request.query.userId)
		.then(function(user) {
			if(!user) {
				return q.reject('invalid user');
			}
			return user;
		});		
}));
```