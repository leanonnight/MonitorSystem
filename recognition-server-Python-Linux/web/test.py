import os
import re

from bs4 import UnicodeDammit
from flask import Flask, redirect, url_for, render_template, request
from util.splitPath import split_path
from util.getFiles import file_name

from web.util.splitBook import split_book

app = Flask(__name__)

# 全局变量
current_path = ""
bookName = ""
chapterName_list = []
chapterContent_list = []

@app.route('/')
@app.route('/<path>')
def ChooseBook(path=None):
    global current_path

    if len(request.args) == 1:
        if request.args['bp'] is not None:
            current_path = request.args['bp']
    dir_list, file_list = file_name(current_path)
    last_path, temp = split_path(current_path)
    return render_template('chooseBook.html', dir_list=dir_list, file_list=file_list, current_path=current_path, last_path=last_path)

@app.route("/catalog/<bn>", methods=["POST", "GET"])
@app.route("/catalog/", methods=["POST", "GET"])
@app.route("/catalog/book/")
@app.route("/catalog/book/<bn>")
def catalog(bn=None):
    global bookName, chapterName_list, chapterContent_list
    print(request.path)
    if "/catalog/book/" not in request.path:
        if request.method == "POST":
            file = request.files["bookName"]

            book = UnicodeDammit(file.read(), ["utf-8", "gbk"]).unicode_markup
            bookName = file.filename
            try:
                with open(bookName, "w", encoding="utf-8") as f:
                    f.write(book)
            except:
                with open(bookName, "w", encoding="gbk") as f:
                    f.write(book)
        else:
            if len(request.args) == 2:
                if request.args['bookname'] is not None:
                    bookName = request.args['bookname']
                    path = request.args['path']
                    book = ""
                    with open(path, 'r', encoding="utf-8") as f:
                        book = f.read()
        bookName, chapterName_list, chapterContent_list = split_book(book)
    return render_template("catalog.html", bookname=bookName, chaptername_list=chapterName_list)

@app.route("/readbook/<chapterName>")
@app.route("/readbook/")
def readbook(chapterName=None):
    chapterName = request.args["cn"]
    chapterName_NO = request.args["cnn"]
    chapterName_NO = int(chapterName_NO)
    chapterContent = chapterContent_list[chapterName_NO]
    # chapterContent = re.sub(r"", "\n", chapterContent)
    chapterContent = re.split(r"\n+", chapterContent)
    return render_template("chapter.html", bookName=bookName, chapterContent=chapterContent, chapterName_NO=chapterName_NO, chapterName_list=chapterName_list)

if __name__ == '__main__':

    current_path = "E:\\Hard\\item\\WIFI_videoSurveillance\\hostComputer\\recognition2.0-server\\web"
    app.run("0.0.0.0", port=80, debug=True)

