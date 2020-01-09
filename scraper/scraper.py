import requests
import re
import time
import json
from bs4 import BeautifulSoup

DEBUG = 0

class QuestionCrawler(object):
    def __init__(self):
        self.visited = set()
        self.output = ""
    
    #Check permissions on the website with the current url
    #returns true if this url is crawlable
    def crawlAllowed(self, url):
        domain = url.split(".com/")[0] + ".com"
        if(DEBUG): print("ROBOTS: reading permissions from " + url)
        robot = requests.get(domain + "/robots.txt").content
        if "User-agent: *" in robot:
            keyword = "User-agent: *"
        else:
            if "User-Agent: *" in robot:
                keyword = "User-Agent: *"
            else:
                if(DEBUG): print("  ALLOWED: Permissions not set :)\n")
                return True                                           #Website has not set permissions, good to crawl!
        permissions = robot.split(keyword)[1]
        permissions = permissions.split("\n")
        
        for location in permissions:                                #If any disallowed domains match, return false and don't crawl
            if "Disallow: " in location:
                permission = domain + location.split("Disallow: ")[1]
                #print("Testing: " + permission)
                if permission in url:
                    if(DEBUG): print("  DISALLOW: " + location + " :(\n")
                    return False
        if(DEBUG):
            print("  ALLOWED: Site crawlable!\n")
        return True                                                 #If it makes it through the for loop without returning false, we can crawl this link

    #Store the HTML of the url (This ignores the HTML in google searches)
    #Returns the HTML to be processed
    def getHTML(self, url):
        try:
            response = requests.get(url) 
            html = response.content
        except Exception as e:
            print("error getting content")
            html = 'error'
        self.output = html 
        return html

    #Parse all the links in the html
    #Returns a list of all the links
    def getLinks(self, url):
        if self.crawlAllowed(url):
            html = self.getHTML(url)
            return re.findall('<a\shref="([^"#]*)"', html)
        else:
            return []

    def parse(self, output):
        content = BeautifulSoup(output, "html.parser")

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

        #give to database
        URL = "http://ukko.d.umn.edu:24559/createFlashcard"
        for element in jsonArray:
            data = jsonArray.get(element)
            requests.post(url = URL, data = data)
        return

    #CRAWL STARTS HERE
    def crawl(self, url):
        for link in self.getLinks(url):
            if DEBUG: print("PROCESSING: " + link)
            if link in self.visited:                                                    #Already scraped this url
                if DEBUG: print("  DUPLICATE: " + link)
                continue
            if link.find("interview-question") == -1:                                   #Not a relevant link
                if DEBUG: print("  SKIP: 'interview-question' not in " + link)
                continue
            else:
                if link not in self.visited:
                    self.visited.add(link)
                    log = open("scraper.log", "w+")
                    log.write("  ADDING: " + link)
                    #print("  ADDING: " + link)          #Add to log file
                    log.close()
                    self.parse(self.output)         ##PARSE
                if DEBUG: print("SLEEPING\n")
                time.sleep(30)
                if DEBUG: print("WAKING UP")
                self.crawl(link)
        #print("ALL LINKS FOUND")
        if DEBUG:
            print("\nVISITED:")
            for link in self.visited:
                print(link)
    

if __name__ == "__main__":
    googleSearch = "https://www.google.com/search?q=computer+science+interview+questions"

    print("Start")
    crawler = QuestionCrawler()
    html = crawler.getHTML(googleSearch)
    for link in re.findall('<a\shref="([^"#]*)"', html):
        if "http" in link:
            if link.startswith("/"):
                link = link.rsplit('http')[1]
                link = "http" + link
            crawler.crawl(link)
    print("ALL LINKS FOUND")

##How to make a get request and then print it out
#---------------------------------------------
#response = requests.get(url)

#print(response.content)
