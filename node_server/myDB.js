const mongojs = require('mongojs'); //imports 'mongojs'
const assert = require('assert'); //Assertion for queries

// Connection URL
const url = "mongodb://ukko.d.umn.edu:24558/"; 
//URL with database included for local mongo db

// Database Name
const collections=["Interview Questions"]; //list of collections that you will be accessing. 
mongodb = mongojs(url, collections);

var ObjectId = require('mongodb').ObjectID;

//var exec = require('exec');
var async = require('async');
module.exports = {
    flagFlashCard : function(question, callback) {		//flashcard should have the text of the question
	console.log("flagging question: " + question.question)
	mongodb.collection('Interview Questions').update(
    	    {"question": question.question},
    	    {$set: {"flagged": "true"}},
    	    {multi: true},
	    function(err, docs) {
	        if(err) {
	    	    console.log("FLAGGING FAILED");
		    callback("FLAGGING FAILED")
	        }
	        else {
		    console.log("Flagged " + docs.nModified + " questions")
	    	    callback(docs)
	        }
	    }
	);
    },

    createFlashCard : function(flashcard, question, callback) {
	if(!JSON.stringify(flashcard).includes("question")) {
	    console.log("ERROR - THE FLASHCARD MUST CONTAIN A QUESTION");
	}
	else {
	    mongodb.collection("Interview Questions").count({"question" : question}, function(err, documentCount) {
		if(documentCount < 1) {
		    mongodb.collection("Interview Questions").insert(flashcard, function(err, docs) {
			if(err) {
			    console.log("FLASHCARD CREATION FAILED");
			    callback("FLASHCARD CREATION FAILED");
			}
			else {
			    callback(docs);
			}
		    });
		}
		else {
		    console.log("DUPLICATE DETECTED - FLASHCARD NOT ADDED");
		    callback("DUPLICATE DETECTED - FLASHCARD NOT ADDED");
		}
	    });
	}
    },

    //returns a flashcard to a user WORKS THANK GOD
    getFlashCard : function(colName, flashID, callback) {
	let data = mongodb.collection("Interview Questions").aggregate([{ $sample: { size: 1} }]).toArray((err, results) => {
	    callback(results);
	});
    },

    removeFlaggedFlashcard : function(callback) {
	let data = mongodb.collection("Interview Questions").remove({"flagged" : "true"}, {justOne : false}, function(err, docs) {
	    if(err) {
		console.log("THERE WAS AN ERROR REMOVING FLAGGED QUESTIONS");
		callback("ERROR - FLAGGED QUESTIONS NOT REMOVED");
	    }
	    else {
		callback("FLAGGED CARDS REMOVED");
	    }
	});
    },

    unFlagFlashCard : function(flashName, callback) {
	console.log("UNFLAGGING FLASHCARD: " + flashName);
	mongodb.collection("Interview Questions").update(
	    {"question" : flashName},
	    {$set : {"flagged" : "false"}},
	    {multi : true},
	    function(err, docs) {
		if(err) {
		    callback("ERROR - UNFLAGGING FAILED");
		}
		else {
		    console.log("Flagged " + docs.nModified + " questions");
		    callback(docs);
		}
	    }
	);
    },

    getAllFlashcards : function(callback) {
	mongodb.collection("Interview Questions").find({}).toArray((err, results) => {
	    if(err) {
		callback("THERE WAS AN ERROR WHEN RETREVING ALL THE FLASHCARDS");
	    }
	    else {
		callback(results);
	    }
	});
    }
    
}



