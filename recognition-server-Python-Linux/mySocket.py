# -*- coding: UTF-8 -*-
import datetime
import socket
import struct
import threading
import time
import traceback

import recognition
from e_mail import SendMail


class MySocket:
    IP = "ronight.top"
    PORT = 6515
    username = 'lizhixingss@qq.com'
    passwd = 'qahpxjupvituddje'
    recv = ['2504989046@qq.com']

    def __init__(self):
        self.sock = self.sock_init()
        self.lock = threading.Lock()
        self.imageRecv = b""
        self.isConnectLock = threading.Lock()
        self.timerLock = threading.Lock()
        self.timerEndLock = threading.Lock()
        self.timerInterval = 10  # 间隔十秒
        self.startTime = time.time()

        threading.Thread(target=self.timer_thread, args=(self.timerLock, self.timerEndLock, self.timerInterval,)).start()  # 开启接收数据线程
        threading.Thread(target=self.event_listener).start()  # 开启接收数据线程
        while True:
            self.isConnectLock.acquire()
            self.timerLock.acquire()  # 如果能获取定时器锁 则说明到达定时时间 间隔 self.timerInterval
            self.connect_TcpServer()

    def event_listener(self):
        while True:
            time.sleep(3600)
            nowTime = time.time()
            intervalTime = int((nowTime - self.startTime)/3600)   # 单位/小时
            if intervalTime > 3:    # 如果超过3个小时 则报警
                m = SendMail(
                    self.username,
                    self.passwd,
                    self.recv,
                    title='警报! 超%d小时未更新图片！' % intervalTime,
                    content='警报! 超%d小时未更新图片！请检查手机!' % intervalTime,
                    file=r'camera.jpeg',
                    ssl=True,
                )
                m.send_mail()

    def sock_init(self):
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_KEEPALIVE, 1)  # 保持连接状态
        sock.setblocking(True)  # 阻塞
        return sock

    # 定时线程
    def timer_thread(self, timerLock, timerEndLock, timeInterval):
        if timerLock.locked():
            timerLock.release()
        timerEndLock.acquire()
        while True:
            try:
                time.sleep(timeInterval)
                timerLock.release()
                if timerEndLock.acquire(False):  # 如果能获取定时结束锁 则定时线程结束
                    break
            except:  # 忽略重复释放锁错误
                pass

    def connect_TcpServer(self):
        try:
            self.sock = self.sock_init()
            self.sock.connect((self.IP, self.PORT))
        except:
            traceback.print_exc()
            print((datetime.datetime.now().strftime('%Y-%m-%d %H-%M-%S')), "服务器连接超时，请检查服务器")
            self.isConnectLock.release()
            self.timerLock.acquire()
            return

        print((datetime.datetime.now().strftime('%Y-%m-%d %H-%M-%S')), "TCP服务器连接成功")
        # 发送此命令 告知TCPServer此为图片识别服务器
        self.sock.send("$r#".encode())
        threading.Thread(target=self.socket_recvData_thread).start()  # 开启接收数据线程

    def save_Image(self, imgByte):
        with open("camera.jpeg", 'wb') as f:
            f.write(imgByte)

    def recognize_errorImage(self, imgByte):
        with open("recognize_error_img/%s.jpeg" % (datetime.datetime.now().strftime('%Y-%m-%d %H-%M-%S')), 'wb') as f:
            f.write(imgByte)

    def socket_recvData_thread(self):
        isStartRecvImage = False
        ImageRecvSum = 0
        while True:
            try:
                buff = self.sock.recv(1024)
                self.imageRecv = self.imageRecv + buff
                if len(buff) is not 0:
                    if not isStartRecvImage:    # 如果没开始接受图片
                        strBuff = self.imageRecv.decode(errors="ignore")
                        if strBuff[0] == 'j' and strBuff[1] == 'p' and strBuff[2] == 'e' and strBuff[3] == 'g':
                            if len(self.imageRecv) > 7:
                                ImageRecvSum = 0
                                ImageRecvSum += (self.imageRecv[4] << 16) & 0xff0000
                                ImageRecvSum += (self.imageRecv[5] << 8) & 0xff00
                                ImageRecvSum += (self.imageRecv[6]) & 0xff
                                isStartRecvImage = True
                            else:
                                continue
                        else:
                            self.imageRecv = b""
                    else:
                        # 收到图片
                        if len(self.imageRecv) - 7 >= ImageRecvSum: # 接收完毕
                            self.save_Image(self.imageRecv[7:])
                            self.startTime = time.time()
                            try:
                                lt_scale, rt_scale, lb_scale, rb_scale = recognition.recognition()
                                if lt_scale <= 2.5 and lb_scale <= 2.5:
                                    m = SendMail(
                                        self.username,
                                        self.passwd,
                                        self.recv,
                                        title='警报! 氧气不足！',
                                        content='警报! 氧气不足！请尽快更换新的氧气罐！',
                                        file=r'recognition_img/recognize.jpeg',
                                        ssl=True,
                                    )
                                    m.send_mail()
                            except:
                                m = SendMail(
                                    self.username,
                                    self.passwd,
                                    recv=['1632971113@qq.com'],
                                    title='识别错误！',
                                    content='识别错误！',
                                    file=r'camera.jpeg',
                                    ssl=True,
                                )
                                m.send_mail()
                                traceback.print_exc()
                                self.recognize_errorImage(self.imageRecv[7:])
                                print("识别出错")

                            self.imageRecv = b""
                            isStartRecvImage = False

                else:
                    break
            except Exception as e:
                traceback.print_exc()
                break
        print((datetime.datetime.now().strftime('%Y-%m-%d %H-%M-%S')), " 断开连接")
        self.sock.close()
        self.isConnectLock.release()

if __name__ == '__main__':
    mySocket = MySocket()