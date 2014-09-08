var express = require('express');
var router = express.Router();

var logic = require('../logic/logic');

/* GET */
router.get('/', function(req, res) {
  res.render('index', { title: 'Elevator logic', message: "I'm a little JavaScript elevator. Please POST state here to get where I want to go." });
});

/* POST */
router.post('/', function(req, res) {
  var state = req.body;
  res.send({"go-to": logic.decideWhichFloorToGo(state)});
});


module.exports = router;
