import os
import re

def split_path(path):
    x = re.compile(r'(.*)[/\\](.*)$')
    y = x.findall(path)
    return y[0][0], y[0][1]

if __name__ == "__main__":
    print(split_path("E:\Hard\item\WIFI_videoSurveillance\hostComputer"))