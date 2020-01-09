import requests
import re
import time

DEBUG = 0

class QuestionCrawler(object):
    def __init__(self):
        self.visited = set()
    
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
        try:                                    #TODO: This should go to a variable for Eric's code instead of the file
            f = open('scrape.txt', 'w')         #
            f.write(html)                       #
            f.close()                           #
        except Exception as e:                  #
            print(e)                            #
        return html

    #Parse all the links in the html
    #Returns a list of all the links
    def getLinks(self, url):
        if self.crawlAllowed(url):
            html = self.getHTML(url)
            return re.findall('<a\shref="([^"#]*)"', html)
        else:
            return []

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
                    print("  ADDING: " + link)
                if DEBUG: print("SLEEPING\n")
                time.sleep(20)
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
    for link in re.findall('<a\shref="([^"#]*)"', crawler.getHTML(googleSearch)):
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
