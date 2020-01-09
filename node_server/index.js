var express = require('express')
var mongojs = require('mongojs')
var bodyParser = require("body-parser");
var app = express()

var db = require('./myDB.js'),
    ObjectId = require('mongodb').ObjectId;

//var exec = require('exec');
var async = require('async');

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.use(express.json())
app.listen(24559, () => console.log('Hello'))
app.get('/', (req, res) => {
    console.log("HELLO");
    res.send("Hello VANE-Inc :)")
})

//req should have the question text of the flashcard to flag
app.post('/flagFlashCard', (req, res) => {
    db.flagFlashCard(req.body, function(docs) {   //gotta get the question field out of that
	res.send(docs);
    });
});

app.post('/unFlagFlashCard', (req, res) => {
    var flashName = req.body.question;
    db.unFlagFlashCard(flashName, function(docs) {
	console.log("THE FLASHCARD HAS BEEN UNFLAGGED");
	res.send(docs);
    });
});

app.post('/createFlashCard', (req, res, next) => {
    var flashCard = req.body;
    var question = req.body.question;
    db.createFlashCard(flashCard, question, function(docs) {
	console.log("Flashcard created: ", docs);
	res.send(docs);
    });
});
app.get('/getAllQuestions', (req, res) => {
    db.getAllFlashcards(function(docs) {
    res.send(docs);
    });
})

app.post('/removeFlaggedCards', (req, res) => {
    db.removeFlaggedFlashcard(function(docs) {
	console.log("FLAGGED CARDS REMOVED", docs);
	res.send(docs);
    });
});

app.get('/getAllQuestions', (req, res) => {
    db.getAllFlashcards(function(docs) {
	res.send(docs);
    });
})

app.get('/getFlashCard', (req, res) => {
    let flashID = ObjectId("5ab7b11dd6fdc73127e97d19");

    db.getFlashCard("Interview Questions", flashID, function(docs) {
	console.log("Flashcard retrieved: ", docs);
	res.send(docs);
    });    
});
