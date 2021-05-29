import re
def split_book(book):
    bookName = ""
    chapterName_list = []
    chapterContent_list = []
    rex = re.compile(r"\n*(第.*章[ .、].*|[零一二三四五六七八九十]+[ .、].*)\b\n*")
    # rex = re.compile(r"\n*(第.*章[ .、].*)\b\n*")
    x = rex.split(book)
    bookName = x[0]
    for i, iter in enumerate(x[1:]):
        if i % 2 == 0:
            chapterName_list.append(iter)
        else:
            chapterContent_list.append(iter)
    # for i in chapterName_list:
    #     print(i)
    return bookName, chapterName_list, chapterContent_list

if __name__ == "__main__":
    book = ""
    with open("../游戏娱乐帝国.txt", "r", encoding="utf-8") as f:
       book = f.read()
    print(split_book(book))