import os


def file_name(file_dir):
    file_list = []  # 将文件放入此列表
    dir_list = []  # 将文件夹放入此列表
    for files in os.listdir(file_dir):  # 不仅仅是文件，当前目录下的文件夹也会被认为遍历到
        absFiles = file_dir + "/" + files
        if os.path.isdir(absFiles):
            dir_list.append(files)
        else:
            file_list.append(files)
    return dir_list, file_list

if __name__ == '__main__':
    print(file_name('E:\Hard\item\WIFI_videoSurveillance\hostComputer'))