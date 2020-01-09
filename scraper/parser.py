from bs4 import BeautifulSoup
import re
import json
import requests

DEBUG = True

#Parsing Testing File
#Author: Eric Fracek
#Term: Fall 2019
#Course: CS 3451 Software Engineering
#When finished, this file will be combined with 
#scraper.py in the parse method


#TODO
#remove all <br /> tags to fix broken answers
#check for multiline answers


#creates BeautifulSoup Object from opened HTML document
with open('/home/eric/Documents/School/Junior/Software Engineering/VANE Inc/scraper/test.txt') as fp:
    content = BeautifulSoup(fp, "html.parser")

#Compiled Regular Expressions
quesRegex = re.compile("\?$") #finds questions only
ansRegex = re.compile("Answer\:") #finds answers only

stringList = list(content.strings) #list of raw strings from HTML

#finds question and answer indices
questionIndices = [i for i, x in enumerate(stringList) if re.search(quesRegex, x)]
answerIndices = [i for i, x in enumerate(stringList) if re.search(ansRegex, x)]

#indices of questions and answers
questionAnswerIndices = answerIndices + questionIndices

#sort list for ordering question answer pairs
questionAnswerIndices.sort()

#makes list of questions and answers
questionAnswerList = []

questionList = []#list of questions
#populates list of questions and answers
for i in questionAnswerIndices:
    if i not in answerIndices:#element is question
        question = stringList[i].lstrip('Q1234567890. ')
        questionLength = len(question)
        if (questionLength > 20) and (questionLength < 250): #if valide question length
            questionAnswerList.append(question) #adds question to QA list
            questionList.append(question) #adds to list of only questions for json formatting later
    if i in answerIndices: # element is answer
        answer = stringList[i + 1].strip('\n') #cleans answer
        indexAfterAnswer = i + 2
        TooLong = len(answer) > 250
        while((indexAfterAnswer not in questionIndices) and (not TooLong)): #checking for mutliline answer
            nextAnswer = stringList[indexAfterAnswer].strip('\n')
            if len(answer + nextAnswer) < 250:
                answer = answer + " " + nextAnswer #concatenate next answer to answer
            indexAfterAnswer += 1
        answerLength = len(answer)
        if (answerLength > 20) and (answerLength < 250): #if valid answer size
            questionAnswerList.append(answer)#adds answer to QA list

#begin json Formatting
jsonArray = {}
for q in questionList:
    questionAnswerListIndex = questionAnswerList.index(q) #index of question in QA list
    answerIndex = questionAnswerListIndex + 1
    answerIndexNotOffEndOfList = answerIndex < len(questionAnswerList) #checks if next index (answer) exists and is not end of list
    
    if answerIndexNotOffEndOfList:
        answer = questionAnswerList[answerIndex]
        if re.search(quesRegex, answer): #Case 1 : next element in list is Question
            data = {"question" : q,
                    "flagged" : "false"}
        else: #Case 2 : Answer Exists
            data = {"question" : q, 
                    "answer" : answer,
                    "flagged" : "false"}
    else :  #Case 3 : next element in list is end of list
        data = {"question" : q,
                "flagged" : "false"}

    if q not in jsonArray.values(): #not a duplicate question
        jsonArray[len(jsonArray) + 1] = data #adds question to jsonArray
#end json formatting


#print to test file
with open('/home/eric/Documents/School/Junior/Software Engineering/VANE Inc/scraper/json.txt', 'w') as outfile:
    json.dump(jsonArray, outfile)


#give to database
URL = "http://ukko.d.umn.edu:24559/createFlashcard"
for element in jsonArray:
    data = jsonArray.get(element)
    if DEBUG : print(data)
    if not DEBUG : requests.post(url = URL, data = data)
