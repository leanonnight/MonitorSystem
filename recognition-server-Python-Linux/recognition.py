# -*- coding: UTF-8 -*-
import cv2
import numpy as np
from myUtil import *


def recognition():
    start_time = time.time()
    visible = True
    #########################
    # 读取图片 并做灰度化
    #########################
    srcImage = cv2.imread("camera.jpeg")  # 读取源图片
    sTplImage = cv2.imread("sTemplate.png")  # 读取模板5
    bTplImage = cv2.imread("bTemplate.png")  # 读取模板5

    srcImage_width, srcImage_height = srcImage.shape[1], srcImage.shape[0]
    print("srcImage: w = %d, h = %d" % (srcImage_width, srcImage_height))



    # 自适应均衡化，参数可选
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(9, 9))
    srcImage_clahe = clahe_BGR_Img(srcImage, clahe)

    ###############################
    # 第一次模板匹配
    ##############################
    tpl1MaxSimilar_list = my_matchTemplate_smallRect(srcImage, sTplImage, 0.5, cv2.TM_CCORR_NORMED)
    tpl1MaxSimilar_list = nms(tpl1MaxSimilar_list)
    # drawMatchResult_list("first_matchResult", srcImage, tpl1MaxSimilar_list, 0.38, visible=visible, color=(0, 0, 255))

    redArea_list = []
    # 去除非红色点
    for matchResult in tpl1MaxSimilar_list:
        confidence, (x, y), width, height = matchResult
        kernel = 13
        point = (int(x+width/2), int(y+height/2))
        flag = True
        for i in range(point[0]-int(kernel/2), point[0]+math.ceil(kernel/2)):
            for j in range(point[1]-int(kernel/2), point[1]+math.ceil(kernel/2)):
                (tb, tg, tr) = srcImage_clahe[j][i]
                if tr < 3*tg or tr < 3*tb:
                    flag = False
                    break
        if flag:
            redArea_list.append(matchResult)

    drawMatchResult_list("redPoint", srcImage_clahe, redArea_list, 0.38, visible=visible, color=(0, 0, 255))

    # temp_list = []
    # for i, redPoint in enumerate(redArea_list):
    #     r = 100
    #     confidence, (x, y), width, height = redPoint
    #     point = (int(x+width/2), int(y+height/2))
    #     x1, y1 = point[0]-r, point[1]-r
    #     x2, y2 = point[0]+r, point[1]+r
    #     if x1 < 0:
    #         x1 = 0
    #         x2 = r*2
    #     if y1 < 0:
    #         y1 = 0
    #         y2 = r*2
    #     if x2 >= srcImage_width:
    #         x2 = srcImage_width-1
    #         x1 = srcImage_width-1-r*2
    #     if y2 >= srcImage_height:
    #         y2 = srcImage_height-1
    #         y1 = srcImage_height-1-r*2
    #     tempImg = srcImage[y1:y2, x1:x2]
    #     tpl1MaxSimilar_list = my_matchTemplate_smallRect(tempImg, bTplImage, 0.5, cv2.TM_CCORR_NORMED)
    #     tpl1MaxSimilar_list = nms(tpl1MaxSimilar_list)
    #     drawMatchResult_list("first_matchResult", tempImg, tpl1MaxSimilar_list, 0.38, visible=visible, color=(0, 0, 255))
        # redArea_list[i] = (max_val, (x, y), width, height)
        # temp_list.append((confidence, (x1, y1), r*2, r*2))
    # drawMatchResult_list("redArea_list", srcImage, redArea_list, 0.38, visible=visible, color=(0, 0, 255))

    #########################################################################################################
    # 找到四个红点 并排序
    #########################################################################################################
    redArea_list = redArea_list[0:4]  # 只取前四个
    Sort_list = sort_byOrientation_list(redArea_list)  # 从左到右 从上到下排列
    temp_list = []
    for item in Sort_list:
        i = redArea_list.index(item)
        temp_list.append(redArea_list[i])
    redArea_list = temp_list
    # 绘出排序结果
    matchResultImg = drawMatchResult_list("redPointSort", srcImage, redArea_list, 0.38, visible=visible, color=(0, 0, 255))

    # 通过红点找到四个仪表盘
    strumentImg_list = []
    redPoint_list = []
    for redPoint in redArea_list:
        confidence, (x, y), width, height = redPoint
        point = (int(x+width/2), int(y+height/2))
        x1, y1 = point[0]-200, point[1]-200
        x2, y2 = point[0]+200, point[1]+200
        if x1 < 0:
            x1 = 0
            x2 = 400
        if y1 < 0:
            y1 = 0
            y2 = 400
        if x2 >= srcImage_width:
            x2 = srcImage_width-1
            x1 = srcImage_width-1-400
        if y2 >= srcImage_height:
            y2 = srcImage_height-1
            y1 = srcImage_height-1-400
        redPoint_list.append((point[0]-x1, point[1]-y1))
        strumentImg_list.append((confidence, (x1, y1), 400, 400))
    # 绘出四个仪表盘
    drawMatchResult_list("strumentImg", srcImage, strumentImg_list, 0.38, visible=visible, color=(0, 0, 255))

    scale_list = []
    for i,strumentImg in enumerate(strumentImg_list):
        confidence, (x, y), width, height = strumentImg
        redPoint = redPoint_list[i]
        strumentImg = srcImage[y:y+height, x:x+width]
        scale = recognition_strument(strumentImg, redPoint, visible=visible)
        scale_list.append(scale)
        srcImage = drawRecognition_scale(srcImage, redArea_list[i][1], scale, (0, 0, 255), 1)

    end_time = time.time()
    print("共耗时:", int((end_time-start_time)*1000), "ms")
    font = cv2.FONT_HERSHEY_SIMPLEX
    cv2.putText(srcImage, (datetime.datetime.now().strftime('%Y-%m-%d %H-%M-%S')),
                (20, 60), font,
                2, (0, 0, 255), 3,
                lineType=cv2.LINE_AA)
    cv2.putText(srcImage, "elapsed-time: " + str(int((end_time-start_time)*1000)) + "ms",
                (20, 120), font,
                2, (0, 0, 255), 3,
                lineType=cv2.LINE_AA)
    cv2.imwrite("recognition_img/%s.jpeg" % (datetime.datetime.now().strftime('%Y-%m-%d %H-%M-%S')), srcImage)
    cv2.imwrite("recognition_img/recognize.jpeg", srcImage)
    if visible:
        srcImage = cv2.resize(srcImage, None, fx=0.38, fy=0.38)
        cv_show("strument", srcImage)

    return scale_list

if __name__ == '__main__':
    lt_scale, rt_scale, lb_scale, rb_scale = recognition()
    print(lt_scale, rt_scale, lb_scale, rb_scale)