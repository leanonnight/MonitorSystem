# -*- coding: UTF-8 -*-
import copy
import datetime
import math
import operator
import time
import cv2
import numpy as np
from numpy.linalg import solve


class Rect:
    def __init__(self, x, y, width, height):
        self.x = x
        self.y = y
        self.width = width
        self.height = height

    # /**
    # #  * @brief 判断两个轴对齐的矩形是否重叠
    # #  * @param rc1 第一个矩阵的位置
    # #  * @param rc2 第二个矩阵的位置
    # #  * @return 两个矩阵是否重叠（边沿重叠，也认为是重叠）
    # #  */
    def isOverlap(self, rc1, rc2=None):
        if rc2 is None:
            rc2 = self
        verticalDistance = abs(rc2.y - rc1.y)
        horizontalDistance = abs(rc2.x - rc1.x)
        verticalThreshold = (rc1.height + rc2.height) / 2
        horizontalThreshold = (rc1.width + rc2.width) / 2
        if verticalDistance < verticalThreshold and horizontalDistance < horizontalThreshold:
            return True
        else:
            return False


# 绘图
def cv_show(windowName, img):
    cv2.imshow(windowName, img)
    cv2.waitKey(0)
    cv2.destroyWindow(windowName)


# 非极大值抑制
def nms(matchResult_list):
    result_list = []
    matchResult_list = sorted(matchResult_list, reverse=True)
    result_list.append(matchResult_list.pop(0))
    while len(matchResult_list) > 0:
        current_maxRect = Rect(result_list[-1][1][0], result_list[-1][1][1], result_list[-1][2], result_list[-1][3])
        temp_list = copy.copy(matchResult_list)  # 拷贝(完全复制一份)
        for matchResult in matchResult_list:
            confidence, (x, y), width, height = matchResult
            rect = Rect(x, y, width, height)
            if current_maxRect.isOverlap(rect):
                temp_list.remove(matchResult)
        matchResult_list = temp_list
        if len(matchResult_list) != 0:
            result_list.append(matchResult_list.pop(0))
    # print("nms result_list:" + str(len(result_list)))
    return result_list


methods = ['cv2.TM_CCOEFF', 'cv2.TM_CCOEFF_NORMED', 'cv2.TM_CCORR',
           'cv2.TM_CCORR_NORMED', 'cv2.TM_SQDIFF', 'cv2.TM_SQDIFF_NORMED']


def get_match_rect(img, template, method):
    '''获取模板匹配的矩形的左上角和右下角的坐标'''
    w, h = template.shape[1], template.shape[0]
    res = cv2.matchTemplate(img, template, method)
    mn_val, max_val, min_loc, max_loc = cv2.minMaxLoc(res)
    # 使用不同的方法，对结果的解释不同
    if method in [cv2.TM_SQDIFF, cv2.TM_SQDIFF_NORMED]:
        top_left = min_loc
        confidence = mn_val
    else:
        top_left = max_loc
        confidence = max_val
    bottom_right = (top_left[0] + w, top_left[1] + h)
    return top_left, bottom_right, confidence


def drawMatchResult_list(windowName, image, resultList, stretch, visible=True, isIdentify=True, color=(127, 127, 127), fontSize=2, thickness=3):
    # pass
    image = image.copy()
    font = cv2.FONT_HERSHEY_SIMPLEX
    for i, (confidence, (leftTop_x, leftTop_y), width, height) in enumerate(resultList):
        cv2.rectangle(image, (leftTop_x, leftTop_y), (leftTop_x + width, leftTop_y + height), color, 3)
        if isIdentify:
            cv2.putText(image, str(round(confidence, 2)), (leftTop_x, leftTop_y - int(width/10)), font, fontSize, color, thickness,
                        lineType=cv2.LINE_AA)
            cv2.putText(image, str(i + 1), (int(leftTop_x + width / 2), int(leftTop_y + height / 2)), font, fontSize, color, thickness,
                        lineType=cv2.LINE_AA)
        # print("i:  " + str(i + 1) + str((leftTop_x, leftTop_y)))
    if visible is True:
        image = cv2.resize(image, None, fx=stretch, fy=stretch)
        cv2.imshow(windowName, image)
        cv2.waitKey(0)
    # print("drawMatchResult len" + str(len(resultList)))
    return image


def drawMatchResult(windowName, image, leftTop, rightBottom, confidence, stretch):
    # pass
    image = image.copy()
    font = cv2.FONT_HERSHEY_SIMPLEX
    cv2.rectangle(image, leftTop, rightBottom, 255, 3)
    cv2.putText(image, str(round(confidence, 2)), (leftTop[0], leftTop[1] - 20), font, 2, 255, 3,
                lineType=cv2.LINE_AA)
    cv2.putText(image, str(1), (int((leftTop[0] + rightBottom[0]) / 2), int((leftTop[1] + rightBottom[1]) / 2)), font,
                2, 255, 3,
                lineType=cv2.LINE_AA)
    # print("i:  " + str(1) + str(leftTop))
    image = cv2.resize(image, None, fx=stretch, fy=stretch)
    # cv2.imshow(windowName, image)
    # print("drawMatchResult" + str(1))
    # cv2.waitKey(0)
    return image


# 绘出识别出的角度
def drawRecognition_scale(image, point, scale, color, stretch=1):
    x, y = point
    image = image.copy()
    font = cv2.FONT_HERSHEY_SIMPLEX
    cv2.putText(image, "scale: " + str(round(scale, 1)),
                (x, y-200), font,
                2, color, 3,
                lineType=cv2.LINE_AA)
    image = cv2.resize(image, None, fx=stretch, fy=stretch)
    return image


def fill_color(image, seedPoint, newVal, loDiff, upDiff):  # 泛洪填充，flags为cv.FLOODFILL_FIXED_RANGE
    copy_img = image.copy()
    h, w = image.shape[0:2]
    mask = np.zeros([h + 2, w + 2], dtype=np.uint8)
    cv2.floodFill(image=copy_img, mask=mask, seedPoint=seedPoint, newVal=newVal,
                  loDiff=loDiff, upDiff=upDiff, flags=cv2.FLOODFILL_FIXED_RANGE)
    return copy_img


def my_matchTemplate_smallRect(srcImage, tplImage, confidence, method, rectWidth=None, rectHeight=None):
    ###############################
    # 模板匹配
    ##############################
    res = cv2.matchTemplate(srcImage, tplImage, method)  # 相似度匹配
    res_width, res_height = res.shape[1], res.shape[0]
    tplImage_width, tplImage_height = tplImage.shape[1], tplImage.shape[0]
    if rectWidth is not None:
        tplImage_width = int(rectWidth)
    if rectHeight is not None:
        tplImage_height = int(rectHeight)
    # 创建一个矩阵 存储匹配结果矩阵(res)分割成模板(tplImage1)大小的一块块小矩阵的左上角顶点
    matrix_tplLeftTop = np.zeros(dtype=tuple,
                                 shape=(int(res_height / tplImage_height) + 1, int(res_width / tplImage_width) + 1))
    # 分割匹配结果矩阵(res)
    if tplImage_width > res_width or tplImage_height > res_height:
        matrix_tplLeftTop[0][0] = (0, 0)
    else:
        for i, h in enumerate(range(0, res_height, tplImage_height)):
            for j, w in enumerate(range(0, res_width, tplImage_width)):
                matrix_tplLeftTop[i][j] = (w, h)

    ##############################
    # 找到每一块小矩阵的极大值点
    ##############################
    tpl1MaxSimilar_list = []
    for i in range(matrix_tplLeftTop.shape[0]):
        for j in range(matrix_tplLeftTop.shape[1]):
            point = matrix_tplLeftTop[i][j]
            if i == matrix_tplLeftTop.shape[0] - 1:
                rect = res[point[1]:res_height, point[0]:point[0] + tplImage_width]
            elif j == matrix_tplLeftTop.shape[1] - 1:
                rect = res[point[1]:point[1] + tplImage_height, point[0]:res_width]
            else:
                rect = res[point[1]:point[1] + tplImage_height, point[0]:point[0] + tplImage_width]
            mn_val, max_val, min_loc, max_loc = cv2.minMaxLoc(rect)
            # 使用不同的方法，对结果的解释不同
            if method in [cv2.TM_SQDIFF, cv2.TM_SQDIFF_NORMED]:
                min_point = (min_loc[0] + point[0], min_loc[1] + point[1])
                mn_val = 1 - mn_val
                if mn_val > confidence:  # 只获取相似度超过百分之30的点
                    tpl1MaxSimilar_list.append((mn_val, min_point, tplImage.shape[1], tplImage.shape[0]))
            else:
                max_point = (max_loc[0] + point[0], max_loc[1] + point[1])
                if max_val > confidence:  # 只获取相似度超过百分之30的点
                    tpl1MaxSimilar_list.append((max_val, max_point, tplImage.shape[1], tplImage.shape[0]))

    # print("my_matchTemplate_smallRect matchResult_len:" + str(len(tpl1MaxSimilar_list)))
    return tpl1MaxSimilar_list


def my_matchTemplate_confidence(srcImage, tplImage, confidence, method):
    res = cv2.matchTemplate(srcImage, tplImage, method)  # 相似度匹配
    tpl1MaxSimilar_list = []
    for h in range(res.shape[0]):
        for w in range(res.shape[1]):
            if res[h][w] >= confidence:
                tpl1MaxSimilar_list.append((res[h][w], (w, h), tplImage.shape[1], tplImage.shape[0]))
    # print("my_matchTemplate_smallRect matchResult_len:" + str(len(tpl1MaxSimilar_list)))
    return tpl1MaxSimilar_list


# 从左到右 从上到下
def sort_byOrientation_list(matchResult_list):
    result_list = []
    sortX_list = []
    leftTop_strument = None
    rightTop_strument = None
    leftBottom_strument = None
    rightBottom_strument = None
    sortX_list.append(matchResult_list[0])
    list_temp = matchResult_list[1:4]
    # 按照x的大小排序 从大到小
    for matchResult in list_temp:
        confidence, (x, y), width, height = matchResult
        if x >= sortX_list[-1][1][0]:
            sortX_list.append(matchResult)
            continue
        for i, sortx in enumerate(sortX_list):
            if x < sortx[1][0]:
                sortX_list.insert(i, matchResult)
                break

    # x较小的点为左侧点
    if sortX_list[0][1][1] < sortX_list[1][1][1]:  # y较小的点为上顶点
        leftTop_strument = sortX_list[0]
        leftBottom_strument = sortX_list[1]
    else:
        leftTop_strument = sortX_list[1]
        leftBottom_strument = sortX_list[0]

    # x较大的点为右侧侧点
    if sortX_list[2][1][1] < sortX_list[3][1][1]:  # y较小的点为上顶点
        rightTop_strument = sortX_list[2]
        rightBottom_strument = sortX_list[3]
    else:
        rightTop_strument = sortX_list[3]
        rightBottom_strument = sortX_list[2]

    result_list.append(leftTop_strument)
    result_list.append(rightTop_strument)
    result_list.append(leftBottom_strument)
    result_list.append(rightBottom_strument)
    return result_list


# 从左到右 从上到下
def sort_rect_point(rect):
    leftTop, rightTop, leftBottom, rightBottom = None, None, None, None
    sortX_list = []
    sortX_list.append((rect[0][0], rect[0][1]))
    for point in rect[1:]:
        # 按照x的大小排序 从大到小
        x, y = point[0], point[1]
        if x >= sortX_list[-1][0]:
            sortX_list.append((x, y))
            continue
        for i, sortx in enumerate(sortX_list):
            if x < sortx[0]:
                sortX_list.insert(i, (x, y))
                break
    # x较小的点为左侧点
    if sortX_list[0][1] < sortX_list[1][1]:  # y较小的点为上顶点
        leftTop = sortX_list[0]
        leftBottom = sortX_list[1]
    else:
        leftTop = sortX_list[1]
        leftBottom = sortX_list[0]

    # x较大的点为右侧侧点
    if sortX_list[2][1] < sortX_list[3][1]:  # y较小的点为上顶点
        rightTop = sortX_list[2]
        rightBottom = sortX_list[3]
    else:
        rightTop = sortX_list[3]
        rightBottom = sortX_list[2]

    x = (leftTop, rightTop, leftBottom, rightBottom)
    return x


# 获取两点间距离
def getPointDistance(point1, point2):
    x1, y1 = point1[0], point1[1]
    x2, y2 = point2[0], point2[1]
    return math.sqrt((x1 - x2) ** 2 + (y1 - y2) ** 2)


# 获取直线斜率
def getStraightSlope(point1, point2):
    x1, y1 = point1[0], point1[1]
    x2, y2 = point2[0], point2[1]
    return (y2 - y1) / (x2 - x1)


def getPointerContour():
    shapeImg = cv2.imread("pointer_shape.png")
    shapeImg = cv2.cvtColor(shapeImg, cv2.COLOR_BGR2GRAY)
    # 寻找二值化图中的轮廓
    contours, hierarchy = cv2.findContours(
        shapeImg, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    maxAreaContour = contours[0]
    maxArea = cv2.contourArea(maxAreaContour)
    for contour in contours:
        if cv2.contourArea(contour) > maxArea:
            maxAreaContour = contour
            maxArea = cv2.contourArea(maxAreaContour)
    drawContoursImg = cv2.drawContours(shapeImg, maxAreaContour, -1, (127, 127, 127), 3)
    cv2.imshow("shapeImg", shapeImg)
    return maxAreaContour




# 获取在直线上一点 该点与线外一点连线垂直于该直线
def getPointInStraight_ToVerticalStraight(line, point):
    k, b = line
    x1, y1 = point
    if y1 == int(x1*k + b):  # point在直线上
        return (x1, y1)

    # y2 - k * x2 = b
    # y2 + x2 / k = x1 / k + y1
    a = np.mat([[1, -k], [1, 1 / k]])  # 系数矩阵
    b = np.mat([b, x1 / k + y1]).T  # 常数项列矩阵
    x = solve(a, b)  # 方程组的解
    return (int(x[1][0]), int(x[0][0]))


# 获取距离中心点最近的点
def getMinDistancePoint_toRect(centerPoint, sortBox):
    minDistancePoint = sortBox[0]
    minDistance = getPointDistance(minDistancePoint, centerPoint)
    for point in sortBox[1:]:
        distance = getPointDistance(point, centerPoint)
        if distance < minDistance:
            minDistancePoint = point
            minDistance = distance
    return minDistancePoint, minDistance


# def getContourAndStraight_intersectionPoint(contour, line):
#     k, b = line
#     for point in contour:
#         print(point)

def recognize_fun1():
    pass




def recognition_strument(srcImg, centerPoint, visible=True):

    srcImg_gray = cv2.cvtColor(srcImg, cv2.COLOR_BGR2GRAY)
    srcImg2 = cv2.circle(srcImg_gray.copy(), centerPoint, 10, 255, -1)
    width, height = srcImg.shape[1], srcImg.shape[0]

    # 使用Otsu阈值法
    ret, srcImg_thres = cv2.threshold(srcImg_gray, 50, 255, cv2.THRESH_BINARY_INV | cv2.THRESH_OTSU)

    kernel_size = 3
    # 开运算
    while True:
        opening = srcImg_thres.copy()
        kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (kernel_size, kernel_size))  # 定义结构元素
        opening = cv2.morphologyEx(opening, cv2.MORPH_OPEN, kernel, iterations=1)  # 开运算
        kernel_size += 1    # 每次开运算都去判断指针轮廓面积 如果过大则说明指针轮廓与仪表盘粘连 则需加大开运算内核

        # 寻找二值化图中的轮廓
        contours, hierarchy = cv2.findContours(
            opening, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

        # 距离中轴点最近的轮廓
        minDistanceContour = contours[0]
        minDistance = cv2.pointPolygonTest(minDistanceContour, centerPoint, True)  # 在轮廓外为负 轮廓内为正
        for contour in contours:
            distance = cv2.pointPolygonTest(contour, centerPoint, True)  # 在轮廓外为负 轮廓内为正
            # print("当前最短距离:" + str(minDistance) + "   距离:" + str(distance))
            if minDistance >= 0 and distance >= 0:
                if distance < minDistance:
                    minDistanceContour = contour
                    minDistance = distance
            else:
                if distance > minDistance:
                    minDistanceContour = contour
                    minDistance = distance
        # print("面积:" + str(cv2.contourArea(minDistanceContour)))
        print("当前最短距离:" + str(minDistance))
        print("面积:", cv2.contourArea(minDistanceContour))
        if cv2.contourArea(minDistanceContour) < 7000:
            break

    # 直线拟合
    output = cv2.fitLine(minDistanceContour, cv2.DIST_L2, 0, 0.01, 0.01)
    k = output[1][0] / output[0][0]
    b = output[3][0] - k * output[2][0]
    rows, cols = srcImg.shape[:2]
    righty = int(cols * k + b)
    if visible:
        lineImg = cv2.line(opening.copy(), (cols - 1, righty), (0, b), (127, 127, 127), 3)

    # 画出轮廓
    if visible:
        drawContoursImg = cv2.drawContours(opening.copy(), minDistanceContour, -1, (127, 127, 127), 3)

    rect = cv2.minAreaRect(minDistanceContour)  # 最小外接矩形
    rect = np.int0(cv2.boxPoints(rect))  # 矩形的四个角点取整
    if visible:
        minAreaRectImg = cv2.drawContours(opening.copy(), [rect], 0, (255, 0, 0), 2)

    # print("rect" + str(rect))
    sortedRect = sort_rect_point(rect)  # 将矩形的四个点从向右 从上到下进行排序
    rectCenterPoint = (int((sortedRect[0][0] + sortedRect[1][0] + sortedRect[2][0] + sortedRect[3][0]) / 4),
                       int((sortedRect[0][1] + sortedRect[1][1] + sortedRect[2][1] + sortedRect[3][1]) / 4))
    if visible:
        lineImg = cv2.circle(lineImg, rectCenterPoint, 10, 127, -1)
        lineImg = cv2.circle(lineImg, centerPoint, 10, 127, -1)
    originalPoint = getPointInStraight_ToVerticalStraight((k, b), centerPoint)
    orientationPoint = getPointInStraight_ToVerticalStraight((k, b), rectCenterPoint)
    if visible:
        lineImg = cv2.circle(lineImg, originalPoint, 5, 200, -1)
        lineImg = cv2.circle(lineImg, orientationPoint, 5, 127, -1)
    angle1 = getAngle(originalPoint, orientationPoint)
    # ret, srcImg_thres2 = cv2.threshold(srcImg_thres, 127, 255, cv2.THRESH_BINARY_INV)
    # srcImg_thres2 = cv2.cvtColor(srcImg_thres2, cv2.COLOR_GRAY2BGR)
    # cv2.imshow("srcImg_thres2", srcImg_thres2)
    # srcImg_thres2 = cv2.circle(srcImg_thres2.copy(), originalPoint, 10, 255, -1)
    # angle2 = get_pointer_rad(srcImg_thres2, originalPoint, visible=visible)
    # angle = (angle1+angle2)/2
    angle = angle1
    scale = counterScale_byAngle(angle)
    print("angle:" + str(angle))
    print("scale:", scale)
    if visible:
        showImg = np.hstack((srcImg2, srcImg_thres, opening, lineImg))
        cv_show("showImg", showImg)
    return scale


def get_pointer_rad(img, centerPoint, visible=True):
    '''获取角度'''
    shape = img.shape
    c_x, c_y = centerPoint
    x1 = c_x+c_x*2
    src = img.copy()
    freq_list = []
    for i in range(361):
        x = (x1 - c_x) * math.cos(i * math.pi / 180) + c_x
        y = (x1 - c_x) * math.sin(i * math.pi / 180) + c_y
        temp = src.copy()
        cv2.line(temp, (c_x, c_y), (int(x), int(y)), (0, 0, 255), thickness=3)
        t1 = img.copy()
        t1[temp[:, :, 2] == 255] = 255
        c = img[temp[:, :, 2] == 255]
        points = c[c == 0]
        freq_list.append((len(points), i))
        if visible:
            cv2.imshow('d', temp)
            cv2.waitKey(1)

    j, i = max(freq_list, key=lambda x: x[0])
    if visible:
        print('当前角度：', 360 - i, '度')
        x = (x1 - c_x) * math.cos(i * math.pi / 180) + c_x
        y = (x1 - c_x) * math.sin(i * math.pi / 180) + c_y
        temp = src.copy()
        cv2.line(temp, (c_x, c_y), (int(x), int(y)), (0, 0, 255), thickness=3)
        cv2.imshow("d", temp)
        cv2.waitKey(0)
    return 360 - i


def getAngle(originalPointer, orientationPointer):
    p = getPointDistance(originalPointer, orientationPointer)
    x1, y1 = originalPointer
    x2, y2 = orientationPointer
    x = abs(x2 - x1)
    y = abs(y2 - y1)
    if x2 > x1 and y2 > y1:  # 第四象限
        y = -y
    elif x2 < x1 and y2 > y1:  # 第三象限
        x = -x
        y = -y
    elif x2 < x1 and y2 < y1:  # 第二象限
        x = -x
    elif x2 > x1 and y2 < y1:  # 第一象限
         pass
    try:
        sinTheta = y/p
        cosTheta = x/p
    except:
        return 0
    # print("sinTheta:" + str(sinTheta))
    # print("cosTheta:" + str(cosTheta))
    if sinTheta >= 0 and cosTheta > 0:  # 第一象限
        angle = math.asin(sinTheta)*180/math.pi
    elif sinTheta > 0 and cosTheta <= 0:  # 第二象限
        angle = math.asin(sinTheta)*180/math.pi + 90
    elif sinTheta <= 0 and cosTheta < 0:  # 第三象限
        angle = math.asin(sinTheta) * 180 / math.pi + 270
    elif sinTheta < 0 and cosTheta >= 0:  # 第四象限
        angle = math.asin(sinTheta) * 180 / math.pi + 360
    else:
        angle = 0
    return round(angle, 5)

def clahe_BGR_Img(img, clahe):
    # 分离每一个通道
    b, g, r = cv2.split(img)
    # 对每一个通道进行局部直方图均衡化
    b = clahe.apply(b)
    g = clahe.apply(g)
    r = clahe.apply(r)
    # 合并处理后的三通道 成为处理后的图
    image = cv2.merge([b, g, r])
    return image

def equalizeHist_BGR_Img(img):
    # 分离每一个通道
    b, g, r = cv2.split(img)
    # 对每一个通道进行局部直方图均衡化
    b = cv2.equalizeHist(b)
    g = cv2.equalizeHist(g)
    r = cv2.equalizeHist(r)
    # 合并处理后的三通道 成为处理后的图
    image = cv2.merge([b, g, r])
    return image

def counterScale_byAngle(angle):
    zeroScale_angle = 42.3           # 0刻度线 角度
    intervalScale_angle = 5.33712    # 每两刻度间隔角度
    scale_angle = 0                  # 当前指向刻度的角度
    scale = 0                        # 当前刻度
    # 计算当前指向刻度的角度
    if angle > zeroScale_angle:
        if angle < 90:   # 如果在90度内 刻度归零
            scale_angle = 0
        else:
            scale_angle = 360 - angle + zeroScale_angle
    else:
        scale_angle = zeroScale_angle - angle

    # 计算刻度
    scale_num = scale_angle / intervalScale_angle
    if scale_num <= 1:
        scale = scale_num
    else:
        scale = (scale_num) / 2

    return scale




if __name__ == '__main__':
    print(getAngle((125, 205), (128, 207)))